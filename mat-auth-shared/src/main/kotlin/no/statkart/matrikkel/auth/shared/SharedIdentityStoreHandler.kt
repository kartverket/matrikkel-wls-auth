package no.statkart.matrikkel.auth.shared

import javax.annotation.Priority
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.context.Initialized
import javax.enterprise.event.Observes
import javax.enterprise.inject.Alternative
import javax.interceptor.Interceptor

@ApplicationScoped
@Alternative
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 200)
class SharedIdentityStoreHandler : AbstractIdentityStoreHandler() {
    internal fun onApplicationStartup(@Observes @Initialized(ApplicationScoped::class) event: Any) {
        // do nothing, trigger creation
    }
}
