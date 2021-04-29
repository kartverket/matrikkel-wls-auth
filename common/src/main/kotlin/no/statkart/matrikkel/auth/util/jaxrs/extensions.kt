package no.statkart.matrikkel.auth.util.jaxrs

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.left
import arrow.core.right
import arrow.fx.coroutines.Environment
import arrow.fx.coroutines.Schedule
import org.apache.logging.log4j.LogManager
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.ExecutorService
import javax.naming.InitialContext
import javax.ws.rs.ProcessingException
import javax.ws.rs.client.Invocation
import javax.ws.rs.client.InvocationCallback
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


@PublishedApi internal val logger = LogManager.getLogger("no.statkart.matrikkel.auth.util.jaxrs.ext")


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

