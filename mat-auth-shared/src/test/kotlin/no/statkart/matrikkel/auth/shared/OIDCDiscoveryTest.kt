package no.statkart.matrikkel.auth.shared

import jakarta.json.Json
import no.statkart.matrikkel.auth.credential.AuthConfigKeys
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OIDCDiscoveryTest {
    val validObject = Json.createObjectBuilder()
        .add("issuer", "myIss")
        .add("authorization_endpoint", "http://dummy.com/auth")
        .add("token_endpoint", "http://dummy.com/token")
        .add("jwks_uri", "http://dummy.com/tokens")
        .add("userinfo_endpoint", "http://dummy.com/userinfo")
        .build()

    @Test
    fun `should fail for missing mandatory attributes`() {
        val jsonObject = Json.createObjectBuilder(validObject)
            .remove("issuer")
            .build()

        val result = OIDCDiscovery.propertyMapFromMetadataJson(jsonObject)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should not fail for missing optional attributes`() {
        val jsonObject = Json.createObjectBuilder(validObject)
            .remove("userinfo_endpoint")
            .build()

        val result = OIDCDiscovery.propertyMapFromMetadataJson(jsonObject)

        assertTrue(result.isRight())
    }

    @Test
    fun `should remove keys without value`() {
        val jsonObject = Json.createObjectBuilder(validObject)
            .remove("userinfo_endpoint")
            .build()

        val result = OIDCDiscovery.propertyMapFromMetadataJson(jsonObject)
            .getOrNull()

        assertFalse(result?.containsKey(AuthConfigKeys.USERINFO_URL) ?: true)
    }
}
