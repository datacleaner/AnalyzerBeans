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
package org.eobjects.analyzer.result.renderer;

import java.util.List;

import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.ResultProducer;
import org.eobjects.analyzer.result.html.BaseHeadElement;
import org.eobjects.analyzer.result.html.DrillToDetailsBodyElement;
import org.eobjects.analyzer.result.html.DrillToDetailsHeadElement;
import org.eobjects.analyzer.result.html.HtmlFragment;
import org.eobjects.analyzer.result.html.HtmlUtils;
import org.eobjects.analyzer.result.html.SimpleHtmlFragment;

public class HtmlCrosstabRendererCallback implements CrosstabRendererCallback<HtmlFragment> {

    private final StringBuilder sb;
    private final SimpleHtmlFragment htmlFragtment;
    private final DescriptorProvider descriptorProvider;

    public HtmlCrosstabRendererCallback(DescriptorProvider descriptorProvider) {
        this.descriptorProvider = descriptorProvider;
        sb = new StringBuilder();
        htmlFragtment = new SimpleHtmlFragment();
        htmlFragtment.addHeadElement(BaseHeadElement.get());
    }

    @Override
    public void beginTable(Crosstab<?> crosstab, List<CrosstabDimension> horizontalDimensions,
            List<CrosstabDimension> verticalDimensions) {
        sb.append("<table class=\"crosstabTable\">");
    }

    @Override
    public void endTable() {
        sb.append("</table>");
    }

    @Override
    public void beginRow() {
        sb.append("<tr>");
    }

    @Override
    public void endRow() {
        sb.append("</tr>");
    }

    @Override
    public void horizontalHeaderCell(String category, CrosstabDimension dimension, int width) {
        if (width <= 0) {
            return;
        }
        if (width > 1) {
            sb.append("<td class=\"crosstabHorizontalHeader\" colspan=\"");
            sb.append(width);
            sb.append("\">");
        } else if (width == 1) {
            sb.append("<td>");
        }
        sb.append(category);
        sb.append("</td>");
    }

    @Override
    public void verticalHeaderCell(String category, CrosstabDimension dimension, int height) {
        if (height <= 0) {
            return;
        }
        if (height > 1) {
            sb.append("<td class=\"crosstabVerticalHeader\" rowspan=\"");
            sb.append(height);
            sb.append("\">");
        } else if (height == 1) {
            sb.append("<td>");
        }
        sb.append(category);
        sb.append("</td>");
    }

    @Override
    public void valueCell(Object value, ResultProducer drillToDetailResultProducer) {
        if (value == null) {
            value = "&lt;null&gt;";
        }

        if (drillToDetailResultProducer == null) {
            simpleValueCell(value);
            return;
        }

        final AnalyzerResult drillResult = drillToDetailResultProducer.getResult();

        final String drillElementId = HtmlUtils.createElementId();
        final DrillToDetailsHeadElement drillHeadElement = new DrillToDetailsHeadElement(drillElementId);
        htmlFragtment.addHeadElement(drillHeadElement);
        
        final DrillToDetailsBodyElement drillBodyElement = new DrillToDetailsBodyElement(drillElementId, descriptorProvider, drillResult);
        htmlFragtment.addBodyElement(drillBodyElement);

        final String invocation = drillHeadElement.toJavaScriptInvocation();

        sb.append("<td>");
        sb.append("<a class=\"drillToDetailsLink\" href=\"#\" onclick=\"" + invocation + "\">");
        sb.append(value.toString());
        sb.append("</a>");
        sb.append("</td>");
    }

    private void simpleValueCell(Object value) {
        sb.append("<td>");
        sb.append(value.toString());
        sb.append("</td>");
    }

    @Override
    public void emptyHeader(CrosstabDimension verticalDimension, CrosstabDimension horizontalDimension) {
        sb.append("<td></td>");
    }

    @Override
    public HtmlFragment getResult() {
        htmlFragtment.addBodyElement(sb.toString());
        return htmlFragtment;
    }
}
