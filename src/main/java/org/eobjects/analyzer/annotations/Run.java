package org.eobjects.analyzer.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a method as the @Run method of an Analyser. A @Run
 * method can contain parameters of the following types, which will be
 * automatically injected:
 * <ul>
 * <li>The DataContext in use / to be used (required for EXPLORING type
 * analysers)</li>
 * <li>The Row to be processed (required for ROW_PROCESSING type analysers)</li>
 * <li>Long which will represent the distinct count of the row (optional for
 * ROW_PROCESSING type analysers)</li>
 * <li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
public @interface Run {
}