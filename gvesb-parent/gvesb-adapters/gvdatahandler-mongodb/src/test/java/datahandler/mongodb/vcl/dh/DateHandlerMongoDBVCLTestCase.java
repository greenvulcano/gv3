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
package datahandler.mongodb.vcl.dh;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.datahandler.DataHandlerCallOperation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.w3c.dom.Node;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;

import datahandler.mongodb.CommonsMongo;

/**
 * @version 3.5.0 Jun 23, 2015
 * @author GreenVulcano Developer Team
 *
 */
public class DateHandlerMongoDBVCLTestCase extends TestCase {

	private MongoClient mongoClient;

	@Override
	protected void setUp() throws Exception
	{
		System.out.println("SET UP...");
		try {
			String serverMongoURI = "mongodb://localhost:27017";
			MongoClientURI connectionString = new MongoClientURI(serverMongoURI);
			mongoClient = new MongoClient(connectionString);

			CommonsMongo.createDB(mongoClient);
		}
		catch(MongoException exc)
		{
			exc.printStackTrace();
			System.out.println("CommonsMongo.createDB --> ERROR_CODE: "+exc.getCode() + " - ERROR_MSG: "+exc.getMessage());
		}
		finally {
			//DO NOTHING
			//mongoClient.close();
		}
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		System.out.println("TEAR DOWN...");
		clearDB();
		
		mongoClient.close();
	}

	/**
	 * @throws SQLException
	 *
	 */
	private void clearDB() throws SQLException
	{
		CommonsMongo.clearDB(mongoClient);
				
		mongoClient.close();
	}
	
	/**
	 * INSERT with RowSet element data defined as input documents + Update .
	 * @throws Exception
	 * 
	 */
	public void testVCLDHCallInsertUpdateSelectMongoDB() throws Exception
	{
		System.out.println("VCL - testVCLDHCallInsertUpdateSelectMongoDB - START");

		DataHandlerCallOperation operation = new DataHandlerCallOperation();
		Node node = XMLConfig.getNode(
				"GVSystems.xml",
				"/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/dh-call[@name='test-dh-call-insert-update-mongo' and @type='call']");
		operation.init(node);

		GVBuffer gvBuffer = new GVBuffer("GVESB", "TestInsertUpdateMongoDB");
		gvBuffer.setProperty("ITEM_ID", "XXXX");
		
		GVBuffer result = operation.perform(gvBuffer);
		assertEquals(1, result.getRetCode());
		assertEquals("0", result.getProperty("REC_DISCARD"));
		assertEquals("1", result.getProperty("REC_UPDATE"));
		assertEquals("1", result.getProperty("REC_TOTAL"));
		assertEquals("0", result.getProperty("REC_INSERT"));
		assertEquals("0", result.getProperty("REC_READ"));
		assertEquals("", result.getProperty("REC_DISCARD_CAUSE"));
		
		System.out.println("RESULT testVCLDHCallInsertUpdateSelectMongoDB: " + result);

		System.out.println("VCL - testVCLDHCallInsertUpdateSelectMongoDB - END");
	}

	

	/**
	 * INSERT with RowSet element data defined as input documents.
	 * @throws Exception
	 * 
	 */
	public void testVCLDHCallInsertMultiMongoDB() throws Exception
	{
		System.out.println("VCL - testVCLDHCallInsertMultiMongoDB - START");

		DataHandlerCallOperation operation = new DataHandlerCallOperation();
		Node node = XMLConfig.getNode(
				"GVSystems.xml",
				"/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/dh-call[@name='test-dh-call-insert-multi-mongo' and @type='call']");
		operation.init(node);

		GVBuffer gvBuffer = new GVBuffer("GVESB", "TestInsertMultiMongoDB");
		gvBuffer.setObject(createInsertMongoTextMessage());
		GVBuffer result = operation.perform(gvBuffer);
		assertEquals(1, result.getRetCode());
		assertEquals("0", result.getProperty("REC_DISCARD"));
		assertEquals("0", result.getProperty("REC_UPDATE"));
		assertEquals("2", result.getProperty("REC_TOTAL"));
		assertEquals("2", result.getProperty("REC_INSERT"));
		assertEquals("0", result.getProperty("REC_READ"));
		assertEquals("", result.getProperty("REC_DISCARD_CAUSE"));

		String output = (String) result.getObject();
		System.out.println("VCL - OUTPUT MultiInsertMongo: "+output);

		System.out.println("VCL - testVCLDHCallInsertMultiMongoDB - END");
	}


