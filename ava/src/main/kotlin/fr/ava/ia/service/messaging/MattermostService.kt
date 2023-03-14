package fr.ava.ia.service.messaging

import fr.ava.ia.service.ai.OpenAIService
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import net.bis5.mattermost.client4.MattermostClient
import net.bis5.mattermost.model.ChannelType
import net.bis5.mattermost.model.Post
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.lang.Thread.sleep
import java.util.logging.Logger


@ApplicationScoped
class MattermostService(
    @ConfigProperty(name = "bot.chat.serverUrl")
    private val serverUrl : String,
    @ConfigProperty(name = "bot.chat.login")
    private val login : String,
    @ConfigProperty(name = "bot.chat.password")
    private val password : String,
    @RestClient
    private var openAIService: OpenAIService
) {

    private var logger = Logger.getLogger(this::class.java.name)

    private lateinit var _client : MattermostClient
    private lateinit var _teamId : String
    private lateinit var _botUserId : String

    fun onStart(@Observes ev: StartupEvent?) {
        // init()
        logger.warning("xxxxxxxxxxxxxxxxxxxxxxx")
        _client = MattermostClient.builder()
            .url(serverUrl)
//            .httpConfig(Consumer<ClientBuilder?> { it.register(
//                LoggingFeature(logger, Level.ALL,
//                    LoggingFeature.Verbosity.PAYLOAD_ANY, 100000)) }
//            )
//            .httpConfig(
//                b -> b.register(new LoggingFeature(Logger.getLogger(MattermostClient.class.getName()), Level.INFO,
//                        LoggingFeature.Verbosity.PAYLOAD_ANY, 100000)))
            // .logLevel(Level.ALL)
            .ignoreUnknownProperties()
            .build()
        _client.login(login, password)
        // introduce ourselves : post to town square
        val team = _client.getTeamByName("test").readEntity()
        _teamId = team.id
        val channelByName = _client.getChannelByName("town-square", _teamId).readEntity()
        val post = Post()
        post.channelId = channelByName.id
        post.message = "Hello, I'm a bot. ts : " + System.currentTimeMillis()
//        post.isPinned = true
        val r = _client.createPost(post).readEntity()
        _botUserId = r.userId

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
            mychannels.forEach { chann ->
                if (chann.type == ChannelType.Direct) {
                    logger.info("found direct channel $chann")
//                    val dchan = client.getChannel(chann.id).readEntity()
                    val dmUsers = _client.getUsersInChannel(chann.id).readEntity() // must be 2
                    dmUsers.forEach { user ->
                        run {
                            if (user.id != _botUserId) {
                                logger.info("possible partner: $user, ${user.username}")
                                if(chatPartners[user.username] == null) {
                                    val post = Post(chann.id, "hello ${user.username} (dm)")
                                    lastExchange = _client.createPost(post).readEntity()
                                    chatPartners.putIfAbsent(user.username, user.id)
                                }
                            }
                        }
                    }
                    // after hello
                    // TODO last hello flag of user
                    val dposts = _client.getPostsAfter(chann.id, lastExchange!!.id).readEntity()
                    dposts.order // TODO !!!!!! last unroll
                    dposts.posts.forEach { (t, dpost) ->
                        logger.info(t)
                        val postId = dpost.id
                        val poster = dpost.userId
                        if(poster == _botUserId && postId == lastExchange!!.id) {
                            // this is my hello / last message
                        }
                        // TODO à cause de update_At/"order", le hello de départ revient > getPostsAfter ?
                        // /!\ le prev_post_id est le hello et le root/parent
                        // /!\ "order" a BIEN les réponses de thread et PAS l'id racine en order
                        if(dpost.rootId == "" && dpost.parentId == "") {
                            // this is not a thread but a dm. TODO handle ?
                        }
                        if(chatPartners.containsValue(poster)) {
                            // this is a partner
                            // TODO find who
                            // logger.info("chatting with " + chatPartners.getValue(dpost.userId))
                        }
                        logger.info(dpost.message)
                        // TODO rootId parentId of post when in a thread
                        // val m = openAIService.getModels().toString()
                        // _client.createPost(Post(chann.id, m)).readEntity()

                    }
//                    println(dposts)
                } else {
                    logger.warning("UNEXPECTED channel type ${chann.type}")
                }
            }
            sleep(8_000)
            println(mychannels)
            println("end of loop...........")
        } while (1>0)
    }


}
