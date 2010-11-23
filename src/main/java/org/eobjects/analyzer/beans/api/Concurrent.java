package org.eobjects.analyzer.beans.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to override the default component concurrency model. Any
 * component (transformer, filter or analyzer) with this annotation can define
 * whether or not the framework should be allowed to invoke the component
 * concurrently (ie. from several threads at the same time) or not.
 * 
 * The default behaviour of the components is:
 * 
 * <ul>
 * <li>Transformers and Filters are invoked concurrently. The rationale behind
 * this default value is that the invoked methods (transform(...) and
 * categorize(...)) both return their results immidiately and thus a stateless
 * implementation will be the normal scenario.</li>
 * <li>Analyzers are <i>not</i> invoked concurrently. The rationale behind this
 * default value is that analyzers are expected to build up it's result during
 * execution and thus will typically be stateful.</li>
 * </ul>
 * 
 * @see FilterBean
 * @see TransformerBean
 * @see AnalyzerBean
 * 
 * @author Kasper Sørensen
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Concurrent {

	public boolean value();
}
