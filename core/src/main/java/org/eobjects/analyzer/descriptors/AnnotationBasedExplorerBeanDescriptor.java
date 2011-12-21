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

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Explorer;
import org.eobjects.analyzer.util.ReflectionUtils;

final class AnnotationBasedExplorerBeanDescriptor<E extends Explorer<?>> extends AbstractBeanDescriptor<E> implements
		ExplorerBeanDescriptor<E> {

	private final String _displayName;

	protected AnnotationBasedExplorerBeanDescriptor(Class<E> explorerClass) throws DescriptorException {
		super(explorerClass, false);

		AnalyzerBean analyzerAnnotation = ReflectionUtils.getAnnotation(explorerClass, AnalyzerBean.class);
		if (analyzerAnnotation == null) {
			throw new DescriptorException(explorerClass + " doesn't implement the AnalyzerBean annotation");
		}

		String displayName = analyzerAnnotation.value();
		if (displayName == null || displayName.trim().length() == 0) {
			displayName = ReflectionUtils.explodeCamelCase(explorerClass.getSimpleName(), false);
		}
		_displayName = displayName;

		visitClass();
	}

	@Override
	public String getDisplayName() {
		return _displayName;
	}

	@Override
	public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesForInput() {
		return Collections.emptySet();
	}
}
