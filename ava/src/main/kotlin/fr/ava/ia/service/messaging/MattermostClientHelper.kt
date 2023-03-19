package fr.ava.ia.service.messaging

import io.quarkus.runtime.StartupEvent
import jakarta.annotation.Priority
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import net.bis5.mattermost.client4.MattermostClient
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
@Priority(999)
class MattermostClientHelper(
    @ConfigProperty(name = "bot.mmost.team")
    private val team : String,
    @ConfigProperty(name = "bot.mmost.home")
    private val home : String,
    @ConfigProperty(name = "bot.mmost.login")
    private val login : String,
    private val mmClient : MattermostClient
) {

    fun onStart(@Observes ev: StartupEvent?) {

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
