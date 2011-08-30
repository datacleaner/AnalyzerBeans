/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.beans.transform;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.reference.SynonymCatalog;

/**
 * A simple transformer that uses a synonym catalog to replace a synonym with
 * it's master term.
 * 
 * @author Kasper Sørensen
 */
@TransformerBean("Synonym replacement")
@Description("Replaces strings with their synonyms")
public class SynonymReplacementTransformer implements Transformer<String> {

	@Configured
	InputColumn<String> column;

	@Configured
	SynonymCatalog synonymCatalog;

	@Configured
	@Description("Retain original value in case no synonym is found (otherwise null)")
	boolean retainOriginalValue = true;

	public SynonymReplacementTransformer() {
	}

	public SynonymReplacementTransformer(InputColumn<String> column, SynonymCatalog synonymCatalog,
			boolean retainOriginalValue) {
		this();
		this.column = column;
		this.synonymCatalog = synonymCatalog;
		this.retainOriginalValue = retainOriginalValue;
	}

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(new String[] { column.getName() + " (synonyms replaced)" });
	}

	@Override
	public String[] transform(InputRow inputRow) {
		final String originalValue = inputRow.getValue(column);

		if (originalValue == null) {
			return new String[1];
		}

		final String replacedValue = synonymCatalog.getMasterTerm(originalValue);
		if (retainOriginalValue && replacedValue == null) {
			return new String[] { originalValue };
		}

		return new String[] { replacedValue };
	}
}
