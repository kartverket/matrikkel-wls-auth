package no.statkart.matrikkel.auth.common

import arrow.core.*
import arrow.core.extensions.applicativeNel
import arrow.fx.coroutines.*
import no.statkart.matrikkel.auth.credential.AuthConfigKeys
import no.statkart.matrikkel.auth.util.jaxrs.readEntity
import no.statkart.matrikkel.auth.util.jaxrs.suspend
import org.apache.logging.log4j.LogManager
import org.eclipse.microprofile.config.spi.ConfigSource
import java.net.URI
import java.time.Instant
import javax.json.JsonObject
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext

class OIDCDiscovery(uri: URI, private val env: Environment = Environment(IOPool)) : ConfigSource {

    private val endpoint: WebTarget = client.target(uri)

    private val propertyMap: Either<Throwable, Map<String, String>> by lazy {
        env.unsafeRunSync {
            val schedule = Schedule
                    .fibonacci<JsonObject>(348.milliseconds)
                    .logOutput { logger.warn("Unable to fetch OpenIDConnect metadata, will retry in ${it.millis}ms") }
                    .and(Schedule.recurs(16))
            propertyMapFromMetadataJson(retry(schedule) { fetchMetadata() }).map {
                logger.info(
                        "OpenID Connect metadata detected {}{}",
                        endpoint.uri,
                        it.entries.joinToString(
                                "\n\t", "\n\t", "\n",
                                transform = { (k, v) -> "$k: $v" }))
                it
            }
        }
    }

    init {
        env.unsafeRunAsync {
            this::propertyMap.get()
        }
    }

    override fun getProperties(): Map<String, String> {
        return propertyMap.getOrHandle { throw it }
    }

    override fun getValue(propertyName: String): String? {
        return properties[propertyName]
    }

    override fun getPropertyNames(): Set<String> {
        return properties.keys
    }

    override fun getName(): String = "OpenID Connect Discovery Config Source"

    override fun getOrdinal(): Int = 80

    private suspend fun fetchMetadata(): JsonObject {
        return endpoint
                .request(MediaType.APPLICATION_JSON_TYPE)
                .acceptEncoding("UTF-8")
                .buildGet()
                .invoke()
                .readEntity<JsonObject>()
                .getOrHandle { errResp: Response ->
                    throw IllegalStateException("Invalid response from metadata endpoint: $errResp")
                }
    }

    companion object {
        const val LAST_UPDATE_CONFIG = "matrikkel.oauth.discovery.last_update"
        private val logger = LogManager.getLogger(this::class.java.enclosingClass)
        private val client = ClientBuilder.newBuilder().build()

        private suspend fun propertyMapFromMetadataJson(json: JsonObject): Either<Throwable, Map<String, String>> = Validated
                .applicativeNel<Throwable>()
                .mapN(Validated.catchNel { json.getString("issuer") },
                      Validated.catchNel { json.getString("authorization_endpoint") },
                      Validated.catchNel { json.getString("token_endpoint") },
                      Validated.catchNel { json.getString("jwks_uri") },
                      Validated.catchNel { json.getString("userinfo_endpoint", null) }
                ) { (iss, auth, token, jwk, userinfo) -> mutableMapOf(
                        AuthConfigKeys.ISSUER to iss,
                        AuthConfigKeys.AUTHORIZATION_URL to auth,
                        AuthConfigKeys.TOKEN_URL to token,
                        AuthConfigKeys.VERIFIER_PUBLIC_KEY_LOCATION to jwk
                    ).also {
                        if (userinfo != null) it[AuthConfigKeys.USERINFO_URL] = userinfo
                        it[LAST_UPDATE_CONFIG] = Instant.now().toEpochMilli().toString()
                    }
                }
                .toEitherSupressed { IllegalArgumentException("Invalid metadata JSON") }

    }
}


// TODO: Flytt til passende sted
inline suspend fun <E : Nel<out Throwable>, A> ValidatedOf<E, A>.getOrThrow() : A =
    toEitherSupressed().fold({ throw it}, ::identity)

// TODO: Flytt til passende sted
inline fun <E : Throwable, A> ValidatedOf<Nel<out E>, A>.toEitherSupressed(f: () -> E): Either<E, A> = fix().mapLeft { ts ->
    ts.singleOrNull() ?: ts.fold(f()) { acc, e -> acc.apply { addSuppressed(e) } }
}.toEither()

// TODO: Flytt til passende sted
inline fun <A> ValidatedOf<Nel<out Throwable>, A>.toEitherSupressed() : Either<Throwable, A> = toEitherSupressed { IllegalStateException("Multiple validations failed")}


