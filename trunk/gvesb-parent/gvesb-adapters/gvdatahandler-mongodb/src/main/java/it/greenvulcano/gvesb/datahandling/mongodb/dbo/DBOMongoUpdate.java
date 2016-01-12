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
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;

/**
 * IDBOMongo Class specialized in updating data to DB.
 * 
 * Updated to use named parameters in statement.
 *
 * @version 3.5.0 Jun 05, 2015
 * @author GreenVulcano Developer Team
 */
public class DBOMongoUpdate extends AbstractMongoDBO
{

	private static final Logger logger = GVLogger.getLogger(DBOMongoUpdate.class);

	/**
	 * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#init(org.w3c.dom.Node)
	 */
	@Override
	public void init(Node config) throws DBOException
	{
		super.init(config);
		isInsert = false;
		try {
			forcedMode = XMLConfig.get(config, "@force-mode", MODE_JSON2DB);

			NodeList stmts = XMLConfig.getNodeList(config, "MDBUpdate");
			String id = null;
			Node stmt;
			for (int i = 0; i < stmts.getLength(); i++) {
				stmt = stmts.item(i);
				id = XMLConfig.get(stmt, "@id");
				MDBUpdate mdbUpdate = new MDBUpdate();
				mdbUpdate.init(stmt);

				if (id == null) {
					id = Integer.toString(i);
				}
				statements.put(id, mdbUpdate);
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
			logger.debug("Begin execution of DB data update through " + dboclass);

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
				org.bson.Document docJsonUpdate = null;
				//PARSE INPUT
				if (input instanceof byte[]) {
					logger.debug("Input is: byte[]");
					docJsonUpdate = org.bson.Document.parse(new String((byte[]) input));
				}
				else if (input instanceof String) {
					logger.debug("Input is: String");
					docJsonUpdate = org.bson.Document.parse((String) input);
				}
				if (docJsonUpdate == null) {
					throw new DBOException("Cannot convert " + input.getClass() + " to org.bson.Document");
				}

				if( docJsonUpdate.containsKey("RowSet") ) {
					// CASE 1: JSON Document defined as input. <RowSet> ... </RowSet>					
					org.bson.Document rowSet = (org.bson.Document)docJsonUpdate.get("RowSet");
					logger.debug("RowSet: \n" + rowSet);

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
								
								Document docQuery = (Document)itemRow.get("w"); //FILTER
								Document docUpdate = (Document)itemRow.get("d"); //DATA UPDATE

								logger.debug("Query Object:\n " + docQuery);
								logger.debug("Update Object:\n " + docUpdate);

								ThreadUtils.checkInterrupted(getClass().getSimpleName(), getName(), logger);
								writes.add(new UpdateManyModel<org.bson.Document>(docQuery, docUpdate));
							}

							bulkWriteResult = collection.bulkWrite(writes);
							
							rowUpdOk += bulkWriteResult.getMatchedCount();

							logger.debug("Record updated: " + rowUpdOk);
						}
					}

				} else {
					// CASE 2: INPUT != null and No <RowSet>. JSON Document defined as input without <RowSet> only one statement as in the XML configuration.
					IDBOOperation stmt = statements.get("0");
					if(stmt != null) {
						mongoStatementInfo = new MongoStatementInfo("0", stmt, localProps, conn);

						logger.debug("MongoDB database: " + mongoStatementInfo.getMongoDBName() + " - collection: "+ mongoStatementInfo.getMongoDBCollection());

						MongoDatabase database = getInternalConn(conn).getDatabase(mongoStatementInfo.getMongoDBName());
						MongoCollection<org.bson.Document> collection = database.getCollection(mongoStatementInfo.getMongoDBCollection());

						Document docQuery = (Document)docJsonUpdate.get("w"); //FILTER
						Document docUpdate = (Document)docJsonUpdate.get("d"); //DATA UPDATE

						List<WriteModel<org.bson.Document>> writes = new ArrayList<WriteModel<org.bson.Document>>();
						writes.add(new UpdateManyModel<org.bson.Document>(docQuery, docUpdate));

						bulkWriteResult = collection.bulkWrite(writes);
						
						rowUpdOk += bulkWriteResult.getMatchedCount();

						logger.debug("Record updated: " + rowUpdOk);

					}
				}

			} else {
				// CASE 3: JSON Document inside XML configuration Service. INPUT FROM XML.
				IDBOOperation stmt = statements.get("0");

				//ESTRAPOLARE DB_NAME|DB_COLLECTION DALLO STATEMENT per eseguire lo script di insert.
				if (stmt != null) {
					mongoStatementInfo = new MongoStatementInfo("0", stmt, localProps, conn);

					logger.debug("Executing update:\n" + Arrays.toString(mongoStatementInfo.getJsonStatement()));

					MongoDatabase database = getInternalConn(conn).getDatabase(mongoStatementInfo.getMongoDBName());
					MongoCollection<org.bson.Document> collection = database.getCollection(mongoStatementInfo.getMongoDBCollection());

					String docQuery  = null;
					String docUpdate = null;
					if(mongoStatementInfo.getJsonStatement() != null && mongoStatementInfo.getJsonStatement().length >= 2) {
						docQuery  = mongoStatementInfo.getJsonStatement()[0];
						docUpdate = mongoStatementInfo.getJsonStatement()[1];

						logger.debug("Query Object:\n" + docQuery);
						logger.debug("Update Object:\n" + docUpdate);
					}

					if(docQuery == null) throw new DBOException("MongoDB Statement error configuration. No filter statement is present in XML Configuration.");
					if(docUpdate == null) throw new DBOException("MongoDB Statement error configuration. No set statement is present in XML Configuration.");

					org.bson.Document docJsonQuery = org.bson.Document.parse(docQuery);
					org.bson.Document docJsonUpdate = org.bson.Document.parse(docUpdate);

					List<WriteModel<org.bson.Document>> writes = new ArrayList<WriteModel<org.bson.Document>>();
					writes.add(new UpdateManyModel<org.bson.Document>(docJsonQuery, docJsonUpdate));

					bulkWriteResult = collection.bulkWrite(writes);
					
					rowUpdOk += bulkWriteResult.getMatchedCount();

					logger.debug("Record updated: " + rowUpdOk);
				}
			}

			logger.debug("End execution of DB data update through " + dboclass);
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
		throw new DBOException("Unsupported method - DBOMongoUpdate::execute(OutputStream, MongoClient, HashMap)");
	}


	public class MDBUpdate implements IDBOOperation {
		private String id;
		private String dbName;
		private String collection;
		private String filter;
		private String setCondition;


		public void init(Node config) throws XMLConfigException {
			this.id = XMLConfig.get(config, "@id");
			this.dbName = XMLConfig.get(config, "@db");
			this.collection = XMLConfig.get(config, "@collection");
			this.filter = XMLConfig.get(config, "filter");
			this.setCondition = XMLConfig.get(config, "set");
		}

		public String[] getComponents() {
			String[] st = new String[2];
			st[0] = filter;
			st[1] = setCondition;
			return st;
		}

		public MDBUpdate() {
			super();
		}

		public MDBUpdate(String id, String dbName, String collection,
				String filter, String setCondition) {
			super();
			this.id = id;
			this.dbName = dbName;
			this.collection = collection;
			this.filter = filter;
			this.setCondition = setCondition;
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

		public String getSetCondition() {
			return setCondition;
		}

		public void setSetCondition(String setCondition) {
			this.setCondition = setCondition;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("MDBUpdate [id=");
			builder.append(id);
			builder.append(", dbName=");
			builder.append(dbName);
			builder.append(", collection=");
			builder.append(collection);
			builder.append(", filter=");
			builder.append(filter);
			builder.append(", setCondition=");
			builder.append(setCondition);
			builder.append("]");
			return builder.toString();
		}

		@Override
		public String getData() {
			// NOTHING: Unsopported method.
			return null;
		}

		@Override
		public OutputType getOutputType() {
			// NOTHING: unsopported method.
			return null;
		}
	}

}
