/*
 * Copyright (c) 2009-2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package datahandler.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;

/**
 * @version 3.5.0 01/07/2015
 * @author GreenVulcano Developer Team
 */
public class CommonsMongo
{
	private static final String TEST_DB = "testdb";
	private static final String TEST_COLLECTION = "test_collection";
	private static final String TEST_MAPREDUCE_IN_COLLECTION = "test_in_mapreduce";
	private static final String TEST_MAPREDUCE_OUT_COLLECTION = "test_out_mapreduce";


	/**
	 * @throws MongoException
	 *
	 */
	public static void createDB(MongoClient mongoClient) throws MongoException
	{
		mongoClient.getDatabase(TEST_DB).createCollection(TEST_COLLECTION);

		//MUST BE CREATED BECAUSE THE OPTION is MapReduceCommand.OutputType.REPLACE 
		//From MongoDB documentation: "Save the job output to a collection, replacing its previous content".
		//MapReduceCommand.OutputType.INLINE: Get the result ad inline.
		mongoClient.getDatabase(TEST_DB).createCollection(TEST_MAPREDUCE_IN_COLLECTION);
		mongoClient.getDatabase(TEST_DB).createCollection(TEST_MAPREDUCE_OUT_COLLECTION);
	}

	/**
	 * @throws MongoException
	 *
	 */
	public static void clearDB(MongoClient mongoClient) throws MongoException
	{
		mongoClient.getDatabase(TEST_DB).drop();
	}

}
