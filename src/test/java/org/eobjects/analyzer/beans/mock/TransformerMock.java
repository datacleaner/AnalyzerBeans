package org.eobjects.analyzer.beans.mock;

import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.Initialize;
import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.beans.Transformer;
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
	public int getOutputColumns() {
		return 1;
	}

	@Override
	public Integer[] transform(InputRow inputRow) {
		return new Integer[] { i.incrementAndGet() };
	}

}
