package org.eobjects.analyzer.util;

import java.io.Serializable;
import java.util.Date;

public class TimeInterval implements Serializable, Comparable<TimeInterval> {

	private static final long serialVersionUID = 1L;

	private boolean _infiniteFrom = false;
	private boolean _infiniteTo = false;
	private long _from;
	private long _to;

	public TimeInterval(Date from, Date to) {
		if (from == null) {
			_infiniteFrom = true;
		} else {
			_from = from.getTime();
		}
		if (to == null) {
			_infiniteTo = true;
		} else {
			_to = to.getTime();
		}
	}

	public boolean before(TimeInterval o) {
		return compareTo(o) < 0;
	}

	public boolean after(TimeInterval o) {
		return compareTo(o) > 0;
	}

	public TimeInterval(Long from, Long to) {
		if (from == null) {
			_infiniteFrom = true;
		} else {
			_from = from;
		}
		if (to == null) {
			_infiniteTo = true;
		} else {
			_to = to;
		}
	}

	public Long getFrom() {
		if (_infiniteFrom) {
			return null;
		}
		return _from;
	}

	public Long getTo() {
		if (_infiniteTo) {
			return null;
		}
		return _to;
	}

	public boolean isInfiniteFrom() {
		return _infiniteFrom;
	}

	public boolean isInfiniteTo() {
		return _infiniteTo;
	}

	@Override
	public int compareTo(TimeInterval o) {
		int diff = CompareUtils.compare(getFrom(), o.getFrom());
		if (diff == 0) {
			if (isInfiniteTo() && o.isInfiniteTo()) {
				return 0;
			}
			if (isInfiniteTo()) {
				return 1;
			}
			if (o.isInfiniteTo()) {
				return -1;
			}
			diff = CompareUtils.compare(getTo(), o.getTo());
		}
		return diff;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (_from ^ (_from >>> 32));
		result = prime * result + (_infiniteFrom ? 1231 : 1237);
		result = prime * result + (_infiniteTo ? 1231 : 1237);
		result = prime * result + (int) (_to ^ (_to >>> 32));
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
		TimeInterval other = (TimeInterval) obj;
		if (_from != other._from)
			return false;
		if (_infiniteFrom != other._infiniteFrom)
			return false;
		if (_infiniteTo != other._infiniteTo)
			return false;
		if (_to != other._to)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TimeInterval[" + getFrom() + "->" + getTo() + "]";
	}
}
