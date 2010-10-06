package org.eobjects.analyzer.job;

/**
 * Provides hashCode, equals and toString implementations for FilterOutcome,
 * making them comparable across different implementations.
 * 
 * Specifically this has been designed to make it possible to use the
 * equals(...) method with both ImmutableFilterOutcome and LazyFilterOutcome
 * instances.
 * 
 * @see ImmutableFilterOutcome
 * @see LazyFilterOutcome
 * 
 * @author Kasper Sørensen
 * 
 */
public abstract class AbstractFilterOutcome implements FilterOutcome {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		Enum<?> category = getCategory();
		FilterJob filterJob = getFilterJob();
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + ((filterJob == null) ? 0 : filterJob.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof AbstractFilterOutcome) {
			Enum<?> category = getCategory();
			FilterJob filterJob = getFilterJob();
			AbstractFilterOutcome other = (AbstractFilterOutcome) obj;
			Enum<?> otherCategory = other.getCategory();
			if (category == null) {
				if (otherCategory != null)
					return false;
			} else if (!category.equals(otherCategory))
				return false;
			FilterJob otherFilterJob = other.getFilterJob();
			if (filterJob == null) {
				if (otherFilterJob != null)
					return false;
			} else if (!filterJob.equals(otherFilterJob))
				return false;
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "Outcome[category=" + getCategory() + "]";
	}
}
