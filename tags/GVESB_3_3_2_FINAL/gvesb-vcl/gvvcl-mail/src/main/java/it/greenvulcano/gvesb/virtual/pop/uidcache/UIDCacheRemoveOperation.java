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
package it.greenvulcano.gvesb.virtual.pop.uidcache;

import java.util.StringTokenizer;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;

import javax.mail.Session;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;


/**
 * Remove message ids from UIDCache.
 *
 * @version 3.4.0 Jun 10, 2013
 * @author GreenVulcano Developer Team
 *
 *
 */
public class UIDCacheRemoveOperation implements CallOperation
{

    private static final Logger logger          = GVLogger.getLogger(UIDCacheRemoveOperation.class);

    /**
     * The configured operation key
     */
    protected OperationKey      key             = null;

    /**
     * Mail session JNDI name.
     */
    private String              jndiName        = null;

    /**
     * Invoked from <code>OperationFactory</code> when an <code>Operation</code>
     * needs initialization.<br>
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws InitializationException
    {
        JNDIHelper initialContext = null;
        try {
            jndiName = XMLConfig.get(node, "@jndi-name");
            logger.debug("JNDI name: " + jndiName);

            initialContext = new JNDIHelper(XMLConfig.getNode(node, "JNDIHelper"));
            logger.debug("Initial Context properties: " + initialContext);

            // only to check the configuration
            Session session = (Session) initialContext.lookup(jndiName);
        }
        catch (Exception exc) {
            logger.error("Error initializing UIDCacheRemove call operation", exc);
            throw new InitializationException("GVVCL_POP_CACHEREMOVER_INIT_ERROR", new String[][]{{"node", node.getLocalName()}},
                    exc);
        }
        finally {
            if (initialContext != null) {
                try {
                    initialContext.close();
                }
                catch (NamingException exc) {
                    logger.error("An error occurred while closing InitialContext.", exc);
                }
            }
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {

        if (gvBuffer == null) {
            return null;
        }
        try {
            UIDCache uidCache = UIDCacheManagerFactory.getInstance().getUIDCache(jndiName);
            String ids = gvBuffer.getProperty("MESSAGE_ID");
            StringTokenizer st = new StringTokenizer(ids, ",");
            while (st.hasMoreTokens()) {
                String id = st.nextToken().trim();
                if (uidCache.remove(id)) {
                    logger.debug("Removed message id[" + id + "] from UIDCache[" + jndiName + "]");
                }
                else {
                    logger.debug("NOT removed message id[" + id + "] from UIDCache[" + jndiName + "]");
                }
            }
            return gvBuffer;
        }
        catch (Exception e) {
            throw new CallException("GV_CALL_SERVICE_ERROR",
                    new String[][]{{"service", gvBuffer.getService()}, {"system", gvBuffer.getSystem()},
                            {"id", gvBuffer.getId().toString()}, {"message", e.getMessage()}}, e);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    public void cleanUp()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    public void destroy()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    public OperationKey getKey()
    {
        return key;
    }

    /**
     * Return the alias for the given service
     *
     * @param gvBuffer
     *        the input service data
     * @return the configured alias
     */
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

}
