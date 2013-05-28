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
package it.greenvulcano.gvesb.virtual.pop;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.gvesb.virtual.pop.uidcache.UIDCache;
import it.greenvulcano.gvesb.virtual.pop.uidcache.UIDCacheManagerFactory;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.Message.RecipientType;
import javax.naming.NamingException;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.log4j.Logger;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.sun.mail.pop3.POP3Folder;


/**
 * Check for emails on POP3 server.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class POPCallOperation implements CallOperation
{

    private static final Logger logger          = GVLogger.getLogger(POPCallOperation.class);

    /**
     * The configured operation key
     */
    protected OperationKey      key             = null;

    /**
     * Mail session JNDI name.
     */
    private String              jndiName        = null;

    /**
     * The protocol-specific default Mail server. This overrides the mail.host
     * property.
     */
    private String              protocolHost    = null;

    /**
     * The protocol-specific default user name for connecting to the Mail
     * server. This overrides the mail.user property.
     */
    private String              protocolUser    = null;

    private String              protocol        = "pop3";

    // POP3 supports only a single folder named "INBOX".
    private String              mbox            = "INBOX";
    private boolean             delete_messages = false;
    private boolean             expunge         = false;
    private Store               store           = null;
    private Pattern             emailRxPattern     = null;

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

            protocolHost = XMLConfig.get(node, "@override-protocol-host");
            if (protocolHost != null) {
                logger.debug("Override protocol host: " + protocolHost);
            }

            protocolUser = XMLConfig.get(node, "@override-protocol-user");
            if (protocolUser != null) {
                logger.debug("Override protocol user: " + protocolUser);
            }

            delete_messages = XMLConfig.getBoolean(node, "@delete-messages", false);
            expunge = XMLConfig.getBoolean(node, "@expunge", false);

            initialContext = new JNDIHelper(XMLConfig.getNode(node, "JNDIHelper"));
            logger.debug("Initial Context properties: " + initialContext);

            Session session = (Session) initialContext.lookup(jndiName);

            Properties props = null;
            NodeList nodeList = XMLConfig.getNodeList(node, "mail-properties/mail-property");
            if (nodeList != null && nodeList.getLength() > 0) {
                props = session.getProperties();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node property = nodeList.item(i);
                    String name = XMLConfig.get(property, "@name");
                    String value = XMLConfig.get(property, "@value");
                    props.setProperty(name, value);
                }
            }

            if ((protocolHost != null) || (protocolUser != null)) {
                if (props == null) {
                    props = session.getProperties();
                }
                if (protocolHost != null) {
                    props.setProperty("mail.protocol.host", protocolHost);
                }
                if (protocolUser != null) {
                    props.setProperty("mail.protocol.user", protocolUser);
                }
            }

            if (props != null) {
                session = Session.getDefaultInstance(props, null);
            }

            if (session == null) {
                throw new InitializationException("GVVCL_POP_NO_SESSION", new String[][]{{"node", node.getLocalName()}});
            }

            String regex = XMLConfig.get(node, "@email-rx-cleaner", "[A-z][A-z0-9_]*([.][A-z0-9_]+)*[@][A-z0-9_]+([.][A-z0-9_]+)*[.][A-z]{2,4}");
            emailRxPattern = Pattern.compile(regex);

            store = session.getStore(protocol);
        }
        catch (Exception exc) {
            logger.error("Error initializing POP call operation", exc);
            throw new InitializationException("GVVCL_POP_INIT_ERROR", new String[][]{{"node", node.getLocalName()}},
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
        store.connect();

        XMLUtils xml = null;
        try {
            Folder folder = store.getDefaultFolder();
            if (folder == null) {
                logger.error("No default folder");
                throw new Exception("No default folder");
            }

            folder = folder.getFolder(mbox);
            if (folder == null) {
                logger.error("Invalid folder " + mbox);
                throw new Exception("Invalid folder " + mbox);
            }

            try {
                folder.open(Folder.READ_WRITE);
            }
            catch (MessagingException ex) {
                folder.open(Folder.READ_ONLY);
            }
            int totalMessages = folder.getMessageCount();
            int messageCount = totalMessages;

            if (totalMessages == 0) {
                logger.debug("Empty folder " + mbox);
            }
            else {
                Message[] msgs = folder.getMessages();
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.ENVELOPE);
                fp.add(UIDFolder.FetchProfileItem.UID);
                fp.add("X-Mailer");
                folder.fetch(msgs, fp);

                UIDCache uidCache = UIDCacheManagerFactory.getInstance().getUIDCache(jndiName);

                xml = XMLUtils.getParserInstance();
                Document doc = xml.newDocument("MailMessages");
                for (int i = 0; i < msgs.length; i++) {
                    boolean skipMessage = false;

                    if (!delete_messages) {
                        if (folder instanceof POP3Folder) {
                            POP3Folder pf = (POP3Folder) folder;
                            String uid = pf.getUID(msgs[i]);
                            if (uid != null) {
                                if (uidCache.contains(uid)) {
                                    skipMessage = true;
                                }
                                else {
                                    uidCache.add(uid);
                                }
                            }
                        }
                    }
                    if (!skipMessage) {
                        Element msg = doc.createElement("Message");
                        doc.getDocumentElement().appendChild(msg);
                        dumpPart(msgs[i], msg, doc);
                    }
                    else {
                        messageCount--;
                    }

                    msgs[i].setFlag(Flags.Flag.SEEN, true);
                }
                if (messageCount > 0) {
                    data.setObject(doc);
                }

                if (delete_messages) {
                    folder.setFlags(msgs, new Flags(Flags.Flag.DELETED), true);
                }
            }
            data.setRetCode(0);
            data.setProperty("POP_MESSAGE_COUNT", "" + messageCount);
            folder.close(expunge);
        }
        finally {
            XMLUtils.releaseParserInstance(xml);
            store.close();
        }

        return data;
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

    private void dumpPart(Part p, Element msg, Document doc) throws Exception
    {
        if (p instanceof Message) {
            dumpEnvelope((Message) p, msg, doc);
        }

        Element content = null;
        if (p.isMimeType("text/plain")) {
            content = doc.createElement("PlainMessage");
            Text body = doc.createTextNode((String) p.getContent());
            content.appendChild(body);
        }
        else if (p.isMimeType("text/html")) {
            content = doc.createElement("HTMLMessage");
            CDATASection body = doc.createCDATASection((String) p.getContent());
            content.appendChild(body);
        }
        else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            content = doc.createElement("Multipart");
            for (int i = 0; i < count; i++) {
                dumpPart(mp.getBodyPart(i), content, doc);
            }
        }
        else if (p.isMimeType("message/rfc822")) {
            content = doc.createElement("NestedMessage");
            dumpPart((Part) p.getContent(), content, doc);
        }
        else {
            content = doc.createElement("EncodedContent");
            DataHandler dh = p.getDataHandler();
            OutputStream os = new ByteArrayOutputStream();
            Base64OutputStream b64os = new Base64OutputStream(os, true, -1, "".getBytes());
            dh.writeTo(b64os);
            b64os.flush();
            b64os.close();
            content.appendChild(doc.createTextNode(os.toString()));
        }
        msg.appendChild(content);
        String filename = p.getFileName();
        if (filename != null) {
            content.setAttribute("file-name", filename);
        }
        String ct = p.getContentType();
        if (ct != null) {
            content.setAttribute("content-type", ct);
        }
        String desc = p.getDescription();
        if (desc != null) {
            content.setAttribute("description", desc);
        }
    }

    private void dumpEnvelope(Message m, Element msg, Document doc) throws Exception
    {
        dumpSR(m.getFrom(), msg, "From", doc);
        dumpSR(m.getRecipients(RecipientType.TO), msg, "To", doc);
        dumpSR(m.getRecipients(RecipientType.CC), msg, "Cc", doc);
        dumpSR(m.getRecipients(RecipientType.BCC), msg, "Bcc", doc);
        dumpSR(m.getReplyTo(), msg, "ReplyTo", doc);
        Element headers = doc.createElement("Headers");
        msg.appendChild(headers);
        Enumeration<?> hEnum = m.getAllHeaders();
        while (hEnum.hasMoreElements()) {
            Header h = (Header) hEnum.nextElement();
            Element el = doc.createElement(h.getName());
            Text txt = doc.createTextNode(h.getValue());
            el.appendChild(txt);
            headers.appendChild(el);
        }
        Element subject = doc.createElement("Subject");
        subject.appendChild(doc.createTextNode(m.getSubject()));
        msg.appendChild(subject);
    }

    private void dumpSR(Address[] addr, Element msg, String container, Document doc) throws Exception
    {
        Element cont = doc.createElement(container);
        msg.appendChild(cont);

        Matcher mtc = emailRxPattern.matcher("");

        String list = "";
        if (addr != null) {
            for (Address address : addr) {
                mtc.reset(address.toString());
                while (mtc.find()) {
                    list += mtc.group() + " ";
                }
            }
        }
        cont.appendChild(doc.createTextNode(list.trim()));
    }
}