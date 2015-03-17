/*
 * Copyright (c) 2009-2015 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.virtual.imap;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.SearchTerm;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.sun.mail.imap.IMAPFolder;


/**
 * Manage emails from IMAP server.
 * 
 * @version 3.5.0 Feb 24, 2015
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class ManageEmailCallOperation extends IMAPCallOperation
{
    private static final Logger logger            = GVLogger.getLogger(ManageEmailCallOperation.class);

    private static final String ACTION_COPY       = "copy";
    private static final String ACTION_MOVE       = "move";
    private static final String ACTION_DELETE     = "delete";
    private static final String ACTION_SET_READ   = "set-read";
    private static final String ACTION_SET_UNREAD = "set-unread";

    private String              folderFrom        = "";
    private String              folderTo          = "";
    private String              action            = "";
    
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
            preInit(node);
            
            folderFrom = XMLConfig.get(node, "@folderFrom", "INBOX");
            folderTo = XMLConfig.get(node, "@folderTo", "");

            action = XMLConfig.get(node, "@action", "");

            if (!(action.equals(ACTION_COPY) || action.equals(ACTION_MOVE) || action.equals(ACTION_DELETE)
                    || action.equals(ACTION_SET_READ) || action.equals(ACTION_SET_UNREAD))) {
                throw new Exception("Invalid value for @action attribute [" + action + "]");
            }

            if ((action.equals(ACTION_COPY) || action.equals(ACTION_MOVE)) && folderTo.equals("")) {
                throw new Exception("For @action [move/copy] @folderTo must be defined");
            }
        }
        catch (Exception exc) {
            logger.error("Error initializing Exchange Receive call operation", exc);
            throw new InitializationException("GVVCL_IMAP_RECEIVE_INIT_ERROR", new String[][]{
                    {"node", node.getLocalName()}, {"message", exc.getMessage()}}, exc);
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
            return manageMail(gvBuffer);
        }
        catch (Exception e) {
            throw new CallException("GV_CALL_SERVICE_ERROR",
                    new String[][]{{"service", gvBuffer.getService()}, {"system", gvBuffer.getSystem()},
                            {"id", gvBuffer.getId().toString()}, {"message", e.getMessage()}}, e);
        }
    }

    /**
     * Manage e-mail.
     * 
     * @param data
     *        the input GVBuffer.
     * @return the GVBuffer.
     * @throws Exception
     * @throws Exception
     */
    private GVBuffer manageMail(GVBuffer data) throws Exception
    {
        XMLUtils xml = null;
        
        Store localStore = getStore(data);
        if (performLogin) {
            localStore.connect(serverHost, loginUser, loginPassword);
        }
        else {
            localStore.connect();
        }
        
        try {
            Folder imapFolderFrom = null;
            Folder imapFolderTo = null;

            Folder folder = localStore.getDefaultFolder();
            if (folder == null) {
                logger.error("No default folder");
                throw new Exception("No default folder");
            }

            logger.info("folderFrom [" + folderFrom + "] - folderTo [" + folderTo + "]");
            if (folderFrom != null) {
            //if ((folderFrom != null) && !"INBOX".equalsIgnoreCase(folderFrom)) {
                imapFolderFrom = folder.getFolder(folderFrom);
                if (imapFolderFrom == null) {
                    logger.error("Invalid folder " + folderFrom);
                    throw new Exception("Invalid folder " + folderFrom);
                }
            } else {
            	imapFolderFrom = folder;
            }
            if (!"".equals(folderTo)) {
                imapFolderTo = folder.getFolder(folderTo);
                if (imapFolderTo == null) {
                    logger.error("Invalid folder " + folderTo);
                    throw new Exception("Invalid folder " + folderTo);
                }
            }
            logger.info("exFolderFrom [" + imapFolderFrom + "] - exFolderTo [" + imapFolderTo + "] - default [" + folder + "]");
            
            try {
                imapFolderFrom.open(Folder.READ_WRITE);
            }
            catch (MessagingException ex) {
                imapFolderFrom.open(Folder.READ_ONLY);
            }
            
            String messageId = data.getProperty("MESSAGE_ID");
            logger.info("MESSAGE_ID: " + messageId);

            SearchTerm searchTerm = new MessageIDTerm(messageId);
            Message[] msgs = ((IMAPFolder) imapFolderFrom).search(searchTerm);

            int totalMessages = msgs.length;
            if (totalMessages == 0) {
                logger.debug("No email found in folder [" + folderFrom + "] for messageId: " + messageId);
            }
            else {
                Message message = msgs[0];
                List<Message> tempList = new ArrayList<Message>();
                tempList.add(message);
                Message[] tempMessageArray = tempList.toArray(new Message[tempList.size()]);
                if (action.equals(ACTION_COPY)) {
                    logger.debug("Email to copy from folder [" + folderFrom + "] in folder [" + folderTo + "] found for messageId: " + messageId);
                    try {
                        imapFolderTo.open(Folder.READ_WRITE);
                    }
                    catch (MessagingException ex) {
                        imapFolderTo.open(Folder.READ_ONLY);
                    }
                    imapFolderFrom.copyMessages(tempMessageArray, imapFolderTo);
                    imapFolderTo.close(true);
                }
                else if (action.equals(ACTION_MOVE)) {
                    logger.debug("Email to move from folder [" + folderFrom + "] in folder [" + folderTo + "] found for messageId: " + messageId);
                    try {
                        imapFolderTo.open(Folder.READ_WRITE);
                    }
                    catch (MessagingException ex) {
                        imapFolderTo.open(Folder.READ_ONLY);
                    }
                    imapFolderFrom.copyMessages(tempMessageArray, imapFolderTo);
                    imapFolderFrom.setFlags(tempMessageArray, new Flags(Flag.DELETED), true);
                    imapFolderTo.close(true);
                }
                else if (action.equals(ACTION_DELETE)) {
                    logger.debug("Email to delete in folder [" + folderFrom + "] found for messageId: " + messageId);
                    imapFolderFrom.setFlags(tempMessageArray, new Flags(Flag.DELETED), true);
                }
                else if (action.equals(ACTION_SET_READ)) {
                    logger.debug("Email to set as read in folder [" + folderFrom + "] found for messageId: " + messageId);
                    imapFolderFrom.setFlags(tempMessageArray, new Flags(Flag.SEEN), true);
                }
                else if (action.equals(ACTION_SET_UNREAD)) {
                    logger.debug("Email to set as unread in folder [" + folderFrom + "] found for messageId: "
                            + messageId);
                    imapFolderFrom.setFlags(tempMessageArray, new Flags(Flag.SEEN), false);
                }
            }

            imapFolderFrom.close(true);
            localStore.close();
            data.setRetCode(0);
            data.setProperty("MANAGED_MESSAGE_COUNT", "" + totalMessages);
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
