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
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.log.GVBufferDump;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This object contains the test information
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class TestGVBufferObject implements TestObject
{

    /**
     * The system value
     */
    private String             system           = "";

    /**
     * The service value
     */
    private String             service          = "";

    /**
     * The id value
     */
    private String             id               = "";

    /**
     * The dataValue value
     */
    private String             dataValue        = "";

    /**
     * The encoding value
     */
    private String             encoding         = "";

    /**
     * The methods value
     */
    private String             method           = "";

    /**
     * The transaction value it can be NoTransaction/Commit/Rollback
     */
    private String             transaction      = "";

    /**
     * The Jndi Name
     */
    private String             jndiName         = "";

    /**
     * The jndiFactory
     */
    private String             jndiFactory      = "";

    /**
     * The provider URL
     */
    private String             providerUrl      = "";

    /**
     * The user
     */
    private String             user             = "";

    /**
     * The password
     */
    private String             password         = "";

    /**
     * The name of properties
     */
    private String[]           extName          = null;

    /**
     * The value of properties
     */
    private String[]           extValue         = null;

    /**
     * The name of internalFields
     */
    private String[]           intName          = null;

    /**
     * The value of internalFields
     */
    private String[]           intValue         = null;

    /**
     * The forward name
     */
    private String             forwardName      = "";

    /**
     * The buffer size
     */
    public static final int    BUFFER_SIZE      = 2048;

    /**
     * The return code
     */
    private String             retCode          = "";

    /**
     * The throwable message if an error occurred
     */
    private String             throwableMsg     = "";

    /**
     * The result post execution method
     */
    private String             result           = "false";

    /**
     * The dataOutput object
     */
    private GVBuffer           dataOutput       = null;


    /**
     * The properties map
     */
    private Map                properties       = null;

    /**
     * The internal fields map
     */
    private Map                internalFields   = null;

    /**
     * The Id class object
     */
    private Id                 idC              = null;

    /**
     * If a new Id is requested
     */
    private String             newId            = "no";

    /**
     * If a binary format isa requested
     */
    private String             binary           = "no";

    /**
     * If an upload is requested
     */
    public String              upload           = "no";

    /**
     * If the test is enabled or not
     */
    private String             enabled          = "yes";

    /**
     * The TestManager object to use the introspection mode
     */
    private TestManager        testManager      = null;

    /**
     * The data pattern to write in the output file
     */
    public static final String DATA_PATTERN     = "yyyy.MM.dd 'at' HH:mm:ss";

    private Node               jndiCtxNode      = null;

    /**
     * Constructor to initialize the gvBuffer input object
     *
     * @throws Throwable
     *         if an error occurred
     */
    public TestGVBufferObject() throws Throwable
    {

    }

    /**
     * Initialize the newObject if requested by user
     *
     * @throws Throwable
     *         If an error occurred
     */
    public void initNewObject()
    {
        transaction = "NoTransaction";
        system = "";
        service = "";
        id = new Id().toString();
        idC = new Id();
        retCode = "1";
        encoding = "UTF-8";
        dataValue = "";
        properties = new HashMap();
        extName = new String[0];
        extValue = new String[0];
        intName = new String[0];
        intValue = new String[0];
        method = "";
        forwardName = "";
        jndiFactory = "weblogic.jndi.WLInitialContextFactory";
        providerUrl = "";
        user = "";
        password = "";
        jndiName = "";
        throwableMsg = "";
        result = "false";
    }

    /**
     * Initialize the object with test information
     *
     * @param configNode
     *        the configuration node value
     * @throws XMLConfigException
     *         If an error occurred reading xml file
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestObject#init(org.w3c.dom.Node)
     */
    public void init(Node configNode) throws XMLConfigException
    {
        transaction = XMLConfig.get(configNode, "@transaction", "No-transaction");
        jndiName = XMLConfig.get(configNode, "@jndiName", "");

        jndiCtxNode = XMLConfig.getNode(configNode, "JNDIHelper");
        jndiFactory = XMLConfig.get(jndiCtxNode, "@initial-context-factory", "");
        providerUrl = XMLConfig.get(jndiCtxNode, "@provider-url", "");
        user = XMLConfig.get(jndiCtxNode, "@security-principal", "");
        password = XMLConfig.getDecrypted(jndiCtxNode, "@security-credentials", "");

        Node node = XMLConfig.getNode(configNode, "Input/GVBuffer");
        if (node != null) {
            system = XMLConfig.get(node, "@system");
            service = XMLConfig.get(node, "@service");
            id = XMLConfig.get(node, "@id");
            if (id == null) {
                idC = new Id();
                id = idC.toString();
            }

            retCode = XMLConfig.get(node, "@retCode");
            if (retCode == null) {
                retCode = "1";
            }
            Node dataNode = XMLConfig.getNode(node, "Data");
            encoding = XMLConfig.get(dataNode, "@encoding");
            dataValue = XMLConfig.get(dataNode, "@value");

            if (dataValue == null) {
                String uploadFile = XMLConfig.get(dataNode, "@fileName");
                if (uploadFile != null) {

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    try {
                        FileInputStream fileInputStream = new FileInputStream(uploadFile);
                        byte[] buf = new byte[BUFFER_SIZE];
                        int len;
                        while ((len = fileInputStream.read(buf)) != -1) {
                            byteArrayOutputStream.write(buf, 0, len);
                        }
                        fileInputStream.close();
                        byteArrayOutputStream.close();
                        dataValue = byteArrayOutputStream.toString();
                    }
                    catch (FileNotFoundException exc) {
                        throw new XMLConfigException("File " + uploadFile + " not found", exc);
                    }
                    catch (IOException exc) {
                        throw new XMLConfigException("File " + uploadFile + " IOException", exc);
                    }
                }
            }

            NodeList extNodeList = XMLConfig.getNodeList(node, "Properties");
            if (extNodeList != null) {
                properties = new HashMap();
                extName = new String[extNodeList.getLength()];
                extValue = new String[extNodeList.getLength()];
                for (int ind = 0; ind < extNodeList.getLength(); ind++) {
                    extName[ind] = XMLConfig.get(extNodeList.item(ind), "@name", "");
                    extValue[ind] = XMLConfig.get(extNodeList.item(ind), "@value", "");
                    properties.put(extName[ind], extValue[ind]);
                }
            }

            NodeList intNodeList = XMLConfig.getNodeList(node, "InternalFields");
            if (intNodeList != null) {
                internalFields = new HashMap();
                intName = new String[intNodeList.getLength()];
                intValue = new String[intNodeList.getLength()];
                for (int ind = 0; ind < intNodeList.getLength(); ind++) {
                    intName[ind] = XMLConfig.get(intNodeList.item(ind), "@name", "");
                    intValue[ind] = XMLConfig.get(intNodeList.item(ind), "@value", "");
                    internalFields.put(intName[ind], intValue[ind]);
                }
            }

            Node flowNode = XMLConfig.getNode(node, "Flow");
            method = XMLConfig.get(flowNode, "@method", "");
            forwardName = XMLConfig.get(flowNode, "@forwardName", "");

        }
    }

    /**
     * Write the output file with test information
     *
     * @param fileWriter
     *        The file writer object
     * @param fileName
     *        The file name
     * @param throwableMsg
     *        The throwable message to write in
     * @param gvBuffer
     *        The GVBuffer object to write in
     * @param ind
     *        The index to identify the test sequence
     */
    public void writeFile(FileWriter fileWriter, String fileName, String throwableMsg, GVBuffer gvBuffer, int ind)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(DATA_PATTERN);
        Date date = new Date();
        String head = "----------- TEST N. " + ind + " " + sdf.format(date) + " -----------";
        String toBeInsert = head;
        ind++;

        if (gvBuffer != null) {
            GVBufferDump dump = new GVBufferDump(gvBuffer);
            toBeInsert = "<pre>" + toBeInsert + dump.toString() + "</pre>";
        }

        if ((throwableMsg != null) && (!throwableMsg.equals(""))) {
            toBeInsert = "<pre>" + toBeInsert + throwableMsg + "</pre>";
        }

        try {
            if (fileWriter != null) {
                fileWriter.write(toBeInsert);
                fileWriter.flush();
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Get the system value for test
     *
     * @return system value
     */
    public String getSystem()
    {
        return system;
    }

    /**
     * Get the service value for test
     *
     * @return service value
     */
    public String getService()
    {
        return service;
    }

    /**
     * Get the id value for test
     *
     * @return id value
     */
    public String getId()
    {
        return id;
    }

    /**
     * Get the transaction value for test
     *
     * @return transaction value
     */
    public String getTransaction()
    {
        return transaction;
    }

    /**
     * Get the encoding value for test
     *
     * @return encoding value
     */
    public String getEncoding()
    {
        return encoding;
    }

    /**
     * Get the retCode value for test
     *
     * @return retCode value
     */
    public String getRetCode()
    {
        return retCode;
    }

    /**
     * Get the dataValue value for test
     *
     * @return dataValue value
     */
    public String getDataValue()
    {
        return dataValue;
    }

    /**
     * Get the methods value for test
     *
     * @return methods value
     */
    public String getMethod()
    {
        return method;
    }

    /**
     * Get the property names
     *
     * @return extName value
     */
    public String[] getExtName()
    {
        return extName;
    }

    /**
     * Get the property values
     *
     * @return extValue value
     */
    public String[] getExtValue()
    {
        return extValue;
    }

    /**
     * Get the forwardName fields values
     *
     * @return forwardName value
     */
    public String getForwardName()
    {
        return forwardName;
    }

    /**
     * Get the jndiFactory fields values
     *
     * @return jndiFactory value
     */
    public String getJndiFactory()
    {
        return jndiFactory;
    }

    /**
     * Get the jndiName fields values
     *
     * @return jndiName value
     */
    public String getJndiName()
    {
        return jndiName;
    }

    /**
     * Get the providerUrl fields values
     *
     * @return providerUrl value
     */
    public String getProviderUrl()
    {
        return providerUrl;
    }

    /**
     * Get the user fields values
     *
     * @return user value
     */
    public String getUser()
    {
        return user;
    }

    /**
     * Get the newId is yes if a new id must be generate
     *
     * @return newId yes or no values
     */
    public String getNewId()
    {
        return newId;
    }

    /**
     * Get the password fields values
     *
     * @return password value
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Get the result fields values
     *
     * @return result value
     */
    public String getResult()
    {
        return result;
    }

    /**
     * Get the dataOutput fields values
     *
     * @return dataOutput value
     */
    public GVBuffer getDataOutput()
    {
        return dataOutput;
    }

    /**
     * Get the binary values
     *
     * @return binary value
     */
    public String getInputBinary()
    {
        return binary;
    }

    /**
     * Get the enabled value
     *
     * @return enabled value
     */
    public String getEnabled()
    {
        return enabled;
    }

    /**
     * Set binary value
     *
     * @param binary
     *        requested
     */
    public void setInputBinary(String binary)
    {
        this.binary = binary;
    }

    /**
     * Set method value
     *
     * @param method
     *        requested
     */
    public void setMethod(String method)
    {
        this.method = method;
    }

    /**
     * Set upload value
     *
     * @param upload
     *        if an upload requested
     */
    public void setUpload(String upload)
    {
        this.upload = upload;
    }

    /**
     * Set throwableMsg message value
     *
     * @param throwableMsg
     *        the exceptiom occurred during test
     */
    public void setThrowableMsg(String throwableMsg)
    {
        this.throwableMsg = throwableMsg;
    }


    /**
     * Get upload value
     *
     * @return upload upload value
     */
    public String getUpload()
    {
        return upload;
    }

    /**
     * Get properties message value
     *
     * @return properties properties map
     */
    public Map getExtendedFields()
    {
        return properties;
    }

    /**
     * Get the internalFields map
     *
     * @return internalFields internalFields map
     */
    public Map getInternalFields()
    {
        return internalFields;
    }

    /**
     * Get the name of the internal field
     *
     * @return Returns the intName.
     */
    public String[] getIntName()
    {
        return intName;
    }

    /**
     * Set the internal field name
     *
     * @param intName
     *        The intName to set.
     */
    public void setIntName(String[] intName)
    {
        this.intName = intName;
    }

    /**
     * @return Returns the intValue.
     */
    public String[] getIntValue()
    {
        return intValue;
    }

    /**
     * @param intValue
     *        The intValue to set.
     */
    public void setIntValue(String[] intValue)
    {
        this.intValue = intValue;
    }

    /**
     * Set result value
     *
     * @param result
     *        the test result
     */
    public void setResult(String result)
    {
        this.result = result;
    }

    /**
     * Set dataOutput message value
     *
     * @param dataOutput
     *        the dataOutput
     */
    public void setDataOutput(GVBuffer dataOutput)
    {
        this.dataOutput = dataOutput;
    }

    /**
     * Set dataValue message value
     *
     * @param dataValue
     *        the dataValue
     */
    public void setDataValue(String dataValue)
    {
        this.dataValue = dataValue;
    }

    /**
     * Set system value
     *
     * @param system
     *        system value
     */
    public void setSystem(String system)
    {
        this.system = system;
    }

    /**
     * Set service value
     *
     * @param service
     *        service value
     */
    public void setService(String service)
    {
        this.service = service;
    }

    /**
     * Set id value
     *
     * @param id
     *        id value
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Set retCode value
     *
     * @param retCode
     *        retCode value
     */
    public void setRetCode(String retCode)
    {
        this.retCode = retCode;
    }

    /**
     * Set extName value
     *
     * @param name
     *        The extended field name value
     */
    public void setExtName(String[] name)
    {
        extName = name;
    }

    /**
     * Set extValue value
     *
     * @param value
     *        The extended field value
     */
    public void setExtValue(String[] value)
    {
        extValue = value;
    }

    /**
     * Set forwardName value
     *
     * @param forwardName
     *        forwardName value
     */
    public void setForwardName(String forwardName)
    {
        this.forwardName = forwardName;
    }

    /**
     * Set charEncoding value
     *
     * @param encoding
     *        charEncoding value
     */
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    /**
     * Set transaction value
     *
     * @param transaction
     *        transaction value
     */
    public void setTransaction(String transaction)
    {
        this.transaction = transaction;
    }

    /**
     * Set the extended fields map
     *
     * @param properties
     *        The property field map
     */
    public void setExtendedFields(HashMap properties)
    {
        this.properties = properties;
    }

    /**
     * Set a new id
     *
     * @param newId
     */
    public void setNewId(String newId)
    {
        this.newId = newId;
    }

    /**
     * Set a multiple test as enabled or disabled
     *
     * @param enabled
     *        The test enabled value
     */
    public void setEnabled(String enabled)
    {
        this.enabled = enabled;
    }

    /**
     * Set the jndiName for EJB invocation
     *
     * @param jndiName
     *        The JndiName value
     */
    public void setJndiName(String jndiName)
    {
        this.jndiName = jndiName;
    }

    /**
     * Set the JndiFactory value
     *
     * @param jndiFactory
     *        The JndiFactory value
     */
    public void setJndiFactory(String jndiFactory)
    {
        this.jndiFactory = jndiFactory;
    }

    /**
     * @param providerUrl
     */
    public void setProviderUrl(String providerUrl)
    {
        this.providerUrl = providerUrl;
    }

    /**
     * The user value to connect at the EJB deployment machine
     *
     * @param user
     *        The user value
     */
    public void setUser(String user)
    {
        this.user = user;
    }

    /**
     * The password value to connect at the EJB deployment machine
     *
     * @param password
     *        The password value
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * Set the TestManager object
     *
     * @param testManager
     *        the testManager Object
     */
    public void setTestManager(TestManager testManager) throws Throwable
    {
        this.testManager = testManager;
    }

    /**
     * Get the TestManager object
     *
     * @return Returns the testManager.
     */
    public TestManager getTestManager()
    {
        return testManager;
    }

    /**
     * Get the output value
     *
     * @return Returns the output.
     */
    public Object getOutput()
    {
        return dataOutput;
    }

    /**
     * Set the output value
     *
     * @param output
     *        The output to set.
     */
    public void setOutput(Object output)
    {
        dataOutput = (GVBuffer) output;
    }

    /**
     * Execute the method requested by user
     *
     * @throws Throwable
     *         if an error occurred
     */
    public void execute() throws Throwable
    {
        if ((method != null) && (!method.equals(""))) {
            testManager.invoke(method);
            TestPluginWrapper wrapper = testManager.getWrapper();
            Throwable throwable = wrapper.getThrowable();
            if (throwable != null) {
                setThrowableMsg(wrapper.getThrowableMessage());
                setResult("false");
            }
            else {
                if (wrapper.getShowsResult()) {
                    setThrowableMsg("");
                    setResult("true");
                    GVBuffer output = (GVBuffer) testManager.get("outputGVBuffer", false);
                    if (output != null) {
                        setDataOutput(output);
                    }
                }
            }
        }
    }

    /**
     * Get the field value with the read method introspection mode
     *
     * @param paramName
     *        The field name to get
     * @return object
     * @throws Throwable
     *         If an error occurred
     */
    public Object getParameters(String paramName) throws Throwable
    {
        PropertyDescriptor descriptor = new PropertyDescriptor(paramName, this.getClass());
        Method method = descriptor.getReadMethod();
        if (method == null) {
            return "";
        }

        try {
            return method.invoke(this, null);
        }
        catch (InvocationTargetException exc) {
            return exc.getTargetException().toString();
        }
    }

    /**
     * Set the field value with the write method introspection mode
     *
     * @param paramName
     *        The field name to set
     * @param paramValue
     *        The field value
     * @return object
     * @throws Throwable
     *         If an error occurred
     */
    public Object setParameters(String paramName, Object paramValue) throws Throwable
    {
        PropertyDescriptor descriptor = new PropertyDescriptor(paramName, this.getClass());
        Method method = descriptor.getWriteMethod();

        if (method == null) {
            return "";
        }

        try {
            return method.invoke(this, new Object[]{paramValue});
        }
        catch (InvocationTargetException exc) {
            return exc.getTargetException().toString();
        }

    }

    /**
     * @see it.greenvulcano.gvesb.workbench.plugin.TestObject#setMethod()
     */
    public void setMethod()
    {
        // Do nothing

    }

    /**
     * @return Returns the throwableMsg.
     */
    public String getThrowableMsg()
    {
        return throwableMsg;
    }

    /**
     * @return Returns the jndiCtxNode.
     */
    public Node getJndiCtxNode()
    {
        return jndiCtxNode;
    }

    /**
     * @param jndiCtxNode
     *        The jndiCtxNode to set.
     */
    public void setJndiCtxNode(Node jndiCtxNode)
    {
        this.jndiCtxNode = jndiCtxNode;
    }
}
