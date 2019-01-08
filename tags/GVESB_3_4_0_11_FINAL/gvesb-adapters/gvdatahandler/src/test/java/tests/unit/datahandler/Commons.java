/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
package tests.unit.datahandler;

import java.sql.Connection;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @version 3.2.0 02/10/2011
 * @author GreenVulcano Developer Team
 */
public class Commons
{
    /**
     * @throws SQLException
     *
     */
    public static void createDB(Connection connection) throws SQLException
    {
        connection.prepareStatement(
                "create table testtable (id INTEGER primary key, field1 VARCHAR(30), field2 TIMESTAMP, field3 NUMERIC(8,3));").execute();
        connection.prepareStatement(
                "insert into testtable (id, field1, field2, field3) values (1, 'testvalue', '2000-01-01 12:30:45', 123.456);").execute();
//                "insert into testtable (id, field1, field2, field3) values (1, 'testvalue', TO_TIMESTAMP('2000-01-01 12:30:45', 'YYYY-MM-DD HH24:MI:SS'), 123.456);").execute();
    }

    /**
     * @throws SQLException
     *
     */
    public static void clearDB(Connection connection) throws SQLException
    {
        connection.prepareStatement("drop table testtable;").execute();
    }

    public static Document createInsertMessage() throws Exception
    {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = documentFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element root = doc.createElement("RowSet");
        doc.appendChild(root);
        Element data = doc.createElement("data");
        root.appendChild(data);

        Element row = doc.createElement("row");
        data.appendChild(row);
        Element col1 = doc.createElement("col");
        col1.setAttribute("type", "numeric");
        col1.appendChild(doc.createTextNode("2"));
        row.appendChild(col1);
        Element col2 = doc.createElement("col");
        col2.setAttribute("type", "string");
        col2.appendChild(doc.createTextNode("testvalue2"));
        row.appendChild(col2);
        Element col3 = doc.createElement("col");
        col3.setAttribute("type", "timestamp");
        col3.setAttribute("format", "dd/MM/yyyy HH:mm:ss");
        col3.appendChild(doc.createTextNode("31/12/2010 23:59:59"));
        row.appendChild(col3);
        Element col4 = doc.createElement("col");
        col4.setAttribute("type", "float");
        col4.setAttribute("decimal-separator", ",");
        col4.setAttribute("grouping-separator", ".");
        col4.setAttribute("number-format", "#,##0.000");
        col4.appendChild(doc.createTextNode("12.345,123"));
        row.appendChild(col4);

        row = doc.createElement("row");
        data.appendChild(row);
        col1 = doc.createElement("col");
        col1.setAttribute("type", "numeric");
        col1.appendChild(doc.createTextNode("3"));
        row.appendChild(col1);
        col2 = doc.createElement("col");
        col2.setAttribute("type", "string");
        col2.appendChild(doc.createTextNode("testvalue3"));
        row.appendChild(col2);
        col3 = doc.createElement("col");
        col3.setAttribute("type", "timestamp");
        col3.setAttribute("format", "dd/MM/yyyy HH:mm:ss");
        col3.appendChild(doc.createTextNode("10/01/2011 00:00:00"));
        row.appendChild(col3);
        col4 = doc.createElement("col");
        col4.setAttribute("type", "float");
        col4.setAttribute("decimal-separator", ".");
        col4.setAttribute("grouping-separator", ",");
        col4.setAttribute("number-format", "#0.00");
        col4.appendChild(doc.createTextNode("5.12"));
        row.appendChild(col4);
        return doc;
    }

