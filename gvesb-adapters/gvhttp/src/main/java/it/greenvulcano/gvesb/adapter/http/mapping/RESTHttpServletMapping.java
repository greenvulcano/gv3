/*
 * Copyright (c) 2009-2014 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.adapter.http.mapping;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.adapter.http.HttpServletMapping;
import it.greenvulcano.gvesb.adapter.http.HttpServletTransactionManager;
import it.greenvulcano.gvesb.adapter.http.exc.GVRequestException;
import it.greenvulcano.gvesb.adapter.http.exc.InboundHttpResponseException;
import it.greenvulcano.gvesb.adapter.http.formatters.FormatterManager;
import it.greenvulcano.gvesb.adapter.http.formatters.handlers.GVTransactionInfo;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpConstants;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpExecutionException;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpInitializationException;
import it.greenvulcano.gvesb.adapter.http.utils.DumpUtils;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.buffer.GVPublicException;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;
import it.greenvulcano.gvesb.internal.data.ChangeGVBuffer;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.json.JSONUtils;
import it.greenvulcano.util.xml.XMLUtils;

/**
 * RESTHttpServletMapping class
 *
 * @version 3.5.0 July 20, 2014
 * @author GreenVulcano Developer Team
 *
 *
 */
public class RESTHttpServletMapping implements HttpServletMapping
{
    private static Logger                 logger              = GVLogger.getLogger(RESTHttpServletMapping.class);

    private HttpServletTransactionManager transactionManager  = null;
    private String                        action              = null;
    private boolean                       dump                = false;
    private boolean                       logBeginEnd         = false;
    private String                        responseContentType = null;
    private String                        responseCharacterEncoding = null;
    private final List<PatternResolver>         operationMappings   = new ArrayList<PatternResolver>();

    /*
     * /APP=prova/ELEMENT=test{{/DATA=\d+{{/SUB_DATA=blabla}}}}
     * {{...}} optional terminal URL path component
     */
    static private class PatternResolver {
        private String pattern;
        private String method;
        private String service;
        private String system;
        private String operation;
        private String masterIdFilter;
        private boolean haveOptional;
        private boolean extractHdr;
        private ChangeGVBuffer cGVBuffer  = null;
        private final List<String> propNames = new ArrayList<String>();
        private final List<Pattern> patterns = new ArrayList<Pattern>();

        public PatternResolver() {
            // do nothing
        }

        public void init(Node node) throws AdapterHttpInitializationException {
            try {
                this.pattern = XMLConfig.get(node, "@pattern");
                if ((this.pattern == null) || "".equals(this.pattern)) {
                    throw new AdapterHttpInitializationException("RESTHttpServletMapping - Error initializing Pattern: empty");
                }
                this.method = XMLConfig.get(node, "@method");
                if ((this.method == null) || "".equals(this.method)) {
                    throw new AdapterHttpInitializationException("RESTHttpServletMapping - Error initializing Pattern[" + this.pattern + "]: empty @method");
                }
                this.service = XMLConfig.get(node, "@service");
                if ((this.service == null) || "".equals(this.service)) {
                    throw new AdapterHttpInitializationException("RESTHttpServletMapping - Error initializing Pattern[" + this.method + "#" + this.pattern + "]: empty @service");
                }
                this.system = XMLConfig.get(node, "@system", GVBuffer.DEFAULT_SYS);
                this.operation = XMLConfig.get(node, "@operation");
                if ((this.operation == null) || "".equals(this.operation)) {
                    throw new AdapterHttpInitializationException("RESTHttpServletMapping - Error initializing Pattern[" + this.method + "#" + this.pattern + "]: empty @operation");
                }
                this.extractHdr = XMLConfig.getBoolean(node, "@extract-headers", false);
                Node cGVBufferNode = XMLConfig.getNode(node, "ChangeGVBuffer");
                if (cGVBufferNode != null) {
                    this.cGVBuffer = new ChangeGVBuffer();
                    this.cGVBuffer.init(cGVBufferNode);
                }
                this.masterIdFilter = XMLConfig.get(node, "@master-id-filter", XMLConfig.get(node, "ancestor::RESTActionMapping/@master-id-filter", ""));
            }
            catch (XMLConfigException exc) {
                throw new AdapterHttpInitializationException("RESTHttpServletMapping - Error initializing Pattern: error reading configuration", exc);
            }
            compile();
        }

