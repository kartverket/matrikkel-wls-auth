package no.statkart.matrikkel.auth.credential

import org.eclipse.microprofile.jwt.config.Names

object AuthConfigKeys {
    // Hentes fra properties
    const val CLIENT_ID = "matrikkel.oauth.client.id"
    const val CLIENT_SECRET = "matrikkel.oauth.client.secret"
    const val AZP = "matrikkel.ouath.azp"
    const val AUD = "matrikkel.oauth.aud"
    const val DISCOVERY_URL = "matrikkel.oauth.discovery.url"
    const val VERIFIER_PUBLIC_KEY_ALG = "mp.jwt.verify.publickey.algorithm"

    // Hentes fra discovery URL
    const val AUTHORIZATION_URL = "matrikkel.oauth.authorization.url"
    const val TOKEN_URL = "matrikkel.oauth.token.url"
    const val USERINFO_URL = "matrikkel.oauth.user.info.url"
    const val ISSUER = Names.ISSUER
    const val VERIFIER_PUBLIC_KEY_LOCATION = Names.VERIFIER_PUBLIC_KEY_LOCATION
}