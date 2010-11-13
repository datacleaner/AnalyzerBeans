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

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.result.AnalyzerResult;

/**
 * 
 * @author Kasper Sørensen
 * 
 * @param <I>
 *            the input of the renderer, ie. the result type to render
 * @param <O>
 *            the output type of the renderer. This should be the same as or a
 *            subclass of the output class of the matching RenderingFormat.
 * 
 * @see RendererBean
 * @see RenderingFormat
 */
public interface Renderer<I extends AnalyzerResult, O> {

	public O render(I result);
}
