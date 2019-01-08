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
package it.greenvulcano.gvesb.social.twitter.directcall;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.social.SocialAdapterAccount;
import it.greenvulcano.gvesb.social.SocialAdapterException;
import it.greenvulcano.log.GVLogger;

import org.apache.log4j.Logger;

import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/** 
 * Class managing data for the direct call of sendDirectMessage method
 * on Twitter
 * 
 * @version 3.3.0 Sep, 2012
 * @author GreenVulcano Developer Team
 */
public class TwitterOperationSendDirectMessage extends TwitterOperationBase {
    private static Logger logger = GVLogger.getLogger(TwitterOperationSendDirectMessage.class);

    private String toAccountId;
    private String message;
    private DirectMessage status;
    
    public TwitterOperationSendDirectMessage(String accountName, String toAccountId, String message) {
        super(accountName);
        this.toAccountId = toAccountId;
        this.message = message;
    }

    @Override
    public void execute(SocialAdapterAccount account) throws SocialAdapterException {
        try {
            Twitter twitter = (Twitter) account.getProxyObject();
            if (message.length() > 140) {
                message = message.substring(0, 139);
                logger.warn("TwitterOperationSendDirectMessage - Message shortened to 140 characters.");
            }
            try {
                long id = Long.parseLong(toAccountId);
                status = twitter.sendDirectMessage(id, message);
            }
            catch (NumberFormatException exc) {
                status = twitter.sendDirectMessage(toAccountId, message);
            }
        } catch (NumberFormatException exc) {
            logger.error("Call to TwitterOperationSendDirectMessage failed. Check toAccountId[" + toAccountId
            		+ "] format.", exc);
            throw new SocialAdapterException("Call to TwitterOperationSendDirectMessage failed. Check toAccountId["
            		+ toAccountId +"] format.", exc);
        } catch (TwitterException exc) {
            logger.error("Call to TwitterOperationSendDirectMessage toAccountId[" + toAccountId +"] failed.", exc);
            throw new SocialAdapterException("Call to TwitterOperationSendDirectMessage toAccountId[" + toAccountId
            		+ "] failed.", exc);
        }
    }

    @Override
    public void updateResult(GVBuffer buffer) throws GVException {
        buffer.setObject(status.getText());
    }
}
