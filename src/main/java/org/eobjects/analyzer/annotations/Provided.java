package org.eobjects.analyzer.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods and fields with the @Provided annotation are used to let
 * AnalyzerBeans retrieve service-objects such as persistent collections. This
 * features ensures separation of concerns: The AnalyzerBeans framework will
 * make sure that persistence is handled and the bean-developer will not have to
 * worry about memory problems related to his/her collection(s).
 * 
 * Valid types for @Provided annotated fields and method arguments are:
 * <ul>
 * <li>List</li>
 * <li>Map</li>
 * </ul>
 * Generic/parameterized types for these collections can be any of:
 * <ul>
 * <li>Boolean</li>
 * <li>Integer</li>
 * <li>Long</li>
 * <li>Double</li>
 * <li>String</li>
 * <li>Byte[] or byte[]</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Documented
@Inherited
public @interface Provided {
}
