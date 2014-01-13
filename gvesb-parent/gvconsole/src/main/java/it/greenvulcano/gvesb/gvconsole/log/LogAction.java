/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
 * reserved. This file is part of GreenVulcano ESB. GreenVulcano ESB is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with GreenVulcano ESB. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.gvconsole.log;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.file.RegExFileFilter;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.actions.LookupDispatchAction;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.1.0 02/feb/2011
 * @author GreenVulcano Developer Team
 */
public class LogAction extends LookupDispatchAction
{
    private static final Logger logger       = GVLogger.getLogger(LogAction.class);
    private HashMap             keyMethodMap = new HashMap();

    /**
	 *
	 */
    public LogAction() {
        keyMethodMap.put("log.filter", "filter");
        keyMethodMap.put("log.showMsg", "showMsg");
        keyMethodMap.put("log.download", "download");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.struts.actions.LookupDispatchAction#getKeyMethodMap()
     */
    @Override
    protected Map getKeyMethodMap() {
        return keyMethodMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.struts.actions.LookupDispatchAction#execute(org.apache.struts
     * .action.ActionMapping, org.apache.struts.action.ActionForm,
     * javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    public ActionForward filter(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        Connection conn = null;
        Statement stmt = null;
        ResultSet res = null;
        String connName = null;
        HttpSession session = request.getSession();
        try {
            Node node = XMLConfig.getNode("GVWorkbenchConfig.xml", "/GVWorkbenchConfig/LogConsole");
            connName = XMLConfig.get(node, "@jdbc-connection-name");

            DynaActionForm frm = (DynaActionForm) form;

            conn = JDBCConnectionBuilder.getConnection(connName);
            stmt = conn.createStatement();

            String selectList = XMLConfig.get(node, "LogFilter");

            Map<String, Object> props = new HashMap<String, Object>();

            String system = frm.getString("system");
            String dateFrom = frm.getString("dateFrom");
            String dateTo = frm.getString("dateTo");
            String service = frm.getString("service");
            String id = frm.getString("id");
            String severity = frm.getString("severity");
            String operation = "NULL"; // TODO va nei filtri?
            String order = "DESC"; // TODO va nei filtri?

            if (system == null || "".equals(system)) {
                system = "NULL";
            }
            if (service == null || "".equals(service)) {
                service = "NULL";
            }
            if (id == null || "".equals(id)) {
                id = "NULL";
            }
            if (severity == null || "".equals(severity)) {
                severity = "NULL";
            }

            props.put("SYSTEM", system);
            props.put("DATE_FROM", dateFrom);
            props.put("DATE_TO", dateTo);
            props.put("SERVICE", service);
            props.put("ID", id);
            props.put("PRIO", severity);
            props.put("OPERATION", operation);
            props.put("ORDER", order);

            String query = PropertiesHandler.expand(selectList, props);
            System.out.println("LogFilter: " + query);
            res = stmt.executeQuery(query);

            List<List<String>> rows = new ArrayList<List<String>>();
            while (res.next()) {
                List<String> col = new ArrayList<String>();
                col.add(res.getString("SOURCE"));
                col.add(res.getString("ID_MSG"));
                col.add(res.getString("TSTAMP"));
                col.add(res.getString("PRIO"));
                col.add(res.getString("IPRIO"));
                col.add(res.getString("CAT"));
                col.add(res.getString("THREAD"));
                col.add(res.getString("SERVER"));
                col.add(res.getString("ID"));
                col.add(res.getString("SYSTEM"));
                col.add(res.getString("SERVICE"));
                col.add(res.getString("OPERATION"));
                col.add(res.getString("MSG"));
                col.add(res.getString("THROWABLE_PRESENT"));

                rows.add(col);
            }

            session.setAttribute("iteratorListLog", rows);
        }
        catch (Exception e) {
            e.printStackTrace();

            session.setAttribute("iteratorListLog", new ArrayList<List<String>>());
        }
        finally {
            if (res != null) {
                try {
                    res.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (conn != null) {
                try {
                    JDBCConnectionBuilder.releaseConnection(connName, conn);
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }

        return mapping.findForward("home");
    }

    public ActionForward showMsg(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        ResultSet res = null;
        String connName = null;
        try {
            Node node = XMLConfig.getNode("GVWorkbenchConfig.xml", "/GVWorkbenchConfig/LogConsole");
            connName = XMLConfig.get(node, "@jdbc-connection-name");

            DynaActionForm frm = (DynaActionForm) form;
            conn = JDBCConnectionBuilder.getConnection(connName);
            stmt = conn.createStatement();

            String idMsg = frm.getString("id_msg");
            String dialog = frm.getString("dialogType");
            String selectMessage = XMLConfig.get(node, "Message");

            Map<String, Object> props = new HashMap<String, Object>();
            props.put("ID_MSG", idMsg);
            props.put("MSG_FIELD", dialog);

            String query = PropertiesHandler.expand(selectMessage, props);
            System.out.println("Message: " + query);
            res = stmt.executeQuery(query);

            res.next();
            Clob clob = res.getClob("MESSAGE");
            InputStream is = clob.getAsciiStream();
            byte[] buffer = new byte[2048];
            ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
            int size;
            while ((size = is.read(buffer)) != -1) {
                baos.write(buffer, 0, size);
            }
            is.close();
            // byte[] strMessagB = baos.toByteArray();
            byte[] strMessagB = XMLUtils.replaceXMLInvalidChars(new String(baos.toByteArray())).getBytes();

            HttpServletResponse resp = response;
            resp.setContentType("text/plain");
            resp.setContentLength(strMessagB.length);
            resp.setHeader("Connection", "close");
            resp.setHeader("Expires", "-1");
            resp.setHeader("Pragma", "no-cache");
            resp.setHeader("Cache-Control", "no-cache");

            OutputStream resstream = resp.getOutputStream();
            resstream.write(strMessagB);
            resstream.flush();
            resstream.close();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (res != null) {
                try {
                    res.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (conn != null) {
                try {
                    JDBCConnectionBuilder.releaseConnection(connName, conn);
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }

        return null;
    }

    public ActionForward download(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String FILTER_LOG_GV = "(.*\\.@{{DATE_A}}\\.log$)|(.*\\.@{{DATE_A}}\\.log\\.zip$)|(.*\\.@{{DATE_A}}\\.log\\.gz$)";
        String FILTER_LOG_JB = "(boot\\.log$)|(boot\\.log\\.zip$)|(boot\\.log\\.gz$)|(server\\.log\\.@{{DATE_A}}$)|(server\\.log\\.@{{DATE_A}}\\.zip$)|(server\\.log\\.@{{DATE_A}}\\.gz$)";
        String FILTER_LOG_JB_TODAY = "(boot\\.log$)|(boot\\.log\\.zip$)|(boot\\.log\\.gz$)|(server\\.log$)";

        try {
            Node node = XMLConfig.getNode("GVWorkbenchConfig.xml", "/GVWorkbenchConfig/LogConsole/LogDownload");
            String gvLogDir = XMLConfig.get(node, "@gv-log-dir", "sp{{gv.app.home}}/log");
            String gvLogFilter = XMLConfig.get(node, "@gv-log-filter", FILTER_LOG_GV);
            String jbLogDir = XMLConfig.get(node, "@jboss-log-dir", "sp{{jboss.server.log.dir}}");
            String jbLogFilter = XMLConfig.get(node, "@jboss-log-filter", FILTER_LOG_JB);

            DynaActionForm frm = (DynaActionForm) form;
            String date = DateUtils.convertString(frm.getString("date"), "dd/MM/yyyy", "yyyy-MM-dd");
            String today = DateUtils.nowToString("yyyy-MM-dd");
            if (today.equals(date)) {
                jbLogFilter = FILTER_LOG_JB_TODAY;
            }

            Map<String, Object> props = new HashMap<String, Object>();
            props.put("DATE_A", date);

            gvLogDir = PropertiesHandler.expand(gvLogDir, props);
            System.out.println("gvLogDir: " + gvLogDir);
            gvLogFilter = PropertiesHandler.expand(gvLogFilter, props);
            System.out.println("gvLogFilter: " + gvLogFilter);

            jbLogDir = PropertiesHandler.expand(jbLogDir, props);
            System.out.println("jbossLogDir: " + jbLogDir);
            jbLogFilter = PropertiesHandler.expand(jbLogFilter, props);
            System.out.println("jbossLogFilter: " + jbLogFilter);

            Set<String> matchingFiles = listFiles(gvLogDir, gvLogFilter, jbLogDir, jbLogFilter);
            
            String zipLogFile = PropertiesHandler.expand("GVLog_" + JMXEntryPoint.getServerName() + "_@{{DATE_A}}_timestamp{{yyyyMMddHHmmss}}.zip", props);
            zip(zipLogFile, matchingFiles);

            File zlFile = new File(PropertiesHandler.expand("${{java.io.tmpdir}}"), zipLogFile);
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(zlFile));
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition",
                        "attachment; filename=" + zipLogFile);
                response.setHeader("Connection", "close");
                response.setContentLength((int) zlFile.length());
                OutputStream resstream = response.getOutputStream();
                IOUtils.copy(is, resstream);
                resstream.flush();
                resstream.close();
            }
            finally {
                if (is != null) {
                    try {
                        is.close();
                    }
                    catch (Exception exc) {
                        // do nothing
                    }
                }
                FileUtils.deleteQuietly(zlFile);
            }
        }
        catch (Exception exc) {
            logger.error("Error processing log files download", exc);
        }

        return null;
    }
    
    
    private Set<String> listFiles(String gvLogDir, String gvLogFilter, String jbLogDir, String jbLogFilter) throws Exception {
        File targetDir = new File(gvLogDir);
        if (!targetDir.isAbsolute()) {
            throw new IllegalArgumentException("The pathname of the log directory is NOT absolute: " + gvLogDir);
        }
        if (!targetDir.exists()) {
            throw new IllegalArgumentException("Log directory (" + targetDir.getAbsolutePath()
                    + ") NOT found on local file system.");
        }
        if (!targetDir.isDirectory()) {
            throw new IllegalArgumentException("Log directory (" + targetDir.getAbsolutePath()
                    + ") is NOT a directory.");
        }
        File[] files = targetDir.listFiles(new RegExFileFilter(gvLogFilter, RegExFileFilter.FILES_ONLY, 0, false));
        Set<String> matchingFiles = new HashSet<String>(files.length);
        for (File file : files) {
            matchingFiles.add(file.getAbsolutePath());
        }
        
        targetDir = new File(jbLogDir);
        if (!targetDir.isAbsolute()) {
            throw new IllegalArgumentException("The pathname of the log directory is NOT absolute: " + jbLogDir);
        }
        if (!targetDir.exists()) {
            throw new IllegalArgumentException("Log directory (" + targetDir.getAbsolutePath()
                    + ") NOT found on local file system.");
        }
        if (!targetDir.isDirectory()) {
            throw new IllegalArgumentException("Log directory (" + targetDir.getAbsolutePath()
                    + ") is NOT a directory.");
        }
        files = targetDir.listFiles(new RegExFileFilter(jbLogFilter, RegExFileFilter.FILES_ONLY, 0, false));
        for (File file : files) {
            matchingFiles.add(file.getAbsolutePath());
        }

        return matchingFiles;
    }

    private void zip(String zipLogFile, Set<String> matchingFiles) {
        OutputStream out = null;
        try {
            File outFile = new File(PropertiesHandler.expand("${{java.io.tmpdir}}"), zipLogFile);
            out = new ZipOutputStream(new FileOutputStream(outFile));
            for (String logFile : matchingFiles) {
                InputStream in = null;
                try {
                    File inFile = new File(logFile);
                    in = new FileInputStream(logFile);
                    ZipEntry ze = new ZipEntry(inFile.getName());
                    ze.setTime(inFile.lastModified());
                    ((ZipOutputStream) out).putNextEntry(ze);
                    IOUtils.copy(in, out);
                }
                catch (Exception exc) {
                    logger.error("Error trying to compress the log file " + logFile, exc);
                }
                finally {
                    if (in != null) {
                        try {
                            in.close();
                        }
                        catch (Exception exc) {
                            // do nothing
                        }
                    }
                }
            }
        }
        catch (Exception exc) {
            logger.error("Error trying to compress the selected log files", exc);
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
    }

}
