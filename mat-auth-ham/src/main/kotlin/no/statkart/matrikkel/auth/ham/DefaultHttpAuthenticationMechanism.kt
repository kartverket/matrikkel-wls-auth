package no.statkart.matrikkel.auth.ham

import no.statkart.matrikkel.auth.credential.AuthenticationChallenger
import no.statkart.matrikkel.auth.credential.extractor.HttpCredentialExtractor
import jakarta.annotation.Priority
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Alternative
import jakarta.enterprise.inject.Instance
import jakarta.inject.Inject
import jakarta.interceptor.Interceptor
import jakarta.security.enterprise.AuthenticationStatus
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext
import jakarta.security.enterprise.identitystore.CredentialValidationResult
import jakarta.security.enterprise.identitystore.IdentityStoreHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import java.util.Collections

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
    private val identityStoreHandler: IdentityStoreHandler,
    private val credentialExtractorInstance: Instance<HttpCredentialExtractor<*>>,
    authenticationChallengers: Instance<AuthenticationChallenger>
) : HttpAuthenticationMechanism {
    private val logger = LoggerFactory.getLogger(DefaultHttpAuthenticationMechanism::class.java)
    private val authenticationChallenger = authenticationChallengers.run { if (isUnsatisfied) null else get() }

    override fun validateRequest(
        request: HttpServletRequest,
        response: HttpServletResponse,
        httpMessageContext: HttpMessageContext
    ): AuthenticationStatus = runCatching {
        if (!httpMessageContext.isProtected) {
            return httpMessageContext.doNothing()
        }
        val messageInfoMap = Collections.unmodifiableMap(httpMessageContext.messageInfo.map)
        val credentialExtractors = credentialExtractorInstance.sortedBy { it.priority }.reversed()
        val validationResult = try {
            credentialExtractors
                .mapNotNull {  it.getCredential(request, messageInfoMap) }
                .map { credential -> identityStoreHandler.validate(credential) }
                .find { validation -> validation.status != CredentialValidationResult.Status.NOT_VALIDATED }
                ?: CredentialValidationResult.NOT_VALIDATED_RESULT
        } finally {
            credentialExtractors.forEach { credentialExtractorInstance.destroy(it) }
        }

        return if (validationResult.status == CredentialValidationResult.Status.VALID) {
            httpMessageContext.notifyContainerAboutLogin(
                validationResult.callerPrincipal.name,
                validationResult.callerGroups)
        } else {
            if (authenticationChallenger?.challenge(request,response,httpMessageContext) == true
                && response.containsHeader("WWW-Authenticate")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN)
            }
            AuthenticationStatus.SEND_FAILURE
        }
    }
        .onFailure { logger.error("[MAT-AUTH] feilet", it) }
        .getOrThrow()
}
