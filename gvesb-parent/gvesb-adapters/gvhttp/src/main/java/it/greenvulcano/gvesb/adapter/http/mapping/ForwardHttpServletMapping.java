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

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.adapter.http.HttpServletMapping;
import it.greenvulcano.gvesb.adapter.http.HttpServletTransactionManager;
import it.greenvulcano.gvesb.adapter.http.exc.InboundHttpResponseException;
import it.greenvulcano.gvesb.adapter.http.formatters.FormatterManager;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpInitializationException;
import it.greenvulcano.gvesb.http.ProtocolFactory;
import it.greenvulcano.log.GVLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 *
 * @version 3.4.0 23/mar/2014
 * @author GreenVulcano Developer Team
 *
 */
public class ForwardHttpServletMapping implements HttpServletMapping
{
    private static final Logger logger                 = GVLogger.getLogger(ForwardHttpServletMapping.class);
    public static final int     DEFAULT_CONN_TIMEOUT   = 10000;
    public static final int     DEFAULT_SO_TIMEOUT     = 30000;

    private String              action;
    private boolean             dump                   = false;
    private HttpClient          httpClient;
    private String              host;
    private String              port;
    private Protocol            protocol               = null;
    private String              contextPath;

    private int                 connTimeout            = DEFAULT_CONN_TIMEOUT;
    private int                 soTimeout              = DEFAULT_SO_TIMEOUT;
    
    
    /**
     * 
     */
    public ForwardHttpServletMapping() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.adapter.http.HttpServletMapping#init(it.greenvulcano.gvesb.adapter.http.HttpServletTransactionManager, it.greenvulcano.gvesb.adapter.http.formatters.FormatterManager, org.w3c.dom.Node)
     */
    @Override
    public void init(HttpServletTransactionManager transactionManager, FormatterManager formatterMgr,
            Node configurationNode) throws AdapterHttpInitializationException {
        httpClient = new HttpClient();
        HostConfiguration hostConfiguration = httpClient.getHostConfiguration();

        try {
            action = XMLConfig.get(configurationNode, "@Action");
            dump = XMLConfig.getBoolean(configurationNode, "@dump-in-out", false);
            
            Node endpointNode = XMLConfig.getNode(configurationNode, "endpoint");
            host = XMLConfig.get(endpointNode, "@host");
            port = XMLConfig.get(endpointNode, "@port", "80");
            contextPath = XMLConfig.get(endpointNode, "@context-path", "");
            boolean secure = XMLConfig.getBoolean(endpointNode, "@secure", false);
            connTimeout = XMLConfig.getInteger(endpointNode, "@conn-timeout", DEFAULT_CONN_TIMEOUT);
            soTimeout = XMLConfig.getInteger(endpointNode, "@so-timeout", DEFAULT_SO_TIMEOUT);
            
            HttpConnectionManagerParams params = httpClient.getHttpConnectionManager().getParams();
            params.setConnectionTimeout(connTimeout);
            params.setSoTimeout(soTimeout);

            Node protocolNode = XMLConfig.getNode(endpointNode, "CustomProtocol");
            if (protocolNode != null) {
                protocol = ProtocolFactory.create(protocolNode);
            }
            else {
                protocol = Protocol.getProtocol(secure ? "https" : "http");
            }

            Node proxyConfigNode = XMLConfig.getNode(endpointNode, "Proxy");
            if (proxyConfigNode != null) {
                String proxyHost = XMLConfig.get(proxyConfigNode, "@host");
                int proxyPort = XMLConfig.getInteger(proxyConfigNode, "@port", 80);
                String proxyUser = XMLConfig.get(proxyConfigNode, "@user");
                String proxyPassword = XMLConfig.getDecrypted(proxyConfigNode, "@password", "");
                hostConfiguration.setProxy(proxyHost, proxyPort);
                if (proxyUser != null) {
                    httpClient.getState().setProxyCredentials(new AuthScope(proxyHost, proxyPort),
                            new UsernamePasswordCredentials(proxyUser, proxyPassword));
                }
            }
            
            httpClient.getHostConfiguration().setHost(host, Integer.parseInt(port), protocol);
        }
        catch (Exception exc) {
            throw new AdapterHttpInitializationException("GVHTTP_CONFIGURATION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }

    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.adapter.http.HttpServletMapping#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp) throws InboundHttpResponseException {
        HttpMethod method = null;
        logger.debug("BEGIN forward: " + req.getRequestURI());
        try {
            //httpClient.getHostConfiguration().setHost(host, Integer.parseInt(port), protocol);
        	
        	ByteArrayOutputStream bodyIn = new ByteArrayOutputStream();
        	ByteArrayOutputStream bodyOut = new ByteArrayOutputStream();

            String methodName = req.getMethod();
            if (methodName.equals("GET")) {
                method = new GetMethod();
            }
            else if (methodName.equals("POST")) {
                method = new PostMethod();
                IOUtils.copy(req.getInputStream(), bodyIn);
            }
            else if (methodName.equals("HEAD")) {
                method = new HeadMethod();
            }
            else if (methodName.equals("OPTIONS")) {
                method = new OptionsMethod();
            }
            else if (methodName.equals("PUT")) {
                method = new PutMethod();
                IOUtils.copy(req.getInputStream(), bodyIn);
            }
            else if (methodName.equals("DELETE")) {
                method = new DeleteMethod();
            }
            else {
                throw new InboundHttpResponseException("GV_CALL_SERVICE_ERROR", new String[][]{{"message", "Unknown method = " + methodName}});
            }
            
            if (dump) {
            	StringBuffer sb = new StringBuffer();
            	Dump(req, bodyIn, sb);
            	logger.info(sb);
            }

            String mapping = req.getPathInfo();
            if (mapping == null) {
                mapping = "/";
            }
            String destPath = contextPath + mapping;
            String queryString = req.getQueryString(); 
            if (queryString != null) {
                destPath += "?" + queryString; 
            }
            if (!destPath.startsWith("/")) {
                destPath = "/" + destPath; 
            }
            logger.info("Resulting QueryString: " + destPath);
            method.setURI(new URI(destPath, true));
            
            Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String hN = headerNames.nextElement();
                Enumeration<String> headers = req.getHeaders(hN);
                while (headers.hasMoreElements()) {
                    method.addRequestHeader(hN, headers.nextElement());
                }
            }
            
            if (methodName.equals("POST")) {
                ((PostMethod) method).setRequestBody(new ByteArrayInputStream(bodyIn.toByteArray()));
            }
            else if (methodName.equals("PUT")) {
                ((PutMethod) method).setRequestBody(new ByteArrayInputStream(bodyIn.toByteArray()));
            }
            
            int status = httpClient.executeMethod(method);

            IOUtils.copy(method.getResponseBodyAsStream(), bodyOut);
            
            if (dump) {
            	StringBuffer sb = new StringBuffer();
            	Dump(method, bodyOut, sb);
            	logger.info(sb);
            }
            
            resp.setStatus(status, method.getStatusText());
            
            Header[] responseHeaders = method.getResponseHeaders();
            for (Header header : responseHeaders) {
                String hN = header.getName();
                String hV = header.getValue();
                if (hV == null) {
                    hV = "";
                }
                resp.addHeader(hN, hV);
            }
            
            OutputStream out = resp.getOutputStream();
            out.write(bodyOut.toByteArray());
            //IOUtils.copy(method.getResponseBodyAsStream(), out);
            out.flush();
            out.close();
            
            return true;
        }
        catch (Exception exc) {
            logger.error("ERROR on forwarding: " + req.getRequestURI(), exc);
            throw new InboundHttpResponseException("GV_CALL_SERVICE_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
        finally {
            try {
                if (method != null) {
                    method.releaseConnection();
                }
            }
            catch (Exception exc) {
                logger.warn("Error while releasing connection", exc);
            }
            logger.debug("END forward: " + req.getRequestURI());
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.adapter.http.HttpServletMapping#destroy()
     */
    @Override
    public void destroy() {
        // do nothing
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.adapter.http.HttpServletMapping#getAction()
     */
    @Override
    public String getAction() {
        return action;
    }
    
    private void Dump(HttpServletRequest request, ByteArrayOutputStream body, StringBuffer log) {
        String hN;

        log.append("DUMP HttpServletRequest START").append("\n");
        log.append("Method: ").append(request.getMethod()).append("\n");
        log.append("RequestedSessionId: ").append(request.getRequestedSessionId()).append("\n");
        log.append("Protocol: ").append(request.getProtocol()).append("\n");
        log.append("ContextPath: ").append(request.getContextPath()).append("\n");
        log.append("PathInfo: ").append(request.getPathInfo()).append("\n");
        log.append("QueryString: ").append(request.getQueryString()).append("\n");
        log.append("RequestURI: ").append(request.getRequestURI()).append("\n");
        log.append("RequestURL: ").append(request.getRequestURL()).append("\n");
        log.append("ContentType: ").append(request.getContentType()).append("\n");
        log.append("ContentLength: ").append(request.getContentLength()).append("\n");
        log.append("CharacterEncoding: ").append(request.getCharacterEncoding()).append("\n");

        
        log.append("Headers START\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            hN = headerNames.nextElement();
            log.append("[" + hN + "]=");
            Enumeration<String> headers = request.getHeaders(hN);
            while (headers.hasMoreElements()) {
            	log.append("[" + headers.nextElement() + "]");
            }
            log.append("\n");
        }
        log.append("Headers END\n");
        
        log.append("Body START\n");
        log.append(body.toString()).append("\n");
        log.append("Body END\n");
       
        log.append("DUMP HttpServletRequest END \n");
    }
    
    private void Dump(HttpMethod response, ByteArrayOutputStream body, StringBuffer log) {
        String hN;
        String hV;

        log.append("DUMP HttpServletResponse START").append("\n");
        log.append("Status: ").append(response.getStatusLine()).append("\n");
        log.append("Headers START\n");
        Header[] responseHeaders = response.getResponseHeaders();
        for (Header header : responseHeaders) {
            hN = header.getName();
            log.append("[" + hN + "]=");
            hV = header.getValue();
            if (hV == null) {
                hV = "";
            }
            log.append("[" + hV + "]").append("\n");
        }
        log.append("Headers END\n");
        
        log.append("Body START\n");
        log.append(body.toString()).append("\n");
        log.append("Body END\n");
       
        log.append("DUMP HttpServletResponse END \n");
    }

}
