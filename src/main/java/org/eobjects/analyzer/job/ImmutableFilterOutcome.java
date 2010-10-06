package org.eobjects.analyzer.job;

public final class ImmutableFilterOutcome implements FilterOutcome {

	private final FilterJob _filterJob;
	private final Enum<?> _category;

	public ImmutableFilterOutcome(FilterJob filterJob, Enum<?> category) {
		_filterJob = filterJob;
		_category = category;
	}

	@Override
	public FilterJob getFilterJob() {
		return _filterJob;
	}

	@Override
	public Enum<?> getCategory() {
		return _category;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_category == null) ? 0 : _category.hashCode());
		result = prime * result + ((_filterJob == null) ? 0 : _filterJob.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImmutableFilterOutcome other = (ImmutableFilterOutcome) obj;
		if (_category == null) {
			if (other._category != null)
				return false;
		} else if (!_category.equals(other._category))
			return false;
		if (_filterJob == null) {
			if (other._filterJob != null)
				return false;
		} else if (!_filterJob.equals(other._filterJob))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Outcome[category=" + _category + "]";
	}
}
