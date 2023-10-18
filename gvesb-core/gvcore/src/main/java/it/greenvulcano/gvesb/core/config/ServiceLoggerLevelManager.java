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
package it.greenvulcano.gvesb.core.config;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.txt.PropertiesFileReader;

/**
 * <code>ServiceLoggerLevelManager</code> is the class for the configuration
 * of services/operation/subflow logger level.
 *
 * @version 3.4.0 Oct 18, 2023
 * @author GreenVulcano Developer Team
 *
 */

public class ServiceLoggerLevelManager implements ConfigurationListener
{
    private static Logger logger = GVLogger.getLogger(ServiceLoggerLevelManager.class);
    private static final String CFG_FILE_NAME = "ServiceLoggerLevel.properties";

    static public class LoggerLevelConfig {
        public Level level;
        public Map<String, LoggerLevelConfig> subLevel = new HashMap<String, LoggerLevelConfig>();

        public LoggerLevelConfig(Level level) {
            this.level = level;
        }

        @Override
        public String toString() {
            return toString(1);
        }

        public String toString(int i) {
            StringBuffer sb = new StringBuffer();
            sb.append("Level [" + this.level + "]");
            if (!this.subLevel.isEmpty()) {
                int subI = ++i;
                for (Map.Entry<String, LoggerLevelConfig> sl : this.subLevel.entrySet()) {
                    sb.append("\n").append(IND.substring(0,i)).append(sl.getKey()).append(" : ").append(sl.getValue().toString(subI));
                }
            }
            return sb.toString();
        }

        private static final String IND = "\t\t\t";
    }

    private Level defaultLevel;
    private Map<String, LoggerLevelConfig> serviceLevel = new HashMap<String, LoggerLevelConfig>();

    private static ServiceLoggerLevelManager instance = null;

    private ServiceLoggerLevelManager()
    {
        init();
    }

    public static synchronized ServiceLoggerLevelManager instance() {
        if (instance == null) {
            instance = new ServiceLoggerLevelManager();
            XMLConfig.addConfigurationListener(instance, GreenVulcanoConfig.getServicesConfigFileName());
        }
        return instance;
    }

    public Level getLoggerLevel(String service) {
        LoggerLevelConfig comp = this.serviceLevel.computeIfAbsent(service, k -> new LoggerLevelConfig(this.defaultLevel));
        return comp.level;
    }

    public Level getLoggerLevel(String service, String operation) {
        LoggerLevelConfig svc = this.serviceLevel.computeIfAbsent(service, k -> new LoggerLevelConfig(this.defaultLevel));
        LoggerLevelConfig op = svc.subLevel.computeIfAbsent(operation, k -> new LoggerLevelConfig(svc.level));
        return op.level;
    }

    public Level getLoggerLevel(String service, String operation, String subFlow) {
        LoggerLevelConfig svc = this.serviceLevel.computeIfAbsent(service, k -> new LoggerLevelConfig(this.defaultLevel));
        LoggerLevelConfig op = svc.subLevel.computeIfAbsent(operation, k -> new LoggerLevelConfig(svc.level));
        LoggerLevelConfig sf = op.subLevel.computeIfAbsent(subFlow, k -> new LoggerLevelConfig(op.level));
        return sf.level;
    }

    /**
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    @Override
    public void configurationChanged(ConfigurationEvent event)
    {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && event.getFile().equals(GreenVulcanoConfig.getServicesConfigFileName())) {
            reinit();
        }
    }

    public void reinit() {
        clear();
        init();
    }

    private void init() {
        this.defaultLevel = Level.INFO;
        Level mLevel = null;
        try {
            NMDC.push();
            NMDC.remove("MASTER_SERVICE");
            mLevel = GVLogger.setThreadMasterLevel(Level.INFO);

            Properties props = PropertiesFileReader.readFileFromCP(CFG_FILE_NAME);
            try {
                this.defaultLevel = Level.toLevel(PropertiesHandler.expand(props.getProperty("default")));
            }
            catch (Exception exc) {
                this.defaultLevel = Level.INFO;
            }

            List<String> keys = props.keySet().stream().map(Object::toString).sorted().collect(Collectors.toList());
            for (String key : keys) {
                if (!"default".equals(key)) {
                    String parts[] = key.split("\\.");
                    Level level = Level.toLevel(PropertiesHandler.expand(props.getProperty(key)));
                    final LoggerLevelConfig svc;
                    final LoggerLevelConfig op;
                    if (parts.length >= 1) {
                        svc = this.serviceLevel.computeIfAbsent(parts[0], k -> new LoggerLevelConfig((parts.length == 1) ? level : this.defaultLevel));
                        if (parts.length >= 2) {
                            op = svc.subLevel.computeIfAbsent(parts[1], k -> new LoggerLevelConfig((parts.length == 2) ? level : svc.level));
                            if (parts.length == 3) {
                                LoggerLevelConfig sf = op.subLevel.computeIfAbsent(parts[2], k -> new LoggerLevelConfig(level));
                            }
                        }
                    }
                }
            }
            System.out.println("Service Logger Level Manager initialized: " + toString());
            logger.info("Service Logger Level Manager initialized: " + toString());
        } catch (FileNotFoundException exc) {
            logger.warn("Not found configuration file for service logger level manager, default to INFO");
        } catch (Exception exc) {
            logger.error("Error initializing service logger level manager", exc);
        }
        finally {
            GVLogger.removeThreadMasterLevel(mLevel);
            NMDC.pop();
        }
    }

    public void clear() {
        this.serviceLevel.clear();
        this.defaultLevel = Level.INFO;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Default Level [" + this.defaultLevel + "]");
        if (!this.serviceLevel.isEmpty()) {
            List<String> services = this.serviceLevel.keySet().stream().sorted().collect(Collectors.toList());

            for (String service : services) {
                sb.append("\n").append(service).append(" : ").append(this.serviceLevel.get(service).toString(0));
            }
        }
        return sb.toString();
    }
}