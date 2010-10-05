package org.eobjects.analyzer.beans.mock;

import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Transformer mock")
public class TransformerMock implements Transformer<Integer> {

	@Configured
	InputColumn<?> input;

	private AtomicInteger i;

	@Initialize
	public void init() {
		i = new AtomicInteger(0);
	}

	@Override
	public OutputColumns getOutputColumns() {
		return OutputColumns.singleOutputColumn();
	}

	@Override
	public Integer[] transform(InputRow inputRow) {
		return new Integer[] { i.incrementAndGet() };
	}
}
