package fr.ava.ia.service

import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.ws.rs.client.ClientBuilder
import net.bis5.mattermost.client4.MattermostClient
import net.bis5.mattermost.client4.hook.IncomingWebhookClient
import net.bis5.mattermost.model.IncomingWebhookRequest
import net.bis5.mattermost.model.Post
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.glassfish.jersey.logging.LoggingFeature
import java.util.function.Consumer
import java.util.logging.Level
import java.util.logging.Logger


@ApplicationScoped
class MattermostService(
    @ConfigProperty(name = "bot.chat.serverUrl")
    private val serverUrl : String,
    @ConfigProperty(name = "bot.chat.login")
    private val login : String,
    @ConfigProperty(name = "bot.chat.password")
    private val password : String
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
            .httpConfig(Consumer<ClientBuilder?> { it.register(
                LoggingFeature(logger, Level.ALL,
                    LoggingFeature.Verbosity.PAYLOAD_ANY, 100000)) }
            )
//            .httpConfig(
//                b -> b.register(new LoggingFeature(Logger.getLogger(MattermostClient.class.getName()), Level.INFO,
//                        LoggingFeature.Verbosity.PAYLOAD_ANY, 100000)))
            .logLevel(Level.ALL)
            //.ignoreUnknownProperties()
            .build()
            client.login(login, password)

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

        val post = Post()
        // client.getChannelsForTeamRoute("test")
        // val email = client.getUser("ava@ava.foo").readEntity().email
        // val channelList = client.getChannelsForTeamForUser("test", "ava@ava.foo").readEntity()
//        val r = client.getTeamByNameRoute("test")
        val t = client.getTeamByName("test").readEntity()
        val tid = t.id
        val channelsForTeamRoute = client.getChannelsForTeamRoute(tid)
        val channelByName = client.getChannelByName("town-square", tid).readEntity()
        post.channelId = channelByName.id
        post.message = "message_" + System.currentTimeMillis()
        // client.createPost()
        client.createPost(post)
    }


}