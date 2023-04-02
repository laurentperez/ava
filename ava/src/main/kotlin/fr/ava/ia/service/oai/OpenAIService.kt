package fr.ava.ia.service.oai

import com.fasterxml.jackson.annotation.JsonProperty
import fr.ava.ia.service.exception.OpenAIExceptionMapper
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider
import java.time.Instant

@RegisterRestClient(configKey = "oai-api")
// @ClientHeaderParam(name = "x-testing", value = "java")
// @ClientHeaderParam(name = "Authorization", value = ["{xxxxxxx.ia.client.OpenAI.getAuthorizationHeader}"])
@RegisterClientHeaders(OpenAIHeadersFactory::class)
@RegisterProvider(OpenAIExceptionMapper::class, priority = 50)
interface OpenAIService { // TODO @Retry

    @GET
    @Path("/models")
    fun getModels(): Models
    // fun getExtensionsById(@QueryParam("id") id: String): Set<Extension>

    @POST
    @Path("/chat/completions")
    fun getChatCompletions(request : ChatRequest) : ChatResponse

    @POST
    @Path("/completions") // warning cost is > gpt3.5 : davinci
    fun getCompletions(request : CompletionRequest) : CompletionResponse

    data class Model(
        @JsonProperty("id")val id: String,
        @JsonProperty("object")val obj: String,
        @JsonProperty("owned_by")val owned_by: String
    )
    data class Models(
        @JsonProperty("object")val obj: String,
        @JsonProperty("data")val data: List<Model>
    )

    // ref : https://platform.openai.com/docs/api-reference/chat/create
    data class ChatRequest(
        val model : String = "gpt-3.5-turbo",
        val messages : List<ChatMessage>,
        val temperature : Double = 1.0,
        val n : Int = 1,
        val max_tokens : Int = 512,
        val user: String = ChatRequest::class.java.simpleName + model + "jdk"
        // echo to echo back the prompt
    )

    data class ChatMessage(
        val role : String, // system, user, assistant
        val content : String
    )

    data class ChatResponse(
        val id : String,
        // val object : String,
        val model: String,
        val created : Instant,
        val choices : List<ChatChoice>,
        val usage : Usage
    )

    data class CompletionRequest(
        val model : String = "code-davinci-002", // codex. or code-cushman-001.
        val prompt : String,
        val temperature : Double = 0.0, // lower means focused deterministic, higher more random
        val n : Int = 1,
        val max_tokens : Int = 512,
        val user: String = CompletionRequest::class.java.simpleName
    )
    data class CompletionResponse(
        val id : String,
        // val object : String,
        val model: String,
        val created : Instant,
        val choices : List<CompletionChoice>,
        val usage : Usage
    )

    data class CompletionChoice(
        val index : Long,
        val text: String,
        val finish_reason : String? // can be null
    )

    data class ChatChoice(
        val index : Long,
        val message: ChatMessage,
        val finish_reason : String
    )

    data class Usage(
        val prompt_tokens : Int,
        val completion_tokens: Int,
        val total_tokens : Int
    )

}
