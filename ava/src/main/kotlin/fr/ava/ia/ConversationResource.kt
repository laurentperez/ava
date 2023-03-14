package fr.ava.ia

import fr.ava.ia.model.Conversation
import fr.ava.ia.repository.ConversationRepository
import fr.ava.ia.service.db.DbService
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path

@Path("/chat")
class ConversationResource {

    @Inject
    private lateinit var dbService: DbService

    @GET
    fun save() {
        return dbService.saveConversation(Conversation("userid123"))
    }
}
