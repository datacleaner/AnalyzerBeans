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
package org.eobjects.analyzer.beans;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Explorer;
import org.eobjects.analyzer.result.ColumnComparisonResult;
import org.eobjects.analyzer.result.ColumnDifference;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AnalyzerBean("Compare column structures")
public class CompareColumnsAnalyzer implements Explorer<ColumnComparisonResult> {

	private static final Logger logger = LoggerFactory.getLogger(CompareColumnsAnalyzer.class);

	@Inject
	@Configured
	Column column1;

	@Inject
	@Configured
	Column column2;

	private boolean relationshipsAnalyzed;
	private ColumnComparisonResult result;

	/**
	 * Constructor used when this analyzer is used as a part of a table
	 * analysis. Relationship analysis will not be included because it is
	 * assumed to be handled by the calling analyzer.
	 * 
	 * @param column1
	 * @param column2
	 */
	public CompareColumnsAnalyzer(Column column1, Column column2) {
		this.column1 = column1;
		this.column2 = column2;
		this.relationshipsAnalyzed = false;
	}

	/**
	 * No-args constructor used when instantiated as a standalone analysis
	 */
	public CompareColumnsAnalyzer() {
		this.relationshipsAnalyzed = true;
	}

	@Override
	public void run(DataContext dc) {
		assert column1 != null;
		assert column2 != null;

		List<ColumnDifference<?>> differences = new ArrayList<ColumnDifference<?>>();
		if (column1 == column2) {
			result = new ColumnComparisonResult(differences);
			return;
		}

		addDiff(differences, "name", column1.getName(), column2.getName());
		addDiff(differences, "type", column1.getType(), column2.getType());
		addDiff(differences, "native type", column1.getNativeType(), column2.getNativeType());
		addDiff(differences, "size", column1.getColumnSize(), column2.getColumnSize());
		addDiff(differences, "nullable", column1.isNullable(), column2.isNullable());
		addDiff(differences, "indexed", column1.isIndexed(), column2.isIndexed());
		addDiff(differences, "column number", column1.getColumnNumber(), column2.getColumnNumber());
		addDiff(differences, "remarks", column1.getRemarks(), column2.getRemarks());

		if (isRelationshipsAnalyzed()) {
			Table table1 = column1.getTable();
			Table table2 = column2.getTable();
			if (table1 == null || table2 == null) {
				logger.warn("Not analyzing relationships - could not resolve table for one or both columns");
			} else {
				// TODO: Include relationship analysis?
			}
		} else {
			logger.debug("Skipping relationship analysis");
		}

		result = new ColumnComparisonResult(differences);
	}

	private <T> void addDiff(List<ColumnDifference<?>> differences, String valueName, T value1, T value2) {
		if (!EqualsBuilder.equals(value1, value2)) {
			ColumnDifference<T> diff = new ColumnDifference<T>(column1, column2, valueName, value1, value2);
			differences.add(diff);
		}
	}

	@Override
	public ColumnComparisonResult getResult() {
		return result;
	}

	public boolean isRelationshipsAnalyzed() {
		return relationshipsAnalyzed;
	}

	public void setRelationshipsAnalyzed(boolean relationshipsAnalyzed) {
		this.relationshipsAnalyzed = relationshipsAnalyzed;
	}
}
