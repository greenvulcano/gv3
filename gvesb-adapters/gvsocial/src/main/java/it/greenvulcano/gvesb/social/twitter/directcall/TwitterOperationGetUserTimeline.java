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
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.xml.XMLUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/** 
 * Class managing data for the direct call of getUserTimeline method
 * on Twitter
 * 
 * @version 3.3.3 Apr, 2014
 * @author GreenVulcano Developer Team
 */
public class TwitterOperationGetUserTimeline extends TwitterOperationBase {
    private static Logger logger = GVLogger.getLogger(TwitterOperationGetUserTimeline.class);

    private String userId;
    private String sinceId;
    private String maxId;
    private String count;
    private String dataUser;
    private String dataUserId;
    private ResponseList<Status> statusList;
    
    public TwitterOperationGetUserTimeline(String accountName, String userId, String sinceId, String maxId, String count) {
        super(accountName);
        this.userId = userId;
        this.sinceId = sinceId;
        this.maxId = maxId;
        this.count = count;
    }

    @Override
    public void execute(SocialAdapterAccount account) throws SocialAdapterException {
        try {
            Twitter twitter = (Twitter) account.getProxyObject();
            Paging paging = new Paging();
            if ((sinceId != null) && !"".equals(sinceId)) {
                paging.setSinceId(Long.parseLong(sinceId));
            }
            if ((maxId != null) && !"".equals(maxId)) {
                paging.setMaxId(Long.parseLong(maxId));
            }
            if ((count != null) && !"".equals(count)) {
                paging.setCount(Integer.parseInt(count));
            }
            if ((userId == null) || "".equals(userId)) {
                dataUser = twitter.getScreenName();
                dataUserId = String.valueOf(twitter.getId());
                statusList = twitter.getUserTimeline(paging);
            }
            else {
                User user = null;
                try {
                    long id = Long.parseLong(userId);
                    user = twitter.showUser(id);
                    statusList = twitter.getUserTimeline(id, paging);
                }
                catch (NumberFormatException exc) {
                    user = twitter.showUser(userId);
                    statusList = twitter.getUserTimeline(userId, paging);
                }
                dataUser = user.getScreenName();
                dataUserId = String.valueOf(user.getId());
            }
        } catch (NumberFormatException exc) {
            logger.error("Call to TwitterOperationGetUserTimeline failed. Check userId[" + userId + "], sinceId["
                    + sinceId + "], maxId[" + maxId + "] and count[" + count + "] format.", exc);
            throw new SocialAdapterException("Call to TwitterOperationGetUserTimeline failed. Check followingId["
                    + userId + "], sinceId[" + sinceId + "], maxId[" + maxId + "] and count[" + count + "] format.", exc);
        } catch (TwitterException exc) {
            logger.error("Call to TwitterOperationGetUserTimeline followingId[" + userId + "], sinceId[" + sinceId
                    + "], maxId[" + maxId + "] and count[" + count + "] failed.", exc);
            throw new SocialAdapterException("Call to TwitterOperationGetUserTimeline followingId[" + userId
                    + "], sinceId[" + sinceId + "], maxId[" + maxId + "] and count[" + count + "] failed.", exc);
        }
    }

    /**
     * Sets an XML containing status updates, for the given user, retrieved into the {@link GVBuffer}.
     */
    @Override
    public void updateResult(GVBuffer buffer) throws GVException {
        XMLUtils parser = null;
        try {
            parser = XMLUtils.getParserInstance();
            Document doc = parser.newDocument("TwitterTimeline");
            Element root = doc.getDocumentElement();
            parser.setAttribute(root, "user", dataUser);
            parser.setAttribute(root, "userId", dataUserId);
            parser.setAttribute(root, "createdAt", DateUtils.nowToString(DateUtils.FORMAT_ISO_DATETIME_UTC));

            for (Status status : statusList) {
                dumpTweet(parser, root, status);
            }
            buffer.setObject(doc);
        }
        catch (Exception exc) {
            logger.error("Error formatting TwitterOperationGetUserTimeline followingId[" + userId + "], sinceId["
                    + sinceId + "] and count[" + count + "] response.", exc);
            throw new GVException("Error formatting TwitterOperationGetUserTimeline followingId[" + userId
                    + "], sinceId[" + sinceId + "] and count[" + count + "] response. " + exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }

}
