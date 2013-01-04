package it.greenvulcano.gvesb.social.twitter.directcall;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.social.SocialAdapterAccount;
import it.greenvulcano.gvesb.social.SocialAdapterException;
import it.greenvulcano.log.GVLogger;

import org.apache.log4j.Logger;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/** Class managing data for the direct call of retweetStatus method
 * on Twitter (Retweet).
 * 
 * @author mb
 *
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
			logger.error(exc);
			throw new SocialAdapterException("Call to TwitterOperationRetweetStatus failed. Check statusId format.", exc);
		} catch (TwitterException exc) {
			logger.error(exc);
			throw new SocialAdapterException("Call to TwitterOperationRetweetStatus failed.", exc);
		}
	}

	@Override
	public void updateResult(GVBuffer buffer) {
//		buffer.setObject(status.getText());
	}
}
