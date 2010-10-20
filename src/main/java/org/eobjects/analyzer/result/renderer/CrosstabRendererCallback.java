package org.eobjects.analyzer.result.renderer;

import java.util.List;

import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.ResultProducer;

public interface CrosstabRendererCallback<E> {

	public void beginTable(Crosstab<?> crosstab, List<CrosstabDimension> horizontalDimensions,
			List<CrosstabDimension> verticalDimensions);

	public void endTable();

	public void beginRow();

	public void endRow();

	public void horizontalHeaderCell(String category, CrosstabDimension dimension, int width);

	public void verticalHeaderCell(String category, CrosstabDimension dimension, int height);

	public void valueCell(Object value, ResultProducer drillToDetailResultProducer);

	public E getResult();

	public void emptyHeader(CrosstabDimension verticalDimension, CrosstabDimension horizontalDimension);
}
