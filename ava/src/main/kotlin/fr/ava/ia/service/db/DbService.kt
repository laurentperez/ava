package fr.ava.ia.service.db

import fr.ava.ia.model.Conversation
import fr.ava.ia.repository.ConversationRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.jboss.logging.Logger

@ApplicationScoped
class DbService {

    @Inject
    private lateinit var conversationRepository: ConversationRepository

    private var logger = Logger.getLogger(this::class.java.name)

    @Transactional
    fun saveConversation(conversation: Conversation) {
        logger.info("\uD83D\uDCDD saving $conversation")
        conversationRepository.persistAndFlush(conversation)
    }
}
