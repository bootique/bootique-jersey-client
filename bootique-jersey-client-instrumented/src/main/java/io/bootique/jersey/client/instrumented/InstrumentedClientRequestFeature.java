package io.bootique.jersey.client.instrumented;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * A binding annotation for a JAX RS client feature that collects request
 * metrics.
 * 
 * @since 0.5
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface InstrumentedClientRequestFeature {

}
