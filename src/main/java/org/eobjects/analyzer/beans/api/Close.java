package org.eobjects.analyzer.beans.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a method as a closing method. The method is invoked to
 * release resources that the object is holding (such as open files).
 * AnalyzerBeans can either annotation methods with this annotation or implement
 * the java.io.Closeable interface to ensure that resources are released.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
public @interface Close {
}