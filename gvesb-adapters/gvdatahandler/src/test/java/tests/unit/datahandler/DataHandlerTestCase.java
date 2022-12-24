/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
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

package tests.unit.datahandler;

import java.sql.Connection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.gvesb.datahandling.DHResult;
import it.greenvulcano.gvesb.datahandling.IDBOBuilder;
import it.greenvulcano.gvesb.datahandling.factory.DHFactory;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;
import junit.framework.TestCase;

/**
 * @version 3.0.0 Dec 13, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class DataHandlerTestCase extends TestCase
{

    private Connection connection;

    private DHFactory  dhFactory;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        Context context = new InitialContext();
        try {
            DataSource ds = (DataSource) context.lookup("openejb:Resource/testDHDataSource");
            this.connection = ds.getConnection();
        }
        finally {
            context.close();
        }
        Commons.createDB(this.connection);
        this.dhFactory = new DHFactory();
        this.dhFactory.initialize(null);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        if (this.dhFactory != null) {
            this.dhFactory.destroy();
        }
        Commons.clearDB(this.connection);
        this.connection.close();
    }

    /**
     * @throws Exception
     *
     */
    public void testDHCallSelect() throws Exception
    {
        String operation = "GVESB::TestSelect";
        IDBOBuilder dboBuilder = this.dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, null, null);
        assertNotNull(result);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getInsert());
        assertEquals(1, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());
        Document output = (Document) result.getData();
        assertNotNull(output);
        assertTrue(output.getDocumentElement().hasChildNodes());
        Node data = output.getDocumentElement().getChildNodes().item(0);
        assertTrue(data.hasChildNodes());
        Node row = data.getChildNodes().item(0);
        assertTrue(row.hasChildNodes());
        NodeList cols = row.getChildNodes();
        assertEquals(4, cols.getLength());
        String id = cols.item(0).getTextContent();
        assertEquals("1", id);
        String field1 = cols.item(1).getTextContent();
        assertEquals("testvalue", field1);
        String field2 = cols.item(2).getTextContent();
        assertEquals("20000101 12:30:45", field2);
        //assertEquals("20000101 18:30:45", field2);
        String field3 = cols.item(3).getTextContent();
        assertEquals("123,45", field3);
    }

    /**
     * @throws Exception
     *
     */
    public void testDHCallSelectJSON() throws Exception
    {
        String operation = "GVESB::TestSelectJSON";
        IDBOBuilder dboBuilder = this.dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, null, null);
        assertNotNull(result);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getInsert());
        assertEquals(1, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());

        System.out.println("testDHCallSelectJSON: " + result.getData());

        String output = (String) result.getData();
        assertNotNull(output);
        JSONArray jsa = new JSONArray(output);
        assertTrue(jsa.length() > 0);
        JSONObject data = jsa.getJSONObject(0);
        assertEquals(4 + 1, JSONObject.getNames(data).length);
        int id = data.getInt("ID");
        assertEquals(1, id);
        String field1 = data.getString("FIELD1");
        assertEquals("testvalue", field1);
        String field2 = data.getString("FIELD2");
        assertEquals("2000-01-01 12:30:45.0", field2);
        //assertEquals("20000101 18:30:45", field2);
        double field3 = data.getDouble("FIELD3");
        assertEquals(123,45, field3);
    }

    /**
     * @throws Exception
     *
     */
    public void testDHCallSelectExtended() throws Exception
    {
    	String ns = "http://www.greenvulcano.com/database";
        String operation = "GVESB::TestSelectExtended";
        IDBOBuilder dboBuilder = this.dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, null, null);
        assertNotNull(result);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getInsert());
        assertEquals(1, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());
        Document output = (Document) result.getData();
        assertNotNull(output);
        assertTrue(output.getDocumentElement().hasChildNodes());
        Node data = output.getDocumentElement().getChildNodes().item(0);
        assertTrue(data.hasChildNodes());
        Node row = data.getChildNodes().item(0);
        assertTrue(row.hasChildNodes());

        NodeList cols = row.getChildNodes();
        assertEquals(4, cols.getLength());
        String id = cols.item(0).getTextContent();
        assertEquals("1", id);
        assertEquals("ID", cols.item(0).getLocalName());
        assertEquals(ns, cols.item(0).getNamespaceURI());

        String field1 = cols.item(1).getTextContent();
        assertEquals("testvalue", field1);
        assertEquals("FIELD1", cols.item(1).getLocalName());
        assertEquals(ns, cols.item(1).getNamespaceURI());

        String field2 = cols.item(2).getTextContent();
        assertEquals("20000101 12:30:45", field2);
        //assertEquals("20000101 18:30:45", field2);
        assertEquals("FIELD2", cols.item(2).getLocalName());
        assertEquals(ns, cols.item(2).getNamespaceURI());

        String field3 = cols.item(3).getTextContent();
        assertEquals("123,45", field3);
        assertEquals("FIELD3", cols.item(3).getLocalName());
        assertEquals(ns, cols.item(3).getNamespaceURI());
    }

    /**
     * @throws Exception
     *
     */
    public void testDHCallSelectExtendedDTE() throws Exception
    {
        String operation = "GVESB::TestSelectExtendedDTE";
        IDBOBuilder dboBuilder = this.dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, null, null);
        assertNotNull(result);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getInsert());
        assertEquals(1, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());
        Document output = (Document) result.getData();
        assertNotNull(output);
        assertTrue(output.getDocumentElement().hasChildNodes());
        Node data = output.getDocumentElement().getChildNodes().item(0);
        assertTrue(data.hasChildNodes());
        Node row = data.getChildNodes().item(0);
        assertTrue(row.hasChildNodes());

        NodeList cols = row.getChildNodes();
        assertEquals(4, cols.getLength());
        String id = cols.item(0).getTextContent();
        assertEquals("1", id);
        assertEquals("ID", cols.item(0).getNodeName());
        assertNull(cols.item(0).getNamespaceURI());

        String field1 = cols.item(1).getTextContent();
        assertEquals("testvalue", field1);
        assertEquals("FIELD1", cols.item(1).getNodeName());
        assertNull("", cols.item(1).getNamespaceURI());

        String field2 = cols.item(2).getTextContent();
        assertEquals("20000101 12:30:45", field2);
        //assertEquals("20000101 18:30:45", field2);
        assertEquals("FIELD2", cols.item(2).getNodeName());
        assertNull("", cols.item(2).getNamespaceURI());

        String field3 = cols.item(3).getTextContent();
        assertEquals("123,45", field3);
        assertEquals("FIELD3", cols.item(3).getNodeName());
        assertNull("", cols.item(3).getNamespaceURI());
    }

    /**
     * @throws Exception
     *
     */
    public void testDHCallThreadSelect() throws Exception
    {
        String operation = "GVESB::TestThreadSelect";
        IDBOBuilder dboBuilder = this.dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, null, null);
        assertNotNull(result);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getInsert());
        assertEquals(2, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());
        Document output = (Document) result.getData();
        assertNotNull(output);
        assertTrue(output.getDocumentElement().hasChildNodes());

        NodeList datas = output.getDocumentElement().getChildNodes();
        assertEquals(2, datas.getLength());
        for (int i = 0; i < datas.getLength(); i++) {
            Element data = (Element) datas.item(i);
            if ("0".equals(data.getAttribute("id"))){
                testData0(data);
            }
            else {
                testData1(data);
            }
        }
    }

    private void testData0(Node data) {
        assertTrue(data.hasChildNodes());
        Node row = data.getChildNodes().item(0);
        assertTrue(row.hasChildNodes());
        NodeList cols = row.getChildNodes();
        assertEquals(4, cols.getLength());
        String id = cols.item(0).getTextContent();
        assertEquals("1", id);
        String field1 = cols.item(1).getTextContent();
        assertEquals("testvalue", field1);
        String field2 = cols.item(2).getTextContent();
        assertEquals("20000101 12:30:45", field2);
        //assertEquals("20000101 18:30:45", field2);
        String field3 = cols.item(3).getTextContent();
        assertEquals("123,45", field3);
    }

    private void testData1(Node data) {
        assertTrue(data.hasChildNodes());
        Node row = data.getChildNodes().item(0);
        assertTrue(row.hasChildNodes());
        NodeList cols = row.getChildNodes();
        assertEquals(4, cols.getLength());
        String id = cols.item(0).getTextContent();
        assertEquals("1", id);
        String field3 = cols.item(1).getTextContent();
        assertEquals("123,45", field3);
        String field1 = cols.item(2).getTextContent();
        assertEquals("testvalue", field1);
        String field2 = cols.item(3).getTextContent();
        assertEquals("20000101 12:30:45", field2);
        //assertEquals("20000101 18:30:45", field2);
    }

    /**
     * @throws Exception
     *
     */
    public void testDHCallThreadSelectJSON() throws Exception
    {
        String operation = "GVESB::TestThreadSelectJSON";
        IDBOBuilder dboBuilder = this.dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, null, null);
        assertNotNull(result);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getInsert());
        assertEquals(2, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());

        System.out.println("testDHCallThreadSelectJSON: " + result.getData());

        String output = (String) result.getData();
        assertNotNull(output);
        JSONArray jsa = new JSONArray(output);
        assertTrue(jsa.length() > 0);

        assertEquals(2, jsa.length());
        for (int i = 0; i < jsa.length(); i++) {
            JSONObject data = jsa.getJSONObject(i);
            if ("0".equals(data.getString("dhId"))){
                testData0_json(data);
            }
            else {
                testData1_json(data);
            }
        }
    }

    private void testData0_json(JSONObject data) {
        assertEquals(4 + 1, JSONObject.getNames(data).length);
        int id = data.getInt("ID");
        assertEquals(1, id);
        String field1 = data.getString("FIELD1");
        assertEquals("testvalue", field1);
        String field2 = data.getString("FIELD2");
        assertEquals("2000-01-01 12:30:45.0", field2);
        //assertEquals("20000101 18:30:45", field2);
        double field3 = data.getDouble("FIELD3");
        assertEquals(123,45, field3);
    }

    private void testData1_json(JSONObject data) {
        assertEquals(4 + 1, JSONObject.getNames(data).length);
        int id = data.getInt("ID");
        assertEquals(1, id);
        String field1 = data.getString("FIELD1");
        assertEquals("testvalue", field1);
        String field2 = data.getString("FIELD2");
        assertEquals("2000-01-01 12:30:45.0", field2);
        //assertEquals("20000101 18:30:45", field2);
        double field3 = data.getDouble("FIELD3");
        assertEquals(123,45, field3);
    }

    /**
     * @throws Exception
     */
    public void testDHCallSelectMerge() throws Exception
    {
        String operation = "GVESB::TestSelectMerge";
        IDBOBuilder dboBuilder = this.dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, null, null);
        assertNotNull(result);
        Document output = (Document) result.getData();
        assertNotNull(output);
        System.out.println("TestSelectMerge: " + XMLUtils.serializeDOM_S(output));
    }

    /**
     * @throws Exception
     */
    public final void testDHCallInsertOrUpdate() throws Exception
    {
        String operation = "GVESB::TestInsert";
        IDBOBuilder dboBuilder = this.dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, Commons.createInsertMessage(), null);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getInsert());
        assertEquals(0, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());

        operation = "GVESB::TestInsertOrUpdate";
        dboBuilder = this.dhFactory.getDBOBuilder(operation);
        result = dboBuilder.EXECUTE(operation, Commons.createInsertOrUpdateMessage(), null);
        assertEquals(0, result.getDiscard());
        assertEquals(1, result.getUpdate());
        assertEquals(2, result.getTotal());
        assertEquals(1, result.getInsert());
        assertEquals(0, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());
    }

    /**
     * @throws Exception
     *
     */
    public void testDHCallFlatSelect() throws Exception
    {
        String operation = "GVESB::TestFlatSelect";
        IDBOBuilder dboBuilder = this.dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, null, null);
        assertNotNull(result);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getInsert());
        assertEquals(1, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());
        Object out = result.getData();
        assertNotNull(out);
        String output = out instanceof String ? (String) out : new String((byte[]) out);
        assertEquals("1@testvalue.....................@20000101 123045@123,45@\n", output);
        //assertEquals("1@testvalue.....................@20000101 183045@123,45@\n", output);
    }

    /**
     * @throws Exception
     *
     */
    public void testDHCallFlatTZoneSelect() throws Exception
    {
        String operation = "GVESB::TestFlatTZoneSelect";
        IDBOBuilder dboBuilder = this.dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, null, null);
        assertNotNull(result);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getInsert());
        assertEquals(1, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());
        Object out = result.getData();
        assertNotNull(out);
        String output = out instanceof String ? (String) out : new String((byte[]) out);
        assertEquals("1@testvalue.....................@20000101 113045@123,45@\n", output);
        //assertEquals("1@testvalue.....................@20000101 173045@123,45@\n", output);
    }

   /**
    * @throws Exception
    *
    */
   public void testDHCallFlatSelectFile() throws Exception
   {
       String operation = "GVESB::TestFlatSelectFile";
       IDBOBuilder dboBuilder = this.dhFactory.getDBOBuilder(operation);
       DHResult result = dboBuilder.EXECUTE(operation, null, null);
       assertNotNull(result);
       assertEquals(0, result.getDiscard());
       assertEquals(0, result.getUpdate());
       assertEquals(0, result.getTotal());
       assertEquals(0, result.getInsert());
       assertEquals(1, result.getRead());
       assertEquals("", result.getDiscardCauseListAsString());
       Object out = result.getData();
       assertNull(out);
       String output = TextUtils.readFile(PropertiesHandler.expand("sp{{gv.app.home}}/log/TestFlatSelectFile.csv"));
       assertEquals("1@testvalue.....................@20000101 123045@123,45@\n", output);
       //assertEquals("1@testvalue.....................@20000101 183045@123,45@\n", output);
   }

   /**
    * @throws Exception
    *
    */
   public void testDHCallMultiFlatSelectFile() throws Exception
   {
       String operation = "GVESB::TestMultiFlatSelectFile";
       IDBOBuilder dboBuilder = this.dhFactory.getDBOBuilder(operation);
       DHResult result = dboBuilder.EXECUTE(operation, null, null);
       assertNotNull(result);
       assertEquals(0, result.getDiscard());
       assertEquals(0, result.getUpdate());
       assertEquals(0, result.getTotal());
       assertEquals(0, result.getInsert());
       assertEquals(2, result.getRead());
       assertEquals("", result.getDiscardCauseListAsString());
       Object out = result.getData();
       assertNull(out);
       String output = TextUtils.readFile(PropertiesHandler.expand("sp{{gv.app.home}}/log/TestMultiFlatSelectFile.csv"));
       assertEquals("id@field1@field2@field3@\n1@testvalue.....................@20000101 123045@123,45@\n", output);
       //assertEquals("id@field1@field2@field3@\n1@testvalue.....................@20000101 183045@123,45@\n", output);
   }


   /**
    * @throws Exception
    *
    */
   public void testDHCallFlatTZoneSelectFile() throws Exception
   {
       String operation = "GVESB::TestFlatTZoneSelectFile";
       IDBOBuilder dboBuilder = this.dhFactory.getDBOBuilder(operation);
       DHResult result = dboBuilder.EXECUTE(operation, null, null);
       assertNotNull(result);
       assertEquals(0, result.getDiscard());
       assertEquals(0, result.getUpdate());
       assertEquals(0, result.getTotal());
       assertEquals(0, result.getInsert());
       assertEquals(1, result.getRead());
       assertEquals("", result.getDiscardCauseListAsString());
       Object out = result.getData();
       assertNull(out);
       String output = TextUtils.readFile(PropertiesHandler.expand("sp{{gv.app.home}}/log/TestFlatTZoneSelectFile.csv"));
       assertEquals("1@testvalue.....................@20000101 113045@123,45@\n", output);
       //assertEquals("1@testvalue.....................@20000101 173045@123,45@\n", output);
   }
}
