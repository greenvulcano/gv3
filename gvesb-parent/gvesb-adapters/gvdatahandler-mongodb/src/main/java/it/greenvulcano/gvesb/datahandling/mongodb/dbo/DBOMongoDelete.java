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
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.MapReduceCommand.OutputType;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.DeleteManyModel;
import com.mongodb.client.model.WriteModel;


/**
 * IDBOMongo Class specialized to parse the input Json document that contain the json operation to
 * delete data to DB.
 * Updated to use named parameters in statement.
 *
 * @version 3.5.0 Jun 05, 2015
 * @author GreenVulcano Developer Team
 */
public class DBOMongoDelete extends AbstractMongoDBO
{

	private static final Logger logger = GVLogger.getLogger(DBOMongoDelete.class);

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
			NodeList stmts = XMLConfig.getNodeList(config, "MDBDelete");
			String id = null;
			Node stmt;
			for (int i = 0; i < stmts.getLength(); i++) {
				stmt = stmts.item(i);
				id = XMLConfig.get(stmt, "@id");
				MDBDelete mdbDelete = new MDBDelete();
				mdbDelete.init(stmt);
				
				if (id == null) {
					id = Integer.toString(i);
				}
				statements.put(id, mdbDelete);
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
			logger.debug("Begin execution of DB data delete through " + dboclass);
			
			resultMessage = new StringBuffer();
			resultStatus = STATUS_OK;
			
			Map<String, Object> localProps = buildProps(props);
			logProps(localProps);

			for (Entry<String, IDBOOperation> entry : statements.entrySet()) {
				ThreadUtils.checkInterrupted(getClass().getSimpleName(), getName(), logger);
				Object idStatement = entry.getKey();
				IDBOOperation stmt = entry.getValue();

				if (stmt != null) {
					mongoStatementInfo = new MongoStatementInfo(idStatement.toString(), stmt, localProps, conn);

					logger.debug("Executing delete:\n" + Arrays.toString(mongoStatementInfo.getJsonStatement()));

					MongoDatabase database = getInternalConn(conn).getDatabase(mongoStatementInfo.getMongoDBName());
					MongoCollection<org.bson.Document> collection = database.getCollection(mongoStatementInfo.getMongoDBCollection());
					
					org.bson.Document docJsonDelete = null;
					if(mongoStatementInfo.getJsonStatement() != null) {
						if( mongoStatementInfo.getJsonStatement().length > 0) {
							docJsonDelete = org.bson.Document.parse(mongoStatementInfo.getJsonStatement()[0]);
						}
					}
					
					if(docJsonDelete == null) throw new DBOException("MongoDB Statement error configuration. No filter delete statement is present in XML Configuration.");
					
					List<WriteModel<org.bson.Document>> writes = new ArrayList<WriteModel<org.bson.Document>>();
					writes.add(new DeleteManyModel<org.bson.Document>(docJsonDelete) );

					BulkWriteResult bulkWriteResult = collection.bulkWrite(writes);
					
					rowInsOk = rowInsOk + bulkWriteResult.getInsertedCount();
					rowUpdOk = rowUpdOk + bulkWriteResult.getModifiedCount();
					
					logger.debug("Executed delete:\n" + mongoStatementInfo.getJsonStatement());
				}
			}

			logger.debug("End execution of DB data delete through " + dboclass);
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
			logger.error("Exception during DB data delete through " + dboclass, exc);
			throw new DBOException("DBO[" + dboclass + "] Exception during DB data delete", exc);
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
	@Override
	public void execute(OutputStream data, MongoClient mongoClient,
			Map<String, Object> props) throws DBOException,
			InterruptedException {
		prepare();
        throw new DBOException("Unsupported method - DBOMongoDelete::execute(OutputStream, MongoClient, HashMap)");
	}
	
	public class MDBDelete implements IDBOOperation {
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
				
		public MDBDelete() {
			super();
		}

		public MDBDelete(String id, String dbName, String collection,
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
			builder.append("MDBDelete [id=");
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
			// NOTHING: Unsopported method.
			return null;
		}

		@Override
		public String getSetCondition() {
			// NOTHING: Unsopported method.
			return null;
		}

		@Override
		public OutputType getOutputType() {
			// NOTHING: Unsopported method.
			return null;
		}
	}

}
