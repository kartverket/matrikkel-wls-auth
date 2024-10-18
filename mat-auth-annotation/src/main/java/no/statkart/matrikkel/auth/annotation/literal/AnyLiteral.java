package no.statkart.matrikkel.auth.annotation.literal;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.util.AnnotationLiteral;

public class AnyLiteral extends AnnotationLiteral<Any> implements Any {
    public static final Any INSTANCE = new AnyLiteral();

    private AnyLiteral() {}
}
