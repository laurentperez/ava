package fr.ava.ia.service.messaging

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import net.bis5.mattermost.client4.MattermostClient
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class MattermostClientProducer(
    @ConfigProperty(name = "bot.mmost.serverUrl")
    private val serverUrl : String,
    @ConfigProperty(name = "bot.mmost.login")
    private val login : String,
    @ConfigProperty(name = "bot.mmost.password")
    private val password : String,
) {

    @Produces
    fun mmClient() : MattermostClient {
        val mmClient = MattermostClient.builder()
            .url(serverUrl)
            .ignoreUnknownProperties()
            .build()
        mmClient.login(login, password)
        return mmClient
    }
}
