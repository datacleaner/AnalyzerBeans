package org.eobjects.analyzer.result;

import javax.swing.table.TableModel;

public interface TableModelResult extends AnalyzerResult {

	public TableModel toTableModel();
}
