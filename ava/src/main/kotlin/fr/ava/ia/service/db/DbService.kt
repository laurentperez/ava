package fr.ava.ia.service.db

import fr.ava.ia.model.ChatPartner
import fr.ava.ia.model.Conversation
import fr.ava.ia.model.LastExchange
import fr.ava.ia.repository.ChatPartnerRepository
import fr.ava.ia.repository.ConversationRepository
import fr.ava.ia.repository.LastExchangeRepository
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.jboss.logging.Logger

@ApplicationScoped
class DbService {

    private var logger = Logger.getLogger(this::class.java.name)

    @Inject
    lateinit var conversationRepository: ConversationRepository

    @Inject
    lateinit var lastExchangeRepository: LastExchangeRepository

    @Inject
    lateinit var chatPartnerRepository: ChatPartnerRepository

    @Transactional
    fun saveConversation(conversation: Conversation) {
        logger.info("\uD83D\uDCDD saving $conversation")
        conversationRepository.persistAndFlush(conversation)
    }

    @Transactional
    fun findConversationWithUserWithRootId(userID : String, rootID: String) : List<Conversation> {
        // return conversationRepository.list("from Conversation where userID=?1", userID)
        return conversationRepository.find("#Conversation.withUserWithRootId", userID, rootID).list()
    }
    @Transactional
    fun findAllConversations() : List<Conversation> {
        return conversationRepository.listAll()
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

    @Transactional
    fun findAllUsers() : List<ChatPartner> {
        return chatPartnerRepository.listAll()
    }

    @Transactional
    fun findUserById(userId: String) : ChatPartner {
        return chatPartnerRepository.find("userId", userId).singleResult()
    }

    @Transactional
    fun saveUser(userId: String, userName: String) {
        chatPartnerRepository.persistAndFlush(ChatPartner(userId, userName))
    }
}
