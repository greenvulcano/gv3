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
package it.greenvulcano.gvesb.social;

/**
 * 
 * Bean class for OAuth tokens and fields.
 * 
 * @version 3.3.0 Sep, 2012
 * @author mb
 * 
 * 
 */
public class Tokens {

    private String consumer_key, consumer_secret, oauth_access_token, oauth_access_token_secret, request_token, AuthorizationURL, PIN;

	public String getPIN() {
		return PIN;
	}

	public void setPIN(String pIN) {
		PIN = pIN;
	}

	public String getConsumerKey() {
		return consumer_key;
	}

	public void setConsumerKey(String consumer_key) {
		this.consumer_key = consumer_key;
	}

	public String getConsumerSecret() {
		return consumer_secret;
	}

	public void setConsumerSecret(String consumer_secret) {
		this.consumer_secret = consumer_secret;
	}

	public void setConsumerTokens(String consumer_key, String consumer_secret) {
		this.consumer_key = consumer_key;
		this.consumer_secret = consumer_secret;
	}

	public String getOauthAccessToken() {
		return oauth_access_token;
	}

	public void setOauthAccessToken(String oauth_access_token) {
		this.oauth_access_token = oauth_access_token;
	}

	public String getOauthAccessTokenSecret() {
		return oauth_access_token_secret;
	}

	public void setOauthAccessTokenSecret(String oauth_access_token_secret) {
		this.oauth_access_token_secret = oauth_access_token_secret;
	}

	public void setOauthAccessTokens(String oauth_access_token, String oauth_access_token_secret) {
		this.oauth_access_token = oauth_access_token;
		this.oauth_access_token_secret = oauth_access_token_secret;
	}

	public String getRequestToken() {
		return request_token;
	}

	public void setRequestToken(String request_token) {
		this.request_token = request_token;
	}

	public String getAuthorizationURL() {
		return AuthorizationURL;
	}

	public void setAuthorizationURL(String authorizationURL) {
		AuthorizationURL = authorizationURL;
	}

}
