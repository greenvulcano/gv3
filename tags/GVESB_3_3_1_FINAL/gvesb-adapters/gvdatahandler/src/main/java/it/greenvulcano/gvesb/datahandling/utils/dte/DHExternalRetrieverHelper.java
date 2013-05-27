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
package it.greenvulcano.gvesb.datahandling.utils.dte;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.datahandling.utils.AbstractRetriever;
import it.greenvulcano.gvesb.gvdte.DTEException;
import it.greenvulcano.gvesb.gvdte.util.TransformerHelper;
import it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xpath.XPathFinder;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.0.0 10/lug/2010
 * @author GreenVulcano Developer Team
 */
public class DHExternalRetrieverHelper implements TransformerHelper
{
    private static final Logger logger = GVLogger.getLogger(DHExternalRetrieverHelper.class);
    private String              dhCallName;
    private String              jdbcConnectionName;
    private Node                dhNode;
    private Connection          conn   = null;

    /**
     * 
     * @see it.greenvulcano.gvesb.gvdte.util.TransformerHelper#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws DTEException
    {
        try {
            dhCallName = XMLConfig.get(node, "@dh-call-name");
            jdbcConnectionName = XMLConfig.get(node, "@jdbc-connection-name");
            dhNode = XMLConfig.getNode("GVCore.xml", "//dh-call[@name='" + dhCallName + "']");
            logger.debug("DHExternalRetrieverHelper[" + dhCallName + "] initialized from node: "
                    + XPathFinder.buildXPath(dhNode));
        }
        catch (Exception exc) {
            throw new DTEException("Error initializing DHExternalRetrieverHelper", exc);
        }
    }

    /**
     * 
     * @see it.greenvulcano.gvesb.gvdte.util.TransformerHelper#register()
     */
    @Override
    public void register() throws DTEException
    {
        try {
            conn = JDBCConnectionBuilder.getConnection(jdbcConnectionName);
            // Static utility classes initialization
            AbstractRetriever.setAllConnection(conn, dhNode);
        }
        catch (Exception exc) {
            if (conn != null) {
                try {
                    JDBCConnectionBuilder.releaseConnection(jdbcConnectionName, conn);
                }
                catch (Exception exc2) {
                    // TODO: handle exception
                }
                conn = null;
            }
            throw new DTEException("Error registering DHExternalRetrieverHelper[" + dhCallName + "]", exc);
        }
    }

    /**
     * 
     * @see it.greenvulcano.gvesb.gvdte.util.TransformerHelper#unregister()
     */
    @Override
    public void unregister() throws DTEException
    {
        AbstractRetriever.cleanupAll();
        if (conn != null) {
            try {
                JDBCConnectionBuilder.releaseConnection(jdbcConnectionName, conn);
            }
            catch (Exception exc2) {
                // TODO: handle exception
            }
            conn = null;
        }
    }
}
