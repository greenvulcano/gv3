/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.greenvulcano.util.ldap;

import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xpath.search.XPathAPI;

import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapContext;

import org.apache.axiom.om.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility class for LDAP context building from XML.
 *
 * @version 3.2.0 25/set/2011
 * @author GreenVulcano Developer Team
 */
public class LDAPContextXmlBuilder
{
    static {
        XPathAPI.installNamespace(LDAPCommons.LDAP_NS_PRE, LDAPCommons.LDAP_NS_URI);
    }

    public LDAPContextXmlBuilder()
    {
        // do nothing
    }

    public void buildContext(Document doc, LdapContext ctx, String root) throws LDAPUtilsException
    {
        XMLUtils parser = null;
        try {
            parser = XMLUtils.getParserInstance();

            LdapContext base = (LdapContext) ctx.lookup(root);
            NodeList entryL = parser.selectNodeList(doc, "/ldapc:LDAPContext/ldapc:Entry");
            for (int i = 0; i < entryL.getLength(); i++) {
                Node entryN = entryL.item(i);

                processEntry(entryN, base, parser);
            }
        }
        catch (Exception exc) {
            throw new LDAPUtilsException(exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }

    private void processEntry(Node entryN, LdapContext base, XMLUtils parser) throws LDAPUtilsException
    {
        try {
            String id = parser.get(entryN, "@id");
            LdapContext entry = null;
            try {
                entry = (LdapContext) base.lookup(id);
                processAttributes(entryN, entry, parser);
            }
            catch (Exception exc) {
                entry = createEntryAndAttributes(entryN, base, parser);
            }

            NodeList entryL = parser.selectNodeList(entryN, "ldapc:Entry");
            for (int i = 0; i < entryL.getLength(); i++) {
                Node subEntryN = entryL.item(i);

                processEntry(subEntryN, entry, parser);
            }
        }
        catch (LDAPUtilsException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new LDAPUtilsException(exc);
        }
    }

    private void processAttributes(Node entryN, LdapContext entry, XMLUtils parser) throws LDAPUtilsException
    {
        try  {
            List<ModificationItem> miL = new ArrayList<ModificationItem>();

            NodeList attrRemL = parser.selectNodeList(entryN, "ldapc:AttributeList/ldapc:Attribute[@mode='remove']");
            if (attrRemL.getLength() > 0) {
                for (int i = 0; i < attrRemL.getLength(); i++) {
                    Node attrN = attrRemL.item(i);
                    String id = parser.get(attrN, "@id");
                    String encoding = parser.get(attrN, "@encoding", "string");
                    BasicAttribute ba = new BasicAttribute(id);

                    NodeList attrVL = parser.selectNodeList(attrN, "ldapc:Value");
                    for (int v = 0; v < attrVL.getLength(); v++) {
                        Node valN = attrVL.item(v);
                        String val = parser.get(valN, ".", "");
                        if ("base64".equals(encoding)) {
                            ba.add(Base64.decode(val));
                        }
                        else {
                            ba.add(val);
                        }
                    }

                    miL.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, ba));
                }
            }

            NodeList attrRepL = parser.selectNodeList(entryN, "ldapc:AttributeList/ldapc:Attribute[@mode='replace']");
            if (attrRepL.getLength() > 0) {
                for (int i = 0; i < attrRepL.getLength(); i++) {
                    Node attrN = attrRepL.item(i);
                    String id = parser.get(attrN, "@id");
                    String encoding = parser.get(attrN, "@encoding", "string");
                    BasicAttribute ba = new BasicAttribute(id);

                    NodeList attrVL = parser.selectNodeList(attrN, "ldapc:Value");
                    for (int v = 0; v < attrVL.getLength(); v++) {
                        Node valN = attrVL.item(v);
                        String val = parser.get(valN, ".");
                        if ("base64".equals(encoding)) {
                            ba.add(Base64.decode(val));
                        }
                        else {
                            ba.add(val);
                        }
                    }

                    miL.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, ba));
                }
            }

            NodeList attrAddL = parser.selectNodeList(entryN, "ldapc:AttributeList/ldapc:Attribute[not(@mode) or @mode='add']");
            if (attrAddL.getLength() > 0) {
                for (int i = 0; i < attrAddL.getLength(); i++) {
                    Node attrN = attrAddL.item(i);
                    String id = parser.get(attrN, "@id");
                    String encoding = parser.get(attrN, "@encoding", "string");
                    BasicAttribute ba = new BasicAttribute(id);

                    NodeList attrVL = parser.selectNodeList(attrN, "ldapc:Value");
                    for (int v = 0; v < attrVL.getLength(); v++) {
                        Node valN = attrVL.item(v);
                        String val = parser.get(valN, ".");
                        if ("base64".equals(encoding)) {
                            ba.add(Base64.decode(val));
                        }
                        else {
                            ba.add(val);
                        }
                    }

                    miL.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, ba));
                }
            }

            if (miL.size() > 0) {
                ModificationItem[] miA = miL.toArray(new ModificationItem[0]);
                entry.modifyAttributes("", miA);
            }
        }
        catch (Exception exc) {
            throw new LDAPUtilsException(exc);
        }
    }

    private LdapContext createEntryAndAttributes(Node entryN, LdapContext base, XMLUtils parser) throws LDAPUtilsException
    {
        try  {
            BasicAttributes bas = new BasicAttributes();

            NodeList attrAddL = parser.selectNodeList(entryN, "ldapc:AttributeList/ldapc:Attribute[not(@mode) or @mode='add' or mode='replace']");
            if (attrAddL.getLength() > 0) {
                for (int i = 0; i < attrAddL.getLength(); i++) {
                    Node attrN = attrAddL.item(i);
                    String id = parser.get(attrN, "@id");
                    String encoding = parser.get(attrN, "@encoding", "string");
                    BasicAttribute ba = new BasicAttribute(id);

                    NodeList attrVL = parser.selectNodeList(attrN, "ldapc:Value");
                    for (int v = 0; v < attrVL.getLength(); v++) {
                        Node valN = attrVL.item(v);
                        String val = parser.get(valN, ".");
                        if ("base64".equals(encoding)) {
                            ba.add(Base64.decode(val));
                        }
                        else {
                            ba.add(val);
                        }
                    }

                    bas.put(ba);
                }
            }

            LdapContext entry = (LdapContext) base.createSubcontext(parser.get(entryN, "@id"), bas);

            return entry;
        }
        catch (Exception exc) {
            throw new LDAPUtilsException(exc);
        }
    }
}
