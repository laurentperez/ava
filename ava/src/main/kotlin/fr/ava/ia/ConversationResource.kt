package fr.ava.ia

import fr.ava.ia.model.Actor
import fr.ava.ia.model.Conversation
import fr.ava.ia.service.db.DbService
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import java.time.Instant
import java.util.*

@Path("/oai/chat")
class ConversationResource {

    @Inject
    private lateinit var dbService: DbService

    @GET
    fun save(userId: String, postId: String, rootId: String, createdAt: Instant, message: String) {
        // return dbService.saveConversation(Conversation("userid123", "root123", 123, "hello"))
        return dbService.saveConversation(Conversation(userId, postId, rootId, Date.from(createdAt), message, Actor.USER))
    }
}
