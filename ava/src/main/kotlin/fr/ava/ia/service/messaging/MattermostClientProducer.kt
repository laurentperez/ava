package fr.ava.ia.service.messaging

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.ws.rs.client.ClientBuilder
import net.bis5.mattermost.client4.MattermostClient
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.concurrent.TimeUnit

@ApplicationScoped
class MattermostClientProducer(
    @ConfigProperty(name = "bot.mmost.enabled")
    private val enabled : Boolean,
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
            //.httpConfig { it.connectTimeout(1, TimeUnit.SECONDS) }
            .build()
        var serverOk = false
        if(enabled) serverOk = !mmClient.login(login, password).checkStatusOk().hasError()
        if(!serverOk) throw IllegalStateException("chat server is not ready ! check bot login status")
        return mmClient
    }
}
