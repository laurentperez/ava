package fr.ava.ia.repository

import fr.ava.ia.model.LastExchange
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class LastExchangeRepository : PanacheRepository<LastExchange>
