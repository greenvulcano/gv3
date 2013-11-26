/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.virtual.ldap;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.ldap.LDAPContextXmlBuilder;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.Map;

import javax.naming.Context;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @version 3.2.0 25/09/2011
 * @author GreenVulcano Developer Team
 */
public class LDAPUpdateOperation implements CallOperation
{
    private static final Logger   logger      = GVLogger.getLogger(LDAPUpdateOperation.class);

    private JNDIHelper            ldapContext = null;
    private String                defUser     = null;
    private String                defPassword = null;
    private String                rootContext = null;

    private LDAPContextXmlBuilder builder     = new LDAPContextXmlBuilder();
    
    private OperationKey          key         = null;

    /**
     * Invoked from <code>OperationFactory</code> when an <code>Operation</code>
     * needs initialization.<br>
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node config) throws InitializationException
    {
        try {
            rootContext = XMLConfig.get(config, "@rootContext", "");

            ldapContext = new JNDIHelper(XMLConfig.getNode(config, "LDAPContext"));
            defUser = ldapContext.getProperty(Context.SECURITY_PRINCIPAL);
            defPassword = ldapContext.getProperty(Context.SECURITY_CREDENTIALS);
        }
        catch (Exception exc) {
            throw new InitializationException("GV_CONFIGURATION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        logger.debug("BEGIN perform(GVBuffer gvBuffer)");
        try {
            PropertiesHandler.enableExceptionOnErrors();
            String currUser = gvBuffer.getProperty(LDAPVclCommons.GVLDAP_USER);
            String currPassword = gvBuffer.getProperty(LDAPVclCommons.GVLDAP_PASSWORD);
            String currRootCtx = gvBuffer.getProperty(LDAPVclCommons.GVLDAP_ROOT_CONTEXT);

            if (currUser == null) {
                currUser = defUser;
            }
            if (currPassword == null) {
                currPassword = defPassword; 
            }
            currRootCtx = (currRootCtx != null) ? currRootCtx : rootContext;

            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            currUser = PropertiesHandler.expand(currUser, params, gvBuffer);
            currPassword = PropertiesHandler.expand(currPassword, params, gvBuffer);
            currRootCtx = PropertiesHandler.expand(currRootCtx, params, gvBuffer);

            logger.debug("Update parameters: user[" + currUser + "] password[*********] rootContext[" + currRootCtx
                    + "]");

            ldapContext.setProperty(Context.SECURITY_PRINCIPAL, currUser);
            ldapContext.setProperty(Context.SECURITY_CREDENTIALS, currPassword);

            LdapContext ctx = ldapContext.getInitialLdapContext();

            Document doc = (Document) XMLUtils.parseObject_S(gvBuffer.getObject(), false, true);

            builder.buildContext(doc, ctx, currRootCtx);
        }
        catch (Exception exc) {
            logger.error("ERROR perform(GVBuffer gvBuffer)", exc);
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
        }
        finally {
            PropertiesHandler.disableExceptionOnErrors();
            try {
                ldapContext.close();
            }
            catch (Exception exc) {
                logger.warn("Error while closing LdapContext", exc);
            }
            logger.debug("END perform(GVBuffer gvBuffer)");
        }
        return gvBuffer;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getServiceAlias(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    @Override
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    @Override
    public OperationKey getKey()
    {
        return key;
    }
}
