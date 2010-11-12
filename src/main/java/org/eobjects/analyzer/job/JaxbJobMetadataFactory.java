package org.eobjects.analyzer.job;

import org.eobjects.analyzer.job.jaxb.JobMetadataType;

public interface JaxbJobMetadataFactory {

	public JobMetadataType create(AnalysisJob analysisJob) throws Exception;

}
