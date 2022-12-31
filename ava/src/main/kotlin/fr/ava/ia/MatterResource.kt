package fr.ava.ia

import fr.ava.ia.service.MattermostService
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
        return mattermostService.toString()
    }
}
