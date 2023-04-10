package fr.ava.ia.repository

import fr.ava.ia.model.ChatPartner
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
@ApplicationScoped
class ChatPartnerRepository : PanacheRepository<ChatPartner>
