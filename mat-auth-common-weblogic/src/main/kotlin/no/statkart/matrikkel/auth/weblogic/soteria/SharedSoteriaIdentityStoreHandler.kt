package no.statkart.matrikkel.auth.weblogic.soteria

import jakarta.annotation.Priority
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.Initialized
import jakarta.enterprise.event.Observes
import jakarta.enterprise.inject.Alternative
import jakarta.interceptor.Interceptor

@ApplicationScoped
@Alternative
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 200)
open class SharedSoteriaIdentityStoreHandler : AbstractSoteriaIdentityStoreHandler() {
    internal open fun onApplicationStartup(@Observes @Initialized(ApplicationScoped::class) event: Any) {
        // do nothing, trigger creation
    }
}
