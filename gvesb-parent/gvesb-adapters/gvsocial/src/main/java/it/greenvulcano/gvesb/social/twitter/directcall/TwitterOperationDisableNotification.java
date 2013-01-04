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
 * Class for disableNotification call on Twitter (Unfollow).
 * 
 * @author mb
 *
 */
public class TwitterOperationDisableNotification extends TwitterOperationBase{

	private String fromAccountId;
	private User user;
	private static Logger logger = GVLogger.getLogger(TwitterOperationDisableNotification.class);
	
	public TwitterOperationDisableNotification(String accountName, String statusText) {
		super(accountName);
		this.fromAccountId = statusText;
	}

	@Override
	public void execute(SocialAdapterAccount account) throws SocialAdapterException {
		try {
			Twitter twitter = (Twitter) account.getProxyObject();
			user = twitter.disableNotification(Long.parseLong(fromAccountId));
		} catch (NumberFormatException exc) {
			logger.error(exc);
			throw new SocialAdapterException("Call to TwitterOperationDisableNotification failed. Check fromAccountId format.", exc);
		} catch (TwitterException exc) {
			logger.error(exc);
			throw new SocialAdapterException("Call to TwitterOperationDisableNotification failed.", exc);
		}
	}

	@Override
	public void updateResult(GVBuffer buffer) {
//		buffer.setObject(user.getId());
	}
}
