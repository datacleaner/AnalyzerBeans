package org.eobjects.analyzer.beans.standardize;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.HasGroupLiteral;
import org.eobjects.analyzer.util.NamedPattern;
import org.eobjects.analyzer.util.NamedPatternMatch;

/**
 * Tokenizes/standardizes the components of an email: Username and Domain
 * 
 * @author Kasper Sørensen
 */
@TransformerBean("Email standardizer")
public class EmailStandardizerTransformer implements Transformer<String> {

	public static final NamedPattern<EmailPart> EMAIL_PATTERN = new NamedPattern<EmailPart>("USERNAME@DOMAIN",
			EmailPart.class);

	public static enum EmailPart implements HasGroupLiteral {
		USERNAME("([a-zA-Z0-9\\._%+-]+)"), DOMAIN("([a-zA-Z0-9\\._%+-]+\\.[a-zA-Z0-9\\._%+-]{2,4})");

		private String groupLiteral;

		private EmailPart(String groupLiteral) {
			this.groupLiteral = groupLiteral;
		}

		@Override
		public String getGroupLiteral() {
			return groupLiteral;
		}
	}

	@Inject
	@Configured
	InputColumn<String> inputColumn;

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns("Username", "Domain");
	}

	@Override
	public String[] transform(InputRow inputRow) {
		String value = inputRow.getValue(inputColumn);
		return transform(value);
	}

	public String[] transform(String value) {
		String username = null;
		String domain = null;

		NamedPatternMatch<EmailPart> match = EMAIL_PATTERN.match(value);
		if (match != null) {
			username = match.get(EmailPart.USERNAME);
			domain = match.get(EmailPart.DOMAIN);
		}
		return new String[] { username, domain };
	}
}
