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
package it.greenvulcano.gvesb.virtual.social.twitter;

import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.OperationKey;

/**
 * Class defining a call to an operation on Twitter.
 * 
 * @version 3.3.0 Sep, 2012
 * @author GreenVulcano Developer Team
 */
public abstract class TwitterSocialCallOperation implements CallOperation{

	private OperationKey key = null;
	private String account;
    private String name = null;
    
    public String getAccount() {
		return account;
	}

	public String getName() {
		return name;
	}

	@Override
	public void init(Node node) throws InitializationException {
		try{
			name = XMLConfig.get(node, "@name");
			account = XMLConfig.get(node, "@account");
		}
        catch (Exception exc) {
            throw new InitializationException("GV_CONF_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
	}

	public TwitterSocialCallOperation() {
		super();
	}

	/**
	 * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
	 */
	@Override
	public void cleanUp() {
	    // do nothing
	}

	/**
	 * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
	 */
	@Override
	public void destroy() {
	    // do nothing
	}

	/**
	 * @see it.greenvulcano.gvesb.virtual.Operation#getServiceAlias(it.greenvulcano.gvesb.buffer.GVBuffer)
	 */
	@Override
	public String getServiceAlias(GVBuffer gvBuffer) {
	    return gvBuffer.getService();
	}

	/**
	 * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
	 */
	@Override
	public void setKey(OperationKey key) {
	    this.key = key;
	}

	/**
	 * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
	 */
	@Override
	public OperationKey getKey() {
	    return key;
	}

}