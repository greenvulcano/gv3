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

import java.net.URI;

import microsoft.exchange.webservices.data.BasePropertySet;
import microsoft.exchange.webservices.data.ConflictResolutionMode;
import microsoft.exchange.webservices.data.DeleteMode;
import microsoft.exchange.webservices.data.EmailMessage;
import microsoft.exchange.webservices.data.EmailMessageSchema;
import microsoft.exchange.webservices.data.ExchangeCredentials;
import microsoft.exchange.webservices.data.ExchangeService;
import microsoft.exchange.webservices.data.FindFoldersResults;
import microsoft.exchange.webservices.data.FindItemsResults;
import microsoft.exchange.webservices.data.Folder;
import microsoft.exchange.webservices.data.FolderSchema;
import microsoft.exchange.webservices.data.FolderTraversal;
import microsoft.exchange.webservices.data.FolderView;
import microsoft.exchange.webservices.data.Item;
import microsoft.exchange.webservices.data.ItemView;
import microsoft.exchange.webservices.data.PropertySet;
import microsoft.exchange.webservices.data.SearchFilter;
import microsoft.exchange.webservices.data.WebCredentials;
import microsoft.exchange.webservices.data.WellKnownFolderName;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;


/**
 * Read emails from MS Exchange server.
 * 
 * @version 3.3.0 Oct 6, 2012
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class ManageEmailCallOperation implements CallOperation
{
    private static final Logger logger            = GVLogger.getLogger(ManageEmailCallOperation.class);

    private static final String ACTION_COPY       = "copy";
    private static final String ACTION_MOVE       = "move";
    private static final String ACTION_DELETE     = "delete";
    private static final String ACTION_SET_READ   = "set-read";
    private static final String ACTION_SET_UNREAD = "set-unread";

    /**
     * The configured operation key
     */
    protected OperationKey      key               = null;

    /**
     * Account user name.
     */
    private String              userName          = null;
    /**
     * Account password.
     */
    private String              password          = null;
    /**
     * Account domain.
     */
    private String              domain            = null;
    private int                 timeout           = 60000;

    private String              exchangeURL       = null;

    private ExchangeService     service           = null;

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
            userName = XMLConfig.get(node, "@userName");
            password = XMLConfig.get(node, "@password");
            domain = XMLConfig.get(node, "@domain", null);
            exchangeURL = XMLConfig.get(node, "@exchangeURL");

            timeout = XMLConfig.getInteger(node, "@timeout", timeout);

            folderFrom = XMLConfig.get(node, "@folderFrom", "Inbox");
            folderTo = XMLConfig.get(node, "@folderTo", "");

            action = XMLConfig.get(node, "@action", "");

            if (!(action.equals(ACTION_COPY) || action.equals(ACTION_MOVE) || action.equals(ACTION_DELETE)
                    || action.equals(ACTION_SET_READ) || action.equals(ACTION_SET_UNREAD))) {
                throw new Exception("Invalid value for @action attribute [" + action + "]");
            }

            if ((action.equals(ACTION_COPY) || action.equals(ACTION_MOVE)) && folderTo.equals("")) {
                throw new Exception("For @action [move/copy] @folderTo must be defined");
            }

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
            throw new InitializationException("GVVCL_MSEX_RECEIVE_INIT_ERROR", new String[][]{
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
        try {
            Folder exFolderFrom = null;
            Folder exFolderTo = null;

            if ((folderFrom != null) && !"Inbox".equals(folderFrom)) {
                exFolderFrom = searchFolder(folderFrom);
            }
            if (!"".equals(folderTo)) {
                exFolderTo = searchFolder(folderTo);
            }

            String messageId = data.getProperty("MESSAGE_ID");

            ItemView view = new ItemView(1);
            FindItemsResults<Item> findResults = null;
            if (exFolderFrom == null) {
                findResults = service.findItems(WellKnownFolderName.Inbox, new SearchFilter.IsEqualTo(
                        EmailMessageSchema.InternetMessageId, messageId), view);
            }
            else {
                findResults = service.findItems(exFolderFrom.getId(), new SearchFilter.IsEqualTo(
                        EmailMessageSchema.InternetMessageId, messageId), view);
            }

            int totalMessages = findResults.getTotalCount();
            if (totalMessages == 0) {
                logger.debug("No email found in folder [" + folderFrom + "] for messageId: " + messageId);
            }
            else {
                PropertySet itemProps = new PropertySet(BasePropertySet.FirstClassProperties);
                Item message = findResults.getItems().get(0);
                message.load(itemProps);

                if (action.equals(ACTION_COPY)) {
                    logger.debug("Email to copy in folder [" + folderTo + "] found for messageId: " + messageId);
                    message.copy(exFolderTo.getId());
                }
                else if (action.equals(ACTION_MOVE)) {
                    logger.debug("Email to move in folder [" + folderTo + "] found for messageId: " + messageId);
                    message.move(exFolderTo.getId());
                }
                else if (action.equals(ACTION_DELETE)) {
                    logger.debug("Email to delete in folder [" + folderFrom + "] found for messageId: " + messageId);
                    message.delete(DeleteMode.MoveToDeletedItems);
                }
                else if (action.equals(ACTION_SET_READ)) {
                    logger.debug("Email to set as read in folder [" + folderFrom + "] found for messageId: "
                            + messageId);
                    ((EmailMessage) message).setIsRead(true);
                    message.update(ConflictResolutionMode.AlwaysOverwrite);
                }
                else if (action.equals(ACTION_SET_UNREAD)) {
                    logger.debug("Email to set as unread in folder [" + folderFrom + "] found for messageId: "
                            + messageId);
                    ((EmailMessage) message).setIsRead(false);
                    message.update(ConflictResolutionMode.AlwaysOverwrite);
                }
            }
            data.setRetCode(0);
            data.setProperty("MANAGED_MESSAGE_COUNT", "" + totalMessages);
        }
        finally {
            XMLUtils.releaseParserInstance(xml);
        }

        return data;
    }

    /**
     * @param folderName
     * @return
     * @throws Exception
     */
    private Folder searchFolder(String folderName) throws Exception
    {
        Folder exFolderFrom;
        SearchFilter searchFilter = new SearchFilter.IsEqualTo(FolderSchema.DisplayName, folderName);
        FolderView view = new FolderView(1);
        view.setPropertySet(new PropertySet(BasePropertySet.IdOnly));
        view.getPropertySet().add(FolderSchema.DisplayName);
        view.setTraversal(FolderTraversal.Deep);
        FindFoldersResults ffrs = service.findFolders(WellKnownFolderName.Root, searchFilter, view);
        exFolderFrom = ffrs.getFolders().get(0);
        return exFolderFrom;
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
