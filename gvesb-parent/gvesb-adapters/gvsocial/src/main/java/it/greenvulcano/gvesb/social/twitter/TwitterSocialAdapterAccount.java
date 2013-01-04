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
package it.greenvulcano.gvesb.social.twitter;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.social.SocialAdapterAccount;
import it.greenvulcano.gvesb.social.SocialAdapterException;
import it.greenvulcano.gvesb.social.SocialAdapterProxy;
import it.greenvulcano.gvesb.social.Tokens;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.txt.TextUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/** 
 * Rappresenta un account utente su Twitter.
 *
 * @version 3.3.0 Sep, 2012
 * @author GreenVulcano Developer Team
 * 
 */
public class TwitterSocialAdapterAccount implements SocialAdapterAccount{
    private static Logger logger = GVLogger.getLogger(SocialAdapterAccount.class);
    private static final String ACCOUNTS = "sp{{gv.app.home}}/xmlconfig/TwitterAccounts.properties";
    private String consumerKey;
	private String consumerSecret;
	private String accessToken;
	private String accessTokenSecret;
	private String realPath;
	private Twitter twitter;
	private RequestToken requestToken;
	private String accountName;
	private boolean isAuthorized;
	private SocialAdapterProxy proxy;

    public TwitterSocialAdapterAccount(SocialAdapterProxy proxy) {
		this.proxy = proxy;
	}

	@Override
	public void init(Node node) throws SocialAdapterException {
    	logger.info("Initializing TwitterSocialAdapterAccount...");
		try {
			realPath = PropertiesHandler.expand(ACCOUNTS);
			accountName = XMLConfig.get(node, "@name");
			consumerKey = XMLConfig.get(node, "@consumer_key");
			consumerSecret = XMLConfig.get(node, "@consumer_secret");
			FileInputStream fis = new FileInputStream(realPath);
			Properties props = new Properties();
			props.load(fis);
			if (props.containsKey(accountName + ".oauth_access_token")){
				this.isAuthorized = true;
				accessToken = props.getProperty(accountName + ".oauth_access_token");
				accessTokenSecret = props.getProperty(accountName + ".oauth_access_token_secret");
			} else {
				this.isAuthorized = false;
			}
			logger.info("...account " + accountName + " initialized.");
		} catch (Exception e) {
			logger.error(e);
			throw new SocialAdapterException("Error initializing Account: " + accountName +".", e);
		}
	}
	
	/**
	 * This method returns the interface class towards Twitter, already instantiated with the
	 * account's tokens
	 * 
	 * @return {@link Twitter}
	 */
    public Twitter getProxyObject(){
    	if (twitter != null) return twitter;
    	else {
    		// setting OAuth tokens
    		ConfigurationBuilder confBuilder = new ConfigurationBuilder();
    		if (proxy != null){
	    		confBuilder.setHttpProxyHost(proxy.getHttpProxyHost());
	    		confBuilder.setHttpProxyPort(proxy.getHttpProxyPort());
	    		confBuilder.setHttpProxyUser(proxy.getHttpProxyUser());
	    		confBuilder.setHttpProxyPassword(proxy.getHttpProxyPassword());
    		}
            confBuilder.setOAuthConsumerKey(consumerKey);
            confBuilder.setOAuthConsumerSecret(consumerSecret);
            confBuilder.setOAuthAccessToken(accessToken);
            confBuilder.setOAuthAccessTokenSecret(accessTokenSecret);
            Configuration config =  confBuilder.build();
            // instantiating Twitter object
    	    this.twitter = new TwitterFactory(config).getInstance();
    	}
	    logger.info("got TwitterFactory instance.");
        return twitter;
	}

    @Override
    public Tokens getConsumerTokens() {
    	Tokens tokens = new Tokens();
    	tokens.setConsumerKey(consumerKey);
    	tokens.setConsumerSecret(consumerSecret);
    	return tokens;
    }

    /**
     * Saves the access tokens to properties file.
     * 
     * @throws SocialAdapterException
     */
    private void saveAccessTokens() throws SocialAdapterException {
    	String toWrite = this.accountName+".oauth_access_token="+this.accessToken;
    	toWrite += "\n"+this.accountName+".oauth_access_token_secret="+this.accessTokenSecret;
    	try {
			TextUtils.writeFile(toWrite, ACCOUNTS, true);
		} catch (IOException e) {
			logger.error(e);
			throw new SocialAdapterException("Error saving tokens for Account: " + accountName +".", e);
		}
    	this.isAuthorized = true;
    }
    
    /**
     * Returns the Request token and URL to be used to confirm the token.
     * 
     */
    @Override
    public Tokens getRequestTokenAndURL() throws SocialAdapterException {
    	Tokens tokens = new Tokens();
    	RequestToken requestToken;
		try {
			requestToken = this.getProxyObject().getOAuthRequestToken();
			this.requestToken = requestToken;
		} catch (TwitterException e) {
			logger.error(e);
			throw new SocialAdapterException("Call to Social Adapter Account: " + accountName +" failed.", e);
		}
    	tokens.setRequestToken(requestToken.getToken());
    	tokens.setAuthorizationURL(requestToken.getAuthorizationURL());
    	return tokens;
    }
    	
    /**
     * Gives the token back to the social platform obtaining the access tokens
     * and saving them. 
     */
	@Override
	public void setPINAndSave(String PIN) throws SocialAdapterException {
		try {
			AccessToken access = this.getProxyObject().getOAuthAccessToken(this.requestToken, PIN);
			this.accessToken = access.getToken();
			this.accessTokenSecret = access.getTokenSecret();
		} catch (TwitterException e) {
			logger.error(e);
			throw new SocialAdapterException("Call to Social Adapter Account: "
					+ accountName + "] failed.", e);
		}
		saveAccessTokens();
	}
	
	/**
	 * Returns a boolean meaning wether the account has been authorized or not.
	 */
	@Override
	public boolean isAuthorized() {
		return isAuthorized;
	}
	
	@Override
	public String getAccountName() {
		return this.accountName;
	}
}
