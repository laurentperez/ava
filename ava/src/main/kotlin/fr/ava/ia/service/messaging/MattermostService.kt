package fr.ava.ia.service.messaging

import fr.ava.ia.model.Actor
import fr.ava.ia.model.Conversation
import fr.ava.ia.model.LastExchange
import fr.ava.ia.service.db.DbService
import fr.ava.ia.service.exception.OpenAIException
import fr.ava.ia.service.oai.OpenAIAssistants.Companion.ASSISTANT_PYTHON_CONSISE
import fr.ava.ia.service.oai.OpenAIAssistants.Companion.ASSISTANT_PYTHON_USING
import fr.ava.ia.service.oai.OpenAIService
import io.quarkus.runtime.StartupEvent
import jakarta.annotation.Priority
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import net.bis5.mattermost.client4.MattermostClient
import net.bis5.mattermost.model.ChannelType
import net.bis5.mattermost.model.Post
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.jboss.logging.Logger
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap


@ApplicationScoped
@Priority(1)
class MattermostService(
    private val dbService: DbService,
    private val mattermostClientHelper: MattermostClientHelper,
    private val mmClient: MattermostClient,
    private val quartz : Scheduler,
    @RestClient
    private var openAIService: OpenAIService,
    @ConfigProperty(name = "bot.mmost.pollingInterval")
    private val pollingInterval : Int,
) {

    private var logger = Logger.getLogger(this::class.java.name)

    private lateinit var _teamId : String
    private lateinit var _botUserId : String

    fun onStart(@Observes ev: StartupEvent?) {
        // CDI ARC reference https://marcelkliemannel.com/articles/2021/migrating-from-spring-to-quarkus/#the-container

        _teamId = mattermostClientHelper.getTeamId()
        _botUserId = mattermostClientHelper.getUserId()

        // introduce ourselves: post to home
        val home = mmClient.getChannelByName(mattermostClientHelper.getHome(), _teamId).readEntity()
        val post = Post()
        post.channelId = home.id
        post.message = "Hello ${home.name}, I'm a bot. Started at: ${humanDate(System.currentTimeMillis())}"
//        post.isPinned = true
        val r = mmClient.createPost(post).readEntity()

        logger.info("ready, my id is $_botUserId........................................")

        val ctx = hashMapOf<String, Any>()
        ctx["teamId"] = _teamId
        ctx["botUserId"] = _botUserId
        ctx["chatPartners"] = hashMapOf<String, String>() // TODO handle roles/quotas
        val job = JobBuilder.newJob(PostPollerJob::class.java)
            .withIdentity("poller", "chat")
            .usingJobData(JobDataMap(ctx))
            .build()
        val trigger: Trigger = TriggerBuilder.newTrigger()
            .withIdentity("pollerTrigger", "chat")
            .startNow()
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(pollingInterval)
                    .repeatForever()
            )
            .build()
        quartz.scheduleJob(job, trigger)

        // doPoll()
    }

    class PostPollerJob : Job {

        @Inject
        private lateinit var mmclient: MattermostClient
        @Inject
        private lateinit var dbService: DbService
        @Inject
        @RestClient
        private lateinit var openAIService: OpenAIService

        private var logger = Logger.getLogger(this::class.java.name)

        override fun execute(context: JobExecutionContext) {
            val teamId = context.mergedJobDataMap["teamId"] as String
            val botUserId = context.mergedJobDataMap["botUserId"] as String
            val chatPartners = context.mergedJobDataMap["chatPartners"] as HashMap<String, String>

        // hint : mock taskbean
        //fun doPoll() {
            //do {
                val mychannels = mmclient.getChannelsForTeamForUser(teamId, botUserId).readEntity()
                mychannels.forEach loopingchannels@ { chann ->
                    if (chann.type == ChannelType.Direct) {
                        val channelId = chann.id
                        val channelName = chann.name
                        logger.info("found direct channel $channelId $channelName, lasPostAt ${chann.lastPostat}, lastRootPost at ${chann.lastRootPostAt}")
                        val dmUsers = mmclient.getUsersInChannel(channelId).readEntity() // must be 2
                        dmUsers.forEach { user ->
                            run {
                                if (user.id != botUserId) {
                                    val userName = user.username
                                    logger.info("\uD83D\uDC91 possible partner: $userName, ${user.id}, ${user.isBot}")
                                    if(chatPartners[userName] == null) {
                                        // hello starter
                                        // FIXME try catch http ?
                                        val p = Post(channelId, "hello $userName (dm), please reply to this message to chat with me")
                                        val hello = mmclient.createPost(p).readEntity()
                                        dbService.saveLastExchange(LastExchange(user.id, userName, hello.id))
                                        logger.info("\uD83D\uDC91 set first lastExchange (hello) to ${hello.id}")
                                        chatPartners.putIfAbsent(user.username, user.id)
                                    }
                                }
                            }
                        } // eof dmUsers welcoming exchange
                        handlePosts(mmclient, dbService, openAIService, botUserId, channelId)
                    } else {
                        logger.warn("UNEXPECTED channel type ${chann.type} : ${chann.name}")
                    }
                }
                // partners are updated, posts updated
//                    sleep(8_000)
                logger.info("end of loop...........")
            //} while (true)
        //}
        }

        private fun handlePosts(client: MattermostClient, dbService: DbService, openAIService: OpenAIService,
                                botUserId: String,
                                channelId: String) : LastExchange? {
            // find the last exchange
            var lastExchange = dbService.findLastExchange()
            logger.info("⏳ polling for new posts after lastExchange ${lastExchange.postID}...............")
            val posts = client.getPostsAfter(channelId, lastExchange.postID).readEntity()
            // GET /api/v4/channels/cxwfr9m4s7byumbpk4k57atugo/posts?after=bsa16fkdubymby7rb7mnu9bfor&page=0&per_page=60
            // todo getPostThread ? since ? getFlaggedPostsForUser ?
            val order = posts.order
            logger.info("\uD83D\uDDE3️ found ${order.size} post(s) after lastExchange ${lastExchange.postID} : $order")
            if(order.isEmpty()) {
                return lastExchange
            }
            // posts may be either DM or DC, from user or bot : we filter below
            posts.posts.forEach loopingposts@{ (postId, post) ->
                val poster = post.userId
                // TODO à cause de update_at, le hello de départ revient > getPostsAfter
                // /!\ le prev_post_id est le hello et le root/parent
                // /!\ "order" a BIEN les réponses de thread et PAS l'id racine en order
                when {
                    poster == botUserId -> {
                        // this is: the conversational "hello" starter, or a bot reply
                        // "hello" can be returned because update_at is updated (?)
                        logger.warn("\uD83E\uDD16 ignoring lastExchange from bot........ for $post")
                        return@loopingposts
                    }
                    (post.rootId == "") && (post.parentId == "") -> {
                        // this is not a conversation but a message in direct channel.
                        // TODO handle control commands like !command
                        logger.warn("\uD83D\uDDE3️ ignoring lastExchange from ${post.userId} is not a thread ! $post")
                        return@loopingposts
                    }
                    (post.rootId != "") && (post.parentId != "") -> {
                        // this is a conversation
                        // filter out us (bot)
                        if(poster != botUserId) {
                            logger.info("found post after lastExchange ${lastExchange.id}: $postId // $post")
                            // only pick the latest 1:1 conversation part
                            val newestPost = order[0]
                            if(postId == newestPost) {
                                logger.info("post lastExchange postId == newestPost ! $newestPost")
                                // immediately set cursor delta:
                                // http may be slow to respond. we don't want next poll to step and replay from previous cursor.
                                lastExchange = LastExchange(post.userId, "?", post.id)
                                dbService.saveLastExchange(lastExchange)
                                logger.info("♻️ refresh lastExchange to $lastExchange")
                                // now kith
                                val request = Conversation(post.userId, postId, repliedTo = lastExchange.postID,
                                    post.rootId, Date.from(Instant.ofEpochMilli(post.createAt)), post.message, Actor.USER)
                                dbService.saveConversation(request)
                                val cRequest = OpenAIService.ChatRequest(
                                    messages = listOf(
                                        OpenAIService.ChatMessage("system", ASSISTANT_PYTHON_CONSISE),
                                        OpenAIService.ChatMessage("user", ASSISTANT_PYTHON_USING
                                                + post.message
                                        )
                                    )
                                )
                                // reply: either success or error
                                val reply : Post = try {
                                    val cResp = openAIService.getChatCompletions(cRequest).choices[0]
                                    val message = cResp.message.content
                                    client.createPost(Post(channelId, message)).readEntity()
                                } catch (oe: OpenAIException) {
                                    logger.error("\uD83D\uDCA5 oops, error!", oe)
                                    client.createPost(Post(channelId, "\uD83D\uDCA5 oops, error! ${oe.message}")).readEntity()
                                }
                                catch (e: Exception) {
                                    logger.error("\uD83D\uDCA5 oops, error!", e)
                                    client.createPost(Post(channelId, "\uD83D\uDCA5 oops, error! ${e.message}")).readEntity()
                                }
                                val response = Conversation(reply.userId, reply.id, repliedTo = postId,
                                    reply.rootId, Date.from(Instant.ofEpochMilli(reply.createAt)), reply.message, Actor.ASSISTANT)
                                dbService.saveConversation(response)
                                return lastExchange
                            }
                        }
                    }
                }
                logger.info(post.message)
            }
            return null
        }

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



    fun humanDate(epoch : Long) : String {
        return SimpleDateFormat("MMM dd,yyyy HH:mm").format(Date(epoch))
    }

}
