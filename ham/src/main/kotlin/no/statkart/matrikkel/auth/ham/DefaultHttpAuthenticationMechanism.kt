package no.statkart.matrikkel.auth.ham

import no.statkart.matrikkel.auth.common.AbstractDefaultHttpAuthenticationMechanism
import no.statkart.matrikkel.auth.credential.AuthenticationChallenger
import no.statkart.matrikkel.auth.credential.extractor.HttpCredentialExtractor
import jakarta.annotation.Priority
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Alternative
import jakarta.enterprise.inject.Instance
import jakarta.inject.Inject
import jakarta.interceptor.Interceptor
import jakarta.security.enterprise.identitystore.IdentityStoreHandler

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
