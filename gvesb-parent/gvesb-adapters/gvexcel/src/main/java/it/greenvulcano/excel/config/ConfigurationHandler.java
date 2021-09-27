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

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.excel.exception.ExcelException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.thread.BaseThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class ConfigurationHandler implements ConfigurationListener
{
    private static Logger                                 logger            = GVLogger.getLogger(ConfigurationHandler.class);

    public static final String                            EF_CONFIG_FILE    = "GVExcelFormat-Configuration.xml";
    public static final String                            ER_CONFIG_FILE    = "GVExcelReport-Configuration.xml";
    public static final String                            MODULE_NAME       = "EXCEL_ENGINE";


    private static ConfigurationHandler                   instance          = null;
    private String                                        defaultWBConfName = null;
    private HashMap<String, WorkbookConfiguration>        wbConfiguration   = null;
    private HashMap<String, HashMap<String, ExcelReport>> excelReportGroups = null;

    public static void setLogContext()
    {
        NMDC.setModule(MODULE_NAME);
    }

    public static synchronized ConfigurationHandler getInstance() throws ExcelException
    {
        try {
            if (instance == null) {
                instance = new ConfigurationHandler();
                instance.init();
                XMLConfig.addConfigurationListener(instance, EF_CONFIG_FILE);
                XMLConfig.addConfigurationListener(instance, ER_CONFIG_FILE);
            }
        }
        catch (ExcelException exc) {
            instance = null;
            throw exc;
        }
        catch (Exception exc) {
            instance = null;
            throw new ExcelException(exc);
        }
        return instance;
    }

    public String getDefaultWBConfName()
    {
        return defaultWBConfName;
    }

    public WorkbookConfiguration getWBConf(String confName)
    {
        if (confName == null) {
            confName = defaultWBConfName;
        }
        WorkbookConfiguration workbookconfiguration = wbConfiguration.get(confName);
        if (workbookconfiguration != null) {
            return workbookconfiguration;
        }
        return wbConfiguration.get(defaultWBConfName);
    }

    public ExcelReport getExcelReport(String group, String name, Set<String> roles) throws ExcelException
    {
        ExcelReport report = null;
        HashMap<String, ExcelReport> excelReports = excelReportGroups.get(group);
        if (excelReports != null) {
            report = excelReports.get(name);
        }
        if (report != null) {
            if (!report.verifyRoles(roles)) {
                return null;
            }
        }
        return report;
    }

    public String[] getAvailableReports(Set<String> roles)
    {
        ArrayList<String> reports = new ArrayList<String>();
        Iterator<String> grpIt = excelReportGroups.keySet().iterator();
        while (grpIt.hasNext()) {
            String group = grpIt.next();
            HashMap<String, ExcelReport> excelReports = excelReportGroups.get(group);
            Iterator<String> repIt = excelReports.keySet().iterator();
            while (repIt.hasNext()) {
                String name = repIt.next();
                ExcelReport report = excelReports.get(name);
                if (report.verifyRoles(roles)) {
                    reports.add(name);
                }
            }
        }

        Collections.sort(reports);
        String as[] = new String[reports.size()];
        Iterator<String> it = reports.iterator();
        for (int i = 0; it.hasNext(); i++) {
            as[i] = it.next();
        }

        return as;
    }

    public String[] getAvailableGroups(Set<String> roles)
    {
        ArrayList<String> groups = new ArrayList<String>();
        Iterator<String> grpIt = excelReportGroups.keySet().iterator();
        while (grpIt.hasNext()) {
            String group = grpIt.next();
            HashMap<String, ExcelReport> excelReports = excelReportGroups.get(group);
            Iterator<String> repIt = excelReports.keySet().iterator();
            while (repIt.hasNext()) {
                ExcelReport report = excelReports.get(repIt.next());
                if (report.verifyRoles(roles)) {
                    groups.add(group);
                    break;
                }
            }
        }

        Collections.sort(groups);
        String as[] = new String[groups.size()];
        Iterator<String> it = groups.iterator();
        for (int i = 0; it.hasNext(); i++) {
            as[i] = it.next();
        }

        return as;
    }

    public String[] getAvailableReports(String group, Set<String> roles)
    {
        ArrayList<String> reports = new ArrayList<String>();
        HashMap<String, ExcelReport> excelReports = excelReportGroups.get(group);
        if (excelReports != null) {
            Iterator<String> repIt = excelReports.keySet().iterator();
            while (repIt.hasNext()) {
                String name = repIt.next();
                ExcelReport report = excelReports.get(name);
                if (report.verifyRoles(roles)) {
                    reports.add(name);
                }
            }
        }

        Collections.sort(reports);
        String as[] = new String[reports.size()];
        Iterator<String> it = reports.iterator();
        for (int i = 0; it.hasNext(); i++) {
            as[i] = it.next();
        }

        return as;
    }

    private ConfigurationHandler()
    {
        // do nothing
    }

    private void init() throws ExcelException
    {
        NMDC.push();
        ConfigurationHandler.setLogContext();
        try {
            try {
                defaultWBConfName = "default";
                wbConfiguration = new HashMap<String, WorkbookConfiguration>();
                NodeList nl = XMLConfig.getNodeList(EF_CONFIG_FILE, "//GVExcelWorkbook");
                if ((nl == null) || (nl.getLength() == 0)) {
                    throw new XMLConfigException("ExcelWorkbook node not found in file " + EF_CONFIG_FILE);
                }
                boolean flag = false;
                for (int j = 0; j < nl.getLength(); j++) {
                    Node node = nl.item(j);
                    WorkbookConfiguration wbCfg = new WorkbookConfiguration(node);
                    String name = wbCfg.getConfigName();
                    wbConfiguration.put(name, wbCfg);
                    logger.debug("Inizialized ExcelWorkbook '" + name + "'");
                    if (name.equals(defaultWBConfName)) {
                        flag = true;
                    }
                }

                if (!flag) {
                    defaultWBConfName = XMLConfig.get(nl.item(0), "@configName", "default");
                }
                logger.debug("Default ExcelWorkbook= '" + defaultWBConfName + "'");
            }
            catch (XMLConfigException exc) {
                throw new ExcelException("Error initializing ExcelWorkbook", exc);
            }

            try {
                excelReportGroups = new HashMap<String, HashMap<String, ExcelReport>>();
                NodeList nl = XMLConfig.getNodeList(ER_CONFIG_FILE, "//GVExcelReport");
                for (int i = 0; i < nl.getLength(); i++) {
                    Node node = nl.item(i);
                    ExcelReport excelreport = new ExcelReport(node);
                    logger.debug("Initialized ExcelReport '" + excelreport.getGroup() + "::" + excelreport.getName()
                            + "'");
                    HashMap<String, ExcelReport> excelReports = excelReportGroups.get(excelreport.getGroup());
                    if (excelReports == null) {
                        excelReports = new HashMap<String, ExcelReport>();
                        excelReportGroups.put(excelreport.getGroup(), excelReports);
                    }
                    excelReports.put(excelreport.getName(), excelreport);
                }

            }
            catch (XMLConfigException exc) {
                throw new ExcelException("Errore nella configurazione degli Excel Report", exc);
            }
        }
        catch (ExcelException exc) {
            throw exc;
        }
        finally {
            NMDC.pop();
        }
    }

    @Override
    public void configurationChanged(ConfigurationEvent event)
    {
        if ((event.getFile().equals(EF_CONFIG_FILE) || event.getFile().equals(ER_CONFIG_FILE))
                && (event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED)) {
            //XMLConfig.removeConfigurationListener(instance);
            //instance = null;
            
        	if (excelReportGroups != null) excelReportGroups.clear();
            excelReportGroups = null;
            if (wbConfiguration != null) wbConfiguration.clear();
            wbConfiguration = null;
            defaultWBConfName = null;
            // initialize after a delay
            Runnable rr = new Runnable() {
                @Override
                public void run()
                {
                    try {
                        Thread.sleep(5000);
                    }
                    catch (InterruptedException exc) {
                        // do nothing
                    }
                    try {
                        init();
                    }
                    catch (ExcelException exc) {
                        // TODO Auto-generated catch block
                        exc.printStackTrace();
                    }
                }
            };

            BaseThread bt = new BaseThread(rr, "Config reloader for Excel ConfigurationHandler");
            bt.setDaemon(true);
            bt.start();
        }
    }

}
