package no.statkart.matrikkel.auth.ee.security.webapp;

import no.statkart.matrikkel.auth.weblogic.soteria.AbstractSoteriaIdentityStoreHandler;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.interceptor.Interceptor;
import javax.servlet.ServletContext;

@ApplicationScoped
@Alternative
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 210)
public class SoteriaIdentityStoreHandler extends AbstractSoteriaIdentityStoreHandler {
    void onApplicationStartup(@Observes @Initialized(ApplicationScoped.class) ServletContext event) {
        // do nothing, trigger creation
    }
}
