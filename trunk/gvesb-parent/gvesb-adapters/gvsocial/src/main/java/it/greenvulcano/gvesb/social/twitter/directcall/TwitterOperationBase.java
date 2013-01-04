package it.greenvulcano.gvesb.social.twitter.directcall;

import it.greenvulcano.gvesb.social.SocialOperation;

/**
 * Superclass for all classes implementing a method call on Twitter.
 * 
 * @author mb
 *
 */
public abstract class TwitterOperationBase implements SocialOperation{

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

}