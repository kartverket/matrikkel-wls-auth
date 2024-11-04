package no.statkart.matrikkel.auth.identity.basic

import no.statkart.matrikkel.auth.credential.BasicAuthenticationCredentialExt
import no.statkart.matrikkel.auth.credential.extractor.HttpCredentialExtractor
import jakarta.enterprise.context.ApplicationScoped
import jakarta.servlet.http.HttpServletRequest

@ApplicationScoped
open class BasicHttpCredentialExtractor : HttpCredentialExtractor<BasicAuthenticationCredentialExt> {
    override fun getCredential(request: HttpServletRequest, map: Map<*, *>): BasicAuthenticationCredentialExt? {
        return BasicAuthenticationCredentialExt.fromAuthorizationHeader(
            request.getHeader("Authorization"),
            Charsets.UTF_8
        ).orElse(null)
    }
}
