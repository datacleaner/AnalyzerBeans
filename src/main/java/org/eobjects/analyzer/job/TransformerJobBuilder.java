package org.eobjects.analyzer.job;

import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.data.TransformedInputColumn;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.lifecycle.TransformerBeanInstance;

public class TransformerJobBuilder
		extends
		AbstractBeanWithInputColumnsBuilder<TransformerBeanDescriptor, TransformerJobBuilder> {

	private LinkedList<MutableInputColumn<?>> _outputColumns = new LinkedList<MutableInputColumn<?>>();
	private IdGenerator _idGenerator;

	public TransformerJobBuilder(TransformerBeanDescriptor descriptor,
			IdGenerator idGenerator) {
		super(descriptor, TransformerJobBuilder.class);
		_idGenerator = idGenerator;
	}

	public List<MutableInputColumn<?>> getOutputColumns() {
		TransformerBeanInstance transformerBeanInstance = new TransformerBeanInstance(
				getDescriptor());
		// TODO: Configure the instance
		transformerBeanInstance.assignConfigured();

		int expectedCols = transformerBeanInstance.getBean().getOutputColumns();
		int existingCols = _outputColumns.size();
		if (expectedCols != existingCols) {
			int colDiff = expectedCols - existingCols;
			if (colDiff > 0) {
				for (int i = 0; i < colDiff; i++) {
					String name = getDescriptor().getDisplayName() + " "
							+ (_outputColumns.size() + 1);
					DataTypeFamily type = getDescriptor()
							.getOutputDataTypeFamily();
					_outputColumns.add(new TransformedInputColumn<Object>(name,
							type, _idGenerator));
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

	public TransformerJob toTransformerJob() throws IllegalStateException {
		if (!isConfigured()) {
			throw new IllegalStateException(
					"Transformer job is not correctly configured");
		}

		return new ImmutableTransformerJob(getDescriptor(),
				new ImmutableBeanConfiguration(getConfiguredProperties()),
				getOutputColumns());
	}

	@Override
	public String toString() {
		return "TransformerJobBuilder[transformer="
				+ getDescriptor().getDisplayName() + ",inputColumns="
				+ getInputColumns() + "]";
	}
}
