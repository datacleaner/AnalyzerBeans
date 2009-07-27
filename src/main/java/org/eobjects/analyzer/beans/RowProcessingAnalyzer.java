package org.eobjects.analyzer.beans;

import dk.eobjects.metamodel.data.Row;

public interface RowProcessingAnalyzer {

	public void run(Row row, long distinctCount);
}
