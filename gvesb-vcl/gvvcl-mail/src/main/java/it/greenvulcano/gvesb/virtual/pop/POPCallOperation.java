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

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.commons.BaseReceiveMailOperation;
import it.greenvulcano.gvesb.virtual.pop.uidcache.UIDCache;
import it.greenvulcano.gvesb.virtual.pop.uidcache.UIDCacheManagerFactory;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.UIDFolder;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.mail.pop3.POP3Store;

/**
 * Check for emails on POP3 server.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class POPCallOperation extends BaseReceiveMailOperation
{

    private static final Logger logger          = GVLogger.getLogger(POPCallOperation.class);

    private String              protocol        = "pop3";
    
    private String              cacheKey        = null;

    /**
     * Invoked from <code>OperationFactory</code> when an <code>Operation</code>
     * needs initialization.<br>
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws InitializationException
    {
        try {
            preInit(node);
        }
        catch (Exception exc) {
            logger.error("Error initializing POP call operation", exc);
            throw new InitializationException("GVVCL_POP_INIT_ERROR", new String[][]{{"node", node.getLocalName()}},
                    exc);
        }
    }

    @Override
    protected String getProtocol() {
        return protocol;
    }

    @Override
    protected void postStore(Store locStore, GVBuffer data) throws Exception {
        cacheKey = null;
        if (!dynamicServer) {
            cacheKey = jndiName;
            return;
        }

        cacheKey = serverHost + "_" + loginUser;
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
    protected GVBuffer receiveMails(GVBuffer data) throws Exception
    {
        Store localStore = getStore(data);
        if (performLogin) {
            localStore.connect(serverHost, loginUser, loginPassword);
        }
        else {
            localStore.connect();
        }

        XMLUtils xml = null;
        try {
            Folder folder = localStore.getDefaultFolder();
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
            int messageCount = 0;

            if (totalMessages == 0) {
                logger.debug("Empty folder " + mbox);
            }
            else {
                List<Message> seen = new ArrayList<Message>();
                Message[] msgs = folder.getMessages();
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.ENVELOPE);
                fp.add(UIDFolder.FetchProfileItem.UID);
                fp.add("X-Mailer");
                folder.fetch(msgs, fp);
                
                UIDCache uidCache = UIDCacheManagerFactory.getInstance().getUIDCache(cacheKey);

                xml = XMLUtils.getParserInstance();
                Document doc = xml.newDocument("MailMessages");
                int i = 0;
                while ((i < msgs.length) && ((maxReadMessages == -1) || (messageCount < maxReadMessages))) {
                    boolean skipMessage = false;

                    if (!delete_messages) {
                        if (localStore instanceof POP3Store) {
                            String uid = msgs[i].getHeader("Message-ID")[0];
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
                        Element msg = xml.insertElement(doc.getDocumentElement(), "Message");
                        dumpPart(msgs[i], msg, xml);
                        messageCount++;
                        if (exportEML) {
                            Element eml = xml.insertElement(msg, "EML");
                            xml.setAttribute(eml, "encoding", "base64");
                            OutputStream os = new ByteArrayOutputStream();
                            Base64OutputStream b64os = new Base64OutputStream(os, true, -1, "".getBytes());
                            msgs[i].writeTo(b64os);
                            b64os.flush();
                            b64os.close();
                            xml.insertText(eml, os.toString());
                        }
                    }

                    msgs[i].setFlag(Flags.Flag.SEEN, true);
                    seen.add(msgs[i]);
                    i++;
                }
                if (messageCount > 0) {
                    data.setObject(doc);
                }

                if (delete_messages) {
                    folder.setFlags(seen.toArray(new Message[seen.size()]), new Flags(Flags.Flag.DELETED), true);
                }
            }
            data.setRetCode(0);
            data.setProperty("POP_MESSAGE_COUNT", String.valueOf(messageCount));
            folder.close(expunge);
        }
        finally {
            XMLUtils.releaseParserInstance(xml);
            if (localStore != null) {
                localStore.close();
            }
        }

        return data;
    }
}
