package fr.ava.ia

import fr.ava.ia.model.Actor
import fr.ava.ia.model.Conversation
import fr.ava.ia.repository.ConversationRepository
import fr.ava.ia.service.db.DbService
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import java.util.*

@QuarkusTest
class ConversationTestIT {

    @Inject
    lateinit var conversationRepository: ConversationRepository
    @Inject
    lateinit var dbService: DbService

    @Test
    fun should_list_conversations() {
        var convs = dbService.findAllConversations()
        val poster = "foo"
        Assertions.assertTrue(convs.isEmpty())
        dbService.saveConversation(Conversation(userID = poster, postId = "123", repliedTo = "456", rootId = "000",
            createdAt = Date(), actor = Actor.USER, message = "hi" ))
        convs = dbService.findAllConversations()
        Assertions.assertTrue(convs.size == 1)
        convs = dbService.findConversationWithUser(poster)
        Assertions.assertTrue(convs.size == 1)
        // dbService.findConversationWithUser("")
    }
}
