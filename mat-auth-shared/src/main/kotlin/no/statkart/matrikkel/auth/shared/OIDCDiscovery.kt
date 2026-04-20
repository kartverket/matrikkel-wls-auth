package no.statkart.matrikkel.auth.shared

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.resilience.Schedule
import arrow.resilience.retryRaise
import jakarta.json.JsonObject
import jakarta.ws.rs.client.ClientBuilder
import jakarta.ws.rs.client.WebTarget
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.statkart.matrikkel.auth.credential.AuthConfigKeys
import no.statkart.matrikkel.auth.util.jaxrs.readEntity
import org.eclipse.microprofile.config.spi.ConfigSource
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

open class OIDCDiscovery(uri: URI) : ConfigSource {
    private val client = ClientBuilder.newBuilder().build()
    private val endpoint: WebTarget = client.target(uri)
    private val warmupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val propertyMap: Either<Throwable, Map<String, String>> by lazy {
        runBlocking(Dispatchers.IO) {
            val schedule: Schedule<Throwable, Pair<Duration, Long>> = Schedule
                .fibonacci<Throwable>(348.milliseconds)
                .log { _, it -> logger.warn("Unable to fetch OpenIDConnect metadata, will retry in ${it.inWholeMilliseconds}ms") }
                .and(Schedule.recurs(16))

            schedule.retryRaise { fetchMetadata() }
                .flatMap(::propertyMapFromMetadataJson)
                .onRight {
                    logger.info(
                        "OpenID Connect metadata detected {}{}",
                        endpoint.uri,
                        it.entries.joinToString(
                            "\n\t", "\n\t", "\n",
                            transform = { (k, v) -> "$k: $v" }
                        )
                    )
                }
        }
    }

    init {
        warmupScope.launch {
            propertyMap
        }
    }

    override fun getProperties(): Map<String, String> {
        return propertyMap.getOrElse { throw it }
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
            .getOrElse { errResp: Response ->
                throw IllegalStateException("Invalid response from metadata endpoint: $errResp")
            }
    }

    companion object {
        const val LAST_UPDATE_CONFIG = "matrikkel.oauth.discovery.last_update"
        private val logger = LoggerFactory.getLogger(this::class.java.enclosingClass)

        internal fun propertyMapFromMetadataJson(json: JsonObject): Either<Throwable, Map<String, String>> {
            return Either.catch {
                mapOf(
                    AuthConfigKeys.ISSUER to requireNotNull(json.getString("issuer", null)),
                    AuthConfigKeys.AUTHORIZATION_URL to requireNotNull(json.getString("authorization_endpoint", null)),
                    AuthConfigKeys.TOKEN_URL to requireNotNull(json.getString("token_endpoint", null)),
                    AuthConfigKeys.VERIFIER_PUBLIC_KEY_LOCATION to requireNotNull(json.getString("jwks_uri", null)),
                    AuthConfigKeys.USERINFO_URL to json.getString("userinfo_endpoint", null),
                    LAST_UPDATE_CONFIG to Instant.now().toEpochMilli().toString()
                ).filterNot { entry -> entry.value.isNullOrEmpty() }
            }
        }
    }
}
