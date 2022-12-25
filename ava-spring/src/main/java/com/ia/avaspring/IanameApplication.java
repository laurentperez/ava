package com.ia.avaspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.bis5.mattermost.client4.MattermostClient;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
public class IanameApplication {

	public static void main(String[] args) {

		         MattermostClient client = MattermostClient.builder()
            .url("http://xnhhlocalhost:8065")
            .logLevel(Level.INFO)
            .ignoreUnknownProperties()
            .build();
        client.login("ava", "aaaaa");
	// https://zetcode.com/springboot/jersey/

		SpringApplication.run(IanameApplication.class, args);
	}

}
