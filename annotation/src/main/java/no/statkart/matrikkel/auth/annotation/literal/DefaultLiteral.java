package no.statkart.matrikkel.auth.annotation.literal;

import jakarta.enterprise.inject.Default;
import jakarta.enterprise.util.AnnotationLiteral;

public class DefaultLiteral extends AnnotationLiteral<Default> implements Default {
    public static final Default INSTANCE = new DefaultLiteral();

    private DefaultLiteral() {}
}
