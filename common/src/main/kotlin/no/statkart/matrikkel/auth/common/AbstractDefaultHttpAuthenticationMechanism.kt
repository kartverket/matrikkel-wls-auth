package no.statkart.matrikkel.auth.common

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

    private val alreadyChallengedKey = AbstractDefaultHttpAuthenticationMechanism::class.java.name + ".alreadyChallenged"
    private val authenticationChallenger = authenticationChallengers.run { if (isUnsatisfied) null else get() }

    override fun validateRequest(
        request: HttpServletRequest,
        response: HttpServletResponse,
        httpMessageContext: HttpMessageContext
    ): AuthenticationStatus {
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

        return when (validationResult.status) {
            CredentialValidationResult.Status.VALID -> httpMessageContext.notifyContainerAboutLogin(
                validationResult.callerPrincipal.name,
                validationResult.callerGroups
            )
            CredentialValidationResult.Status.INVALID -> {
                httpMessageContext.response.sendError(HttpServletResponse.SC_FORBIDDEN)
                AuthenticationStatus.SEND_FAILURE
            }
            CredentialValidationResult.Status.NOT_VALIDATED, null -> {
                if (httpMessageContext.isProtected) {
                    if (httpMessageContext.messageInfo.map.containsKey(alreadyChallengedKey)) {
                      response.sendError(HttpServletResponse.SC_FORBIDDEN)
                      AuthenticationStatus.SEND_FAILURE
                    } else if (authenticationChallenger != null && authenticationChallenger.challenge(request,response,httpMessageContext)) {
                        response.status = HttpServletResponse.SC_UNAUTHORIZED
                        response.sendError(response.status)
                        httpMessageContext.messageInfo.map[alreadyChallengedKey] = true.toString()
                        AuthenticationStatus.SEND_CONTINUE
                    } else {
                        httpMessageContext.responseUnauthorized()
                    }
                } else {
                    httpMessageContext.doNothing()
                }
            }
        }
    }

}