        private void compile() throws AdapterHttpInitializationException {
            logger.debug("Compile - BEGIN");
            logger.debug("Pattern: " + this.method + "#" + this.pattern);
            String locPattern = this.pattern;
            if (locPattern.startsWith("/")) {
                locPattern = locPattern.substring(1);
            }
            String[] list = locPattern.split("/");
            for (String element : list) {
                String elem = element.trim();
                if (!"".equals(elem)) {
                    logger.debug("Element: " + elem);
                    String pN = elem.split("=")[0].trim();
                    String p = elem.split("=")[1].trim();
                    if (pN.startsWith("?:")) {
                    	this.haveOptional = true;
                    }
                    else {
                    	if (this.haveOptional) {
                    		throw new AdapterHttpInitializationException("RESTHttpServletMapping - Error initializing Pattern[" + this.method + "#" + this.pattern + "]: NON Optional property [" + pN + "] come afther an optional property");
                    	}
                    }
                    this.propNames.add(pN);
                    this.patterns.add(Pattern.compile(p));
                    logger.debug("[" + pN + "]=[" + p + "]");
                }
            }
            logger.debug("Compile - END");
            if (this.patterns.isEmpty()) {
                throw new AdapterHttpInitializationException("RESTHttpServletMapping - Error initializing Pattern[" + this.method + "#" + this.pattern + "]: empty");
            }
        }

        public String match(HttpServletRequest request, String methodName, String path, GVBuffer data) throws AdapterHttpExecutionException {
            try {
            	try {
            		if (this.masterIdFilter.startsWith("hdr:")) {
            			String mid = request.getHeader(this.masterIdFilter.substring(this.masterIdFilter.indexOf("hdr:") + 4));
            			if (mid != null) {
            				data.setProperty("GV_MASTER_ID", mid);
            				GVBufferMDC.changeMasterId(mid);
            			}
            		}
            	}
            	catch (Exception exc) {
            		logger.error("Error decoding GV Master Id: " + this.masterIdFilter, exc);
            	}

                logger.debug("Checking [" + this.method + "#" + this.pattern +"] on [" + methodName + "#" + path + "]");
                if (!this.method.equalsIgnoreCase(methodName)) {
                    logger.debug("Pattern [" + this.method + "#" + this.pattern + "] NOT matched");
                    return null;
                }
                List<String> values = new ArrayList<String>();
                String locPath = path;
                if (locPath.startsWith("/")) {
                    locPath = locPath.substring(1);
                }
                String[] parts = locPath.split("/");
                if ((parts.length == this.patterns.size()) || (this.haveOptional && (parts.length <= this.patterns.size()))) {
                    for (int i = 0; i < parts.length; i++) {
                        Matcher m = this.patterns.get(i).matcher(parts[i]);
                        if (m.matches()) {
                            values.add(parts[i]);
                        }
                        else {
                            logger.debug("Pattern [" + this.method + "#" + this.pattern +"] NOT matched");
                            return null;
                        }
                    }
                    if (parts.length < this.patterns.size()) {
                    	String pN = this.propNames.get(parts.length);
                    	if (! pN.startsWith("?:")) {
                    		logger.debug("Pattern [" + this.method + "#" + this.pattern +"] NOT matched");
                            return null;
                    	}
                    }

                    data.setService(this.service);
                    data.setSystem(this.system);
                    for (int i = 0; i < this.propNames.size(); i++) {
                    	String pN = this.propNames.get(i);
                    	if (pN.startsWith("?:")) {
                    		pN = pN.substring(2);
                    	}
                    	if (i < values.size()) {
                    		data.setProperty(pN, values.get(i));
                    	}
                    	else {
                    		data.setProperty(pN, "NULL");
                    	}
                    }

                    try {
                		if (this.masterIdFilter.startsWith("path:")) {
                			String mid = data.getProperty(this.masterIdFilter.substring(this.masterIdFilter.indexOf("path:") + 5));
                			if (mid != null) {
                				data.setProperty("GV_MASTER_ID", mid);
                				GVBufferMDC.changeMasterId(mid);
                			}
                		}
                	}
                	catch (Exception exc) {
                		logger.error("Error decoding GV Master Id: " + this.masterIdFilter, exc);
                	}

                    try {
                		if (this.masterIdFilter.startsWith("qs:")) {
                			String mid = request.getParameter(this.masterIdFilter.substring(this.masterIdFilter.indexOf("qs:") + 3));
                			if (mid != null) {
                				data.setProperty("GV_MASTER_ID", mid);
                				GVBufferMDC.changeMasterId(mid);
                			}
                		}
                	}
                	catch (Exception exc) {
                		logger.error("Error decoding GV Master Id: " + this.masterIdFilter, exc);
                	}
                    logger.debug("Pattern [" + this.method + "#" + this.pattern +"] matched");
                    return this.operation;
                }
                logger.debug("Pattern [" + this.method + "#" + this.pattern +"] NOT matched");
                return null;
            }
            catch (Exception exc) {
                throw new AdapterHttpExecutionException("RESTHttpServletMapping - Error evaluating Pattern[" + this.method + "#" + this.pattern + "]", exc);
            }
        }

