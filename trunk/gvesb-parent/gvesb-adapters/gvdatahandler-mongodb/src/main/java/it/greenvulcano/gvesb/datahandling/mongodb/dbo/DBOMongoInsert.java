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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.MapReduceCommand.OutputType;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;

/**
 * IDBOMongo Class specialized to parse the input RowSet document that contain the json operation in order to
 * insert data to DB.
 * Updated to use named parameters in statement.
 *
 * @version 3.5.0 May 27, 2015
 * @author GreenVulcano Developer Team
 */
public class DBOMongoInsert extends AbstractMongoDBO
{

	private static final Logger logger = GVLogger.getLogger(DBOMongoInsert.class);

	/**
	 * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#init(org.w3c.dom.Node)
	 */
	@Override
	public void init(Node config) throws DBOException
	{
		super.init(config);
		isInsert = true;
		try {
			forcedMode = XMLConfig.get(config, "@force-mode", MODE_JSON2DB);
			NodeList stmts = XMLConfig.getNodeList(config, "MDBInsert");
			String id = null;
			Node stmt;
			for (int i = 0; i < stmts.getLength(); i++) {
				stmt = stmts.item(i);
				id = XMLConfig.get(stmt, "@id");
				MDBInsert mdbInsert = new MDBInsert();
				mdbInsert.init(stmt);
				
				if (id == null) {
					id = Integer.toString(i);
				}
				statements.put(id, mdbInsert);
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

	@Override
	public void execute(Object input, MongoClient conn, Map<String, Object> props) throws DBOException, 
	InterruptedException {
		try {
			prepare();
			logger.debug("Begin execution of DB data insert through " + dboclass);

			BulkWriteResult bulkWriteResult = null;
			resultMessage = new StringBuffer();
			resultStatus = STATUS_OK;

			MongoStatementInfo mongoStatementInfo;

			Map<String, Object> localProps = buildProps(props);
			logProps(localProps);

			if (ignoreInput) {
				input = null;
			}

			if (input != null) {
				org.bson.Document docJsonInsert = null;
				//PARSE INPUT
				if (input instanceof byte[]) {
					logger.debug("Input is: byte[]");
					docJsonInsert = org.bson.Document.parse(new String((byte[]) input));
				}
				else if (input instanceof String) {
					logger.debug("Input is: String");
					docJsonInsert = org.bson.Document.parse((String) input);
				}
				if (docJsonInsert == null) {
					throw new DBOException("Cannot convert " + input.getClass() + " to org.bson.Document");
				}

				if( docJsonInsert.containsKey("RowSet") ) {
					// CASE 1: Docs to insert defined as input <RowSet> ...</RowSet>
					org.bson.Document rowSet = (org.bson.Document)docJsonInsert.get("RowSet");
					logger.debug("rowSet: "+rowSet);

					@SuppressWarnings("unchecked")
					List<org.bson.Document> dataList = (List<org.bson.Document>)rowSet.get("data");

					for(org.bson.Document itemData : dataList) {
						//GET ID STATEMENT
						String idStatement = itemData.get("id").toString();

						//GET STATEMENT FROM MAP statements
						IDBOOperation stmt = statements.get(idStatement);

						if(stmt != null) {
							mongoStatementInfo = new MongoStatementInfo(idStatement, stmt, localProps, conn);

							logger.debug("MongoDB database: " + mongoStatementInfo.getMongoDBName() + " - collection: "+ mongoStatementInfo.getMongoDBCollection());

							MongoDatabase database = getInternalConn(conn).getDatabase(mongoStatementInfo.getMongoDBName());
							MongoCollection<org.bson.Document> collection = database.getCollection(mongoStatementInfo.getMongoDBCollection());

							List<WriteModel<org.bson.Document>> writes = new ArrayList<WriteModel<org.bson.Document>>();

							//GET ROWS
							@SuppressWarnings("unchecked")
							List<Document> rowList = (List<Document>)itemData.get("row");
							for(Document itemRow : rowList) {
								ThreadUtils.checkInterrupted(getClass().getSimpleName(), getName(), logger);
								writes.add(new InsertOneModel<org.bson.Document>(itemRow) );
							}

							bulkWriteResult = collection.bulkWrite(writes);
							
							rowInsOk += bulkWriteResult.getInsertedCount();
							rowUpdOk += bulkWriteResult.getModifiedCount();

							logger.debug("Records inserted: " + rowInsOk + ";Record updated: " + rowUpdOk);
						}
					}

				} else {
					IDBOOperation stmt = statements.get("0");
					if(stmt != null) {
						mongoStatementInfo = new MongoStatementInfo("0", stmt, localProps, conn);

						logger.debug("MongoDB database: " + mongoStatementInfo.getMongoDBName() + " - collection: "+ mongoStatementInfo.getMongoDBCollection());

						MongoDatabase database = getInternalConn(conn).getDatabase(mongoStatementInfo.getMongoDBName());
						MongoCollection<org.bson.Document> collection = database.getCollection(mongoStatementInfo.getMongoDBCollection());

						collection.insertOne(docJsonInsert);
						rowInsOk += 1;

						logger.debug("Records inserted: " + rowInsOk);
					}
				}

			} else {
				IDBOOperation stmt = statements.get("0");

				//ESTRAPOLARE DB_NAME|DB_COLLECTION DALLO STATEMENT per eseguire lo script di insert.
				if (stmt != null) {
					mongoStatementInfo = new MongoStatementInfo("0", stmt, localProps, conn);

					logger.debug("MongoDB Statement Expanded: " + Arrays.toString(mongoStatementInfo.getJsonStatement()));

					MongoDatabase database = getInternalConn(conn).getDatabase(mongoStatementInfo.getMongoDBName());
					MongoCollection<org.bson.Document> collection = database.getCollection(mongoStatementInfo.getMongoDBCollection());

					String docToInsert = null;
					if(mongoStatementInfo.getJsonStatement() != null) {
						if( mongoStatementInfo.getJsonStatement().length > 0) {
							docToInsert = mongoStatementInfo.getJsonStatement()[0];
						}
					}
					
					if(docToInsert == null) throw new DBOException("MongoDB Statement error configuration. No data statement is present in XML Configuration.");
					
					collection.insertOne(Document.parse(docToInsert));
					rowInsOk += 1;

					logger.debug("Records inserted: " + rowInsOk);
				}
			}

			logger.debug("End execution of DB data insert through " + dboclass);
		}
		catch (MongoException exc) {
			rowDisc++;
			
			MongoDBError oraerr = MongoExceptionHandler.handleSQLException(exc);
			if (isBlockingError(oraerr.getErrorType())) {
				resultStatus = STATUS_KO;
				logger.error("Mongo Statement Informations:\n" + mongoStatementInfo);
				logger.error("MongoException configured as blocking error for the IDBO '" + serviceName + "' on row "
						+ rowCounter + ".", exc);
				throw new DBOException("MongoException configured as blocking error class on row "
						+ rowCounter + ": " + exc.getMessage(), exc);
			}

			resultMessage.append("MongoException error on row ").append(rowCounter).append(": ").append(exc.getMessage());
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
				logger.warn("Partial insert for service " + serviceName + ":" + resultMessage.toString() + ".");
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
	@Override
	public void execute(OutputStream data, MongoClient mongoClient,
			Map<String, Object> props) throws DBOException,
			InterruptedException {
		prepare();
        throw new DBOException("Unsupported method - DBOMongoInsert::execute(OutputStream, MongoClient, HashMap)");
	}
	
	
	public class MDBInsert implements IDBOOperation {
		private String id;
		private String dbName;
		private String collection;
		private String data;
		
		
		public void init(Node config) throws XMLConfigException {
			this.id = XMLConfig.get(config, "@id");
			this.dbName = XMLConfig.get(config, "@db");
			this.collection = XMLConfig.get(config, "@collection");
			this.data = XMLConfig.get(config, "data");
		}
		
		public String[] getComponents() {
			String[] st = new String[1];
			st[0] = data;
			
			return st;
		}
				
		public MDBInsert() {
			super();
		}

		public MDBInsert(String id, String dbName, String collection,
				String data) {
			super();
			this.id = id;
			this.dbName = dbName;
			this.collection = collection;
			this.data = data;
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
		public String getData() {
			return data;
		}
		public void setData(String data) {
			this.data = data;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("MDBDelete [id=");
			builder.append(id);
			builder.append(", dbName=");
			builder.append(dbName);
			builder.append(", collection=");
			builder.append(collection);
			builder.append(", data=");
			builder.append(data);
			builder.append("]");
			return builder.toString();
		}

		@Override
		public String getFilter() {
			// NOTHING: Not supported.
			return null;
		}

		@Override
		public String getSetCondition() {
			// NOTHING: Not supported.
			return null;
		}

		@Override
		public OutputType getOutputType() {
			// NOTHING: Not supported.
			return null;
		}
	}

}
