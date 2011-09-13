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
package org.eobjects.analyzer.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.util.CollectionUtils2;
import org.eobjects.metamodel.util.BaseObject;

public final class ImmutableTransformerJob extends BaseObject implements TransformerJob {

	private final String _name;
	private final TransformerBeanDescriptor<?> _descriptor;
	private final BeanConfiguration _beanConfiguration;
	private final List<MutableInputColumn<?>> _output;
	private final Outcome _requirement;

	public ImmutableTransformerJob(String name, TransformerBeanDescriptor<?> descriptor,
			BeanConfiguration beanConfiguration, Collection<MutableInputColumn<?>> output, Outcome requirement) {
		_name = name;
		_descriptor = descriptor;
		_beanConfiguration = beanConfiguration;
		_output = Collections.unmodifiableList(new ArrayList<MutableInputColumn<?>>(output));
		_requirement = LazyOutcomeUtils.load(requirement);
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public TransformerBeanDescriptor<?> getDescriptor() {
		return _descriptor;
	}

	@Override
	public BeanConfiguration getConfiguration() {
		return _beanConfiguration;
	}

	@Override
	public InputColumn<?>[] getInput() {
		List<InputColumn<?>> result = new LinkedList<InputColumn<?>>();
		Set<ConfiguredPropertyDescriptor> propertiesForInput = _descriptor.getConfiguredPropertiesForInput();
		for (ConfiguredPropertyDescriptor propertyDescriptor : propertiesForInput) {
			Object property = _beanConfiguration.getProperty(propertyDescriptor);
			InputColumn<?>[] inputs = CollectionUtils2.arrayOf(InputColumn.class, property);
			if (inputs != null) {
				for (InputColumn<?> inputColumn : inputs) {
					result.add(inputColumn);
				}
			}
		}
		return result.toArray(new InputColumn<?>[result.size()]);
	}

	@Override
	public MutableInputColumn<?>[] getOutput() {
		return _output.toArray(new MutableInputColumn<?>[_output.size()]);
	}

	@Override
	public boolean equalsIgnoreColumnIds(TransformerJob other) {
		ImmutableTransformerJob transformerJob1 = new ImmutableTransformerJob(_name, _descriptor, _beanConfiguration,
				Collections.<MutableInputColumn<?>> emptyList(), _requirement);
		Outcome requirement = null;
		Outcome[] requirements = other.getRequirements();
		if (requirements != null && requirements.length > 0) {
			requirement = requirements[0];
		}
		if (equalsOutputColumnNames(other.getOutput())) {
			ImmutableTransformerJob transformerJob2 = new ImmutableTransformerJob(other.getName(), other.getDescriptor(),
					other.getConfiguration(), Collections.<MutableInputColumn<?>> emptyList(), requirement);
			return transformerJob1.equals(transformerJob2);
		}
		return false;
	}

	/**
	 * Compares the names of this output columns to the names of the specified
	 * other output columns. The result is true if and only if the argument is
	 * not null and is a columns object that represents the same sequence of
	 * column names as this output columns.
	 * 
	 * @param other
	 *            The columns to compare this columns against
	 * 
	 * @return true if the given object represents the same sequence of column
	 *         names as this output columns, false otherwise
	 */
	private boolean equalsOutputColumnNames(InputColumn<?>[] other) {
		if (other == null) {
			return false;
		}

		List<String> outputNames = new ArrayList<String>();
		for (MutableInputColumn<?> mic : _output) {
			outputNames.add(mic.getName());
		}
		List<String> otherOutputNames = new ArrayList<String>();
		for (InputColumn<?> inputColumn : other) {
			otherOutputNames.add(inputColumn.getName());
		}

		if (outputNames.size() != otherOutputNames.size()) {
			return false;
		}

		Iterator<String> it1 = outputNames.iterator();
		Iterator<String> it2 = otherOutputNames.iterator();
		while (it1.hasNext()) {
			assert it2.hasNext();
			String next1 = it1.next();
			String next2 = it2.next();
			if (!next1.equals(next2)) {
				return false;
			}
		}
		assert !it2.hasNext();

		return true;
	}

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		identifiers.add(_name);
		identifiers.add(_beanConfiguration);
		identifiers.add(_descriptor);
		identifiers.add(_output);
		identifiers.add(_requirement);
	}

	@Override
	public String toString() {
		return "ImmutableTransformerJob[name=" + _name + ",transformer=" + _descriptor.getDisplayName() + "]";
	}

	@Override
	public Outcome[] getRequirements() {
		if (_requirement == null) {
			return new Outcome[0];
		}
		return new Outcome[] { _requirement };
	}

}
