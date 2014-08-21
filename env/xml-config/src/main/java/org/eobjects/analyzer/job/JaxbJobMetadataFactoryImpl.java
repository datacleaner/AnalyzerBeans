/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.job;

import java.util.GregorianCalendar;
import java.util.Map;

import javax.xml.datatype.DatatypeFactory;

import org.eobjects.analyzer.job.jaxb.JobMetadataType;
import org.eobjects.analyzer.job.jaxb.MetadataProperties;
import org.eobjects.analyzer.job.jaxb.MetadataProperties.Property;

public class JaxbJobMetadataFactoryImpl implements JaxbJobMetadataFactory {

	private final DatatypeFactory _datatypeFactory;
	private final String _author;
	private final String _jobName;
	private final String _jobDescription;
	private final String _jobVersion;
	private final Map<String,String> _properties;

	public JaxbJobMetadataFactoryImpl() {
		this(null, null, null, null,null);
	}

	public JaxbJobMetadataFactoryImpl(String author, String jobName, String jobDescription, String jobVersion, Map<String,String> properties){
		_author = author;
		_jobName = jobName;
		_jobDescription = jobDescription;
		_jobVersion = jobVersion;
		_properties=properties;
		
		try {
			_datatypeFactory = DatatypeFactory.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public JobMetadataType create(AnalysisJob analysisJob) {
		JobMetadataType jobMetadata = new JobMetadataType();
		jobMetadata.setUpdatedDate(_datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar()));

		jobMetadata.setAuthor(_author);
		jobMetadata.setJobName(_jobName);
		jobMetadata.setJobDescription(_jobDescription);
		jobMetadata.setJobVersion(_jobVersion);
		MetadataProperties metadataProperties = null;
		if(_properties!=null && _properties.size() > 0){
			metadataProperties = getMetadataProperties(_properties);
		}
		jobMetadata.setMetadataProperties(metadataProperties);

		return jobMetadata;
	}
	
	private MetadataProperties getMetadataProperties(Map<String,String> properties){
		 MetadataProperties metadataProperties = new MetadataProperties();
		for(Map.Entry<String, String> metadataProperty : properties.entrySet()){
			Property property = new Property();
			property.setName(metadataProperty.getKey());
			property.setValue(metadataProperty.getValue());
			metadataProperties.getProperty().add(property);
		}
		return metadataProperties;
	}

}
