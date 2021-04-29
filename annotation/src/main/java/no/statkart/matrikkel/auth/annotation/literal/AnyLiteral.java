package no.statkart.matrikkel.auth.annotation.literal;

import javax.enterprise.inject.Any;
import javax.enterprise.util.AnnotationLiteral;

public class AnyLiteral extends AnnotationLiteral<Any> implements Any {
    public static final Any INSTANCE = new AnyLiteral();

    private AnyLiteral() {}
}
