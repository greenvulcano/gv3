/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.log.db;

/**
 *
 * @version 3.1.0 24/gen/2011
 * @author GreenVulcano Developer Team
 */
public class Column {
	String name = null;
	ColumnType logtype = ColumnType.EMPTY;
	boolean ignore = true;
	Object value = null;
	
	public Column(String name, ColumnType logtype, Object value) throws Exception {
	    if ((logtype == ColumnType.STATIC) && (value == null)) { throw new Exception("Column::Column(), Missing argument value STATIC!"); }
	    if ((logtype == ColumnType.MDC) && (value == null)) { throw new Exception("Column::Column(), Missing argument value MDC!"); }
        if ((logtype == ColumnType.SEQUENCE) && (value == null)) { throw new Exception("Column::Column(), Missing argument value SEQUENCE!"); }
        this.name = name.toUpperCase();
        this.logtype = logtype;
        this.value = value;
        this.ignore = (logtype == ColumnType.EMPTY);
    }
}