package fr.ava.ia.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import java.util.*

@Entity
class Conversation (

    // https://spring.io/guides/tutorials/spring-boot-kotlin/

    @Column(nullable = false)
    val userID: String, // TODO sha1 salt this

    @Column(nullable = false)
    val postId: String,

    @Column(nullable = true)
    val repliedTo: String?,

    @Column(nullable = true)
    val rootId: String?,

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    val createdAt: Date,

    @Column(nullable = false)
    val message: String,

    @Column(nullable = false)
    val actor: Actor,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    var id: Long? = null

)

enum class Actor {
    USER, ASSISTANT
}