        public boolean isExtractHdr() {
            return this.extractHdr;
        }

		public ChangeGVBuffer getChangeGVBuffer() {
			return this.cGVBuffer;
		}

		public String getMasterIdFilter() {
			return this.masterIdFilter;
		}

        @Override
        public String toString() {
            return this.method + "#" + this.pattern + " -> " + this.service + "/" + this.system + "/" + this.operation;
        }
    }

    /**
     * @param transactionManager
     * @param formatterMgr
     * @param configurationNode
     * @param configurationFile
     * @throws AdapterHttpInitializationException
     */
    @Override
	public void init(HttpServletTransactionManager transactionManager, FormatterManager formatterMgr,
            Node configurationNode) throws AdapterHttpInitializationException
    {
        this.transactionManager = transactionManager;

        try {
            this.action = XMLConfig.get(configurationNode, "@Action");
            this.dump = XMLConfig.getBoolean(configurationNode, "@dump-in-out", false);
            this.logBeginEnd = XMLConfig.getBoolean(configurationNode, "@log-begin-end", false);

            this.responseContentType = XMLConfig.get(configurationNode, "@RespContentType",
                    AdapterHttpConstants.APPXML_MIMETYPE_NAME);
            this.responseCharacterEncoding = XMLConfig.get(configurationNode, "@RespCharacterEncoding", "UTF-8");

            NodeList opMaps = XMLConfig.getNodeList(configurationNode, "OperationMappings/Mapping");
            for (int i = 0; i < opMaps.getLength(); i++) {
                Node opM = opMaps.item(i);
                this.operationMappings.add(buildPatternResolver(opM));
            }
        }
        /*catch (AdapterHttpInitializationException exc) {
            throw exc;
        }*/
        catch (Exception exc) {
            logger.error("RESTHttpServletMapping - Error initializing action '" + this.action + "'", exc);
            throw new AdapterHttpInitializationException("RESTHttpServletMapping - Error initializing action '" + this.action
                    + "'", exc);
        }
    }

