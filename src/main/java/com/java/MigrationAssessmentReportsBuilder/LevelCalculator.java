package com.java.MigrationAssessmentReportsBuilder;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.java.MigrationAssessmentReportsBuilder.utility.DatabaseUtils;

@Component
@ComponentScan("com.java.MigrationAssessmentReportsBuilder.utility")
public class LevelCalculator {
	
	private Statement stmt = null;
	
	// HashMap for FROM_ID as key and TO_ID list as value for level finding
	private HashMap<String, ArrayList<String>> keyIDListMap = new HashMap<String, ArrayList<String>>();
		
	// HashMap for ID and Level storage
	private HashMap<String, Integer> idLevelStorageMap = new HashMap<String, Integer>();
	
	// HashMap for ID and Structure storage
	private HashMap<String, Integer> idStructureStorageMap = new HashMap<String, Integer>();
	
	@Autowired(required = false)
	private DatabaseUtils DatabaseUtils;
	
	public void run() throws Exception {
		stmt = DatabaseUtils.getNewStatement();

		try {
			// Add ID_LEVEL and STRUCTURE_COUNT columns in EXT_OBJECT_DATA
			String addColumn = "ALTER TABLE EXT_OBJECT_DATA  ADD (ID_LEVEL INT, STRUCTURE_COUNT INT)";
		
			stmt.execute(addColumn);
		} catch ( Exception e ) {}
		
		String selectAllIDQuery= "SELECT DISTINCT FROM_ID,TO_ID FROM EXT_STRUCTURE_DATA ORDER BY FROM_ID DESC, TO_ID DESC";
		
		// Get levels for all IDs
		getAllLevel(selectAllIDQuery);
	
		String selectIDQuery = "SELECT ID FROM EXT_OBJECT_DATA";
		// Insert Level and Structure for ID in table
		insertLevel(selectIDQuery);
		
		DatabaseUtils.closeStatement(stmt);
	}
	
	// Find To_ID list for FROM_ID and call recurisonLevelStructureCalculator(String key) method for every ID
	public void getAllLevel(String selectAllIDQuery) throws Exception {
			
		ArrayList<String> To_ID = null;
		ResultSet rs = stmt.executeQuery(selectAllIDQuery);
		// Find TO_ID List having the same FROM_ID and store it in a list, and that list is stored in HahMap for FROM_ID key
		while (rs.next()) {  
			String key = rs.getString(1);
			if(keyIDListMap.containsKey(key)) {
				To_ID.add(rs.getString(2).trim());
			}
			else {
				// Create a new array list for all new FROM_ID
				To_ID = new ArrayList<String>();
				
				// Store the first element of TO_ID having the same FROM_ID into ArrayList for all FROM_ID
				To_ID.add(rs.getString(2).trim());
				keyIDListMap.put(key, To_ID);
			}	
        }  
		
		// Iterate through all of FROM_ID and call the recursion function to find level and structure and store them into HashMap
		for (Map.Entry<String,ArrayList<String>> entry : keyIDListMap.entrySet()) {
			
			String key = entry.getKey();
			ArrayList<Integer> idLevelStructure = recurisonLevelStructureCalculator(key );
			
			if(!idLevelStorageMap.containsKey(key) && !idStructureStorageMap.containsKey(key)) {
				idLevelStorageMap.put(key, idLevelStructure.get(0));
				idStructureStorageMap.put(key, idLevelStructure.get(1));
			}
        }  
	}
	
	
	// This Method is used for count level and structure for Key/ID
	
	/*----------------------- IMPORTANT COMMENT ----------------------- */
	/* It returns an ArrayList of size 2, which contains level and structure for Key/ID
	 * First element of ArrayList is Level of Key/ID
	 * Second element of ArrayList is Structure of Key/ID
	 */
	public ArrayList<Integer> recurisonLevelStructureCalculator(String key){
		
		if(idLevelStorageMap.containsKey(key)) {
			
			// If the level and structure for correspondent ID is exit, then it returns it
			return new ArrayList<Integer>(Arrays.asList(idLevelStorageMap.get(key), idStructureStorageMap.get(key)));
			
		} else if(keyIDListMap.containsKey(key)) {
			
			// If keyIDListMap contain Key/ID it perform following steps
			                                                                    
			int StructureCount = 0, levelCount = 0;
			
			// Get the TO_ID list for correspondent ID
			ArrayList<String> idList = keyIDListMap.get(key);
			
			// ArrayList for storing level for TO_ID list
			ArrayList<Integer> levelList = new ArrayList<Integer>();
			
			for(int i=0; i<idList.size(); i++) {
				
				ArrayList<Integer> returnList = recurisonLevelStructureCalculator(idList.get(i));
				
				// Get levelCount and StructureCount for Key/ID and store them in the level and structure map
				levelCount = returnList.get(0);
				StructureCount = returnList.get(1);
				idLevelStorageMap.put(idList.get(i), levelCount);
				idStructureStorageMap.put(idList.get(i), StructureCount);
				
				// Increment level and store it in a level list
				levelCount++;
				levelList.add(levelCount);
			}
			 
			// Add size of idList to structure to get structure count for Key/ID
			StructureCount += idList.size();
			
			// Return maximum level from the TO_ID level list and structure
			return new ArrayList<Integer>(Arrays.asList(Collections.max(levelList), StructureCount));
			
		} else {
			
			// Return 0 for both level and structure if level or structure not found for ID
			return new ArrayList<Integer>(Arrays.asList(0,0)); 
		}
	}
	
	
	// This method is used for updating or inserting level and structure for Key/Id in database
	public void insertLevel(String selectIDQuery) throws Exception {
				
		int batchSize = 10000, batchCount = 0;
		Statement updateQuery = DatabaseUtils.getNewStatement();
		
		ResultSet rs = stmt.executeQuery(selectIDQuery);
		
		while(rs.next()) {
			
			// Get the ID from result set
			String ID = rs.getString("ID");
			int LevelCount, StructureCount;
			
			// If idLevelStorageMap contains ID, then update ID level to their level
			if(idLevelStorageMap.containsKey(ID)) {
				
				// Get level and structure for correspondent ID
				LevelCount = idLevelStorageMap.get(ID);
				StructureCount = idStructureStorageMap.get(ID);
				
				// String builder for update query
				String insertLevelQuery = "UPDATE EXT_OBJECT_DATA SET ID_LEVEL=" + LevelCount + ", STRUCTURE_COUNT=" + StructureCount + " WHERE ID='" + ID + "'";
				
				// Add batch to updateQuery until the batch count reaches batchSize
				updateQuery.addBatch(insertLevelQuery);
				batchCount++;
				
				// If batchCount % batchSize = 0, then execute all batches in updateQuery
				if( batchCount % batchSize == 0) {
					updateQuery.executeBatch();
					batchCount = 0;
				}
			}
		}
		
		// After a while loop, if batchCount is not zero, then execute the remaining batch
		if( batchCount != 0) {
			updateQuery.executeBatch();
		}
 
		DatabaseUtils.closeStatement(updateQuery);
	}
}
