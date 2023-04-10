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
import jakarta.persistence.NoResultException
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


@ApplicationScoped
@Priority(1)
class MattermostService(
    private val mattermostClientHelper: MattermostClientHelper,
    private val mmClient: MattermostClient,
    private val quartz : Scheduler,
    @ConfigProperty(name = "bot.mmost.enabled")
    private val enabled : Boolean,
    @ConfigProperty(name = "bot.mmost.pollingInterval")
    private val pollingInterval : Int,
) {

    private var logger = Logger.getLogger(this::class.java.name)

    private lateinit var teamId : String
    private lateinit var botUserId : String

    fun onStart(@Observes ev: StartupEvent?) {
        if(!enabled) return
        // CDI ARC reference https://marcelkliemannel.com/articles/2021/migrating-from-spring-to-quarkus/#the-container

        teamId = mattermostClientHelper.getTeamId()
        botUserId = mattermostClientHelper.getUserId()

        // introduce ourselves: post to home
        val home = mmClient.getChannelByName(mattermostClientHelper.getHome(), teamId).readEntity()
        val post = Post()
        post.channelId = home.id
        post.message = "Hello ${home.name}, I'm a bot. Started at: ${humanDate(System.currentTimeMillis())}"
//        post.isPinned = true
        val r = mmClient.createPost(post).readEntity()

        logger.info("ready, my id is $botUserId........................................")

        val ctx = hashMapOf<String, Any>()
        ctx["teamId"] = teamId
        ctx["botUserId"] = botUserId
        // ctx["chatPartners"] = hashMapOf<String, String>() // TODO handle roles/quotas
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

        private var logger = Logger.getLogger(this::class.java.name)

        @Inject
        lateinit var mmclient: MattermostClient
        @Inject
        lateinit var dbService: DbService
        @Inject
        @RestClient
        lateinit var openAIService: OpenAIService


        override fun execute(context: JobExecutionContext) {
            val teamId = context.mergedJobDataMap["teamId"] as String
            val botUserId = context.mergedJobDataMap["botUserId"] as String

        // hint : mock taskbean
        //fun doPoll() {
            //do {
                val mychannels = mmclient.getChannelsForTeamForUser(teamId, botUserId).readEntity()
                mychannels.forEach loopingchannels@ { chann ->
                    if (chann.type == ChannelType.Direct) {
                        val channelId = chann.id
                        val channelName = chann.name
                        logger.info("found direct channel $channelId $channelName, lasPostAt ${chann.lastPostat}, lastRootPost at ${chann.lastRootPostAt}")
                        val dmUsers = mmclient.getUsersInChannel(channelId).readEntity()
                            .filter { !it.isBot && it.id != botUserId } // must be pairs. avoid ourselves + surveybot, advisor
                        dmUsers.forEach { user ->
                        run {
                            val userName = user.username
                            val userId = user.id
                            logger.info("\uD83D\uDC91 possible partner: $userName, $userId, bot?:${user.isBot}")
                            val u = try {
                                    dbService.findUserById(user.id)
                                } catch (nre: NoResultException) {
                                    // not found before => save then hello starter
                                    dbService.saveUser(userId, userName)
                                    val p = Post(channelId, "hello $userName, please reply to this message using a Direct Message to chat with me")
                                    val hello = mmclient.createPost(p).readEntity()
                                    // FIXME try catch @Retry http ?
                                    dbService.saveLastExchange(LastExchange(userId, userName, hello.id))
                                    logger.info("\uD83D\uDC91 set first lastExchange (hello) with $userName to ${hello.id}")
                                }
                                handlePosts(mmclient, dbService, openAIService, botUserId, channelId)
                            }
                        } // eof dmUsers hello exchange
                    } else {
                        logger.warn("UNEXPECTED channel type ${chann.type} : ${chann.name}")
                    }
                }
                // partners are updated, posts updated
                logger.info("end of loop...........")
            //} while (true)
        //}
        }

        private fun handlePosts(client: MattermostClient, dbService: DbService, openAIService: OpenAIService,
                                botUserId: String,
                                channelId: String) : LastExchange? {
            // find the last exchange
            // TODO : dequeue this for each saved hello'ed user.
            // spawn vertices and fork join per user ?
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
                        // this is: the conversational "hello" starter, or a bot (ourselves) reply
                        // "hello" can be returned because update_at is updated (?)
                        logger.warn("\uD83E\uDD16 ignoring lastExchange from bot........ for $post")
                        return@loopingposts
                    }
                    (post.rootId == "") && (post.parentId == "") -> {
                        // this is not a conversation but a message in direct channel.
                        // TODO handle control commands like !command
                        logger.warn("\uD83D\uDDE3️ ignoring lastExchange from $poster is not a thread ! $post")
                        return@loopingposts
                    }
                    (post.rootId != "") && (post.parentId != "") -> {
                        // this is a user conversation
                        // filter out us (bot)
                        if(poster != botUserId) {
                            logger.info("found post after lastExchange ${lastExchange.id}: $postId // $post")
                            // only pick the latest 1:1 conversation part
                            val newestPost = order[0]
                            if(postId == newestPost) {
                                logger.info("post lastExchange postId == newestPost ! $newestPost")
                                // immediately set cursor delta:
                                // http may be slow to respond. we don't want next poll to step into, and replay from previous cursor.
                                lastExchange = LastExchange(poster, "?", post.id) // TODO FIXME withusername
                                dbService.saveLastExchange(lastExchange)
                                logger.info("♻️ refresh lastExchange to $lastExchange")
                                // now kith
                                val rootID = post.rootId
                                val request = Conversation(poster, postId, repliedTo = lastExchange.postID,
                                    rootID, Date.from(Instant.ofEpochMilli(post.createAt)), post.message, Actor.USER)
                                dbService.saveConversation(request)
                                val cu = dbService.findConversationWithUserWithRootId(poster, rootID)
                                // TODO aggregate : https://platform.openai.com/docs/guides/chat/introduction
                                // TODO bind root!id of ASSISTANT response, active
                                val messages = listOf(
                                    OpenAIService.ChatMessage(role = "system", content = ASSISTANT_PYTHON_CONSISE),
                                    OpenAIService.ChatMessage(
                                        role = "user", content = ASSISTANT_PYTHON_USING
                                                + post.message
                                    ))
                                val cRequest = OpenAIService.ChatRequest(
                                    // model = "gpt-4", // TODO match preferred usermodel
                                    messages = messages
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
                                    logger.error("\uD83D\uDCA5 oops, generic error!", e)
                                    client.createPost(Post(channelId, "\uD83D\uDCA5 oops, generic error! ${e.message}")).readEntity()
                                }
                                // FIXME: why was reply.rootid="" ? because reply post is not in thread. use rootID instead
                                val response = Conversation(reply.userId, reply.id, repliedTo = postId,
                                    rootID, Date.from(Instant.ofEpochMilli(reply.createAt)), reply.message, Actor.ASSISTANT)
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
