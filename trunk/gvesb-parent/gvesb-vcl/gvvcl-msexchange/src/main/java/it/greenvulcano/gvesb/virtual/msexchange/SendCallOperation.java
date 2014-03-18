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
package it.greenvulcano.gvesb.virtual.msexchange;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
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
import it.greenvulcano.util.txt.StringToHTML;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.activation.FileDataSource;
import javax.mail.MessagingException;

import microsoft.exchange.webservices.data.BodyType;
import microsoft.exchange.webservices.data.EmailAddressCollection;
import microsoft.exchange.webservices.data.EmailMessage;
import microsoft.exchange.webservices.data.ExchangeCredentials;
import microsoft.exchange.webservices.data.ExchangeService;
import microsoft.exchange.webservices.data.FileAttachment;
import microsoft.exchange.webservices.data.Importance;
import microsoft.exchange.webservices.data.MessageBody;
import microsoft.exchange.webservices.data.ServiceLocalException;
import microsoft.exchange.webservices.data.WebCredentials;
import microsoft.exchange.webservices.data.WellKnownFolderName;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Send emails to MS Exchange server.
 * 
 * @version 3.3.0 Oct 6, 2012
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class SendCallOperation implements CallOperation
{

    private static final Logger logger                     = GVLogger.getLogger(SendCallOperation.class);

    /**
     * The configured operation key
     */
    protected OperationKey      key                        = null;

    /**
     * Account user name.
     */
    private String              userName                   = null;
    /**
     * Account password.
     */
    private String              password                   = null;
    /**
     * Account domain.
     */
    private String              domain                     = null;
    private int                 timeout                    = 60000;

    private String              exchangeURL                = null;

    private ExchangeService     service                    = null;

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
    private boolean             saveCopy                   = true;

    /**
     * 
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            userName = XMLConfig.get(node, "@userName");
            password = XMLConfig.get(node, "@password");
            domain = XMLConfig.get(node, "@domain", null);
            exchangeURL = XMLConfig.get(node, "@exchangeURL");

            timeout = XMLConfig.getInteger(node, "@timeout", timeout);

            messageIDProperty = XMLConfig.get(node, "@message-id-property", "messageID");
            saveCopy = XMLConfig.getBoolean(node, "@save-copy", true);
            logger.debug("Save message copy: " + saveCopy + " - Message ID property: " + messageIDProperty);

            service = new ExchangeService();
            service.setTimeout(timeout);
            ExchangeCredentials credentials = (domain == null)
                    ? new WebCredentials(userName, password)
                    : new WebCredentials(userName, password, domain);
            service.setCredentials(credentials);
            service.setUrl(new URI(exchangeURL));

            initMailProperties(XMLConfig.getNode(node, "mail-message"));
        }
        catch (Exception exc) {
            logger.error("Error initializing SMTP call operation", exc);
            throw new InitializationException("GVVCL_MSEX_SEND_INIT_ERROR",
                    new String[][]{{"node", node.getLocalName()}}, exc);
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
        subjectText = XMLConfig.get(node, "@subject");
        logger.debug("Subject: " + subjectText);
        contentType = XMLConfig.get(node, "@content-type").replace('-', '/');
        logger.debug("Content type: " + contentType);

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

        isHighPriority = XMLConfig.getBoolean(node, "@high-priority", false);
        logger.debug("Is high priority: " + isHighPriority);
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
                to += XMLConfig.get(list.item(i), "@address");
                if (i < (list.getLength() - 1)) {
                    to += ",";
                }
            }
            logger.debug("To: " + to);
        }

        Node ccNode = XMLConfig.getNode(node, "cc");
        if (ccNode != null) {
            NodeList list = XMLConfig.getNodeList(ccNode, "mail-address");
            for (int i = 0; i < list.getLength(); i++) {
                cc += XMLConfig.get(list.item(i), "@address");
                if (i < (list.getLength() - 1)) {
                    cc += ",";
                }
            }
            logger.debug("Cc: " + cc);
        }

        Node bccNode = XMLConfig.getNode(node, "bcc");
        if (bccNode != null) {
            NodeList list = XMLConfig.getNodeList(bccNode, "mail-address");
            for (int i = 0; i < list.getLength(); i++) {
                bcc += XMLConfig.get(list.item(i), "@address");
                if (i < (list.getLength() - 1)) {
                    bcc += ",";
                }
            }
            logger.debug("Bcc: " + bcc);
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
        gvBufferDump = XMLConfig.getBoolean(node, "@gvBuffer-dump", false);
        logger.debug("GVBuffer Dump: " + gvBufferDump);

        Node text = XMLConfig.getNode(node, "message-text");

        if (text != null) {
            messageText = XMLConfig.get(text, "text()", "GreenVulcano Message");
            escapeHTMLInGVBufferFields = XMLConfig.getBoolean(text, "@escape-HTML-in-gvBuffer-fields", true);
        }
        else {
            messageText = "";
        }

        logger.debug("Text: " + messageText);
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
            fileAttachments = new ArrayList<String>();
            for (int i = 0; i < numFiles; i++) {
                fileAttachments.add(XMLConfig.get(list.item(i), "@path"));
            }
            logger.debug("Attach file: " + fileAttachments);
        }

        Node gvBufferNode = XMLConfig.getNode(node, "gvBuffer");
        if (gvBufferNode != null) {
            gvBufferName = XMLConfig.get(gvBufferNode, "@name");
            logger.debug("Attach gvBuffer: " + gvBufferName);
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
        EmailMessage msg = new EmailMessage(service);

        if (isHighPriority) {
            msg.setImportance(Importance.High);
        }

        String appoTO = gvBuffer.getProperty("GV_SMTP_TO");
        if ((appoTO == null) || "".equals(appoTO)) {
            appoTO = to;
        }
        if (!appoTO.equals("")) {
            EmailAddressCollection eac = msg.getToRecipients();
            String[] atos = appoTO.split(",");
            for (String ato : atos) {
                eac.add(ato);
            }
            logger.debug("Send TO: " + appoTO);
        }

        String appoCC = gvBuffer.getProperty("GV_SMTP_CC");
        if ((appoCC == null) || "".equals(appoCC)) {
            appoCC = cc;
        }
        if (!appoCC.equals("")) {
            EmailAddressCollection eac = msg.getCcRecipients();
            String[] accs = appoCC.split(",");
            for (String acc : accs) {
                eac.add(acc);
            }
            logger.debug("Send CC: " + appoCC);
        }

        String appoBCC = gvBuffer.getProperty("GV_SMTP_BCC");
        if ((appoBCC == null) || "".equals(appoBCC)) {
            appoBCC = bcc;
        }
        if (!appoBCC.equals("")) {
            EmailAddressCollection eac = msg.getBccRecipients();
            String[] abccs = appoBCC.split(",");
            for (String abcc : abccs) {
                eac.add(abcc);
            }

            logger.debug("Send BCC: " + appoBCC);
        }

        String subject = null;
        if (subjectText.indexOf("${") != -1) {
            subject = parseMessage(subjectText, gvBuffer);
        }
        else {
            subject = subjectText;
        }
        logger.debug("Generated mail subject: " + subject);
        msg.setSubject(subject);

        String message = "";
        if (messageText != null) {
            if (messageText.indexOf("${") != -1) {
                message = parseMessage(messageText, gvBuffer);
            }
            else {
                message = messageText;
            }
        }
        logger.debug("Generated mail body: \n" + message);

        if (gvBufferDump) {
            StringBuffer buf = new StringBuffer(message);
            if (contentType.equals("text/html")) {
                buf.append("<pre>");
            }
            buf.append(System.getProperty("line.separator"));

            String appMsg = new GVBufferDump(gvBuffer, true).toString();

            if (contentType.equals("text/html")) {
                appMsg = StringToHTML.quote(appMsg);
            }

            buf.append(appMsg);

            if (contentType.equals("text/html")) {
                buf.append("</pre>");
            }

            message = buf.toString();
        }

        MessageBody body = new MessageBody();
        if (contentType.equals("text/html")) {
            body.setBodyType(BodyType.HTML);
            body.setText(message);
        }
        else {
            body.setBodyType(BodyType.Text);
            body.setText(message);
        }
        msg.setBody(body);

        String appoBufferName = gvBuffer.getProperty("GV_SMTP_BUFFER_NAME");
        if ((appoBufferName == null) || "".equals(appoBufferName)) {
            appoBufferName = gvBufferName;
        }

        if (appoBufferName != null) {
            addAttachGVBuffer(msg, parseMessage(appoBufferName, gvBuffer), gvBuffer.getObject());
        }

        List<String> appoFileAttachments = fileAttachments;
        String appoFiles = gvBuffer.getProperty("GV_SMTP_ATTACHMENTS");
        if ((appoFiles != null) && (!"".equals(appoFiles))) {
            appoFileAttachments = TextUtils.splitByStringSeparator(appoFiles, ";");
        }
        if (appoFileAttachments != null) {
            for (int i = 0; i < appoFileAttachments.size(); i++) {
                addAttachFile(msg, appoFileAttachments.get(i), gvBuffer);
            }
        }

        if (saveCopy) {
            msg.sendAndSaveCopy(WellKnownFolderName.SentItems);
        }
        else {
            msg.send();
        }
        //String messageID = msg.getInternetMessageId();
        //gvBuffer.setProperty(messageIDProperty, messageID);
    }

    /**
     * Gets the MIME body part containing the file to be attached.
     * 
     * @param filePath
     *        the path of the file to be attached.
     * @param gvBuffer
     * 
     * @return the MIME body part containing the file to be attached.
     * 
     * @throws IOException
     * @throws ServiceLocalException
     */
    private void addAttachFile(EmailMessage msg, String filePath, GVBuffer gvBuffer) throws ServiceLocalException,
            IOException
    {
        try {
            filePath = PropertiesHandler.expand(filePath, null, gvBuffer);
        }
        catch (Exception exc) {
            logger.warn("Cannot expand attachment file path", exc);
        }
        FileDataSource fds = new FileDataSource(filePath);

        FileAttachment atch = msg.getAttachments().addFileAttachment(fds.getName(), fds.getInputStream());
        atch.setContentType(fds.getContentType());
    }

    /**
     * Gets the MIME body part containing the data to be attached.
     * 
     * @param data
     *        the data to be attached.
     * 
     * @throws MessagingException
     *         if an error occurs.
     */
    private void addAttachGVBuffer(EmailMessage msg, String name, Object data) throws Exception
    {
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
        FileAttachment atch = msg.getAttachments().addFileAttachment(name, payload);
        atch.setContentType("application/octet-stream");
    }

    /**
     * Parses the text message applying the substitutions.
     * 
     * @param messageText
     *        the template message.
     * @param gvBuffer
     *        the input buffer
     * @return the message parsed.
     */
    private String parseMessage(String message, GVBuffer gvBuffer)
    {
        MessageFormatter messageFormatter = new MessageFormatter(message, gvBuffer, escapeHTMLInGVBufferFields);
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
        return key;
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
