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

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.html.HtmlFragment;

@RendererBean(HtmlRenderingFormat.class)
public class CrosstabHtmlRenderer extends AbstractRenderer<CrosstabResult, HtmlFragment> {

    @Inject
    DescriptorProvider descriptorProvider;

    public CrosstabHtmlRenderer() {
        this(null);
    }

    public CrosstabHtmlRenderer(DescriptorProvider descriptorProvider) {
        this.descriptorProvider = descriptorProvider;
    }

    @Override
    public HtmlFragment render(CrosstabResult result) {
        return render(result.getCrosstab());
    }

    public HtmlFragment render(Crosstab<?> crosstab) {
        CrosstabRenderer crosstabRenderer = new CrosstabRenderer(crosstab);
        HtmlFragment htmlFragment = crosstabRenderer.render(new HtmlCrosstabRendererCallback(descriptorProvider));
        return htmlFragment;
    }

}
