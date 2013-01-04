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
 * Represents a bean containing Proxy settings.
 * 
 * @version 3.3.0 Sep, 2012
 * @author mb
 * 
 * 
 */
public class SocialAdapterProxy {
	private String httpProxyHost;
	private int httpProxyPort;
	private String httpProxyUser;
	private String httpProxyPassword;

	public SocialAdapterProxy(String httpProxyHost, int httpProxyPort,
			String httpProxyUser, String httpProxyPassword) {
		super();
		this.httpProxyHost = httpProxyHost;
		this.httpProxyPort = httpProxyPort;
		this.httpProxyUser = httpProxyUser;
		this.httpProxyPassword = httpProxyPassword;
	}

	public SocialAdapterProxy() {
		super();
	}

	public String getHttpProxyHost() {
		return httpProxyHost;
	}

	public void setHttpProxyHost(String httpProxyHost) {
		this.httpProxyHost = httpProxyHost;
	}

	public int getHttpProxyPort() {
		return httpProxyPort;
	}

	public void setHttpProxyPort(int httpProxyPort) {
		this.httpProxyPort = httpProxyPort;
	}

	public String getHttpProxyUser() {
		return httpProxyUser;
	}

	public void setHttpProxyUser(String httpProxyUser) {
		this.httpProxyUser = httpProxyUser;
	}

	public String getHttpProxyPassword() {
		return httpProxyPassword;
	}

	public void setHttpProxyPassword(String httpProxyPassword) {
		this.httpProxyPassword = httpProxyPassword;
	}

}
