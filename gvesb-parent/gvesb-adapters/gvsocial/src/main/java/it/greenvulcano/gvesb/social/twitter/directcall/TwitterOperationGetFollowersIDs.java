package it.greenvulcano.gvesb.social.twitter.directcall;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.social.SocialAdapterAccount;
import it.greenvulcano.gvesb.social.SocialAdapterException;
import it.greenvulcano.log.GVLogger;

import org.apache.log4j.Logger;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/** Class managing data for the direct call of getFollowersIDs method
 * on Twitter
 * 
 * @author mb
 *
 */
public class TwitterOperationGetFollowersIDs extends TwitterOperationBase{

	private String followingId;
	private String cursor;
	private IDs ids;
	private static Logger logger = GVLogger.getLogger(TwitterOperationGetFollowersIDs.class);
	
	public TwitterOperationGetFollowersIDs(String accountName, String followingId, String cursor) {
		super(accountName);
		this.followingId = followingId;
		this.cursor = cursor;
	}


	@Override
	public void execute(SocialAdapterAccount account) throws SocialAdapterException {
		try {
			Twitter twitter = (Twitter) account.getProxyObject();
			ids = twitter.getFollowersIDs(Long.parseLong(followingId), Long.parseLong(cursor));
		} catch (NumberFormatException exc) {
			logger.error(exc);
			throw new SocialAdapterException("Call to TwitterOperationEnableNotification failed. Check followingId and cursor format.", exc);
		} catch (TwitterException exc) {
			logger.error(exc);
			throw new SocialAdapterException("Call to TwitterOperationGetFollowersIDs failed.", exc);
		}
	}

	/**
	 * Sets a long[] with all the ids retrieved into the {@link GVBuffer}.
	 */
	@Override
	public void updateResult(GVBuffer buffer) throws GVException {
		buffer.setObject(ids.getIDs());
	}
}
