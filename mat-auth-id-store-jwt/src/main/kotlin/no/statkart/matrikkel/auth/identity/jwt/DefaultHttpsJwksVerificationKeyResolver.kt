package no.statkart.matrikkel.auth.identity.jwt

import no.statkart.matrikkel.auth.credential.AuthConfigKeys
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jose4j.jwk.HttpsJwks
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwx.JsonWebStructure
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver
import org.jose4j.keys.resolvers.VerificationKeyResolver
import java.security.Key
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Instance
import javax.inject.Inject

@ApplicationScoped
class DefaultHttpsJwksVerificationKeyResolver(private val verificationKeyResolver: Lazy<VerificationKeyResolver>) : VerificationKeyResolver {
    @Inject
    protected constructor (
        @ConfigProperty(name = AuthConfigKeys.VERIFIER_PUBLIC_KEY_LOCATION) locationProvider: Instance<String>
    ) : this(lazy { HttpsJwksVerificationKeyResolver(HttpsJwks(locationProvider.get())) })

    override fun resolveKey(jws: JsonWebSignature?, nestingContext: MutableList<JsonWebStructure>?): Key =
        verificationKeyResolver.value.resolveKey(jws, nestingContext)
}
