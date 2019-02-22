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

import java.net.URI;
import java.util.EnumSet;

import org.apache.log4j.Logger;
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
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.FolderTraversal;
import microsoft.exchange.webservices.data.core.enumeration.service.ConflictResolutionMode;
import microsoft.exchange.webservices.data.core.enumeration.service.DeleteMode;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.core.service.schema.FolderSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.misc.ITraceListener;
import microsoft.exchange.webservices.data.misc.ImpersonatedUserId;
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
public class ManageEmailCallOperation implements CallOperation
{
    private static final Logger logger            = GVLogger.getLogger(ManageEmailCallOperation.class);
    private static final Logger loggerTrc         = GVLogger.getLogger("EWSApiTrace");

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
    private String              oauthId           = null;
    private int                 timeout           = 60000;

    private String              exchangeURL       = null;
    private boolean             traceEWS          = false;

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
            this.userName = XMLConfig.get(node, "@userName", null);
            this.password = XMLConfig.get(node, "@password", null);
            this.domain = XMLConfig.get(node, "@domain", null);
            this.oauthId = XMLConfig.get(node, "@oauth-id", null);
            this.exchangeURL = XMLConfig.get(node, "@exchangeURL");
            this.traceEWS = XMLConfig.getBoolean(node, "@traceEWS", false);

            this.timeout = XMLConfig.getInteger(node, "@timeout", this.timeout);

            this.folderFrom = XMLConfig.get(node, "@folderFrom", "Inbox");
            this.folderTo = XMLConfig.get(node, "@folderTo", "");

            this.action = XMLConfig.get(node, "@action", "");

            if (!(this.action.equals(ACTION_COPY) || this.action.equals(ACTION_MOVE) || this.action.equals(ACTION_DELETE)
                    || this.action.equals(ACTION_SET_READ) || this.action.equals(ACTION_SET_UNREAD))) {
                throw new Exception("Invalid value for @action attribute [" + this.action + "]");
            }

            if ((this.action.equals(ACTION_COPY) || this.action.equals(ACTION_MOVE)) && this.folderTo.equals("")) {
                throw new Exception("For @action [move/copy] @folderTo must be defined");
            }

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
            if (this.oauthId != null) {
            	this.service.getHttpHeaders().put("Authorization", "Bearer " + OAuthHelper.instance().getToken(this.oauthId));
            }
            Folder exFolderFrom = null;
            Folder exFolderTo = null;

            if ((this.folderFrom != null) && !"Inbox".equals(this.folderFrom)) {
                exFolderFrom = searchFolder(this.folderFrom);
            }
            if (!"".equals(this.folderTo)) {
                exFolderTo = searchFolder(this.folderTo);
            }

            String messageId = data.getProperty("MESSAGE_ID");

            ItemView view = new ItemView(1);
            FindItemsResults<Item> findResults = null;
            if (exFolderFrom == null) {
                findResults = this.service.findItems(WellKnownFolderName.Inbox, new SearchFilter.IsEqualTo(
                        EmailMessageSchema.InternetMessageId, messageId), view);
            }
            else {
                findResults = this.service.findItems(exFolderFrom.getId(), new SearchFilter.IsEqualTo(
                        EmailMessageSchema.InternetMessageId, messageId), view);
            }

            int totalMessages = findResults.getTotalCount();
            if (totalMessages == 0) {
                logger.debug("No email found in folder [" + this.folderFrom + "] for messageId: " + messageId);
            }
            else {
                PropertySet itemProps = new PropertySet(BasePropertySet.FirstClassProperties);
                Item message = findResults.getItems().get(0);
                message.load(itemProps);

                if (this.action.equals(ACTION_COPY)) {
                    logger.debug("Email to copy in folder [" + this.folderTo + "] found for messageId: " + messageId);
                    message.copy(exFolderTo.getId());
                }
                else if (this.action.equals(ACTION_MOVE)) {
                    logger.debug("Email to move in folder [" + this.folderTo + "] found for messageId: " + messageId);
                    message.move(exFolderTo.getId());
                }
                else if (this.action.equals(ACTION_DELETE)) {
                    logger.debug("Email to delete in folder [" + this.folderFrom + "] found for messageId: " + messageId);
                    message.delete(DeleteMode.MoveToDeletedItems);
                }
                else if (this.action.equals(ACTION_SET_READ)) {
                    logger.debug("Email to set as read in folder [" + this.folderFrom + "] found for messageId: "
                            + messageId);
                    ((EmailMessage) message).setIsRead(true);
                    message.update(ConflictResolutionMode.AlwaysOverwrite);
                }
                else if (this.action.equals(ACTION_SET_UNREAD)) {
                    logger.debug("Email to set as unread in folder [" + this.folderFrom + "] found for messageId: "
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
        FindFoldersResults ffrs = this.service.findFolders(WellKnownFolderName.Root, searchFilter, view);
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
