package org.eobjects.analyzer.result.renderer;

import java.util.List;

import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.ResultProducer;

public class HtmlCrosstabRendererCallback implements CrosstabRendererCallback<String> {

	private StringBuilder sb;

	public HtmlCrosstabRendererCallback() {
		sb = new StringBuilder();
	}

	@Override
	public void beginTable(Crosstab<?> crosstab, List<CrosstabDimension> horizontalDimensions,
			List<CrosstabDimension> verticalDimensions) {
		sb.append("<table>");
	}

	@Override
	public void endTable() {
		sb.append("</table>");
	}

	@Override
	public void beginRow() {
		sb.append("<tr>");
	}

	@Override
	public void endRow() {
		sb.append("</tr>");
	}

	@Override
	public void horizontalHeaderCell(String category, CrosstabDimension dimension, int width) {
		if (width <= 0) {
			return;
		}
		if (width > 1) {
			sb.append("<td colspan=\"");
			sb.append(width);
			sb.append("\">");
		} else if (width == 1) {
			sb.append("<td>");
		}
		sb.append(category);
		sb.append("</td>");
	}

	@Override
	public void verticalHeaderCell(String category, CrosstabDimension dimension, int height) {
		if (height <= 0) {
			return;
		}
		if (height > 1) {
			sb.append("<td rowspan=\"");
			sb.append(height);
			sb.append("\">");
		} else if (height == 1) {
			sb.append("<td>");
		}
		sb.append(category);
		sb.append("</td>");
	}

	@Override
	public void valueCell(Object value, ResultProducer drillToDetailResultProducer) {
		if (value == null) {
			value = "<null>";
		}
		sb.append("<td>");
		sb.append(value.toString());
		sb.append("</td>");
	}

	@Override
	public void emptyHeader(CrosstabDimension verticalDimension, CrosstabDimension horizontalDimension) {
		sb.append("<td></td>");
	}

	@Override
	public String getResult() {
		return sb.toString();
	}
}
