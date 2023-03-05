package fr.ava.ia.service.db

import fr.ava.ia.model.Conversation
import fr.ava.ia.repository.ConversationRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class DbService {

    @Inject
    private lateinit var conversationRepository: ConversationRepository

    @Transactional
    fun saveConversation(conversation: Conversation) {
        conversationRepository.persist(conversation)
    }
}
