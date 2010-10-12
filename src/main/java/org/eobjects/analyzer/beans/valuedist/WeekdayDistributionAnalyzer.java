package org.eobjects.analyzer.beans.valuedist;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.result.CrosstabResult;

@AnalyzerBean("Weekday distribution")
public class WeekdayDistributionAnalyzer implements RowProcessingAnalyzer<CrosstabResult> {

	private Map<InputColumn<Date>, Map<Integer, Integer>> distributionMap;

	@Configured
	InputColumn<Date>[] dateColumns;

	@Initialize
	public void init() {
		distributionMap = new HashMap<InputColumn<Date>, Map<Integer, Integer>>();
		for (InputColumn<Date> col : dateColumns) {
			Map<Integer, Integer> countMap = new HashMap<Integer, Integer>(7);
			for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
				// put a count of 0 for each day of the week
				countMap.put(i, 0);
			}
			distributionMap.put(col, countMap);
		}
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		for (InputColumn<Date> col : dateColumns) {
			Date value = row.getValue(col);
			if (value != null) {
				Calendar c = Calendar.getInstance();
				c.setTime(value);
				int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
				Map<Integer, Integer> countMap = distributionMap.get(col);
				int count = countMap.get(dayOfWeek);
				count += distinctCount;
				countMap.put(dayOfWeek, count);
			}
		}
	}

	@Override
	public CrosstabResult getResult() {
		CrosstabDimension columnDimension = new CrosstabDimension("Column");
		CrosstabDimension weekdayDimension = new CrosstabDimension("Weekday");
		weekdayDimension.addCategory("Sunday").addCategory("Monday").addCategory("Tuesday").addCategory("Wednesday")
				.addCategory("Thursday").addCategory("Friday").addCategory("Saturday");
		Crosstab<Integer> crosstab = new Crosstab<Integer>(Integer.class, columnDimension, weekdayDimension);
		for (InputColumn<Date> col : dateColumns) {
			columnDimension.addCategory(col.getName());
			CrosstabNavigator<Integer> nav = crosstab.where(columnDimension, col.getName());
			Map<Integer, Integer> countMap = distributionMap.get(col);
			nav.where(weekdayDimension, "Sunday").put(countMap.get(Calendar.SUNDAY));
			nav.where(weekdayDimension, "Monday").put(countMap.get(Calendar.MONDAY));
			nav.where(weekdayDimension, "Tuesday").put(countMap.get(Calendar.TUESDAY));
			nav.where(weekdayDimension, "Wednesday").put(countMap.get(Calendar.WEDNESDAY));
			nav.where(weekdayDimension, "Thursday").put(countMap.get(Calendar.THURSDAY));
			nav.where(weekdayDimension, "Friday").put(countMap.get(Calendar.FRIDAY));
			nav.where(weekdayDimension, "Saturday").put(countMap.get(Calendar.SATURDAY));
		}

		return new CrosstabResult(crosstab);
	}

	// used only for unittesting
	public void setDateColumns(InputColumn<Date>[] dateColumns) {
		this.dateColumns = dateColumns;
	}
}
