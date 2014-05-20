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
import it.greenvulcano.util.ArrayUtils;

import org.apache.log4j.Logger;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/** 
 * Class managing data for the direct call of getFollowersIDs method
 * on Twitter
 * 
 * @version 3.3.0 Sep, 2012
 * @author GreenVulcano Developer Team
 */
public class TwitterOperationGetFollowersIDs extends TwitterOperationBase {
    private static Logger logger = GVLogger.getLogger(TwitterOperationGetFollowersIDs.class);

    private String followingId;
    private String cursor;
    private IDs ids;
    
    public TwitterOperationGetFollowersIDs(String accountName, String followingId, String cursor) {
        super(accountName);
        this.followingId = followingId;
        this.cursor = cursor;
    }

    @Override
    public void execute(SocialAdapterAccount account) throws SocialAdapterException {
        try {
            Twitter twitter = (Twitter) account.getProxyObject();
            if ((followingId == null) || "".equals(followingId)) {
                ids = twitter.getFollowersIDs(Long.parseLong(cursor));
            }
            else {
                try {
                    long id = Long.parseLong(followingId);
                    ids = twitter.getFollowersIDs(id, Long.parseLong(cursor));
                }
                catch (NumberFormatException exc) {
                    ids = twitter.getFollowersIDs(followingId, Long.parseLong(cursor));
                }
            }
        } catch (NumberFormatException exc) {
            logger.error("Call to TwitterOperationGetFollowersIDs failed. Check followingId[" + followingId
            		+ "] and cursor[" + cursor + "] format.", exc);
            throw new SocialAdapterException("Call to TwitterOperationGetFollowersIDs failed. Check followingId["
            		+ followingId + "] and cursor[" + cursor + "] format.", exc);
        } catch (TwitterException exc) {
            logger.error("Call to TwitterOperationGetFollowersIDs followingId[" + followingId + "] and cursor[" + cursor
            		+ "] failed.", exc);
            throw new SocialAdapterException("Call to TwitterOperationGetFollowersIDs followingId[" + followingId
            		+ "] and cursor[" + cursor + "] failed.", exc);
        }
    }

    /**
     * Sets a List of longs with all the ids retrieved into the {@link GVBuffer}.
     */
    @Override
    public void updateResult(GVBuffer buffer) throws GVException {
        buffer.setObject(ArrayUtils.arrayToList(ids.getIDs()));
    }
}
