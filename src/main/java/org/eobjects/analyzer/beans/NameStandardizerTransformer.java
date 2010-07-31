package org.eobjects.analyzer.beans;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.Initialize;
import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.NamedPattern;
import org.eobjects.analyzer.util.NamedPatternMatch;

@TransformerBean("Name standardizer")
public class NameStandardizerTransformer implements Transformer<String> {

	public static final String[] DEFAULT_PATTERNS = { "FIRSTNAME LASTNAME",
			"FIRSTNAME MIDDLENAME LASTNAME", "LASTNAME, FIRSTNAME",
			"LASTNAME, FIRSTNAME MIDDLENAME" };

	public static enum NamePart {
		FIRSTNAME, LASTNAME, MIDDLENAME
	}

	@Inject
	@Configured
	InputColumn<String> inputColumn;

	@Inject
	@Configured("Name patterns")
	String[] stringPatterns = DEFAULT_PATTERNS;

	private List<NamedPattern<NamePart>> namedPatterns;

	@Initialize
	public void init() {
		if (stringPatterns == null) {
			stringPatterns = new String[0];
		}

		namedPatterns = new ArrayList<NamedPattern<NamePart>>();

		for (String stringPattern : stringPatterns) {
			namedPatterns.add(new NamedPattern<NamePart>(stringPattern,
					NamePart.class));
		}
	}

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns("Firstname","Lastname","Middlename");
	}

	@Override
	public String[] transform(InputRow inputRow) {
		String value = inputRow.getValue(inputColumn);
		return transform(value);
	}

	public String[] transform(String value) {
		String firstName = null;
		String lastName = null;
		String middleName = null;

		if (value != null) {
			for (NamedPattern<NamePart> namedPattern : namedPatterns) {
				NamedPatternMatch<NamePart> match = namedPattern.match(value);
				if (match != null) {
					firstName = match.get(NamePart.FIRSTNAME);
					lastName = match.get(NamePart.LASTNAME);
					middleName = match.get(NamePart.MIDDLENAME);
					break;
				}
			}
		}
		return new String[] { firstName, lastName, middleName };
	}

	@SuppressWarnings("unchecked")
	public void setInputColumn(InputColumn<?> inputColumn) {
		this.inputColumn = (InputColumn<String>) inputColumn;
	}

	public void setStringPatterns(String... stringPatterns) {
		this.stringPatterns = stringPatterns;
	}
}
