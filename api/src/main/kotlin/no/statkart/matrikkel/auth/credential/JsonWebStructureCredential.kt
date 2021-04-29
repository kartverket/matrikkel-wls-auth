package no.statkart.matrikkel.auth.credential

import javax.security.enterprise.credential.Credential

data class JsonWebStructureCredential(val compactSerialization: String) : Credential {
}