package fr.ava.ia

import fr.ava.ia.model.Conversation
import fr.ava.ia.repository.ConversationRepository
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.mockito.InjectMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@QuarkusTest
class ConversationTest {

    @InjectMock
    var conversationRepository: ConversationRepository? = null

    @Test
    fun should_mock_conversations() {
        Assertions.assertEquals(0, conversationRepository!!.count())

        val c = Conversation("id1")
//        Mockito.`when`(conversationRepository!!.findById(123L)).thenReturn(c)
//        Assertions.assertSame(c, conversationRepository!!.findById(123L));

        conversationRepository!!.persist(c)
        Assertions.assertEquals(0, conversationRepository!!.count())
    }
}
