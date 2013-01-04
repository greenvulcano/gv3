package it.greenvulcano.gvesb.social.twitter.directcall;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.social.SocialAdapterAccount;
import it.greenvulcano.gvesb.social.SocialAdapterException;
import it.greenvulcano.log.GVLogger;

import org.apache.log4j.Logger;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/** Class managing data for the direct call of updateStatus method
 * on Twitter (Tweet).
 * 
 * @author mb
 *
 */
public class TwitterOperationUpdateStatus extends TwitterOperationBase{

	private String statusText;
	private Status status;
	private static Logger logger = GVLogger.getLogger(TwitterOperationUpdateStatus.class);
	
	public TwitterOperationUpdateStatus(String accountName, String statusText) {
		super(accountName);
		this.statusText = statusText;
	}

	@Override
	public void execute(SocialAdapterAccount account) throws SocialAdapterException {
		try {
			Twitter twitter = (Twitter) account.getProxyObject();
			status = twitter.updateStatus(statusText);
		} catch (TwitterException exc) {
			logger.error(exc);
			throw new SocialAdapterException("Call to TwitterUpdateStatus failed.", exc);
		}
	}

	@Override
	public void updateResult(GVBuffer buffer) {
//		buffer.setObject(status.getText());
	}
}
