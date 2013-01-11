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
import it.greenvulcano.gvesb.social.SocialAdapterAccount;
import it.greenvulcano.gvesb.social.SocialAdapterException;
import it.greenvulcano.log.GVLogger;

import org.apache.log4j.Logger;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/** 
 * Class managing data for the direct call of retweetStatus method
 * on Twitter (Retweet).
 * 
 * @version 3.3.0 Sep, 2012
 * @author GreenVulcano Developer Team
 */
public class TwitterOperationRetweetStatus extends TwitterOperationBase{

	private String statusId;
	private Status status;
	private static Logger logger = GVLogger.getLogger(TwitterOperationRetweetStatus.class);
	
	public TwitterOperationRetweetStatus(String accountName, String statusText) {
		super(accountName);
		this.statusId = statusText;
	}

	@Override
	public void execute(SocialAdapterAccount account) throws SocialAdapterException {
		try {
			Twitter twitter = (Twitter) account.getProxyObject();
			status = twitter.retweetStatus(Long.parseLong(statusId));
		} catch (NumberFormatException exc) {
			logger.error("Call to TwitterOperationRetweetStatus failed. Check statusId format.", exc);
			throw new SocialAdapterException("Call to TwitterOperationRetweetStatus failed. Check statusId format.", exc);
		} catch (TwitterException exc) {
			logger.error("Call to TwitterOperationRetweetStatus failed.", exc);
			throw new SocialAdapterException("Call to TwitterOperationRetweetStatus failed.", exc);
		}
	}

	@Override
	public void updateResult(GVBuffer buffer) {
		//buffer.setObject(status.getText());
	}
}