    /**
     * @param req
     * @param resp
     * @return if request handling was successful
     * @throws InboundHttpResponseException
     */
    @Override
	public boolean handleRequest(String methodName, HttpServletRequest req, HttpServletResponse resp) throws InboundHttpResponseException
    {
        logger.debug("handleRequest start");
        long startTime = System.currentTimeMillis();
        boolean mustRollback = true;
        boolean forceTxRollBack = false;
        GVTransactionInfo transInfo = new GVTransactionInfo();
        boolean status = false;
        Throwable exception = null;
    	GVBuffer response = null;
    	Level level = Level.INFO;

        try {
            if (this.dump) {
                StringBuffer sb = new StringBuffer();
                DumpUtils.dump(req, sb);
                logger.info(sb);
            }

            String path = req.getPathInfo();
            if (path == null) {
                path = "/";
            }

            String query = req.getQueryString();
            if (query == null) {
                query = "";
            }

            GVBuffer request = new GVBuffer();
            String operationType = null;
            PatternResolver pr = null;
            ChangeGVBuffer cGVBuffer = null;
            Iterator<PatternResolver> i = this.operationMappings.iterator();
            while (i.hasNext()) {
                pr = i.next();
                operationType = pr.match(req, methodName, path, request);
                if (operationType != null) {
                    break;
                }
            }

            if (operationType == null) {
                logger.error(this.action + " - handleRequest - Error while handling request parameters: unable to decode requested operation [" + methodName + "#" + path + "]");
                resp.sendError(500, "Unable to decode the requested operation [" + methodName + "#" + path + "]");
                NMDC.put("HTTP_STATUS", 500);
                return false;
            }

            transInfo.setService(request.getService());
            transInfo.setSystem(request.getSystem());
            transInfo.setId(request.getId());
            transInfo.setOperation(operationType);

            request.setProperty("HTTP_ACTION", this.action);
            request.setProperty("HTTP_PATH", path);
            request.setProperty("HTTP_QUERY", query);
            request.setProperty("HTTP_METHOD", methodName);
            // get remote transport address...
            String remAddr = req.getRemoteAddr();
            request.setProperty("HTTP_REMOTE_ADDR", (remAddr != null ? remAddr : ""));

            parseRequest(req, methodName, pr, request);

            GVBufferMDC.put(request);
            NMDC.setOperation(operationType);
            if (this.logBeginEnd) {
                logger.info(GVFormatLog.formatBEGINOperation(request));
            }

            this.transactionManager.begin(transInfo);

            response = executeService(operationType, request);

            if ("Y".equalsIgnoreCase(response.getProperty("HTTP_RESP_FORCE_TX_ROLLBACK"))) {
                logger.warn("Output contains HTTP_RESP_FORCE_TX_ROLLBACK=Y : prepare to roll back transaction");
                forceTxRollBack = true;
            }

            if (!forceTxRollBack) {
            	this.transactionManager.commit(transInfo, true);
            }
            else {
            	this.transactionManager.rollback(transInfo, false);
            }

            manageHttpResponse(response, resp);

            if (!forceTxRollBack) {
            	this.transactionManager.commit(transInfo, false);
            }
            mustRollback = false;
            status = true;
            logger.debug("handleRequest stop");
        }
        catch (Throwable exc) {
            NMDC.put("HTTP_STATUS", 500);
        	exception = exc;
            level = Level.ERROR;
            logger.error("handleRequest - Service request failed", exc);
            try {
                resp.sendError(500, "" + exc);
            }
            catch (IOException exc1) {
                throw new InboundHttpResponseException("GVHTTP_INBOUND_HTTP_RESPONSE_ERROR", new String[][]{{"errorName",
                    "" + exc1}}, exc1);
            }
        }
        finally {
            if (mustRollback) {
                try {
                    this.transactionManager.rollback(transInfo, false);
                }
                catch (Exception exc) {
                    logger.error("handleRequest - Transaction failed: " + exc);
                }
            }

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            if (this.logBeginEnd) {
                GVFormatLog gvFormatLog = null;
                if (exception != null) {
                    gvFormatLog = GVFormatLog.formatENDOperation(exception, totalTime);
                }
                else {
                    if (response != null) {
                        gvFormatLog = GVFormatLog.formatENDOperation(response, totalTime);
                    }
                    else {
                        gvFormatLog = GVFormatLog.formatENDOperation(totalTime);
                    }
                }
                logger.log(level, gvFormatLog);
            }
            else {
                if (level == Level.ERROR) {
                    logger.error("Error performing service call: " + exception);
                }
            }
            NMDC.remove("MASTER_SERVICE");
            NMDC.remove("GV_MASTER_ID");
        }
        return status;
    }

