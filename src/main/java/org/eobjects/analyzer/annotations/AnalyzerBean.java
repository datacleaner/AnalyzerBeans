package org.eobjects.analyzer.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eobjects.analyzer.engine.ExecutionType;

/**
 * Classes that are annotated with the @AnalyzerBean annotation are components
 * for data analysis.
 * 
 * The life-cycle of an AnalyzerBean is as follows:
 * <ul>
 * <li>Instantiation. All AnalyzerBeans need to provide a no-args constructor.</li>
 * <li>All methods or fields with the @Configured annotation are
 * invoked/assigned to configure the AnalyzerBean before execution.</li>
 * <li>All methods or fields with the @Provided annotation are invoked/assigned</li>
 * <li>Any no-args methods with the @Initialize annotation are executed.</li>
 * <li>All methods with the @Run annotation are executed (A number of times,
 * depending on the ExecutionType parameter).</li>
 * <li>All methods with the @Result annotation are invoked to retrieve the
 * result.</li>
 * <li>Any no-args methods with the @Close annotation are invoked if the
 * analyzer needs to release any resources.</li>
 * <li>If the analyzer implements the java.io.Closeable interface, the close()
 * method is also invoked.</li>
 * <li>The AnalyzerBean object is dereferenced and garbage collected</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface AnalyzerBean {

	/**
	 * The display name of the AnalyzerBean. The display name should be humanly
	 * readable and is presented to the user in User Interfaces.
	 * 
	 * @return the name of the AnalyzerBean
	 */
	String displayName() default "";

	/**
	 * The execution type of the AnalyzerBean, which basically determines
	 * whether or not the AnalyzerBean is able to perform it's own queries
	 * (explore the data context) or if it shares queries with other
	 * AnalyzerBeans and just processes incoming rows.
	 * 
	 * @return the type of execution mechanism to use for the AnalyzerBean
	 */
	ExecutionType execution() default ExecutionType.ROW_PROCESSING;
}
