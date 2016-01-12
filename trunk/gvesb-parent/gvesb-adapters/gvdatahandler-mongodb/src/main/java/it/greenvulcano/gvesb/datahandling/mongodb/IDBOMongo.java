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
package it.greenvulcano.gvesb.datahandling.mongodb;

import it.greenvulcano.gvesb.datahandling.DBOException;
import it.greenvulcano.gvesb.datahandling.DHResult;

import java.io.OutputStream;
import java.util.Map;

import org.w3c.dom.Node;

import com.mongodb.MongoClient;

/**
 * Interface implemented from classes that interact with the Mongo DB.
 *
 * @version 3.5.0 Mar 30, 2015
 * @author GreenVulcano Developer Team
 *
 *
 */
public interface IDBOMongo
{
	/**
    *
    */
   public static final String MODE_DB2JSON = "db2json";
   
   public static final String MODE_JSON2DB = "json2db";
	
    /**
     * Method to implement to configure the object that implements this
     * interface.
     *
     * @param config
     *        XML node that contains configuration parameters.
     * @throws DBOException
     *         whenever the configuration is wrong.
     */
    public void init(Node config) throws DBOException;

    /**
     * Method <i>execute</i> implemented by IDBOMongo classes having update
     * interaction with Mongo DB.
     *
     * @param input
     *        data to insert or update on Mongo DB.
     * @param mongoClient
     *        mongoClient instance used to connect to the Mongo DB.
     * @param props
     *        parameters to substitute in the MongoDB statement to execute.
     * @throws DBOException
     *         if any error occurs.
     */
    public void execute(Object input, MongoClient mongoClient, Map<String, Object> props) throws DBOException, 
            InterruptedException;

    /**
     * Method <i>execute</i> implemented by IDBOMongo classes having read interaction
     * with DB.
     *
     * @param data
     *        data returned from the Mongo DB.
     * @param mongoClient
     *        mongoClient instance used to connect to the Mongo DB.
     * @param props
     *        parameters to substitute in the MongoDB statement to execute.
     * @throws DBOException
     *         if any error occurs.
     */
    public void execute(OutputStream data, MongoClient mongoClient, Map<String, Object> props) throws DBOException, 
            InterruptedException;

    /**
     * @param dataIn
     * @param dataOut
     * @param mongoClient
     *        mongoClient instance used to connect to the Mongo DB.
     * @param props
     * @throws DBOException
     */
    public void execute(Object dataIn, OutputStream dataOut, MongoClient mongoClient, Map<String, Object> props)
            throws DBOException, InterruptedException;

    /**
     * @return the configured name of this IDBO
     */
    public String getName();

    /**
     * Returns the transformation's name to execute.
     *
     * @return the transformation's name to execute.
     */
    public String getTransformation();

    /**
     * Executes cleanup operations.
     */
    public void cleanup();

    /**
     * Executes finalization operations.
     */
    public void destroy();

    /**
     * @param serviceName
     */
    public void setServiceName(String serviceName);

    /**
     * @return the input data name
     */
    public String getInputDataName();

    /**
     * @return the output data name
     */
    public String getOutputDataName();

    /**
     * @return if forced mode enabled
     */
    public String getForcedMode();

    /**
     * @return the execution result
     */
    public DHResult getExecutionResult();

    /**
     * @return if returns data.
     */
    public boolean isReturnData();
}

