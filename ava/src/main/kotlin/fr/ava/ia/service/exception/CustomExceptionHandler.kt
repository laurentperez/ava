package fr.ava.ia.service.exception

import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

@Provider
class CustomExceptionHandler : ExceptionMapper<Exception> {

    override fun toResponse(exception: Exception): Response? {
        return mapExceptionToResponse(exception)
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
