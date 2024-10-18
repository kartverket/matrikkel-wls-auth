package no.statkart.matrikkel.auth.util.jaxrs

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.slf4j.LoggerFactory
import jakarta.ws.rs.client.Invocation
import jakarta.ws.rs.client.InvocationCallback
import jakarta.ws.rs.core.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


@PublishedApi internal val logger = LoggerFactory.getLogger("no.statkart.matrikkel.auth.util.jaxrs.ext")


suspend inline fun Invocation.suspend(): Response = suspendCoroutine { continuation ->
    submit(object : InvocationCallback<Response> {
        override fun completed(response: Response) {
            continuation.resume(response)
        }

        override fun failed(throwable: Throwable) {
            continuation.resumeWithException(throwable)
        }
    })
}

suspend inline fun <reified T> Response.readEntity(vararg responseStatuses: Response.Status = arrayOf(Response.Status.OK)) :Either<Response, T> =
    if (this.statusInfo.statusCode in responseStatuses.map { it.statusCode }) {
        readEntity(T::class.java).right()
    } else {
        this.left()
    }

