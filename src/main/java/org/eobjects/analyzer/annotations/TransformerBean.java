package org.eobjects.analyzer.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes that are annotated with the @TransformerBean annotation are
 * components for data transformation. All @TransformerBean classes must
 * implement the <code>org.eobjects.analyzer.beans.Transformer</code> interface.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface TransformerBean {

	/**
	 * The display name of the Transformer. The display name should be humanly
	 * readable and is presented to the user in User Interfaces.
	 * 
	 * @return the name of the TransformerBean
	 */
	String value();
}
