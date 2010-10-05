package org.eobjects.analyzer.beans.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes annotated with @FilterBean are used for filtering and categorizing
 * rows to create subflows where certain rows are processed by only certain
 * successive components.
 * 
 * A @FilterBean annotated class should implement the Filter interface.
 * 
 * @see Filter
 * 
 * @author Kasper Sørensen
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface FilterBean {

	/**
	 * The display name of the FilterBean. The display name should be humanly
	 * readable and is presented to the user in User Interfaces.
	 * 
	 * @return the name of the FilterBean
	 */
	String value();
}
