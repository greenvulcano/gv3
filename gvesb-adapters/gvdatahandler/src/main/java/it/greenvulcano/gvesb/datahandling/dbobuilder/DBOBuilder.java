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
package it.greenvulcano.gvesb.datahandling.dbobuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.datahandling.DHResult;
import it.greenvulcano.gvesb.datahandling.DataHandlerException;
import it.greenvulcano.gvesb.datahandling.IDBO;
import it.greenvulcano.gvesb.datahandling.IDBOBuilder;
import it.greenvulcano.gvesb.datahandling.utils.AbstractRetriever;
import it.greenvulcano.gvesb.datahandling.utils.DiscardCause;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleExceptionHandler;
import it.greenvulcano.gvesb.gvdte.controller.DTEController;
import it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.bin.Dump;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.xml.XMLUtils;

/**
 * <code>DBOBuilder</code> is the class that holds the creation logic of IDBO
 * objects. Its role is to invoke the DTE to make transformations and
 * initializing the IDBO objects that will physically manipulate the DB. It's
 * based on a connection requested to the <code>JdbcDataBaseConnection</code>
 * and released at the end of operations. This component, to handle the
 * concurrent behavior, is designed as thread-safe module.
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class DBOBuilder implements IDBOBuilder
{
    private class MergeInfo {
        String source;
        String xpathSrc;
        String xpathDest;

        /**
         * @param source
         * @param xpathSrc
         * @param xpathDest
         */
        public MergeInfo(String source, String xpathSrc, String xpathDest) {
            this.source = source;
            this.xpathSrc = xpathSrc;
            this.xpathDest = xpathDest;
        }

        @Override
        public String toString() {
            return "Source: " + this.source + " - xpathSrc: " + this.xpathSrc + " - xpathDest: " + this.xpathDest;
        }
    }

    private Vector<IDBO>              dboList            = null;
    private Map<String, IDBO>         dboOutputMap       = null;

    /**
     * Configured properties to eventually overwrite in the service call.
     */
    private Map<String, String>       baseProps;

    private boolean                   resolveMetadata;

    private int                       internalIdx;

    private Node                      configurationNode  = null;

    private DTEController             dteController      = null;

    private String                    jdbcConnectionName = null;

    private boolean                   transacted         = true;
    private boolean                   isXA               = false;

    private String                    serviceName        = null;

    private String                    outputDataName     = null;
    private String                    statsDataName      = null;
    private final static String       ALL_STATS          = "ALL";

    private static final Logger       logger             = GVLogger.getLogger(DBOBuilder.class);

    private final Map<String, Object> dataCache          = new HashMap<String, Object>();
    private final List<MergeInfo>           mergeList          = new Vector<MergeInfo>();

    private int                       makeDump           = DUMP_TEXT;

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node builder) throws DataHandlerException
    {
        this.dboList = new Vector<IDBO>();
        this.dboOutputMap = new HashMap<String, IDBO>();
        this.baseProps = new HashMap<String, String>();
        String dboClassName = "";

        NMDC.push();
        try {
            NMDC.put("DH_SERVICE", "");
            logger.debug("DBOBuilder initialized with node [" + builder.toString() + "].");
            String sDump = XMLConfig.get(builder, "@make-dump", "text");
            if (sDump.equals("none")) {
                this.makeDump = DUMP_NONE;
            }
            else if (sDump.equals("hex")) {
                this.makeDump = DUMP_HEX;
            }
            else {
                this.makeDump = DUMP_TEXT;
            }
            this.serviceName = XMLConfig.get(builder, "@name");
            NMDC.put("DH_SERVICE", this.serviceName);
            this.jdbcConnectionName = XMLConfig.get(builder, "@jdbc-connection-name");
            logger.debug("Connection = " + this.jdbcConnectionName);
            this.transacted = XMLConfig.getBoolean(builder, "@transacted", true);
            logger.debug("Execute in transaction: " + this.transacted + ".");
            this.isXA = XMLConfig.getBoolean(builder, "@isXA", false);
            logger.debug("Is XA: " + this.isXA + ".");

            logger.debug("Listing for DBOs.");
            NodeList dbosNodes = XMLConfig.getNodeList(builder, "*[@type='dbo']");
            IDBO idbo = null;
            for (int i = 0; i < dbosNodes.getLength(); i++) {
                Node dboNode = dbosNodes.item(i);
                dboClassName = XMLConfig.get(dboNode, "@class");

                idbo = (IDBO) Class.forName(dboClassName).newInstance();
                idbo.init(dboNode);
                idbo.setServiceName(this.serviceName);
                idbo.setTransacted(this.transacted);
                idbo.setJdbcConnectionName(this.jdbcConnectionName);
                this.dboList.add(idbo);
                this.dboOutputMap.put(idbo.getOutputDataName(), idbo);
                logger.debug("Added a IDBO class [" + dboClassName + "].");
            }

            this.outputDataName = XMLConfig.get(builder, "@output-data", idbo.getOutputDataName());
            // stats-data = ALL ensure to add all the statistics by DHResult
            this.statsDataName = XMLConfig.get(builder, "@output-stats", this.outputDataName);

            this.resolveMetadata = XMLConfig.getBoolean(builder, "DHVariables/@resolve-metadata-on-call", true);
            NodeList nlv = XMLConfig.getNodeList(builder, "DHVariables/DHVariable");
            if (nlv != null) {
                for (int i = 0; i < nlv.getLength(); i++) {
                    Node nv = nlv.item(i);
                    this.baseProps.put(XMLConfig.get(nv, "@name"),
                            XMLConfig.get(nv, "@value", XMLConfig.get(nv, ".", "")).trim());
                }
            }

            NodeList mergeNodes = XMLConfig.getNodeList(builder, "XMLMerge/MergeInfo");
            for (int i = 0; i < mergeNodes.getLength(); i++) {
                Node mergeNode = mergeNodes.item(i);
                MergeInfo mergeInfo = new MergeInfo(XMLConfig.get(mergeNode, "@source"), XMLConfig.get(mergeNode, "@xpath-source"),
                        XMLConfig.get(mergeNode, "@xpath-dest"));
                this.mergeList.add(mergeInfo);
                logger.debug("Added a MergeInfo[" + mergeInfo + "].");
            }
        }
        catch (XMLConfigException exc) {
            logger.error("Error reading configuration", exc);
            throw new DataHandlerException("Error reading configuration", exc);
        }
        catch (IllegalAccessException exc) {
            logger.error("Error accessing IDBO class '" + dboClassName + "'", exc);
            throw new DataHandlerException("Error accessing IDBO class '" + dboClassName + "'", exc);
        }
        catch (InstantiationException exc) {
            logger.error("Error instantiating IDBO class '" + dboClassName + "'", exc);
            throw new DataHandlerException("Error instantiating IDBO class '" + dboClassName + "'", exc);
        }
        catch (ClassNotFoundException exc) {
            logger.error("Error creating IDBO class '" + dboClassName + "'", exc);
            throw new DataHandlerException("Error creating IDBO class '" + dboClassName + "'", exc);
        }
        catch (Exception exc) {
            logger.error("Unhandled exception in IDBO initialization", exc);
            throw new DataHandlerException("Unhandled exception in IDBO initialization", exc);
        }
        finally {
            NMDC.pop();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#XML2DB(java.lang.String,
     *      Object object, java.util.Map)
     */
    @Override
    public void XML2DB(String operation, Object object, Map<String, Object> params) throws DataHandlerException,
            InterruptedException {
        long start = System.currentTimeMillis();
        long timeConn = 0;
        long timeTransf = 0;
        Map<String, Object> localParams = buildProps(params);
        NMDC.push();
        NMDC.put("DH_SERVICE", this.serviceName);
        logger.debug("Start executing XML2DB [" + operation + "]\n\tParams    : " + localParams.toString());
        if (logger.isDebugEnabled() && (object != null) && (this.makeDump != DUMP_NONE)) {
            if (this.makeDump == DUMP_HEX) {
                if (object instanceof byte[]) {
                    logger.debug("Input data: [\n" + new Dump((byte[]) object, -1) + "\n].");
                }
            }
            else if (object instanceof Node){
                try {
                    logger.debug("Input data: [\n" + XMLUtils.serializeDOM_S((Node)object) + "\n].");
                }
                catch (Exception exc) {
                    logger.debug("Input data: [\nDUMP ERROR!!!!!\n].");
                }
            }
            else {
                logger.debug("Input data: [\n" + new String(object.toString()) + "\n].");
            }
        }
        this.internalIdx = 0;
        Connection conn = null;
        String intConnName = null;
        try {
            intConnName = (String) localParams.get(DBO_JDBC_CONNECTION_NAME);
            if ((intConnName != null) && !"".equals(intConnName) && !"NULL".equals(intConnName)) {
                logger.info("Overwriting default Connection with: " + intConnName);
            }
            else {
                intConnName = this.jdbcConnectionName;
            }
            intConnName = PropertiesHandler.expand(intConnName, localParams);
            logger.info("Searching for a new available connection named [" + intConnName + "].");

            long startConn = System.currentTimeMillis();
            conn = JDBCConnectionBuilder.getConnection(intConnName);
            timeConn = System.currentTimeMillis() - startConn;
            if (this.transacted && !this.isXA) {
                conn.setAutoCommit(false);
            }

            // Static utility classes initialization
            AbstractRetriever.setAllConnection(conn, this.configurationNode);

            while (hasNext()) {
                ThreadUtils.checkInterrupted(getClass().getSimpleName(), this.serviceName, logger);

                IDBO idbo = nextDBO();
                NMDC.push();
                try {
                    NMDC.put("DH_DBO", idbo.getName());
                    logger.debug("Start executing of IDBO [" + idbo.toString() + "].");
                    long startTransf = System.currentTimeMillis();
                    Object xmlFile = transform(idbo, object, localParams);
                    long lTimeTransf = System.currentTimeMillis() - startTransf;
                    timeTransf += lTimeTransf;

                    if (xmlFile != null) {
                        if (logger.isDebugEnabled() && (this.makeDump != DUMP_NONE)) {
                            if (xmlFile instanceof byte[]) {
                                if (this.makeDump == DUMP_HEX) {
                                    logger.debug("Transformation output: [\n" + new Dump((byte[]) xmlFile, -1) + "\n].");
                                }
                                else {
                                    logger.debug("Transformation output: [\n" + new String((byte[]) xmlFile) + "\n].");
                                }
                            }
                            else if (xmlFile instanceof Node) {
                                try {
                                    logger.debug("Transformation output: [\n" + XMLUtils.serializeDOM_S((Node) xmlFile)
                                            + "\n].");
                                }
                                catch (Exception exc) {
                                    logger.debug("Transformation output: [\nDUMP ERROR!!!!!\n].");
                                }
                            }
                            else {
                                logger.debug("Transformation output: [\n" + xmlFile + "\n].");
                            }
                        }
                    }
                    idbo.executeIn(xmlFile, conn, localParams);
                    logger.info("End executing of IDBO [" + idbo.toString() + "]. ExecutionTime: " + getPartialTime(start) + " - ConnectionTime: " + formatPartialTime(idbo.getTimeConn()) + " - TransformationTime: " + formatPartialTime(lTimeTransf));
                }
                finally {
                    NMDC.pop();
                }
            }
            if (this.transacted && !this.isXA) {
                logger.debug("Committing XML2DB [" + operation + "].");
                conn.commit();
            }
        }
        catch (SQLException exc) {
            if (conn != null) {
                if (this.transacted && !this.isXA) {
                    try {
                        logger.warn("Rolling-back XML2DB [" + operation + "].");
                        conn.rollback();
                    }
                    catch (Exception ex) {
                        // Nothing to do
                    }
                }
            }
            OracleExceptionHandler.handleSQLException(exc);
            throw new DataHandlerException("SQL Exception: " + exc.getMessage(), exc);
        }
        catch (Exception exc) {
            if (conn != null) {
                if (this.transacted && !this.isXA) {
                    try {
                        logger.warn("Rolling-back XML2DB [" + operation + "].");
                        conn.rollback();
                    }
                    catch (Exception ex) {
                        // Nothing to do
                    }
                }
            }
            logger.error("Unhandled Exception", exc);
            ThreadUtils.checkInterrupted(exc);
            throw new DataHandlerException("Unhandled Exception: " + exc.getMessage(), exc);
        }
        finally {
            cleanup();
            try {
                JDBCConnectionBuilder.releaseConnection(intConnName, conn);
            }
            catch (Exception exc) {
                // do nothing
            }
            logger.info("End executing XML2DB [" + operation + "]. ExecutionTime: " + getPartialTime(start) + " - ConnectionTime: " + formatPartialTime(timeConn) + " - TransformationTime: " + formatPartialTime(timeTransf));
            NMDC.pop();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#DB2XML(java.lang.String,
     *      java.lang.Object, java.util.Map)
     */
    @Override
    public Object DB2XML(String operation, Object object, Map<String, Object> params) throws DataHandlerException,
            InterruptedException {
        long start = System.currentTimeMillis();
        long timeConn = 0;
        long timeTransf = 0;
        Map<String, Object> localParams = buildProps(params);
        NMDC.push();
        NMDC.put("DH_SERVICE", this.serviceName);
        logger.debug("Start executing DB2XML [" + operation + "]\n\tParams    : " + localParams.toString());
        this.internalIdx = 0;
        Connection conn = null;
        String intConnName = null;
        try {
            intConnName = (String) localParams.get(DBO_JDBC_CONNECTION_NAME);
            if ((intConnName != null) && !"".equals(intConnName) && !"NULL".equals(intConnName)) {
                logger.info("Overwriting default Connection with: " + intConnName);
            }
            else {
                intConnName = this.jdbcConnectionName;
            }
            intConnName = PropertiesHandler.expand(intConnName, localParams);
            logger.info("Searching for a new available connection named [" + intConnName + "].");

            long startConn = System.currentTimeMillis();
            conn = JDBCConnectionBuilder.getConnection(intConnName);
            timeConn = System.currentTimeMillis() - startConn;
            if (this.transacted) {
                conn.setAutoCommit(false);
            }

            // Static utility classes initialization
            AbstractRetriever.setAllConnection(conn, this.configurationNode);

            IDBO idbo = firstDBO();
            this.dataCache.put(idbo.getInputDataName(), object);
            while (hasNext()) {
                ThreadUtils.checkInterrupted(getClass().getSimpleName(), this.serviceName, logger);
                idbo = nextDBO();
                NMDC.push();
                try {
                    NMDC.put("DH_DBO", idbo.getName());
                    if (idbo.getForcedMode().equals(IDBO.MODE_XML2DB)) {
                        logger.debug("Start executing IDBO [" + idbo.toString() + "] in forced XML2DB mode.");
                        long startTransf = System.currentTimeMillis();
                        Object xmlFile = transform(idbo, this.dataCache.get(idbo.getInputDataName()), localParams);
                        long lTimeTransf = System.currentTimeMillis() - startTransf;
                        timeTransf += lTimeTransf;

                        if (xmlFile != null) {
                            this.dataCache.put(idbo.getOutputDataName(), xmlFile);
                            if (logger.isDebugEnabled() && (this.makeDump != DUMP_NONE)) {
                                if (xmlFile instanceof byte[]) {
                                    if (this.makeDump == DUMP_HEX) {
                                        logger.debug("Transformation output: [\n" + new Dump((byte[]) xmlFile, -1)
                                                + "\n].");
                                    }
                                    else {
                                        logger.debug("Transformation output: [\n" + new String((byte[]) xmlFile)
                                                + "\n].");
                                    }
                                }
                                else if (xmlFile instanceof Node) {
                                    try {
                                        logger.debug("Transformation output: [\n" + XMLUtils.serializeDOM_S((Node) xmlFile)
                                                + "\n].");
                                    }
                                    catch (Exception exc) {
                                        logger.debug("Transformation output: [\nDUMP ERROR!!!!!\n].");
                                    }
                                }
                                else {
                                    logger.debug("Transformation output: [\n" + xmlFile + "\n].");
                                }
                            }
                        }
                        idbo.executeIn(null, conn, localParams);
                        logger.debug("End executing IDBO [" + idbo.toString() + "] in forced XML2DB mode. ExecutionTime: " + getPartialTime(start) + " - ConnectionTime: " + formatPartialTime(idbo.getTimeConn()) + " - TransformationTime: " + formatPartialTime(lTimeTransf));
                    }
                    else {
                        logger.debug("Start executing IDBO [" + idbo.toString() + "] in normal mode.");
                        Object output = idbo.executeOut(conn, localParams);
                        if (logger.isDebugEnabled() && (output != null) && (this.makeDump != DUMP_NONE)) {
                            if (output instanceof byte[]) {
                                if (this.makeDump == DUMP_HEX) {
                                    logger.debug("Received data from DB: [\n" + new Dump((byte[]) output, -1)
                                            + "\n].");
                                }
                                else {
                                    logger.debug("Received data from DB: [\n" + new String((byte[]) output)
                                            + "\n].");
                                }
                            }
                            else if (output instanceof Node) {
                                try {
                                    logger.debug("Received data from DB: [\n" + XMLUtils.serializeDOM_S((Node) output)
                                            + "\n].");
                                }
                                catch (Exception exc) {
                                    logger.debug("Received data from DB: [\nDUMP ERROR!!!!!\n].");
                                }
                            }
                            else {
                                logger.debug("Received data from DB: [\n" + output + "\n].");
                            }
                        }
                        long startTransf = System.currentTimeMillis();
                        Object xmlFile = transform(idbo, output, localParams);
                        long lTimeTransf = System.currentTimeMillis() - startTransf;
                        timeTransf += lTimeTransf;
                        this.dataCache.put(idbo.getOutputDataName(), xmlFile);
                        logger.info("End executing IDBO [" + idbo.toString() + "] in normal mode. ExecutionTime: " + getPartialTime(start) + " - ConnectionTime: " + formatPartialTime(idbo.getTimeConn()) + " - TransformationTime: " + formatPartialTime(lTimeTransf));
                    }
                }
                finally {
                    NMDC.pop();
                }
            }
            if (this.transacted && !this.isXA) {
                logger.debug("Committing DB2XML [" + operation + "].");
                conn.commit();
            }
            Object xmlFile = null;

            if (this.mergeList.size() < 2) {
                xmlFile = XMLUtils.parseObject_S(this.dataCache.get(this.outputDataName), false, true);
            }
            else {
                XMLUtils parser = null;
                try {
                    parser = XMLUtils.getParserInstance();
                    MergeInfo mergeDest = this.mergeList.get(0);
                    Document dest = (Document) parser.parseObject(this.dataCache.get(mergeDest.source), false, true);
                    for (int i = 1; i < this.mergeList.size(); i++) {
                        MergeInfo mergeInfo = this.mergeList.get(i);
                        Object srcO = this.dataCache.get(mergeInfo.source);
                        if (srcO != null) {
                            Document src = (Document) parser.parseObject(srcO, false, true);
                            NodeList sources = parser.selectNodeList(src, mergeInfo.xpathSrc);
                            if (sources.getLength() > 0) {
                                Node destNode = parser.selectSingleNode(dest, mergeInfo.xpathDest);
                                for (int j = 0; j < sources.getLength(); ++j) {
                                    Node node = sources.item(j);
                                    node = dest.importNode(node, true);
                                    destNode.appendChild(node);
                                }
                            }
                        }
                    }

                    xmlFile = dest;
                }
                finally {
                    XMLUtils.releaseParserInstance(parser);
                }
            }

            if (logger.isDebugEnabled() && (xmlFile != null) && (this.makeDump != DUMP_NONE)) {
                if (xmlFile instanceof byte[]) {
                    if (this.makeDump == DUMP_HEX) {
                        logger.debug("Returning data: [\n" + new Dump((byte[]) xmlFile, -1)
                                + "\n].");
                    }
                    else {
                        logger.debug("Returning data: [\n" + new String((byte[]) xmlFile)
                                + "\n].");
                    }
                }
                else if (xmlFile instanceof Node) {
                    try {
                        logger.debug("Returning data: [\n" + XMLUtils.serializeDOM_S((Node) xmlFile)
                                + "\n].");
                    }
                    catch (Exception exc) {
                        logger.debug("Returning data: [\nDUMP ERROR!!!!!\n].");
                    }
                }
                else {
                    logger.debug("Returning data: [\n" + xmlFile + "\n].");
                }
            }
            return xmlFile;
        }
        catch (SQLException exc) {
            logger.error("Unhandled SQL exception.", exc);
            if (conn != null) {
                if (this.transacted && !this.isXA) {
                    try {
                        logger.warn("Rolling-back DB2XML [" + operation + "].");
                        conn.rollback();
                    }
                    catch (Exception ex) {
                        // Nothing to do
                    }
                }
            }
            OracleExceptionHandler.handleSQLException(exc);
            throw new DataHandlerException("SQL Exception: " + exc.getMessage(), exc);
        }
        catch (Exception exc) {
            if (conn != null) {
                if (this.transacted && !this.isXA) {
                    try {
                        logger.warn("Rolling-back DB2XML [" + operation + "].");
                        conn.rollback();
                    }
                    catch (Exception ex) {
                        // Nothing to do
                    }
                }
            }
            logger.warn("Unhandled Exception", exc);
            ThreadUtils.checkInterrupted(exc);
            throw new DataHandlerException("Unhandled Exception: " + exc.getMessage(), exc);
        }
        finally {
            cleanup();
            try {
                JDBCConnectionBuilder.releaseConnection(intConnName, conn);
            }
            catch (Exception exc) {
                // do nothing
            }
            logger.info("End executing DB2XML [" + operation + "]. ExecutionTime: " + getPartialTime(start) + " - ConnectionTime: " + formatPartialTime(timeConn) + " - TransformationTime: " + formatPartialTime(timeTransf));
            NMDC.pop();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#CALL(java.lang.String,
     *      java.lang.Object, java.util.Map)
     */
    @Override
    public Object CALL(String operation, Object object, Map<String, Object> params) throws DataHandlerException,
            InterruptedException {
        long start = System.currentTimeMillis();
        long timeConn = 0;
        long timeTransf = 0;
        Map<String, Object> localParams = buildProps(params);
        NMDC.push();
        NMDC.put("DH_SERVICE", this.serviceName);
        logger.debug("Start executing CALL [" + operation + "]\n\tParams    :" + localParams.toString());
        if (logger.isDebugEnabled() && (object != null) && (this.makeDump != DUMP_NONE)) {
            if (object instanceof byte[]) {
                if (this.makeDump == DUMP_HEX) {
                    logger.debug("Input data: [\n" + new Dump((byte[]) object, -1)
                            + "\n].");
                }
                else {
                    logger.debug("Input data: [\n" + new String((byte[]) object)
                            + "\n].");
                }
            }
            else if (object instanceof Node) {
                try {
                    logger.debug("Input data: [\n" + XMLUtils.serializeDOM_S((Node) object)
                            + "\n].");
                }
                catch (Exception exc) {
                    logger.debug("Input data: [\nDUMP ERROR!!!!!\n].");
                }
            }
            else {
                logger.debug("Input data: [\n" + object + "\n].");
            }
        }
        this.internalIdx = 0;
        Connection conn = null;
        String intConnName = null;
        try {
            intConnName = (String) localParams.get(DBO_JDBC_CONNECTION_NAME);
            if ((intConnName != null) && !"".equals(intConnName) && !"NULL".equals(intConnName)) {
                logger.info("Overwriting default Connection with: " + intConnName);
            }
            else {
                intConnName = this.jdbcConnectionName;
            }
            intConnName = PropertiesHandler.expand(intConnName, localParams);
            logger.info("Searching for a new available connection named [" + intConnName + "].");

            long startConn = System.currentTimeMillis();
            conn = JDBCConnectionBuilder.getConnection(intConnName);
            timeConn = System.currentTimeMillis() - startConn;
            if (this.transacted && !this.isXA) {
                conn.setAutoCommit(false);
            }

            // Static utility classes initialization
            AbstractRetriever.setAllConnection(conn, this.configurationNode);

            IDBO idbo = firstDBO();
            this.dataCache.put(idbo.getInputDataName(), object);
            while (hasNext()) {
                ThreadUtils.checkInterrupted(getClass().getSimpleName(), this.serviceName, logger);
                idbo = nextDBO();
                NMDC.push();
                try {
                    NMDC.put("DH_DBO", idbo.getName());

                    logger.debug("Start executing IDBO [" + idbo.toString() + "].");
                    long startTransf = System.currentTimeMillis();
                    Object xmlFile = transform(idbo, object, localParams);
                    long lTimeTransf = System.currentTimeMillis() - startTransf;
                    timeTransf += lTimeTransf;
                    if (xmlFile != null) {
                        this.dataCache.put(idbo.getOutputDataName(), xmlFile);
                        if (logger.isDebugEnabled() && (this.makeDump != DUMP_NONE)) {
                            if (xmlFile instanceof byte[]) {
                                if (this.makeDump == DUMP_HEX) {
                                    logger.debug("Transformation output: [\n" + new Dump((byte[]) xmlFile, -1) + "\n].");
                                }
                                else {
                                    logger.debug("Transformation output: [\n" + new String((byte[]) xmlFile) + "\n].");
                                }
                            }
                            else if (xmlFile instanceof Node) {
                                try {
                                    logger.debug("Transformation output: [\n" + XMLUtils.serializeDOM_S((Node) xmlFile)
                                            + "\n].");
                                }
                                catch (Exception exc) {
                                    logger.debug("Transformation output: [\nDUMP ERROR!!!!!\n].");
                                }
                            }
                            else {
                                logger.debug("Transformation output: [\n" + xmlFile + "\n].");
                            }
                        }
                    }
                    xmlFile = idbo.executeInOut(xmlFile, conn, localParams);

                    this.dataCache.put(idbo.getOutputDataName(), xmlFile);
                    logger.info("End executing IDBO [" + idbo.toString() + "]. ExecutionTime: " + getPartialTime(start) + " - ConnectionTime: " + formatPartialTime(idbo.getTimeConn()) + " - TransformationTime: " + formatPartialTime(lTimeTransf));
                }
                finally {
                    NMDC.pop();
                }
            }
            if (this.transacted && !this.isXA) {
                logger.debug("Committing CALL [" + operation + "].");
                conn.commit();
            }
            Object xmlFile = this.dataCache.get(this.outputDataName);
            if (logger.isDebugEnabled() && (xmlFile != null) && (this.makeDump != DUMP_NONE)) {
                if (xmlFile instanceof byte[]) {
                    if (this.makeDump == DUMP_HEX) {
                        logger.debug("Returning data: [\n" + new Dump((byte[]) xmlFile, -1)
                                + "\n].");
                    }
                    else {
                        logger.debug("Returning data: [\n" + new String((byte[]) xmlFile)
                                + "\n].");
                    }
                }
                else if (xmlFile instanceof Node) {
                    try {
                        logger.debug("Returning data: [\n" + XMLUtils.serializeDOM_S((Node) xmlFile)
                                + "\n].");
                    }
                    catch (Exception exc) {
                        logger.debug("Returning data: [\nDUMP ERROR!!!!!\n].");
                    }
                }
                else {
                    logger.debug("Returning data: [\n" + xmlFile + "\n].");
                }
            }
            return xmlFile;
        }
        catch (SQLException exc) {
            if (conn != null) {
                if (this.transacted && !this.isXA) {
                    try {
                        logger.warn("Rolling-back CALL [" + operation + "].");
                        conn.rollback();
                    }
                    catch (Exception ex) {
                        // Nothing to do
                    }
                }
            }
            OracleExceptionHandler.handleSQLException(exc);
            throw new DataHandlerException("SQL Exception: " + exc.getMessage(), exc);
        }
        catch (Exception exc) {
            if (conn != null) {
                if (this.transacted && !this.isXA) {
                    try {
                        logger.warn("Rolling-back CALL [" + operation + "].");
                        conn.rollback();
                    }
                    catch (Exception ex) {
                        // Nothing to do
                    }
                }
            }
            logger.error("Unhandled Exception", exc);
            ThreadUtils.checkInterrupted(exc);
            throw new DataHandlerException("Unhandled Exception: " + exc.getMessage(), exc);
        }
        finally {
            cleanup();
            try {
                JDBCConnectionBuilder.releaseConnection(intConnName, conn);
            }
            catch (Exception exc) {
                // do nothing
            }
            logger.info("End executing CALL [" + operation + "]. ExecutionTime: " + getPartialTime(start) + " - ConnectionTime: " + formatPartialTime(timeConn) + " - TransformationTime: " + formatPartialTime(timeTransf));
            NMDC.pop();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#EXECUTE(java.lang.String,
     *      java.lang.Object, java.util.Map)
     */
    @Override
    public DHResult EXECUTE(String operation, Object object, Map<String, Object> params) throws DataHandlerException,
            InterruptedException {
        long start = System.currentTimeMillis();
        long timeConn = 0;
        long timeTransf = 0;
        Map<String, Object> localParams = buildProps(params);
        NMDC.push();
        NMDC.put("DH_SERVICE", this.serviceName);
        logger.debug("Start executing EXECUTE [" + operation + "]\n\tParams    : " + localParams.toString());
        if (logger.isDebugEnabled() && (object != null) && (this.makeDump != DUMP_NONE)) {
            if (object instanceof byte[]) {
                if (this.makeDump == DUMP_HEX) {
                    logger.debug("Input data: [\n" + new Dump((byte[]) object, -1) + "\n].");
                }
                else {
                    logger.debug("Input data: [\n" + new String((byte[]) object) + "\n].");
                }
            }
            else if (object instanceof Node) {
                try {
                    logger.debug("Input data: [\n" + XMLUtils.serializeDOM_S((Node) object) + "\n].");
                }
                catch (Exception exc) {
                    logger.debug("Input data: [\nDUMP ERROR!!!!!\n].", exc);
                }
            }
            else {
                logger.debug("Input data: [\n" + object.toString() + "\n].");
            }
        }
        this.internalIdx = 0;
        Connection conn = null;
        String intConnName = null;
        try {
            intConnName = (String) localParams.get(DBO_JDBC_CONNECTION_NAME);
            if ((intConnName != null) && !"".equals(intConnName) && !"NULL".equals(intConnName)) {
                logger.info("Overwriting default Connection with: " + intConnName);
            }
            else {
                intConnName = this.jdbcConnectionName;
            }
            intConnName = PropertiesHandler.expand(intConnName, localParams);
            logger.info("Searching for a new available connection named [" + intConnName + "].");

            long startConn = System.currentTimeMillis();
            conn = JDBCConnectionBuilder.getConnection(intConnName);
            timeConn = System.currentTimeMillis() - startConn;
            if (this.transacted && !this.isXA) {
                conn.setAutoCommit(false);
            }

            // Static utility classes initialization
            AbstractRetriever.setAllConnection(conn, this.configurationNode);

            IDBO idbo = firstDBO();
            {
                DHResult dhrL = new DHResult(idbo.getExecutionResult());
                dhrL.setData(object);
                this.dataCache.put(idbo.getInputDataName(), dhrL);
            }
            while (hasNext()) {
                ThreadUtils.checkInterrupted(getClass().getSimpleName(), this.serviceName, logger);
                idbo = nextDBO();
                NMDC.push();
                try {
                    NMDC.put("DH_DBO", idbo.getName());
                    if (idbo.getForcedMode().equals(IDBO.MODE_XML2DB)) {
                        logger.debug("Start executing IDBO [" + idbo.toString() + "] in forced XML2DB mode.");
                        DHResult dhrL = (DHResult) this.dataCache.get(idbo.getInputDataName());
                        if (dhrL == null) {
                            dhrL = idbo.getExecutionResult();
                            dhrL.setData(object);
                        }
                        long startTransf = System.currentTimeMillis();
                        Object xmlFile = transform(idbo, dhrL.getData(), localParams);
                        long lTimeTransf = System.currentTimeMillis() - startTransf;
                        timeTransf += lTimeTransf;

                        dhrL = idbo.getExecutionResult();
                        dhrL.setData(xmlFile);
                        this.dataCache.put(idbo.getOutputDataName(), dhrL);
                        if (xmlFile != null) {
                            if (logger.isDebugEnabled() && (this.makeDump != DUMP_NONE)) {
                                if (xmlFile instanceof byte[]) {
                                    if (this.makeDump == DUMP_HEX) {
                                        logger.debug("Input data: [\n" + new Dump((byte[]) xmlFile, -1) + "\n].");
                                    }
                                    else {
                                        logger.debug("Input data: [\n" + new String((byte[]) xmlFile) + "\n].");
                                    }
                                }
                                else if (xmlFile instanceof Node) {
                                    try {
                                        logger.debug("Transformation output: [\n"
                                                + XMLUtils.serializeDOM_S((Node) xmlFile) + "\n].");
                                    }
                                    catch (Exception exc) {
                                        logger.debug("Transformation output: [\nDUMP ERROR!!!!!\n].");
                                    }
                                }
                                else {
                                    logger.debug("Transformation output: [\n" + xmlFile + "\n].");
                                }
                            }
                        }
                        idbo.executeIn(xmlFile, conn, localParams);
                        logger.info("End executing IDBO [" + idbo.toString() + "] in forced XML2DB mode. ExecutionTime: " + getPartialTime(start) + " - ConnectionTime: " + formatPartialTime(idbo.getTimeConn()) + " - TransformationTime: " + formatPartialTime(lTimeTransf));
                    }
                    else if (idbo.getForcedMode().equals(IDBO.MODE_DB2XML)) {
                        logger.debug("Start executing IDBO [" + idbo.toString() + "] in forced DB2XML mode.");
                        Object output = idbo.executeOut(conn, localParams);
                        if (logger.isDebugEnabled() && (output != null) && (this.makeDump != DUMP_NONE)) {
                            if (output instanceof byte[]) {
                                if (this.makeDump == DUMP_HEX) {
                                    logger.debug("Received data from DB: [\n" + new Dump((byte[]) output, -1)
                                            + "\n].");
                                }
                                else {
                                    logger.debug("Received data from DB: [\n" + new String((byte[]) output)
                                            + "\n].");
                                }
                            }
                            else if (output instanceof Node) {
                                try {
                                    logger.debug("Received data from DB: [\n" + XMLUtils.serializeDOM_S((Node) output)
                                            + "\n].");
                                }
                                catch (Exception exc) {
                                    logger.debug("Received data from DB: [\nDUMP ERROR!!!!!\n].");
                                }
                            }
                            else {
                                logger.debug("Received data from DB: [\n" + output + "\n].");
                            }
                        }
                        Object xmlOut = null;
                        try {
                            xmlOut = XMLUtils.parseObject_S(output, false, true);
                        }
                        catch (Throwable e) {
                            xmlOut = output;
                        }
                        long startTransf = System.currentTimeMillis();
                        Object xmlFile = transform(idbo, xmlOut, localParams);
                        long lTimeTransf = System.currentTimeMillis() - startTransf;
                        timeTransf += lTimeTransf;

                        DHResult dhrL = idbo.getExecutionResult();
                        dhrL.setData(xmlFile);
                        this.dataCache.put(idbo.getOutputDataName(), dhrL);
                        logger.info("End executing IDBO [" + idbo.toString() + "] in forced DB2XML mode. ExecutionTime: " + getPartialTime(start) + " - ConnectionTime: " + formatPartialTime(idbo.getTimeConn()) + " - TransformationTime: " + formatPartialTime(lTimeTransf));
                    }
                    else {
                        logger.debug("Start executing IDBO [" + idbo.toString() + "] in normal mode.");
                        DHResult dhrL = (DHResult) this.dataCache.get(idbo.getInputDataName());
                        if (dhrL == null) {
                            dhrL = idbo.getExecutionResult();
                            dhrL.setData(object);
                        }
                        long startTransf = System.currentTimeMillis();
                        Object xmlFile = transform(idbo, dhrL.getData(), localParams);
                        long lTimeTransf = System.currentTimeMillis() - startTransf;
                        timeTransf += lTimeTransf;

                        dhrL = idbo.getExecutionResult();
                        dhrL.setData(xmlFile);
                        this.dataCache.put(idbo.getOutputDataName(), dhrL);
                        if (xmlFile != null) {
                            if (logger.isDebugEnabled() && (this.makeDump != DUMP_NONE)) {
                                if (xmlFile instanceof byte[]) {
                                    if (this.makeDump == DUMP_HEX) {
                                        logger.debug("Transformation output: [\n" + new Dump((byte[]) xmlFile, -1) + "\n].");
                                    }
                                    else {
                                        logger.debug("Transformation output: [\n" + new String((byte[]) xmlFile) + "\n].");
                                    }
                                }
                                else if (xmlFile instanceof Node) {
                                    try {
                                        logger.debug("Transformation output: [\n"
                                                + XMLUtils.serializeDOM_S((Node) xmlFile) + "\n].");
                                    }
                                    catch (Exception exc) {
                                        logger.debug("Transformation output: [\nDUMP ERROR!!!!!\n].", exc);
                                    }
                                }
                                else {
                                    logger.debug("Transformation output: [\n" + xmlFile + "\n].");
                                }
                            }
                        }
                        xmlFile = idbo.executeInOut(xmlFile, conn, localParams);
                        dhrL = idbo.getExecutionResult();
                        dhrL.setData(xmlFile);
                        this.dataCache.put(idbo.getOutputDataName(), dhrL);
                        logger.info("End executing IDBO [" + idbo.toString() + "] in normal mode. ExecutionTime: " + getPartialTime(start) + " - ConnectionTime: " + formatPartialTime(idbo.getTimeConn()) + " - TransformationTime: " + formatPartialTime(lTimeTransf));
                    }
                }
                finally {
                    NMDC.pop();
                }
            }
            if (this.transacted && !this.isXA) {
                logger.debug("Committing EXECUTE [" + operation + "].");
                conn.commit();
            }
            DHResult dhr = new DHResult((DHResult) this.dataCache.get(this.outputDataName));
            if (!this.dboOutputMap.get(this.outputDataName).isReturnData()) {
                dhr.setData(null);
            }
            Object xmlFile = dhr.getData();

            // Manage statistics (if the @output-stats attribute is set)
            if (this.statsDataName != null) {
                if (this.statsDataName.equals(ALL_STATS)) {

                    // statistics from all DBOs...
                    this.internalIdx = 0;

                    DHResult _dhr = null;
                    long readAll = 0;
                    long insertAll = 0;
                    long updateAll = 0;
                    long discardAll = 0;
                    List<DiscardCause> discardCause = new ArrayList<DiscardCause>();
                    long totalAll = 0;
                    while (hasNext()) {
                        idbo = nextDBO();
                        _dhr = (DHResult) this.dataCache.get(idbo.getOutputDataName());

                        readAll += _dhr.getRead();
                        insertAll += _dhr.getInsert();
                        updateAll += _dhr.getUpdate();
                        discardAll += _dhr.getDiscard();
                        discardCause.addAll(_dhr.getDiscardCauseList());
                        totalAll += _dhr.getTotal();
                    }
                    _dhr = null;
                    dhr.setRead(readAll);
                    dhr.setInsert(insertAll);
                    dhr.setUpdate(updateAll);
                    dhr.setDiscard(discardAll);
                    dhr.setDiscardCauseList(discardCause);
                    dhr.setTotal(totalAll);
                }
                else if (!this.statsDataName.equals(this.outputDataName)) {
                    // You need statistics from a different IDBO
                    DHResult _dhr = (DHResult) this.dataCache.get(this.statsDataName);

                    dhr.setRead(_dhr.getRead());
                    dhr.setInsert(_dhr.getInsert());
                    dhr.setUpdate(_dhr.getUpdate());
                    dhr.setTotal(_dhr.getTotal());

                    _dhr = null;
                }
            }

            if (this.mergeList.size() >1) {
                XMLUtils parser = null;
                try {
                    parser = XMLUtils.getParserInstance();
                    MergeInfo mergeDest = this.mergeList.get(0);
                    Document dest = (Document) parser.parseObject(((DHResult)this.dataCache.get(mergeDest.source)).getData(), false, true);
                    for (int i = 1; i < this.mergeList.size(); i++) {
                        MergeInfo mergeInfo = this.mergeList.get(i);
                        Object srcO = ((DHResult) this.dataCache.get(mergeInfo.source)).getData();
                        if (srcO != null) {
                            Document src = (Document) parser.parseObject(srcO, false, true);
                            NodeList sources = parser.selectNodeList(src, mergeInfo.xpathSrc);
                            if (sources.getLength() > 0) {
                                Node destNode = parser.selectSingleNode(dest, mergeInfo.xpathDest);
                                for (int j = 0; j < sources.getLength(); ++j) {
                                    Node node = sources.item(j);
                                    node = dest.importNode(node, true);
                                    destNode.appendChild(node);
                                }
                            }
                        }
                    }

                    xmlFile = dest;
                }
                finally {
                    XMLUtils.releaseParserInstance(parser);
                }
                dhr.setData(xmlFile);
            }

            if (logger.isDebugEnabled() && (xmlFile != null) && (this.makeDump != DUMP_NONE)) {
                if (xmlFile instanceof byte[]) {
                    if (this.makeDump == DUMP_HEX) {
                        logger.debug("Returning data: [\n" + new Dump((byte[]) xmlFile, -1) + "\n].");
                    }
                    else {
                        logger.debug("Returning data: [\n" + new String((byte[]) xmlFile) + "\n].");
                    }
                }
                else if (xmlFile instanceof Node) {
                    try {
                        logger.debug("Returning data: [\n" + XMLUtils.serializeDOM_S((Node) xmlFile) + "\n].");
                    }
                    catch (Exception exc) {
                        logger.debug("Returning data: [\nDUMP ERROR!!!!!\n].", exc);
                    }
                }
                else {
                    logger.debug("Returning data: [\n" + xmlFile + "\n].");
                }
            }
            return dhr;
        }
        catch (SQLException exc) {
            if (conn != null) {
                if (this.transacted && !this.isXA) {
                    try {
                        logger.warn("Rolling-back EXECUTE [" + operation + "].");
                        conn.rollback();
                    }
                    catch (Exception ex) {
                        // Nothing to do
                    }
                }
            }
            OracleExceptionHandler.handleSQLException(exc);
            throw new DataHandlerException("SQL Exception: " + exc.getMessage(), exc);
        }
        catch (Exception exc) {
            if (conn != null) {
                if (this.transacted && !this.isXA) {
                    try {
                        logger.warn("Rolling-back EXECUTE [" + operation + "].");
                        conn.rollback();
                    }
                    catch (Exception ex) {
                        // Nothing to do
                    }
                }
            }
            logger.error("Unhandled Exception", exc);
            ThreadUtils.checkInterrupted(exc);
            throw new DataHandlerException("Unhandled Exception: " + exc.getMessage(), exc);
        }
        finally {
            cleanup();
            try {
                JDBCConnectionBuilder.releaseConnection(intConnName, conn);
            }
            catch (Exception exc) {
                // do nothing
            }
            logger.info("End executing EXECUTE [" + operation + "]. ExecutionTime: " + getPartialTime(start) + " - ConnectionTime: " + formatPartialTime(timeConn) + " - TransformationTime: " + formatPartialTime(timeTransf));
            NMDC.pop();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#cleanup()
     */
    @Override
    public void cleanup()
    {
        this.dataCache.clear();
        this.internalIdx = 0;
        while (hasNext()) {
            IDBO idbo = nextDBO();
            try {
                idbo.cleanup();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        // Static utility classes reset
        AbstractRetriever.cleanupAll();
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#destroy()
     */
    @Override
    public void destroy()
    {
        this.baseProps.clear();

        this.internalIdx = 0;
        while (hasNext()) {
            IDBO idbo = nextDBO();
            try {
                idbo.destroy();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        // Static utility classes reset
        AbstractRetriever.cleanupAll();
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#getConfigurationNode()
     */
    @Override
    public Node getConfigurationNode()
    {
        return this.configurationNode;
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#setConfigurationNode(org.w3c.dom.Node)
     */
    @Override
    public void setConfigurationNode(Node configurationNode)
    {
        this.configurationNode = configurationNode;
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#setDteController(it.greenvulcano.gvesb.gvdte.controller.DTEController)
     */
    @Override
    public void setDteController(DTEController dteController)
    {
        this.dteController = dteController;
    }

    private Object transform(IDBO idbo, Object input, Map<String, Object> params) throws Exception
    {
        Object output = input;
        String transformation = idbo.getTransformation();
        if ((transformation != null) && !transformation.equals("")) {
            logger.debug("Transformation [" + transformation + "] execution using DTE.");
            output = this.dteController.transform(transformation, input, params);
        }
        else {
            logger.debug("No transformation for this IDBO.");
        }
        return output;
    }

    private IDBO firstDBO()
    {
        return this.dboList.get(0);
    }

    private IDBO nextDBO()
    {
        return this.dboList.get(this.internalIdx++);
    }

    private boolean hasNext()
    {
        return (this.internalIdx < this.dboList.size());
    }

    private Map<String, Object> buildProps(Map<String, Object> props) throws DataHandlerException
    {
        try {
            Map<String, Object> allProps = new HashMap<String, Object>(this.baseProps);
            if (props != null) {
                allProps.putAll(props);
            }
            if (this.resolveMetadata) {
                boolean toDecode = true;
                while (toDecode) {
                    toDecode = false;
                    for (Entry<String, Object> entry : allProps.entrySet()) {
                        String name = entry.getKey();
                        String value = (String) entry.getValue();
                        if (!PropertiesHandler.isExpanded(value)) {
                            String nValue = PropertiesHandler.expand(value, allProps);
                            if (!PropertiesHandler.isExpanded(nValue) && ((nValue != null) && (!nValue.equals(value)))) {
                                toDecode = true;
                            }
                            allProps.put(name, nValue);
                        }
                    }
                }
            }
            return allProps;
        }
        catch (Exception exc) {
            throw new DataHandlerException("Error building properties map", exc);
        }
    }

    private static String getPartialTime(long start)
    {
        long end = System.currentTimeMillis();
        return formatPartialTime(end - start);
    }

    private static String formatPartialTime(long partial)
    {
        return String.valueOf(partial);
        /*int ms = (int) partial % 1000;
        String msec = Integer.toString(ms);
        if (ms < 10) {
            msec = "00" + ms;
        }
        else if (ms < 100) {
            msec = "0" + ms;
        }
        partial = (partial - ms) / 1000;

        int s = (int) partial % 60;
        String sec = Integer.toString(s);
        if (s < 10) {
            sec = "0" + s;
        }
        partial = (partial - s) / 60;

        int m = (int) partial % 3600;
        String min = Integer.toString(m);
        if (m < 10) {
            min = "0" + m;
        }
        return min + ":" + sec + "." + msec;*/
    }
}
