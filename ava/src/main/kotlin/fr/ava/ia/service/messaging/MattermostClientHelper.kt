package fr.ava.ia.service.messaging

import io.quarkus.runtime.StartupEvent
import jakarta.annotation.Priority
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import net.bis5.mattermost.client4.MattermostClient
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.lang.Thread.sleep

@ApplicationScoped
@Priority(999)
class MattermostClientHelper(
    @ConfigProperty(name = "bot.chat.serverUrl")
    private val serverUrl : String,
    @ConfigProperty(name = "bot.chat.login")
    private val login : String,
    @ConfigProperty(name = "bot.chat.password")
    private val password : String,
    @ConfigProperty(name = "bot.chat.team")
    private val team : String,
    @ConfigProperty(name = "bot.chat.home")
    private val home : String,
) {

    private lateinit var mmClient : MattermostClient

    fun onStart(@Observes ev: StartupEvent?) {
        mmClient = MattermostClient.builder()
            .url(serverUrl)
            .ignoreUnknownProperties()
            .build()
        mmClient.login(login, password)
        sleep(6_000)
    }

    fun getClient() : MattermostClient {
        return mmClient
    }

    fun getHome() : String {
        return home
    }

    fun getTeamId() : String {
        return mmClient.getTeamByName(team).readEntity().id
    }
    fun getUserId() : String {
        return mmClient.getUserByEmail(login).readEntity().id
    }
}
