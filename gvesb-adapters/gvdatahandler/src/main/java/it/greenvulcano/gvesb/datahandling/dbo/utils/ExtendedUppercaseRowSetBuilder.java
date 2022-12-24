/*
 * Copyright (c) 2009-2020 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.datahandling.dbo.utils;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 *
 * @version 3.4.0 29/sep/2020
 * @author GreenVulcano Developer Team
 *
 */
public class ExtendedUppercaseRowSetBuilder extends ExtendedRowSetBuilder
{
    @Override
    public RowSetBuilder getCopy() {
        ExtendedUppercaseRowSetBuilder copy = new ExtendedUppercaseRowSetBuilder();

        copy.name = this.name;
        copy.logger = this.logger;
        copy.numberFormat = this.numberFormat;
        copy.groupSeparator = this.groupSeparator;
        copy.decSeparator = this.decSeparator;
        copy.parser = this.parser;
        copy.dateFormatter = this.dateFormatter;
        copy.timeFormatter = this.timeFormatter;
        copy.numberFormatter = this.numberFormatter;

        return copy;
    }

    @Override
    protected String getColumnName(ResultSetMetaData rsm, int i) throws SQLException {
        String cName = rsm.getColumnLabel(i);
        if (cName == null) {
            return rsm.getColumnName(i).toUpperCase();
        }
        else if (cName.equals(rsm.getColumnName(i))) {
            return cName.toUpperCase();
        }
        return cName;
    }
}
