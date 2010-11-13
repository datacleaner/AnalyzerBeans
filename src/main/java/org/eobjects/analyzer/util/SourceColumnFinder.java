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
package org.eobjects.analyzer.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.InputColumnSourceJob;
import org.eobjects.analyzer.job.builder.SourceColumns;

import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

/**
 * Helper class for traversing dependencies between virtual and physical
 * columns.
 * 
 * @author Kasper Sørensen
 */
public class SourceColumnFinder {

	private List<InputColumnSourceJob> _sources = new ArrayList<InputColumnSourceJob>();

	public void addSources(InputColumnSourceJob... inputColumnSources) {
		for (InputColumnSourceJob inputColumnSource : inputColumnSources) {
			_sources.add(inputColumnSource);
		}
	}

	public void addSources(Collection<? extends InputColumnSourceJob> inputColumnSources) {
		_sources.addAll(inputColumnSources);
	}

	public void addSources(AnalysisJob job) {
		addSources(new SourceColumns(job.getSourceColumns()));
		addSources(job.getTransformerJobs());
		addSources(job.getMergedOutcomeJobs());
	}

	public InputColumnSourceJob findInputColumnSource(InputColumn<?> outputColumn) {
		for (InputColumnSourceJob source : _sources) {
			InputColumn<?>[] output = source.getOutput();
			for (InputColumn<?> mutableInputColumn : output) {
				if (outputColumn.equals(mutableInputColumn)) {
					return source;
				}
			}
		}
		return null;
	}

	public Table findOriginatingTable(InputColumn<?> inputColumn) {
		if (inputColumn.isPhysicalColumn()) {
			return inputColumn.getPhysicalColumn().getTable();
		}

		for (InputColumnSourceJob source : _sources) {
			InputColumn<?>[] outputColumns = source.getOutput();
			for (InputColumn<?> outputColumn : outputColumns) {
				if (outputColumn.equals(inputColumn)) {
					InputColumn<?>[] input = source.getInput();
					if (input.length > 0) {
						return findOriginatingTable(input[0]);
					}
				}
			}
		}

		throw new IllegalStateException("Could not find originating table for column: " + inputColumn);
	}

	public List<InputColumn<?>> findInputColumns(DataTypeFamily dataTypeFamily) {
		if (dataTypeFamily == null) {
			dataTypeFamily = DataTypeFamily.UNDEFINED;
		}

		List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
		for (InputColumnSourceJob source : _sources) {
			InputColumn<?>[] outputColumns = source.getOutput();
			for (InputColumn<?> col : outputColumns) {
				DataTypeFamily dtf = col.getDataTypeFamily();
				if (dtf == dataTypeFamily || dataTypeFamily == DataTypeFamily.UNDEFINED) {
					result.add(col);
				}
			}
		}

		return result;
	}

	public Set<Column> findOriginatingColumns(InputColumn<?> inputColumn) {
		// TODO: Detect cyclic dependencies between transformers (A depends on
		// B, B depends on A)

		Set<Column> physicalColumns = new HashSet<Column>();
		if (inputColumn.isPhysicalColumn()) {
			physicalColumns.add(inputColumn.getPhysicalColumn());
		} else {
			boolean found = false;
			for (InputColumnSourceJob source : _sources) {
				InputColumn<?>[] outputColumns = source.getOutput();
				for (InputColumn<?> outputColumn : outputColumns) {
					if (inputColumn.equals(outputColumn)) {
						found = true;
						InputColumn<?>[] input = source.getInput();
						for (InputColumn<?> sourceInputColumn : input) {
							physicalColumns.addAll(findOriginatingColumns(sourceInputColumn));
						}
					}
				}
			}

			if (!found) {
				throw new IllegalStateException("Could not find source physical column for: " + inputColumn);
			}
		}
		return physicalColumns;
	}
}
