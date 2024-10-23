package no.statkart.matrikkel.auth.util.cdi

import no.statkart.matrikkel.auth.annotation.literal.AnyLiteral
import no.statkart.matrikkel.auth.annotation.literal.DefaultLiteral
import java.lang.IllegalArgumentException
import java.util.*
import java.util.Collections.newSetFromMap
import jakarta.enterprise.inject.spi.*


val Annotated.annotatedType: AnnotatedType<*>
    get() = when(this) {
        is AnnotatedType<*> -> this
        is AnnotatedMember<*> -> this.declaringType.annotatedType
        is AnnotatedParameter<*> -> this.declaringCallable.declaringType
        else -> throw IllegalArgumentException()
    }

inline val Extension.defaultQualifiers: Set<Annotation>
    get() = CdiConstants.defaultQualifiers


object CdiConstants {
    @JvmStatic
    val defaultQualifiers: Set<Annotation> = setOf(AnyLiteral.INSTANCE, DefaultLiteral.INSTANCE)
}
