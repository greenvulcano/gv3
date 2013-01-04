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

import org.w3c.dom.Node;

import twitter4j.Twitter;

/**
 * 
 * Represents an Account in a social platform.
 * 
 * @version 3.3.0 Sep, 2012
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public interface SocialAdapterAccount {

	/** Initializing method.
	 *  
	 * @param node XML configuration node
	 * @throws SocialAdapterException 
	 */
	public abstract void init(Node node) throws SocialAdapterException;
	
	/**
	 * This method returns the interface class towards the social platform
	 * already instantiated with the account's tokens.
	 * 
	 * @return {@link Object}
	 */
    public abstract Object getProxyObject();
    
    /**
     *  Returns the consumer tokens.
     */
    public abstract Tokens getConsumerTokens();
    
    /**
     * Returns the Request token and URL to be used to confirm the token.
     * 
     */
    public abstract Tokens getRequestTokenAndURL() throws SocialAdapterException;

    /**
     * Gives the token back to the social platform obtaining the access tokens
     * and saving them. 
     */
	public abstract void setPINAndSave(String pIN) throws SocialAdapterException;
	
	/**
	 * Returns a boolean meaning wether the account has been authorized or not.
	 */
	public abstract boolean isAuthorized();
	
	/**
	 * Returns the account name as configured.
	 */
	public abstract String getAccountName();
}
