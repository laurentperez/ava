package fr.ava.ia.service.oai

import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.MultivaluedHashMap
import jakarta.ws.rs.core.MultivaluedMap

@ApplicationScoped
class OpenAIHeadersFactory : ClientHeadersFactory {

    @Inject
    @ConfigProperty(name = "client.oai.secret-key")
    private lateinit var secretKey: String

    override fun update(
        incomingHeaders: MultivaluedMap<String, String>,
        clientOutgoingHeaders: MultivaluedMap<String, String>
    ): MultivaluedMap<String, String> {
        val result: MultivaluedMap<String, String> = MultivaluedHashMap()
        result.add("Authorization", secretKey)
        return result
    }
}
