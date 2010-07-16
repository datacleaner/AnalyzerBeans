package org.eobjects.analyzer.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Methods and fields with the @Configured annotation are used to configure an
 * AnalyzerBean before execution. Typically, the @Configured annotated
 * methods/fields will be used to prompt the user for configuration. Methods
 * annotated with @Configured need to have a single argument, equivalent to a
 * property setter/modifier method.
 * 
 * Valid types for @Configured annotated fields and method arguments are single
 * instances or arrays of:
 * <ul>
 * <li>Boolean</li>
 * <li>Long</li>
 * <li>Integer</li>
 * <li>Double</li>
 * <li>String</li>
 * <li>org.eobjects.analyzer.data.InputColumn</li>
 * </ul>
 * 
 * Additionally exploring analyzers are allowed to inject these @Configured
 * types (for querying purposes):
 * 
 * <ul>
 * <li>dk.eobjects.metamodel.schema.Column</li>
 * <li>dk.eobjects.metamodel.schema.Table</li>
 * <li>dk.eobjects.metamodel.schema.Schema</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Documented
@Inherited
@Qualifier
public @interface Configured {

	/**
	 * Defines the name of the required configuration property.
	 * 
	 * @return the name of the configuration property
	 */
	String value() default "";
}
