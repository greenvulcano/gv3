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
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import microsoft.exchange.webservices.data.Attachment;
import microsoft.exchange.webservices.data.AttachmentCollection;
import microsoft.exchange.webservices.data.BasePropertySet;
import microsoft.exchange.webservices.data.BodyType;
import microsoft.exchange.webservices.data.ConflictResolutionMode;
import microsoft.exchange.webservices.data.DeleteMode;
import microsoft.exchange.webservices.data.EmailAddress;
import microsoft.exchange.webservices.data.EmailAddressCollection;
import microsoft.exchange.webservices.data.EmailMessage;
import microsoft.exchange.webservices.data.EmailMessageSchema;
import microsoft.exchange.webservices.data.ExchangeCredentials;
import microsoft.exchange.webservices.data.ExchangeService;
import microsoft.exchange.webservices.data.FileAttachment;
import microsoft.exchange.webservices.data.FindFoldersResults;
import microsoft.exchange.webservices.data.FindItemsResults;
import microsoft.exchange.webservices.data.Folder;
import microsoft.exchange.webservices.data.FolderSchema;
import microsoft.exchange.webservices.data.FolderTraversal;
import microsoft.exchange.webservices.data.FolderView;
import microsoft.exchange.webservices.data.InternetMessageHeader;
import microsoft.exchange.webservices.data.InternetMessageHeaderCollection;
import microsoft.exchange.webservices.data.Item;
import microsoft.exchange.webservices.data.ItemSchema;
import microsoft.exchange.webservices.data.ItemView;
import microsoft.exchange.webservices.data.MessageBody;
import microsoft.exchange.webservices.data.PropertySet;
import microsoft.exchange.webservices.data.SearchFilter;
import microsoft.exchange.webservices.data.SortDirection;
import microsoft.exchange.webservices.data.WebCredentials;
import microsoft.exchange.webservices.data.WellKnownFolderName;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


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
    private int                 timeout         = 60000;

    private String              exchangeURL     = null;

    private ExchangeService     service         = null;

    private String              folderName      = "Inbox";
    private boolean             delete_messages = false;
    private Pattern             emailRxPattern  = null;

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
            userName = XMLConfig.get(node, "@userName");
            password = XMLConfig.get(node, "@password");
            domain = XMLConfig.get(node, "@domain", null);
            exchangeURL = XMLConfig.get(node, "@exchangeURL");

            timeout = XMLConfig.getInteger(node, "@timeout", timeout);

            folderName = XMLConfig.get(node, "@folderName", "Inbox");

            delete_messages = XMLConfig.getBoolean(node, "@delete-messages", false);

            exportEML = XMLConfig.getBoolean(node, "@export-EML", false);

            String regex = XMLConfig.get(node, "@email-rx-cleaner",
                    "[A-z][A-z0-9_\\-]*([.][A-z0-9_\\-]+)*[@][A-z0-9_\\-]+([.][A-z0-9_\\-]+)*[.][A-z]{2,4}");
            emailRxPattern = Pattern.compile(regex);

            service = new ExchangeService();
            service.setTimeout(timeout);
            ExchangeCredentials credentials = (domain == null)
                    ? new WebCredentials(userName, password)
                    : new WebCredentials(userName, password, domain);
            service.setCredentials(credentials);
            service.setUrl(new URI(exchangeURL));
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
            Folder searchFolder = null;

            if ((folderName != null) && !"Inbox".equals(folderName)) {
                SearchFilter searchFilter = new SearchFilter.IsEqualTo(FolderSchema.DisplayName, folderName);
                FolderView view = new FolderView(1);
                view.setPropertySet(new PropertySet(BasePropertySet.IdOnly));
                view.getPropertySet().add(FolderSchema.DisplayName);
                view.setTraversal(FolderTraversal.Deep);
                FindFoldersResults ffrs = service.findFolders(WellKnownFolderName.Root, searchFilter, view);
                searchFolder = ffrs.getFolders().get(0);
            }

            ItemView view = new ItemView(10);
            //ItemView view = new ItemView(1); // FOR TEST
            view.getOrderBy().add(ItemSchema.DateTimeReceived, SortDirection.Descending);

            FindItemsResults<Item> findResults = null;
            if (searchFolder == null) {
                findResults = service.findItems(WellKnownFolderName.Inbox, new SearchFilter.IsEqualTo(
                        EmailMessageSchema.IsRead, false), view);
            }
            else {
                findResults = service.findItems(searchFolder.getId(), new SearchFilter.IsEqualTo(
                        EmailMessageSchema.IsRead, false), view);
            }

            System.out.println("Total number of items found: " + findResults.getTotalCount());

            int totalMessages = findResults.getTotalCount();
            if (totalMessages == 0) {
                logger.debug("Empty folder " + folderName);
            }
            else {
                PropertySet itemProps = new PropertySet(BasePropertySet.FirstClassProperties);
                itemProps.setRequestedBodyType(BodyType.Text);
                if (exportEML) {
                    itemProps.add(ItemSchema.MimeContent);
                }

                xml = XMLUtils.getParserInstance();
                Document doc = xml.newDocument("MailMessages");
                for (Item item : findResults) {
                    //item = Item.bind(service, item.getId());
                    item.load(itemProps);
                    Element msg = xml.insertElement(doc.getDocumentElement(), "Message");
                    dumpMessage((EmailMessage) item, msg, xml);
                }

                data.setObject(doc);

                for (Item item : findResults) {
                    ((EmailMessage) item).setIsRead(true);
                    item.update(ConflictResolutionMode.AlwaysOverwrite);
                    if (delete_messages) {
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

        if (exportEML) {
            Element eml = xml.insertElement(msg, "EML");
            xml.setAttribute(eml, "encoding", "base64");
            xml.insertText(eml, Base64.encodeBase64String(m.getMimeContent().getContent()));
        }
    }


    private void dumpSR(EmailAddressCollection addr, Element msg, String container, XMLUtils xml) throws Exception
    {
        Element cont = xml.insertElement(msg, container);

        Matcher mtc = emailRxPattern.matcher("");

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
        Matcher mtc = emailRxPattern.matcher("");

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
