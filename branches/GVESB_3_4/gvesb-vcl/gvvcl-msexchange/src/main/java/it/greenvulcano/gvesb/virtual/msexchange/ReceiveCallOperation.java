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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.gvesb.virtual.msexchange.oauth.OAuthHelper;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xml.XMLUtils;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ConnectingIdType;
import microsoft.exchange.webservices.data.core.enumeration.misc.TraceFlags;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BodyType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.FolderTraversal;
import microsoft.exchange.webservices.data.core.enumeration.search.SortDirection;
import microsoft.exchange.webservices.data.core.enumeration.service.ConflictResolutionMode;
import microsoft.exchange.webservices.data.core.enumeration.service.DeleteMode;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.core.service.schema.FolderSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.misc.ITraceListener;
import microsoft.exchange.webservices.data.misc.ImpersonatedUserId;
import microsoft.exchange.webservices.data.property.complex.Attachment;
import microsoft.exchange.webservices.data.property.complex.AttachmentCollection;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.EmailAddressCollection;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.InternetMessageHeader;
import microsoft.exchange.webservices.data.property.complex.InternetMessageHeaderCollection;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import microsoft.exchange.webservices.data.search.FindFoldersResults;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.FolderView;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;


/**
 * Read emails from MS Exchange server.
 *
 * @version 3.3.0 Oct 6, 2012
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ReceiveCallOperation implements CallOperation
{
    private static final Logger logger          = GVLogger.getLogger(ReceiveCallOperation.class);
    private static final Logger loggerTrc       = GVLogger.getLogger("EWSApiTrace");

    /**
     * The configured operation key
     */
    protected OperationKey      key             = null;

    /**
     * Account user name.
     */
    private String              userName        = null;
    /**
     * Account password.
     */
    private String              password        = null;
    /**
     * Account domain.
     */
    private String              domain          = null;
    private String              oauthId         = null;
    private int                 timeout         = 60000;
    private int                 intOpTimeout    = -1;

    private String              exchangeURL     = null;
    private boolean             traceEWS        = false;

    private ExchangeService     service         = null;

    private String              folderName      = "Inbox";
    private boolean             delete_messages = false;
    private Pattern             emailRxPattern  = null;
    private int                 numEmails       = 10;

    private boolean             exportEML       = false;

    /**
     * Invoked from <code>OperationFactory</code> when an <code>Operation</code>
     * needs initialization.<br>
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            this.userName = XMLConfig.get(node, "@userName", null);
            this.password = XMLConfig.get(node, "@password", null);
            this.domain = XMLConfig.get(node, "@domain", null);
            this.oauthId = XMLConfig.get(node, "@oauth-id", null);
            this.exchangeURL = XMLConfig.get(node, "@exchangeURL");
            this.traceEWS = XMLConfig.getBoolean(node, "@traceEWS", false);

            this.timeout = XMLConfig.getInteger(node, "@timeout", this.timeout);

            this.intOpTimeout = XMLConfig.getInteger(node, "@intOpTimeout", this.intOpTimeout);

            this.folderName = XMLConfig.get(node, "@folderName", "Inbox");
            this.numEmails  = XMLConfig.getInteger(node, "@numEmails", this.numEmails);

            this.delete_messages = XMLConfig.getBoolean(node, "@delete-messages", false);

            this.exportEML = XMLConfig.getBoolean(node, "@export-EML", false);

            String regex = XMLConfig.get(node, "@email-rx-cleaner",
                    "[A-z][A-z0-9_\\-]*([.][A-z0-9_\\-]+)*[@][A-z0-9_\\-]+([.][A-z0-9_\\-]+)*[.][A-z]{2,4}");
            this.emailRxPattern = Pattern.compile(regex);

            this.service = new ExchangeService();
            if (this.traceEWS) {
                this.service.setTraceEnabled(true);
                this.service.setTraceFlags(EnumSet.allOf(TraceFlags.class)); // can also be restricted
                this.service.setTraceListener(new ITraceListener() {
                    @Override
					public void trace(String traceType, String traceMessage) {
                        // do some logging-mechanism here
                        loggerTrc.debug("Type:" + traceType + " Message:" + traceMessage);
                    }
                });
            }

            this.service.setTimeout(this.timeout);
            if (this.oauthId == null) {
            	ExchangeCredentials credentials = (this.domain == null)
            			? new WebCredentials(this.userName, this.password)
            			: new WebCredentials(this.userName, this.password, this.domain);
            			this.service.setCredentials(credentials);
            }
            this.service.setImpersonatedUserId(new ImpersonatedUserId(ConnectingIdType.SmtpAddress, this.userName));
            //this.service.getHttpHeaders().put("X-AnchorMailbox", "testinvitalia@invitalia.it");
            this.service.setUrl(new URI(this.exchangeURL));
        }
        catch (Exception exc) {
            logger.error("Error initializing Exchange Receive call operation", exc);
            throw new InitializationException("GVVCL_MSEX_RECEIVE_INIT_ERROR", new String[][]{{"node",
                    node.getLocalName()}}, exc);
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
            return receiveMails(gvBuffer);
        }
        catch (Exception e) {
            throw new CallException("GV_CALL_SERVICE_ERROR",
                    new String[][]{{"service", gvBuffer.getService()}, {"system", gvBuffer.getSystem()},
                            {"id", gvBuffer.getId().toString()}, {"message", e.getMessage()}}, e);
        }
    }

    /**
     * Receives e-mails.
     *
     * @param data
     *        the input GVBuffer.
     * @return the GVBuffer.
     * @throws Exception
     * @throws Exception
     */
    private GVBuffer receiveMails(GVBuffer data) throws Exception
    {
        XMLUtils xml = null;
        try {
            if (this.oauthId != null) {
            	this.service.getHttpHeaders().put("Authorization", "Bearer " + OAuthHelper.instance().getToken(this.oauthId));
            }
        	Folder searchFolder = null;

            if ((this.folderName != null) && !"Inbox".equals(this.folderName)) {
                SearchFilter searchFilter = new SearchFilter.IsEqualTo(FolderSchema.DisplayName, this.folderName);
                FolderView view = new FolderView(1);
                view.setPropertySet(new PropertySet(BasePropertySet.IdOnly));
                view.getPropertySet().add(FolderSchema.DisplayName);
                view.setTraversal(FolderTraversal.Deep);
                FindFoldersResults ffrs = this.service.findFolders(WellKnownFolderName.Root, searchFilter, view);
                searchFolder = ffrs.getFolders().get(0);
            }

            ItemView view = new ItemView(this.numEmails);
            view.getOrderBy().add(ItemSchema.DateTimeReceived, SortDirection.Descending);

            FindItemsResults<Item> findResults = null;
            if (searchFolder == null) {
                findResults = this.service.findItems(WellKnownFolderName.Inbox, new SearchFilter.IsEqualTo(
                        EmailMessageSchema.IsRead, false), view);
            }
            else {
                findResults = this.service.findItems(searchFolder.getId(), new SearchFilter.IsEqualTo(
                        EmailMessageSchema.IsRead, false), view);
            }

            logger.info("Total number of emails found: " + findResults.getTotalCount());

            int totalMessages = findResults.getTotalCount();
            if (totalMessages == 0) {
                logger.debug("Empty folder " + this.folderName);
            }
            else {
                PropertySet itemProps = new PropertySet(BasePropertySet.FirstClassProperties);
                itemProps.setRequestedBodyType(BodyType.Text);
                if (this.exportEML) {
                    itemProps.add(ItemSchema.MimeContent);
                }

                xml = XMLUtils.getParserInstance();
                Document doc = xml.newDocument("MailMessages");
                for (Item item : findResults) {
                    if (this.intOpTimeout > 0) {
                        System.out.println("Sleeping for " + this.intOpTimeout + "ms during EWSAPI calls");
                        Thread.sleep(this.intOpTimeout);
                    }
                    //item = Item.bind(service, item.getId());
                    item.load(itemProps);
                    Element msg = xml.insertElement(doc.getDocumentElement(), "Message");
                    dumpMessage((EmailMessage) item, msg, xml);
                }

                data.setObject(doc);

                for (Item item : findResults) {
                    if (this.intOpTimeout > 0) {
                        System.out.println("Sleeping for " + this.intOpTimeout + "ms during EWSAPI calls");
                        Thread.sleep(this.intOpTimeout);
                    }
                    ((EmailMessage) item).setIsRead(true);
                    item.update(ConflictResolutionMode.AlwaysOverwrite);
                    if (this.delete_messages) {
                        item.delete(DeleteMode.MoveToDeletedItems);
                    }
                }
            }
            data.setRetCode(0);
            data.setProperty("POP_MESSAGE_COUNT", "" + totalMessages);
        }
        finally {
            XMLUtils.releaseParserInstance(xml);
        }

        return data;
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

    private void dumpAttachment(Attachment at, Element msg, XMLUtils xml) throws Exception
    {
        if (at instanceof FileAttachment) {
            Element content = xml.insertElement(msg, "EncodedContent");
            OutputStream os = new ByteArrayOutputStream();
            Base64OutputStream b64os = new Base64OutputStream(os, true, -1, "".getBytes());
            ((FileAttachment) at).load(b64os);
            b64os.flush();
            b64os.close();
            xml.insertText(content, os.toString());
            String filename = at.getName();
            if (filename != null) {
                xml.setAttribute(content, "file-name", filename);
            }
            String ct = at.getContentType();
            if (ct != null) {
                xml.setAttribute(content, "content-type", ct);
            }
        }
    }

    private void dumpMessage(EmailMessage m, Element msg, XMLUtils xml) throws Exception
    {
        /*EmailAddress ema = m.getFrom();
        if (ema == null) {
            ema = m.getSender();
        }
        dumpSR(ema, msg, "From", xml);*/
        dumpSR(m.getFrom(), msg, "From", xml);
        dumpSR(m.getToRecipients(), msg, "To", xml);
        dumpSR(m.getCcRecipients(), msg, "Cc", xml);
        dumpSR(m.getBccRecipients(), msg, "Bcc", xml);
        dumpSR(m.getReplyTo(), msg, "ReplyTo", xml);
        Element headers = xml.insertElement(msg, "Headers");
        InternetMessageHeaderCollection hEnum = m.getInternetMessageHeaders();
        if (hEnum != null) {
            for (InternetMessageHeader h : hEnum) {
                Element el = xml.insertElement(headers, h.getName());
                xml.insertText(el, h.getValue());
            }
        }
        Element subject = xml.insertElement(msg, "Subject");
        xml.insertText(subject, m.getSubject());

        Element content = xml.insertElement(msg, "Multipart");
        xml.setAttribute(content, "content-type", "multipart/alternative");
        MessageBody b = m.getBody();
        if (b.getBodyType() == BodyType.Text) {
            Element pm = xml.insertElement(content, "PlainMessage");
            xml.setAttribute(pm, "content-type", "text/plain");
            xml.insertText(pm, MessageBody.getStringFromMessageBody(b));
        }
        else {
            Element hm = xml.insertElement(content, "HTMLMessage");
            xml.setAttribute(hm, "content-type", "text/html");
            xml.insertCDATA(hm, MessageBody.getStringFromMessageBody(b));
        }

        if (m.getHasAttachments()) {
            AttachmentCollection atc = m.getAttachments();
            for (Attachment at : atc) {
                dumpAttachment(at, content, xml);
            }
        }

        if (this.exportEML) {
            Element eml = xml.insertElement(msg, "EML");
            xml.setAttribute(eml, "encoding", "base64");
            xml.insertText(eml, Base64.encodeBase64String(m.getMimeContent().getContent()));
        }
    }


    private void dumpSR(EmailAddressCollection addr, Element msg, String container, XMLUtils xml) throws Exception
    {
        Element cont = xml.insertElement(msg, container);

        Matcher mtc = this.emailRxPattern.matcher("");

        String list = "";
        if (addr != null) {
            for (EmailAddress address : addr) {
                mtc.reset(address.getAddress());
                while (mtc.find()) {
                    list += mtc.group() + " ";
                }
            }
        }
        xml.insertText(cont, list.trim());
    }


    private void dumpSR(EmailAddress addr, Element msg, String container, XMLUtils xml) throws Exception
    {
        Element cont = xml.insertElement(msg, container);
        Matcher mtc = this.emailRxPattern.matcher("");

        String list = "";
        if (addr != null) {
            mtc.reset(addr.getAddress());
            while (mtc.find()) {
                list += mtc.group() + " ";
            }
        }
        xml.insertText(cont, list.trim());
    }
}
