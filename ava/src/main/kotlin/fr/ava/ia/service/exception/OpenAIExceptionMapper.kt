package fr.ava.ia.service.exception

import jakarta.annotation.Priority
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper
import java.io.ByteArrayInputStream

@Priority(4000)
class OpenAIExceptionMapper : ResponseExceptionMapper<Exception> {

    override fun toThrowable(response: Response?): Exception {
        val status = response!!.status
        val body = getBody(response)
        val re: RuntimeException = when (status) {
            401 -> OpenAIException(body, response)
            else -> WebApplicationException("oops", response)
        }
        return re
    }

    private fun getBody(response: Response): String {
        val `is`: ByteArrayInputStream = response.entity as ByteArrayInputStream
        val bytes = ByteArray(`is`.available())
        `is`.read(bytes, 0, `is`.available())
        return String(bytes)
    }

    private fun mapExceptionToResponse(exception: java.lang.Exception): Response? {
        // Use response from WebApplicationException as they are
        return if (exception is WebApplicationException) {
            // Overwrite error message
            val originalErrorResponse = exception.response
            Response.fromResponse(originalErrorResponse)
                .entity(exception.message)
                .build()
        } else {
            Response.serverError().entity("Internal Server Error").build()
        }
    }

}
