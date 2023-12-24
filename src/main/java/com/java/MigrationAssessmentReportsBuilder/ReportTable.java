package com.java.MigrationAssessmentReportsBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.java.MigrationAssessmentReportsBuilder.utility.DatabaseUtils;
import com.java.MigrationAssessmentReportsBuilder.utility.Properties;

@Component
@ComponentScan(basePackages = { "com.java.MigrationAssessmentReportsBuilder.utility" })
public class ReportTable {
		
	private Statement stmt = null;
	
	@Autowired
	private	DatabaseUtils DatabaseUtils;
	
	@Autowired
	private  Properties property;
	
	
	public void run() throws Exception {
		
		// Retrieve all table names in a list from the report property file 
		List<String> TableList = Arrays.asList(property.getProperty("report.tables").split(","));
		
		stmt = DatabaseUtils.getNewStatement();
		
		try {
			// Drop table if it already exists
			dropTable(TableList);
		} catch (Exception ignore) { }
		
		// 1. Creation of tables for report
		createReportTable(TableList);
		
		// 2. Insertion of data into tables for report
		insertIntoReportTable();
		
		DatabaseUtils.closeStatement(stmt);
	}

	
	// This method is used for creating all report table in database
	public void createReportTable(List<String> TableList) throws Exception {
		
		String colVarcharSize = property.getProperty("report.table.column.VARCHAR").trim();
		
		for (String strTableName : TableList) {
			
			strTableName = strTableName.trim();
			
			String strColList = property.getProperty("report.table.column." + strTableName);

			// Retrieve all column names for every table for table creation
			List<String> columnList = Arrays.asList(strColList.split(","));
			
			// Create a new string builder for creating tables for every table
			StringBuilder sbCreateTable = new StringBuilder();
			sbCreateTable.append("CREATE TABLE ").append(strTableName).append("(");

			// Append all column names with data-type for the create table query
			for (String strColName : columnList) {
				strColName = strColName.trim().replace("_"," ");

				if(strColName.equals("#")) {
					sbCreateTable.append("\""+strColName+"\"").append(" INT AUTO_INCREMENT");
				} else if(!strColName.isEmpty()) {
					sbCreateTable.append("\""+strColName+"\"").append(" ").append(colVarcharSize);
				}
				sbCreateTable.append(",");
			}
			
			// Remove last comma
			sbCreateTable.setLength(sbCreateTable.length() - 1);
			sbCreateTable.append(")");
			stmt.execute(sbCreateTable.toString());
		}
	}
	
	
	//This method is used for inserting data in report table in database
	public void insertIntoReportTable() throws Exception {
		
		Statement insert = DatabaseUtils.getNewStatement();
		ResultSet rs = null;
		
		// Retrieve total report
		int totalReportTable  = Integer.parseInt(property.getProperty("total.reports.count"));
		
		for (int reportIndex = 1; reportIndex <= totalReportTable; reportIndex++) {
			
			String status = property.getProperty("report."+ reportIndex +".status");
			
			// Retrieve the query for every report
			String query = property.getProperty("report."+ reportIndex +".query");
			
			// Retrieve the table name for data insertion for every report
			String tableName = property.getProperty("report."+ reportIndex +".name");
			List<String> columnList = new ArrayList<String>(Arrays.asList(property.getProperty("report.table.column."+tableName).split(",")));
			
			// Remove the first element index column from columnList because it increments automatically, so there is no need to add it to the query
			if(columnList.contains("#")) {
				columnList.remove(0);
			}
			
			// Check status if it enable then proceed it
			if( status.equals("enable")) {
				
				// Execute Query and get result set
				rs = stmt.executeQuery(query);
				
				while (rs.next()) {
					
					// Call the getInsertQuery() method to get the insert query and execute it
					String insertQuery =  getInsertQuery(tableName, columnList, rs);
					insert.execute(insertQuery);
		        }  
			} else if( status.equals("special")  ) {
				
				// Call getSpecialCharacterList() method that return the  list of special character 
				ArrayList<Character> specialCharList = getSpecialCharacterList();
				
				for(int i=0; i<specialCharList.size(); i++) {
					
					// Get query for count of affected Objects and replace '{special_character}' with every special character from list
					String specialCharQuery = property.getProperty("report."+ reportIndex +".query");
					specialCharQuery =  specialCharQuery.replace("{special_character}",specialCharList.get(i).toString());
					
					// Execute Query and get result set
					rs = stmt.executeQuery( specialCharQuery);
					
					while (rs.next()) {
						
						// Call the getInsertQuery() method to get the insert query and execute it
						String insertQuery = getInsertQuery(tableName, columnList, rs);
						insert.execute(insertQuery);
			        } 
				}
			}
		}
		
		DatabaseUtils.closeStatement(insert);
	}
	
	
	// This Method used for deleting report table
	public void dropTable(List<String> TableList) throws Exception {
		for (String strTableName : TableList) {
			strTableName = strTableName.trim();
			String query = "DROP TABLE " + strTableName;
			stmt.execute(query);
		}
	}
	
	
	//  This method creates a new insert query for every record and returns it in string format
	public String getInsertQuery(String tableName, List<String> columnList, ResultSet rs) throws SQLException {
		
		// Create a new string builder for inserting data into the table for every record
		StringBuilder insertQuery = new StringBuilder();
		insertQuery.append("INSERT INTO " + tableName + "(");
		
		// Get the column name for inserting data into that column
		for(int i = 0; i < columnList.size(); i++) {
			insertQuery.append("\""+columnList.get(i).trim().replace("_", " ")+"\",");
		}
		
		// Remove last comma
		insertQuery.setLength(insertQuery.length() - 1);
		insertQuery.append(") VALUES(");
		
		// Get the column value and append based on the column list size
		for(int columnIndex=1; columnIndex <=columnList.size(); columnIndex++) {
			insertQuery.append("'" + rs.getString(columnIndex)+ "',");
		}
		
		// Remove last comma
		insertQuery.setLength(insertQuery.length() - 1);
		insertQuery.append(")");
		
		return insertQuery.toString();
	}
	
	
	// This method return list of special character
	public ArrayList<Character> getSpecialCharacterList() throws Exception { 
		
		ArrayList<Character> specialCharList = new ArrayList<Character>();
		
		// Query for getting a row that contains a special character
		String specialCharQuery = "SELECT DISTINCT TITLE FROM EXT_OBJECT_DATA WHERE REGEXP_LIKE(TITLE, '[^A-Za-z0-9 ]')";
		Statement specialCharStmt = DatabaseUtils.getNewStatement();
		
		// Execute Query and get result set
		ResultSet rs = specialCharStmt.executeQuery(specialCharQuery);
		
		while(rs.next()) {
			
			// Replace all of the alphanumeric data with "" in each row of data. 
			String title = rs.getString(1);
			title = title.replaceAll("[A-Za-z0-9 ]","") ;
			
			// Iterate through each special character from the list

			for(int i=0; i<title.length(); i++) {
				
				// If the list does not contain special characters, add them to the list; otherwise, do nothing
				if( !specialCharList.contains(title.charAt(i)) ) {
					specialCharList.add(title.charAt(i));
				}
			}
		}
		return specialCharList;
	}

}
