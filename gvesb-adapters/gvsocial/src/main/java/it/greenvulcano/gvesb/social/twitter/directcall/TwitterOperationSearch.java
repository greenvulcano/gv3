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

import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;

/** 
 * Class managing data for the direct call of search method on Twitter
 * 
 * @version 3.3.3 Apr, 2014
 * @author GreenVulcano Developer Team
 */
public class TwitterOperationSearch extends TwitterOperationBase {
    private static Logger logger = GVLogger.getLogger(TwitterOperationSearch.class);
    
    private String query;
    private String sinceId;
    private String maxId;
    private String since;
    private String until;
    private String count;
    private Document doc = null;
    
    public TwitterOperationSearch(String accountName, String query, String sinceId, String maxId, String since,
    		String until, String count) {
        super(accountName);
        this.query = query;
        this.sinceId = sinceId;
        this.maxId = maxId;
        this.since = since;
        this.until = until;
        this.count = count;
    }

    @Override
    public void execute(SocialAdapterAccount account) throws SocialAdapterException {
        try {
            Twitter twitter = (Twitter) account.getProxyObject();
            Query q = new Query();
            if ((query != null) && !"".equals(query)) {
                q.setQuery(query);
            }
            if ((sinceId != null) && !"".equals(sinceId)) {
                q.setSinceId(Long.parseLong(sinceId));
            }
            if ((maxId != null) && !"".equals(maxId)) {
                q.setMaxId(Long.parseLong(maxId));
            }
            if ((since != null) && !"".equals(since)) {
                q.setSince(since);
            }
            if ((until != null) && !"".equals(until)) {
                q.setUntil(until);
            }
            if ((count != null) && !"".equals(count)) {
                q.setCount(Integer.parseInt(count));
            }

            XMLUtils parser = null;
            try {
                parser = XMLUtils.getParserInstance();
                doc = parser.newDocument("TwitterQuery");
                Element root = doc.getDocumentElement();
                parser.setAttribute(root, "user", twitter.getScreenName());
                parser.setAttribute(root, "userId", String.valueOf(twitter.getId()));
                parser.setAttribute(root, "createdAt", DateUtils.nowToString(DateUtils.FORMAT_ISO_DATETIME_UTC));

                QueryResult result;
                do {
                    result = twitter.search(q);
                    List<Status> tweets = result.getTweets();
                    for (Status tweet : tweets) {
                        dumpTweet(parser, root, tweet);
                    }
                } while ((q = result.nextQuery()) != null);
            }
            catch (Exception exc) {
                logger.error("Error formatting TwitterOperationSearch query[" + query + "], sinceId[" + sinceId
                        + "], maxId[" + maxId + "], since[" + since + "], until[" + until + "] and count[" + count
                        + "] response.", exc);
                throw new SocialAdapterException("Error formatting TwitterOperationSearch query[" + query + "], sinceId["
                        + sinceId + "], maxId[" + maxId + "], since[" + since + "], until[" + until + "] and count["
                        + count + "] response.", exc);
            }
            finally {
                XMLUtils.releaseParserInstance(parser);
            }
        } catch (NumberFormatException exc) {
            logger.error("Call to TwitterOperationSearch failed. Check query[" + query + "], sinceId[" + sinceId
                    + "], maxId[" + maxId + "], since[" + since + "], until[" + until + "] and count[" + count
                    + "] format.", exc);
            throw new SocialAdapterException("Call to TwitterOperationSearch failed. Check query[" + query
                    + "], sinceId[" + sinceId + "], maxId[" + maxId + "], since[" + since + "], until[" + until
                    + "] and count[" + count + "] format.", exc);
        }
    }

    /**
     * Sets an XML containing status updates, for the given user, retrieved into the {@link GVBuffer}.
     */
    @Override
    public void updateResult(GVBuffer buffer) throws GVException {
        buffer.setObject(doc);
    }

}
