package fr.ava.ia.service.hf.bloom

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.inject.Inject
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import org.eclipse.microprofile.openapi.annotations.tags.Tag

@Path("/hf/bloom")
class BloomResource {

    @Inject
    private lateinit var bloomService: BloomService

    @Tag(name = "Bloom prompt", description = "The prompt")
    @POST
    fun prompt(prompt: BloomPrompt): String {
        return bloomService.prompt(prompt.msg)
    }
}

data class BloomPrompt(
    @JsonProperty("msg") val msg: String
)
