package no.statkart.matrikkel.auth.ee.security.webapp;


import no.statkart.matrikkel.auth.weblogic.soteria.SoteriaServerAuthModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.glassfish.soteria.mechanisms.jaspic.DefaultAuthConfigProvider;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Set;

public class WeblogicSoteriaInitializer implements ServletContainerInitializer {
    private static final Logger logger = LogManager.getLogger(WeblogicSoteriaInitializer.class);

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
                logger.info(() -> new StringFormattedMessage("Java EE Security (Soteria) enabled for %s", appContext));
            } else {
                logger.warn(() -> new StringFormattedMessage("Java EE Security (Soteria) disabled: No HttpAuthenticationMechanisms found for %s", appContext));
            }
        }

        @Override
        public void contextDestroyed(ServletContextEvent sce) {
            String appContext = virtualServerName + " " + sce.getServletContext().getContextPath();
            if (authConfigProvider != null) {
                if (authConfigProvider == AuthConfigFactory.getFactory().getConfigProvider(LAYER, appContext, null)) {
                    AuthConfigFactory.getFactory().registerConfigProvider(null, LAYER, appContext, null);
                    logger.info(() -> new StringFormattedMessage("Java EE Security (Soteria) cleanup: Servlet context %s destroyed", appContext));
                } else {
                    logger.warn(() -> new StringFormattedMessage(
                            "Java EE Security (Soteria) cleanup failed: Servlet context %s destroyed - JASPIC had an unexpected %s registered",
                            appContext,
                            AuthConfigProvider.class.getName()));
                }
                authConfigProvider = null;
            }
            if (httpAuthenticationMechanism != null) {
                httpAuthenticationMechanisms.destroy(httpAuthenticationMechanism);
                logger.debug(() -> new StringFormattedMessage("Java EE Security (Soteria) cleanup: HttpAuthenticationMechanism destroyed", appContext));
                httpAuthenticationMechanism = null;
            }
        }
    }
}
