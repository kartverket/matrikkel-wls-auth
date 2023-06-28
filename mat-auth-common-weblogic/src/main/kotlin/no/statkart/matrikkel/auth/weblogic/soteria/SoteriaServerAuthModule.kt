package no.statkart.matrikkel.auth.weblogic.soteria

import org.glassfish.soteria.mechanisms.HttpMessageContextImpl
import org.glassfish.soteria.mechanisms.jaspic.Jaspic
import java.util.*
import javax.security.auth.Subject
import javax.security.auth.callback.CallbackHandler

class SoteriaServerAuthModule(private val httpAuthenticationMechanism: jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism) :
    jakarta.security.auth.message.module.ServerAuthModule {
    private var handler: CallbackHandler? = null
    override fun initialize(
        requestPolicy: jakarta.security.auth.message.MessagePolicy?,
        responsePolicy: jakarta.security.auth.message.MessagePolicy?,
        handler: CallbackHandler,
        options: Map<*, *>?
    ) {
        this.handler = handler
    }

    override fun getSupportedMessageTypes(): Array<Class<*>> {
        return SUPPORTED_MESSAGE_TYPES.toTypedArray()
    }

    @Throws(jakarta.security.auth.message.AuthException::class)
    override fun validateRequest(
        messageInfo: jakarta.security.auth.message.MessageInfo,
        clientSubject: Subject,
        serviceSubject: Subject?
    ): jakarta.security.auth.message.AuthStatus {
        val msgContext: jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext = HttpMessageContextImpl(handler, messageInfo, clientSubject)
        var status: jakarta.security.enterprise.AuthenticationStatus? =
            jakarta.security.enterprise.AuthenticationStatus.NOT_DONE
        Jaspic.setLastAuthenticationStatus(msgContext.request, status)
        status = try {
            httpAuthenticationMechanism.validateRequest(
                msgContext.request,
                msgContext.response,
                msgContext
            )
        } catch (e: jakarta.security.enterprise.AuthenticationException) {
            // In case of an explicit AuthException, status will
            // be set to SEND_FAILURE, for any other (non checked) exception
            // the status will be the default NOT_DONE
            Jaspic.setLastAuthenticationStatus(msgContext.request,
                jakarta.security.enterprise.AuthenticationStatus.SEND_FAILURE
            )
            throw (jakarta.security.auth.message.AuthException("Authentication failure in HttpAuthenticationMechanism")
                .initCause(e) as jakarta.security.auth.message.AuthException)
        }
        Jaspic.setLastAuthenticationStatus(msgContext.request, status)
        return Jaspic.fromAuthenticationStatus(status)
    }

    @Throws(jakarta.security.auth.message.AuthException::class)
    override fun secureResponse(messageInfo: jakarta.security.auth.message.MessageInfo, serviceSubject: Subject?): jakarta.security.auth.message.AuthStatus {
        val msgContext: jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext = HttpMessageContextImpl(handler, messageInfo, null)
        return try {
            val status = httpAuthenticationMechanism
                .secureResponse(
                    msgContext.request,
                    msgContext.response,
                    msgContext
                )
            val authStatus = Jaspic.fromAuthenticationStatus(status)
            if (authStatus === jakarta.security.auth.message.AuthStatus.SUCCESS) {
                jakarta.security.auth.message.AuthStatus.SEND_SUCCESS
            } else authStatus
        } catch (e: jakarta.security.enterprise.AuthenticationException) {
            throw (jakarta.security.auth.message.AuthException("Secure response failure in HttpAuthenticationMechanism")
                .initCause(e) as jakarta.security.auth.message.AuthException)
        }
    }

    /**
     * Called in response to a [HttpServletRequest.logout] call.
     *
     */
    override fun cleanSubject(messageInfo: jakarta.security.auth.message.MessageInfo, subject: Subject) {
        val msgContext: jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext = HttpMessageContextImpl(handler, messageInfo, subject)
        httpAuthenticationMechanism.cleanSubject(msgContext.request, msgContext.response, msgContext)
    }

    companion object {
        private val SUPPORTED_MESSAGE_TYPES = Collections.unmodifiableList(
            Arrays.asList(
                jakarta.servlet.http.HttpServletRequest::class.java, jakarta.servlet.http.HttpServletResponse::class.java
            )
        )
    }
}
