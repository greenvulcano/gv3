package it.greenvulcano.gvesb.social.twitter.directcall;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.social.SocialAdapterAccount;
import it.greenvulcano.gvesb.social.SocialAdapterException;
import it.greenvulcano.log.GVLogger;

import org.apache.log4j.Logger;

import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/** Class managing data for the direct call of sendDirectMessage method
 * on Twitter
 * 
 * @author mb
 *
 */
public class TwitterOperationSendDirectMessage extends TwitterOperationBase{

	private String toAccountId;
	private String message;
	private DirectMessage status;
	private static Logger logger = GVLogger.getLogger(TwitterOperationSendDirectMessage.class);
	
	public TwitterOperationSendDirectMessage(String accountName, String toAccountId, String message) {
		super(accountName);
		this.toAccountId = toAccountId;
		this.message = message;
	}


	@Override
	public void execute(SocialAdapterAccount account) throws SocialAdapterException {
		try {
			Twitter twitter = (Twitter) account.getProxyObject();
			status = twitter.sendDirectMessage(Long.parseLong(toAccountId), message);
		} catch (NumberFormatException exc) {
			logger.error(exc);
			throw new SocialAdapterException("Call to TwitterOperationRetweetStatus failed. Check toAccountId format.", exc);
		} catch (TwitterException exc) {
			logger.error(exc);
			throw new SocialAdapterException("Call to TwitterOperationSendDirectMessage failed.", exc);
		}
	}

	@Override
	public void updateResult(GVBuffer buffer) {
//		buffer.setObject(status.getText());
	}
}
