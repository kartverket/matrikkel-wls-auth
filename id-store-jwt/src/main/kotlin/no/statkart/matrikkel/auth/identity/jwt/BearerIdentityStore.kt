package no.statkart.matrikkel.auth.identity.jwt

import no.statkart.matrikkel.auth.credential.JsonWebStructureCredential
import no.statkart.matrikkel.auth.credential.AuthConfigKeys
import org.apache.logging.log4j.LogManager
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.jwt.Claims
import org.jose4j.jwt.consumer.JwtConsumer
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.security.enterprise.credential.Credential
import jakarta.security.enterprise.identitystore.CredentialValidationResult
import jakarta.security.enterprise.identitystore.IdentityStore

@ApplicationScoped
open class BearerIdentityStore @Inject constructor(
    jsonWebKeySet: org.jose4j.keys.resolvers.VerificationKeyResolver?,
    @ConfigProperty(name = AuthConfigKeys.ISSUER) issuer: String?,
    @ConfigProperty(name = AuthConfigKeys.AUD, defaultValue = "") audiences: List<String>?
) : IdentityStore {

    constructor() : this(null, null, null)

    private val jwtConsumer: JwtConsumer = org.jose4j.jwt.consumer.JwtConsumerBuilder()
        .setVerificationKeyResolver(jsonWebKeySet)
        .setRequireJwtId()
        .setEnableRequireIntegrity()
        .setRequireExpirationTime()
        .setJwsAlgorithmConstraints(
            org.jose4j.jwa.AlgorithmConstraints.ConstraintType.PERMIT,
            org.jose4j.jws.AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256,
            org.jose4j.jws.AlgorithmIdentifiers.RSA_USING_SHA256
        )
        .setAllowedClockSkewInSeconds(60)
        .setExpectedIssuer(true, issuer)
        .run {
            if (audiences!!.isEmpty()) {
                setSkipDefaultAudienceValidation()
            } else {
                setExpectedAudience(true, *audiences.toTypedArray())
            }
        }
        .build()

    override fun validate(credential: Credential?): CredentialValidationResult =
        if (credential is JsonWebStructureCredential) {
            validate(credential)
        } else {
            CredentialValidationResult.NOT_VALIDATED_RESULT
        }

    fun validate(credential: JsonWebStructureCredential): CredentialValidationResult {
        val jwtContext = try {
            jwtConsumer.process(credential.compactSerialization)
        } catch (e: org.jose4j.jwt.consumer.InvalidJwtException) {
            // Vi ønsker ikke å logge så mye for den normale flyten hvor et token som kommer fra et password grant er utgått
            if (credential.fromPasswordGrant && e.errorDetails.size == 1 && e.hasExpired()) {
                logger.debug("Authentication expired: {}", e.message)
            } else {
                logger.warn("Authentication failed", e)
            }
            return CredentialValidationResult.INVALID_RESULT
        }

        val claims = jwtContext?.jwtClaims ?: return CredentialValidationResult.INVALID_RESULT
        val storeId = claims.issuer
        val maybeCallerName = claims.runCatching { getClaimValueAsString(Claims.upn.name)
            ?: getClaimValueAsString(Claims.preferred_username.name)
            ?: getClaimValueAsString(Claims.sub.name)
        }
        val callerName = maybeCallerName.getOrNull() ?: return CredentialValidationResult.INVALID_RESULT
        val callerUniqueId = claims.subject
        val groups = claims
            .runCatching { getStringListClaimValue(Claims.groups.name)?.toSet() }
            .getOrNull() ?: emptySet()

        return CredentialValidationResult(storeId, callerName, null, callerUniqueId, groups)
    }

    companion object {
        private val logger = LogManager.getLogger(this::class.java.enclosingClass)
    }
}
