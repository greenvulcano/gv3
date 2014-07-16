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
package it.greenvulcano.gvesb.core.forward.preprocess.hl7;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.core.forward.JMSForwardException;
import it.greenvulcano.gvesb.core.forward.preprocess.Validator;
import it.greenvulcano.gvesb.gvhl7.utils.HL7Connection;
import it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @version 3.4.0 07/gen/2014
 * @author GreenVulcano Developer Team
 *
 */
public class HL7ConnectionValidator implements Validator
{
    private String name;
    private List<HL7Connection> urls = new ArrayList<HL7Connection>();
    private HL7ConnectionValidatorHolder cHolder = HL7ConnectionValidatorHolder.instance();

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.core.forward.preprocess.Validator#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws JMSForwardException {
        try {
            name = XMLConfig.get(node, "@name");

            Set<HL7Connection> locUrls = new HashSet<HL7Connection>(); 
            processHL7Server(node, locUrls);
            processHL7Call(node, locUrls);
            processDBSelect(node, locUrls);

            urls.addAll(locUrls);
        }
        catch (Exception exc) {
            throw new JMSForwardException("Error initializing HL7ConnectionValidator", exc);
        }
    }

    /**
     * @param node
     * @throws XMLConfigException
     */
    private void processHL7Server(Node node, Set<HL7Connection> locUrls) throws XMLConfigException {
        NodeList nl = XMLConfig.getNodeList(node, "HL7Server");
        if (nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                locUrls.add(new HL7Connection(XMLConfig.get(n, "@host"), XMLConfig.getInteger(n, "@port")));
            }
        }
    }
    
    /**
     * @param node
     * @throws XMLConfigException
     */
    private void processHL7Call(Node node, Set<HL7Connection> locUrls) throws XMLConfigException, JMSForwardException {
        NodeList nl = XMLConfig.getNodeList(node, "hl7-call-ref");
        if (nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                String sys = XMLConfig.get(n, "@id-system");
                String chn = XMLConfig.get(n, "@id-channel");
                String ope = XMLConfig.get(n, "@operation");
                Node opn = XMLConfig.getNode("GVSystems.xml", "/GVSystems/Systems/System[@id-system='" + sys + "']/Channel[@id-channel='" + chn + "']/*[@type='call' and @name='" + ope + "']");
                if (opn == null) {
                    throw new JMSForwardException("HL7 operation not found: [" + sys + "/" + chn + "/" + ope);
                }
                locUrls.add(new HL7Connection(XMLConfig.get(opn, "@host"), XMLConfig.getInteger(opn, "@port")));
            }
        }
    }
    
    /**
     * @param node
     * @throws XMLConfigException
     */
    private void processDBSelect(Node node, Set<HL7Connection> locUrls) throws XMLConfigException, JMSForwardException {
        Node dbs = XMLConfig.getNode(node, "DBSelect");
        if (dbs != null) {
            try {
                String connName = XMLConfig.get(dbs, "@jdbc-connection-name");
                String select = PropertiesHandler.expand(XMLConfig.get(dbs, "statement"));
                Connection conn = null;
                Statement stm = null;
                ResultSet rs = null;
                try {
                    conn = JDBCConnectionBuilder.getConnection(connName);

                    stm = conn.createStatement();
                    rs = stm.executeQuery(select);
                    while (rs.next()) {
                        locUrls.add(new HL7Connection(rs.getString(1), rs.getInt(2)));
                    }
                }
                finally {
                    if (rs != null) {
                        rs.close();
                    }
                    if (stm != null) {
                        stm.close();
                    }
                    JDBCConnectionBuilder.releaseConnection(connName, conn);
                }
            }
            catch (Exception exc) {
                throw new JMSForwardException("Error extracting HL7 connection data from DB", exc);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.core.forward.preprocess.Validator#isValid()
     */
    @Override
    public boolean isValid() throws JMSForwardException {
        return cHolder.isValid(name, urls);
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.core.forward.preprocess.Validator#reset()
     */
    @Override
    public void reset() {
        cHolder.reset(name, urls);
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.core.forward.preprocess.Validator#destroy()
     */
    @Override
    public void destroy() {
        cHolder.reset(name, urls);
        urls.clear();
    }
    
    @Override
    public String toString() {
        return "HL7ConnectionValidator[" + name + "]: " + urls;
    }
}
