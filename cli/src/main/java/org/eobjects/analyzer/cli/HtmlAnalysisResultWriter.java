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
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.html.BodyElement;
import org.eobjects.analyzer.result.html.HeadElement;
import org.eobjects.analyzer.result.html.HtmlFragment;
import org.eobjects.analyzer.result.renderer.HtmlRenderingFormat;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.util.LabelUtils;
import org.eobjects.metamodel.util.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlAnalysisResultWriter implements AnalysisResultWriter {

    private static final Logger logger = LoggerFactory.getLogger(HtmlAnalysisResultWriter.class);

    @Override
    public void write(AnalysisResult result, AnalyzerBeansConfiguration configuration, Ref<Writer> writerRef,
            Ref<OutputStream> outputStreamRef) throws Exception {

        final Writer writer = writerRef.get();

        final RendererFactory rendererFactory = new RendererFactory(configuration);
        final Map<ComponentJob, HtmlFragment> htmlFragments = new LinkedHashMap<ComponentJob, HtmlFragment>();
        for (Entry<ComponentJob, AnalyzerResult> entry : result.getResultMap().entrySet()) {
            final ComponentJob componentJob = entry.getKey();
            final AnalyzerResult analyzerResult = entry.getValue();
            final Renderer<? super AnalyzerResult, ? extends HtmlFragment> renderer = rendererFactory.getRenderer(
                    analyzerResult, HtmlRenderingFormat.class);
            if (renderer == null) {
                throw new IllegalStateException("No HTML renderer found for result: " + analyzerResult);
            }

            try {
                final HtmlFragment htmlFragment = renderer.render(analyzerResult);
                htmlFragments.put(componentJob, htmlFragment);
            } catch (Exception e) {
                logger.error("Error while rendering analyzer result: " + analyzerResult, e);
                writeRenderingError(writer, componentJob, analyzerResult, e);
            }
        }

        writeHtmlBegin(writer);
        writeHead(writer, htmlFragments);
        writeBody(writer, htmlFragments);
        writeHtmlEnd(writer);
    }

    private void writeMaterializationError(Writer writer, ComponentJob componentJob, Exception e) throws IOException {
        writeGenericError(writer, componentJob, null, e);
    }

    private void writeRenderingError(Writer writer, ComponentJob componentJob, AnalyzerResult analyzerResult,
            Exception e) throws IOException {
        writeGenericError(writer, componentJob, analyzerResult, e);
    }

    private void writeGenericError(Writer writer, ComponentJob componentJob, AnalyzerResult analyzerResult, Exception e)
            throws IOException {
        writer.write("<div class=\"renderingError\">");
        writer.write("<p>Component job: " + LabelUtils.getLabel(componentJob) + "</p>");
        if (analyzerResult != null) {
            writer.write("<p>Analyzer result: " + analyzerResult + "</p>");
        }
        writer.write("<pre>");
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        writer.write("</pre>");
        writer.write("</div>");
    }

    protected void writeHtmlBegin(Writer writer) throws IOException {
        writer.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
        writer.write("<html>\n");
    }

    protected void writeHtmlEnd(Writer writer) throws IOException {
        writer.write("</html>");
    }

    protected void writeHead(final Writer writer, final Map<ComponentJob, HtmlFragment> htmlFragments)
            throws IOException {
        final Set<HeadElement> allHeadElements = new HashSet<HeadElement>();

        writeHeadBegin(writer);

        for (Entry<ComponentJob, HtmlFragment> entry : htmlFragments.entrySet()) {
            final HtmlFragment htmlFragment = entry.getValue();
            final List<HeadElement> headElements = htmlFragment.getHeadElements();
            for (HeadElement headElement : headElements) {
                if (!allHeadElements.contains(headElement)) {
                    final ComponentJob componentJob = entry.getKey();
                    writeHeadElement(writer, componentJob, headElement);
                    allHeadElements.add(headElement);
                }
            }
        }

        writeHeadEnd(writer);
    }

    protected void writeHeadBegin(Writer writer) throws IOException {
        writer.write("<head>\n");
        writer.write("  <title>Analysis result</title>");
    }

    protected void writeHeadEnd(Writer writer) throws IOException {
        writer.write("</head>");
    }

    protected void writeHeadElement(Writer writer, ComponentJob componentJob, HeadElement headElement)
            throws IOException {
        writer.write("  ");
        try {
            String html = headElement.toHtml();
            writer.write(html);
        } catch (Exception e) {
            writeMaterializationError(writer, componentJob, e);
        }
        writer.write('\n');
    }

    protected void writeBody(final Writer writer, final Map<ComponentJob, HtmlFragment> htmlFragments)
            throws IOException {
        writeBodyBegin(writer);

        for (Entry<ComponentJob, HtmlFragment> entry : htmlFragments.entrySet()) {
            final ComponentJob componentJob = entry.getKey();
            final HtmlFragment htmlFragment = entry.getValue();
            {
                writeBodyHtmlFragment(writer, componentJob, htmlFragment);
            }
        }
        writeBodyEnd(writer);
    }

    protected void writeBodyBegin(Writer writer) throws IOException {
        writer.write("<body>\n");
        writer.write("<div class=\"analysisResultContainer\">\n");
    }

    protected void writeBodyEnd(Writer writer) throws IOException {
        writer.write("</div>\n");
        writer.write("</body>");
    }

    protected void writeBodyHtmlFragment(Writer writer, ComponentJob componentJob, HtmlFragment htmlFragment)
            throws IOException {
        writer.write("<h2 class=\"analyzerResultHeader\">"
                + StringEscapeUtils.escapeHtml(LabelUtils.getLabel(componentJob)) + "</h2>");
        writer.write("<div class=\"analyzerResultPanel\">\n");

        final List<BodyElement> bodyElements = htmlFragment.getBodyElements();
        for (BodyElement bodyElement : bodyElements) {
            writeBodyElement(writer, componentJob, htmlFragment, bodyElement);
        }

        writer.write("</div>\n");
    }

    protected void writeBodyElement(Writer writer, ComponentJob componentJob, HtmlFragment htmlFragment,
            BodyElement bodyElement) throws IOException {
        writer.write("  ");
        try {
            String html = bodyElement.toHtml();
            writer.write(html);
        } catch (Exception e) {
            writeMaterializationError(writer, componentJob, e);
        }
        writer.write('\n');
    }
}
