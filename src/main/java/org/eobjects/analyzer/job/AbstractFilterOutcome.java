package org.eobjects.analyzer.job;

import java.util.List;

import org.eobjects.analyzer.job.builder.LazyFilterOutcome;

import dk.eobjects.metamodel.util.BaseObject;

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
public abstract class AbstractFilterOutcome extends BaseObject implements FilterOutcome {

	@Override
	protected final void decorateIdentity(List<Object> identifiers) {
		identifiers.add(getCategory());
		identifiers.add(getFilterJob());
	}

	@Override
	protected final boolean classEquals(BaseObject obj) {
		// works with all subtypes
		return obj instanceof FilterOutcome;
	}

	@Override
	public String toString() {
		return "FilterOutcome[category=" + getCategory() + "]";
	}

	@Override
	public final boolean satisfiesRequirement(Outcome requirement) {
		return equals(requirement);
	}
}
