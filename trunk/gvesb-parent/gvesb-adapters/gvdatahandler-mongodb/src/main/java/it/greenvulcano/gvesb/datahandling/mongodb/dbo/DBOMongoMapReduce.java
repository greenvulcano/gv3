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
package it.greenvulcano.gvesb.datahandling.mongodb.dbo;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.datahandling.DBOException;
import it.greenvulcano.gvesb.datahandling.mongodb.utils.MongoDBError;
import it.greenvulcano.gvesb.datahandling.mongodb.utils.MongoExceptionHandler;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.thread.ThreadUtils;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceCommand.OutputType;
import com.mongodb.MapReduceOutput;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;


/**
 * IDBOMongo Class specialized to parse the input Json javascript Map and Reduce Functions in order to execute MapReduceCommand on the mongo DB collection.
 * Updated to use named parameters in statement.
 *
 * @version 3.5.0 Jun 20, 2015
 * @author GreenVulcano Developer Team
 */
public class DBOMongoMapReduce extends AbstractMongoDBO
{

	private static final Logger logger = GVLogger.getLogger(DBOMongoMapReduce.class);

	/**
	 * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#init(org.w3c.dom.Node)
	 */
	@Override
	public void init(Node config) throws DBOException
	{
		super.init(config);
		isInsert = true;
		try {
			forcedMode = XMLConfig.get(config, "@force-mode", MODE_DB2JSON);
			isReturnData = XMLConfig.getBoolean(config, "@return-data", true);
			
			NodeList stmts = XMLConfig.getNodeList(config, "MDBMapReduce");
			String id = null;
			Node stmt;
			for (int i = 0; i < stmts.getLength(); i++) {
				stmt = stmts.item(i);
				id = XMLConfig.get(stmt, "@id");
				MDBMapReduce mdbMapReduce = new MDBMapReduce();
				mdbMapReduce.init(stmt);
				
				if (id == null) {
					id = Integer.toString(i);
				}
				statements.put(id, mdbMapReduce);
			}
			
			if (statements.isEmpty()) {
				throw new DBOException("Empty/misconfigured statements list for [" + getName() + "/" + dboclass + "]");
			}
		}
		catch (DBOException exc) {
			throw exc;
		}
		catch (Exception exc) {
			logger.error("Error reading configuration of [" + getName() + "/" + dboclass + "]", exc);
			throw new DBOException("Error reading configuration of [" + getName() + "/" + dboclass + "]", exc);
		}
	}


