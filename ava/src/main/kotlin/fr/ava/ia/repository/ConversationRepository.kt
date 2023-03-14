package fr.ava.ia.repository

import fr.ava.ia.model.Conversation
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ConversationRepository : PanacheRepository<Conversation> {
}
