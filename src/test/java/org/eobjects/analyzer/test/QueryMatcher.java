package org.eobjects.analyzer.test;

import org.easymock.IArgumentMatcher;

import dk.eobjects.metamodel.query.Query;

public class QueryMatcher implements IArgumentMatcher {

	private String queryToString;

	public QueryMatcher(String queryToString) {
		if (queryToString == null) {
			throw new NullPointerException();
		}
		this.queryToString = queryToString;
	}

	@Override
	public boolean matches(Object argument) {
		Query q = (Query) argument;
		return queryToString.equals(q.toString());
	}

	@Override
	public void appendTo(StringBuffer buffer) {
		buffer.append("QueryMatcher(" + queryToString + ")");
	}
}