	/**
	 * @see it.greenvulcano.gvesb.datahandling.mongodb.IDBOMongo#execute(java.io.OutputStream, com.mongodb.MongoClient, java.util.Map)
	 */
	@Override
	public void execute(OutputStream dataOut, MongoClient conn,
			Map<String, Object> props) throws DBOException,
			InterruptedException {
		try {
			prepare();
			logger.debug("Begin execution of DB data map reduce function through " + dboclass);

			resultMessage = new StringBuffer();
			resultStatus = STATUS_OK;

			Map<String, Object> localProps = buildProps(props);
			logProps(localProps);

			boolean outStmtStarted = false;
			StringBuilder rowSetResult = new StringBuilder();
			int stmtCounter = 0;
			for (Entry<String, IDBOOperation> entry : statements.entrySet()) {
				ThreadUtils.checkInterrupted(getClass().getSimpleName(), getName(), logger);
				Object idStatement = entry.getKey();
				IDBOOperation stmt = entry.getValue();

				if (stmt != null) {
					mongoStatementInfo = new MongoStatementInfo(idStatement.toString(), stmt, localProps, conn);
					
					logger.debug("MapReduceCommand START for statement id: " + mongoStatementInfo.getId());
					logger.debug("Executing map reduce:\n" + Arrays.toString(mongoStatementInfo.getJsonStatement()));

					com.mongodb.DB database = getInternalConn(conn).getDB(mongoStatementInfo.getMongoDBName());
					com.mongodb.DBCollection collection = database.getCollection(mongoStatementInfo.getMongoDBCollection());
					
					String mapFunction    = null;
					String reduceFunction = null;
					String queryFilter    = null;
					String outputType     = null;
					String outputCollection  = null;
					if(mongoStatementInfo.getJsonStatement() != null && mongoStatementInfo.getJsonStatement().length >= 2) {
						mapFunction  = mongoStatementInfo.getJsonStatement()[0];
						reduceFunction = mongoStatementInfo.getJsonStatement()[1];
						if(mongoStatementInfo.getJsonStatement().length >= 3 && mongoStatementInfo.getJsonStatement()[2] != null) {
							queryFilter = mongoStatementInfo.getJsonStatement()[2];
						}
						if(mongoStatementInfo.getJsonStatement().length >= 4 && mongoStatementInfo.getJsonStatement()[3] != null) {
							outputType = mongoStatementInfo.getJsonStatement()[3];
						}						
						if(mongoStatementInfo.getJsonStatement().length >= 5 && mongoStatementInfo.getJsonStatement()[4] != null) {
							outputCollection = mongoStatementInfo.getJsonStatement()[4];
						}

						logger.debug("statement id: " + mongoStatementInfo.getId() + "\n mapFunction:\n" + mapFunction + "reduceFunction:\n" + reduceFunction);
					}
					
					DBObject dbObjectFilter = (DBObject)JSON.parse(queryFilter);
					
					if(mapFunction == null || reduceFunction == null) {
						throw new DBOException("MongoDB Statement error configuration. No <map-function> or <reduce-function> are present in XML Configuration.");
					}					
					
					if( MapReduceCommand.OutputType.INLINE == MapReduceCommand.OutputType.valueOf(outputType) ) {
						//RETURN <RowSet> structured output
						MapReduceCommand cmd = new MapReduceCommand(collection, mapFunction, reduceFunction, null, MapReduceCommand.OutputType.INLINE, dbObjectFilter);

						MapReduceOutput out = collection.mapReduce(cmd);
						
						if(!outStmtStarted) {
							outStmtStarted = true;
							rowSetResult = new StringBuilder("{ \"" + ROWSET_NAME + "\"" + " : { \"" + DATA_NAME + "\" : [ ");
						}
						
						StringBuilder data = new StringBuilder("{ \"" + ID_NAME + "\" : " + idStatement + " , \"" + ROW_NAME + "\" : [ ");
						
						int k = 0;
						for (DBObject element : out.results()) {
							if( k > 0 ) data.append(", ");
							data.append(element.toString());
							rowCounter += 1;
							k++;
						}
						
						//CLOSE DATA FOR id
						data.append(" ] } ");
						
						if(stmtCounter > 0) rowSetResult.append(", ");
						rowSetResult.append(data);
						stmtCounter++;
						
					} else {
						if( outputCollection == null ) throw new DBOException("MongoDB Statement error configuration. No <output-collection> defined in XML Configuration. If output-type <> INLINE this parameter is mandatory.");
						
						MapReduceCommand.OutputType outputTypeOption = MapReduceCommand.OutputType.valueOf(outputType);
						MapReduceCommand cmd = new MapReduceCommand(collection, mapFunction, reduceFunction, outputCollection, outputTypeOption, dbObjectFilter);
						collection.mapReduce(cmd);
					}
					
					if(outStmtStarted) {
						//endRowSetData();
						rowSetResult.append(" ] } }");

						dataOut.write(rowSetResult.toString().getBytes()); // output.

						dhr.setRead(rowCounter);
					}

					logger.debug("MapReduceCommand DONE for statement id: " + mongoStatementInfo.getId());
				}
			}

			logger.debug("End execution of MongoDB Map Reduce through " + dboclass);
		}
		catch (MongoException exc) {
			rowDisc++;
						
			MongoDBError oraerr = MongoExceptionHandler.handleSQLException(exc);
			if (isBlockingError(oraerr.getErrorType())) {
				resultStatus = STATUS_KO;
				logger.error("Mongo Statement Informations:\n" + mongoStatementInfo);
				logger.error("MongoException configured as blocking error for the IDBO '" + serviceName + ".", exc);
				throw new DBOException("MongoException configured as blocking error class " + ": " + exc.getMessage(), exc);
			}

			resultMessage.append("MongoException error executing MapReduce command ").append(": ").append(exc.getMessage());
			resultMessage.append("MongoDB Statement Informations:\n").append(mongoStatementInfo);
			resultStatus = STATUS_PARTIAL;
		}
		catch (InterruptedException exc) {
			resultStatus = STATUS_KO;
			logger.error("DBO[" + dboclass + "] interrupted", exc);
			throw new DBOException("DBO[" + dboclass + "] interrupted", exc);
		}
		catch(Exception exc) {
			resultStatus = STATUS_KO;
			logger.error("Exception during DB data insert through " + dboclass, exc);
			throw new DBOException("DBO[" + dboclass + "] Exception during DB data insert", exc);
		}
		finally {
			rowCounter++;
			if (resultStatus != STATUS_OK) {
				logger.warn("Partial execution for service " + serviceName + ":" + resultMessage.toString() + ".");
				logger.warn("Elaboration result for service '" + serviceName + "':\nrecord insert  \t[" + rowInsOk
						+ "]\nrecord updated\t[" + rowUpdOk + "]\nrecord discarded  \t["
						+ Long.toString(rowCounter - (rowInsOk + rowUpdOk)) + "]\nrecord total  \t["
						+ Long.toString(rowInsOk + rowUpdOk + rowDisc) + "].");

			}
			else {
				logger.debug("Elaboration result for service '" + serviceName + "':\nrecord insert  \t[" + rowInsOk
						+ "]\nrecord updated\t[" + rowUpdOk + "]\nrecord total  \t["
						+ Long.toString(rowInsOk + rowUpdOk + rowDisc) + "].");
			}

			dhr.setTotal(rowInsOk + rowUpdOk + rowDisc);
			dhr.setInsert(rowInsOk);
			dhr.setUpdate(rowUpdOk);
			dhr.setDiscard(rowDisc);
		}
	}


