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

import it.greenvulcano.util.json.JSONUtils;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.HashSet;
import java.util.Set;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.w3c.dom.Node;

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
        
        JSONObject json= JSONUtils.xmlToJson(TextUtils.readFileFromCP("testX2J.xml"), forceElementsArray);
        //System.out.println("\nTestXml2Json: " + json);
        String outJSON = TextUtils.readFileFromCP("testJ2X.json");
        JSONAssert.assertEquals(outJSON, json, true);
    }

    /**
     * Test the JSON2XMLTransformer.
     * 
     * @throws Exception
     */
    public void testJSON2XML_noattr() throws Exception
    {
        Node xml= JSONUtils.jsonToXml(TextUtils.readFileFromCP("testJ2X.json"));
        String dom = XMLUtils.serializeDOM_S((Node) xml);
        //System.out.println("\nTestJson2Xml_noattr: " + dom);
        String outXML = TextUtils.readFileFromCP("testJ2X_noattr.xml");
        assertXMLEqual("TestJson2Xml_noattr failed", outXML, dom);
    }
    
    /**
     * Test the JSON2XMLTransformer.
     * 
     * @throws Exception
     */
    public void testJSON2XML() throws Exception
    {
        Set<String> forceAttributes = new HashSet<String>();
        forceAttributes.add("id");
        forceAttributes.add("string");
        forceAttributes.add("int");
        forceAttributes.add("float");
        forceAttributes.add("boolean");
        forceAttributes.add("nullVal");

        Node xml= JSONUtils.jsonToXml(TextUtils.readFileFromCP("testJ2X.json"), forceAttributes);
        String dom = XMLUtils.serializeDOM_S((Node) xml);
        //System.out.println("\nTestJson2Xml: " + dom);
        String outXML = TextUtils.readFileFromCP("testX2J.xml");
        assertXMLEqual("TestJson2Xml failed", outXML, dom);
    }
    
    /**
     * Test the JSON2XMLTransformer.
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
        String dom = XMLUtils.serializeDOM_S((Node) xml);
        //System.out.println("\nTestJson_arr2Xml: " + dom);
        String outXML = TextUtils.readFileFromCP("testJ2X_arr.xml");
        assertXMLEqual("TestJson_arr2Xml failed", outXML, dom);
    }
}
