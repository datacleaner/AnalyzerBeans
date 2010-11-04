package org.eobjects.analyzer.storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

final class SqlDatabaseUtils {

	private SqlDatabaseUtils() {
		// prevent instantiation
	}

	public static void performUpdate(Connection connection, String sql) {
		Statement st = null;
		try {
			st = connection.createStatement();
			st.execute(sql);
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		} finally {
			safeClose(null, st);
		}
	}

	public static void safeClose(ResultSet rs, Statement st) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		}

		if (st != null) {
			try {
				st.close();
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
