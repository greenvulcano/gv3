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
package it.greenvulcano.gvesb.virtual.postmark;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wildbit.java.postmark.Postmark;
import com.wildbit.java.postmark.client.ApiClient;
import com.wildbit.java.postmark.client.data.model.message.Message;
import com.wildbit.java.postmark.client.data.model.message.MessageResponse;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
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
 * @version 3.4.0 Nov 25, 2022
 * @author GreenVulcano Developer Team
 *
 */
public class PostmarkSendCallOperation implements CallOperation
{

    private static final Logger logger                     = GVLogger.getLogger(PostmarkSendCallOperation.class);

    /**
     * The configured operation key
     */
    protected OperationKey      key                        = null;

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

    private String              serverToken                 = null;
    private ApiClient           client                      = null;
    private boolean             debugMode                   = false;

    /**
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            this.serverToken = XMLConfig.get(node, "@server-token");

            this.messageIDProperty = XMLConfig.get(node, "@message-id-property", "messageID");
            logger.debug("Message ID property: " + this.messageIDProperty);
            this.serverResponseProperty = XMLConfig.get(node, "@server-response-property", "serverResponse");
            logger.debug("Server Response property: " + this.serverResponseProperty);
            this.debugMode = XMLConfig.getBoolean(node, "@debug-mode", false);
            logger.debug("Debug mode: " + this.debugMode);

            this.client = Postmark.getApiClient(this.serverToken);
            if (this.debugMode) {
            	this.client.setDebugMode();
            }

            initMailProperties(XMLConfig.getNode(node, "mail-message"));
        }
        catch (Exception exc) {
            logger.error("Error initializing PostmarkSend call operation", exc);
            throw new InitializationException("GVVCL_POSTMARK_INIT_ERROR", new String[][]{{"node", node.getLocalName()}},
                    exc);
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
    private void initMailProperties(Node node) throws Exception
    {
        this.senderDisplayName = XMLConfig.get(node, "@sender-display-name");
        logger.debug("Sender: " + this.senderDisplayName);
        this.senderAddress = XMLConfig.get(node, "@sender-address");
    	logger.debug("Sender address: " + this.senderAddress);
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

            Message msg = new Message();

            if (this.isHighPriority) {
                msg.addHeader("X-Priority", "1");
            }
            msg.setFrom(PropertiesHandler.expand(this.senderDisplayName, params, gvBuffer),
            		    PropertiesHandler.expand(this.senderAddress, params, gvBuffer));

            String appoTO = gvBuffer.getProperty("GV_SMTP_TO");
            if ((appoTO == null) || "".equals(appoTO)) {
                appoTO = this.to;
            }
            appoTO = PropertiesHandler.expand(appoTO, params, gvBuffer);
            if (!appoTO.equals("")) {
            	msg.setTo(Arrays.asList(appoTO.split(",")));
                logger.debug("Send TO: " + appoTO);
            }

            String appoCC = gvBuffer.getProperty("GV_SMTP_CC");
            if ((appoCC == null) || "".equals(appoCC)) {
                appoCC = this.cc;
            }
            appoCC = PropertiesHandler.expand(appoCC, params, gvBuffer);
            if (!appoCC.equals("")) {
            	msg.setCc(Arrays.asList(appoCC.split(",")));
                logger.debug("Send CC: " + appoCC);
            }

            String appoBCC = gvBuffer.getProperty("GV_SMTP_BCC");
            if ((appoBCC == null) || "".equals(appoBCC)) {
                appoBCC = this.bcc;
            }
            appoBCC = PropertiesHandler.expand(appoBCC, params, gvBuffer);
            if (!appoBCC.equals("")) {
            	msg.setBcc(Arrays.asList(appoBCC.split(",")));
                logger.debug("Send BCC: " + appoBCC);
            }

            String subject = parseMessage(this.subjectText, gvBuffer, params);
            logger.debug("Generated mail subject: " + subject);
            msg.setSubject(subject);

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
            if (this.contentType.equals("text/html")) {
            	logger.debug("Set html message: " + message);
            	msg.setHtmlBody(message);
            }
            else {
            	logger.debug("Set text message: " + message);
            	msg.setTextBody(message);
            }

            String appoBufferName = gvBuffer.getProperty("GV_SMTP_BUFFER_NAME");
            if ((appoBufferName == null) || "".equals(appoBufferName)) {
                appoBufferName = this.gvBufferName;
            }

            if (appoBufferName != null) {
                attachGVBuffer(msg, parseMessage(appoBufferName, gvBuffer, params), gvBuffer.getObject());
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
                    msg.addAttachment(filePath);
                }
            }

        	MessageResponse resp = this.client.deliverMessage(msg);

            String serverResponse = "" + resp.getErrorCode() + " - " + resp.getMessage();
            String messageID = resp.getMessageId();
            gvBuffer.setProperty(this.messageIDProperty, messageID);
            gvBuffer.setProperty(this.serverResponseProperty, serverResponse);
        }
        catch(Exception exc) {
            logger.error("Error preparing/sending email", exc);
            throw exc;
        }
        finally {
            PropertiesHandler.disableExceptionOnErrors();
        }
    }


    /**
     * Attaches GVBuufer data to message.
     *
     * @param data
     *        the data to be attached.
     *
     * @throws MessagingException
     *         if an error occurs.
     */
    private void attachGVBuffer(Message msg,String name, Object data) throws Exception
    {
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

        msg.addAttachment(name, payload, "application/octet-stream");
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
