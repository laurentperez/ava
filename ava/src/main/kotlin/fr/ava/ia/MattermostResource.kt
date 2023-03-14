package fr.ava.ia

import fr.ava.ia.service.messaging.MattermostService
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path

@Path("/matter")
class MattermostResource {

    @Inject
    lateinit var mattermostService: MattermostService

    @GET
    fun hello(): String {
        return mattermostService.toString()
    }
}
