package fr.ava.ia

import fr.ava.ia.service.OpenAIService
import org.eclipse.microprofile.rest.client.inject.RestClient
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/prompt")
class CodeResource {

    @Inject
    @RestClient
    lateinit var openAIService: OpenAIService

    @GET
    fun hello(): String {
        return openAIService.getModels().toString()
        // return openAI.generateCompletion()
    }
}
