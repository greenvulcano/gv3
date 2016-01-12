/*
 * Copyright (c) 2009-2016 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */

package datahandler.mongodb;

import it.greenvulcano.gvesb.datahandling.DHResult;
import it.greenvulcano.gvesb.datahandling.IDBOBuilder;
import it.greenvulcano.gvesb.datahandling.factory.DHFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;

/**
 * @version 3.5.0 Jun 03, 2015
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class DataHandlerMongoDBTestCase extends TestCase
{

	private MongoClient mongoClient;

	private DHFactory  dhFactory;
	
	
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		System.out.println("SET UP...");
		try {
			String serverMongoURI = "mongodb://localhost:27017";
			MongoClientURI connectionString = new MongoClientURI(serverMongoURI);
			mongoClient = new MongoClient(connectionString);
			//mongoClient = new MongoClient("mongodb://localhost:27017");
			
			CommonsMongo.createDB(mongoClient);
		}
		catch(MongoException exc)
		{
			System.out.println("CommonsMongo.createDB --> ERROR_CODE: "+exc.getCode() + " - ERROR_MSG: "+exc.getMessage());
		}
		finally {
			//DO NOTHING
			//mongoClient.close();
		}
		dhFactory = new DHFactory();

		dhFactory.initialize(null);
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		CommonsMongo.clearDB(mongoClient);
		
		if (dhFactory != null) {
			dhFactory.destroy();
		}
		
		mongoClient.close();
	}

	
	/**
	 * INSERT with mongo statement in the XML Configuration without params and SELECT with statements in XML
	 * @throws Exception
	 * 
	 */
	public void testDHCallInsertSelectMongoDB() throws Exception
	{
		System.out.println("MULTI OPERATION -> testDHCallInsertSelectMongoDB - START");
		
		//INSERT
		System.out.println("OPERATION: GVESB::TestInsertMongoDB - START");

		String operation = "GVESB::TestInsertMongoDB";
		IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
		DHResult result = dboBuilder.EXECUTE(operation, null, null);
		System.out.println("RESULT INSERT: "+result);

		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(1, result.getTotal());
		assertEquals(1, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());
		
		System.out.println("OPERATION: GVESB::TestInsertMongoDB - END");

		//SELECT
		System.out.println("OPERATION GVESB::TestSelectMongoDB - START");		
		operation = "GVESB::TestSelectMongoDB";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ITEM_ID", "AAAA");
		result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("RESULT: "+result);

		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(0, result.getTotal());
		assertEquals(0, result.getInsert());
		assertTrue(result.getRead() > 0);
		assertEquals("", result.getDiscardCauseListAsString());

		String output = new String((byte[]) result.getData());
		System.out.println("OPERATION GVESB::TestSelectMongoDB output: "+output);
		//System.out.println( "OUTPUT SELECT: "+new String(getByteArray(result.getData())) );

		System.out.println("OPERATION GVESB::TestSelectMongoDB - END");
		
		System.out.println("MULTI OPERATION -> testDHCallInsertSelectMongoDB - END");
	}

