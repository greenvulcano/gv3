/*
 * Copyright (c) 2009-2014 GreenVulcano ESB Open Source Project. All rights
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
package tests.unit.util.json;

import java.util.HashSet;
import java.util.Set;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.w3c.dom.Node;

import it.greenvulcano.util.json.JSONUtils;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;

/**
 * JSONUtilsTestCase class
 *
 *
 * @version 3.5.0 29/ago/2014
 * @author GreenVulcano Developer Team
 */
public class JSONUtilsTestCase extends XMLTestCase
{

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }


    /**
     * Test the XML2JSON.
     *
     * @throws Exception
     */
    public void testXML2JSON() throws Exception
    {
        Set<String> forceElementsArray = new HashSet<String>();
        forceElementsArray.add("elemC");
        forceElementsArray.add("elemD");
        Set<String> forceStringValue = new HashSet<String>();
        forceStringValue.add("intStr");

        JSONObject json= JSONUtils.xmlToJson(TextUtils.readFileFromCP("testX2J.xml"), forceElementsArray, forceStringValue);
        //System.out.println("\nTestXml2Json: " + json);
        String outJSON = TextUtils.readFileFromCP("testJ2X.json");
        JSONAssert.assertEquals(outJSON, json, true);
    }

    public void testXML2JSON_forceStr() throws Exception
    {
    	Set<String> forceElementsArray = new HashSet<String>();
        forceElementsArray.add("elemC");
        forceElementsArray.add("elemD");
        Set<String> forceStringValue = new HashSet<String>();
        forceStringValue.add("*");

        JSONObject json= JSONUtils.xmlToJson(TextUtils.readFileFromCP("testX2J.xml"), forceElementsArray, forceStringValue);
        System.out.println("\nTestXml2Json_forceStr: " + json);
        String outJSON = TextUtils.readFileFromCP("testJ2X_forceStr.json");
        JSONAssert.assertEquals(outJSON, json, true);
    }

    public void testXML2JSON_2() throws Exception
    {
        Set<String> forceElementsArray = new HashSet<String>();
        forceElementsArray.add("driver");
        forceElementsArray.add("manager");
        forceElementsArray.add("supervisor");
        forceElementsArray.add("administrator");
        Set<String> forceStringValue = new HashSet<String>();
        forceStringValue.add("status");
        forceStringValue.add("code");
        forceStringValue.add("message");
        forceStringValue.add("token");
        forceStringValue.add("userId");
        forceStringValue.add("name");
        forceStringValue.add("surname");
        forceStringValue.add("nationalRegistration");
        forceStringValue.add("email");
        forceStringValue.add("telephone");
        forceStringValue.add("address");
        forceStringValue.add("town");
        forceStringValue.add("country");
        forceStringValue.add("zipCode");
        forceStringValue.add("company");
        forceStringValue.add("fleetId");
        forceStringValue.add("fleetName");
        forceStringValue.add("fleetCreationDate");
        forceStringValue.add("groupId");
        forceStringValue.add("groupName");

        JSONObject json= JSONUtils.xmlToJson(XMLUtils.parseDOM_S(TextUtils.readFileFromCP("testX2J_2.xml")), forceElementsArray, forceStringValue);
        //System.out.println("\nTestXml2Json_2: " + json);
        String outJSON = TextUtils.readFileFromCP("testJ2X_2.json");
        JSONAssert.assertEquals(outJSON, json, true);
    }

    /**
     * Test the XML2JSON BadgerFish.
     *
     * @throws Exception
     */
    public void testXML2JSON_BadgerFish() throws Exception
    {
        JSONObject json= JSONUtils.xmlToJson_BadgerFish(TextUtils.readFileFromCP("testX2J_xmlns.xml"));
        //System.out.println("\nTestXml2Json_BadgerFish: " + json);
        String outJSON = TextUtils.readFileFromCP("testJ2X_BadgerFish.json");
        JSONAssert.assertEquals(outJSON, json, true);
    }

    /**
     * Test the JSON2XML BadgerFish.
     *
     * @throws Exception
     */
    public void testJSON2XML_BadgerFish() throws Exception
    {
        Node xml= JSONUtils.jsonToXml_BadgerFish(TextUtils.readFileFromCP("testJ2X_BadgerFish.json"));
        String dom = XMLUtils.serializeDOM_S(xml);
        //System.out.println("\nTestJson2Xml_BadgerFishr: " + dom);
        String outXML = TextUtils.readFileFromCP("testX2J_xmlns.xml");
        assertXMLEqual("TestJson2Xml_BadgerFishr failed", outXML, dom);
    }

    /**
     * Test the XML2JSON with namespaces.
     *
     * @throws Exception
     */
    public void testXML2JSON_xmlns() throws Exception
    {
        JSONObject json= JSONUtils.xmlToJson(TextUtils.readFileFromCP("testX2J_xmlns.xml"));
        //System.out.println("\nTestXml2Json_xmlns: " + json);
        String outJSON = TextUtils.readFileFromCP("testJ2X_xmlns.json");
        JSONAssert.assertEquals(outJSON, json, true);
    }

    /**
     * Test the JSON2XML no attributes.
     *
     * @throws Exception
     */
    public void testJSON2XML_noattr() throws Exception
    {
        Node xml= JSONUtils.jsonToXml(TextUtils.readFileFromCP("testJ2X.json"));
        String dom = XMLUtils.serializeDOM_S(xml);
        //System.out.println("\nTestJson2Xml_noattr: " + dom);
        String outXML = TextUtils.readFileFromCP("testJ2X_noattr.xml");
        assertXMLEqual("TestJson2Xml_noattr failed", outXML, dom);
    }

    /**
     * Test the JSON2XML force attributes.
     *
     * @throws Exception
     */
    public void testJSON2XML() throws Exception
    {
        Set<String> forceAttributes = new HashSet<String>();
        forceAttributes.add("id");
        forceAttributes.add("string");
        forceAttributes.add("int");
        forceAttributes.add("intStr");
        forceAttributes.add("float");
        forceAttributes.add("boolean");
        forceAttributes.add("nullVal");

        Node xml= JSONUtils.jsonToXml(TextUtils.readFileFromCP("testJ2X.json"), forceAttributes);
        String dom = XMLUtils.serializeDOM_S(xml);
        //System.out.println("\nTestJson2Xml: " + dom);
        String outXML = TextUtils.readFileFromCP("testX2J.xml");
        assertXMLEqual("TestJson2Xml failed", outXML, dom);
    }

    /**
     * Test the JSON2XML array.
     *
     * @throws Exception
     */
    public void testJSON_arr2XML() throws Exception
    {
        JSONObject[] arr = new JSONObject[2];
        arr[0] = new JSONObject();
        arr[0].put("id", 0);
        arr[0].put("string", "000");
        arr[1] = new JSONObject();
        arr[1].put("id", 1);
        arr[1].put("string", "001");
        arr[1].append("value", "aaa");
        arr[1].append("value", "bbb");
        arr[1].append("value", "ccc");

        Node xml= JSONUtils.jsonToXml(arr, "container");
        String dom = XMLUtils.serializeDOM_S(xml);
        //System.out.println("\nTestJson_arr2Xml: " + dom);
        String outXML = TextUtils.readFileFromCP("testJ2X_arr.xml");
        assertXMLEqual("TestJson_arr2Xml failed", outXML, dom);
    }

    /**
     * Test the JSON2XML array.
     *
     * @throws Exception
     */
    public void testJSON_arr2XML_2() throws Exception
    {
    	String json = "[{\"id\": 0, \"string\": \"000\"},"
    			+ "{\"id\": 1, \"string\": \"001\", \"value\": [\"aaa\",\"bbb\",\"ccc\"]}]";

        Node xml= JSONUtils.jsonToXml(json, "container");
        String dom = XMLUtils.serializeDOM_S(xml);
        //System.out.println("\nTestJson_arr2Xml: " + dom);
        String outXML = TextUtils.readFileFromCP("testJ2X_arr.xml");
        assertXMLEqual("TestJson_arr2Xml failed", outXML, dom);
    }

    /**
     * Test the JSON2XML no root.
     *
     * @throws Exception
     */
    public void testJSON_noroot2XML() throws Exception
    {
        JSONObject json = new JSONObject();
        JSONObject obj = new JSONObject();
        obj.put("id", 0);
        obj.put("string", "000");
        json.put("objA", obj);

        obj = new JSONObject();
        obj.put("id", 1);
        obj.put("string", "001");
        obj.append("value", "aaa");
        obj.append("value", "bbb");
        obj.append("value", "ccc");
        json.put("objB", obj);

        Node xml= JSONUtils.jsonToXml(json);
        String dom = XMLUtils.serializeDOM_S(xml);
        //System.out.println("\nTestJson_noroot2Xml: " + dom);
        String outXML = TextUtils.readFileFromCP("testJ2X_noroot.xml");
        assertXMLEqual("TestJson_noroot2Xml failed", outXML, dom);
    }

    /**
     * Test the XML2JSON no root.
     *
     * @throws Exception
     */
    public void testXML2JSON_noroot() throws Exception
    {
        JSONObject outJSON = new JSONObject();
        JSONObject obj = new JSONObject();
        obj.put("id", 0);
        obj.put("string", "000");
        outJSON.put("objA", obj);

        obj = new JSONObject();
        obj.put("id", 1);
        obj.put("string", "001");
        obj.append("value", "aaa");
        obj.append("value", "bbb");
        obj.append("value", "ccc");
        outJSON.put("objB", obj);

        JSONObject json= JSONUtils.xmlToJson(TextUtils.readFileFromCP("testJ2X_noroot.xml"));
        //System.out.println("\ntestXML2JSON_noroot: " + json);
        JSONAssert.assertEquals(outJSON.toString(), json, true);
    }

    /**
     * Test the XML2JSON with empty array.
     *
     * @throws Exception
     */
    public void testXML2JSON_emptyArr() throws Exception
    {
    	Set<String> forceElementsArray = new HashSet<String>();
    	forceElementsArray.add("policyScores");

        JSONObject json= JSONUtils.xmlToJson(TextUtils.readFileFromCP("testX2J_emptyArr.xml"), forceElementsArray, new HashSet<String>());
        System.out.println("\nTestXml2Json_emptyArr: " + json);
        String outJSON = TextUtils.readFileFromCP("testJ2X_emptyArr.json");
        JSONAssert.assertEquals(outJSON, json, true);
    }

}
