package no.statkart.matrikkel.auth.ee.security.webapp;

import no.statkart.matrikkel.auth.weblogic.soteria.AbstractSoteriaIdentityStoreHandler;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Alternative;
import jakarta.interceptor.Interceptor;
import jakarta.servlet.ServletContext;

@ApplicationScoped
@Alternative
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 210)
public class SoteriaIdentityStoreHandler extends AbstractSoteriaIdentityStoreHandler {
    void onApplicationStartup(@Observes @Initialized(ApplicationScoped.class) ServletContext event) {
        // do nothing, trigger creation
    }
}

