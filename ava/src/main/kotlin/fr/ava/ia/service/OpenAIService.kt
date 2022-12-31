package fr.ava.ia.service

import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path

@RegisterRestClient(baseUri = "https://api.openai.com/v1/")
// @RegisterClientHeaders(RequestUUIDHeaderFactory::class)
// @ClientHeaderParam(name = "x-testing", value = "java")
// @ClientHeaderParam(name = "Authorization", value = ["{com.itk.ia.client.OpenAI.getAuthorizationHeader}"])
@RegisterClientHeaders(OpenAIHeadersFactory::class)
interface OpenAIService {

    @GET
    @Path("/models")
    fun getModels(): Models
    // fun getExtensionsById(@QueryParam("id") id: String): Set<Extension>

    data class Extension(val id: String, val name: String, val shortName: String, val keywords: List<String>)

    data class Model(@JsonProperty("id")val id: String)
    data class Models(@JsonProperty("data")val data: List<Model>)

    data class Completion(
        val prompt: String,
        val model: String = "",
        val maxTokens: Int = 64,
        val user: String = "testjdk"
    )

}
