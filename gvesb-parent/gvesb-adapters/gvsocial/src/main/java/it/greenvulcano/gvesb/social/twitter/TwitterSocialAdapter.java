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
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.social.SocialAdapter;
import it.greenvulcano.gvesb.social.SocialAdapterAccount;
import it.greenvulcano.gvesb.social.SocialAdapterException;
import it.greenvulcano.gvesb.social.SocialAdapterProxy;
import it.greenvulcano.log.GVLogger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import twitter4j.conf.ConfigurationBuilder;

/**
 * 
 * Represents the Twitter Social Adapter. Contains all
 * the Account configured for Twitter.
 * 
 * @version 3.3.0 Sep, 2012
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class TwitterSocialAdapter extends SocialAdapter{
    private static Logger logger = GVLogger.getLogger(SocialAdapter.class);
	Node accountsConfig;
	public Map<String, SocialAdapterAccount> accounts = new HashMap<String, SocialAdapterAccount>();
	private String socialName;
	private SocialAdapterProxy proxy;
	
	@Override
	public void init(Node in) throws SocialAdapterException {
		this.accountsConfig = in;
		logger.info("Initializing TwitterSocialAdapter...");
		try {
			socialName = XMLConfig.get(in, "@social");
			if (XMLConfig.exists(in, "Proxy")){
				// lettura del nodo <Proxy proxyHost="" proxyPassword="" proxyPort=""...
				proxy = new SocialAdapterProxy();
				Node nodeProxy = XMLConfig.getNode(in, "Proxy");
				proxy.setHttpProxyHost(XMLConfig.get(nodeProxy, "@proxyHost"));
				proxy.setHttpProxyPort(XMLConfig.getInteger(nodeProxy, "@proxyPort"));
				proxy.setHttpProxyUser(XMLConfig.get(nodeProxy, "@proxyUser"));
				proxy.setHttpProxyPassword(XMLConfig.get(nodeProxy, "@proxyPassword"));
			}
			logger.info("...done.");
		} catch (Exception e) {
			logger.error(e);
			throw new SocialAdapterException("Error initializing TwitterSocialAdapter", e);
		}
	}
	
	public SocialAdapterAccount getAccount(String accountName) throws SocialAdapterException {
		// ricerca nell'HashMap
logger.info("getAccount - name: " + accountName);
		SocialAdapterAccount account = accounts.get(accountName);
		try {
			if (account == null){
				// reading account configuration
				Node accountConfig = XMLConfig.getNode(accountsConfig, "//TwitterSocialAdapter/*/Account[@name='" + accountName + "']");
				if (accountConfig == null) throw new SocialAdapterException("Missing account: " + accountName +
					" in SocialAdapter: " + socialName);
				account = new TwitterSocialAdapterAccount(proxy);
				account.init(accountConfig);
				accounts.put(accountName, account);
			}
		} catch (XMLConfigException e) {
			logger.error("Error initializing account: " + accountName +
					" in SocialAdapter: " + socialName, e);
			throw new SocialAdapterException("Error initializing account: " + accountName +
					" in SocialAdapter: " + socialName, e);
		}
		return account;
	}

    @Override
	protected String getSocialName() {
		return socialName;
	}
    
    @Override
    public Set<String> getAccountNames(boolean authorized) {
    	HashSet<String> names = new HashSet<String>();
    	for (Iterator<String> i = accounts.keySet().iterator(); i.hasNext();) {
			SocialAdapterAccount account = accounts.get(i.next());
			if (account.isAuthorized()){
				names.add(i.toString());
			}
		}
    	return names;
    }
}
