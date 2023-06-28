package no.statkart.matrikkel.auth.credential

import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

interface AuthenticationChallenger {
    fun challenge(
        request: HttpServletRequest,
        response: HttpServletResponse,
        httpMessageContext: HttpMessageContext): Boolean
}
