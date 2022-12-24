package com.itk.ia

import com.itk.ia.service.OpenAIService
import org.eclipse.microprofile.rest.client.inject.RestClient
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

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
