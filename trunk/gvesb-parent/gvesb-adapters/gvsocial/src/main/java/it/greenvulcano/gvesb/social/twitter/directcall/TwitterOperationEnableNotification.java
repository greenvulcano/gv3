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

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Class for enableNotification call on Twitter (Follow).
 * 
 * @version 3.3.0 Sep, 2012
 * @author GreenVulcano Developer Team
 */
public class TwitterOperationEnableNotification extends TwitterOperationBase{

	private String fromAccountId;
	private User user;
	private static Logger logger = GVLogger.getLogger(TwitterOperationEnableNotification.class);
	
	public TwitterOperationEnableNotification(String accountName, String statusText) {
		super(accountName);
		this.fromAccountId = statusText;
	}

	@Override
	public void execute(SocialAdapterAccount account) throws SocialAdapterException {
		try {
			Twitter twitter = (Twitter) account.getProxyObject();
			user = twitter.enableNotification(Long.parseLong(fromAccountId));
		} catch (NumberFormatException exc) {
			logger.error("Call to TwitterOperationEnableNotification failed. Check fromAccountId format.", exc);
			throw new SocialAdapterException("Call to TwitterOperationEnableNotification failed. Check fromAccountId format.", exc);
		} catch (TwitterException exc) {
			logger.error("Call to TwitterOperationEnableNotification failed.", exc);
			throw new SocialAdapterException("Call to TwitterOperationEnableNotification failed.", exc);
		}
	}

	@Override
	public void updateResult(GVBuffer buffer) {
		//buffer.setObject(user.getId());
	}
}
