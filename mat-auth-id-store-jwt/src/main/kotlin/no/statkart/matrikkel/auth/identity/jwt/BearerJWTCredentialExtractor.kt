package no.statkart.matrikkel.auth.identity.jwt

import no.statkart.matrikkel.auth.credential.JsonWebStructureCredential
import no.statkart.matrikkel.auth.credential.extractor.HttpCredentialExtractor
import jakarta.enterprise.context.ApplicationScoped
import jakarta.servlet.http.HttpServletRequest

@ApplicationScoped
class BearerJWTCredentialExtractor : HttpCredentialExtractor<JsonWebStructureCredential> {
    override fun getCredential(request: HttpServletRequest, map: Map<*, *>): JsonWebStructureCredential? =
        request.getHeader("Authorization")?.let { credentials(it) }?.let {
            JsonWebStructureCredential(it, false)
        }

    companion object {
        private fun credentials(s: String?): String? {
            if (s == null || s.length <= 10) return null
            return if (!s.substring(0, 7).equals("Bearer ", ignoreCase = true)) {
                null
            } else {
                s.substring(7)
            }
        }
    }
}
