package org.eobjects.analyzer.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a method as an initializing method. Use this annotation
 * if you need to initialize the state of a bean before it starts executing.
 * 
 * The method is invoked after any @Configured and @Provided methods/fields are
 * invoked/assigned but before any @Run annotated methods are invoked.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
public @interface Initialize {
}