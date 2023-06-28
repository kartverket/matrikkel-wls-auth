package no.statkart.matrikkel.auth.config.impl;

//import io.smallrye.config.SecuritySupport;

import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.inject.ConfigProducer;
import io.smallrye.config.inject.ConfigProducerUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Specializes;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
@Specializes
public class WeblogicConfigProducer extends ConfigProducer {

    private static final Logger LOG = LoggerFactory.getLogger(WeblogicConfigProducer.class);

    @Inject
    public WeblogicConfigProducer() {
    }

    @Produces
    static Config getConfig(InjectionPoint ip) {
        ClassLoader classLoader = getInjectionPointClassLoader(ip);
        if (classLoader != null) {
            return ConfigProvider.getConfig(classLoader);
        } else {
            return ConfigProvider.getConfig();
        }
    }

    @Dependent
    @Produces
    @ConfigProperty
    protected String produceStringConfigProperty(InjectionPoint ip) {
        return getValue(ip, String.class, getConfig(ip));
    }

    @Dependent
    @Produces
    @ConfigProperty
    protected Long getLongValue(InjectionPoint ip) {
        return getValue(ip, Long.class, getConfig(ip));
    }

    @Dependent
    @Produces
    @ConfigProperty
    protected Integer getIntegerValue(InjectionPoint ip) {
        return getValue(ip, Integer.class, getConfig(ip));
    }

    @Dependent
    @Produces
    @ConfigProperty
    protected Float produceFloatConfigProperty(InjectionPoint ip) {
        return getValue(ip, Float.class, getConfig(ip));
    }

    @Dependent
    @Produces
    @ConfigProperty
    protected Double produceDoubleConfigProperty(InjectionPoint ip) {
        return getValue(ip, Double.class, getConfig(ip));
    }

    @Dependent
    @Produces
    @ConfigProperty
    protected Boolean produceBooleanConfigProperty(InjectionPoint ip) {
        return getValue(ip, Boolean.class, getConfig(ip));
    }

    @Dependent
    @Produces
    @ConfigProperty
    <T> Optional<T> produceOptionalConfigValue(InjectionPoint injectionPoint) {
        return ConfigProducerUtil.getValue(injectionPoint, getConfig(injectionPoint));
    }

    @Dependent
    @Produces
    @ConfigProperty
    <T> Set<T> producesSetConfigProperty(InjectionPoint ip) {
        return ConfigProducerUtil.collectionConfigProperty(ip, getConfig(ip), new HashSet<>());
    }

    @Dependent
    @Produces
    @ConfigProperty
    <T> List<T> producesListConfigProperty(InjectionPoint ip) {
        return ConfigProducerUtil.collectionConfigProperty(ip, getConfig(ip), new ArrayList<>());
    }

    public static ClassLoader getInjectionPointClassLoader(InjectionPoint ip) {
        Annotated annotated = ip.getAnnotated();
        ClassLoader classLoader;
        if (annotated instanceof AnnotatedParameter) {
            classLoader = ((AnnotatedParameter<?>) annotated).getDeclaringCallable().getDeclaringType().getJavaClass().getClassLoader();
        } else if (annotated instanceof AnnotatedField) {
            classLoader = ((AnnotatedField<?>) annotated).getDeclaringType().getJavaClass().getClassLoader();
        } else {
            // SecuritySupport er blitt package scope i nyere versjoner av smallrye-config
            // Kopiert inn koden fra klassen slik at oppførselen skal være det samme
            classLoader = getContextClassLoader();
        }
        return classLoader;
    }


    private static ClassLoader getContextClassLoader() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        } else {
            return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> {
                ClassLoader tccl = null;
                try {
                    tccl = Thread.currentThread().getContextClassLoader();
                } catch (SecurityException e) {
                    LOG.error("Exception while getting class loader", e);
                }
                return tccl;
            });
        }
    }

    public static <T> T getValue
            (InjectionPoint injectionPoint, Class<T> target, Config config) {
        String name = getName(injectionPoint);
        try {
            if (name == null) {
                return null;
            }
            Optional<T> optionalValue = config.getOptionalValue(name, target);
            if (optionalValue.isPresent()) {
                return optionalValue.get();
            } else {
                String defaultValue = getDefaultValue(injectionPoint);
                if (defaultValue != null && !defaultValue.equals(ConfigProperty.UNCONFIGURED_VALUE)) {
                    return ((SmallRyeConfig) config).convert(defaultValue, target);
                } else {
                    return null;
                }
            }
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String getDefaultValue(InjectionPoint injectionPoint) {
        for (Annotation qualifier : injectionPoint.getQualifiers()) {
            if (qualifier.annotationType().equals(ConfigProperty.class)) {
                return ((ConfigProperty) qualifier).defaultValue();
            }
        }
        return null;
    }

    private static String getName(InjectionPoint injectionPoint) {
        for (Annotation qualifier : injectionPoint.getQualifiers()) {
            if (qualifier.annotationType().equals(ConfigProperty.class)) {
                ConfigProperty configProperty = ((ConfigProperty)qualifier);
                return WeblogicConfigExtension.getConfigKey(injectionPoint, configProperty);
            }
        }
        return null;
    }
}
