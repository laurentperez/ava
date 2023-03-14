package fr.ava.ia.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Conversation (

    // https://spring.io/guides/tutorials/spring-boot-kotlin/

    @Column(nullable = false)
    val userID : String,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    var id: Long? = null

)
