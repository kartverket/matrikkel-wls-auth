package no.statkart.matrikkel.auth.ham

import no.statkart.matrikkel.auth.credential.AuthenticationChallenger
import no.statkart.matrikkel.auth.credential.extractor.HttpCredentialExtractor
import javax.annotation.Priority
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Alternative
import javax.enterprise.inject.Instance
import javax.inject.Inject
import javax.interceptor.Interceptor
import javax.security.enterprise.identitystore.IdentityStoreHandler

/**
 * CDI bønne må være tilgjengelig fra alle applikasjoner.
 * Kan gjøres ved å legge til mat-auth-ham til alle relevante war-er.
 * [https://javaee.github.io/security-spec/spec/jsr375-spec.html#_installation_and_configuration]
 */
@Suppress("NO_NOARG_CONSTRUCTOR_IN_SUPERCLASS")
@ApplicationScoped
@Alternative
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 200)
class DefaultHttpAuthenticationMechanism @Inject constructor (
    identityStoreHandler: IdentityStoreHandler,
    credentialExtractorInstance: Instance<HttpCredentialExtractor<*>>,
    authenticationChallengers: Instance<AuthenticationChallenger>
) : AbstractDefaultHttpAuthenticationMechanism(
        identityStoreHandler,
        credentialExtractorInstance,
        authenticationChallengers)
