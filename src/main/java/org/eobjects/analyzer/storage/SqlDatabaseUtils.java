package org.eobjects.analyzer.storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SqlDatabaseUtils {

	private final static Logger logger = LoggerFactory.getLogger(SqlDatabaseUtils.class);

	private SqlDatabaseUtils() {
		// prevent instantiation
	}

	public static void performUpdate(Connection connection, String sql) {
		Statement st = null;
		try {
			st = connection.createStatement();
			st.executeUpdate(sql);
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		} finally {
			safeClose(null, st);
		}
	}

	public static void safeClose(ResultSet rs, Statement st) {
		if (rs != null) {
			try {
				if (rs.isClosed()) {
					logger.info("result set is already closed: {}", rs);
				} else {
					logger.debug("closing result set: {}", rs);
					rs.close();
				}
			} catch (SQLException e) {
				logger.warn("could not close result set", e);
			}
		}

		if (st != null) {
			try {
				if (st.isClosed()) {
					logger.info("statement is already closed: {}", st);
				} else {
					logger.debug("closing statement: {}", st);
					st.close();
				}
			} catch (SQLException e) {
				logger.warn("could not close statement", e);
			}
		}
	}
}
