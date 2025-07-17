package no.statkart.matrikkel.auth.shared

import jakarta.security.enterprise.CallerPrincipal
import jakarta.security.enterprise.credential.Credential
import jakarta.security.enterprise.identitystore.CredentialValidationResult
import jakarta.security.enterprise.identitystore.IdentityStore
import no.statkart.matrikkel.auth.shared.AbstractIdentityStoreHandler.Companion.validate
import no.statkart.matrikkel.auth.shared.AbstractIdentityStoreHandler.Companion.extractProvidedGroups
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AbstractIdentityStoreHandlerTest {
    @Nested
    inner class ValidateTest {
        @Test
        fun `invalid result before valid should take presedence`() {
            val result = listOf(
                NamedIdentityStore("1", CredentialValidationResult.Status.INVALID),
                NamedIdentityStore("2", CredentialValidationResult.Status.VALID),
                NamedIdentityStore("3", CredentialValidationResult.Status.INVALID),
                NamedIdentityStore("4", CredentialValidationResult.Status.VALID),
            ).validate(object : Credential {})

            assertTrue(result is IdentityStoreValidationResult.Invalid)
            assertEquals(CredentialValidationResult.Status.INVALID, result.status.status)
        }

        @Test
        fun `invalid result after valid should take presedence`() {
            val result = listOf(
                NamedIdentityStore("2", CredentialValidationResult.Status.VALID),
                NamedIdentityStore("1", CredentialValidationResult.Status.INVALID),
                NamedIdentityStore("3", CredentialValidationResult.Status.INVALID),
            ).validate(object : Credential {})

            assertTrue(result is IdentityStoreValidationResult.Invalid)
            assertEquals(CredentialValidationResult.Status.INVALID, result.status.status)
        }

        @Test
        fun `first valid return should take presedence`() {
            val result = listOf(
                NamedIdentityStore("1", CredentialValidationResult.Status.VALID),
                NamedIdentityStore("2", CredentialValidationResult.Status.VALID),
            ).validate(object : Credential {})

            assertTrue(result is IdentityStoreValidationResult.Valid)
            assertEquals(CredentialValidationResult.Status.VALID, result.status.status)
            assertEquals("1", ((result as IdentityStoreValidationResult.Valid).identityStore as NamedIdentityStore).id)
        }
    }

    @Nested
    inner class ExtractGroupsTest {
        val identityStore = NamedIdentityStore(
            id = "1",
            result = CredentialValidationResult.Status.VALID,
            callerGroups = setOf("admin")
        )
        val validation = IdentityStoreValidationResult.Valid(
            identityStore = identityStore,
            status = identityStore.validate(object : Credential {})
        )


        @Test
        fun name() {
            val authIdentityStores: List<IdentityStore> = listOf(
                NamedIdentityStore("1", CredentialValidationResult.Status.INVALID, setOf("1")),
                NamedIdentityStore("2", CredentialValidationResult.Status.VALID, setOf("2")),
            )

            val groups = authIdentityStores.extractProvidedGroups(validation)

            assertTrue(groups.contains("admin"))
            assertTrue(groups.contains("1"))
            assertTrue(groups.contains("2"))
        }
    }

    class NamedIdentityStore(
        val id: String,
        private val result: CredentialValidationResult.Status,
        private val callerGroups: Set<String> = emptySet(),
    ) : IdentityStore {
        override fun validate(credential: Credential?): CredentialValidationResult {
            return when (result) {
                CredentialValidationResult.Status.NOT_VALIDATED -> CredentialValidationResult.NOT_VALIDATED_RESULT
                CredentialValidationResult.Status.INVALID -> CredentialValidationResult.INVALID_RESULT
                CredentialValidationResult.Status.VALID -> CredentialValidationResult(
                    CallerPrincipal("anon")
                )
            }
        }

        override fun getCallerGroups(validationResult: CredentialValidationResult?): Set<String?>? {
            return callerGroups
        }
    }
}
