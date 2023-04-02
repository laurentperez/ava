package fr.ava.ia.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.NamedQuery
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import java.util.*

@Entity
@NamedQuery(name = "Conversation.getByUserID", query = "from Conversation where userID = ?1 and isActive = true")
class Conversation (

    // https://spring.io/guides/tutorials/spring-boot-kotlin/

    @Column(nullable = false)
    val userID: String, // TODO crypt this

    @Column(nullable = false)
    val postId: String,

    @Column(nullable = true)
    val repliedTo: String?,

    @Column(nullable = true)
    val rootId: String?,

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    val createdAt: Date,

    @Column(nullable = false, columnDefinition = "TEXT")
    val message: String,

    @Column(nullable = false)
    val actor: Actor,

    @Column(nullable = false)
    val isActive: Boolean = true,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    var id: Long? = null

)

enum class Actor {
    USER, ASSISTANT
}