	/**
	 * Unsupported method for this IDBO.
	 * 
	 * @see it.greenvulcano.gvesb.datahandling.mongodb.IDBOMongo#execute(java.io.OutputStream, com.mongodb.MongoClient, java.util.Map)
	 */
	public void execute(OutputStream dataOut, Object input, MongoClient conn, Map<String, Object> props) throws DBOException, 
		InterruptedException {
		prepare();
		throw new DBOException("Unsupported method - DBOMongoDelete::execute(OutputStream, MongoClient, HashMap)");
	}
	
	
	public class MDBMapReduce implements IDBOOperation {
		private String id;
		private String dbName;
		private String collection; //input collection.
		private String mapFunction;
		private String reduceFunction;
		private String filter;
		private String outputCollection;
		private MapReduceCommand.OutputType outputType; 
		
		
		public void init(Node config) throws XMLConfigException {
			this.id = XMLConfig.get(config, "@id");
			this.dbName = XMLConfig.get(config, "@db");
			this.collection = XMLConfig.get(config, "@input-collection");
			this.filter = XMLConfig.get(config, "filter");
			this.mapFunction = XMLConfig.get(config, "map-function");
			this.reduceFunction = XMLConfig.get(config, "reduce-function");
			this.outputCollection = XMLConfig.get(config, "@output-collection");
			this.outputType = MapReduceCommand.OutputType.valueOf(XMLConfig.get(config, "output-type"));
			
			if( outputType == null ) throw new XMLConfigException("MongoDB Statement error configuration. No <output-type> defined in XML Configuration. This parameter is mandatory.");
			else if(outputType != MapReduceCommand.OutputType.INLINE && outputCollection == null) throw new XMLConfigException("MongoDB Statement error configuration. No <output-collection> defined in XML Configuration. If output-type <> INLINE this parameter is mandatory.");
		}
		
		public String[] getComponents() {
			String[] st = new String[5];
			st[0] = mapFunction;
			st[1] = reduceFunction;
			st[2] = filter;
			st[3] = outputType.toString();
			st[4] = outputCollection;
			
			return st;
		}
				
		public MDBMapReduce() {
			super();
		}

		public MDBMapReduce(String id, String dbName, String collection,
				String mapFunction, String reduceFunction, String filter,
				String outputCollection, OutputType outputType) {
			super();
			this.id = id;
			this.dbName = dbName;
			this.collection = collection;
			this.mapFunction = mapFunction;
			this.reduceFunction = reduceFunction;
			this.filter = filter;
			this.outputCollection = outputCollection;
			this.outputType = outputType;
		}

		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getDbName() {
			return dbName;
		}
		public void setDbName(String dbName) {
			this.dbName = dbName;
		}
		public String getCollection() {
			return collection;
		}
		public void setCollection(String collection) {
			this.collection = collection;
		}
		public String getFilter() {
			return filter;
		}
		public void setFilter(String filter) {
			this.filter = filter;
		}
		
		public String getMapFunction() {
			return mapFunction;
		}

		public void setMapFunction(String mapFunction) {
			this.mapFunction = mapFunction;
		}

		public String getReduceFunction() {
			return reduceFunction;
		}

		public void setReduceFunction(String reduceFunction) {
			this.reduceFunction = reduceFunction;
		}

		public String getOutputCollection() {
			return outputCollection;
		}

		public void setOutputCollection(String outputCollection) {
			this.outputCollection = outputCollection;
		}
				
		public MapReduceCommand.OutputType getOutputType() {
			return outputType;
		}

		public void setOutputType(MapReduceCommand.OutputType outputType) {
			this.outputType = outputType;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("MDBMapReduce [id=");
			builder.append(id);
			builder.append(", dbName=");
			builder.append(dbName);
			builder.append(", collection=");
			builder.append(collection);
			builder.append(", mapFunction=");
			builder.append(mapFunction);
			builder.append(", reduceFunction=");
			builder.append(reduceFunction);
			builder.append(", filter=");
			builder.append(filter);
			builder.append(", outputCollection=");
			builder.append(outputCollection);
			builder.append(", outputType=");
			builder.append(outputType);
			builder.append("]");
			return builder.toString();
		}

		@Override
		public String getData() {
			// NOTHING: Unsopported method.
			return null;
		}

		@Override
		public String getSetCondition() {
			// NOTHING: Unsopported method.
			return null;
		}
	}

	/**
	 * Unsupported method for this IDBO. 
	 * @see it.greenvulcano.gvesb.datahandling.mongodb.dbo.AbstractMongoDBO#execute(java.lang.Object, com.mongodb.MongoClient, java.util.Map)
	 */
	@Override
	public void execute(Object input, MongoClient mongoClient,
			Map<String, Object> props) throws DBOException,
			InterruptedException {
		prepare();
		throw new DBOException("Unsupported method - DBOMongoMapReduceCommand::execute(Object, Connection, Map)");
		
	}
}
