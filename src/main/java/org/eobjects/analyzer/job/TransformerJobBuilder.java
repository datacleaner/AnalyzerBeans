package org.eobjects.analyzer.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.data.TransformedInputColumn;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.lifecycle.TransformerBeanInstance;

public class TransformerJobBuilder {

	private AnalysisJobBuilder _analysisJobBuilder;
	private TransformerBeanDescriptor _descriptor;
	private List<InputColumn<?>> _inputColumns = new ArrayList<InputColumn<?>>();
	private LinkedList<MutableInputColumn<?>> _outputColumns = new LinkedList<MutableInputColumn<?>>();
	private IdGenerator _idGenerator;

	public TransformerJobBuilder(TransformerBeanDescriptor descriptor,
			IdGenerator idGenerator, AnalysisJobBuilder analysisJobBuilder) {
		_descriptor = descriptor;
		_idGenerator = idGenerator;
		_analysisJobBuilder = analysisJobBuilder;
	}

	public AnalysisJobBuilder parentBuilder() {
		return _analysisJobBuilder;
	}

	public TransformerBeanDescriptor getDescriptor() {
		return _descriptor;
	}

	public TransformerJobBuilder addInputColumn(InputColumn<?> inputColumn) {
		_inputColumns.add(inputColumn);
		return this;
	}

	public TransformerJobBuilder addInputColumns(InputColumn<?>... inputColumns) {
		for (InputColumn<?> inputColumn : inputColumns) {
			addInputColumn(inputColumn);
		}
		return this;
	}

	public TransformerJobBuilder removeInputColumn(InputColumn<?> inputColumn) {
		_inputColumns.remove(inputColumn);
		// TODO: Notify consumers
		return this;
	}

	public List<InputColumn<?>> getInputColumns() {
		return Collections.unmodifiableList(_inputColumns);
	}

	public List<MutableInputColumn<?>> getOutputColumns() {
		TransformerBeanInstance transformerBeanInstance = new TransformerBeanInstance(
				_descriptor);
		// TODO: Configure the instance
		
		transformerBeanInstance.assignConfigured();

		int expectedCols = transformerBeanInstance.getBean().getOutputColumns();
		int existingCols = _outputColumns.size();
		if (expectedCols != existingCols) {
			int colDiff = expectedCols - existingCols;
			if (colDiff > 0) {
				for (int i = 0; i < colDiff; i++) {
					String name = _descriptor.getDisplayName() + " "
							+ (_outputColumns.size() + 1);
					_outputColumns.add(new TransformedInputColumn<Object>(name,
							_idGenerator));
				}
			} else if (colDiff < 0) {
				for (int i = 0; i < Math.abs(colDiff); i++) {
					// remove from the tail
					_outputColumns.removeLast();
				}

				// TODO: Notify consumers of the removed columns
			}
		}

		return _outputColumns;
	}
}