    /**
     * @param req
     * @param request
     * @throws GVException
     */
    private void parseRequest(HttpServletRequest req, String methodName, PatternResolver pr, GVBuffer request) throws GVException {
        try {
            Map<String, String[]> params = req.getParameterMap();
            Iterator<String> i = params.keySet().iterator();
            while (i.hasNext()) {
                String n = i.next();
                String v = params.get(n)[0];

                request.setProperty(n, ((v != null) && !"".equals(v)) ? v : "NULL");
            }

            String ct = req.getContentType();
            request.setProperty("HTTP_REQ_CONTENT_TYPE", (ct != null) ? ct : "NULL");
            String acc = req.getHeader("Accept");
            request.setProperty("HTTP_REQ_ACCEPT", (acc != null) ? acc : "NULL");
            if (methodName.equals("POST") || methodName.equals("PUT")) {
                if (!ct.startsWith(AdapterHttpConstants.URLENCODED_MIMETYPE_NAME)) {
                    Object requestContent = IOUtils.toByteArray(req.getInputStream());
                    if (ct.startsWith(AdapterHttpConstants.APPXML_MIMETYPE_NAME) ||
                        ct.startsWith(AdapterHttpConstants.APPJSON_MIMETYPE_NAME) ||
                        ct.startsWith("text/")) {
                        /* GESTIRE ENCODING!!! */
                        requestContent = new String((byte[]) requestContent);
                    }
                    request.setObject(requestContent);
                }
            }

            if (pr.isExtractHdr()) {
                XMLUtils parser = null;
                try {
                    parser = XMLUtils.getParserInstance();
                    Document doc = parser.newDocument("Hdr");
                    Element root = doc.getDocumentElement();

                    Enumeration<?> hn = req.getHeaderNames();
                    while (hn.hasMoreElements()) {
                        Element h = parser.insertElement(root, "h");
                        String name = (String) hn.nextElement();
                        String val = req.getHeader(name);
                        parser.setAttribute(h, "n", name);
                        parser.setAttribute(h, "v", val);
                    }
                    request.setProperty("HTTP_REQ_HEADERS", parser.serializeDOM(doc, true, false));
                }
                finally {
                    XMLUtils.releaseParserInstance(parser);
                }
            }
        	ChangeGVBuffer cGVBuffer = pr.getChangeGVBuffer();
            if (cGVBuffer != null) {
                try {
                	request = cGVBuffer.execute(request, new HashMap<String, Object>());
                }
                catch (Exception exc) {
                    cGVBuffer.cleanUp();
                }
            }

            String masterIdFilter = pr.getMasterIdFilter();
            try {
        		if (masterIdFilter.startsWith("body:")) {
        			String expr = masterIdFilter.substring(masterIdFilter.indexOf("body:") + 5);
        			try {
	        			JSONObject js = JSONUtils.parseObject(request.getObject());
	        			String mid = JSONUtils.get(js, expr, null);
	        			if (mid != null) {
	        				request.setProperty("GV_MASTER_ID", mid);
	        				GVBufferMDC.changeMasterId(mid);
	        			}
        			}
        			catch (Exception exc) {
                		logger.error("Error decoding GV Master Id: " + masterIdFilter, exc);
	        			Node xml = XMLUtils.parseObject_S(request.getObject(), false, true);
	        			String mid = XMLUtils.get_S(xml, expr, null);
	        			if (mid != null) {
	        				request.setProperty("GV_MASTER_ID", mid);
	        				GVBufferMDC.changeMasterId(mid);
	        			}
                	}
        		}
        	}
        	catch (Exception exc) {
        		logger.error("Error decoding GV Master Id: " + masterIdFilter, exc);
        	}
        }
        catch (Exception exc) {
            throw new AdapterHttpExecutionException("RESTHttpServletMapping - Error parsing request data", exc);
        }
    }

    @Override
    public boolean isDumpInOut() {
        return this.dump;
    }

    @Override
    public boolean isLogBeginEnd() {
        return this.logBeginEnd;
    }

    /**
     *
     */
    @Override
	public void destroy()
    {
        this.transactionManager = null;
        this.operationMappings.clear();
    }

    /**
     * @return the servlet action
     */
    @Override
	public String getAction()
    {
        return this.action;
    }

