package fr.ava.ia.service

import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
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

    var logger = Logger.getLogger(this::class.java.name)

    fun onStart(@Observes ev: StartupEvent?) {
        init()
    }

    fun init() {
        logger.warning("xxxxxxxxxxxxxxxxxxxxxxx")
        println("boo**********matter")
        val client = MattermostClient.builder()
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
            client.login(login, password)
        logger.warning("yyyyyyyyyyyyyyyyyyyyyyyy")

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
        // val email = client.getUser("ava@ava.foo").readEntity().email
        // val channelList = client.getChannelsForTeamForUser("test", "ava@ava.foo").readEntity()
//        val r = client.getTeamByNameRoute("test")
//        val channelsForTeamRoute = client.getChannelsForTeamRoute(tid)
        // init, post to town square
        val team = client.getTeamByName("test").readEntity()
        val teamid = team.id
        val channelByName = client.getChannelByName("town-square", teamid).readEntity()
        val post = Post()
        post.channelId = channelByName.id
        post.message = "Hello, ts : " + System.currentTimeMillis()
//        post.isPinned = true
        val r = client.createPost(post).readEntity()
        val userId = r.userId
        val pid = r.id
//        sleep(30_000)

//        val plist = client.getPostThread(pid, null).readEntity()
//        logger.info("" + plist)

        // TODO loop schedule this poller
        val mychannels = client.getChannelsForTeamForUser(teamid, userId).readEntity()
        mychannels.forEach { c ->
            run {
                if (c.type == ChannelType.Direct) {
                    logger.info("found direct channel $c")
//                    val dchan = client.getChannel(c.id).readEntity()
                    val post = Post(c.id, "(dm)")
                    val rdm = client.createPost(post).readEntity()
                    val dusers = client.getUsersInChannel(c.id).readEntity()
                    dusers.forEach { user ->
                        run {
                            if (user.id != userId) {
                                logger.info("partner: $user, ${user.username}")
                            }
                        }
                    }
                    sleep(30_000)
                    val dposts = client.getPostsAfter(c.id, rdm.id).readEntity()
                    dposts.posts.forEach { (t, dpost) ->
                            logger.info(t)
                            // post.userId
                            logger.info(dpost.message)
                            val m = openAIService.getModels().toString()
                            client.createPost(Post(c.id, m)).readEntity()
                    }
//                    println(dposts)
                }
            }
        }
        println(mychannels)
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


}
