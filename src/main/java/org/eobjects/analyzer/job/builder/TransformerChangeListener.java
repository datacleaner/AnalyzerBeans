package org.eobjects.analyzer.job.builder;

import java.util.List;

import org.eobjects.analyzer.data.MutableInputColumn;

/**
 * Listener interface for receiving notifications when transformers are being
 * added, removed or modified in a way which changes their output.
 * 
 * @author Kasper Sørensen
 */
public interface TransformerChangeListener {

	public void onAdd(TransformerJobBuilder<?> transformerJobBuilder);

	public void onRemove(TransformerJobBuilder<?> transformerJobBuilder);

	public void onConfigurationChanged(TransformerJobBuilder<?> transformerJobBuilder);

	public void onRequirementChanged(TransformerJobBuilder<?> transformerJobBuilder);

	/**
	 * This method will be invoked each time a change in a transformer's output
	 * columns is observed.
	 * 
	 * Note that this method will also be invoked with an empty list if a
	 * transformer is being removed. This is to make it easier for listeners to
	 * handle updates on output columns using a single listening-method.
	 * 
	 * @param transformerJobBuilder
	 * @param outputColumns
	 */
	public void onOutputChanged(TransformerJobBuilder<?> transformerJobBuilder, List<MutableInputColumn<?>> outputColumns);
}
