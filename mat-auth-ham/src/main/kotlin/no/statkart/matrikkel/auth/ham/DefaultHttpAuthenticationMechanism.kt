package no.statkart.matrikkel.auth.ham

import no.statkart.matrikkel.auth.common.AbstractDefaultHttpAuthenticationMechanism
import no.statkart.matrikkel.auth.credential.AuthenticationChallenger
import no.statkart.matrikkel.auth.credential.extractor.HttpCredentialExtractor
import javax.annotation.Priority
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Alternative
import javax.enterprise.inject.Instance
import javax.inject.Inject
import javax.interceptor.Interceptor
import javax.security.enterprise.identitystore.IdentityStoreHandler

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
