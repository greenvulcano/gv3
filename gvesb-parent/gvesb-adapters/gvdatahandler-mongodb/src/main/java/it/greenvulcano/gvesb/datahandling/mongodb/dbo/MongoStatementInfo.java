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

import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;

import java.util.Arrays;
import java.util.Map;

import com.mongodb.MongoClient;


/**
 * @version 3.5.0 Jun 18, 2015
 * @author GreenVulcano Developer Team
 *
 *
 */
public class MongoStatementInfo {

	private String id;
	private String mongoDBName;
	private String mongoDBCollection;
	private String[] jsonStatement = new String[0];


	public MongoStatementInfo(String id, IDBOOperation stmt, Map<String, Object> localProps, MongoClient conn) throws PropertiesHandlerException {
		super();
		this.id = id;

		if(stmt != null) {
			String[] tmp = stmt.getComponents(); 
			
			this.mongoDBName = PropertiesHandler.expand(stmt.getDbName(), localProps, null, conn);
			this.mongoDBCollection = PropertiesHandler.expand(stmt.getCollection(), localProps, null, conn);
			this.jsonStatement = new String[tmp.length];
			
			for(int i=0; i < tmp.length; i++) {
				this.jsonStatement[i] = PropertiesHandler.expand(tmp[i], localProps, null, conn);
			}
		}
	}

	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getMongoDBName() {
		return mongoDBName;
	}


	public void setMongoDBName(String mongoDBName) {
		this.mongoDBName = mongoDBName;
	}


	public String getMongoDBCollection() {
		return mongoDBCollection;
	}


	public void setMongoDBCollection(String mongoDBCollection) {
		this.mongoDBCollection = mongoDBCollection;
	}


	public String[] getJsonStatement() {
		return jsonStatement;
	}

	public void setJsonStatement(String[] jsonStatement) {
		this.jsonStatement = jsonStatement;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MongoStatementInfo [id=");
		builder.append(id);
		builder.append(", mongoDBName=");
		builder.append(mongoDBName);
		builder.append(", mongoDBCollection=");
		builder.append(mongoDBCollection);
		builder.append(", jsonStatement=");
		builder.append(Arrays.toString(jsonStatement));
		builder.append("]");
		return builder.toString();
	}

}