    /**
     * @return the <code>GVBuffer</code> response
     * @throws AdapterHttpInitializationException
     */
    /*public GVBuffer getResponse()
    {
        return response;
    }*/

    private PatternResolver buildPatternResolver(Node n) throws AdapterHttpInitializationException {
        PatternResolver pr = new PatternResolver();
        pr.init(n);
        return pr;
    }

    /**
     * Invoke GVCore object method corresponding to the specified
     * operationType passing it the <code>GVBuffer</code> object
     * <code>gvdInput</code> as input. Returns an <code>GVBuffer</code> object
     * encapsulating response.
     *
     * @param operationType
     *        the type of communication paradigm to be used.
     * @param gvdInput
     *        the input <code>GVBuffer</code> object.
     * @return an <code>GVBuffer</code> object encapsulating GVCore
     *         response
     * @throws GVRequestException
     *         if service request to GVConnector fails.
     */
    private GVBuffer executeService(String operationType, GVBuffer gvInput) throws GVRequestException,
            GVPublicException
    {
        if (this.logBeginEnd) {
            logger.info("BEGIN - Perform Remote Call(GVCore) - Operation(" + operationType + ")");
        }
        GVBuffer gvOutput = null;
        String status = "OK";
        long startTime = System.currentTimeMillis();
        long endTime = 0;
        long totalTime = 0;
        NMDC.push();
        try {
            Level level = Level.INFO;
            GreenVulcanoPool greenVulcanoPool = GreenVulcanoPoolManager.instance().getGreenVulcanoPool(AdapterHttpConstants.SUBSYSTEM);
            if (greenVulcanoPool == null) {
                throw new InboundHttpResponseException("GVHTTP_GREENVULCANOPOOL_NOT_CONFIGURED");
            }

            try {
                gvOutput = greenVulcanoPool.forward(gvInput, operationType);
                return gvOutput;
            }
            catch (Exception exc) {
                status = "FAILED";
                level = Level.ERROR;
                throw exc;
            }
            finally {
                NMDC.pop();
                endTime = System.currentTimeMillis();
                totalTime = endTime - startTime;
                if (this.logBeginEnd) {
                    logger.log(level, "END - Perform Remote Call(GVCore) - Operation(" + operationType
                            + ") - ExecutionTime (" + totalTime + ") - Status: " + status);
                }
            }
        }
        catch (GVPublicException exc) {
            throw exc;
        }
        catch (Throwable exc) {
            logger.error("executeServiceGVC - Runtime error while invoking GVCore: ", exc);
            throw new GVRequestException("GVHTTP_RUNTIME_ERROR", new String[][]{{"phase", "invoking GVCore"},
                    {"errorName", "" + exc}}, exc);
        }
    }