    public static Document createInsertOrUpdateMessage() throws Exception
    {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = documentFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element root = doc.createElement("RowSet");
        doc.appendChild(root);
        Element data = doc.createElement("data");
        root.appendChild(data);

        // row
        Element row = doc.createElement("row");
        data.appendChild(row);

        // column
        Element col1 = doc.createElement("col");
        col1.setAttribute("type", "numeric");
        col1.appendChild(doc.createTextNode("2"));
        row.appendChild(col1);
        Element col2 = doc.createElement("col");
        col2.setAttribute("type", "string");
        col2.appendChild(doc.createTextNode("testvalue2-new"));
        row.appendChild(col2);
        Element col3 = doc.createElement("col");
        col3.setAttribute("type", "timestamp");
        col3.setAttribute("format", "dd/MM/yyyy HH:mm:ss");
        col3.appendChild(doc.createTextNode("31/12/2010 15:00:00"));
        row.appendChild(col3);
        Element col4 = doc.createElement("col");
        col4.setAttribute("type", "float");
        col4.setAttribute("decimal-separator", ",");
        col4.setAttribute("grouping-separator", ".");
        col4.setAttribute("number-format", "#,##0.000");
        col4.appendChild(doc.createTextNode("10.000,000"));
        row.appendChild(col4);

        // column update
        Element col_update2 = doc.createElement("col-update");
        col_update2.setAttribute("type", "string");
        col_update2.appendChild(doc.createTextNode("testvalue2-new"));
        row.appendChild(col_update2);
        Element col_update3 = doc.createElement("col-update");
        col_update3.setAttribute("type", "timestamp");
        col_update3.setAttribute("format", "dd/MM/yyyy HH:mm:ss");
        col_update3.appendChild(doc.createTextNode("31/12/2010 15:00:00"));
        row.appendChild(col_update3);
        Element col_update4 = doc.createElement("col-update");
        col_update4.setAttribute("type", "float");
        col_update4.setAttribute("decimal-separator", ",");
        col_update4.setAttribute("grouping-separator", ".");
        col_update4.setAttribute("number-format", "#,##0.000");
        col_update4.appendChild(doc.createTextNode("10.000,000"));
        row.appendChild(col_update4);
        Element col_update1 = doc.createElement("col-update");
        col_update1.setAttribute("type", "numeric");
        col_update1.appendChild(doc.createTextNode("2"));
        row.appendChild(col_update1);

        // row
        row = doc.createElement("row");
        data.appendChild(row);

        // column
        col1 = doc.createElement("col");
        col1.setAttribute("type", "numeric");
        col1.appendChild(doc.createTextNode("4"));
        row.appendChild(col1);
        col2 = doc.createElement("col");
        col2.setAttribute("type", "string");
        col2.appendChild(doc.createTextNode("testvalue4"));
        row.appendChild(col2);
        col3 = doc.createElement("col");
        col3.setAttribute("type", "timestamp");
        col3.setAttribute("format", "dd/MM/yyyy HH:mm:ss");
        col3.appendChild(doc.createTextNode("20/10/2010 16:00:00"));
        row.appendChild(col3);
        col4 = doc.createElement("col");
        col4.setAttribute("type", "float");
        col4.setAttribute("decimal-separator", ",");
        col4.setAttribute("grouping-separator", ".");
        col4.setAttribute("number-format", "#,##0.000");
        col4.appendChild(doc.createTextNode("13.456,004"));
        row.appendChild(col4);

        // column update
        col_update2 = doc.createElement("col-update");
        col_update2.setAttribute("type", "string");
        col_update2.appendChild(doc.createTextNode("testvalue4"));
        row.appendChild(col_update2);
        col_update3 = doc.createElement("col-update");
        col_update3.setAttribute("type", "timestamp");
        col_update3.setAttribute("format", "dd/MM/yyyy HH:mm:ss");
        col_update3.appendChild(doc.createTextNode("20/10/2010 16:00:00"));
        row.appendChild(col_update3);
        col_update4 = doc.createElement("col-update");
        col_update4.setAttribute("type", "float");
        col_update4.setAttribute("decimal-separator", ",");
        col_update4.setAttribute("grouping-separator", ".");
        col_update4.setAttribute("number-format", "#,##0.000");
        col_update4.appendChild(doc.createTextNode("13.456,004"));
        row.appendChild(col_update4);
        col_update1 = doc.createElement("col-update");
        col_update1.setAttribute("type", "numeric");
        col_update1.appendChild(doc.createTextNode("4"));
        row.appendChild(col_update1);

        return doc;
    }
}
