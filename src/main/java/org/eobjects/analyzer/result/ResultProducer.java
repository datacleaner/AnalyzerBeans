package org.eobjects.analyzer.result;


/**
 * Result producers produce AnalyzerResults. Typically they are used to
 * represent command-objects that produce a drill-to-detail/exploration style
 * result in a crosstab, chart or similar.
 * 
 * ResultProducers are not required to be Serializable, but if they are they
 * will be saved with the rest of the AnalyzerResult that contain them, if that
 * is persisted.
 */
public interface ResultProducer {

	public AnalyzerResult getResult();
}
