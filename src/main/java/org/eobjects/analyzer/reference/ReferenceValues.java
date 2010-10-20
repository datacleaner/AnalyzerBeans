package org.eobjects.analyzer.reference;

import java.util.Collection;

public interface ReferenceValues<E> {

	public Collection<E> getValues();

	public boolean containsValue(E value);
}
