package it.greenvulcano.gvesb.virtual.commons;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.util.Properties;

import javax.mail.Session;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class BaseMailOperation implements CallOperation {

    private static final Logger logger          = GVLogger.getLogger(BaseMailOperation.class);

    /**
     * The configured operation key
     */
    protected OperationKey      key             = null;
    
    /**
     * Mail session JNDI name.
     */
    protected String              jndiName        = null;

    /**
     * The protocol-specific default Mail server. This overrides the mail.host
     * property.
     */
    protected String              protocolHost    = null;

    /**
     * The protocol-specific default user name for connecting to the Mail
     * server. This overrides the mail.user property.
     */
    protected String              protocolUser    = null;

    protected boolean             dynamicServer   = false;
    protected Properties          serverProps     = null;
    protected boolean             performLogin    = false;

    /**
     * Preliminary initialization operations
     */
    protected Session preInit(Node node) throws InitializationException {
        JNDIHelper initialContext = null;
        try {
            jndiName = XMLConfig.get(node, "@jndi-name");
            if (jndiName != null) {
                logger.debug("JNDI name: " + jndiName);
            }

            protocolHost = XMLConfig.get(node, "@override-protocol-host");
            if (protocolHost != null) {
                logger.debug("Override protocol host: " + protocolHost);
            }

            protocolUser = XMLConfig.get(node, "@override-protocol-user");
            if (protocolUser != null) {
                logger.debug("Override protocol user: " + protocolUser);
            }

            Session session = null;
            if (jndiName != null) {
                initialContext = new JNDIHelper(XMLConfig.getNode(node, "JNDIHelper"));
                session = (Session) initialContext.lookup(jndiName);
            }

            NodeList nodeList = XMLConfig.getNodeList(node, "mail-properties/mail-property");
            if (nodeList != null && nodeList.getLength() > 0) {
                serverProps = new Properties();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node property = nodeList.item(i);
                    String name = XMLConfig.get(property, "@name");
                    String value = XMLConfig.get(property, "@value");
                    if (name.contains(".password")) {
                        performLogin = true;
                    }
                    if (!PropertiesHandler.isExpanded(value)) {
                        dynamicServer = true;
                    }
                    serverProps.setProperty(name, value);
                }
            }

            if ((protocolHost != null) || (protocolUser != null)) {
                if (serverProps == null) {
                    serverProps = session.getProperties();
                }
                if (protocolHost != null) {
                    serverProps.setProperty("mail.protocol.host", protocolHost);
                }
                if (protocolUser != null) {
                    serverProps.setProperty("mail.protocol.user", protocolUser);
                }
            }

            if (!dynamicServer) {
                if (serverProps != null) {
                    session = Session.getDefaultInstance(serverProps, null);
                }

                if (session == null) {
                    throw new InitializationException("GVVCL_MAIL_NO_SESSION", new String[][]{{"node", node.getLocalName()}});
                }
            }

            return session;
        }
        catch (Exception exc) {
            logger.error("Error initializing IMAP call operation", exc);
            throw new InitializationException("GVVCL_MAIL_INIT_ERROR", new String[][]{{"node", node.getLocalName()}},
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
