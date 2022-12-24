package com.itk.ia.client

import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class OpenAI {

    fun generateCompletion(): String {
        return "using kotlin language. print hello world"
    }
}
