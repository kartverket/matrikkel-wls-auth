package no.statkart.matrikkel.auth.ee.security.webapp;


import no.statkart.matrikkel.auth.weblogic.soteria.SoteriaServerAuthModule;
import org.glassfish.soteria.mechanisms.jaspic.DefaultAuthConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.security.auth.message.config.AuthConfigFactory;
import jakarta.security.auth.message.config.AuthConfigProvider;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.util.Set;

public class WeblogicSoteriaInitializer implements ServletContainerInitializer {
    private static final Logger logger = LoggerFactory.getLogger(WeblogicSoteriaInitializer.class);

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) {
        ctx.addListener(new ForServletContext(ctx.getVirtualServerName()));
    }

    private static class ForServletContext implements ServletContextListener {

        protected static final String LAYER = "HttpServlet";
        private final String virtualServerName;
        private DefaultAuthConfigProvider authConfigProvider;
        private Instance<HttpAuthenticationMechanism> httpAuthenticationMechanisms;
        private HttpAuthenticationMechanism httpAuthenticationMechanism;

        public ForServletContext(String virtualServerName) {
            this.virtualServerName = virtualServerName;
        }

        @Override
        public void contextInitialized(ServletContextEvent sce)  {
            String appContext = virtualServerName + " " + sce.getServletContext().getContextPath();
            httpAuthenticationMechanisms = CDI.current().select(HttpAuthenticationMechanism.class);
            if (!(httpAuthenticationMechanisms.isUnsatisfied() || httpAuthenticationMechanisms.isAmbiguous())) {
                httpAuthenticationMechanism = httpAuthenticationMechanisms.get();
                authConfigProvider = new DefaultAuthConfigProvider(new SoteriaServerAuthModule(httpAuthenticationMechanism));
                AuthConfigFactory.getFactory().registerConfigProvider(
                        authConfigProvider,
                        LAYER,
                        appContext,
                        "Java EE Security (Soteria/WebLogic) " + appContext);
                logger.info("Java EE Security (Soteria) enabled for {}", appContext);
            } else {
                logger.warn("Java EE Security (Soteria) disabled: No HttpAuthenticationMechanisms found for {}", appContext);
            }
        }

        @Override
        public void contextDestroyed(ServletContextEvent sce) {
            String appContext = virtualServerName + " " + sce.getServletContext().getContextPath();
            if (authConfigProvider != null) {
                if (authConfigProvider == AuthConfigFactory.getFactory().getConfigProvider(LAYER, appContext, null)) {
                    AuthConfigFactory.getFactory().registerConfigProvider(null, LAYER, appContext, null);
                    logger.info("Java EE Security (Soteria) cleanup: Servlet context {} destroyed", appContext);
                } else {
                    logger.warn(
                        "Java EE Security (Soteria) cleanup failed: Servlet context {} destroyed - JASPIC had an unexpected {} registered",
                        appContext,
                        AuthConfigProvider.class.getName());
                }
                authConfigProvider = null;
            }
            if (httpAuthenticationMechanism != null) {
                httpAuthenticationMechanisms.destroy(httpAuthenticationMechanism);
                logger.debug("Java EE Security (Soteria) cleanup: HttpAuthenticationMechanism {} destroyed", appContext);
                httpAuthenticationMechanism = null;
            }
        }
    }
}
