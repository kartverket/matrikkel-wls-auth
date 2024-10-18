package no.statkart.matrikkel.auth.credential.extractor

import javax.annotation.Priority
import javax.interceptor.Interceptor
import javax.security.enterprise.credential.Credential
import javax.servlet.http.HttpServletRequest

interface HttpCredentialExtractor<T : Credential> {

    val priority: Int
        get() = generateSequence(javaClass as Class<*>) { it.superclass }
            .map { it.getAnnotation(Priority::class.java) }
            .filterNotNull()
            .firstOrNull()
            ?.value ?: Interceptor.Priority.APPLICATION + 200

    fun getCredential(request: HttpServletRequest, map: Map<*, *>): T?
}
