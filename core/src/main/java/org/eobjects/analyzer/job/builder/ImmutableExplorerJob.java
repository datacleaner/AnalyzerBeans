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
package org.eobjects.analyzer.job.builder;

import org.eobjects.analyzer.descriptors.ExplorerBeanDescriptor;
import org.eobjects.analyzer.job.BeanConfiguration;
import org.eobjects.analyzer.job.ExplorerJob;
import org.eobjects.analyzer.job.ImmutableBeanConfiguration;

public final class ImmutableExplorerJob implements ExplorerJob {

	private final String _name;
	private final ExplorerBeanDescriptor<?> _descriptor;
	private final ImmutableBeanConfiguration _configuration;

	public ImmutableExplorerJob(String name, ExplorerBeanDescriptor<?> descriptor, ImmutableBeanConfiguration configuration) {
		_name = name;
		_descriptor = descriptor;
		_configuration = configuration;
	}

	@Override
	public ExplorerBeanDescriptor<?> getDescriptor() {
		return _descriptor;
	}

	@Override
	public BeanConfiguration getConfiguration() {
		return _configuration;
	}

	@Override
	public String getName() {
		return _name;
	}

}
