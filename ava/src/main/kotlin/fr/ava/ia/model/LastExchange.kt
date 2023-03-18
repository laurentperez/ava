package fr.ava.ia.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class LastExchange (

    @Column(nullable = false)
    val withUserID: String, // TODO sha1 salt this
    @Column(nullable = false)
    val withUserName: String, // TODO sha1 salt this
    @Column(nullable = false)
    val postID: String,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    var id: Long? = null

)
