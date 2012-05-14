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
package org.eobjects.analyzer.cli;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringEscapeUtils;
import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.html.BodyElement;
import org.eobjects.analyzer.result.html.HeadElement;
import org.eobjects.analyzer.result.html.HtmlFragment;
import org.eobjects.analyzer.result.renderer.HtmlRenderingFormat;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.metamodel.util.Ref;

public class HtmlAnalysisResultWriter implements AnalysisResultWriter {

    @Override
    public void write(AnalysisResult result, AnalyzerBeansConfiguration configuration, Ref<Writer> writerRef,
            Ref<OutputStream> outputStreamRef) throws Exception {
        final Writer writer = writerRef.get();

        final RendererFactory rendererFactory = new RendererFactory(configuration.getDescriptorProvider());
        final Map<ComponentJob, HtmlFragment> htmlFragments = new LinkedHashMap<ComponentJob, HtmlFragment>();
        for (Entry<ComponentJob, AnalyzerResult> entry : result.getResultMap().entrySet()) {
            final ComponentJob componentJob = entry.getKey();
            final AnalyzerResult analyzerResult = entry.getValue();
            final Renderer<? super AnalyzerResult, ? extends HtmlFragment> renderer = rendererFactory.getRenderer(
                    analyzerResult, HtmlRenderingFormat.class);
            if (renderer == null) {
                throw new IllegalStateException("No HTML renderer found for result: " + analyzerResult);
            }
            final HtmlFragment htmlFragment = renderer.render(analyzerResult);
            htmlFragments.put(componentJob, htmlFragment);
        }

        writer.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
        writer.write("<html>\n");
        writeHead(writer, htmlFragments);
        writeBody(writer, htmlFragments);
        writer.write("</html>");
    }

    private void writeHead(final Writer writer, final Map<ComponentJob, HtmlFragment> htmlFragments) throws IOException {
        final Set<HeadElement> allHeadElements = new HashSet<HeadElement>();
        
        writer.write("<head>\n");
        writer.write("<title>Analysis result</title>");
       
        for (HtmlFragment htmlFragment : htmlFragments.values()) {
            final List<HeadElement> headElements = htmlFragment.getHeadElements();
            for (HeadElement headElement : headElements) {
                if (!allHeadElements.contains(headElement)) {
                    writer.write(headElement.toHtml());
                    writer.write('\n');
                    allHeadElements.add(headElement);
                }
            }
        }
        
        writer.write("</head>\n");
    }

    private void writeBody(final Writer writer, final Map<ComponentJob, HtmlFragment> htmlFragments) throws IOException {
        writer.write("<body>\n");
        writer.write("<div class=\"analysisResultContainer\">\n");
        writer.write("<h1 class=\"analysisResultHeader\">Success!</h1>\n");

        for (Entry<ComponentJob, HtmlFragment> entry : htmlFragments.entrySet()) {
            final ComponentJob componentJob = entry.getKey();
            final HtmlFragment htmlFragment = entry.getValue();
            writer.write("<div class=\"analyzerResultContainer\">\n");
            {
                writer.write("<h2 class=\"analyzerResultHeader\">Result: "
                        + StringEscapeUtils.escapeHtml(getLabel(componentJob)) + "</h2>");
                writer.write("<div class=\"analyzerResultPanel\">\n");

                final List<BodyElement> bodyElements = htmlFragment.getBodyElements();
                for (BodyElement bodyElement : bodyElements) {
                    writer.write(bodyElement.toHtml());
                    writer.write('\n');
                }

                writer.write("</div>\n");
            }
            writer.write("</div>\n");
        }
        writer.write("</div>\n");
        writer.write("</body>\n");
    }

    private String getLabel(ComponentJob componentJob) {
        String label = componentJob.getName();
        if (label == null) {
            ComponentDescriptor<?> descriptor = componentJob.getDescriptor();
            label = descriptor.getDisplayName();
        }
        return label;
    }

}
