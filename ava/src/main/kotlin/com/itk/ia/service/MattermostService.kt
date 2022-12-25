package com.itk.ia.service

import net.bis5.mattermost.client4.MattermostClient
import java.util.logging.Level
import java.util.logging.Logger
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class MattermostService {

    var logger = Logger.getLogger(this::class.java.name)

//    @PostConstruct
    fun init() {
        logger.warning("xxxxxxxxxxxxxxxxxxxxxxx")
        println("boo**********matter")
        var client: MattermostClient
        // client = MattermostClient("YOUR-MATTERMOST-URL")
        client = MattermostClient.builder()
            .url("http://localhost:8065")
            .logLevel(Level.INFO)
            .ignoreUnknownProperties()
            .build()
        client.login("ava", "aaaaa")
    }


}
