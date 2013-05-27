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
package it.greenvulcano.excel;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.excel.config.ParameterDef;
import it.greenvulcano.excel.exception.ExcelException;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class ReportSheet
{
    private int                 id               = -1;
    private String              name             = null;
    private String              title            = null;
    private String              select           = null;
    private Vector<String>      preSelect        = null;
    private String              connection       = null;
    private List<ParameterDef>  parameters       = new ArrayList<ParameterDef>();

    private static final String METADATA_REG_EXP = "[@][{][{]\\w*[}][}]";

    public ReportSheet(Node node, String connection) throws ExcelException
    {
        try {
            id = XMLConfig.getInteger(node, "@id");
            name = XMLConfig.get(node, "@name", "");
            title = XMLConfig.get(node, "@title", "");
            this.connection = XMLConfig.get(node, "@jdbc-connection", connection);
            NodeList nl = XMLConfig.getNodeList(node, "prep-statement");
            preSelect = new Vector<String>();
            for (int j = 0; j < nl.getLength(); j++) {
                preSelect.add(XMLConfig.get(nl.item(j), "."));
            }
            select = XMLConfig.get(node, "statement", "");

            initParamList();
        }
        catch (XMLConfigException exc) {
            throw new ExcelException("Error configuring ReportSheet", exc);
        }
    }

    public ReportSheet(int id, String name, String title, String select, Vector<String> preSelect, String connection)
    {
        this.id = id;
        this.name = name;
        this.title = title;
        this.select = select;
        this.preSelect = preSelect;
        this.connection = connection;

        initParamList();
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getTitle()
    {
        return title;
    }

    public String getSelect()
    {
        return select;
    }

    public Vector<String> getPreSelect()
    {
        return preSelect;
    }

    public String getConnection()
    {
        return connection;
    }

    public List<ParameterDef> getParams()
    {
        return parameters;
    }

    private void initParamList()
    {
        Pattern pattern = Pattern.compile(METADATA_REG_EXP);
        Matcher matcher = pattern.matcher(select);
        do {
            if (!matcher.find()) {
                break;
            }
            String s = matcher.group();
            s = s.substring(3, s.length() - 2);
            ParameterDef p = new ParameterDef(s);
            if (!parameters.contains(p)) {
                parameters.add(p);
            }
        }
        while (true);
        matcher = pattern.matcher(name);
        do {
            if (!matcher.find()) {
                break;
            }
            String s = matcher.group();
            s = s.substring(3, s.length() - 2);
            ParameterDef p = new ParameterDef(s);
            if (!parameters.contains(p)) {
                parameters.add(p);
            }
        }
        while (true);
        matcher = pattern.matcher(title);
        do {
            if (!matcher.find()) {
                break;
            }
            String s = matcher.group();
            s = s.substring(3, s.length() - 2);
            ParameterDef p = new ParameterDef(s);
            if (!parameters.contains(p)) {
                parameters.add(p);
            }
        }
        while (true);
    }

}
