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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eobjects.analyzer.job.jaxb.JobMetadataType;
import org.eobjects.analyzer.job.jaxb.MetadataProperties;

public class JaxbJobMetadataFactoryImplTest extends TestCase {

	public void testCreate() throws Exception {
		Map<String,String> properties = new HashMap<String,String>();
		properties.put("propertyName", "propertyValue");
		JaxbJobMetadataFactory factory = new JaxbJobMetadataFactoryImpl("kasper", "my job", "desc", "1.0",properties);

		JobMetadataType metadata = factory.create(EasyMock.createMock(AnalysisJob.class));
		int year = Calendar.getInstance().get(Calendar.YEAR);

		assertEquals("kasper", metadata.getAuthor());
		assertEquals("my job", metadata.getJobName());
		assertEquals("desc", metadata.getJobDescription());
		assertEquals("1.0", metadata.getJobVersion());
		MetadataProperties metadataProperties = metadata.getMetadataProperties();
		assertEquals("propertyName", metadataProperties.getProperty().get(0).getName());
		assertEquals("propertyValue",metadataProperties.getProperty().get(0).getValue());

		assertEquals(year, metadata.getUpdatedDate().getYear());
		assertEquals(null, metadata.getCreatedDate());
	}
}
