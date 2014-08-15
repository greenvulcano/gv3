/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 */
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
package it.greenvulcano.gvesb.gvconsole.workbench.plugin;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Node;

/**
 * <code>EjbCallPlugin</code> abstract class with set methods and get methods
 * useful for the GreenVulcano test
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public abstract class EjbCallPlugin implements TestPlugin
{

    /**
     * The JNDI name
     */
    private String           jndiName           = "";

    /**
     * The JNDI Factory
     */
    private String           jndiFactory        = "";

    /**
     * The Initial context
     */
    protected InitialContext newInitialContext  = null;

    /**
     * The URL provider
     */
    private String           providerUrl        = "";

    /**
     * The user value
     */
    private String           user               = "";

    /**
     * The password value
     */
    private String           password           = "";

    /**
     * The binary data
     */
    private boolean          binaryData         = false;

    /**
     * The encoding
     */
    private String           charEncoding       = "";

    /**
     * The output encoding
     */
    private String           charEncodingOutput = "";

    /**
     * The transaction mode
     */
    private String           transactionMode    = "";
    
    /**
     * The transaction timeout
     */
    private String           transactionTimeout = "-1";

    /**
     * The forward name
     */
    protected String         forwardName        = "";

    /**
     * The iteratorNumber
     */
    public static String     iteratorNumber     = "1";

    /**
     * The GVBuffer input
     */
    protected GVBuffer       gvBufferInput      = null;

    /**
     * The GVBuffer output
     */
    protected GVBuffer       gvBufferOutput     = null;

    /**
     * The reset value to reset session and request when the test changes.
     */
    private String           resetValue         = null;

    private String           fileNameInput      = "";

    private String           fileNameOutput     = "";

    /**
     * The home object
     */
    protected Object         home               = null;

    /**
     * The constructor method to create a new GVBuffer object
     *
     * @throws Throwable
     *         If an error occurred
     */
    public EjbCallPlugin() throws Throwable
    {
        gvBufferInput = new GVBuffer();
    }

    /**
     * Init method declared in the <code>TestPlugin</code> interface to
     * initialize the Ejb parameters the encoding and the internal GVBuffer
     * fields
     *
     * @param configNode
     *        Node object of the EJBtest element
     * @throws Throwable
     *         If an error occurred during the initialization
     */
    public void init(Node configNode) throws Throwable
    {
        // Manage the context
        //
        jndiName = XMLConfig.get(configNode, "@jndiName");

        Node jndiCtxNode = XMLConfig.getNode(configNode, "JNDIHelper");
        jndiFactory = XMLConfig.get(jndiCtxNode, "@initial-context-factory");
        providerUrl = XMLConfig.get(jndiCtxNode, "@provider-url");
        user = XMLConfig.get(jndiCtxNode, "@security-principal");
        password = XMLConfig.getDecrypted(jndiCtxNode, "@security-credentials");

        fileNameOutput = PropertiesHandler.expand(XMLConfig.get(configNode, "@fileNameOutput", "C:/Temp/Output.txt"));
        fileNameInput = PropertiesHandler.expand(XMLConfig.get(configNode, "@fileNameInput", "C:/Temp/Input.txt"));

        iteratorNumber = "1";

        // Initialize the Encoding
        //
        charEncoding = "Binary";

        // Initialize transaction
        //
        transactionMode = "No transaction";
        transactionTimeout = "-1";

    }

    /**
     * Set the jndiName of this Plugin object
     *
     * @param jndiName
     *        name of Ejb to call
     */
    public void setJndiName(String jndiName)
    {
        this.jndiName = jndiName;
    }

    /**
     * Set the jndiFactory of this Plugin object
     *
     * @param jndiFactory
     *        name of jndi Factory of the application server where the ejb is
     *        deployed
     */
    public void setJndiFactory(String jndiFactory)
    {
        this.jndiFactory = jndiFactory;
    }

    /**
     * Set the providerUrl of this Plugin object
     *
     * @param providerUrl
     *        Url of the application server where the ejb is deployed
     */
    public void setProviderUrl(String providerUrl)
    {
        this.providerUrl = providerUrl;
    }

    /**
     * Set the user of this Plugin object
     *
     * @param user
     *        user of the application server where the ejb is deployed
     */
    public void setUser(String user)
    {
        this.user = user;
    }

    /**
     * Set the password of this Plugin object
     *
     * @param password
     *        password of the application server where the ejb is deployed
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * Get the jndiName value
     *
     * @return jndiName Name of the ejb to call
     */
    public String getJndiName()
    {
        return jndiName;
    }

    /**
     * Get the jndiFactory value
     *
     * @return jndiFactory jndiFactory of the application server where the ejb
     *         is deployed
     */
    public String getJndiFactory()
    {
        return jndiFactory;
    }

    /**
     * Get the providerUrl value
     *
     * @return providerUrl providerUrl of the application server where the ejb
     *         is deployed
     */
    public String getProviderUrl()
    {
        return providerUrl;
    }

    /**
     * Get the user value
     *
     * @return user user of the application server where the ejb is deployed
     */
    public String getUser()
    {
        return user;
    }

    /**
     * Get the password value
     *
     * @return password providerUrl of the application server where the ejb is
     *         deployed
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Get the inputGVBuffer value
     *
     * @return GVBuffer object
     */
    public GVBuffer getInputGVBuffer()
    {
        return gvBufferInput;
    }


    /**
     * Get the input system from GVBuffer
     *
     * @return system system value <br>
     *         <code>spaces</code> if the GVBuffer is null
     */
    public String getInputSystem()
    {
        if (gvBufferInput == null) {
            return "";
        }
        return gvBufferInput.getSystem();
    }

    /**
     * Get the input service from GVBuffer
     *
     * @return service service value <br>
     *         <code>spaces</code> if the GVBuffer is null
     */
    public String getInputService()
    {
        if (gvBufferInput == null) {
            return "";
        }
        return gvBufferInput.getService();
    }

    /**
     * Get the input Id from GVBuffer and apply toString() method
     *
     * @return id id value <br>
     *         <code>spaces</code> if the GVBuffer is null
     */
    public String getInputId()
    {
        if (gvBufferInput == null) {
            return "";
        }
        return gvBufferInput.getId().toString();
    }

    /**
     * Get the input return code from GVBuffer
     *
     * @return Return code value <br>
     *         <code>spaces</code> if the GVBuffer is null
     */
    public String getInputRetCode()
    {
        if (gvBufferInput == null) {
            return "";
        }
        return "" + (gvBufferInput.getRetCode());
    }

    /**
     * get the encoding field
     *
     * @return The char encoding
     */
    public String getCharEncoding()
    {
        return charEncoding;
    }

    /**
     * get the transaction mode
     *
     * @return The transaction mode
     */
    public String getTransactionMode()
    {
        return transactionMode;
    }

    /**
     * get the transaction timeout
     *
     * @return The transaction timeout
     */
    public String getTransactionTimeout()
    {
        return transactionTimeout;
    }

    /**
     * Get the input data from GVBuffer and apply toString() method.
     *
     * @return data value with the requested encoding <br>
     *         <code>spaces</code> if the GVBuffer is null
     * @throws Throwable
     *         If an error occurred
     */
    public String getInputData() throws Throwable
    {
        if (gvBufferInput == null) {
            return "";
        }

        // return (String) gvBufferInput.getObject();
        Object obj = gvBufferInput.getObject();
        if (obj == null) {
            return "";
        }
        if (obj instanceof byte[]) {
            return new String((byte[]) obj);
        }
        return String.valueOf(obj);
    }

    /**
     * Get the input data from GVBuffer and dump his value in a hex code using a
     * TestPluginWrapper method.
     *
     * @return data data value <br>
     *         <code>spaces</code> if the GVBuffer is null
     * @throws UnsupportedEncodingException
     * @see TestPluginWrapper
     */
    public String getInputDataDump() throws UnsupportedEncodingException
    {
        if (gvBufferInput == null) {
            return "";
        }
        Object obj = gvBufferInput.getObject();
        String toDump = "";
        if (obj instanceof byte[]) {
            if ("Binary".equals(charEncoding)) {
                toDump = new String((byte[]) obj);
            }
            else {
                toDump = new String((byte[]) obj, charEncoding);
            }
        }
        else {
            toDump = obj.toString();
        }
        return TestPluginWrapper.dump(toDump);
    }

    /**
     * Get the input binary data this is <code>true</code> if an upload action
     * requested for a binary file.
     *
     * @return bynaryData boolean flag of the Plugin Object <br>
     */
    public boolean getInputBinaryData()
    {
        return binaryData;
    }

    /**
     * Set the input GVBuffer of the Plugin object
     *
     * @param gvBuffer
     *        GVBuffer input object <br>
     * @throws Throwable
     *         If an error occurred
     */
    public void setInputGVBuffer(GVBuffer gvBuffer) throws Throwable
    {
        if (gvBuffer == null) {
            gvBuffer = new GVBuffer();
        }
        gvBufferInput = gvBuffer;
    }

    /**
     * Set the input system in the GVBuffer input.
     *
     * @param system
     *        system value to set GVBuffer input object <br>
     * @throws Throwable
     *         If an error occurred
     */
    public void setInputSystem(String system) throws Throwable
    {
        gvBufferInput.setSystem(system);
    }

    /**
     * Set the input service in the GVBuffer input.
     *
     * @param service
     *        service value to set GVBuffer input object <br>
     * @throws Throwable
     *         If an error occurred
     */
    public void setInputService(String service) throws Throwable
    {
        gvBufferInput.setService(service);
    }

    /**
     * Set the input Id in the GVBuffer input.
     *
     * @param id
     *        id value to set GVBuffer input object <br>
     * @throws Throwable
     *         If an error occurred
     */
    public void setInputId(String id) throws Throwable
    {
        gvBufferInput.setId(new Id(id));
    }

    /**
     * Set the input Return Code in the GVBuffer input.
     *
     * @param retCode
     *        RetCode value to set GVBuffer input object <br>
     * @throws Throwable
     *         If an error occurred
     */
    public void setInputRetCode(String retCode) throws Throwable
    {
        gvBufferInput.setRetCode(Integer.parseInt(retCode));
    }

    /**
     * Set the input charEncoding
     *
     * @param charEncoding
     *        the encoding requested
     */
    public void setCharEncoding(String charEncoding)
    {
        this.charEncoding = charEncoding;
    }

    /**
     * Set the input transaction mode
     *
     * @param transactionMode
     *        the encoding requested
     * @throws Throwable
     *         If an error occurred
     */
    public void setTransactionMode(String transactionMode) throws Throwable
    {
        this.transactionMode = transactionMode;
    }

    /**
     * Set the input transaction timeout
     *
     * @param transactionTimeout
     *        the encoding requested
     * @throws Throwable
     *         If an error occurred
     */
    public void setTransactionTimeout(String transactionTimeout) throws Throwable
    {
        this.transactionTimeout = transactionTimeout;
    }

    /**
     * Set the input data in the GVBuffer input with the encoding requested.
     *
     * @param data
     *        data value to set GVBuffer input object <br>
     * @throws Throwable
     *         If an error occurred
     */
    public void setInputData(String data) throws Throwable
    {
        gvBufferInput.setObject(data);
    }

    /**
     * Do nothing
     *
     * @param data
     *        string object to dump
     */
    public void setInputDataDump(String data)
    {
        // do nothing
    }

    /**
     * Do nothing
     *
     * @param length
     *        the input length
     */
    public void setInputLength(String length)
    {
        // do nothing
    }

    /**
     * Set the input binary data in the plug-in object.
     *
     * @param binaryData
     *        binaryData value to set the binaryData boolean object
     */
    public void setInputBinaryData(boolean binaryData)
    {
        this.binaryData = binaryData;
    }

    /**
     * Get the GVBuffer Output object
     *
     * @return gvBufferOutput GVBuffer output object
     */
    public GVBuffer getOutputGVBuffer()
    {
        return gvBufferOutput;
    }


    /**
     * Get the output system from GVBuffer output
     *
     * @return system system value <br>
     *         <code>spaces</code> if the GVBuffer is null
     */
    public String getSystem()
    {
        if (gvBufferOutput == null) {
            return "";
        }
        return gvBufferOutput.getSystem();
    }

    /**
     * Get the output service from GVBuffer output
     *
     * @return service service value <br>
     *         <code>spaces</code> if the GVBuffer is null
     */
    public String getService()
    {
        if (gvBufferOutput == null) {
            return "";
        }
        return gvBufferOutput.getService();
    }

    /**
     * Get the output Id from GVBuffer output
     *
     * @return id id value <br>
     *         <code>spaces</code> if the GVBuffer is null
     */
    public String getId()
    {
        if (gvBufferOutput == null) {
            return "";
        }
        return gvBufferOutput.getId().toString();
    }

    /**
     * Get the output charEncoding requested
     *
     * @return charEncoding the encoding requested
     */
    public String getCharEncodingOutput()
    {
        return charEncodingOutput;
    }

    /**
     * Get the output Return Code from GVBuffer output
     *
     * @return the RetCode value or an empty string if the GVBuffer is null
     */
    public String getRetCode()
    {
        if (gvBufferOutput == null) {
            return "";
        }
        return "" + (gvBufferOutput.getRetCode());
    }

    /**
     * Get the output data from GVBuffer output
     *
     * @return data data value with the encoding requested or an empty string if
     *         the GVBuffer is null
     * @throws Throwable
     *         If an error occurred
     */
    public String getData() throws Throwable
    {
        if (gvBufferOutput == null) {
            return "";
        }

        return convertToString(gvBufferOutput.getObject());
    }

    /**
     * @param object
     * @return
     * @throws UnsupportedEncodingException
     * @throws XMLUtilsException
     */
    private String convertToString(Object object)
    {
        String result = "";
        if (object != null) {
            String enc = (charEncodingOutput != null && !charEncodingOutput.equalsIgnoreCase("Binary"))
                    ? charEncodingOutput
                    : "UTF-8";
            if (object instanceof byte[]) {
                try {
                    result = new String((byte[]) object, enc);
                }
                catch (UnsupportedEncodingException exc) {
                    exc.printStackTrace();
                }
            }
            else if (object instanceof Node) {
                XMLUtils xmlUtils = null;
                try {
                    xmlUtils = XMLUtils.getParserInstance();
                    result = xmlUtils.serializeDOM((Node) object, enc, false, true);
                }
                catch (XMLUtilsException exc) {
                    exc.printStackTrace();
                }
                finally {
                    XMLUtils.releaseParserInstance(xmlUtils);
                }
            }
            else {
                result = object.toString();
            }
        }
        return result;
    }

    /**
     * Get the output data from GVBuffer and dump his value in a hex code using
     * a TestPluginWrapper method.
     *
     * @return data data value <br>
     *         <code>spaces</code> if the GVBuffer is null
     * @see TestPluginWrapper
     */
    public String getOutputDataDump()
    {
        if (gvBufferOutput == null) {
            return "";
        }
        return TestPluginWrapper.dump(convertToString(gvBufferOutput.getObject()));
    }

    /**
     * Return the iterator number for the test requested
     *
     * @return the iterator number
     */
    public String getIteratorNumber()
    {
        return iteratorNumber;
    }

    /**
     * Set the output GVBuffer object.
     *
     * @param gvBuffer
     *        the GVBuffer output object <br>
     * @throws Throwable
     *         If an error occurred
     */
    public void setOutputGVBuffer(GVBuffer gvBuffer) throws Throwable
    {
        if (gvBuffer == null) {
            gvBuffer = new GVBuffer();
        }
        gvBufferOutput = gvBuffer;
    }

    /**
     * Set the system in the GVBuffer output object
     *
     * @param system
     *        system value to set GVBuffer output object <br>
     * @throws Throwable
     *         If an error occurred
     */
    public void setSystem(String system) throws Throwable
    {
        gvBufferOutput.setSystem(system);
    }

    /**
     * Set the service in the GVBuffer output object
     *
     * @param service
     *        service value to set GVBuffer output object <br>
     * @throws Throwable
     *         If an error occurred
     */
    public void setService(String service) throws Throwable
    {
        gvBufferOutput.setService(service);
    }

    /**
     * Set the Id in the GVBuffer output object
     *
     * @param id
     *        id value to set GVBuffer output object <br>
     * @throws Throwable
     *         If an error occurred
     */
    public void setId(String id) throws Throwable
    {
        gvBufferOutput.setId(new Id(id));
    }

    /**
     * Set the Return Code in the GVBuffer output object
     *
     * @param retCode
     *        Return Code value to set GVBuffer output object
     * @throws Throwable
     *         If an error occurred
     */
    public void setRetCode(String retCode) throws Throwable
    {
        gvBufferOutput.setRetCode(Integer.parseInt(retCode));
    }

    /**
     * Set charEncoding requested
     *
     * @param charEncodingOutput
     *        char Encoding requested
     * @throws Throwable
     *         If an error occurred
     */
    public void setCharEncodingOutput(String charEncodingOutput) throws Throwable
    {
        this.charEncodingOutput = charEncodingOutput;
    }

    /**
     * Set the data in the GVBuffer output object with the encoding requested
     *
     * @param data
     *        data value to set GVBuffer output object <br>
     * @throws Throwable
     *         If an error occurred
     */
    public void setData(String data) throws Throwable
    {
        gvBufferOutput.setObject(data);
    }

    /**
     * Do nothing
     *
     * @param data
     *        the data to dump
     * @throws Throwable
     *         If an error occurred
     */
    public void setOutputDataDump(String data) throws Throwable
    {
        // do nothing
    }

    /**
     * Set the iterator number for the test requested
     *
     * @param iteratorNumber
     *        the iterator number to set
     */
    public void setIteratorNumber(String iteratorNumber)
    {
        EjbCallPlugin.iteratorNumber = iteratorNumber;
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#prepareInput(javax.servlet.http.HttpServletRequest)
     */
    public void prepareInput(HttpServletRequest request) throws Throwable
    {
        // Do Nothing
    }

    /**
     * This method prepare the GVBuffer Input object
     *
     * @param request
     *        HttpServletRequest object
     * @param testObject
     *        TestObject object
     * @param testType
     *        The test type requested
     * @param number
     * @throws Throwable
     *         if an error occurred
     */
    public void prepareInput(HttpServletRequest request, TestObject testObject, String testType, int number)
            throws Throwable
    {
        String dataValue = "";
        String[] propertyNames = new String[0];
        String[] propertyValues = new String[0];
        String[] nameIntFields = new String[0];
        String[] valueIntFields = new String[0];
        Id id = null;
        String system = "";
        String service = "";
        String transaction = "";
        String transactionTm = "";
        String retCode = "";
        String forwardName = "";

        if (testType != null) {
            if (testType.equals("singleTest")) {

                dataValue = request.getParameter("byteData");
                if (dataValue != null) {
                    testObject.setParameters("dataValue", dataValue);
                }

                propertyNames = request.getParameterValues("property");
                if (propertyNames != null) {
                    testObject.setParameters("extName", propertyNames);
                }

                propertyValues = request.getParameterValues("propertyValue");
                if (propertyValues != null) {
                    testObject.setParameters("extValue", propertyValues);
                }

                nameIntFields = request.getParameterValues("intField");
                if (nameIntFields != null) {
                    testObject.setParameters("intName", nameIntFields);
                }

                valueIntFields = request.getParameterValues("intValue");
                if (valueIntFields != null) {
                    testObject.setParameters("intValue", valueIntFields);
                }
                Integer integer = new Integer(number);
                if ((number == 0) || (number == 1)) {
                    id = new Id(request.getParameter("id"));
                }
                else {
                    id = new Id();
                }

                if (id != null) {
                    testObject.setParameters("id", id.toString());
                }

                if (integer != null) {
                    setIteratorNumber(integer.toString());
                }

                system = request.getParameter("system");
                if (system != null) {
                    testObject.setParameters("system", system);
                }

                service = request.getParameter("service");
                if (service != null) {
                    testObject.setParameters("service", service);
                }

                charEncoding = request.getParameter("charEncoding");
                if (charEncoding != null) {
                    testObject.setParameters("encoding", charEncoding);
                }

                transaction = request.getParameter("transaction");
                if (transaction != null) {
                    testObject.setParameters("transaction", transaction);
                }

                transactionTm = request.getParameter("txTimeout");
                if (transactionTm != null) {
                    testObject.setParameters("txTimeout", transactionTm);
                }

                retCode = request.getParameter("retCode");
                if (retCode != null) {
                    testObject.setParameters("retCode", retCode);
                }

                forwardName = request.getParameter("forwardName");
                if (forwardName != null) {
                    testObject.setParameters("forwardName", forwardName);
                }
            }
            else {
                if (testType.equals("multipleTest")) {
                    system = (String) testObject.getParameters("system");
                    service = (String) testObject.getParameters("service");
                    String newId = (String) testObject.getParameters("newId");
                    if ((newId != null) && (newId.equals("yes"))) {
                        id = new Id();
                        if (id != null) {
                            testObject.setParameters("id", id.toString());
                        }
                    }
                    else {
                        id = new Id((String) testObject.getParameters("id"));
                    }

                    dataValue = (String) testObject.getParameters("dataValue");
                    charEncoding = (String) testObject.getParameters("encoding");
                    transaction = (String) testObject.getParameters("transaction");
                    transactionTm = (String) testObject.getParameters("txTimeout");
                    retCode = (String) testObject.getParameters("retCode");
                    forwardName = (String) testObject.getParameters("forwardName");
                    propertyNames = (String[]) testObject.getParameters("extName");
                    propertyValues = (String[]) testObject.getParameters("extValue");
                    nameIntFields = (String[]) testObject.getParameters("intName");
                    valueIntFields = (String[]) testObject.getParameters("intValue");
                }
            }
        }

        gvBufferInput.setSystem(system);
        gvBufferInput.setService(service);
        gvBufferInput.setId(id);

        charEncodingOutput = "Binary";

        if (charEncoding == null) {
            charEncoding = "Binary";
        }

        if (!binaryData) {
            gvBufferInput.setObject(dataValue);
        }
        setTransactionMode(transaction);
        setTransactionTimeout(transactionTm);

        Set<String> fields = new HashSet<String>();
        if ((propertyNames != null) && (propertyValues != null)) {
            for (int ind = 0; ind < propertyNames.length; ind++) {
                gvBufferInput.setProperty(propertyNames[ind], propertyValues[ind]);
                fields.add(propertyNames[ind]);
            }

        }

        Iterator<String> iterator = gvBufferInput.getPropertyNamesIterator();
        while (iterator.hasNext()) {
            String fieldName = iterator.next();
            if (!fields.contains(fieldName)) {
                iterator.remove();
            }
        }

        int returnCode = (retCode == null || retCode.length() > 0 ? Integer.parseInt(retCode) : 0);
        gvBufferInput.setRetCode(returnCode);

        this.forwardName = forwardName;
    }

    /**
     * Set an Array string with all the possibles test method <br>
     * With this array String the client creates for each a submit button. <br>
     *
     * @return stringArray Containing the possibles action to execute the test
     */
    public String getForwardName()
    {
        return forwardName;
    }

    /**
     * Set the forward name
     *
     * @param forwardName
     *        The forward name
     */
    public void setForwardName(String forwardName)
    {
        this.forwardName = forwardName;
    }

    /**
     * Generate Id action invoked by user. <br>
     * Set the GVBuffer input with the generated id.
     *
     * @param request
     *        HttpServletRequest
     * @throws Throwable
     *         If an error occurred
     */
    public void generateID(HttpServletRequest request) throws Throwable
    {
        gvBufferInput.setId(new Id());
    }

    /**
     * Clear action invoked by user. <br>
     * Instantiate a new GVBuffer Input object <br>
     * Set a new Id in the GVBuffer input <br>
     * Set to <code>null</code> the GVBuffer output Set to false the binaryData
     * boolean fields
     *
     * @param request
     *        HttpServletRequest
     * @throws Throwable
     *         If an error occurred
     */
    public void clear(HttpServletRequest request) throws Throwable
    {
        Id id = gvBufferInput.getId();
        gvBufferInput = new GVBuffer();
        gvBufferInput.setId(id);
        gvBufferOutput = null;
        binaryData = false;
    }

    /**
     * Reset Data invoked by user. <br>
     * Set the InputData field to <code>spaces</code><br>
     * Set the OutputData field to <code>spaces</code><br>
     * Set to false the binaryData boolean fields
     *
     * @param request
     *        HttpServletRequest
     * @throws Throwable
     *         If an error occurred
     */
    public void resetData(HttpServletRequest request) throws Throwable
    {
        setInputData("");
        String data = getData();
        if (!data.equals("")) {
            setData("");
        }
        binaryData = false;
    }

    /**
     *
     * @param fileNameI
     * @throws Throwable
     */
    public void saveData(String fileNameI) throws Throwable
    {
        FileOutputStream fos = new FileOutputStream(fileNameI);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(gvBufferInput);
        oos.flush();

    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#savedData(java.lang.String)
     */
    public void savedData(String fileNameI) throws Throwable
    {
        FileInputStream fis = new FileInputStream(fileNameI);
        ObjectInputStream ois = new ObjectInputStream(fis);
        GVBuffer data = (GVBuffer) ois.readObject();
        fis.close();

        gvBufferInput = new GVBuffer(data);
    }

    /**
     * Reset method do nothing
     *
     * @param request
     * @throws Throwable
     *
     * @see TestPluginWrapper <br>
     */
    public void reset(HttpServletRequest request) throws Throwable
    {
        // do nothing
    }

    /**
     * Get the home object to execute the lookup
     *
     * @throws Throwable
     *         If an error occurred
     */
    protected void getHome() throws Throwable
    {
        // Lookup
        //
        home = newInitialContext.lookup(jndiName);
    }

    /**
     * Get the Initial Context only one time in the servlet call
     *
     * @return initialContext the initial context object
     * @throws Throwable
     *         If an error occurred
     */
    public InitialContext prepare() throws Throwable
    {
        JNDIHelper jndiHelper = new JNDIHelper();
        setSafeNull(jndiHelper, Context.PROVIDER_URL, providerUrl, false);
        setSafeNull(jndiHelper, Context.INITIAL_CONTEXT_FACTORY, jndiFactory, false);
        setSafeNull(jndiHelper, Context.SECURITY_PRINCIPAL, user, true);
        setSafeNull(jndiHelper, Context.SECURITY_CREDENTIALS, password, true);

        newInitialContext = jndiHelper.getInitialContext();

        internalPrepare();

        return newInitialContext;
    }

    /**
     * @param jndiHelper
     * @param providerUrl2
     * @param providerUrl3
     */
    private void setSafeNull(JNDIHelper jndiHelper, String property, String value, boolean canBeEmpty) throws Exception
    {
        boolean isEmpty = false;
        if (value == null || value.length() == 0) {
            value = "";
            isEmpty = true;
        }
        if (!isEmpty || canBeEmpty) {
            jndiHelper.setProperty(property, value);
        }
    }

    /**
     * Get the Initial Context only one time in the servlet call
     *
     * @param testObject
     * @return initialContext the initial context object
     * @throws Throwable
     *         If an error occurred
     */
    public InitialContext prepare(TestObject testObject) throws Throwable
    {
        JNDIHelper jndiHelper = null;

        Node jndiCtxNode = (Node) testObject.getParameters("jndiCtxNode");
        jndiHelper = new JNDIHelper(jndiCtxNode);
        newInitialContext = jndiHelper.getInitialContext();

        internalPrepare();
        return newInitialContext;
    }

    /**
     * Prepare the EJB of plug-in object
     *
     * @throws Throwable
     *         if an error occurred
     */
    protected abstract void internalPrepare() throws Throwable;

    /**
     * Upload action invoked by user. <br>
     * Get the file selected <br>
     * Read the file and write in a byte array <br>
     * Set the bynaryData boolean field at <code>true</code> if the parameter is
     * true <br>
     *
     * @param parameters
     *        the multiform data parser
     * @throws Throwable
     *         If an error occurred
     */
    public void upload(MultipartFormDataParser parameters) throws Throwable
    {

        InputStream inputStream = parameters.getInputStream("data");
        byte[] byteData = readFile(inputStream);

        charEncoding = parameters.getString("charEncoding");
        if (charEncoding.equals("Binary")) {
            binaryData = true;
            gvBufferInput.setObject(byteData);
        }
        else {
            binaryData = false;
            gvBufferInput.setObject(new String(byteData, charEncoding));
        }
    }

    /**
     * Upload action invoked by user. <br>
     * Get the file selected <br>
     * Read the file and write in a byte array <br>
     * Set the bynaryData boolean field at <code>true</code> if the parameter is
     * true <br>
     *
     * @param parameters
     *        the multiform data parser
     * @param testObject
     *        The test in upload mode
     * @throws Throwable
     *         If an error occurred
     */
    public void uploadMultiple(MultipartFormDataParser parameters, TestObject testObject) throws Throwable
    {
        InputStream inputStream = parameters.getInputStream("data");
        byte[] byteData = readFile(inputStream);

        testObject.setParameters("byteData", byteData);
        testObject.setParameters("dataValue", byteData.toString());

        gvBufferInput.setObject(byteData);

        charEncoding = parameters.getString("charEncoding");
        testObject.setParameters("charEncoding", charEncoding);
        testObject.setParameters("upload", "yes");

        if (charEncoding.equals("Binary")) {
            testObject.setParameters("inputBinary", "yes");
        }
        else {
            testObject.setParameters("inputBinary", "no");
        }
    }

    /**
     * Read the Input Stream and write it in the ByteArrayOutputStream
     *
     * @param inputStream
     *        file selected by user
     * @return byteArray input file value written in a ByteArray
     * @throws Throwable
     *         If an error occurred
     */
    public byte[] readFile(InputStream inputStream) throws Throwable
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte[] buf = new byte[2048];
        int len;
        while ((len = inputStream.read(buf)) != -1) {
            byteArrayOutputStream.write(buf, 0, len);
        }
        inputStream.close();

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Update the input data value
     *
     * @param data
     *        The data to update value
     * @param charEncoding
     *        The encoding to encode the data
     * @throws Throwable
     *         if an error occurred
     */
    public void updateDataInput(String data, String charEncoding) throws Throwable
    {
        gvBufferInput.setObject(data);
    }

    /**
     * Get the reset value
     *
     * @return Returns the resetValue.
     */
    public String getResetValue()
    {
        return resetValue;
    }

    /**
     * The reset value
     *
     * @param resetValue
     *        The resetValue to set.
     */
    public void setResetValue(String resetValue)
    {
        this.resetValue = resetValue;
    }

    /**
     * @return Returns the fileNameInput.
     */
    public String getFileNameInput()
    {
        return fileNameInput;
    }

    /**
     * @param fileNameInput
     *        The fileNameInput to set.
     */
    public void setFileNameInput(String fileNameInput)
    {
        this.fileNameInput = fileNameInput;
    }

    /**
     * @return Returns the fileNameOutput.
     */
    public String getFileNameOutput()
    {
        return fileNameOutput;
    }

    /**
     * @param fileNameOutput
     *        The fileNameOutput to set.
     */
    public void setFileNameOutput(String fileNameOutput)
    {
        this.fileNameOutput = fileNameOutput;
    }
}
