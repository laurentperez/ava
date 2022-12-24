package com.itk.ia.service

import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap

@ApplicationScoped
class OpenAIHeadersFactory : ClientHeadersFactory {

    @Inject
    @ConfigProperty(name = "client.openai.secret-key")
    lateinit var secretKey: String

    override fun update(
        incomingHeaders: MultivaluedMap<String, String>,
        clientOutgoingHeaders: MultivaluedMap<String, String>
    ): MultivaluedMap<String, String> {
        val result: MultivaluedMap<String, String> = MultivaluedHashMap()
        result.add("Authorization", secretKey)
        return result
    }
}
