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
package org.eobjects.analyzer.descriptors;

import java.util.Collections;
import java.util.Set;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.ExploringAnalyzer;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.util.ReflectionUtils;

final class AnnotationBasedAnalyzerBeanDescriptor<A extends Analyzer<?>> extends AbstractBeanDescriptor<A> implements
		AnalyzerBeanDescriptor<A> {

	private final String _displayName;
	private final boolean _exploringAnalyzer;
	private final boolean _rowProcessingAnalyzer;

	protected AnnotationBasedAnalyzerBeanDescriptor(Class<A> analyzerClass) throws DescriptorException {
		super(analyzerClass, ReflectionUtils.is(analyzerClass, RowProcessingAnalyzer.class));

		_rowProcessingAnalyzer = ReflectionUtils.is(analyzerClass, RowProcessingAnalyzer.class);
		_exploringAnalyzer = ReflectionUtils.is(analyzerClass, ExploringAnalyzer.class);

		if (!_rowProcessingAnalyzer && !_exploringAnalyzer) {
			throw new DescriptorException(analyzerClass + " does not implement either "
					+ RowProcessingAnalyzer.class.getName() + " or " + ExploringAnalyzer.class.getName());
		}

		AnalyzerBean analyzerAnnotation = analyzerClass.getAnnotation(AnalyzerBean.class);
		if (analyzerAnnotation == null) {
			throw new DescriptorException(analyzerClass + " doesn't implement the AnalyzerBean annotation");
		}

		String displayName = analyzerAnnotation.value();
		if (displayName == null || displayName.trim().length() == 0) {
			displayName = ReflectionUtils.explodeCamelCase(analyzerClass.getSimpleName(), false);
		}
		_displayName = displayName;

		visitClass();
	}

	@Override
	public String getDisplayName() {
		return _displayName;
	}

	@Override
	public boolean isExploringAnalyzer() {
		return _exploringAnalyzer;
	}

	@Override
	public boolean isRowProcessingAnalyzer() {
		return _rowProcessingAnalyzer;
	}

	@Override
	public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesForInput() {
		if (isRowProcessingAnalyzer()) {
			return super.getConfiguredPropertiesForInput();
		}
		return Collections.emptySet();
	}
}
