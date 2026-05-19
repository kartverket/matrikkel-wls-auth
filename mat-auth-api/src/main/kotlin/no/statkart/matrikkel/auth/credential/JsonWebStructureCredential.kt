package no.statkart.matrikkel.auth.credential

import jakarta.security.enterprise.credential.Credential

data class JsonWebStructureCredential(
    val compactSerialization: String,
    val fromPasswordGrant: Boolean,
    val username: String? = null
) : Credential
