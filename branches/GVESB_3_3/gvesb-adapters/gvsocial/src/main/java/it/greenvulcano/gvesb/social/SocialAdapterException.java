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
 * Exception class for errors in Social adapter operations
 * 
 * @version 3.3.0 Sep, 2012
 * @author GreenVulcano Developer Team
 *
 */
public class SocialAdapterException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5202706604374077432L;

	public SocialAdapterException(String message, Throwable cause) {
		super(message, cause);
	}

	public SocialAdapterException(String message) {
		super(message);
	}

	
}
