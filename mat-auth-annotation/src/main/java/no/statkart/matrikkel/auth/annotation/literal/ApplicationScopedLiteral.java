package no.statkart.matrikkel.auth.annotation.literal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.util.AnnotationLiteral;

public class ApplicationScopedLiteral extends AnnotationLiteral<ApplicationScoped> implements ApplicationScoped {
    public static final ApplicationScoped INSTANCE = new ApplicationScopedLiteral();

    private ApplicationScopedLiteral() {}
}
