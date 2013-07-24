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
package org.eobjects.analyzer.configuration;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.job.AnalysisJobMetadata;
import org.eobjects.analyzer.job.ImmutableAnalysisJobMetadata;
import org.eobjects.metamodel.schema.ColumnType;
import org.eobjects.metamodel.util.DateUtils;
import org.eobjects.metamodel.util.Month;

import junit.framework.TestCase;

public class ImmutableAnalysisJobMetadataTest extends TestCase {

    public void testGetters() throws Exception {
        String jobName = "name";
        String jobVersion = "version";
        String jobDescription = "desc";
        String author = "auth";
        Date createdDate = DateUtils.get(2013, Month.JULY, 23);
        Date updatedDate = DateUtils.get(2013, Month.JULY, 24);
        String datastoreName = "ds";
        List<String> sourceColumnPaths = Arrays.asList("foo", "bar");
        List<ColumnType> sourceColumnTypes = Arrays.asList(ColumnType.VARCHAR, ColumnType.BINARY);
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("foo", "bar");

        AnalysisJobMetadata metadata = new ImmutableAnalysisJobMetadata(jobName, jobVersion, jobDescription, author,
                createdDate, updatedDate, datastoreName, sourceColumnPaths, sourceColumnTypes, variables);

        assertEquals(jobName, metadata.getJobName());
        assertEquals(jobDescription, metadata.getJobDescription());
        assertEquals(jobVersion, metadata.getJobVersion());
        assertEquals(author, metadata.getAuthor());
        assertEquals(createdDate, metadata.getCreatedDate());
        assertEquals(updatedDate, metadata.getUpdatedDate());
        assertEquals(datastoreName, metadata.getDatastoreName());
        assertEquals(sourceColumnPaths, metadata.getSourceColumnPaths());
        assertEquals(sourceColumnTypes, metadata.getSourceColumnTypes());

        assertEquals(variables, metadata.getVariables());

        AnalysisJobMetadata metadata2 = new ImmutableAnalysisJobMetadata(jobName, jobVersion, jobDescription, author,
                createdDate, updatedDate, datastoreName, sourceColumnPaths, sourceColumnTypes, variables);

        assertEquals(metadata, metadata2);
    }
}
