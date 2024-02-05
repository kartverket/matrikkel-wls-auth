package no.statkart.matrikkel.auth.annotation.literal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.util.AnnotationLiteral;

public class ApplicationScopedLiteral extends AnnotationLiteral<ApplicationScoped> implements ApplicationScoped {
    public static final ApplicationScoped INSTANCE = new ApplicationScopedLiteral();

    private ApplicationScopedLiteral() {}
}
