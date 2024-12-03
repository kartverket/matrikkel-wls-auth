package no.statkart.matrikkel.auth.shared

import jakarta.annotation.Priority
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.Initialized
import jakarta.enterprise.event.Observes
import jakarta.enterprise.inject.Alternative
import jakarta.interceptor.Interceptor

@ApplicationScoped
@Alternative
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 200)
class SharedIdentityStoreHandler : AbstractIdentityStoreHandler() {
    internal fun onApplicationStartup(@Observes @Initialized(ApplicationScoped::class) event: Any) {
        // do nothing, trigger creation
    }
}
