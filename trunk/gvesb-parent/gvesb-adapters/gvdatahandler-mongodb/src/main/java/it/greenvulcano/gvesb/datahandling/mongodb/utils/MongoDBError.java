/*
 * Copyright (c) 2009-2016 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.datahandling.mongodb.utils;

import it.greenvulcano.log.GVLogger;

import org.apache.log4j.Logger;

import com.mongodb.MongoException;


/**
 * MongoDBError class
 *
 * @version 3.5.0 Jun 18, 2015
 * @author GreenVulcano Developer Team
 *
 *
 */
public class MongoDBError
{
	/**
	 *
	 */
	public static final int     PLATFORM_ERROR   = 1;

	/**
	 *
	 */
	public static final int     DATA_ERROR       = 2;

	/**
	 *
	 */
	public static final int     CONSTRAINT_ERROR = 3;

	/**
	 *
	 */
	public static final int     STATEMENT_ERROR  = 4;

	/**
	 *
	 */
	public static final int     SECURITY_ERROR   = 5;

	private int                 errorType        = -1;

	private String              module           = "";

	private MongoException      mongoException     = null;

	private static final Logger logger           = GVLogger.getLogger(MongoDBError.class);

	/**
	 * @param code
	 */
	public MongoDBError(int code)
	{
		errorType = code;
	}

	/**
	 * @param code
	 * @param mod
	 */
	public MongoDBError(int code, String mod)
	{
		errorType = code;
		module = mod;
	}

	/**
	 * @return the error type
	 */
	public int getErrorType()
	{
		return errorType;
	}

	void setErrorType(int errorType)
	{
		this.errorType = errorType;
	}

	/**
	 * @return the module
	 */
	public String getModule()
	{
		return module;
	}

	void setModule(String module)
	{
		this.module = module;
	}

	void setMongoException(MongoException mongoException)
	{
		this.mongoException = mongoException;
	}

	/**
	 *
	 */
	 public void printLoggerInfo()
	 {
		 logger.error("Exception during the execution of ${dboclass}.", mongoException);
	 }
}