    /**
     * Handle GreenVulcano response to service request from external systems
     * communicating via HTTP.
     *
     * @throws InboundHttpResponseException
     *         if any error occurs.
     */
    private void manageHttpResponse(GVBuffer response, HttpServletResponse resp) throws InboundHttpResponseException
    {
        logger.debug("manageHttpResponse start");
        String respCharacterEncoding = null;
        try {
            String respStatusCode = response.getProperty("HTTP_RESP_STATUS_CODE");
            String respStatusMsg = response.getProperty("HTTP_RESP_STATUS_MSG");
            if (respStatusCode != null) {
                NMDC.put("HTTP_STATUS", respStatusCode);
                if (respStatusMsg == null) {
                    resp.setStatus(Integer.parseInt(respStatusCode));
                }
                else {
                    //resp.sendError(Integer.parseInt(respStatusCode), respStatusMsg);
                    resp.setStatus(Integer.parseInt(respStatusCode), respStatusMsg);
                }
            }

            /*Map<String, String> headerMap = (Map<String, String>) environment.get(AdapterHttpConstants.ENV_KEY_HTTP_HEADER);
            if (headerMap != null) {
                for (Entry<String, String> entry : headerMap.entrySet()) {
                    String paramName = entry.getKey();
                    String value = entry.getValue();
                    resp.setHeader(paramName, value);
                }
            }*/

            Object data = response.getObject();
            if (data != null) {
                respCharacterEncoding = response.getProperty("HTTP_RESP_CHAR_ENCODING");
                if (respCharacterEncoding == null) {
                    respCharacterEncoding = this.responseCharacterEncoding;
                }
                String respContentType = response.getProperty("HTTP_RESP_CONTENT_TYPE");
                if (respContentType == null) {
                    respContentType = this.responseContentType;
                }
            	String fileName = response.getProperty("HTTP_RESP_FILE_NAME");
            	if ((fileName != null) && !"".equals(fileName)) {
            		int fileSize = -1;
            		if (data instanceof byte[]) {
            			fileSize = ((byte[]) data).length;
                    }
                    else {
                        throw new InboundHttpResponseException("Invalid GVBuffer content: " + data.getClass().getName());
                    }
            		setRespDownloadHeaders(resp, respContentType, fileName, fileSize);
            	}
            	else {
	                setRespContentTypeAndCharset(resp, respContentType, respCharacterEncoding);
            	}

                OutputStream out = resp.getOutputStream();
                if (respContentType.equals(AdapterHttpConstants.APPXML_MIMETYPE_NAME) ||
                    respContentType.equals(AdapterHttpConstants.APPJSON_MIMETYPE_NAME) ||
                    respContentType.startsWith("text/")) {
                    if (data instanceof byte[]) {
                        IOUtils.write((byte[]) data, out);
                    }
                    else if (data instanceof String) {
                        IOUtils.write((String) data, out, respCharacterEncoding);
                    }
                    else if (data instanceof Node) {
                        XMLUtils.serializeDOMToStream_S((Node) data, out, respCharacterEncoding, false, false);
                    }
                    else {
                        throw new InboundHttpResponseException("Invalid GVBuffer content: " + data.getClass().getName());
                    }
                }
                else {
                    if (data instanceof byte[]) {
                        IOUtils.write((byte[]) data, out);
                    }
                    else {
                        throw new InboundHttpResponseException("Invalid GVBuffer content: " + data.getClass().getName());
                    }
                }
                out.flush();
                out.close();
            }

            if (this.dump) {
                StringBuffer sb = new StringBuffer();
                DumpUtils.dump(resp, sb);
                logger.info(sb);
            }
        }
        catch (UnsupportedEncodingException exc) {
            logger.error("manageResponse - Can't encode response to invoking system using encoding "
                    + respCharacterEncoding + ": " + exc);
            throw new InboundHttpResponseException("GVHTTP_CHARACTER_ENCODING_ERROR", new String[][]{
                    {"encName", respCharacterEncoding}, {"errorName", "" + exc}}, exc);
        }
        catch (Exception exc) {
            logger.error("manageResponse - Can't send response to invoking system: " + exc);
            throw new InboundHttpResponseException("GVHTTP_INBOUND_HTTP_RESPONSE_ERROR", new String[][]{{"errorName",
                    "" + exc}}, exc);
        }
        finally {
            logger.debug("manageHttpResponse stop");
        }
    }

    /**
     * Sets content type and charset header fields of the servlet response.
     *
     * @param resp
     *        An HttpServletResponse object
     * @param contentType
     *        A string containing the declared response's content type
     * @param charset
     *        A string containing the declared response's charset
     */
    private void setRespContentTypeAndCharset(HttpServletResponse resp, String contentType, String charset)
    {
        resp.setContentType(contentType);
        resp.setCharacterEncoding(charset);
    }
    /**
     * Sets header fields for file download.
     *
     * @param resp
     *        An HttpServletResponse object
     * @param contentType
     *        A string containing the declared response's content type
     * @param fileName
     *        A string containing the downloaded file name
     * @param fileSize
     *        A string containing the downloaded file size
     */
    private void setRespDownloadHeaders(HttpServletResponse resp, String contentType, String fileName, int fileSize)
    {
        resp.setContentType(contentType);
        resp.setContentLength(fileSize);
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        resp.setHeader("Connection", "close");
        resp.setHeader("Expires", "-1");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Cache-Control", "no-cache");
    }
}
