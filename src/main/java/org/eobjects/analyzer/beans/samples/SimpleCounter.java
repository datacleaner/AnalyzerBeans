package org.eobjects.analyzer.beans.samples;

import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.ExecutionType;
import org.eobjects.analyzer.annotations.Result;
import org.eobjects.analyzer.annotations.Run;
import org.eobjects.analyzer.result.AnalyzerBeanResult;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Table;

@AnalyzerBean(displayName = "Table count", execution = ExecutionType.EXPLORING)
public class SimpleCounter {

	@Configured("Table to count")
	private Table table;
	
	private Number _count;

	@Run
	public void doCounting(DataContext dc) {
		Query q = new Query().selectCount().from(table);
		DataSet ds = dc.executeQuery(q);
		ds.next();
		_count = (Number) ds.getRow().getValue(0);
		ds.close();
	}
	
	@Result
	public AnalyzerBeanResult result() {
		// Result structure not final yet
		System.out.println("Count: " + _count);
		return null;
	}
}
