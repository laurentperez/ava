package com.itk.ia

import com.itk.ia.service.MattermostService
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/matter")
class MatterResource {

    @Inject
    lateinit var mattermostService: MattermostService

    @GET
    fun hello(): String {
        mattermostService.init();
        return mattermostService.toString()
    }
}
