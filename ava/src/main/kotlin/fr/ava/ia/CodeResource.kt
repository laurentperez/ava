package fr.ava.ia

import com.fasterxml.jackson.databind.ObjectMapper
import fr.ava.ia.service.oai.OpenAIService
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import org.eclipse.microprofile.rest.client.inject.RestClient

@Path("/oai")
class CodeResource {

    @Inject
    @RestClient
    lateinit var openAIService: OpenAIService

    @GET
    fun models(): OpenAIService.Models {
        return openAIService.getModels()
    }

    @GET
    @Path("/chat")
    fun chat(): OpenAIService.ChatResponse {
        val r = OpenAIService.ChatRequest(
            messages = listOf(
                OpenAIService.ChatMessage("system", "you are a code assistant"),
                OpenAIService.ChatMessage("user", "using python, write an hello world")
            )
        )
        return openAIService.getChatCompletions(r)
    }

    @GET
    @Path("/completion")
    fun completion(): OpenAIService.CompletionResponse {
        val p : String = "\"\"\"python language, connect to rabbitmq server at url of 10.0.0.1\n\"\"\""
        val payload: String = ObjectMapper().writeValueAsString(
            Payload(p)
        )
        val r = OpenAIService.CompletionRequest(
            prompt = p
        )
        return openAIService.getCompletions(r)
    }

    data class Payload(
        val prompt: String
    )
}
