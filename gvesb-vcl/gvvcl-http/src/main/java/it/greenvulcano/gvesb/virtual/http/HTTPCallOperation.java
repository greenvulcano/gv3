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
package it.greenvulcano.gvesb.virtual.http;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.log4j.Logger;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.http.ProtocolFactory;
import it.greenvulcano.gvesb.http.auth.HttpAuth;
import it.greenvulcano.gvesb.http.auth.HttpAuthFactory;
import it.greenvulcano.gvesb.http.proxy.HttpProxy;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.XMLUtils;

/**
 * @version 3.0.0 Jul 26, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class HTTPCallOperation implements CallOperation
{
    enum HttpMethodName {
        OPTIONS, GET, HEAD, POST, PUT, DELETE
    }

    private static final Logger logger                 = GVLogger.getLogger(HTTPCallOperation.class);
    private static final String RESPONSE_PREFIX        = "GVHTTP_RESPONSE_";
    private static final String RESPONSE_STATUS        = RESPONSE_PREFIX + "STATUS";
    private static final String RESPONSE_MESSAGE       = RESPONSE_PREFIX + "MESSAGE";
    private static final String RESPONSE_HEADER_PREFIX = RESPONSE_PREFIX + "HEADER_";
    public static final int     DEFAULT_CONN_TIMEOUT   = 10000;
    public static final int     DEFAULT_SO_TIMEOUT     = 30000;

    private String              methodURI;
    private String              contextPath;
    private boolean             uriEscaped             = true;
    private HttpClient          httpClient;
    private String              userAgent;
    private String              host;
    private String              port;
    private Protocol            protocol               = null;
    private String              refDP;
    private HttpMethodName      methodName;
    private int                 connTimeout            = DEFAULT_CONN_TIMEOUT;
    private int                 soTimeout              = DEFAULT_SO_TIMEOUT;
    private HttpProxy           proxy                  = null;
    private HttpAuth            auth                   = null;

    private OperationKey        key                    = null;

    /**
     * Invoked from <code>OperationFactory</code> when an <code>Operation</code>
     * needs initialization.<br>
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node config) throws InitializationException
    {
        this.httpClient = new HttpClient();

        try {
            Node endpointNode = XMLConfig.getNode(config, "endpoint");
            this.host = XMLConfig.get(endpointNode, "@host");
            this.port = XMLConfig.get(endpointNode, "@port", "80");
            this.contextPath = XMLConfig.get(endpointNode, "@context-path", "");
            boolean secure = XMLConfig.getBoolean(endpointNode, "@secure", false);
            this.connTimeout = XMLConfig.getInteger(endpointNode, "@conn-timeout", DEFAULT_CONN_TIMEOUT);
            this.soTimeout = XMLConfig.getInteger(endpointNode, "@so-timeout", DEFAULT_SO_TIMEOUT);

            this.userAgent = XMLConfig.get(endpointNode, "@user-agent", "");
            this.httpClient.getParams().setParameter(HttpMethodParams.USER_AGENT, this.userAgent);

            HttpConnectionManagerParams params = this.httpClient.getHttpConnectionManager().getParams();
            params.setConnectionTimeout(this.connTimeout);
            params.setSoTimeout(this.soTimeout);

            Node protocolNode = XMLConfig.getNode(endpointNode, "CustomProtocol");
            if (protocolNode != null) {
                this.protocol = ProtocolFactory.create(protocolNode);
            }
            else {
                this.protocol = Protocol.getProtocol(secure ? "https" : "http");
            }

            this.proxy = new HttpProxy();
            this.proxy.init(XMLConfig.getNode(endpointNode, "Proxy"));

            this.auth = HttpAuthFactory.getInstance(XMLConfig.getNode(endpointNode, "*[@type='http-auth']"));
            logger.debug("HttpAuth: " + this.auth);

            Node methodNode = XMLConfig.getNode(config, "method");
            this.refDP = XMLConfig.get(methodNode, "@ref-dp", "");

            this.methodURI = XMLConfig.get(methodNode, "@request-uri", "/");
            this.methodName = HttpMethodName.valueOf(XMLConfig.get(methodNode, "@name"));
            this.uriEscaped = XMLConfig.getBoolean(methodNode, "@uri-escaped", true);
        }
        catch (Exception exc) {
            throw new InitializationException("GV_CONFIGURATION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        logger.debug("BEGIN perform(GVBuffer gvBuffer)");
        HttpMethod method = null;
        try {
            String currMethodURI = null;
            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);

            String currHost = PropertiesHandler.expand(this.host, params, gvBuffer);
            String currPort = PropertiesHandler.expand(this.port, params, gvBuffer);
            logger.debug("Server Host: " + currHost + " - Port: " + currPort);
            this.httpClient.getHostConfiguration().setHost(currHost, Integer.parseInt(currPort), this.protocol);

            this.auth.setAuthentication(this.httpClient, currHost, Integer.parseInt(currPort), gvBuffer, params);
            this.proxy.setProxy(this.httpClient, gvBuffer, params);

            currMethodURI = PropertiesHandler.expand(this.contextPath + this.methodURI, params, gvBuffer);
            logger.debug("MethodURI[escaped:" + this.uriEscaped + "]=[" + currMethodURI + "]");
            switch (this.methodName) {
                case OPTIONS :
                    method = new OptionsMethod();
                    break;
                case GET :
                    method = new GetMethod();
                    break;
                case HEAD :
                    method = new HeadMethod();
                    break;
                case POST :
                    method = new PostMethod();
                    break;
                case PUT :
                    method = new PutMethod();
                    break;
                case DELETE :
                    method = new DeleteMethod();
                    break;
                default :
                    throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                            {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()},
                            {"message", "Unknown method = " + this.methodName}});
            }
            method.setURI(new URI(currMethodURI, this.uriEscaped));

            if ((this.refDP != null) && (this.refDP.length() > 0)) {
                logger.debug("Calling configured Data Provider: " + this.refDP);
                DataProviderManager dataProviderManager = DataProviderManager.instance();
                IDataProvider dataProvider = dataProviderManager.getDataProvider(this.refDP);
                try {
                    dataProvider.setContext(method);
                    dataProvider.setObject(gvBuffer);
                    method = (HttpMethod) dataProvider.getResult();
                }
                finally {
                    dataProviderManager.releaseDataProvider(this.refDP, dataProvider);
                }
            }

            int status = this.httpClient.executeMethod(method);
            gvBuffer.setProperty(RESPONSE_STATUS, String.valueOf(status));
            String statusTxt = method.getStatusText();
            gvBuffer.setProperty(RESPONSE_MESSAGE, (statusTxt != null ? statusTxt : "NULL"));
            Header[] responseHeaders = method.getResponseHeaders();
            for (Header header : responseHeaders) {
                String headerName = RESPONSE_HEADER_PREFIX + header.getName();
                String value = header.getValue();
                if (value == null) {
                    value = "";
                }
                gvBuffer.setProperty(headerName, value);
            }
            String cType = "text/html";
            Header cTypeHeader = method.getResponseHeader("Content-Type");
            if (cTypeHeader != null) {
                String cTypeValue = cTypeHeader.getValue();
                if (cTypeValue != null) {
                    cType = cTypeValue;
                }
            }
            logger.debug("Response content-type: " + cType);
            ContentType contentType = new ContentType(cType);
            byte[] responseBody = method.getResponseBody();
            Object object = responseBody;
            if (contentType.getPrimaryType().equals("multipart")) {
                object = handleMultipart(responseBody, cType);
            }
            gvBuffer.setObject(object);
        }
        catch (CallException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("ERROR perform(GVBuffer gvBuffer)", exc);
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
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
            logger.debug("END perform(GVBuffer gvBuffer)");
        }
        return gvBuffer;
    }

    /**
     * @param responseBody
     * @throws MessagingException
     */
    private Document handleMultipart(byte[] responseBody, String contentType) throws Exception
    {
        ByteArrayDataSource bads = new ByteArrayDataSource(responseBody, contentType);
        MimeMultipart multipart = new MimeMultipart(bads);
        XMLUtils xml = XMLUtils.getParserInstance();
        Document doc = null;
        try {
            doc = xml.newDocument("MultipartHttpResponse");
        }
        finally {
            XMLUtils.releaseParserInstance(xml);
        }
        for (int i = 0; i < multipart.getCount(); i++) {
            dumpPart(multipart.getBodyPart(i), doc.getDocumentElement(), doc);
        }
        return doc;
    }

    private void dumpPart(Part p, Element msg, Document doc) throws Exception
    {
        Element content = null;
        if (p.isMimeType("text/plain")) {
            content = doc.createElement("PlainMessage");
            Text body = doc.createTextNode((String) p.getContent());
            content.appendChild(body);
        }
        else if (p.isMimeType("text/html")) {
            content = doc.createElement("HTMLMessage");
            CDATASection body = doc.createCDATASection((String) p.getContent());
            content.appendChild(body);
        }
        else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            content = doc.createElement("Multipart");
            for (int i = 0; i < count; i++) {
                dumpPart(mp.getBodyPart(i), content, doc);
            }
        }
        else if (p.isMimeType("message/rfc822")) {
            content = doc.createElement("NestedMessage");
            dumpPart((Part) p.getContent(), content, doc);
        }
        else {
            content = doc.createElement("EncodedContent");
            DataHandler dh = p.getDataHandler();
            OutputStream os = new ByteArrayOutputStream();
            Base64OutputStream b64os = new Base64OutputStream(os, true, -1, "".getBytes());
            dh.writeTo(b64os);
            b64os.flush();
            b64os.close();
            content.appendChild(doc.createTextNode(os.toString()));
        }
        msg.appendChild(content);
        String filename = p.getFileName();
        if (filename != null) {
            content.setAttribute("file-name", filename);
        }
        String ct = p.getContentType();
        if (ct != null) {
            content.setAttribute("content-type", ct);
        }
        String desc = p.getDescription();
        if (desc != null) {
            content.setAttribute("description", desc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getServiceAlias(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    @Override
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    @Override
    public OperationKey getKey()
    {
        return this.key;
    }
}
