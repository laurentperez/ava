package com.itk.ia

import com.itk.ia.service.MattermostService
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

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
