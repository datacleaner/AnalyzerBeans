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
package org.eobjects.analyzer.beans.valuedist;

import java.io.File;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionResult;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionResultHtmlRenderer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.result.html.HtmlFragment;
import org.eobjects.metamodel.util.FileHelper;

public class ValueDistributionResultHtmlRendererTest extends TestCase {

    public void testSingleDistribution() throws Exception {
        InputColumn<String> col1 = new MockInputColumn<String>("email username", String.class);

        ValueDistributionAnalyzer analyzer = new ValueDistributionAnalyzer(col1, true, null, null);

        analyzer.run(new MockInputRow().put(col1, "kasper"), 6);
        analyzer.run(new MockInputRow().put(col1, "kasper.sorensen"), 3);
        analyzer.run(new MockInputRow().put(col1, "kasper"), 3);
        analyzer.run(new MockInputRow().put(col1, "info"), 1);

        ValueDistributionResult result = analyzer.getResult();

        HtmlFragment htmlFragment = new ValueDistributionResultHtmlRenderer().render(result);
        assertEquals("SimpleHtmlFragment[headElements=0,bodyElements=1]", htmlFragment.toString());
        
        String html = htmlFragment.getBodyElements().get(0).toHtml();
        assertEquals(FileHelper.readFileAsString(new File(
                "src/test/resources/value_distribution_result_html_renderer_single.html")), html);
    }

    public void testMultipleDistributions() throws Exception {
        InputColumn<String> col1 = new MockInputColumn<String>("email username", String.class);
        InputColumn<String> col2 = new MockInputColumn<String>("email domain", String.class);

        ValueDistributionAnalyzer analyzer = new ValueDistributionAnalyzer(col1, col2, true, null, null);

        analyzer.run(new MockInputRow().put(col1, "kasper").put(col2, "eobjects.dk"), 4);
        analyzer.run(new MockInputRow().put(col1, "kasper.sorensen").put(col2, "eobjects.dk"), 2);
        analyzer.run(new MockInputRow().put(col1, "info").put(col2, "eobjects.dk"), 1);
        analyzer.run(new MockInputRow().put(col1, null).put(col2, "eobjects.dk"), 1);
        analyzer.run(new MockInputRow().put(col1, "kasper.sorensen").put(col2, "humaninference.com"), 1);
        analyzer.run(new MockInputRow().put(col1, "winfried.vanholland").put(col2, "humaninference.com"), 1);
        analyzer.run(new MockInputRow().put(col1, "kaspers").put(col2, "humaninference.com"), 1);

        ValueDistributionResult result = analyzer.getResult();

        HtmlFragment htmlFragment = new ValueDistributionResultHtmlRenderer().render(result);
        assertEquals("SimpleHtmlFragment[headElements=0,bodyElements=1]", htmlFragment.toString());

        String html = htmlFragment.getBodyElements().get(0).toHtml();
        assertEquals(FileHelper.readFileAsString(new File(
                "src/test/resources/value_distribution_result_html_renderer_multiple.html")), html);
    }
}
