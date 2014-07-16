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

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/** 
 * Class managing data for the direct call of updateStatus method
 * on Twitter (Tweet).
 * 
 * @version 3.3.0 Sep, 2012
 * @author GreenVulcano Developer Team
 */
public class TwitterOperationUpdateStatus extends TwitterOperationBase {
    private static Logger logger = GVLogger.getLogger(TwitterOperationUpdateStatus.class);

    private String statusText;
    private Status status;
    
    public TwitterOperationUpdateStatus(String accountName, String statusText) {
        super(accountName);
        this.statusText = statusText;
    }

    @Override
    public void execute(SocialAdapterAccount account) throws SocialAdapterException {
        try {
            Twitter twitter = (Twitter) account.getProxyObject();
            if (statusText.length() > 140){
                statusText = statusText.substring(0, 139);
                logger.warn("TwitterUpdateStatus - Status shortened to 140 characters.");
            }
            status = twitter.updateStatus(statusText);
        } catch (TwitterException exc) {
            logger.error("Call to TwitterUpdateStatus failed.", exc);
            throw new SocialAdapterException("Call to TwitterUpdateStatus failed.", exc);
        }
    }

    @Override
    public void updateResult(GVBuffer buffer) throws GVException  {
        buffer.setObject(status.getText());
    }
}
