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
package org.eobjects.analyzer.result.renderer;

import javax.swing.table.TableModel;

import org.apache.commons.lang.StringEscapeUtils;
import org.eobjects.analyzer.result.html.BodyElement;
import org.eobjects.analyzer.util.LabelUtils;

/**
 * Body element that renders a HTML table based on a {@link TableModel}.
 */
public class TableBodyElement implements BodyElement {

    private final TableModel _tableModel;
    private final String _tableClassName;

    /**
     * Constructs a table body element.
     * 
     * @param tableModel
     *            the table model to render
     * @param tableClassName
     *            a CSS class name to to set to the table
     */
    public TableBodyElement(TableModel tableModel, String tableClassName) {
        _tableModel = tableModel;
        _tableClassName = tableClassName;
    }

    @Override
    public String toHtml() {
        final int columnCount = _tableModel.getColumnCount();

        final StringBuilder sb = new StringBuilder();

        if (_tableClassName == null) {
            sb.append("<table>");
        } else {
            sb.append("<table class=\"" + _tableClassName + "\">");
        }

        int rowNumber = 0;
        rowNumber++;

        sb.append("<tr class=\"" + (rowNumber % 2 == 0 ? "even" : "odd") + "\">");
        for (int col = 0; col < columnCount; col++) {
            String columnName = _tableModel.getColumnName(col);
            sb.append("<th>");
            sb.append(StringEscapeUtils.escapeHtml(columnName));
            sb.append("</th>");
        }
        sb.append("</tr>");

        int rowCount = _tableModel.getRowCount();
        for (int row = 0; row < rowCount; row++) {
            rowNumber++;
            sb.append("<tr class=\"" + (rowNumber % 2 == 0 ? "even" : "odd") + "\">");
            for (int col = 0; col < columnCount; col++) {
                Object value = _tableModel.getValueAt(row, col);
                String stringValue = LabelUtils.getValueLabel(value);
                sb.append("<td>");
                sb.append(StringEscapeUtils.escapeHtml(stringValue));
                sb.append("</td>");
            }
            sb.append("</tr>");
        }

        sb.append("</table>");

        return sb.toString();
    }
}
