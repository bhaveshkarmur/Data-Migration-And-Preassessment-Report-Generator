package com.java.MigrationAssessmentReportsBuilder.utility;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class DatabaseUtils {
	
	private static Connection conn = null;
	
	@Autowired
	private DataSource dataSource;
	
	// New Connection
	public Connection getConnection() throws SQLException {
		if (conn == null) {
			conn = dataSource.getConnection();
		}
		return conn;
	}

	
	// Close Connection
	public void closeConnection(Connection connection) throws SQLException {
		if (connection != null && !connection.isClosed()) {
			connection.close();
		}
	}
	
	
	// New Statement
	public Statement getNewStatement() throws SQLException {
		if (conn == null) {
			conn = dataSource.getConnection();
		}
		return conn.createStatement();
	}
	
	
	// Close Statement
	public void closeStatement(Statement stmt) throws SQLException {
		if (stmt != null && !stmt.isClosed()) {
			stmt.close();
		}
	}
}