	/**
	 * INSERT + MAP REDUCE
	 * Insert with RowSet as Object input in the collection "test_in_mapreduce" + MapReduce 
	 * on that collection and OUTPUT INLINE.
	 * 
	 * @throws Exception
	 * 
	 */
	public void testVCLDHCallMapReduceMongoDB() throws Exception
	{
		//INSERT WITH PARAMS.
		System.out.println("VCL - testVCLDHCallMapReduceMongoDB - START");

		DataHandlerCallOperation operation = new DataHandlerCallOperation();

		Node node = XMLConfig.getNode(
				"GVSystems.xml",
				"/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/dh-call[@name='test-dh-call-insert-mapreduce-mongo' and @type='call']");
		operation.init(node);
		
		GVBuffer gvBuffer = new GVBuffer("GVESB", "TestInsertAndMapReduceMongoDB");
		System.out.println("VCL - insert Mongo: \n" + createInsertMongoTextMessageMapReduce());
		gvBuffer.setObject(createInsertMongoTextMessageMapReduce());
		
		GVBuffer result = operation.perform(gvBuffer);
		
		System.out.println("VCL - RESULT MAP REDUCE: "+result);

		assertNotNull(result);
		assertEquals("0", result.getProperty("REC_DISCARD"));
		assertEquals("0", result.getProperty("REC_UPDATE"));
		assertEquals("0", result.getProperty("REC_TOTAL"));
		assertEquals("0", result.getProperty("REC_INSERT"));
		assertEquals("2", result.getProperty("REC_READ"));
		assertEquals("", result.getProperty("REC_DISCARD_CAUSE"));

		String output = new String((byte[]) result.getObject());
		System.out.println("VCL - testVCLDHCallMapReduceMongoDB output: "+output);
		
		System.out.println("VCL - testVCLDHCallMapReduceMongoDB - END");
	}

	/**
	 * INSERT + SELECT.
	 * INSERT defined in the XML Configuration with props and SELECT with props in the XML.
	 * 
	 * @throws Exception
	 * 
	 */
	public void testDHCallInsertSelectPropsMongoDB() throws Exception
	{
		//SELECT WITH PARAMS.
		System.out.println("VCL - testDHCallInsertSelectPropsMongoDB - START");

		DataHandlerCallOperation operation = new DataHandlerCallOperation();

		Node node = XMLConfig.getNode(
				"GVSystems.xml",
				"/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/dh-call[@name='test-dh-call-select-mongo' and @type='call']");
		operation.init(node);

		GVBuffer gvBuffer = new GVBuffer("GVESB", "TestInsertSelectXmlMongoDB");
		gvBuffer.setProperty("ITEM_ID", "XXXX");
		GVBuffer result = operation.perform(gvBuffer);

		System.out.println("VCL - RESULT SELECT: "+result);

		assertNotNull(result);
		assertEquals("0", result.getProperty("REC_DISCARD"));
		assertEquals("0", result.getProperty("REC_UPDATE"));
		assertEquals("0", result.getProperty("REC_TOTAL"));
		assertEquals("0", result.getProperty("REC_INSERT"));
		assertEquals("1", result.getProperty("REC_READ"));
		assertEquals("", result.getProperty("REC_DISCARD_CAUSE"));

		String output = new String((byte[]) result.getObject());
		System.out.println("VCL - testDHCallSelectPropsMongoDB output: "+output);
		System.out.println("VCL - OPERATION --> GVESB::TestSelectXmlMongoDB - END");

		System.out.println("VCL - testDHCallInsertSelectPropsMongoDB - END");
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
	
	public static String createInsertMongoTextMessageMapReduce(){
		String txt = "{ \"RowSet\" : \n" + 
				"    { \"data\" : [\n" + 
				"	            { \"id\" : 0, \n" + 
				"			      \"row\" : [\n" + 
				"			               { \"cust_id\" : \"A123\", \"amount\" : 500, \"status\" : \"A\" } \n" + 
				"			              ,{ \"cust_id\" : \"A123\", \"amount\" : 250, \"status\" : \"A\" } \n" + 
				"			              ,{ \"cust_id\" : \"B121\", \"amount\" : 200, \"status\" : \"A\" } \n" + 
				"			              ,{ \"cust_id\" : \"A123\", \"amount\" : 300, \"status\" : \"D\" } \n" + 
				"						  ] \n" + 
				"				}\n" + 
				"			   ] \n" + 
				"    }\n" + 
				"}";

		return txt;
	}

	public static void main(String[] args) {
		//String st = createUpsertMongoTextMessage();
		String st = createInsertMongoTextMessageMapReduce();
		System.out.println("st: "+st);
	}


}
