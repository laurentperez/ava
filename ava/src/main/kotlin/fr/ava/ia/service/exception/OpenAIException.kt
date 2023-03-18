package fr.ava.ia.service.exception

import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response

class OpenAIException(message: String?, response: Response) : WebApplicationException(message, response)
