package fr.ava.ia.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class ChatPartner (

    @Id
    @Column(name = "userId", nullable = false)
    var userId: String? = null,

    @Column(nullable = false)
    val userName: String

)
