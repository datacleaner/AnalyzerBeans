package org.eobjects.analyzer.beans.transform;

import java.util.Date;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.joda.time.DateTime;
import org.joda.time.Years;

/**
 * A transformer for turning a date into age (both in years and in days).
 * 
 * The transformer and it's intended usage is depicted in this graph:
 * 
 * <img src="doc-files/DateToAgeTransformer.jpg" alt=""/>
 * 
 * <p>
 * The transformer has been more thouroughly explained at kasper's source, see
 * link below.
 * </p>
 * 
 * @see http
 *      ://kasper.eobjects.org/2010/09/developing-value-transformer-using.html
 * 
 * @author Kasper Sørensen
 */
@TransformerBean("Date to age")
public class DateToAgeTransformer implements Transformer<Integer> {

	@Configured("Date column")
	InputColumn<Date> dateColumn;

	private Date today = new Date();

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns("Age in days", "Age in years");
	}

	@Override
	public Integer[] transform(InputRow inputRow) {
		Integer[] result = new Integer[2];
		Date date = inputRow.getValue(dateColumn);

		if (date != null) {
			long diffMillis = today.getTime() - date.getTime();
			int diffDays = (int) (diffMillis / (1000 * 60 * 60 * 24));

			result[0] = diffDays;

			// use Joda time to easily calculate the diff in years
			int diffYears = Years.yearsBetween(new DateTime(date),
					new DateTime(today)).getYears();
			result[1] = diffYears;
		}

		return result;
	}

	// injection for testing purposes only
	public void setToday(Date today) {
		this.today = today;
	}

	// injection for testing purposes only
	public void setDateColumn(InputColumn<Date> dateColumn) {
		this.dateColumn = dateColumn;
	}
}
