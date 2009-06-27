package org.eobjects.analyzer.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eobjects.analyzer.engine.ExecutionType;


/**
 * Classes that are annotated with the @Analyser annotation are components for
 * data analysis.
 * 
 * The life-cycle of an analyser is as follows:
 * <ul>
 * <li>Instantiation. All analysers need to provide a no-args constructor</li>
 * <li>All methods or fields with the Require annotation are invoked/assigned to
 * configure the Analyser before execution</li>
 * <li>All methods with the Run annotation are executed</li>
 * <li>All methods with the Result annotation are invoked to retrieve the result
 * </li>
 * <li>The analyser object is dereferenced and garbage collected</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface AnalyzerBean {

	/**
	 * The display name of the analyser. The display name should be humanly
	 * readable and is presented to the user in User Interfaces.
	 * 
	 * @return the name of the analyser
	 */
	String displayName();

	/**
	 * The execution type of the analyser, which basically determines whether or
	 * not the analyser is able to perform it's own queries (explore the data
	 * context) or if it shares queries with other analysers and just processes
	 * incoming rows.
	 * 
	 * @return the type of execution mechanism to use for the analyser
	 */
	ExecutionType execution();
}
