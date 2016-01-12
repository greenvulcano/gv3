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
package it.greenvulcano.gvesb.datahandling.mongodb.dbobuilder;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.datahandling.DBOException;
import it.greenvulcano.gvesb.datahandling.DHResult;
import it.greenvulcano.gvesb.datahandling.DataHandlerException;
import it.greenvulcano.gvesb.datahandling.IDBOBuilder;
import it.greenvulcano.gvesb.datahandling.mongodb.IDBOMongo;
import it.greenvulcano.gvesb.datahandling.utils.AbstractRetriever;
import it.greenvulcano.gvesb.datahandling.utils.DiscardCause;
import it.greenvulcano.gvesb.gvdte.controller.DTEController;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.bin.Dump;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;

/**
 * <code>DBOMongoBuilder</code> is the class that holds the creation logic of IDBOMongo
 * objects. Its role is to invoke the DTE to make transformations and
 * initializing the IDBOMongo objects that will physically manipulate the Mongo DB. It's
 * based on a connection to the MongoDB using the <code>MongoClient</code>
 * for all the operations. This component, to handle the concurrent behavior, 
 * is designed as thread-safe module.
 * 
 * @version 3.5.0 May 18, 2015
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class DBOMongoBuilder implements IDBOBuilder
{
	/**
	 *
	 */
	public static final String DBO_MONGO_SERVER_ADDRESS = "DBO_MONGO_SERVER_ADDRESS";

	private Vector<IDBOMongo>              dboMongoList            = null;
	private Map<String, IDBOMongo>         dboMongoOutputMap       = null;

	/**
	 * Configured properties to eventually overwrite in the service call.
	 */
	private Map<String, String>       baseProps;

	private boolean                   resolveMetadata;

	private int                       internalIdx;

	private Node                      configurationNode  = null;

	private DTEController             dteController      = null;

	private String                    serverMongoURI     = null;

	private String                    serviceName        = null;

	private String                    outputDataName     = null;
	private String                    statsDataName      = null;
	private final static String       ALL_STATS          = "ALL";

	private static final Logger       logger             = GVLogger.getLogger(DBOMongoBuilder.class);

	private final Map<String, Object> dataCache          = new HashMap<String, Object>();

	private int                       makeDump           = DUMP_TEXT;

	/**
	 * @see it.greenvulcano.gvesb.datahandling.IDBOMongoBuilder#init(org.w3c.dom.Node)
	 */
	@Override
	public void init(Node builder) throws DataHandlerException
	{
		dboMongoList = new Vector<IDBOMongo>();
		dboMongoOutputMap = new HashMap<String, IDBOMongo>();
		baseProps = new HashMap<String, String>();
		String dboClassName = "";

		NMDC.push();
		try {
			NMDC.put("DH_SERVICE", "");
			logger.debug("DBOMongoBuilder initialized with node\n[" + XMLUtils.serializeDOM_S(builder) + "].");
			String sDump = XMLConfig.get(builder, "@make-dump", "text");
			if (sDump.equals("none")) {
				makeDump = DUMP_NONE;
			}
			else if (sDump.equals("hex")) {
				makeDump = DUMP_HEX;
			}
			else {
				makeDump = DUMP_TEXT;
			}
			serviceName = XMLConfig.get(builder, "@name");
			NMDC.put("DH_SERVICE", serviceName);

			serverMongoURI = XMLConfig.get(builder, "@mongo-server-uri");
			logger.debug("serverMongoURI = " + serverMongoURI);

			logger.debug("Listing for Mongo DBOs.");
			NodeList dbosNodes = XMLConfig.getNodeList(builder, "*[@type='dbo']");
			IDBOMongo idbo = null;
			for (int i = 0; i < dbosNodes.getLength(); i++) {
				Node dboNode = dbosNodes.item(i);
				dboClassName = XMLConfig.get(dboNode, "@class");

				idbo = (IDBOMongo) Class.forName(dboClassName).newInstance();
				idbo.init(dboNode);
				idbo.setServiceName(serviceName);

				dboMongoList.add(idbo);
				dboMongoOutputMap.put(idbo.getOutputDataName(), idbo);
				logger.debug("Added a Mongo IDBO class [" + dboClassName + "].");
			}

			outputDataName = XMLConfig.get(builder, "@output-data", idbo.getOutputDataName());
			// stats-data = ALL ensure to add all the statistics by DHResult
			statsDataName = XMLConfig.get(builder, "@output-stats", outputDataName);

			resolveMetadata = XMLConfig.getBoolean(builder, "DHVariables/@resolve-metadata-on-call", true);
			NodeList nlv = XMLConfig.getNodeList(builder, "DHVariables/DHVariable");
			if (nlv != null) {
				for (int i = 0; i < nlv.getLength(); i++) {
					Node nv = nlv.item(i);
					baseProps.put(XMLConfig.get(nv, "@name"),
							XMLConfig.get(nv, "@value", XMLConfig.get(nv, ".", "")).trim());
				}
			}
		}
		catch (XMLConfigException exc) {
			logger.error("Error reading configuration", exc);
			throw new DataHandlerException("Error reading configuration", exc);
		}
		catch (IllegalAccessException exc) {
			logger.error("Error accessing IDBO class '" + dboClassName + "'", exc);
			throw new DataHandlerException("Error accessing IDBO class '" + dboClassName + "'", exc);
		}
		catch (InstantiationException exc) {
			logger.error("Error instantiating IDBO class '" + dboClassName + "'", exc);
			throw new DataHandlerException("Error instantiating IDBO class '" + dboClassName + "'", exc);
		}
		catch (ClassNotFoundException exc) {
			logger.error("Error creating IDBO class '" + dboClassName + "'", exc);
			throw new DataHandlerException("Error creating IDBO class '" + dboClassName + "'", exc);
		}
		catch (Exception exc) {
			logger.error("Unhandled exception in IDBO initialization", exc);
			throw new DataHandlerException("Unhandled exception in IDBO initialization", exc);
		}
		finally {
			NMDC.pop();
		}
	}


	/**
	 * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#EXECUTE(java.lang.String,
	 *      java.lang.Object, java.util.Map)
	 */
	@Override
	public DHResult EXECUTE(String operation, Object object, Map<String, Object> params) throws DataHandlerException,
	InterruptedException {
		long start = System.currentTimeMillis();
		Map<String, Object> localParams = buildProps(params);
		NMDC.push();
		NMDC.put("DH_SERVICE", serviceName);
		logger.debug("Start executing EXECUTE [" + operation + "]\n\tParams    : " + localParams.toString());
		if (logger.isDebugEnabled() && (object != null) && (makeDump != DUMP_NONE)) {
			if (object instanceof byte[]) {
				if (makeDump == DUMP_HEX) {
					logger.debug("Input data: [\n" + new Dump((byte[]) object, -1) + "\n].");
				}
				else {
					logger.debug("Input data: [\n" + new String((byte[]) object) + "\n].");
				}
			}
			else if (object instanceof Node) {
				try {
					logger.debug("Input data: [\n" + XMLUtils.serializeDOM_S((Node) object) + "\n].");
				}
				catch (Exception exc) {
					logger.debug("Input data: [\nDUMP ERROR!!!!!\n].", exc);
				}
			}
			else {
				logger.debug("Input data: [\n" + object.toString() + "\n].");
			}
		}
		internalIdx = 0;

		MongoClient conn = null;
		ByteArrayOutputStream out = null;

		try {
			logger.debug("Searching for a new available connection to the MongoDB URI [" + serverMongoURI + "].");

			MongoClientURI connectionString = new MongoClientURI(serverMongoURI);
			conn = new MongoClient(connectionString);

			IDBOMongo idbo = firstDBO();
			DHResult dhr = idbo.getExecutionResult();
			dhr.setData(object);
			dataCache.put(idbo.getInputDataName(), dhr);
			while (hasNext()) {
				ThreadUtils.checkInterrupted(getClass().getSimpleName(), serviceName, logger);
				idbo = nextDBO();
				NMDC.push();
				try {
					NMDC.put("DH_DBO", idbo.getName());

					if (idbo.getForcedMode().equals(IDBOMongo.MODE_DB2JSON)) {
						out = new ByteArrayOutputStream();
						logger.debug("Start executing IDBOMongo [" + idbo.toString() + "] in forced DB2JSON mode.");
						idbo.execute(out, conn, localParams);
						logger.debug("End executing IDBO [" + idbo.toString()
								+ "] in forced DB2JSON mode. Execution time: " + getPartialTime(start));
						byte[] output = out.toByteArray();
						if (logger.isDebugEnabled() && (output != null) && (makeDump != DUMP_NONE)) {
							if (makeDump == DUMP_HEX) {
								logger.debug("Received data from DB: [\n" + new Dump(output, -1) + "\n].");
							}
							else {
								logger.debug("Received data from DB: [\n" + new String(output) + "\n].");
							}
						}
						dhr = idbo.getExecutionResult();
						dhr.setData(output);
						dataCache.put(idbo.getOutputDataName(), dhr);
					} else if(idbo.getForcedMode().equals(IDBOMongo.MODE_JSON2DB)) {
						logger.debug("Start executing IDBOMongo [" + idbo.toString() + "] in forced JSON2DB mode.");
						idbo.execute(object, conn, localParams);
						logger.debug("End executing IDBO [" + idbo.toString()
								+ "] in forced JSON2DB mode. Execution time: " + getPartialTime(start));
						dhr = idbo.getExecutionResult();
						// dhr.setData(output);
						dataCache.put(idbo.getOutputDataName(), dhr);
					}
				}
				finally {
					if (out != null) {
						try {
							out.close();
						}
						catch (IOException exc) {
							// Nothing to do
						}
					}
					NMDC.pop();
				}
			}

			dhr = (DHResult) dataCache.get(outputDataName);
			if (!dboMongoOutputMap.get(outputDataName).isReturnData()) {
				dhr.setData(null);
			}
			Object jsonFile = dhr.getData();

			// Manage statistics (if the @output-stats attribute is set)
			if (statsDataName != null) {
				if (statsDataName.equals(ALL_STATS)) {

					// statistics from all DBOs...
					internalIdx = 0;

					DHResult _dhr = null;
					long readAll = 0;
					long insertAll = 0;
					long updateAll = 0;
					long discardAll = 0;
					List<DiscardCause> discardCause = new ArrayList<DiscardCause>();
					long totalAll = 0;
					while (hasNext()) {
						idbo = nextDBO();
						_dhr = (DHResult) dataCache.get(idbo.getOutputDataName());

						readAll += _dhr.getRead();
						insertAll += _dhr.getInsert();
						updateAll += _dhr.getUpdate();
						discardAll += _dhr.getDiscard();
						discardCause.addAll(_dhr.getDiscardCauseList());
						totalAll += _dhr.getTotal();
					}
					_dhr = null;
					dhr.setRead(readAll);
					dhr.setInsert(insertAll);
					dhr.setUpdate(updateAll);
					dhr.setDiscard(discardAll);
					dhr.setDiscardCauseList(discardCause);
					dhr.setTotal(totalAll);
				}
				else if (!statsDataName.equals(outputDataName)) {
					// You need statistics from a different IDBO
					DHResult _dhr = (DHResult) dataCache.get(statsDataName);

					dhr.setRead(_dhr.getRead());
					dhr.setInsert(_dhr.getInsert());
					dhr.setUpdate(_dhr.getUpdate());
					dhr.setTotal(_dhr.getTotal());

					_dhr = null;

				}
			}

			if (logger.isDebugEnabled() && (jsonFile != null) && (makeDump != DUMP_NONE)) {
				if (jsonFile instanceof byte[]) {
					if (makeDump == DUMP_HEX) {
						logger.debug("Returning data: [\n" + new Dump((byte[]) jsonFile, -1) + "\n].");
					}
					else {
						logger.debug("Returning data: [\n" + new String((byte[]) jsonFile) + "\n].");
					}
				}
				else if (jsonFile instanceof Node) {
					try {
						logger.debug("Returning data: [\n" + XMLUtils.serializeDOM_S((Node) jsonFile) + "\n].");
					}
					catch (Exception exc) {
						logger.debug("Returning data: [\nDUMP ERROR!!!!!\n].", exc);
					}
				}
				else {
					logger.debug("Returning data: [\n" + jsonFile + "\n].");
				}
			}
			return dhr;
		}
		catch (MongoException exc) {
			throw new DataHandlerException("MongoException: " + exc.getMessage(), exc);
		}
		catch (Exception exc) {
			logger.error("Unhandled Exception", exc);
			ThreadUtils.checkInterrupted(exc);
			throw new DataHandlerException("Unhandled Exception: " + exc.getMessage(), exc);
		}
		finally {
			cleanup();
			try {
				conn.close();
			}
			catch (Exception exc) {
				// do nothing
			}
			logger.debug("End executing EXECUTE [" + operation + "]. Execution time: " + getPartialTime(start));
			NMDC.pop();
		}
	}

	/**
	 * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#cleanup()
	 */
	@Override
	public void cleanup()
	{
		dataCache.clear();
		internalIdx = 0;
		while (hasNext()) {
			IDBOMongo idbo = nextDBO();
			try {
				idbo.cleanup();
			}
			catch (Exception exc) {
				// do nothing
			}
		}
	}

	/**
	 * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#destroy()
	 */
	@Override
	public void destroy()
	{
		baseProps.clear();

		internalIdx = 0;
		while (hasNext()) {
			IDBOMongo idbo = nextDBO();
			try {
				idbo.destroy();
			}
			catch (Exception exc) {
				// do nothing
			}
		}
		// Static utility classes reset
		AbstractRetriever.cleanupAll();
	}

	/**
	 * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#getConfigurationNode()
	 */
	@Override
	public Node getConfigurationNode()
	{
		return configurationNode;
	}

	/**
	 * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#setConfigurationNode(org.w3c.dom.Node)
	 */
	@Override
	public void setConfigurationNode(Node configurationNode)
	{
		this.configurationNode = configurationNode;
	}

	/**
	 * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#setDteController(it.greenvulcano.gvesb.gvdte.controller.DTEController)
	 */
	@Override
	public void setDteController(DTEController dteController)
	{
		this.dteController = dteController;
	}

	private Object transform(IDBOMongo idbo, Object input, Map<String, Object> params) throws Exception
	{
		Object output = input;
		String transformation = idbo.getTransformation();
		if ((transformation != null) && !transformation.equals("")) {
			logger.debug("Transformation [" + transformation + "] execution using DTE.");
			output = dteController.transform(transformation, input, params);
		}
		else {
			logger.debug("No transformation for this IDBO.");
		}
		return output;
	}

	private IDBOMongo firstDBO()
	{
		return dboMongoList.get(0);
	}

	private IDBOMongo nextDBO()
	{
		return dboMongoList.get(internalIdx++);
	}

	private boolean hasNext()
	{
		return (internalIdx < dboMongoList.size());
	}

	private Map<String, Object> buildProps(Map<String, Object> props) throws DataHandlerException
	{
		try {
			Map<String, Object> allProps = new HashMap<String, Object>(baseProps);
			if (props != null) {
				allProps.putAll(props);
			}
			if (resolveMetadata) {
				boolean toDecode = true;
				while (toDecode) {
					toDecode = false;
					for (Entry<String, Object> entry : allProps.entrySet()) {
						String name = entry.getKey();
						String value = (String) entry.getValue();
						String nValue = PropertiesHandler.expand(value, allProps);
						if (!PropertiesHandler.isExpanded(nValue) && ((nValue != null) && (!nValue.equals(value)))) {
							toDecode = true;
						}
						allProps.put(name, nValue);
					}
				}
			}
			return allProps;
		}
		catch (Exception exc) {
			throw new DataHandlerException("Error building properties map", exc);
		}
	}

	private static String getPartialTime(long start)
	{

		long end = System.currentTimeMillis();
		long partial = end - start;
		int ms = (int) partial % 1000;
		String msec = Integer.toString(ms);
		if (ms < 10) {
			msec = "00" + ms;
		}
		else if (ms < 100) {
			msec = "0" + ms;
		}
		partial = (partial - ms) / 1000;

		int s = (int) partial % 60;
		String sec = Integer.toString(s);
		if (s < 10) {
			sec = "0" + s;
		}
		partial = (partial - s) / 60;

		int m = (int) partial % 3600;
		String min = Integer.toString(m);
		if (m < 10) {
			min = "0" + m;
		}
		return min + ":" + sec + "." + msec;
	}


	@Override
	public void XML2DB(String operation, byte[] file, Map<String, Object> params)
			throws DataHandlerException, InterruptedException {
		// DO NOTHING.
		throw new DBOException("Unsupported method - DBOMongoBuilder::XML2DB(String, byte[], Map)");
	}


	@Override
	public byte[] DB2XML(String operation, byte[] file,
			Map<String, Object> params) throws DataHandlerException,
			InterruptedException {

		// DO NOTHING.
		throw new DBOException("Unsupported method - DBOMongoBuilder::DB2XML(String, byte[], Map)");
	}


	@Override
	public byte[] CALL(String operation, byte[] file, Map<String, Object> params)
			throws DataHandlerException, InterruptedException {
		// DO NOTHING.
		throw new DBOException("Unsupported method - DBOMongoBuilder::CALL(String, byte[], Map)");
	}
}
