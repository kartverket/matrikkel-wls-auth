package no.statkart.matrikkel.auth.config.impl;

import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.inject.ConfigProducer;
import io.smallrye.config.inject.ConfigProducerUtil;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
@Specializes
public class WeblogicConfigProducer extends ConfigProducer {
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
    String produceStringConfigProperty(InjectionPoint ip) {
        return getValue(ip, String.class, getConfig(ip));
    }

    @Dependent
    @Produces
    @ConfigProperty
    Long getLongValue(InjectionPoint ip) {
        return getValue(ip, Long.class, getConfig(ip));
    }

    @Dependent
    @Produces
    @ConfigProperty
    Integer getIntegerValue(InjectionPoint ip) {
        return getValue(ip, Integer.class, getConfig(ip));
    }

    @Dependent
    @Produces
    @ConfigProperty
    Float produceFloatConfigProperty(InjectionPoint ip) {
        return getValue(ip, Float.class, getConfig(ip));
    }

    @Dependent
    @Produces
    @ConfigProperty
    Double produceDoubleConfigProperty(InjectionPoint ip) {
        return getValue(ip, Double.class, getConfig(ip));
    }

    @Dependent
    @Produces
    @ConfigProperty
    Boolean produceBooleanConfigProperty(InjectionPoint ip) {
        return getValue(ip, Boolean.class, getConfig(ip));
    }

    @Dependent
    @Produces
    @ConfigProperty
    <T> Optional<T> produceOptionalConfigValue(InjectionPoint injectionPoint) {
        return ConfigProducerUtil.optionalConfigValue(injectionPoint, getConfig(injectionPoint));
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

    /**
     * @return NB: Callers should not leak this Classloader
     */
    private static ClassLoader getInjectionPointClassLoader(InjectionPoint ip) {
        Annotated annotated = ip.getAnnotated();
        if (annotated instanceof AnnotatedParameter) {
            return ((AnnotatedParameter<?>) annotated).getDeclaringCallable().getDeclaringType().getJavaClass().getClassLoader();
        } else if (annotated instanceof AnnotatedField) {
            return ((AnnotatedField<?>) annotated).getDeclaringType().getJavaClass().getClassLoader();
        } else {
            return null;
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
