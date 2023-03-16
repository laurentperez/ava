package fr.ava.ia.service.messaging

import fr.ava.ia.model.Actor
import fr.ava.ia.model.Conversation
import fr.ava.ia.service.ai.OpenAIService
import fr.ava.ia.service.db.DbService
import io.quarkus.runtime.StartupEvent
import io.quarkus.scheduler.Scheduler
import jakarta.annotation.Priority
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import net.bis5.mattermost.client4.MattermostClient
import net.bis5.mattermost.model.ChannelType
import net.bis5.mattermost.model.Post
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.logging.Logger


@ApplicationScoped
@Priority(199)
class MattermostService(
    private val dbService: DbService,
    private val mattermostClientHelper: MattermostClientHelper,
    private val quartz : Scheduler,
    @RestClient
    private var openAIService: OpenAIService
) {

    private var logger = Logger.getLogger(this::class.java.name)

    private lateinit var _client : MattermostClient
    private lateinit var _teamId : String
    private lateinit var _botUserId : String

    fun onStart(@Observes ev: StartupEvent?) {
        // CDI ARC reference https://marcelkliemannel.com/articles/2021/migrating-from-spring-to-quarkus/#the-container

        _client = mattermostClientHelper.getClient()
        _teamId = mattermostClientHelper.getTeamId()
        _botUserId = mattermostClientHelper.getUserId()

        // introduce ourselves: post to home
        val home = _client.getChannelByName(mattermostClientHelper.getHome(), _teamId).readEntity()
        val post = Post()
        post.channelId = home.id
        val now = humanDate(System.currentTimeMillis())
        post.message = "Hello ${home.name}, I'm a bot. Started at: $now"
//        post.isPinned = true
        val r = _client.createPost(post).readEntity()

        println("ready, my id is $_botUserId........................................")

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

        // TODO loop schedule this poller

        // client.getChannelsForTeamForUser(tid, "")

        // TODO : get requests from the thread,
        //  identify each user & demand
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
            val mychannels = _client.getChannelsForTeamForUser(_teamId, _botUserId).readEntity()
            mychannels.forEach loopingchannels@ { chann ->
            if (chann.type == ChannelType.Direct) {
                logger.info("found direct channel $chann")
//                    val dchan = client.getChannel(chann.id).readEntity()
                val dmUsers = _client.getUsersInChannel(chann.id).readEntity() // must be 2
                dmUsers.forEach { user ->
                    run {
                        if (user.id != _botUserId) {
                            logger.info("possible partner: $user, ${user.username}")
                            if(chatPartners[user.username] == null) {
                                val post = Post(chann.id, "hello ${user.username} (dm), please reply to this thread to chat with me")
                                lastExchange = _client.createPost(post).readEntity()
                                logger.info("set first lastExchange to $lastExchange") // the "hello"
                                chatPartners.putIfAbsent(user.username, user.id)
                            }
                        }
                    }
                }
                // after hello
                logger.info("polling for new posts after lastExchange ${lastExchange!!.id}...............")
                val dposts = _client.getPostsAfter(chann.id, lastExchange!!.id).readEntity()
                // GET /api/v4/channels/cxwfr9m4s7byumbpk4k57atugo/posts?after=bsa16fkdubymby7rb7mnu9bfor&page=0&per_page=60
                // todo getPostThread ? since ? getFlaggedPostsForUser ?
                val order = dposts.order
                if(order.isEmpty()) {
                    return@loopingchannels
                }
                logger.info("found ${order.size} post(s) after lastExchange ${lastExchange!!.id} : $order")
                dposts.posts.forEach loopingposts@{ (postId, dpost) ->
                    val poster = dpost.userId
                    // TODO à cause de update_at, le hello de départ revient > getPostsAfter ?
                    // /!\ le prev_post_id est le hello et le root/parent
                    // /!\ "order" a BIEN les réponses de thread et PAS l'id racine en order
                    when {
                        poster == _botUserId -> {
                            // this is the conversation "hello" starter
                            // this is returned because update_at is updated (?)
                            logger.warning("\uD83E\uDD16 ignoring lastExchange from bot........")
                            return@loopingposts
                        }
                        dpost.rootId == "" && dpost.parentId == "" -> {
                            // this is not a conversation. TODO handle ?
                            // ours has replyCount, handle ?
                            logger.warning("\uD83E\uDD16 lastExchange not a thread ! $dpost")
                            return@loopingposts
                        }
                        dpost.rootId != "" && dpost.parentId != "" -> {
                            // this is a conversation
                            // filter out us (bot)
                            if(poster != _botUserId) {
                                logger.info("dpost after lastExchange ${lastExchange!!.id}: $postId // $dpost")
                                // only pick the latest 1:1 conversation part
                                //
                                val newestPost = order[0]
                                if(dpost.id == newestPost) {
                                    val conversation = Conversation(dpost.userId, dpost.id,
                                        dpost.rootId, Date.from(Instant.ofEpochMilli(dpost.createAt)), dpost.message, Actor.USER)
                                    dbService.saveConversation(conversation)
                                    lastExchange = dpost
                                    logger.info("refresh lastExchange to $lastExchange")
                                }
                            }
                        }
                    }
                    logger.info(dpost.message)
                    // val m = openAIService.getModels().toString()
                    // _client.createPost(Post(chann.id, m)).readEntity()

                }

//                    println(dposts)
            } else {
                logger.warning("UNEXPECTED channel type ${chann.type} : ${chann.name}")
                }
            }
            // partners are updated, posts updated
            sleep(8_000)
            logger.info("end of loop...........")
        } while (true)
    }

    fun humanDate(epoch : Long) : String {
        return SimpleDateFormat("MMM dd,yyyy HH:mm").format(Date(epoch))
    }

}
