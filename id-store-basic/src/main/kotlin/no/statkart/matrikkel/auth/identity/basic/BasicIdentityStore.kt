package no.statkart.matrikkel.auth.identity.basic

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.getOrHandle
import arrow.fx.coroutines.Environment
import arrow.fx.coroutines.Schedule
import arrow.fx.coroutines.milliseconds
import arrow.fx.coroutines.retry
import no.statkart.matrikkel.auth.credential.AuthConfigKeys
import no.statkart.matrikkel.auth.credential.BasicAuthenticationCredentialExt
import no.statkart.matrikkel.auth.credential.JsonWebStructureCredential
import no.statkart.matrikkel.auth.util.jaxrs.readEntity
import no.statkart.matrikkel.auth.util.jaxrs.suspend
import org.apache.logging.log4j.LogManager
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.net.URI
import java.nio.CharBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import jakarta.annotation.Priority
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Alternative
import jakarta.inject.Inject
import jakarta.interceptor.Interceptor
import jakarta.json.JsonObject
import jakarta.security.enterprise.credential.BasicAuthenticationCredential
import jakarta.security.enterprise.credential.Credential
import jakarta.security.enterprise.identitystore.CredentialValidationResult
import jakarta.security.enterprise.identitystore.IdentityStore
import jakarta.security.enterprise.identitystore.IdentityStoreHandler
import jakarta.ws.rs.client.ClientBuilder
import jakarta.ws.rs.client.Entity
import jakarta.ws.rs.client.WebTarget
import jakarta.ws.rs.core.Form
import jakarta.ws.rs.core.Response

@ApplicationScoped
@Alternative
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 200)
open class BasicIdentityStore protected constructor(
    private val tokenTarget: WebTarget?,
    private val clientCredential: String?,
    private val identityStores: IdentityStoreHandler?,
    private val env: Environment = Environment()
) : IdentityStore {

    private val map: ConcurrentMap<String, Pair<JsonWebStructureCredential, String?>> = ConcurrentHashMap()
    private val salt = SecureRandom().run { byteArrayOf().also { nextBytes(it) } }

    constructor() : this(null, null, null)

    @Inject
    protected constructor(
            identityStores: IdentityStoreHandler,
            @ConfigProperty(name = AuthConfigKeys.TOKEN_URL) tokenURI: URI,
            @ConfigProperty(name = AuthConfigKeys.CLIENT_ID) clientIdOption: Optional<String>,
            @ConfigProperty(name = AuthConfigKeys.CLIENT_SECRET) clientSecretOption: Optional<String>
    ) : this(
            client.target(tokenURI),
            clientIdOption.flatMap { clientId ->
                clientSecretOption.map { clientSecret -> Base64
                        .getUrlEncoder()
                        .encodeToString("$clientId:$clientSecret".toByteArray()) }
            }.orElse(null),
            identityStores
    )

    fun validate(credential: BasicAuthenticationCredentialExt): CredentialValidationResult {
        val credentialKey = MessageDigest.getInstance("SHA-256").run {
            update(credential.caller.toByteArray())
            update(Charsets.UTF_8.encode(CharBuffer.wrap(credential.password.value)))
            update(salt)
            Base64.getEncoder().encodeToString(digest())
        }
        val cachedTokens = map[credentialKey]
        if (cachedTokens != null) {
            val cachedValidationResult = cachedTokens.let { (accessToken, _) -> identityStores!!.validate(accessToken) }.takeIf { it?.status == CredentialValidationResult.Status.VALID }
            if (cachedValidationResult?.status == CredentialValidationResult.Status.VALID) {
                return cachedValidationResult
            }
        }

        val tokens = map.compute(credentialKey) { _, v ->
            env.unsafeRunSync {
                val refreshedTokens = v?.second?.let {
                    fetchTokens(it, this::refreshTokenResult).getOrHandle {
                        logger.debug("Failed to refresh token: {}", it)
                        null
                    }
                }
                refreshedTokens ?: fetchTokens(credential, this::fetchTokenResult).getOrHandle {
                    logger.debug("Failed to fetch access token for {}:", credential.caller, it)
                    null
                }
            }
        }

        return if (tokens == null) {
            CredentialValidationResult.INVALID_RESULT
        } else {
            val validationResult = identityStores!!.validate(tokens.first)
            if (validationResult.status != CredentialValidationResult.Status.VALID) {
                map.remove(credentialKey, tokens)
            }
            validationResult
        }
    }

    override fun validate(credential: Credential?): CredentialValidationResult {
        return when(credential) {
            is BasicAuthenticationCredentialExt -> validate(credential)
            is BasicAuthenticationCredential -> validate(BasicAuthenticationCredentialExt(credential.caller, credential.password))
            else -> CredentialValidationResult.NOT_VALIDATED_RESULT
        }
    }

    private suspend fun <A> fetchTokens(credential: A, fetch: suspend (A) -> Either<Response, JsonObject>) : Either<Throwable, Pair<JsonWebStructureCredential, String?>> = either {
        val tokenResult = !Either.catch {
            retry(Schedule.fibonacci<Either<Response, JsonObject>>(833.milliseconds).and(Schedule.recurs(3))) {
                fetch(credential)
            }.getOrHandle { throw IllegalAccessException("Unable to fetch token: $it") }
        }
        val accessToken = !Either.catch { tokenResult.getString("access_token") }.map { JsonWebStructureCredential(it, true) }
        accessToken to tokenResult.getString("refresh_token", null)
    }

    private suspend fun fetchTokenResult(credential: BasicAuthenticationCredentialExt): Either<Response, JsonObject> = tokenTarget!!
            .request()
            .run {
                if (clientCredential != null) {
                    header("Authorization", "Basic $clientCredential")
                }
                val form = Entity.form(Form()
                        .param("grant_type", "password")
                        .param("username", credential.caller)
                        .param("password", credential.passwordAsString))
                buildPost(form).suspend().readEntity()
            }

    private suspend fun refreshTokenResult(refreshToken: String): Either<Response, JsonObject> = tokenTarget!!
            .request()
            .run {
                if (clientCredential != null) {
                    header("Authorization", "Basic $clientCredential")
                }
                val form = Entity.form(Form()
                        .param("grant_type", "refresh_token")
                        .param("refresh_token", refreshToken))
                buildPost(form).suspend().readEntity()
            }


    companion object {
        private val logger = LogManager.getLogger(this::class.java.enclosingClass)
        private val client = ClientBuilder.newClient()
    }
}
