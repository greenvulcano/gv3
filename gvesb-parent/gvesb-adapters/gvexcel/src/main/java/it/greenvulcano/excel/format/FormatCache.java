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
package it.greenvulcano.excel.format;

import it.greenvulcano.excel.exception.ExcelException;

import java.util.HashMap;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class FormatCache {
    private HashMap<String, ColumnFormat> colFrmtCache = new HashMap<String, ColumnFormat>();

    public ColumnFormat getColumnFormat(String colType, String colName, String configName) throws ExcelException
    {
        String key = colType + "_" + colName + "_" + configName;
        ColumnFormat cf = colFrmtCache.get(key);
        if (cf == null) {
            cf = new ColumnFormat(colType, colName, configName);
            colFrmtCache.put(key, cf);
        }
        return cf;
    }

    public void reset()
    {
        colFrmtCache.clear();
    }
}
