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
package it.greenvulcano.excel.config;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.excel.ExcelWorkBook;
import it.greenvulcano.excel.ReportSheet;
import it.greenvulcano.excel.exception.ExcelException;
import it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class ExcelReport
{
    private static Logger       logger             = GVLogger.getLogger(ExcelReport.class);
    private String              name               = null;
    private String              group              = null;
    private Set<String>         roles              = null;
    private String              format             = null;
    private Vector<ReportSheet> sheets             = null;
    private List<ParameterDef>  parameters         = new ArrayList<ParameterDef>();
    private boolean             discoverParameters = true;

    public ExcelReport(Node node) throws ExcelException
    {
        try {
            name = XMLConfig.get(node, "@name");
            group = XMLConfig.get(node, "@group", "Generic");
            format = XMLConfig.get(node, "@format", "");

            String sRoles = XMLConfig.get(node, "@roles", "");
            roles = new HashSet<String>();
            StringTokenizer str = new StringTokenizer(sRoles, ",");
            while (str.hasMoreTokens()) {
                String role = str.nextToken();
                roles.add(role.trim());
            }

            String connection = XMLConfig.get(node, "@jdbc-connection", "");
            sheets = new Vector<ReportSheet>();
            NodeList nl = XMLConfig.getNodeList(node, "Sheet");
            for (int k = 0; k < nl.getLength(); k++) {
                Node sheet = nl.item(k);
                ReportSheet reportsheet = new ReportSheet(sheet, connection);
                sheets.addElement(reportsheet);
            }

            String parList = XMLConfig.get(node, "@parameters", "");
            if (!"".equals(parList)) {
                discoverParameters = false;
                // NOME[::R],NOME,NOME[::R]
                StringTokenizer stp = new StringTokenizer(parList, ",");
                while (stp.hasMoreTokens()) {
                    String param = (stp.nextToken()).trim();
                    String[] pInfo = param.split("::");
                    if (pInfo.length == 2) {
                        parameters.add(new ParameterDef(pInfo[0], true));
                    }
                    else {
                        parameters.add(new ParameterDef(pInfo[0]));
                    }
                }
            }
            initParamList();
        }
        catch (ExcelException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new ExcelException("Error configuring ExcelReport", exc);
        }
    }

    public ExcelReport(String group, String name, String format, Vector<ReportSheet> sheets)
    {
        this(group, name, new HashSet<String>(), format, sheets);
    }

    public ExcelReport(String group, String name, String roles, String format, Vector<ReportSheet> sheets)
    {
        this.roles = new HashSet<String>();
        StringTokenizer st = new StringTokenizer(roles, ",");
        while (st.hasMoreTokens()) {
            String role = st.nextToken();
            this.roles.add(role.trim());
        }
        this.group = group;
        this.name = name;
        this.format = format;
        this.sheets = new Vector<ReportSheet>();
        Iterator<ReportSheet> i = sheets.iterator();
        while (i.hasNext()) {
            this.sheets.addElement(i.next());
        }

        initParamList();
    }

    public ExcelReport(String group, String name, Set<String> roles, String format, Vector<ReportSheet> sheets)
    {
        this.roles = new HashSet<String>(roles);
        this.group = group;
        this.name = name;
        this.format = format;
        this.sheets = new Vector<ReportSheet>();
        Iterator<ReportSheet> i = sheets.iterator();
        while (i.hasNext()) {
            this.sheets.addElement(i.next());
        }

        initParamList();
    }

    public String getName()
    {
        return name;
    }

    public String getGroup()
    {
        return group;
    }

    public Set<String> getRoles()
    {
        return roles;
    }

    public List<ParameterDef> getParams()
    {
        return parameters;
    }

    private void initParamList()
    {
        if (discoverParameters) {
            parameters.clear();
            for (int i = 0; i < sheets.size(); i++) {
                ReportSheet rs = sheets.elementAt(i);
                List<ParameterDef> params = rs.getParams();
                for (int j = 0; j < params.size(); j++) {
                    ParameterDef p = params.get(j);
                    if (!parameters.contains(p)) {
                        parameters.add(p);
                    }
                }

            }
        }
    }

    public boolean verifyRoles(Set<String> roles)
    {
        if (this.roles.isEmpty()) {
            return true;
        }
        Set<String> appo = new HashSet<String>(roles);
        appo.retainAll(this.roles);
        return !appo.isEmpty();
    }

    public boolean verifyRoles(String roles)
    {
        if (this.roles.isEmpty()) {
            return true;
        }
        StringTokenizer st = new StringTokenizer(roles, ",");
        while (st.hasMoreTokens()) {
            String role = st.nextToken().trim();
            if (this.roles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    public byte[] getExcelReportAsByteArray(Map<String, Object> props) throws ExcelException, 
            InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeExcelReportOnOS(baos, props);
        return baos.toByteArray();
    }

    public void writeExcelReportOnOS(OutputStream os, Map<String, Object> props) throws ExcelException, 
            InterruptedException {
        NMDC.push();
        ConfigurationHandler.setLogContext();
        HashMap<String, Connection> connMap = new HashMap<String, Connection>();
        try {
            logger.debug("Start ExcelReport '" + name + "' generation...");
            ExcelWorkBook ew = new ExcelWorkBook(os, format);
            int i = sheets.size();
            for (int j = 0; j < i; j++) {
                if (j % 10 == 0) {
                    ThreadUtils.checkInterrupted("ExcelReport", name, logger);
                }
                ReportSheet rs = sheets.get(j);
                Statement statement = null;
                ResultSet resultset = null;
                Connection connection = null;
                try {
                    String connName = rs.getConnection();
                    connection = connMap.get(connName);
                    if (connection == null) {
                        connection = JDBCConnectionBuilder.getConnection(connName);
                        logger.debug("Creating JDBC Connection: '" + connName + "'");
                        connMap.put(connName, connection);
                    }
                    connection.setAutoCommit(false);

                    Vector<String> pStatements = rs.getPreSelect();
                    for (int k = 0; k < pStatements.size(); k++) {
                        String pStatement = pStatements.get(k);
                        if (props != null) {
                            pStatement = PropertiesHandler.expand(pStatement, props, null, connection);
                        }
                        logger.debug("Executing preparation statement: " + pStatement);
                        statement = connection.createStatement();
                        try {
                            statement.execute(pStatement);
                        }
                        finally {
                            if (statement != null) {
                                try {
                                    statement.close();
                                }
                                catch (Exception exc) {
                                    // do nothing
                                }
                            }
                        }
                    }

                    statement = connection.createStatement();
                    String select = null;
                    String name = null;
                    String title = null;
                    if (props != null) {
                        select = PropertiesHandler.expand(rs.getSelect(), props, connection);
                        name = PropertiesHandler.expand(rs.getName(), props, connection);
                        title = PropertiesHandler.expand(rs.getTitle(), props, connection);
                        logger.debug("Query parameters: " + props.toString());
                    }
                    else {
                        select = rs.getSelect();
                        name = rs.getName();
                        title = rs.getTitle();
                    }
                    logger.debug("Executing query: " + select);
                    resultset = statement.executeQuery(select);
                    ew.fillWithResultSet(resultset, name, title);
                }
                catch (Exception exc) {
                    ThreadUtils.checkInterrupted(exc);
                    throw new ExcelException(exc);
                }
                finally {
                    if (resultset != null) {
                        try {
                            resultset.close();
                        }
                        catch (Exception exc) {
                            // do nothing
                        }
                    }
                    if (statement != null) {
                        try {
                            statement.close();
                        }
                        catch (Exception exc) {
                            // do nothing
                        }
                    }
                    if (connection != null) {
                        try {
                            connection.commit();
                        }
                        catch (Exception exc) {
                            // do nothing
                        }
                    }
                }
            }

            ew.closeWorkBook();
            logger.debug("End ExcelReport '" + name + "' generation");
        }
        catch (ExcelException exc) {
            throw exc;
        }
        finally {
            closeConnections(connMap);
            NMDC.pop();
        }
    }

    private void closeConnections(HashMap<String, Connection> connMap)
    {
        String connName = null;
        Iterator<String> i = connMap.keySet().iterator();
        while (i.hasNext()) {
            try {
                connName = i.next();
                logger.debug("Closing JDBC Connection: '" + connName + "'");
                Connection connection = connMap.get(connName);
                // connection.commit();
                connection.setAutoCommit(true);

                JDBCConnectionBuilder.releaseConnection(connName, connection);
            }
            catch (Exception exc) {
                logger.error("Error closing JDBC Connection: '" + connName + "'", exc);
            }
            i.remove();
        }
    }

}
