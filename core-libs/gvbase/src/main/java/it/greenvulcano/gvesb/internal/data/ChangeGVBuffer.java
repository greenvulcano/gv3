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
package it.greenvulcano.gvesb.internal.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.expression.ExpressionEvaluator;
import it.greenvulcano.expression.ExpressionEvaluatorException;
import it.greenvulcano.expression.ExpressionEvaluatorHelper;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.internal.GVInternalException;
import it.greenvulcano.js.initializer.JSInit;
import it.greenvulcano.js.initializer.JSInitManager;
import it.greenvulcano.js.util.JavaScriptHelper;
import it.greenvulcano.js.util.ScriptCache;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.crypto.CryptoHelper;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xpath.XPathFinder;
import it.greenvulcano.util.zip.ZipHelper;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ChangeGVBuffer
{
    private static Logger           logger                    = GVLogger.getLogger(ChangeGVBuffer.class);
    /**
     * define the crypto operation "none".
     */
    public static final String      CRYPTO_OP_NONE            = "none";
    /**
     * define the crypto operation "encrypt".
     */
    public static final String      CRYPTO_OP_ENCRYPT         = "encrypt";
    /**
     * define the crypto operation "decrypt".
     */
    public static final String      CRYPTO_OP_DECRYPT         = "decrypt";
    /**
     * define the compress algorithm "zip".
     */
    public static final String      COMPRESS_TYPE_ZIP         = "zip";
    /**
     * define the compress algorithm "gzip".
     */
    public static final String      COMPRESS_TYPE_GZIP        = "gzip";
    /**
     * define the compress operation "none".
     */
    public static final String      COMPRESS_OP_NONE          = "none";
    /**
     * define the compress operation "compress".
     */
    public static final String      COMPRESS_OP_COMPRESS      = "compress";
    /**
     * define the compress operation "uncompress".
     */
    public static final String      COMPRESS_OP_UNCOMPRESS    = "uncompress";
    /**
     * define the base64 operation "none".
     */
    public static final String      BASE64_OP_NONE            = "none";
    /**
     * define the base64 operation "encode".
     */
    public static final String      BASE64_OP_ENCODE          = "encode";
    /**
     * define the base64 operation "decode".
     */
    public static final String      BASE64_OP_DECODE          = "decode";
    /**
     * define the default script name.
     */
    private static final String     INTERNAL_SCRIPT           = "internal";

    /**
     * if true the GVBuffer body is canceled.
     */
    private boolean                 clearData                 = false;
    /**
     * the value to set as GVBuffer System field.
     */
    private String                  system                    = "";
    /**
     * the value to set as GVBuffer Service field.
     */
    private String                  service                   = "";
    /**
     * the value to set as GVBuffer Retcode field.
     */
    private int                     retCode                   = 0;
    /**
     * the names/values to set as GVBuffer properties.
     */
    private final HashMap<String, String> properties                = new HashMap<String, String>();
    /**
     * defines the order of properties setting in GVBuffer.
     */
    private final List<String>            propertiesList            = new ArrayList<String>();
    /**
     * if true is configured a crypto operation.
     */
    private boolean                 cryptoOn                  = false;
    /**
     * the value of the crypto operation.
     */
    private boolean                 cryptoOpEncrypt           = false;
    /**
     * the value to use as crypto key.
     */
    private String                  keyId                     = "";
    /**
     * if true is configured a compress operation.
     */
    private boolean                 compressOn                = false;
    /**
     * the value of the compress operation.
     */
    private boolean                 compressOpZip             = false;
    /**
     * the value to use as compression algorithm.
     */
    private String                  compressionType          = "zip";
    /**
     * the value to use as compression level level.
     */
    private String                  compressionLevel          = "";
    /**
     * ZipHelper instance.
     */
    private ZipHelper               zipHelper                 = null;
    /**
     * if true is configured a base64 operation.
     */
    private boolean                 base64On                  = false;
    /**
     * the value of the base64 operation.
     */
    private boolean                 base64OpEncode            = false;
    /**
     * if true is configured a JavaScript operation.
     */
    private boolean                 javascriptOn              = false;
    /**
     * The execution scope for the script.
     */
    private String                  scopeName                 = "";
    /**
     * The script file name.
     */
    private String                  scriptName                = "";
    /**
     * The script.
     */
    private String                  script                    = "";
    private Script					compiledScript            = null;
    /**
     * The execution context.
     */
    private Context                 cx                        = null;
    /**
     * The GVBuffer body builder.
     */
    private GVBufferBodyMaker       bodyBuilder               = null;

    /**
     * if true is configured a BodyBuild operation.
     */
    private boolean                 bodyBuilderOn             = false;

    /**
     * the name of the property to be used for body overwriting, if the
     * name is empty the feature is disabled, the feature is applied before
     * applying crypto, zip or base64 features, WARNING!!! if the clearData
     * field is true the body is emptied
     */
    private String                  overwriteBodyWithProperty = "";
    private String                  ognlScript;

    /**
     * guard for an undefined field value.
     */
    private static final String     UNDEFINED                 = "UNDEFINED";

    /**
     * Initialize the instance.
     *
     * @param node
     *        the node from which to read the configuration
     * @throws XMLConfigException
     *         if errors occurs
     */
    public final void init(Node node) throws XMLConfigException
    {
        try {
            this.clearData = XMLConfig.getBoolean(node, "@clear-data", false);
            this.system = XMLConfig.get(node, "@system", "");
            this.service = XMLConfig.get(node, "@service", "");
            this.ognlScript = XMLConfig.get(node, "OGNLScript");

            int operations = 0;

            String cryptoOp = XMLConfig.get(node, "@crypto-op", CRYPTO_OP_NONE);
            this.keyId = XMLConfig.get(node, "@key-id", CryptoHelper.DEFAULT_KEY_ID);

            if (cryptoOp.equals(CRYPTO_OP_NONE)) {
                this.cryptoOn = false;
            }
            else {
                this.cryptoOn = true;
                operations++;
                this.cryptoOpEncrypt = cryptoOp.equals(CRYPTO_OP_ENCRYPT);
            }

            String compressOp = XMLConfig.get(node, "@compress-op", COMPRESS_OP_NONE);
            this.compressionType = XMLConfig.get(node, "@compress-type", COMPRESS_TYPE_ZIP);
            this.compressionLevel = XMLConfig.get(node, "@compress-level", ZipHelper.DEFAULT_COMPRESSION_S);

            if (compressOp.equals(COMPRESS_OP_NONE)) {
                this.compressOn = false;
            }
            else {
                this.compressOn = true;
                operations++;
                this.compressOpZip = compressOp.equals(COMPRESS_OP_COMPRESS);
                if (this.compressionType.equals(COMPRESS_TYPE_ZIP)) {
                	this.zipHelper = new ZipHelper();
                	this.zipHelper.setCompressionLevel(this.compressionLevel);
                }
            }

            String base64Op = XMLConfig.get(node, "@base64-op", BASE64_OP_NONE);

            if (base64Op.equals(BASE64_OP_NONE)) {
                this.base64On = false;
            }
            else {
                this.base64On = true;
                operations++;
                this.base64OpEncode = base64Op.equals(BASE64_OP_ENCODE);
            }

            this.scopeName = XMLConfig.get(node, "@scope-name", "");
            if (!this.scopeName.equals("")) {
                this.scriptName = XMLConfig.get(node, "@script-file", INTERNAL_SCRIPT);
                if (this.scriptName.equals(INTERNAL_SCRIPT)) {
                    this.script = XMLConfig.get(node, "Script", "");
                    if (this.script.equals("")) {
                        throw new XMLConfigException(
                                "Must be defined at least the @script-file attribute or the Script element. Node: "
                                        + XPathFinder.buildXPath(node));
                    }
                }
                this.javascriptOn = true;
                operations++;
            }

            Node bbNode = XMLConfig.getNode(node, "*[@type='body-builder']");
            if (bbNode == null) {
                this.bodyBuilderOn = false;
            }
            else {
                String className = XMLConfig.get(bbNode, "@class");
                this.bodyBuilder = (GVBufferBodyMaker) Class.forName(className).newInstance();
                this.bodyBuilder.init(bbNode);
                this.bodyBuilderOn = true;
            }

            if (operations > 1) {
                throw new XMLConfigException(
                        "A ChangeGVBuffer can't execute crypto, compress, base64 or javascript operation together. Node: "
                                + XPathFinder.buildXPath(node));
            }

            String sRetCode = XMLConfig.get(node, "./@ret-code", UNDEFINED);
            if (!sRetCode.equals(UNDEFINED)) {
                this.retCode = Integer.parseInt(sRetCode);
            }
            else {
                this.retCode = Integer.MIN_VALUE;
            }

            NodeList nl = XMLConfig.getNodeList(node, "./PropertyDef");
            if (nl != null) {
                for (int i = 0; i < nl.getLength(); i++) {
                    String name = XMLConfig.get(nl.item(i), "./@name");
                    String value = XMLConfig.get(nl.item(i), "./@value", "");
                    boolean overwriteBody = XMLConfig.getBoolean(nl.item(i), "./@overwrite-body", false);
                    if (overwriteBody) {
                        if (!this.overwriteBodyWithProperty.equals("")) {
                            throw new XMLConfigException(
                                    "The 'overwrite-body' body attribute can be set only once. Node: "
                                            + XPathFinder.buildXPath(nl.item(i)));
                        }
                        this.overwriteBodyWithProperty = name;
                    }
                    this.properties.put(name, value);
                    this.propertiesList.add(name);
                }
            }
        }
        catch (XMLConfigException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new XMLConfigException("Error occurred initializing ChangeGVBuffer: " + exc.getMessage(), exc);
        }
    }

    /**
     * Perform the GVBuffer modification.
     *
     * @param gvBuffer
     *        the input data
     * @param environment
     *        the flow environment
     * @return the modified data
     * @throws GVException
     *         if errors occurs
     * @throws ExpressionEvaluatorException
     */
    public final GVBuffer execute(GVBuffer gvBuffer, Map<String, Object> environment) throws GVException,
            ExpressionEvaluatorException, InterruptedException
    {
        if (!this.system.equals("")) {
            gvBuffer.setSystem(this.system);
        }
        if (!this.service.equals("")) {
            gvBuffer.setService(this.service);
        }
        if (this.retCode != Integer.MIN_VALUE) {
            gvBuffer.setRetCode(this.retCode);
        }

        try {
            PropertiesHandler.enableExceptionOnErrors();
            PropertiesHandler.enableResourceLocalStorage();
            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            for (String fName : this.propertiesList) {
                if (this.overwriteBodyWithProperty.equals(fName)) {
                    String value = gvBuffer.getProperty(fName);
                    if (value == null) {
                        value = this.properties.get(fName);
                    }
                    value = PropertiesHandler.expand(value, params, gvBuffer);
                    gvBuffer.setObject(value.getBytes());
                }
                else {
                    String value = PropertiesHandler.expand(this.properties.get(fName), params, gvBuffer);
                    params.put(fName, value);
                    gvBuffer.setProperty(fName, value);
                }
            }
        }
        catch (Exception exc) {
            throw new GVInternalException("PROPERTIES_EXPANSION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
        finally {
        	PropertiesHandler.disableResourceLocalStorage();
            PropertiesHandler.disableExceptionOnErrors();
        }

        if (this.clearData) {
            gvBuffer.setObject(null);
        }

        if ((this.ognlScript != null) && (this.ognlScript.length() > 0)) {
            handleOgnlScript(gvBuffer, environment);
        }
        if (!this.clearData) {
            if (this.bodyBuilderOn) {
                gvBuffer.setObject(this.bodyBuilder.getBuffer(gvBuffer));
            }
            if (this.cryptoOn) {
                handleEncryption(gvBuffer);
            }
            else if (this.compressOn) {
                handleCompression(gvBuffer);
            }
            else if (this.base64On) {
                handleBase64(gvBuffer);
            }
            else if (this.javascriptOn) {
                handleJavaScript(gvBuffer, environment);
            }
        }

        return gvBuffer;
    }

    /**
     * @param gvBuffer
     * @param environment
     */
    private void handleOgnlScript(GVBuffer gvBuffer, Map<String, Object> environment)
            throws ExpressionEvaluatorException, InterruptedException
    {
        ExpressionEvaluatorHelper.startEvaluation();
        try {
            ExpressionEvaluatorHelper.addToContext("environment", environment);
            ExpressionEvaluator expressionEvaluator = ExpressionEvaluatorHelper.getExpressionEvaluator(ExpressionEvaluatorHelper.OGNL_EXPRESSION_LANGUAGE);
            expressionEvaluator.getValue(this.ognlScript, gvBuffer);
        }
        catch (ExpressionEvaluatorException exc) {
            ThreadUtils.checkInterrupted(exc);
            throw exc;
        }
        finally {
            ExpressionEvaluatorHelper.endEvaluation();
        }
    }

    /**
     * @param gvBuffer
     *        the GVBuffer to handle
     * @throws GVInternalException
     *         if error occurs
     */
    private void handleCompression(GVBuffer gvBuffer) throws GVInternalException
    {
        try {
        	if (this.compressionType.equals(COMPRESS_TYPE_ZIP)) {
	            if (this.compressOpZip) {
	                gvBuffer.setObject(this.zipHelper.zip(toBytes(gvBuffer.getObject())));
	            }
	            else {
	                gvBuffer.setObject(this.zipHelper.unzip(toBytes(gvBuffer.getObject())));
	            }
        	}
        	else {
        		InputStream in = null;
        		OutputStream out = null;
        		ByteArrayOutputStream outBA = new ByteArrayOutputStream();
                try {
                    if (this.compressOpZip) {
                        in = new ByteArrayInputStream(toBytes(gvBuffer.getObject()));
                        out = new GZIPOutputStream(outBA);
                        IOUtils.copy(in, out);
                        out.flush();
                    }
                    else {
                		outBA = new ByteArrayOutputStream();
                		out = outBA;
                    	in = new GZIPInputStream(new ByteArrayInputStream(toBytes(gvBuffer.getObject())));
                        IOUtils.copy(in, out);
                        out.flush();
                    }
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
                    if (out != null) {
                        try {
                            out.close();
                        }
                        catch (Exception exc) {
                            // do nothing
                        }
                    }
                }
                gvBuffer.setObject(outBA.toByteArray());
        	}
        }
        catch (Exception exc) {
            throw new GVInternalException("COMPRESS_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
    }

    /**
     * @param gvBuffer
     *        the GVBuffer to handle
     * @throws GVInternalException
     *         if error occurs
     */
    private void handleEncryption(GVBuffer gvBuffer) throws GVInternalException
    {
        try {
            if (this.cryptoOpEncrypt) {
                gvBuffer.setObject(CryptoHelper.encrypt(this.keyId, toBytes(gvBuffer.getObject()), false));
            }
            else {
                gvBuffer.setObject(CryptoHelper.decrypt(this.keyId, toBytes(gvBuffer.getObject()), false));
            }
        }
        catch (Exception exc) {
            throw new GVInternalException("CRYPTO_ERROR", new String[][]{{"keyId", this.keyId},
                    {"message", exc.getMessage()}}, exc);
        }
    }

    /**
     * @param gvBuffer
     *        the GVBuffer to handle
     * @throws GVInternalException
     *         if error occurs
     */
    private void handleBase64(GVBuffer gvBuffer) throws GVInternalException
    {
        try {
            if (this.base64OpEncode) {
                gvBuffer.setObject(Base64.encodeBase64(toBytes(gvBuffer.getObject())));
            }
            else {
                gvBuffer.setObject(Base64.decodeBase64(toBytes(gvBuffer.getObject())));
            }
        }
        catch (Exception exc) {
            throw new GVInternalException("BASE64_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
    }

    /**
     * @param gvBuffer
     *        the data to handle
     * @param environment
     *        the flow environment
     * @throws GVInternalException
     *         if error occurs
     */
    private void handleJavaScript(GVBuffer gvBuffer, Map<String, Object> environment) throws GVInternalException, InterruptedException
    {
        this.cx = ContextFactory.getGlobal().enterContext();
        Scriptable scope = null;
        try {
            scope = JSInitManager.instance().getJSInit(this.scopeName).getScope();
            scope = JSInit.setProperty(scope, "environment", environment);
            scope = JSInit.setProperty(scope, "data", gvBuffer);
            scope = JSInit.setProperty(scope, "logger", logger);
            if (!this.scriptName.equals(INTERNAL_SCRIPT)) {
                this.script = ScriptCache.instance().getScript(this.scriptName);
            }
            String localScript = this.script;
            if (!PropertiesHandler.isExpanded(this.script)) {
                Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
                localScript = PropertiesHandler.expand(this.script, params, gvBuffer);
                logger.debug("Executing JavaScript:\n" + localScript);
                JavaScriptHelper.executeScript(localScript, this.scriptName, scope, this.cx);
            }
            else {
            	if (this.compiledScript == null) {
            		this.compiledScript = JavaScriptHelper.compileScript(localScript, this.scriptName, scope, this.cx);
            	}
                JavaScriptHelper.executeScript(this.compiledScript, scope, this.cx);
                //JavaScriptHelper.executeScript(localScript, this.scriptName, scope, this.cx);
            }
        }
        catch (Exception exc) {
            ThreadUtils.checkInterrupted(exc);
            throw new GVInternalException("JAVASCRIPT_ERROR", new String[][]{{"scope", this.scopeName},
                    {"script", this.scriptName}, {"message", exc.toString()}}, exc);
        }
    }

    private byte[] toBytes(Object obj) throws Exception {
    	if (obj == null) {
    		return null;
    	}
    	if (obj instanceof byte[]) {
    		return (byte[]) obj;
    	}
    	if (obj instanceof String) {
    		return ((String) obj).getBytes("UTF-8");
    	}
    	if (obj instanceof Node) {
    		return XMLUtils.serializeDOMToByteArray_S((Node) obj);
    	}
    	if (obj instanceof JSONObject) {
    		return ((JSONObject) obj).toString().getBytes("UTF-8");
    	}
    	return null;
    }

    /**
     * Perform cleanup operations.
     *
     */
    public final void cleanUp()
    {
        if (this.bodyBuilder != null) {
            this.bodyBuilder.cleanUp();
        }
        if (this.cx != null) {
            this.cx = null;
            Context.exit();
        }
    }

    /**
     * Perform destroy operations.
     *
     */
    public final void destroy()
    {
        if (this.bodyBuilder != null) {
            this.bodyBuilder.cleanUp();
            this.bodyBuilder = null;
        }
        if (this.cx != null) {
            this.cx = null;
            Context.exit();
        }
    }

}