//	
//	public void testDHCallSelectPropsMongoDB() throws Exception
//	{
//		//SELECT WITH PARAMS.
//		System.out.println("testDHCallSelectPropsMongoDB - START");
//		
//		System.out.println("OPERATION --> GVESB::TestSelectXmlMongoDB - START");
//		String operation = "GVESB::TestSelectXmlMongoDB";
//		IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("ITEM_ID", "BBBB");
//		params.put("COMPANY", "ACME");
//		
//		DHResult result = dboBuilder.EXECUTE(operation, null, params);
//		System.out.println("RESULT SELECT: "+result);
//
//		assertNotNull(result);
//		assertEquals(0, result.getDiscard());
//		assertEquals(0, result.getUpdate());
//		assertEquals(0, result.getTotal());
//		assertEquals(0, result.getInsert());
//		assertTrue(result.getRead() > 0);
//		assertEquals("", result.getDiscardCauseListAsString());
//
//		String output = new String((byte[]) result.getData());
//		System.out.println("testDHCallSelectPropsMongoDB output: "+output);
//		System.out.println("OPERATION --> GVESB::TestSelectXmlMongoDB - END");
//		
//		System.out.println("testDHCallSelectPropsMongoDB - END");
//		//System.out.println( "OUTPUT SELECT: "+new String(getByteArray(result.getData())) );
//	}
//	
	
	/**
	 * INSERT with mongo statement in the XML Configuration with props.
	 * @throws Exception
	 * 
	 */
	public void testDHCallInsertSelectPropsMongoDB() throws Exception
	{
		//INSERT WITH PARAMS.
		System.out.println("MULTI testDHCallInsertSelectPropsMongoDB - START");

		System.out.println("OPERATION --> GVESB::TestInsertPropsMongoDB - START");
		String operation = "GVESB::TestInsertPropsMongoDB";
		IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ITEM_ID", "BBBB");
		params.put("COMPANY", "ACME");

		DHResult result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("RESULT INSERT: "+result);

		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(1, result.getTotal());
		assertEquals(1, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		System.out.println("OPERATION --> GVESB::TestInsertPropsMongoDB - END");
		
		//SELECT WITH PARAMS.
		System.out.println("OPERATION --> GVESB::TestSelectXmlMongoDB - START");
		operation = "GVESB::TestSelectXmlMongoDB";
		dboBuilder = dhFactory.getDBOBuilder(operation);

		result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("RESULT: "+result);

		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(0, result.getTotal());
		assertEquals(0, result.getInsert());
		assertTrue(result.getRead() > 0);
		assertEquals("", result.getDiscardCauseListAsString());

		String output = new String((byte[]) result.getData());
		System.out.println("testDHCallSelectMongoDB output: "+output);
		//System.out.println( "OUTPUT SELECT: "+new String(getByteArray(result.getData())) );
		System.out.println("OPERATION --> GVESB::TestSelectXmlMongoDB - END");
		
		System.out.println("MULTI testDHCallInsertSelectPropsMongoDB - END");
	}

	/**
	 * INSERT without mongo statement in the XML Configuration but with RowSet element data defined as input documents.
	 * @throws Exception
	 * 
	 */
	public void testDHCallInsertMultiMongoDB() throws Exception
	{
		System.out.println("testDHCallInsertMultiMongoDB - START");

		String operation = "GVESB::TestInsertMultiMongoDB";
		IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);

		DHResult result = dboBuilder.EXECUTE(operation, createInsertMongoTextMessage(), null);
		System.out.println("RESULT INSERT: "+result);

		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(2, result.getTotal());
		assertEquals(2, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());
		System.out.println("testDHCallInsertMultiMongoDB - END");
	}

	/**
	 * SELECT with mongo statement in the XML Configuration with props.
	 * @throws Exception
	 * 
	 */
		public void testDHCallInsertSelectXMLStmtMongoDB() throws Exception
		{
			System.out.println("testDHCallInsertSelectXMLStmtMongoDB - START");
			
			String operation = "GVESB::TestInsertPropsMongoDB";
			IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("ITEM_ID", "ZZZZ");
			params.put("COMPANY", "ACME");

			DHResult result = dboBuilder.EXECUTE(operation, null, params);
			System.out.println("RESULT INSERT: "+result);

			assertNotNull(result);
			assertEquals(0, result.getDiscard());
			assertEquals(0, result.getUpdate());
			assertEquals(1, result.getTotal());
			assertEquals(1, result.getInsert());
			assertEquals(0, result.getRead());
			assertEquals("", result.getDiscardCauseListAsString());
			
			operation = "GVESB::TestSelectMongoDB";
			dboBuilder = dhFactory.getDBOBuilder(operation);
			params = new HashMap<String, Object>();
			params.put("ITEM_ID", "ZZZZ");
			result = dboBuilder.EXECUTE(operation, null, params);
			System.out.println("RESULT: "+result);
	
			assertNotNull(result);
			assertEquals(0, result.getDiscard());
			assertEquals(0, result.getUpdate());
			assertEquals(0, result.getTotal());
			assertEquals(0, result.getInsert());
			assertTrue(result.getRead() > 0);
			assertEquals("", result.getDiscardCauseListAsString());
			
			String output = new String((byte[]) result.getData());
			System.out.println("testDHCallSelectMongoDB output: "+output);
			//System.out.println( "OUTPUT SELECT: "+new String(getByteArray(result.getData())) );
			
			System.out.println("testDHCallInsertSelectXMLStmtMongoDB - END");
		}

	/**
	 * UPDATE with mongo statement in the XML Configuration with props.
	 * @throws Exception
	 * 
	 */
	public void testDHCallUpdateMongoDB() throws Exception
	{
		System.out.println("testDHCallUpdateMongoDB - START");
		
		String operation = "GVESB::TestInsertPropsMongoDB";
		IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ITEM_ID", "XXXX");
		params.put("COMPANY", "ACME");

		DHResult result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("RESULT INSERT: "+result);

		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(1, result.getTotal());
		assertEquals(1, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		operation = "GVESB::TestUpdateMongoDB";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		params = new HashMap<String, Object>();
		params.put("ITEM_ID", "XXXX");

		result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("RESULT UPDATE: "+result);

		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertTrue(result.getUpdate() > 0);
		assertTrue(result.getTotal() > 0);
		assertEquals(0, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		System.out.println("testDHCallUpdateMongoDB - END");
	}

	/**
	 * UPSERT without mongo statement in the XML Configuration but with UPSERT RowSet element data defined as input documents.
	 * 
	 * @throws Exception
	 * 
	 */
	public void testDHCallUpsertMongoDB() throws Exception
	{
		System.out.println("testDHCallUpsertMongoDB - START");

		String operation = "GVESB::TestUpsertMongoDBInpuRowSet";
		IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
		DHResult result = dboBuilder.EXECUTE(operation, createUpsertMongoTextMessage(), null);
		System.out.println("RESULT UPSERT: "+result);

		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertTrue(result.getUpdate() + result.getInsert()  > 0 );
		assertTrue(result.getTotal() > 0);
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		System.out.println("testDHCallUpsertMongoDB - END");
	}

	/**
	 * DELETE with mongo statement in the XML Configuration with props.
	 * 
	 * @throws Exception
	 * 
	 */
	public void testDHCallDeleteMongoDB() throws Exception
	{
		System.out.println("testDHCallDeleteMongoDB - START");

		String operation = "GVESB::TestInsertPropsMongoDB";
		IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ITEM_ID", "XXXX");
		params.put("COMPANY", "ACME");

		DHResult result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("RESULT INSERT: "+result);

		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(1, result.getTotal());
		assertEquals(1, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());
		
		operation = "GVESB::TestDeleteMongoDB";
		params = new HashMap<String, Object>();
		params.put("ITEM_ID", "XXXX");
		dboBuilder = dhFactory.getDBOBuilder(operation);
		result = dboBuilder.EXECUTE(operation, null, null);
		System.out.println("RESULT DELETE: "+result);

		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate() + result.getInsert());
		assertEquals(0,result.getTotal());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());
		
		operation = "GVESB::TestSelectXmlMongoDB";
		params = new HashMap<String, Object>();
		params.put("ITEM_ID", "XXXX");
		dboBuilder = dhFactory.getDBOBuilder(operation);
		result = dboBuilder.EXECUTE(operation, null, null);
		System.out.println("RESULT SELECT: "+result);

		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate() + result.getInsert());
		assertEquals(0, result.getTotal());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());
		
		System.out.println("testDHCallDeleteMongoDB - END");
	}
	
	/**
	 * MAP REDUCE with mongo statement in the XML Configuration with props.
	 * 
	 * @throws Exception
	 * 
	 */
	public void testDHCallMapReduceMongoDB() throws Exception
	{
		//INSERT WITH PARAMS.
		System.out.println("testDHCallInsertPropsMongoDB - START");

		String operation = "GVESB::TestInsertMapReduceMongoDB";
		IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("CUST_ID", "A123");
		params.put("AMOUNT", "500");
		params.put("STATUS", "A");
		
		DHResult result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("RESULT INSERT: "+result);

		params.put("CUST_ID", "A123");
		params.put("AMOUNT", "250");
		params.put("STATUS", "A");
		result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("RESULT INSERT: "+result);
		
		params.put("CUST_ID", "B121");
		params.put("AMOUNT", "250");
		params.put("STATUS", "A");
		result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("RESULT INSERT: "+result);
		
		params.put("CUST_ID", "A123");
		params.put("AMOUNT", "300");
		params.put("STATUS", "D");
		result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("RESULT INSERT: "+result);
		
		System.out.println("testDHCallInsertPropsMongoDB - END");
		
		
		System.out.println("testDHCallMapReduceMongoDB - START");
		operation = "GVESB::TestMapReduceMongoDB";
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("ITEM_ID", "ROW1_UPSERTED4");
		dboBuilder = dhFactory.getDBOBuilder(operation);
		result = dboBuilder.EXECUTE(operation, null, null);
		System.out.println("RESULT MAP REDUCE: "+result);

		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate() + result.getInsert());
		
		assertEquals(0,result.getTotal());
		assertTrue(result.getRead() > 0);
		assertEquals("", result.getDiscardCauseListAsString());
		
		String output = new String((byte[]) result.getData());
		System.out.println("OPERATION GVESB::TestMapReduceMongoDB output: "+output);

		System.out.println("testDHCallMapReduceMongoDB - END");
	}


	private static byte[] getByteArray(Object inputObject) throws IOException {
		byte[] readBytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			out = new ObjectOutputStream(bos);   
			out.writeObject(inputObject);
			out.flush();
			readBytes = bos.toByteArray();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException ex) {
				// ignore close exception
			}
			try {
				bos.close();
			} catch (IOException ex) {
				// ignore close exception
			}
		}

		return readBytes;
	}

	// Not Used.
	public static org.bson.Document createInsertMongoMessage()
	{
		List<org.bson.Document> data = new ArrayList<org.bson.Document>();

		List<org.bson.Document> rows = new ArrayList<org.bson.Document>();
		rows.add(new org.bson.Document( "ID0_ROW1", "{item: \"ROW1\", details: { model: \"0001\", manufacturer: \"XYZ Company\"}}" ) );
		rows.add(new org.bson.Document( "ID0_ROW2", "{item: \"ROW2\", details: { model: \"0002\", manufacturer: \"XYZ Company\"}}" ) );

		data.add(new org.bson.Document("id", 0));
		data.add(new org.bson.Document("row", rows));

		org.bson.Document doc = new org.bson.Document("RowSet", new org.bson.Document("data",  data ));

		System.out.println("OUT: "+doc);

		return doc;
	}

	public static String createInsertMongoTextMessage(){
		String txt = "{ \"RowSet\" : \n" + 
				"    { \"data\" : [\n" + 
				"	            { \"id\" : 0, \n" + 
				"			      \"row\" : [\n" + 
				"			               { \"ID0_ROW1\" : {item: \"ROW1\", details: { model: \"0001\", manufacturer: \"XYZ Company\"}} }\n" + 
				"						 , { \"ID0_ROW2\" : {item: \"ROW2\", details: { model: \"0002\", manufacturer: \"XYZ Company\"}} }\n" + 
				"						  ] \n" + 
				"				}\n" + 
				"			   ] \n" + 
				"    }\n" + 
				"}";

		return txt;
	}
	
	public static String createInsertMongoTextMessageMapReduce(){
		String txt = "{ \"RowSet\" : \n" + 
				"    { \"data\" : [\n" + 
				"	            { \"id\" : 0, \n" + 
				"			      \"row\" : [\n" + 
				"			               { \"cust_id\" : \"A123\", \"amount\" : \"500\", \"status\" : \"A\" } \n" + 
				"			              ,{ \"cust_id\" : \"A123\", \"amount\" : \"250\", \"status\" : \"A\" } \n" + 
				"			              ,{ \"cust_id\" : \"B121\", \"amount\" : \"200\", \"status\" : \"A\" } \n" + 
				"			              ,{ \"cust_id\" : \"A123\", \"amount\" : \"300\", \"status\" : \"D\" } \n" + 
				"						  ] \n" + 
				"				}\n" + 
				"			   ] \n" + 
				"    }\n" + 
				"}";

		return txt;
	}


	public static String createUpsertMongoTextMessage(){
		String txt = "{ \"RowSet\" : \n" + 
				"    { \"data\" : [\n" + 
				"	            { \"id\" : 0, \n" + 
				"			      \"row\" : [ { \n" + 
				"			               \"w\" : { \"ID0_ROW1.item\": \"ROW1_UPSERTED4\" } , \"d\" : { $set : { \"IDO_ROW1.details.manufacturer\": \"XYZ Company Upsert\" } }\n" + 
				"						  } ] \n" + 
				"				}\n" + 
				"			   ] \n" + 
				"    }\n" + 
				"}";

		return txt;
	}

	public static void main(String[] args) {
		String st = createUpsertMongoTextMessage();
		System.out.println("UPSERT: "+st);
		
		st = createInsertMongoTextMessage();
		System.out.println("INSERT: "+st);
	}

}
