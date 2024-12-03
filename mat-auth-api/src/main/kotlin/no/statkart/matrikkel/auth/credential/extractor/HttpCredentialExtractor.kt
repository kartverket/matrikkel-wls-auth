package no.statkart.matrikkel.auth.credential.extractor

import jakarta.annotation.Priority
import jakarta.interceptor.Interceptor
import jakarta.security.enterprise.credential.Credential
import jakarta.servlet.http.HttpServletRequest

interface HttpCredentialExtractor<T : Credential> {

    val priority: Int
        get() = generateSequence(javaClass as Class<*>) { it.superclass }
            .map { it.getAnnotation(Priority::class.java) }
            .filterNotNull()
            .firstOrNull()
            ?.value ?: Interceptor.Priority.APPLICATION + 200

    fun getCredential(request: HttpServletRequest, map: Map<*, *>): T?
}
