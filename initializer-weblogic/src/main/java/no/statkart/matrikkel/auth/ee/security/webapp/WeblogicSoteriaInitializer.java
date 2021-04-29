package no.statkart.matrikkel.auth.ee.security.webapp;


import no.statkart.matrikkel.auth.weblogic.soteria.SoteriaServerAuthModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.glassfish.soteria.mechanisms.jaspic.DefaultAuthConfigProvider;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;

public class WeblogicSoteriaInitializer implements ServletContainerInitializer {
    private static final Logger logger = LogManager.getLogger(WeblogicSoteriaInitializer.class);

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) {
        ctx.addListener(new ForServletContext(ctx.getVirtualServerName()));
    }

    private static class ForServletContext implements ServletContextListener {

        private final String virtualServerName;
        private Instance<HttpAuthenticationMechanism> httpAuthenticationMechanisms;
        private HttpAuthenticationMechanism httpAuthenticationMechanism;
        private String registrationId;

        public ForServletContext(String virtualServerName) {
            this.virtualServerName = virtualServerName;
        }

        @Override
        public void contextInitialized(ServletContextEvent sce)  {
            String appContext = virtualServerName + " " + sce.getServletContext().getContextPath();
            httpAuthenticationMechanisms = CDI.current().select(HttpAuthenticationMechanism.class);
            if (!(httpAuthenticationMechanisms.isUnsatisfied() || httpAuthenticationMechanisms.isAmbiguous())) {
                httpAuthenticationMechanism = httpAuthenticationMechanisms.get();
                AuthConfigFactory.getFactory().registerConfigProvider(
                        new DefaultAuthConfigProvider(new SoteriaServerAuthModule(httpAuthenticationMechanism)),
                        "HttpServlet",
                        appContext,
                        "Java EE Security (Soteria/WebLogic) " + appContext);
                logger.info(() -> new StringFormattedMessage("Java EE Security (Soteria) enabled for %s", appContext));
            } else {
                logger.warn(() -> new StringFormattedMessage("Java EE Security (Soteria) disabled: No HttpAuthenticationMechanisms found for %s", appContext));
            }
        }

        @SuppressWarnings("java:S1905") // falsk positiv, lambda må castes pga overloads
        @Override
        public void contextDestroyed(ServletContextEvent sce) {
            if (registrationId != null) {
                AccessController.doPrivileged((PrivilegedAction<Boolean>) () ->
                        AuthConfigFactory.getFactory().removeRegistration(registrationId)
                );
                registrationId = null;
            }
            if (httpAuthenticationMechanism != null) {
                httpAuthenticationMechanisms.destroy(httpAuthenticationMechanism);
                httpAuthenticationMechanisms = null;
            }
        }
    }
}
