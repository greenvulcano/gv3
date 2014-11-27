/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package tests.unit.ldap;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.ldap.LDAPSearchOperation;
import it.greenvulcano.gvesb.virtual.ldap.LDAPUpdateOperation;
import it.greenvulcano.gvesb.virtual.ldap.LDAPVclCommons;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.w3c.dom.Document;

/**
 * @version 3.4.0 Nov 25, 2013
 * @author GreenVulcano Developer Team
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LDAPvclTestCase extends XMLTestCase
{

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        XMLUnit.setIgnoreWhitespace(true);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        // do nothing
    }

    /**
     * @throws Exception
     * 
     */
    public void test01ContextCleanup() throws Exception
    {
        LDAPUpdateOperation op = new LDAPUpdateOperation();
        op.init(XMLConfig.getNode("GVCore.xml", "//ldap-update-call[@name='LDAPUpdatePeople']"));

        GVBuffer gb = new GVBuffer();
        gb.setObject(TextUtils.readFileFromCP("conf/LDAPCtx_Remove.xml"));
        gb = op.perform(gb);

        System.out.println("Removed: " + gb.getObject());
    }

    /**
     * @throws Exception
     * 
     */
    public void test02ContextAdd() throws Exception
    {
        LDAPUpdateOperation op = new LDAPUpdateOperation();
        op.init(XMLConfig.getNode("GVCore.xml", "//ldap-update-call[@name='LDAPAddPeople']"));

        GVBuffer gb = new GVBuffer();
        gb.setObject(TextUtils.readFileFromCP("conf/LDAPCtx_Add.xml"));
        gb = op.perform(gb);

        System.out.println("Added: " + gb.getObject());
    }
    
    /**
     * @throws Exception
     * 
     */
    public void test03SearchPeople() throws Exception
    {
        String expected = TextUtils.readFileFromCP("conf/LDAPSearchPeople.xml");

        LDAPSearchOperation op = new LDAPSearchOperation();
        //op.init(XMLConfig.getNode("GVCore.xml", "//ldap-search-call[@name='LDAPSearchGroups']"));
        op.init(XMLConfig.getNode("GVCore.xml", "//ldap-search-call[@name='LDAPSearchPeople']"));

        GVBuffer gb = new GVBuffer();
        gb.setProperty(LDAPVclCommons.GVLDAP_FILTER, "(&(objectclass=person)(sn=Di Maio))");
        gb = op.perform(gb);
        String res = XMLUtils.serializeDOM_S((Document) gb.getObject());

        System.out.println("Result: " + res);
        assertXMLEqual("SearchPeople Failed", expected, res);
    }


    /**
     * @throws Exception
     * 
     */
    public void test04ContextUpdate() throws Exception
    {
        LDAPUpdateOperation op = new LDAPUpdateOperation();
        op.init(XMLConfig.getNode("GVCore.xml", "//ldap-update-call[@name='LDAPUpdatePeople']"));

        GVBuffer gb = new GVBuffer();
        gb.setObject(TextUtils.readFileFromCP("conf/LDAPCtx_Update.xml"));
        gb = op.perform(gb);

        System.out.println("Updated: " + gb);
    }

    
    /**
     * @throws Exception
     * 
     */
    public void test05Login() throws Exception
    {
        String name = "cn=Gianluca Di Maio,ou=People,o=JNDITutorial,dc=example,dc=com";
        String password = "654321";
        
        String expected = TextUtils.readFileFromCP("conf/LDAPCtx_LoginSearch.xml");

        LDAPSearchOperation op = new LDAPSearchOperation();
        op.init(XMLConfig.getNode("GVCore.xml", "//ldap-search-call[@name='LDAPLogin']"));

        GVBuffer gb = new GVBuffer();
        gb.setProperty(LDAPVclCommons.GVLDAP_USER, name);
        gb.setProperty(LDAPVclCommons.GVLDAP_PASSWORD, password);
        gb = op.perform(gb);
        String res = XMLUtils.serializeDOM_S((Document) gb.getObject());

        System.out.println("Result: " + res);
        assertXMLEqual("Login Failed", expected, res);
    }
   
}
