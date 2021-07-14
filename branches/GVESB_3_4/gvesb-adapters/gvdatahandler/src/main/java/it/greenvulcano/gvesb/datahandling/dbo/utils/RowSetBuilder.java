/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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

import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import it.greenvulcano.gvesb.datahandling.utils.FieldFormatter;
import it.greenvulcano.util.xml.XMLUtils;

/**
 *
 * @version 3.4.0 06/ago/2013
 * @author GreenVulcano Developer Team
 *
 */
public interface RowSetBuilder
{
    public void setName(String name);
    public String getName();

    public void setLogger(Logger logger);
    public Logger getLogger();

    public void setXMLUtils(XMLUtils parser);
    public XMLUtils getXMLUtils();

    public void setDateFormatter(SimpleDateFormat dateFormatter);
    public SimpleDateFormat getDateFormatter();

    public void setNumberFormatter(DecimalFormat numberFormatter);
    public DecimalFormat getNumberFormatter();

    public void setDecSeparator(String decSeparator);
    public String getDecSeparator();

    public void setGroupSeparator(String groupSeparator);
    public String getGroupSeparator();

    public void setNumberFormat(String numberFormat);
    public String getNumberFormat();

    public Document createDocument(XMLUtils parser) throws NullPointerException;

    public int build(Document doc, String id, ResultSet rs, Set<Integer> keyField,
            Map<String, FieldFormatter> fieldNameToFormatter, Map<String, FieldFormatter> fieldIdToFormatter)
            throws Exception;

    public void cleanup();

    public RowSetBuilder getCopy();
}
