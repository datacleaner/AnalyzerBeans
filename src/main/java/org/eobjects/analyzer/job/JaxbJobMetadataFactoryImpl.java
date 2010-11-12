package org.eobjects.analyzer.job;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.eobjects.analyzer.job.jaxb.JobMetadataType;

public class JaxbJobMetadataFactoryImpl implements JaxbJobMetadataFactory {

	private final DatatypeFactory _datatypeFactory;

	public JaxbJobMetadataFactoryImpl() throws DatatypeConfigurationException {
		_datatypeFactory = DatatypeFactory.newInstance();
	}

	@Override
	public JobMetadataType create(AnalysisJob analysisJob) {
		JobMetadataType jobMetadata = new JobMetadataType();
		jobMetadata.setUpdatedDate(_datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar()));
		return jobMetadata;
	}

}
