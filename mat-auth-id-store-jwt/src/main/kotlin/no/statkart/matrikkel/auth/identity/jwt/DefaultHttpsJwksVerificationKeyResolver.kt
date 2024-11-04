package no.statkart.matrikkel.auth.identity.jwt

import no.statkart.matrikkel.auth.credential.AuthConfigKeys
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jose4j.jwk.HttpsJwks
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwx.JsonWebStructure
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver
import org.jose4j.keys.resolvers.VerificationKeyResolver
import java.security.Key
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Instance
import jakarta.inject.Inject

@ApplicationScoped
open class DefaultHttpsJwksVerificationKeyResolver(private val verificationKeyResolver: Lazy<VerificationKeyResolver>?) : VerificationKeyResolver {
    constructor():this(null)

    @Inject
    protected constructor (
        @ConfigProperty(name = AuthConfigKeys.VERIFIER_PUBLIC_KEY_LOCATION) locationProvider: String
    ) : this(lazy { HttpsJwksVerificationKeyResolver(HttpsJwks(locationProvider)) })

    override fun resolveKey(jws: JsonWebSignature?, nestingContext: MutableList<JsonWebStructure>?): Key =
        verificationKeyResolver!!.value.resolveKey(jws, nestingContext)
}
