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

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.beans.api.Explorer;
import org.eobjects.analyzer.descriptors.ExplorerBeanDescriptor;
import org.eobjects.analyzer.job.ExplorerJob;
import org.eobjects.analyzer.job.ImmutableBeanConfiguration;

public final class ExplorerJobBuilder<A extends Explorer<?>> extends
		AbstractBeanJobBuilder<ExplorerBeanDescriptor<A>, A, ExplorerJobBuilder<A>> {

	public ExplorerJobBuilder(AnalysisJobBuilder analysisJobBuilder, ExplorerBeanDescriptor<A> descriptor) {
		super(analysisJobBuilder, descriptor, ExplorerJobBuilder.class);
	}

	public ExplorerJob toExplorerJob() throws IllegalStateException {
		return toExplorerJob(true);
	}

	public ExplorerJob toExplorerJob(boolean validate) throws IllegalStateException {
		if (validate && !isConfigured()) {
			throw new IllegalStateException("Explorer job is not correctly configured");
		}

		return new ImmutableExplorerJob(getName(), getDescriptor(),
				new ImmutableBeanConfiguration(getConfiguredProperties()));
	}

	@Override
	public String toString() {
		return "ExplorerJobBuilder[analyzer=" + getDescriptor().getDisplayName() + "]";
	}

	@Override
	public void onConfigurationChanged() {
		super.onConfigurationChanged();
		List<ExplorerChangeListener> listeners = new ArrayList<ExplorerChangeListener>(getAnalysisJobBuilder()
				.getExplorerChangeListeners());
		for (ExplorerChangeListener listener : listeners) {
			listener.onConfigurationChanged(this);
		}
	}
}
