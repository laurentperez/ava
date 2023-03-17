package fr.ava.ia.service.messaging

import fr.ava.ia.model.Actor
import fr.ava.ia.model.Conversation
import fr.ava.ia.service.oai.OpenAIService
import fr.ava.ia.service.db.DbService
import fr.ava.ia.service.oai.OpenAIAssistants.Companion.ASSISTANT_PYTHON_CONSISE
import fr.ava.ia.service.oai.OpenAIAssistants.Companion.ASSISTANT_PYTHON_USING
import io.quarkus.runtime.StartupEvent
import io.quarkus.scheduler.Scheduler
import jakarta.annotation.Priority
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import net.bis5.mattermost.client4.MattermostClient
import net.bis5.mattermost.model.ChannelType
import net.bis5.mattermost.model.Post
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.jboss.logging.Logger
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*


@ApplicationScoped
@Priority(1)
class MattermostService(
    private val dbService: DbService,
    private val mattermostClientHelper: MattermostClientHelper,
    private val mmClient: MattermostClient,
    private val quartz : Scheduler,
    @RestClient
    private var openAIService: OpenAIService
) {

    private var logger = Logger.getLogger(this::class.java.name)

    private lateinit var mmclient : MattermostClient
    private lateinit var _teamId : String
    private lateinit var _botUserId : String

    fun onStart(@Observes ev: StartupEvent?) {
        // CDI ARC reference https://marcelkliemannel.com/articles/2021/migrating-from-spring-to-quarkus/#the-container

        _teamId = mattermostClientHelper.getTeamId()
        _botUserId = mattermostClientHelper.getUserId()

        // introduce ourselves: post to home
        val home = mmclient.getChannelByName(mattermostClientHelper.getHome(), _teamId).readEntity()
        val post = Post()
        post.channelId = home.id
        val now = humanDate(System.currentTimeMillis())
        post.message = "Hello ${home.name}, I'm a bot. Started at: $now"
//        post.isPinned = true
        val r = mmclient.createPost(post).readEntity()

        println("ready, my id is $_botUserId........................................")

        // TODO quartz : job can lookup or receive datamap

        doPoll()
    }

    fun init() {

//        val iclient = IncomingWebhookClient(serverUrl)
//        val payload = IncomingWebhookRequest()
//        payload.text = "Hello World!"
//        payload.username = "Override Username"
//        iclient.postByIncomingWebhook(payload);
//        val mmClient = MattermostClient(serverUrl)
//        mmClient.login(login, password)
//        mmClient.getChannel("/test/channels/town-square").readEntity()

        // mmClient.createPost()
        // client.updateUserRoles("ava", Role.TEAM_USER)
        // val config = client.config.readEntity()
        //config.serviceSettings.isEnableCommands = true
        //client.updateConfig(config).readEntity()

        // client.setAccessToken(token);
        // client.getChannelsForTeamRoute("test")

        // client.getChannelsForTeamRoute("test")
        // val email = client.getUser("ava@ava.co").readEntity().email
        // val channelList = client.getChannelsForTeamForUser("test", "ava@ava.foo").readEntity()
//        val r = client.getTeamByNameRoute("test")
//        val channelsForTeamRoute = client.getChannelsForTeamRoute(tid)

       // val pid = r.id
//        sleep(30_000)

//        val plist = client.getPostThread(pid, null).readEntity()
//        logger.info("" + plist)

        // perform then reply in direct channel
        // OR : get my dms and mentions, then reply in dm
        // remember direct channel is between 2 users (id the couples, id the channels)
        // https://api.mattermost.com/#tag/channels/operation/CreateDirectChannel
        // my channels : https://api.mattermost.com/#tag/channels/operation/GetChannelsForTeamForUser
        // (just like users do)

//        val direct = client.createDirectChannel("", "")
//        val directid = direct.readEntity().id

        // when it logs post offline :
        // Get the total unread messages and mentions for a channel for a user.

    }

    fun doPoll() {
        val chatPartners = hashMapOf<String, String>()
        var lastExchange : Post? = null

        do {
            val mychannels = mmclient.getChannelsForTeamForUser(_teamId, _botUserId).readEntity()
            mychannels.forEach loopingchannels@ { chann ->
            if (chann.type == ChannelType.Direct) {
                val channelId = chann.id
                val channelName = chann.name
                logger.info("found direct channel $channelId $channelName, lasPostAt ${chann.lastPostat}, lastRootPost at ${chann.lastRootPostAt}")
                val dmUsers = mmclient.getUsersInChannel(channelId).readEntity() // must be 2
                dmUsers.forEach { user ->
                    run {
                        if (user.id != _botUserId) {
                            val userName = user.username
                            logger.info("\uD83D\uDC91 possible partner: $userName, ${user.id}, ${user.isBot}")
                            if(chatPartners[userName] == null) {
                                val post = Post(channelId, "hello $userName (dm), please reply to this message to chat with me")
                                lastExchange = mmclient.createPost(post).readEntity() // FIXME try catch ?
                                logger.info("\uD83D\uDC91 set first lastExchange to $lastExchange") // the "hello"
                                chatPartners.putIfAbsent(user.username, user.id)
                            }
                        }
                    }
                } // eof dmUsers welcoming exchange
                val ack = handlePosts(mmclient, dbService, channelId, lastExchange)
                if(ack == null) return@loopingchannels
                else {
                    lastExchange = ack
                }
            } else {
                    logger.warn("UNEXPECTED channel type ${chann.type} : ${chann.name}")
                }
            }
            // partners are updated, posts updated
            sleep(8_000)
            logger.info("end of loop...........")
        } while (true)
    }

    private fun handlePosts(client: MattermostClient, dbService: DbService, channelId: String, lastExchange: Post?) : Post? {
        logger.info("⏳ polling for new posts after lastExchange ${lastExchange!!.id}...............")
        val posts = client.getPostsAfter(channelId, lastExchange.id).readEntity()
        // GET /api/v4/channels/cxwfr9m4s7byumbpk4k57atugo/posts?after=bsa16fkdubymby7rb7mnu9bfor&page=0&per_page=60
        // todo getPostThread ? since ? getFlaggedPostsForUser ?
        val order = posts.order
        logger.info("\uD83D\uDDE3️ found ${order.size} post(s) after lastExchange ${lastExchange.id} : $order")
        if(order.isEmpty()) {
            return lastExchange
        }
        // posts may be either DM or DC : we filter below
        posts.posts.forEach loopingposts@{ (postId, post) ->
            val poster = post.userId
            // TODO à cause de update_at, le hello de départ revient > getPostsAfter
            // /!\ le prev_post_id est le hello et le root/parent
            // /!\ "order" a BIEN les réponses de thread et PAS l'id racine en order
            when {
                poster == _botUserId -> {
                    // this is: the conversational "hello" starter, or a bot reply
                    // "hello" can be returned because update_at is updated (?)
                    logger.warn("\uD83E\uDD16 ignoring lastExchange from bot........ for $post")
                    return@loopingposts
                }
                (post.rootId == "") && (post.parentId == "") -> {
                    // this is not a conversation but a user message in direct channel. TODO handle ?
                    // ours has replyCount, handle ?
                    logger.warn("\uD83D\uDDE3️ ignoring lastExchange from ${post.userId} is not a thread ! $post")
                    return@loopingposts
                }
                (post.rootId != "") && (post.parentId != "") -> {
                    // this is a conversation
                    // filter out us (bot)
                    if(poster != _botUserId) {
                        logger.info("post after lastExchange ${lastExchange.id}: $postId // $post")
                        // only pick the latest 1:1 conversation part
                        val newestPost = order[0]
                        if(postId == newestPost) {
                            logger.info("post lastExchange postId == newestPost ! $newestPost")
                            // now kith
                            val request = Conversation(post.userId, postId, lastExchange.id,
                                post.rootId, Date.from(Instant.ofEpochMilli(post.createAt)), post.message, Actor.USER)
                            dbService.saveConversation(request)
                            val cRequest = OpenAIService.ChatRequest(
                                messages = listOf(
                                    OpenAIService.ChatMessage("system", ASSISTANT_PYTHON_CONSISE),
                                    OpenAIService.ChatMessage("user", ASSISTANT_PYTHON_USING
                                            + post.message
                                    )
                                )
                                // todo on subsequent calls, drop "using"
                            )
                            // reply: either success or error
                            val reply : Post = try {
                                val cResp = openAIService.getChatCompletions(cRequest).choices[0]
                                val message = cResp.message.content
                                // val role = message.role // ?
                                client.createPost(Post(channelId, message)).readEntity()
                            } catch (e: Exception) {
                                logger.error("oops, error!", e)
                                client.createPost(Post(channelId, "oops, error! ${e.message}")).readEntity()
                            }
                            val response = Conversation(reply.userId, reply.id, postId,
                                reply.rootId, Date.from(Instant.ofEpochMilli(reply.createAt)), reply.message, Actor.ASSISTANT)
                            dbService.saveConversation(response)
                            // cursor
                            logger.info("♻️ refresh lastExchange to $post")
                            return post
                        }
                    }
                }
            }
            logger.info(post.message)
        }
        return null
    }

    fun humanDate(epoch : Long) : String {
        return SimpleDateFormat("MMM dd,yyyy HH:mm").format(Date(epoch))
    }

}
