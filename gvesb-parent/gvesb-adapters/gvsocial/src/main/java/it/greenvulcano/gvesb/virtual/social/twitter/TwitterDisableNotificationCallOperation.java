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
package it.greenvulcano.gvesb.virtual.social.twitter;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.social.SocialAdapterManager;
import it.greenvulcano.gvesb.social.SocialOperation;
import it.greenvulcano.gvesb.social.twitter.directcall.TwitterOperationDisableNotification;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * Class defining a call to disableNotification (Unfollow) on Twitter.
 * 
 * @version 3.3.0 Sep, 2012
 * @author GreenVulcano Developer Team
 */
public class TwitterDisableNotificationCallOperation extends TwitterSocialCallOperation {
    private static Logger logger = GVLogger.getLogger(TwitterDisableNotificationCallOperation.class);
	private String fromAccountId;
    
    @Override
	public void init(Node node) throws InitializationException {
		try{
			super.init(node);
			fromAccountId = XMLConfig.get(node, "@fromAccountId");
		}
        catch (Exception exc) {
            logger.error("ERROR TwitterDisableNotificationCallOperation[" + getName() + "] initialization", exc);
            throw new InitializationException("GV_CONF_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
	}
	
	@Override
	public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException,
			CallException, InvalidDataException {
		SocialAdapterManager instance = SocialAdapterManager.getInstance();
		try {
			Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);

			String acc = PropertiesHandler.expand(getAccount(), params, gvBuffer);
			String frmAcc = PropertiesHandler.expand(fromAccountId, params, gvBuffer);
			logger.debug("Account: " + acc + " - FromAccount: " + frmAcc);

			SocialOperation op = new TwitterOperationDisableNotification(acc, frmAcc);
			instance.directExecute(op);
			op.updateResult(gvBuffer);
		}
        catch (Exception exc) {
        	logger.error("ERROR TwitterDisableNotificationCallOperation[" + getName() + "] execution", exc);
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
        }
		return gvBuffer;
	}
}
