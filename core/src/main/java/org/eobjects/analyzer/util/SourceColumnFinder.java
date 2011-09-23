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
import java.util.TreeSet;

import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.ExpressionBasedInputColumn;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.InputColumnSinkJob;
import org.eobjects.analyzer.job.InputColumnSourceJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.OutcomeSinkJob;
import org.eobjects.analyzer.job.OutcomeSourceJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.SourceColumns;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;

/**
 * Helper class for traversing dependencies between virtual and physical
 * columns.
 * 
 * @author Kasper Sørensen
 */
public class SourceColumnFinder {

	private Set<InputColumnSinkJob> _inputColumnSinks = new HashSet<InputColumnSinkJob>();
	private Set<InputColumnSourceJob> _inputColumnSources = new HashSet<InputColumnSourceJob>();
	private Set<OutcomeSourceJob> _outcomeSources = new HashSet<OutcomeSourceJob>();
	private Set<OutcomeSinkJob> _outcomeSinks = new HashSet<OutcomeSinkJob>();

	private void addSources(Object... sources) {
		for (Object source : sources) {
			if (source instanceof InputColumnSinkJob) {
				_inputColumnSinks.add((InputColumnSinkJob) source);
			}
			if (source instanceof InputColumnSourceJob) {
				_inputColumnSources.add((InputColumnSourceJob) source);
			}
			if (source instanceof OutcomeSourceJob) {
				_outcomeSources.add((OutcomeSourceJob) source);
			}
			if (source instanceof OutcomeSinkJob) {
				_outcomeSinks.add((OutcomeSinkJob) source);
			}
		}
	}

	private void addSources(Collection<?> sources) {
		addSources(sources.toArray());
	}

	public void addSources(AnalysisJobBuilder job) {
		addSources(new SourceColumns(job.getSourceColumns()));
		addSources(job.getFilterJobBuilders());
		addSources(job.getTransformerJobBuilders());
		addSources(job.getMergedOutcomeJobBuilders());
		addSources(job.getAnalyzerJobBuilders());
	}

	public void addSources(AnalysisJob job) {
		addSources(new SourceColumns(job.getSourceColumns()));
		addSources(job.getFilterJobs());
		addSources(job.getTransformerJobs());
		addSources(job.getMergedOutcomeJobs());
		addSources(job.getAnalyzerJobs());
	}

	public List<InputColumn<?>> findInputColumns(DataTypeFamily dataTypeFamily, Class<?> dataType) {
		if (dataTypeFamily == null) {
			dataTypeFamily = DataTypeFamily.UNDEFINED;
		}

		List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
		for (InputColumnSourceJob source : _inputColumnSources) {
			InputColumn<?>[] outputColumns = source.getOutput();
			for (InputColumn<?> col : outputColumns) {
				DataTypeFamily dtf = col.getDataTypeFamily();
				if (dtf == dataTypeFamily || dataTypeFamily == DataTypeFamily.UNDEFINED) {
					if (dataType == null) {
						result.add(col);
					} else {
						if (ReflectionUtils.is(col.getDataType(), dataType)) {
							result.add(col);
						}
					}
				}
			}
		}

		return result;
	}

	public InputColumnSourceJob findInputColumnSource(InputColumn<?> inputColumn) {
		if (inputColumn instanceof ExpressionBasedInputColumn) {
			return null;
		}
		for (InputColumnSourceJob source : _inputColumnSources) {
			InputColumn<?>[] output = source.getOutput();
			for (InputColumn<?> column : output) {
				if (inputColumn.equals(column)) {
					return source;
				}
			}
		}
		return null;
	}

	public OutcomeSourceJob findOutcomeSource(Outcome requirement) {
		for (OutcomeSourceJob source : _outcomeSources) {
			Outcome[] outcomes = source.getOutcomes();
			for (Outcome outcome : outcomes) {
				if (requirement.equals(outcome)) {
					return source;
				}
			}
		}
		return null;
	}

	public Set<Column> findOriginatingColumns(Outcome requirement) {
		OutcomeSourceJob source = findOutcomeSource(requirement);

		HashSet<Column> result = new HashSet<Column>();
		findOriginatingColumnsOfSource(source, result);
		return result;
	}

	public Table findOriginatingTable(Outcome requirement) {
		OutcomeSourceJob source = findOutcomeSource(requirement);
		return findOriginatingTableOfSource(source);
	}

	public Table findOriginatingTable(InputColumn<?> inputColumn) {
		if (inputColumn.isPhysicalColumn()) {
			return inputColumn.getPhysicalColumn().getTable();
		}
		return findOriginatingTableOfSource(findInputColumnSource(inputColumn));
	}

	private Table findOriginatingTableOfSource(Object source) {
		Set<Table> result = new TreeSet<Table>();
		if (source instanceof InputColumnSinkJob) {
			InputColumn<?>[] input = ((InputColumnSinkJob) source).getInput();
			if (input != null) {
				for (InputColumn<?> col : input) {
					Table table = findOriginatingTable(col);
					if (table != null) {
						result.add(table);
					}
				}
			}
		}
		if (source instanceof OutcomeSinkJob) {
			Outcome[] requirements = ((OutcomeSinkJob) source).getRequirements();
			if (requirements != null) {
				for (Outcome outcome : requirements) {
					Table table = findOriginatingTable(outcome);
					if (table != null) {
						result.add(table);
					}
				}
			}
		}

		if (result.isEmpty()) {
			return null;
		}
		if (result.size() == 1) {
			return result.iterator().next();
		}
		StringBuilder sb = new StringBuilder();
		for (Table table : result) {
			if (sb.length() != 0) {
				sb.append(", ");
			}
			sb.append(table.getName());
		}
		throw new IllegalStateException("Multiple originating tables (" + sb + ") found for source: " + source);
	}

	private void findOriginatingColumnsOfInputColumn(InputColumn<?> inputColumn, Set<Column> result) {
		if (inputColumn == null) {
			return;
		}
		if (inputColumn.isPhysicalColumn()) {
			result.add(inputColumn.getPhysicalColumn());
		} else {
			InputColumnSourceJob source = findInputColumnSource(inputColumn);
			findOriginatingColumnsOfSource(source, result);
		}
	}

	private void findOriginatingColumnsOfOutcome(Outcome requirement, Set<Column> result) {
		OutcomeSourceJob source = findOutcomeSource(requirement);
		findOriginatingColumnsOfSource(source, result);
	}

	private void findOriginatingColumnsOfSource(Object source, Set<Column> result) {
		if (source == null) {
			return;
		}
		if (source instanceof InputColumnSinkJob) {
			InputColumn<?>[] input = ((InputColumnSinkJob) source).getInput();
			if (input != null) {
				for (InputColumn<?> inputColumn : input) {
					findOriginatingColumnsOfInputColumn(inputColumn, result);
				}
			}
		}
		if (source instanceof OutcomeSinkJob) {
			Outcome[] requirements = ((OutcomeSinkJob) source).getRequirements();
			for (Outcome outcome : requirements) {
				findOriginatingColumnsOfOutcome(outcome, result);
			}
		}
	}

	public Set<Column> findOriginatingColumns(InputColumn<?> inputColumn) {
		Set<Column> result = new HashSet<Column>();

		// TODO: Detect cyclic dependencies between transformers (A depends on
		// B, B depends on A)

		findOriginatingColumnsOfInputColumn(inputColumn, result);
		return result;
	}
}
