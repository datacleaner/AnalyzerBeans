package org.eobjects.analyzer.beans.transform;

import java.util.Arrays;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@TransformerBean("Date mask matcher")
@Description("Matches String values against a set of date masks, producing a corresponding set of output columns, specifying whether or not the strings could be interpreted as dates given those date masks")
public class DateMaskMatcherTransformer implements Transformer<Boolean> {

	public static final String[] DEFAULT_DATE_MASKS = new String[] { "yyyy-MM-dd", "yyyy/MM/dd", "dd.MM.yyyy", "dd/MM/yyyy",
			"MM/dd/yy", "d MMM yyyy HH:mm:ss", "yyyy-MM-dd HH:mm:ss.S" };

	@Configured
	InputColumn<String> _column;

	@Configured
	String[] _dateMasks = DEFAULT_DATE_MASKS;

	private DateTimeFormatter[] _dateTimeFormatters;

	public DateMaskMatcherTransformer(InputColumn<String> column) {
		_column = column;
	}

	public DateMaskMatcherTransformer() {
	}

	@Initialize
	public void init() {
		_dateTimeFormatters = new DateTimeFormatter[_dateMasks.length];
		for (int i = 0; i < _dateTimeFormatters.length; i++) {
			try {
				_dateTimeFormatters[i] = DateTimeFormat.forPattern(_dateMasks[i]);
			} catch (Exception e) {
				// not a valid pattern!
				_dateTimeFormatters[i] = null;
			}
		}
	}

	@Override
	public OutputColumns getOutputColumns() {
		String columnName = _column.getName();
		String[] names = new String[_dateMasks.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = columnName + " '" + _dateMasks[i] + "'";
		}
		return new OutputColumns(names);
	}

	@Override
	public Boolean[] transform(InputRow inputRow) {
		Boolean[] result = new Boolean[_dateMasks.length];
		Arrays.fill(result, false);

		String value = inputRow.getValue(_column);
		if (value != null) {
			for (int i = 0; i < _dateTimeFormatters.length; i++) {
				DateTimeFormatter dateTimeFormatter = _dateTimeFormatters[i];
				if (dateTimeFormatter != null) {
					try {
						// this will throw an exception if the value is not
						// complying to the pattern
						dateTimeFormatter.parseDateTime(value);
						result[i] = true;
					} catch (Exception e) {
						result[i] = false;
					}
				}
			}
		}
		return result;
	}

	public void setDateMasks(String[] dateMasks) {
		_dateMasks = dateMasks;
	}
}
