package fr.ava.ia.service.oai

import fr.ava.ia.service.oai.OpenAIAssistants.Companion.ASSISTANT_PYTHON_COMPLETION
import fr.ava.ia.service.oai.OpenAIAssistants.Companion.ASSISTANT_PYTHON_CONSISE
import fr.ava.ia.service.oai.OpenAIAssistants.Companion.ASSISTANT_PYTHON_USING
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.eclipse.microprofile.rest.client.inject.RestClient

@Path("/oai")
// TODO https://stackoverflow.com/questions/64154593/quarkus-swagger-ui-authorization
@SecuritySchemes(value = [SecurityScheme(securitySchemeName = "apiKey",
    type = SecuritySchemeType.HTTP, scheme = "Bearer")])
class OpenAIResource {

    @Inject
    @RestClient
    private lateinit var openAIService: OpenAIService

    @Tag(name = "OpenAI models", description = "Returns available models")
    @GET
    @SecurityRequirement(name = "apiKey")
    fun models(): OpenAIService.Models {
        return openAIService.getModels()
    }

    @GET
    @Path("/chat")
    fun chat(): OpenAIService.ChatResponse {
        val r = OpenAIService.ChatRequest(
            messages = listOf(
                OpenAIService.ChatMessage("system", ASSISTANT_PYTHON_CONSISE),
                OpenAIService.ChatMessage("user", ASSISTANT_PYTHON_USING + "write hello world. then uppercase it.")
            )
        )
        return openAIService.getChatCompletions(r)
    }

    @GET
    @Path("/completion")
    fun completion(): OpenAIService.CompletionResponse {
        val p = ASSISTANT_PYTHON_COMPLETION.replace("%PROMPT%","connect to rabbitmq server at url of 10.0.0.1\n")
        val r = OpenAIService.CompletionRequest(
            prompt = p
        )
        return openAIService.getCompletions(r)
    }

}
