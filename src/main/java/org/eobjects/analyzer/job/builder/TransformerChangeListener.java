package org.eobjects.analyzer.job.builder;

import java.util.List;

import org.eobjects.analyzer.data.MutableInputColumn;

public interface TransformerChangeListener {

	public void onAdd(TransformerJobBuilder<?> transformerJobBuilder);

	public void onRemove(TransformerJobBuilder<?> transformerJobBuilder);

	public void onOutputChanged(TransformerJobBuilder<?> transformerJobBuilder, List<MutableInputColumn<?>> outputColumns);
}
