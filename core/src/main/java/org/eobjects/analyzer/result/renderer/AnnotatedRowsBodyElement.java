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
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.html.BodyElement;

public class AnnotatedRowsBodyElement implements BodyElement {

    private final AnnotatedRowsResult _result;

    public AnnotatedRowsBodyElement(AnnotatedRowsResult result) {
        _result = result;
    }

    @Override
    public String toHtml() {
        final TableModel tableModel = _result.toTableModel();
        final int columnCount = tableModel.getColumnCount();

        final StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"annotatedRowsTable\">");

        sb.append("<tr>");
        for (int col = 0; col < columnCount; col++) {
            String columnName = tableModel.getColumnName(col);
            sb.append("<th>");
            sb.append(StringEscapeUtils.escapeHtml(columnName));
            sb.append("</th>");
        }
        sb.append("</tr>");

        int rowCount = tableModel.getRowCount();
        for (int row = 0; row < rowCount; row++) {
            sb.append("<tr>");
            for (int col = 0; col < columnCount; col++) {
                Object value = tableModel.getValueAt(row, col);
                if (value == null) {
                    value = "<null>";
                }
                sb.append("<td>");
                sb.append(StringEscapeUtils.escapeHtml(value.toString()));
                sb.append("</td>");
            }
            sb.append("</tr>");
        }

        sb.append("</table>");

        return sb.toString();
    }
}
