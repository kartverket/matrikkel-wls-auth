package no.statkart.matrikkel.auth.ham

import no.statkart.matrikkel.auth.credential.AuthenticationChallenger
import no.statkart.matrikkel.auth.credential.extractor.HttpCredentialExtractor
import java.util.*
import javax.enterprise.inject.Instance
import javax.security.enterprise.AuthenticationStatus
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext
import javax.security.enterprise.identitystore.CredentialValidationResult
import javax.security.enterprise.identitystore.IdentityStoreHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


abstract class AbstractDefaultHttpAuthenticationMechanism protected constructor(
    private val identityStoreHandler: IdentityStoreHandler,
    private val credentialExtractorInstance: Instance<HttpCredentialExtractor<*>>,
    authenticationChallengers: Instance<AuthenticationChallenger>
) : HttpAuthenticationMechanism {

    private val authenticationChallenger = authenticationChallengers.run { if (isUnsatisfied) null else get() }

    override fun validateRequest(
        request: HttpServletRequest,
        response: HttpServletResponse,
        httpMessageContext: HttpMessageContext
    ): AuthenticationStatus {
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

}
