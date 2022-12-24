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
package it.greenvulcano.gvesb.virtual.smtp;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.mail.smtp.SMTPTransport;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.gvesb.log.GVBufferDump;
import it.greenvulcano.gvesb.utils.MessageFormatter;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.txt.StringToHTML;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;

/**
 * Sends an email message.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class SMTPCallOperation implements CallOperation
{

    private static final Logger logger                     = GVLogger.getLogger(SMTPCallOperation.class);

    /**
     * The configured operation key
     */
    protected OperationKey      key                        = null;

    /**
     * Mail session JNDI name.
     */
    private String              jndiName                   = null;

    /**
     * The protocol-specific default Mail server. This overrides the mail.host
     * property.
     */
    private String              protocolHost               = null;

    /**
     * The protocol-specific default user name for connecting to the Mail
     * server. This overrides the mail.user property.
     */
    private String              protocolUser               = null;

    /**
     * The mail session.
     */
    private Session             session                    = null;

    /**
     * The sender display name.
     */
    private String              senderDisplayName          = null;

    /**
     * The sender address.
     */
    private String              senderAddress              = null;

    /**
     * The content type.
     */
    private String              contentType                = null;

    /**
     * The mail subject.
     */
    private String              subjectText                = null;

    /**
     * The list of address which the mail will be sent.
     */
    private String              to                         = "";

    /**
     * The list of address in carbon copy which the mail will be sent.
     */
    private String              cc                         = "";

    /**
     * The list of address in blind carbon copy which the mail will be sent.
     */
    private String              bcc                        = "";

    /**
     * If true the X-Priority header will be set
     */
    private boolean             isHighPriority             = false;

    /**
     * If true the mail body will contain the GVBuffer dump.
     */
    private boolean             gvBufferDump               = false;

    /**
     * If true, any HTML invalid char within GVBuffer fields will be escaped
     * before insertion into mail body.
     */
    private boolean             escapeHTMLInGVBufferFields = false;

    /**
     * The text of the mail message.
     */
    private String              messageText                = null;

    /**
     * The name of the attachment containing the GVBuffer message.
     */
    private String              gvBufferName               = null;

    /**
     * The list of file path that will be sent with the mail.
     */
    private List<String>        fileAttachments            = null;

    private String              messageIDProperty;
    private String              serverResponseProperty;

    private boolean             dynamicServer              = false;
    private Properties          serverProps                = null;
    private boolean             performLogin               = false;
    private String              loginUser                  = null;
    private String              loginPassword              = null;
    private String              serverHost                 = null;

    /**
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        JNDIHelper initialContext = null;
        try {
            this.jndiName = XMLConfig.get(node, "@jndi-name");
            if (this.jndiName != null) {
                logger.debug("JNDI name: " + this.jndiName);
            }

            this.protocolHost = XMLConfig.get(node, "@override-protocol-host");
            if (this.protocolHost != null) {
                logger.debug("Override protocol host: " + this.protocolHost);
            }

            this.protocolUser = XMLConfig.get(node, "@override-protocol-user");
            if (this.protocolUser != null) {
                logger.debug("Override protocol user: " + this.protocolUser);
            }

            this.messageIDProperty = XMLConfig.get(node, "@message-id-property", "messageID");
            logger.debug("Message ID property: " + this.messageIDProperty);
            this.serverResponseProperty = XMLConfig.get(node, "@server-response-property", "serverResponse");
            logger.debug("Server Response property: " + this.serverResponseProperty);

            if (this.jndiName != null) {
                initialContext = new JNDIHelper(XMLConfig.getNode(node, "JNDIHelper"));
                this.session = (Session) initialContext.lookup(this.jndiName);
            }

            NodeList nodeList = XMLConfig.getNodeList(node, "mail-properties/mail-property");
            if ((nodeList != null) && (nodeList.getLength() > 0)) {
                this.serverProps = new Properties();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node property = nodeList.item(i);
                    String name = XMLConfig.get(property, "@name");
                    String value = XMLConfig.get(property, "@value");
                    if (name.contains(".host")) {
                        logger.debug("Logging-in to host: " + value);
                        this.serverHost = value;
                    }
                    else if (name.contains(".user")) {
                        logger.debug("Logging-in as user: " + value);
                        this.loginUser = value;
                    }
                    else if (name.contains(".password")) {
                        value = XMLConfig.getDecrypted(value);
                        //logger.debug("Logging-in with password: " + value);
                        this.loginPassword = value;
                        this.performLogin = true;
                    }
                    if (!PropertiesHandler.isExpanded(value)) {
                        this.dynamicServer = true;
                    }
                    this.serverProps.setProperty(name, value);
                }
            }

            if ((this.protocolHost != null) || (this.protocolUser != null)) {
                if (this.serverProps == null) {
                    this.serverProps = this.session.getProperties();
                }
                if (this.protocolHost != null) {
                    this.serverProps.setProperty("mail.protocol.host", this.protocolHost);
                }
                if (this.protocolUser != null) {
                    this.serverProps.setProperty("mail.protocol.user", this.protocolUser);
                }
            }

            if (!this.dynamicServer) {
                if (this.serverProps != null) {
                    this.session = Session.getInstance(this.serverProps, null);
                }

                if (this.session == null) {
                    throw new InitializationException("GVVCL_SMTP_NO_SESSION", new String[][]{{"node", node.getLocalName()}});
                }
            }

            initMailProperties(XMLConfig.getNode(node, "mail-message"));
        }
        catch (Exception exc) {
            logger.error("Error initializing SMTP call operation", exc);
            throw new InitializationException("GVVCL_SMTP_INIT_ERROR", new String[][]{{"node", node.getLocalName()}},
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
     * Initializes the properties of the mail.
     *
     * @param node
     *        the configuration node containing the mail properties.
     * @throws XMLConfigException
     *         if an error occurs.
     */
    private void initMailProperties(Node node) throws XMLConfigException
    {
        this.senderDisplayName = XMLConfig.get(node, "@sender-display-name");
        logger.debug("Sender: " + this.senderDisplayName);
        this.senderAddress = XMLConfig.get(node, "@sender-address", "");
        if (!"".equals(this.senderAddress)) {
        	logger.debug("Sender address: " + this.senderAddress);
        }
        this.subjectText = XMLConfig.get(node, "@subject");
        logger.debug("Subject: " + this.subjectText);
        this.contentType = XMLConfig.get(node, "@content-type").replace('-', '/');
        logger.debug("Content type: " + this.contentType);

        Node destinations = XMLConfig.getNode(node, "destinations");
        if (destinations != null) {
            initDestinations(destinations);
        }

        Node msgBody = XMLConfig.getNode(node, "message-body");
        if (msgBody != null) {
            initMessageBody(msgBody);
        }

        Node attachments = XMLConfig.getNode(node, "attachments");
        if (attachments != null) {
            initAttachments(attachments);
        }

        this.isHighPriority = XMLConfig.getBoolean(node, "@high-priority", false);
        logger.debug("Is high priority: " + this.isHighPriority);
    }

    /**
     * Initializes the list of mail destinations.
     *
     * @param node
     *        the configuration node containing the destinations.
     * @throws XMLConfigException
     *         if an error occurs.
     */
    private void initDestinations(Node node) throws XMLConfigException
    {
        Node toNode = XMLConfig.getNode(node, "to");
        if (toNode != null) {
            NodeList list = XMLConfig.getNodeList(toNode, "mail-address");
            for (int i = 0; i < list.getLength(); i++) {
                this.to += XMLConfig.get(list.item(i), "@address") + ",";
            }
            logger.debug("To: " + this.to);
        }

        Node ccNode = XMLConfig.getNode(node, "cc");
        if (ccNode != null) {
            NodeList list = XMLConfig.getNodeList(ccNode, "mail-address");
            for (int i = 0; i < list.getLength(); i++) {
                this.cc += XMLConfig.get(list.item(i), "@address") + ",";
            }
            logger.debug("Cc: " + this.cc);
        }

        Node bccNode = XMLConfig.getNode(node, "bcc");
        if (bccNode != null) {
            NodeList list = XMLConfig.getNodeList(bccNode, "mail-address");
            for (int i = 0; i < list.getLength(); i++) {
                this.bcc += XMLConfig.get(list.item(i), "@address") + ",";
            }
            logger.debug("Bcc: " + this.bcc);
        }
    }

    /**
     * Initializes the message body of the mail.
     *
     * @param node
     *        the configuration node containing the message body properties.
     * @throws XMLConfigException
     *         if an error occurs.
     */
    private void initMessageBody(Node node) throws XMLConfigException
    {
        this.gvBufferDump = XMLConfig.getBoolean(node, "@gvBuffer-dump", false);
        logger.debug("GVBuffer Dump: " + this.gvBufferDump);

        Node text = XMLConfig.getNode(node, "message-text");

        if (text != null) {
            this.messageText = XMLConfig.get(text, "text()", "GreenVulcano Message");
            this.escapeHTMLInGVBufferFields = XMLConfig.getBoolean(text, "@escape-HTML-in-gvBuffer-fields", true);
        }
        else {
            this.messageText = "";
        }

        logger.debug("Text: " + this.messageText);
    }

    /**
     * Initializes the attachments of the mail.
     *
     * @param node
     *        the configuration node containing the attachments properties.
     * @throws XMLConfigException
     *         if an error occurs.
     */
    private void initAttachments(Node node) throws XMLConfigException
    {
        NodeList list = XMLConfig.getNodeList(node, "file-attachment");
        int numFiles = list.getLength();
        if (numFiles > 0) {
            this.fileAttachments = new ArrayList<String>();
            for (int i = 0; i < numFiles; i++) {
                this.fileAttachments.add(XMLConfig.get(list.item(i), "@path"));
            }
            logger.debug("Attach file: " + this.fileAttachments);
        }

        Node gvBufferNode = XMLConfig.getNode(node, "gvBuffer");
        if (gvBufferNode != null) {
            this.gvBufferName = XMLConfig.get(gvBufferNode, "@name");
            logger.debug("Attach gvBuffer: " + this.gvBufferName);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        if (gvBuffer == null) {
            return null;
        }
        try {
            sendMail(gvBuffer);
            return gvBuffer;
        }
        catch (Exception e) {
            throw new CallException("GV_CALL_SERVICE_ERROR",
                    new String[][]{{"service", gvBuffer.getService()}, {"system", gvBuffer.getSystem()},
                            {"id", gvBuffer.getId().toString()}, {"message", e.getMessage()}}, e);
        }
    }

    /**
     * Sends an email.
     *
     * @param gvBuffer
     *        the gvBuffer.
     *
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     * @throws GVException
     */
    private void sendMail(GVBuffer gvBuffer) throws Exception
    {
        try {
            PropertiesHandler.enableExceptionOnErrors();
            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);

            Session localSession = getSession(gvBuffer, params);
            SMTPTransport transport = null;

            try {
                transport = (SMTPTransport) localSession.getTransport();
                if (this.performLogin) {
                    transport.connect(this.serverHost, this.loginUser, this.loginPassword);
                }
                else {
                    transport.connect();
                }

                MimeMessage msg = new MimeMessage(localSession);
                if (this.isHighPriority) {
                    msg.addHeader("X-Priority", "1");
                }
                String localSA = localSession.getProperties().getProperty("mail.from");
        		if (!"".equals(this.senderAddress)) {
            		localSA = PropertiesHandler.expand(this.senderAddress, params, gvBuffer);
        		}
                msg.setFrom(new InternetAddress(localSA, PropertiesHandler.expand(this.senderDisplayName, params, gvBuffer)));

                String appoTO = gvBuffer.getProperty("GV_SMTP_TO");
                if ((appoTO == null) || "".equals(appoTO)) {
                    appoTO = this.to;
                }
                appoTO = PropertiesHandler.expand(appoTO, params, gvBuffer);
                if (!appoTO.equals("")) {
                    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(appoTO, false));
                    logger.debug("Send TO: " + appoTO);
                }

                String appoCC = gvBuffer.getProperty("GV_SMTP_CC");
                if ((appoCC == null) || "".equals(appoCC)) {
                    appoCC = this.cc;
                }
                appoCC = PropertiesHandler.expand(appoCC, params, gvBuffer);
                if (!appoCC.equals("")) {
                    msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(appoCC, false));
                    logger.debug("Send CC: " + appoCC);
                }

                String appoBCC = gvBuffer.getProperty("GV_SMTP_BCC");
                if ((appoBCC == null) || "".equals(appoBCC)) {
                    appoBCC = this.bcc;
                }
                appoBCC = PropertiesHandler.expand(appoBCC, params, gvBuffer);
                if (!appoBCC.equals("")) {
                    msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(appoBCC, false));
                    logger.debug("Send BCC: " + appoBCC);
                }

                String subject = parseMessage(this.subjectText, gvBuffer, params);
                logger.debug("Generated mail subject: " + subject);
                msg.setSubject(subject);

                MimeBodyPart messageBodyPart = new MimeBodyPart();

                String message = parseMessage(this.messageText, gvBuffer, params);
                logger.debug("Generated mail body: \n" + message);

                if (this.gvBufferDump) {
                    StringBuffer buf = new StringBuffer(message);
                    if (this.contentType.equals("text/html")) {
                        buf.append("<pre>");
                    }
                    buf.append(System.getProperty("line.separator"));

                    String appMsg = new GVBufferDump(gvBuffer, true).toString();

                    if (this.contentType.equals("text/html")) {
                        appMsg = StringToHTML.quote(appMsg);
                    }

                    buf.append(appMsg);

                    if (this.contentType.equals("text/html")) {
                        buf.append("</pre>");
                    }

                    message = buf.toString();
                }
                messageBodyPart.setContent(message, this.contentType);
                messageBodyPart.setContentLanguage(new String[]{Locale.getDefault().getLanguage()});

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);

                String appoBufferName = gvBuffer.getProperty("GV_SMTP_BUFFER_NAME");
                if ((appoBufferName == null) || "".equals(appoBufferName)) {
                    appoBufferName = this.gvBufferName;
                }

                if (appoBufferName != null) {
                    MimeBodyPart gvBufferPart = getAttachGVBuffer(parseMessage(appoBufferName, gvBuffer, params), gvBuffer.getObject());
                    multipart.addBodyPart(gvBufferPart);
                }

                List<String> appoFileAttachments = this.fileAttachments;
                String appoFiles = gvBuffer.getProperty("GV_SMTP_ATTACHMENTS");
                if ((appoFiles != null) && (!"".equals(appoFiles))) {
                    appoFileAttachments = TextUtils.splitByStringSeparator(appoFiles, ";");
                }
                if (appoFileAttachments != null) {
                    for (int i = 0; i < appoFileAttachments.size(); i++) {
                        String filePath = appoFileAttachments.get(i);
                        try {
                            filePath = PropertiesHandler.expand(filePath, params, gvBuffer);
                        }
                        catch (Exception exc) {
                            logger.warn("Cannot expand attachment file path [" + filePath + "]", exc);
                        }
                        MimeBodyPart filePart = getAttachFile(filePath);
                        multipart.addBodyPart(filePart);
                    }
                }

                msg.setContent(multipart);
                msg.setSentDate(new Date());

                msg.saveChanges();
                transport.sendMessage(msg, msg.getAllRecipients());
                String serverResponse = transport.getLastServerResponse();

                String messageID = msg.getMessageID();
                gvBuffer.setProperty(this.messageIDProperty, messageID);
                gvBuffer.setProperty(this.serverResponseProperty, serverResponse);
            }
            finally {
                if (transport != null) {
                    transport.close();
                }
            }
        }
        catch(Exception exc) {
            logger.error("Error preparing/sending email", exc);
            throw exc;
        }
        finally {
            PropertiesHandler.disableExceptionOnErrors();
        }
    }

    private Session getSession(GVBuffer data, Map<String, Object> params) throws Exception {
        this.loginUser     = null;
        this.loginPassword = null;
        this.serverHost    = null;

        if (!this.dynamicServer) {
            return this.session;
        }

        try {
            Properties localProps = new Properties();
            for (Object element : this.serverProps.keySet()) {
                String name = (String) element;
                String value = PropertiesHandler.expand(this.serverProps.getProperty(name), params, data);
                if (name.contains(".host")) {
                    logger.debug("Logging-in to host: " + value);
                    this.serverHost = value;
                }
                else if (name.contains(".user")) {
                    logger.debug("Logging-in as user: " + value);
                    this.loginUser = value;
                }
                else if (name.contains(".password")) {
                    value = XMLConfig.getDecrypted(value);
                    //logger.debug("Logging-in with password: " + value);
                    this.loginPassword = value;
                }
                localProps.setProperty(name, value);
            }

            Session session = Session.getInstance(localProps, null);

            if (session == null) {
                throw new CallException("GVVCL_SMTP_NO_SESSION", new String[][]{{"properties", "" + localProps}});
            }

            return session;
        }
        catch (CallException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new CallException("GVVCL_SMTP_SESSION_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
    }


    /**
     * Gets the MIME body part containing the file to be attached.
     *
     * @param filePath
     *        the path of the file to be attached.
     *
     * @return the MIME body part containing the file to be attached.
     *
     * @throws MessagingException
     *         if an error occurs.
     */
    private MimeBodyPart getAttachFile(String filePath) throws MessagingException
    {
        MimeBodyPart filePart = new MimeBodyPart();
        FileDataSource fds = new FileDataSource(filePath);
        filePart.setDataHandler(new DataHandler(fds));
        filePart.setFileName(fds.getName());

        return filePart;
    }

    /**
     * Gets the MIME body part containing the data to be attached.
     *
     * @param data
     *        the data to be attached.
     *
     * @return the MIME body part containing the data to be attached.
     *
     * @throws MessagingException
     *         if an error occurs.
     */
    private MimeBodyPart getAttachGVBuffer(String name, Object data) throws Exception
    {
        MimeBodyPart dataPart = new MimeBodyPart();
        //dataPart.setDataHandler(new DataHandler(data, "application/octet-stream"));
        byte[] payload = null;
        if (data instanceof byte[]) {
            payload = (byte[]) data;
        }
        else if (data instanceof String) {
            payload = ((String) data).getBytes();
        }
        else if (data instanceof Node) {
            payload = XMLUtils.serializeDOMToByteArray_S((Node) data);
        }
        else {
            throw new InvalidDataException("Invalid GVBuffer content: " + data.getClass().getName());
        }
        dataPart.setDataHandler(new DataHandler(new ByteArrayDataSource(payload, "application/octet-stream")));
        dataPart.setFileName(name);
        return dataPart;
    }

    /**
     * Parses the text message applying the substitutions.
     *
     * @param messageText
     *        the template message.
     * @param gvBuffer
     *        the input buffer
     * @return the message parsed.
     * @throws PropertiesHandlerException
     */
    private String parseMessage(String message, GVBuffer gvBuffer, Map<String, Object> params) throws PropertiesHandlerException
    {
        MessageFormatter messageFormatter = new MessageFormatter(PropertiesHandler.expand(message, params, gvBuffer),
                gvBuffer, this.escapeHTMLInGVBufferFields);
        return messageFormatter.toString();
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
        return this.key;
    }

    /**
     * Return the alias for the given service
     *
     * @param gvBuffer
     *        the input service data
     * @return the configured alias
     */
    @Override
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }
}
