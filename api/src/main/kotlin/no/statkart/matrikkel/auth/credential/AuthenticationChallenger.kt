package no.statkart.matrikkel.auth.credential

import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

interface AuthenticationChallenger {
    fun challenge(
        request: HttpServletRequest,
        response: HttpServletResponse,
        httpMessageContext: HttpMessageContext): Boolean
}