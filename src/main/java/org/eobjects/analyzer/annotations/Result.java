package org.eobjects.analyzer.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines methods that provide the result(s) of an AnalyzerBean. The return types
 * of @Result annotated methods are single instances or arrays of
 * AnalyzerBeanResult.
 * 
 * If an array of AnalysisResult's is returned and the result-objects don't have
 * their name fields set, then they will be automatically named with a counting
 * suffix, eg. "Output #1", "Output #2" etc.
 * 
 * @see org.eobjects.analyzer.result.AnalyzerBeanResult
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
public @interface Result {

	public String value() default "";
}
