package org.eobjects.analyzer.result.renderer;

import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabNavigator;

/**
 * A class that encapsulates all the complicated logic of traversing a crosstab
 * in the correct order, if it is to be rendered using two axes (horizontal and
 * vertical). The actual output of the rendering is not specified by this class
 * - it uses a callback class for all the concrete rendering.
 * 
 * @see CrosstabRendererCallback
 * 
 * @author Kasper Sørensen
 */
public class CrosstabRenderer {

	private static final int MAX_HORIZONTAL_CELLS = 10;

	private Crosstab<?> crosstab;
	private List<CrosstabDimension> horizontalDimensions;
	private List<CrosstabDimension> verticalDimensions;
	private int horizontalCells = 1;
	private int verticalCells = 1;

	public CrosstabRenderer(Crosstab<?> crosstab) {
		if (crosstab == null) {
			throw new IllegalArgumentException("Crosstab cannot be null");
		}
		this.crosstab = crosstab;
		this.horizontalDimensions = new LinkedList<CrosstabDimension>();
		this.verticalDimensions = new LinkedList<CrosstabDimension>();
	}

	public void autoAssignDimensions() {
		// create a list of dimensions (with max 10 categories) to be layed
		// out horizontally

		List<CrosstabDimension> dimensions = crosstab.getDimensions();
		List<CrosstabDimension> autoAssignDimensions = new LinkedList<CrosstabDimension>();

		for (CrosstabDimension dimension : dimensions) {
			if (!isAssigned(dimension)) {
				autoAssignDimensions.add(dimension);
			}
		}
		
		if (autoAssignDimensions.size() == 2) {
			makeHorizontal(autoAssignDimensions.get(0));
			makeVertical(autoAssignDimensions.get(1));
		} else {
			for (CrosstabDimension dimension : autoAssignDimensions) {
				boolean horizontal = false;
				int categoryCount = dimension.getCategoryCount();
				if (horizontalCells <= MAX_HORIZONTAL_CELLS
						&& categoryCount <= MAX_HORIZONTAL_CELLS) {
					if (horizontalCells * categoryCount <= MAX_HORIZONTAL_CELLS) {
						makeHorizontal(dimension);
						horizontal = true;
					}
				}
				
				if (!horizontal) {
					makeVertical(dimension);
				}
			}
		}
	}

	public boolean isAssigned(CrosstabDimension dimension) {
		return verticalDimensions.contains(dimension)
				|| horizontalDimensions.contains(dimension);
	}

	public void makeHorizontal(CrosstabDimension dimension) {
		if (verticalDimensions.contains(dimension)) {
			verticalDimensions.remove(dimension);
			verticalCells = verticalCells / dimension.getCategoryCount();
		}
		if (!horizontalDimensions.contains(dimension)) {
			horizontalDimensions.add(dimension);
			horizontalCells = horizontalCells * dimension.getCategoryCount();
		}
	}

	public void makeVertical(CrosstabDimension dimension) {
		if (horizontalDimensions.contains(dimension)) {
			horizontalDimensions.remove(dimension);
			horizontalCells = horizontalCells / dimension.getCategoryCount();
		}
		if (!verticalDimensions.contains(dimension)) {
			verticalDimensions.add(dimension);
			verticalCells = verticalCells * dimension.getCategoryCount();
		}
	}

	public <E> E render(CrosstabRendererCallback<E> callback) {
		autoAssignDimensions();

		List<CrosstabDimension> dimensions = crosstab.getDimensions();
		if (dimensions == null || dimensions.isEmpty()) {
			return callback.getResult();
		}

		callback.beginTable(crosstab, horizontalDimensions, verticalDimensions);

		// print the (horizontal) headers
		{
			int colspan = horizontalCells;
			int repeatHeaders = 1;
			for (int i = 0; i < horizontalDimensions.size(); i++) {
				callback.beginRow();
				CrosstabDimension dimension = horizontalDimensions.get(i);

				// empty cells for each vertical dimension
				for (CrosstabDimension verticalDimension : verticalDimensions) {
					callback.emptyHeader(verticalDimension, dimension);
				}

				List<String> categories = dimension.getCategories();
				colspan = colspan / categories.size();
				for (int j = 0; j < repeatHeaders; j++) {
					for (String category : categories) {
						callback.horizontalHeaderCell(category, dimension,
								colspan);
					}
				}
				repeatHeaders = repeatHeaders * categories.size();
				callback.endRow();
			}
		}

		// print the content rows
		{
			CrosstabNavigator<?> navigator = crosstab.navigate();

			for (int i = 0; i < verticalCells; i++) {
				callback.beginRow();

				navigateOnAxis(verticalDimensions, i, verticalCells, navigator);

				// print the vertical headers
				{

					int rowspan = verticalCells;
					for (int j = 0; j < verticalDimensions.size(); j++) {
						CrosstabDimension dimension = verticalDimensions.get(j);
						rowspan = rowspan / dimension.getCategoryCount();

						if (i % rowspan == 0) {
							String category = navigator.getCategory(dimension);

							callback.verticalHeaderCell(category, dimension,
									rowspan);
						}
					}
				}

				for (int j = 0; j < horizontalCells; j++) {

					navigateOnAxis(horizontalDimensions, j, horizontalCells,
							navigator);

					callback.valueCell(navigator.get(), navigator.explore());
				}

				callback.endRow();
			}
		}

		callback.endTable();

		return callback.getResult();
	}

	private void navigateOnAxis(List<CrosstabDimension> dimensionsOnAxis,
			int cellIndex, int cellCount, CrosstabNavigator<?> navigator) {
		int colspan = cellCount;
		int category = 0;
		int localIndex = cellIndex;

		for (int k = 0; k < dimensionsOnAxis.size(); k++) {
			CrosstabDimension dimension = dimensionsOnAxis.get(k);

			int categoryCount = dimension.getCategoryCount();

			int offset = category * colspan;

			colspan = colspan / categoryCount;

			localIndex = localIndex - offset;

			category = localIndex / colspan;

			String categoryName = dimension.getCategories().get(category);
			navigator.where(dimension, categoryName);
		}
	}
}
