package org.eobjects.analyzer.storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SqlDatabaseUtils {

	private final static Logger logger = LoggerFactory.getLogger(SqlDatabaseUtils.class);

	public final static String CREATE_TABLE_PREFIX = "CREATE CACHED TABLE ";

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
			boolean close = true;

			try {
				if (rs.isClosed()) {
					close = false;
					if (logger.isInfoEnabled()) {
						logger.info("result set is already closed: {}", rs);
						StackTraceElement[] stackTrace = new Throwable().getStackTrace();
						for (int i = 0; i < stackTrace.length && i < 5; i++) {
							logger.info(" - stack frame {}: {}", i, stackTrace[i]);
						}
					}
				}
			} catch (Throwable e) {
				logger.debug("could not determine if result set is already closed", e);
			}

			if (close) {
				logger.debug("closing result set: {}", rs);
				try {
					rs.close();
				} catch (SQLException e) {
					logger.warn("could not close result set", e);
				}
			}
		}

		if (st != null) {
			boolean close = true;

			try {
				if (st.isClosed()) {
					close = false;
					if (logger.isInfoEnabled()) {
						logger.info("statement is already closed: {}", st);
						StackTraceElement[] stackTrace = new Throwable().getStackTrace();
						for (int i = 0; i < stackTrace.length && i < 5; i++) {
							logger.info(" - stack frame {}: {}", i, stackTrace[i]);
						}
					}
				}
			} catch (Throwable e) {
				logger.debug("could not determine if statement is already closed", e);
			}

			if (close) {
				logger.debug("closing statement: {}", st);
				try {
					st.close();
				} catch (SQLException e) {
					logger.warn("could not close statement", e);
				}
			}
		}
	}
}
