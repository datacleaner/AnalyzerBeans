package org.eobjects.analyzer.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;

public class CrosstabDimension implements Serializable, Iterable<String> {

	private static final long serialVersionUID = 1L;

	private List<String> categories = new ArrayList<String>();
	private String name;

	public CrosstabDimension(String name) {
		if (name.contains("|")) {
			throw new IllegalArgumentException(
					"Dimensions cannot contain the character '^'");
		}
		this.name = name;
	}

	public void addCategory(String category) {
		if (!categories.contains(category)) {
			categories.add(category);
		}
	}

	public String getName() {
		return name;
	}

	public boolean containsCategory(String category) {
		return categories.contains(category);
	}

	public List<String> getCategories() {
		return Collections.unmodifiableList(categories);
	}

	@Override
	public Iterator<String> iterator() {
		return getCategories().iterator();
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == getClass()) {
			CrosstabDimension that = (CrosstabDimension) obj;
			return new EqualsBuilder().append(this.name, that.name)
					.append(this.categories, that.categories).isEquals();
		}
		return false;
	}

	public int getCategoryCount() {
		return categories.size();
	}

	@Override
	public String toString() {
		return "CrosstabDimension[name=" + name + ", categories="
				+ categories + "]";
	}

}
