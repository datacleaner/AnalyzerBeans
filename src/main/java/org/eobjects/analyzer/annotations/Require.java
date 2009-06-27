package org.eobjects.analyzer.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods and fields with the @Require annotation are used to configure an
 * AnalyzerBean before execution. Methods annotated with Require need to have a
 * single argument, equivalent to a property setter/modifier method.
 * 
 * Valid types for @Require annotated fields and method arguments are single
 * instances or arrays of:
 * <ul>
 * <li>Boolean</li>
 * <li>Long</li>
 * <li>Integer</li>
 * <li>Double</li>
 * <li>String</li>
 * <li>Column</li>
 * <li>Table</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Documented
@Inherited
public @interface Require {

	/**
	 * Defines the name of the required configuration property.
	 * 
	 * @return the name of the configuration property
	 */
	String value();
}
