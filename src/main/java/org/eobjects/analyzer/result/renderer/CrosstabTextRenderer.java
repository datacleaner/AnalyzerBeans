package org.eobjects.analyzer.result.renderer;

import java.util.List;

import org.eobjects.analyzer.annotations.RendererBean;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.ResultProducer;

@RendererBean(TextRenderingFormat.class)
public class CrosstabTextRenderer implements Renderer<CrosstabResult, String> {

	private static class TextCrosstabRendererCallback implements
			CrosstabRendererCallback<String> {

		private StringBuilder sb;
		private int horizontalDimensionWidth;

		@Override
		public void beginTable(Crosstab<?> crosstab,
				List<CrosstabDimension> horizontalDimensions,
				List<CrosstabDimension> verticalDimensions) {
			sb = new StringBuilder();
			horizontalDimensionWidth = 0;
			for (CrosstabDimension dimension : horizontalDimensions) {
				List<String> categories = dimension.getCategories();
				for (String category : categories) {
					horizontalDimensionWidth = Math.max(
							horizontalDimensionWidth, category.length());
				}
			}
		}

		@Override
		public void endTable() {
		}

		@Override
		public void beginRow() {
		}

		@Override
		public void endRow() {
			sb.append('\n');
		}

		@Override
		public void horizontalHeaderCell(String category,
				CrosstabDimension dimension, int width) {
			sb.append(category);

			int trailingBlanks = horizontalDimensionWidth - category.length();
			for (int i = 0; i < trailingBlanks; i++) {
				sb.append(' ');
			}

			// separator
			sb.append(' ');
		}

		@Override
		public void verticalHeaderCell(String category,
				CrosstabDimension dimension, int height) {
			sb.append(category);

			int dimensionWidth = getWidth(dimension);
			dimensionWidth = dimensionWidth - category.length();
			for (int i = 0; i < dimensionWidth; i++) {
				sb.append(' ');
			}

			// separator
			sb.append(' ');
		}

		@Override
		public void valueCell(Object value,
				ResultProducer drillToDetailResultProducer) {
			if (value == null) {
				value = "<null>";
			}
			String stringValue = value.toString();

			int trailingBlanks = horizontalDimensionWidth
					- stringValue.length();
			if (value instanceof Number) {
				for (int i = 0; i < trailingBlanks; i++) {
					sb.append(' ');
				}
				sb.append(stringValue);
			} else {
				sb.append(stringValue);
				for (int i = 0; i < trailingBlanks; i++) {
					sb.append(' ');
				}
			}

			// separator
			sb.append(' ');
		}

		@Override
		public void emptyHeader(CrosstabDimension verticalDimension,
				CrosstabDimension horizontalDimension) {
			int dimensionWidth = getWidth(verticalDimension);

			for (int i = 0; i < dimensionWidth; i++) {
				sb.append(' ');
			}

			// separator
			sb.append(' ');
		}

		private int getWidth(CrosstabDimension verticalDimension) {
			List<String> categories = verticalDimension.getCategories();
			int longestCategory = 0;
			for (String category : categories) {
				longestCategory = Math.max(longestCategory, category.length());
			}
			return longestCategory;
		}

		@Override
		public String getResult() {
			return sb.toString();
		}

	}

	@Override
	public String render(CrosstabResult result) {
		return new CrosstabRenderer(result.getCrosstab())
				.render(new TextCrosstabRendererCallback());
	}

}
