package no.statkart.matrikkel.auth.shared

import jakarta.security.enterprise.identitystore.CredentialValidationResult
import jakarta.security.enterprise.identitystore.IdentityStore

sealed class IdentityStoreValidationResult(val status: CredentialValidationResult) {
    class Valid(val identityStore: IdentityStore, status: CredentialValidationResult): IdentityStoreValidationResult(status)
    class Invalid(status: CredentialValidationResult): IdentityStoreValidationResult(status)

    companion object {
        val INVALID = Invalid(CredentialValidationResult.INVALID_RESULT)
        val NOT_VALIDATED = Invalid(CredentialValidationResult.NOT_VALIDATED_RESULT)

        fun of(identityStore: IdentityStore, status: CredentialValidationResult): IdentityStoreValidationResult {
            return when (status.status) {
                CredentialValidationResult.Status.NOT_VALIDATED -> Invalid(status)
                CredentialValidationResult.Status.INVALID -> Invalid(status)
                CredentialValidationResult.Status.VALID -> Valid(identityStore, status)
            }
        }
    }
}
