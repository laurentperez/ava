package fr.ava.ia.service.db

import fr.ava.ia.model.Conversation
import fr.ava.ia.model.LastExchange
import fr.ava.ia.repository.ConversationRepository
import fr.ava.ia.repository.LastExchangeRepository
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.jboss.logging.Logger

@ApplicationScoped
class DbService {

    @Inject
    private lateinit var conversationRepository: ConversationRepository

    @Inject
    private lateinit var lastExchangeRepository: LastExchangeRepository

    private var logger = Logger.getLogger(this::class.java.name)

    @Transactional
    fun saveConversation(conversation: Conversation) {
        logger.info("\uD83D\uDCDD saving $conversation")
        conversationRepository.persistAndFlush(conversation)
    }

    @Transactional
    fun saveLastExchange(lastExchange: LastExchange) {
        logger.info("\uD83D\uDCDD saving $lastExchange")
        lastExchangeRepository.persistAndFlush(lastExchange)
    }

    @Transactional
    fun findLastExchange() : LastExchange {
        return lastExchangeRepository.findAll(Sort.by("id").descending()).firstResult()
        // return lastExchangeRepository.findAll(Sort.descending()).firstResult()
    }
}
