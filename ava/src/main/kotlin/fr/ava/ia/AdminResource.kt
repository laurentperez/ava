package fr.ava.ia

import fr.ava.ia.model.Actor
import fr.ava.ia.model.Conversation
import fr.ava.ia.service.db.DbService
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import java.time.Instant
import java.util.*

// @Path("/admin")
class AdminResource {

    @Inject
    private lateinit var dbService: DbService

    @POST
    fun save(userId: String, postId: String, repliedTo: String, rootId: String, createdAt: Instant, message: String) {
        // return dbService.saveConversation(Conversation("userid123", "root123", 123, "hello"))
        return dbService.saveConversation(Conversation(userId, postId, repliedTo, rootId, Date.from(createdAt), message, Actor.USER))
    }
}
