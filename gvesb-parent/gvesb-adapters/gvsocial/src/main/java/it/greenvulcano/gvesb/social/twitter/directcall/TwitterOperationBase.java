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

import org.w3c.dom.Element;

import twitter4j.Status;
import it.greenvulcano.gvesb.social.SocialOperation;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

/**
 * Superclass for all classes implementing a method call on Twitter.
 * 
 * @version 3.3.0 Sep, 2012
 * @author GreenVulcano Developer Team
 */
public abstract class TwitterOperationBase implements SocialOperation {
    private String accountName;
    final String SOCIAL_NAME = "twitter";

    public TwitterOperationBase(String accountName) {
        this.accountName = accountName;
    }

    @Override
    public String getSocialName() {
        return SOCIAL_NAME;
    }

    @Override
    public String getAccountName() {
        return this.accountName;
    }

    /**
     * @param parser
     * @param root
     * @param status
     * @throws XMLUtilsException
     */
    protected void dumpTweet(XMLUtils parser, Element root, Status status) throws XMLUtilsException {
        Element tweet = parser.insertElement(root, "Tweet");
        parser.setAttribute(tweet, "id", String.valueOf(status.getId()));
        parser.setAttribute(tweet, "createdAt", DateUtils.dateToString(status.getCreatedAt(), DateUtils.FORMAT_ISO_DATETIME_UTC));
        parser.setAttribute(tweet, "fromUser", status.getUser().getScreenName());
        parser.setAttribute(tweet, "fromUserId", String.valueOf(status.getUser().getId()));
        parser.insertText(tweet, status.getText());
    }

}