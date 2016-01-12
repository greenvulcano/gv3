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
import it.greenvulcano.gvesb.datahandling.utils.DiscardCause;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.thread.ThreadUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.mongodb.MapReduceCommand.OutputType;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

/**
 * IDBOMongo Class specialized in selecting data from the DB.
 * 
 * 
 * @version 3.5.0 May 28, 2015
 * @author GreenVulcano Developer Team
 */
public class DBOMongoSelect extends AbstractMongoDBO
{
	private static final Logger                      logger                 = GVLogger.getLogger(DBOMongoSelect.class);

	private StringBuilder rowSetResult = null;

	/**
	 *
	 */
	public DBOMongoSelect()
	{
		super();
	}

	/**
	 * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#init(org.w3c.dom.Node)
	 */
	@Override
	public void init(Node config) throws DBOException
	{
		super.init(config);
		try {
			forcedMode = XMLConfig.get(config, "@force-mode", MODE_DB2JSON);
			isReturnData = XMLConfig.getBoolean(config, "@return-data", true);

			NodeList stmts = XMLConfig.getNodeList(config, "MDBSelect");
			String id = null;
			Node stmt;
			for (int i = 0; i < stmts.getLength(); i++) {
				stmt = stmts.item(i);
				id = XMLConfig.get(stmt, "@id");
				MDBSelect mdbSelect = new MDBSelect();
				mdbSelect.init(stmt);
				
				if (id == null) {
					id = Integer.toString(i);
				}
				statements.put(id, mdbSelect);
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
	 * Unsupported method for this IDBO. 
	 * @see it.greenvulcano.gvesb.datahandling.mongodb.dbo.AbstractMongoDBO#execute(java.lang.Object, com.mongodb.MongoClient, java.util.Map)
	 */
	@Override
	public void execute(Object input, MongoClient conn, Map<String, Object> props) throws DBOException
	{
		prepare();
		throw new DBOException("Unsupported method - DBOSelect::execute(Object, Connection, Map)");
	}

	@Override
	public void execute(OutputStream dataOut, MongoClient conn, Map<String, Object> props) throws DBOException,
	InterruptedException {

		try {
			prepare();
			logger.debug("Begin execution of DB data read through " + dboclass);

			resultMessage = new StringBuffer();
			resultStatus = STATUS_OK;

			Map<String, Object> localProps = buildProps(props);
			logProps(localProps);

			startRowSetData();
			//rowSetResult = new StringBuilder("{ \"" + ROWSET_NAME + "\"" + " : { \"" + DATA_NAME + "\" : [ ");

			int stmtCounter = 0;
			for (Entry<String, IDBOOperation> entry : statements.entrySet()) {
				ThreadUtils.checkInterrupted(getClass().getSimpleName(), getName(), logger);
				Object key = entry.getKey();
				IDBOOperation stmt = entry.getValue();

				//ESTRAPOLARE DB_NAME|DB_COLLECTION DALLO STATEMENT per eseguire lo script.
				StringBuilder data = new StringBuilder();
				if (stmt != null) {
					mongoStatementInfo = new MongoStatementInfo(key.toString(), stmt, localProps, conn);

					logger.debug("Executing select:\n" + Arrays.toString(mongoStatementInfo.getJsonStatement()));

					MongoDatabase database = getInternalConn(conn).getDatabase(mongoStatementInfo.getMongoDBName());
					MongoCollection<org.bson.Document> collection = database.getCollection(mongoStatementInfo.getMongoDBCollection());

					String docToSelect = null;
					if(mongoStatementInfo.getJsonStatement() != null) {
						if( mongoStatementInfo.getJsonStatement().length > 0) {
							docToSelect = mongoStatementInfo.getJsonStatement()[0];
						}
					}
					
					if(docToSelect == null) throw new DBOException("MongoDB Statement error configuration. No filter statement is present in XML Configuration.");
					
					org.bson.Document docJsonSelect = org.bson.Document.parse(docToSelect);
					MongoCursor<org.bson.Document> cursor = collection.find(docJsonSelect).iterator();

					data = new StringBuilder("{ \"" + ID_NAME + "\" : " + key + " , \"" + ROW_NAME + "\" : [ ");
					try {
						int k = 0;
						while (cursor.hasNext()) {
							if( k > 0 ) data.append(", ");

							String element = cursor.next().toJson();
							data.append(element);
							rowCounter += 1;
							k++;
						}                        	
					} finally {
						if (cursor != null) {
							try {
								cursor.close();
							}
							catch (Exception exc) {
								// do nothing
							}
							cursor = null;
						}
					}

					//CLOSE DATA FOR id
					data.append(" ] } "); 
				}

				if(stmtCounter > 0) rowSetResult.append(", ");
				rowSetResult.append(data);
				stmtCounter++;
			}

			endRowSetData();
			//rowSetResult.append(" ] } }");

			//byte[] dataDOM = parser.serializeDOMToByteArray(doc);
			//dataOut.write(dataDOM);
			dataOut.write(rowSetResult.toString().getBytes()); // output.

			dhr.setRead(rowCounter);

			logger.debug("End execution of DB data read through " + dboclass);
		}
		catch (IOException exc) {
			resultStatus = STATUS_KO;
	        logger.error("Error on execution of " + dboclass + " with name [" + getName() + "]", exc);
	        logger.error("MongoDB Statement Informations:\n" + mongoStatementInfo);
	        throw new DBOException("Error on execution of " + dboclass + " with name [" + getName() + "]: " + exc.getMessage(), exc);
	    }
		catch (InterruptedException exc) {
			resultStatus = STATUS_KO;
			logger.error("DBO[" + getName() + "] interrupted", exc);
			throw exc;
		}
		catch (SAXException exc) {
			resultStatus = STATUS_KO;
	        logger.error("Error on execution of " + dboclass + " with name [" + getName() + "]", exc);
	        logger.error("MongoDB Statement Informations:\n" + mongoStatementInfo);
	        ThreadUtils.checkInterrupted(exc);
	        throw new DBOException("Error on execution of " + dboclass + " with name [" + getName() + "]: " + exc.getMessage(), exc);
	    }
		catch (Exception exc) {
			resultStatus = STATUS_KO;
	        logger.error("Error on execution of " + dboclass + " with name [" + getName() + "]", exc);
	        logger.error("MongoDB Statement Informations:\n" + mongoStatementInfo);
	        String msg = "" + exc.getMessage() + " - XSL Message: " + currentXSLMessage;
	        dhr.addDiscardCause(new DiscardCause(rowCounter, msg));
	        throw new DBOException(msg, exc);
	    }
		finally {
			rowSetResult = null;
			if (resultStatus != STATUS_OK) {
	            logger.warn("Partial select for service " + serviceName + ":" + resultMessage.toString() + ".");
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
		}
	}
	
	private void startRowSetData() {
		//START RowSet
		this.rowSetResult = new StringBuilder("{ \"" + ROWSET_NAME + "\" : { \"" + DATA_NAME + "\" : [ ");
	}

	private void endRowSetData() {
		this.rowSetResult.append(" ] } }");
	}
	
	
	public class MDBSelect implements IDBOOperation {
		private String id;
		private String dbName;
		private String collection;
		private String filter;
		
		
		public void init(Node config) throws XMLConfigException {
			this.id = XMLConfig.get(config, "@id");
			this.dbName = XMLConfig.get(config, "@db");
			this.collection = XMLConfig.get(config, "@collection");
			this.filter = XMLConfig.get(config, "filter");
		}
		
		public String[] getComponents() {
			String[] st = new String[1];
			st[0] = filter;
			
			return st;
		}
				
		public MDBSelect() {
			super();
		}

		public MDBSelect(String id, String dbName, String collection,
				String filter) {
			super();
			this.id = id;
			this.dbName = dbName;
			this.collection = collection;
			this.filter = filter;
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

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("MDBSelect [id=");
			builder.append(id);
			builder.append(", dbName=");
			builder.append(dbName);
			builder.append(", collection=");
			builder.append(collection);
			builder.append(", filter=");
			builder.append(filter);
			builder.append("]");
			return builder.toString();
		}

		@Override
		public String getData() {
			// NOTHING: unsopported method.
			return null;
		}

		@Override
		public String getSetCondition() {
			// NOTHING: unsopported method.
			return null;
		}

		@Override
		public OutputType getOutputType() {
			// NOTHING: unsopported method.
			return null;
		}
	}
}
