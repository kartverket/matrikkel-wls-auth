package no.statkart.matrikkel.auth.util.cdi

import java.lang.reflect.Type
import javax.enterprise.inject.spi.*

sealed class AnnotatedImpl(
    annotated: Annotated,
    annotations: Set<Annotation>,
    typeFilter: (Type) -> Boolean = { _ -> true}
) : Annotated {
    private val baseType = annotated.baseType
    private val typeClosure = annotated.typeClosure.filter(typeFilter).toSet()
    private val annotations = annotations.toSet()

    constructor(
        annotated: Annotated,
        annotationFilter: (Annotation) -> Boolean = { _ -> true },
        typeFilter: (Type) -> Boolean = { _ -> true }
    ) : this(annotated, annotated.annotations.filter(annotationFilter).toSet(), typeFilter)

    final override fun getBaseType(): Type {
        return baseType
    }

    final override fun getTypeClosure(): Set<Type> {
        return typeClosure
    }

    @Suppress("UNCHECKED_CAST")
    final override fun <T : Annotation> getAnnotation(annotationType: Class<T>): T? {
        return annotations.firstOrNull { it.annotationClass == annotationType } as T?
    }

    final override fun getAnnotations(): Set<Annotation> {
        return annotations
    }

    final override fun isAnnotationPresent(annotationType: Class<out Annotation>): Boolean {
        return annotations.any { it.annotationClass == annotationType }
    }
}

class AnnotatedTypeImpl<X>(
    annotated: AnnotatedType<X>,
    annotations: Set<Annotation>,
    typeFilter: (Type) -> Boolean = { _ -> true},
    constructorMapping: (AnnotatedConstructor<X>) -> (AnnotatedConstructor<X>?) = { it },
    methodMapping: (AnnotatedMethod<in X>) -> (AnnotatedMethod<in X>?) = { it },
    fieldMapping: (AnnotatedField<in X>) -> (AnnotatedField<in X>?) = { it }
) : AnnotatedImpl(annotated, annotations, typeFilter), AnnotatedType<X> {

    private val javaClass = annotated.javaClass
    private val constructors = annotated.constructors.mapNotNull(constructorMapping).toSet()
    private val methods = annotated.methods.mapNotNull(methodMapping).toSet()
    private val fields = annotated.fields.mapNotNull(fieldMapping).toSet()

    constructor(
        annotated: AnnotatedType<X>,
        annotationFilter: (Annotation) -> Boolean = { _ -> true },
        typeFilter: (Type) -> Boolean = { _ -> true},
        constructorMapping: (AnnotatedConstructor<X>) -> (AnnotatedConstructor<X>?) = { it },
        methodMapping: (AnnotatedMethod<in X>) -> (AnnotatedMethod<in X>?) = { it },
        fieldMapping: (AnnotatedField<in X>) -> (AnnotatedField<in X>?) = { it }
    ) : this(
        annotated,
        annotated.annotations.filter(annotationFilter).toSet(),
        typeFilter,
        constructorMapping,
        methodMapping,
        fieldMapping
    )

    override fun getJavaClass(): Class<X> = javaClass

    override fun getConstructors(): Set<AnnotatedConstructor<X>> = constructors

    override fun getMethods(): Set<AnnotatedMethod<in X>> = methods

    override fun getFields(): Set<AnnotatedField<in X>> = fields
}