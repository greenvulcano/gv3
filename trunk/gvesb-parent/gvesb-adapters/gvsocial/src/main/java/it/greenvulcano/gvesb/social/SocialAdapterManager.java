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

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.log.GVLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * 
 * Manage social networks accounts.
 * 
 * @version 3.3.0 Sep, 2012
 * @author GreenVulcano Developer Team
 */
public class SocialAdapterManager implements ConfigurationListener {

    private static Logger logger = GVLogger.getLogger(SocialAdapterManager.class);
	public static final String CFG_FILE="GVSocialAdapter-Configuration.xml";
	private static SocialAdapterManager instance;
	private Map<String, SocialAdapter> adapters;

	private SocialAdapterManager() {
		adapters = new HashMap<String, SocialAdapter>();
	}

	public static synchronized SocialAdapterManager getInstance() {
		if (instance == null) {
			instance = new SocialAdapterManager();
	        XMLConfig.addConfigurationListener(instance, CFG_FILE);
		}
		return instance;
	}

	/** This method returns the required {@link SocialAdapter} already initialized. 
	 * 
	 * @param socialName l'attributo social del nodo SocialAdapter
	 * @return {@link SocialAdapter}
	 */
	private SocialAdapter getAdapter(String socialName) {
		SocialAdapter adapter = null;
		try {
			adapter = adapters.get(socialName);
			if (adapter == null) {
	    		// lettura configurazione adapter
		    	Node adpNode = XMLConfig.getNode(CFG_FILE, "//*[@type='social-adapter' and @social='" + socialName + "']");
				adapter = (SocialAdapter) Class.forName(XMLConfig.get(adpNode, "@class")).newInstance();
				adapter.init(adpNode);
				// registrazione in HashMap
				adapters.put(socialName, adapter);
				logger.info("SocialAdapterManager:getInstance: " + socialName);
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return adapter;
	}

	/**
	 * Interface method towards the social platform.
	 * 
	 * @param buffer data to send and operation to invoke
	 * @param socialName network to invoke
	 * @return response data
	 * @throws SocialAdapterException 
	 */
	public GVBuffer execute(GVBuffer buffer, String socialName) throws SocialAdapterException{
		SocialAdapter adapter = getAdapter(socialName);
		adapter.execute(buffer);
		return buffer;
	}

	/**
	 * Interface method towards the social platform. Unlike execute(), this method
	 * is used to call directly one of the methods exposed by the platform, without
	 * xml composition and reflection call.
	 * 
	 * @param op operation to invoke 
	 * @throws SocialAdapterException 
	 */
	public void directExecute(SocialOperation op) throws SocialAdapterException{
		SocialAdapter adapter = getAdapter(op.getSocialName());
		adapter.directExecute(op);
	}

	/**
	 * Gives a {@link List} of account names
	 * @param authorized true if the account has been already authorized
	 * @return the accounts list
	 */
	public Set<String> getAccountsList(boolean authorized, String socialName){
		return getAdapter(socialName).getAccountNames(authorized);
	}

	public Tokens getConsumerTokens(String socialName, String accountName) throws SocialAdapterException{
		SocialAdapterAccount account = getSocialAccount(socialName, accountName);
		return account.getConsumerTokens();
	}

	/**
	 * 
	 * @param PIN
	 * @param socialName
	 * @param accountName
	 * @throws SocialAdapterException
	 */
	public void setPINAndSave(String PIN, String socialName, String accountName) throws SocialAdapterException{
		SocialAdapterAccount account = getSocialAccount(socialName, accountName);
		account.setPINAndSave(PIN);
	}

	public Tokens getRequestTokenAndURL(String socialName, String accountName) throws SocialAdapterException{
		SocialAdapterAccount account = getSocialAccount(socialName, accountName);
		return account.getRequestTokenAndURL();
	}

	private SocialAdapterAccount getSocialAccount(String socialName, String accountName) throws SocialAdapterException {
		SocialAdapter adapter = this.getAdapter(socialName);
		SocialAdapterAccount account = null;
		try {
			account = adapter.getAccount(accountName);
		} catch (SocialAdapterException e) {
			logger.error("Error retrieving Account name: " + accountName + ".", e);
			throw new SocialAdapterException("Error retrieving Account name: " + accountName + ".", e);
		}
		return account;
	}

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.configuration.ConfigurationListener#configurationChanged
     * (it.greenvulcano.configuration.ConfigurationEvent)
     */
    @Override
    public void configurationChanged(ConfigurationEvent evt)
    {
        logger.debug("BEGIN - Operation(reload Configuration)");
        if ((evt.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && evt.getFile().equals(CFG_FILE)) {
            destroy();
        }
        logger.debug("END - Operation(reload Configuration)");
    }

    public void destroy()
    {
        logger.debug("BEGIN - Destroying SocialAdapterManager");
        try {
            for (Entry<String, SocialAdapter> entry : adapters.entrySet()) {
                try {
                    entry.getValue().destroy();
                }
                catch (Exception exc) {
                    logger.error("Error destroying SocialAdapter[" + entry.getKey() + "]", exc);
                }
            }
            adapters.clear();
        }
        catch (Exception exc) {
            // TODO: handle exception
        }
        logger.debug("END - Destroying SocialAdapterManager");
    }
}
