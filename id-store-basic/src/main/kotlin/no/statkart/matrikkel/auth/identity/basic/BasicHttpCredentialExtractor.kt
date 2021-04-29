package no.statkart.matrikkel.auth.identity.basic

import no.statkart.matrikkel.auth.credential.BasicAuthenticationCredentialExt
import no.statkart.matrikkel.auth.credential.extractor.HttpCredentialExtractor
import javax.enterprise.context.ApplicationScoped
import javax.servlet.http.HttpServletRequest

@ApplicationScoped
class BasicHttpCredentialExtractor : HttpCredentialExtractor<BasicAuthenticationCredentialExt> {
    override fun getCredential(request: HttpServletRequest, map: Map<*, *>): BasicAuthenticationCredentialExt? {
        return BasicAuthenticationCredentialExt.fromAuthorizationHeader(
            request.getHeader("Authorization"),
            Charsets.UTF_8
        ).orElse(null)
    }
}