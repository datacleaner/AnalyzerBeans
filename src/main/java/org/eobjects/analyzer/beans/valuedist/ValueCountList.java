package org.eobjects.analyzer.beans.valuedist;

import java.util.List;

public interface ValueCountList {

	public List<ValueCount> getValueCounts();
	
	public int getMaxSize();
	
	public int getActualSize();
}
