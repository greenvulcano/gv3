package it.greenvulcano.gvesb.virtual.commons;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Store;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class BaseReceiveMailOperation extends BaseMailOperation {

    private static final Logger logger          = GVLogger.getLogger(BaseReceiveMailOperation.class);

    protected String            loginUser       = null;
    protected String            loginPassword   = null;
    protected String            serverHost      = null;

    protected String            mbox            = "INBOX";
    protected boolean           delete_messages = false;
    protected boolean           expunge         = false;
    protected boolean           exportEML       = false;
    protected Store             store           = null;
    /**
     * The emails cleaner pattern
     */
    protected Pattern           emailRxPattern  = null;

    /**
     * Preliminary initialization operations
     */
    protected Session preInit(Node node) throws InitializationException {
        try {
            mbox = XMLConfig.get(node, "@folder", "INBOX");
            logger.debug("Messages folder: " + mbox);

            delete_messages = XMLConfig.getBoolean(node, "@delete-messages", false);
            expunge = XMLConfig.getBoolean(node, "@expunge", false);

            exportEML = XMLConfig.getBoolean(node, "@export-EML", false);

            String regex = XMLConfig.get(node, "@email-rx-cleaner", "[A-z][A-z0-9_\\-]*([.][A-z0-9_\\-]+)*[@][A-z0-9_\\-]+([.][A-z0-9_\\-]+)*[.][A-z]{2,4}");
            emailRxPattern = Pattern.compile(regex);

            Session session = super.preInit(node);

            if (!dynamicServer) {
                store = session.getStore(getProtocol());
            }

            return session;
        }
        catch (Exception exc) {
            logger.error("Error initializing IMAP call operation", exc);
            throw new InitializationException("GVVCL_RCV_MAIL_INIT_ERROR", new String[][]{{"node", node.getLocalName()}},
                    exc);
        }
    }

    protected abstract String getProtocol();

    /**
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        try {
            return receiveMails(gvBuffer);
        }
        catch (Exception exc) {
            throw new CallException("GV_CALL_SERVICE_ERROR",
                    new String[][]{{"service", gvBuffer.getService()}, {"system", gvBuffer.getSystem()},
                            {"id", gvBuffer.getId().toString()}, {"message", exc.getMessage()}}, exc);
        }
    }

    protected abstract GVBuffer receiveMails(GVBuffer data) throws Exception;

    protected abstract void postStore(Store locStore, GVBuffer data) throws Exception;

    protected Store getStore(GVBuffer data) throws Exception {
        loginUser     = null;
        loginPassword = null;
        serverHost    = null;

        if (!dynamicServer) {
            postStore(store, data);
            return store;
        }

        try {
            PropertiesHandler.enableExceptionOnErrors();
            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(data, true);

            Properties localProps = new Properties();
            for (Iterator iterator = serverProps.keySet().iterator(); iterator.hasNext();) {
                String name = (String) iterator.next();
                String value = PropertiesHandler.expand(serverProps.getProperty(name), params, data);
                if (name.contains(".host")) {
                    logger.debug("Logging-in to host: " + value);
                    serverHost = value;
                }
                else if (name.contains(".user")) {
                    logger.debug("Logging-in as user: " + value);
                    loginUser = value;
                }
                else if (name.contains(".password")) {
                    value = XMLConfig.getDecrypted(value);
                    //logger.debug("Logging-in with password: " + value);
                    loginPassword = value;
                }
                localProps.setProperty(name, value);
            }

            Session session = Session.getInstance(localProps, null);

            if (session == null) {
                throw new CallException("GVVCL_RCV_MAIL_NO_SESSION", new String[][]{{"properties", "" + localProps}});
            }

            Store locStore = session.getStore(getProtocol());

            postStore(locStore, data);

            return locStore;
        }
        catch (CallException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new CallException("GVVCL_RCV_MAIL_SESSION_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
        finally {
            PropertiesHandler.disableExceptionOnErrors();
        }
    }

    protected void dumpPart(Part p, Element msg, XMLUtils xml) throws Exception
    {
        if (p instanceof Message) {
            dumpEnvelope((Message) p, msg, xml);
        }

        Element content = null;
        String filename = p.getFileName();
        if (p.isMimeType("text/plain") && (filename == null)) {
            content = xml.insertElement(msg, "PlainMessage");
            xml.insertText(content, (String) p.getContent());
        }
        else if (p.isMimeType("text/html") && (filename == null)) {
            content = xml.insertElement(msg, "HTMLMessage");
            xml.insertCDATA(content, (String) p.getContent());
        }
        else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            content = xml.insertElement(msg, "Multipart");
            for (int i = 0; i < count; i++) {
                dumpPart(mp.getBodyPart(i), content, xml);
            }
        }
        else if (p.isMimeType("message/rfc822")) {
            content = xml.insertElement(msg, "NestedMessage");
            dumpPart((Part) p.getContent(), content, xml);
        }
        else {
            content = xml.insertElement(msg, "EncodedContent");
            DataHandler dh = p.getDataHandler();
            OutputStream os = new ByteArrayOutputStream();
            Base64OutputStream b64os = new Base64OutputStream(os, true, -1, "".getBytes());
            dh.writeTo(b64os);
            b64os.flush();
            b64os.close();
            xml.insertText(content, os.toString());
        }

        if (filename != null) {
            xml.setAttribute(content, "file-name", filename);
        }
        String ct = p.getContentType();
        if (ct != null) {
            xml.setAttribute(content, "content-type", ct);
        }
        String desc = p.getDescription();
        if (desc != null) {
            xml.setAttribute(content, "description", desc);
        }
    }

    private void dumpEnvelope(Message m, Element msg, XMLUtils xml) throws Exception
    {
        dumpSR(m.getFrom(), msg, "From", xml);
        dumpSR(m.getRecipients(RecipientType.TO), msg, "To", xml);
        dumpSR(m.getRecipients(RecipientType.CC), msg, "Cc", xml);
        dumpSR(m.getRecipients(RecipientType.BCC), msg, "Bcc", xml);
        dumpSR(m.getReplyTo(), msg, "ReplyTo", xml);
        Element headers = xml.insertElement(msg, "Headers");
        Enumeration<?> hEnum = m.getAllHeaders();
        while (hEnum.hasMoreElements()) {
            Header h = (Header) hEnum.nextElement();
            Element el = xml.insertElement(headers, h.getName());
            xml.insertText(el, h.getValue());
        }
        Element subject = xml.insertElement(msg, "Subject");
        xml.insertText(subject, m.getSubject());
    }

    private void dumpSR(Address[] addr, Element msg, String container, XMLUtils xml) throws Exception
    {
        Element cont = xml.insertElement(msg, container);
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
        xml.insertText(cont, list.trim());
    }

}
