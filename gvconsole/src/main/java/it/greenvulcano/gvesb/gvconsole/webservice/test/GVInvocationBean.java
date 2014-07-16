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
package it.greenvulcano.gvesb.gvconsole.webservice.test;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.ejb.J2EEGreenVulcano;
import it.greenvulcano.gvesb.core.ejb.J2EEGreenVulcanoHome;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.log.GVLogger;

import javax.rmi.PortableRemoteObject;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * Specific implementation of GreenVulcano client
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class GVInvocationBean extends InvocationBean
{
    private static Logger    logger        = GVLogger.getLogger(GVInvocationBean.class);

    private boolean          initialized   = false;
    private Exception        lastException = null;

    /**
     * The EJB core object
     */
    private J2EEGreenVulcano greenVulcano  = null;

    /**
     * forward command
     */
    private String           forward;

    /**
     * @throws Exception
     */
    public GVInvocationBean() throws Exception
    {
        operations = new String[]{"request", "requestReply", "sendReply", "getReply", "getRequest", "requestGetReply",
                "getRequestSendReply", "forward"};
    }

    /**
     * Constructor with JNDI Context and JNDI Name
     *
     * @param jndiHelper
     * @param jndiName
     * @throws Exception
     */
    public GVInvocationBean(JNDIHelper jndiHelper, String jndiName) throws Exception
    {
        this.jndiName = jndiName;
        this.init(jndiHelper);
    }

    /**
     * Initialize configuration
     *
     * @param configuration
     * @throws Exception
     */
    public void init(Node configuration) throws Exception
    {
        Node j2eeParametersConfiguration = XMLConfig.getNode(configuration, "J2EE-Parameter");
        jndiName = XMLConfig.get(j2eeParametersConfiguration, "@jndi-name");
        transactionMode = XMLConfig.get(j2eeParametersConfiguration, "@transaction-mode");
        Node jndiConfiguration = XMLConfig.getNode(j2eeParametersConfiguration, "JNDIHelper");
        jndiHelper = new JNDIHelper(jndiConfiguration);
        initGreenVulcano();
    }

    /**
     * Initialize configuration with JNDI Context
     *
     * @param jndiHelper
     * @throws Exception
     */
    public void init(JNDIHelper jndiHelper) throws Exception
    {
        setJndiContext(jndiHelper);
        initGreenVulcano();
    }

    /**
     * Retrieve the GreenVulcano EJB
     *
     * @throws Exception
     */
    private void initGreenVulcano() throws Exception
    {
        lastException = null;
        initialized = false;
        try {
            Object home = jndiHelper.lookup(jndiName);
            J2EEGreenVulcanoHome greenVulcanoHome = (J2EEGreenVulcanoHome) PortableRemoteObject.narrow(home,
                    J2EEGreenVulcanoHome.class);
            greenVulcano = greenVulcanoHome.create();
            initialized = true;
        }
        catch (Exception exc) {
            lastException = exc;
            logger.warn("Error initializing the InvocationBean. I'll retry later", exc);
        }
        finally {
            jndiHelper.close();
        }
    }

    /**
     * @return
     */
    public String getForward()
    {
        return forward;
    }

    /**
     * @param forward
     */
    public void setForward(String forward)
    {
        this.forward = forward;
    }

    /**
     * @return
     */
    public boolean isInitialized()
    {
        return initialized;
    }

    /**
     * @return
     */
    public Exception getLastException()
    {
        return lastException;
    }

    /**
     * Invoke the EJB method requested by user
     *
     * @return GVBuffer result
     * @throws Exception
     *         If a error occurred
     */
    public GVBuffer request() throws Exception
    {
        ensureConnect();
        try {
            startTransaction();
            GVBuffer result = greenVulcano.request(getGVBuffer());
            return result;
        }
        finally {
            endTransaction();
        }
    }

    /**
     * Invoke the EJB method requested by user
     *
     * @return GVBuffer result
     * @throws Exception
     *         If a error occurred
     */
    public GVBuffer requestReply() throws Exception
    {
        ensureConnect();
        try {
            startTransaction();
            GVBuffer result = greenVulcano.requestReply(getGVBuffer());
            return result;
        }
        finally {
            endTransaction();
        }
    }

    /**
     * Invoke the ejb method requested by user
     *
     * @return GVBuffer result
     * @throws Exception
     *         If a error occurred
     */
    public GVBuffer sendReply() throws Exception
    {
        ensureConnect();
        try {
            startTransaction();
            GVBuffer result = greenVulcano.sendReply(getGVBuffer());
            return result;
        }
        finally {
            endTransaction();
        }
    }

    /**
     * Invoke the ejb method requested by user
     *
     * @return GVBuffer result
     * @throws Exception
     *         If a error occurred
     */
    public GVBuffer getReply() throws Exception
    {
        ensureConnect();
        try {
            startTransaction();
            GVBuffer result = greenVulcano.getReply(getGVBuffer());
            return result;
        }
        finally {
            endTransaction();
        }
    }

    /**
     * Invoke the ejb method requested by user
     *
     * @return GVBuffer result
     * @throws Exception
     *         If a error occurred
     */
    public GVBuffer getRequest() throws Exception
    {
        ensureConnect();
        try {
            startTransaction();
            GVBuffer result = greenVulcano.getRequest(getGVBuffer());
            return result;
        }
        finally {
            endTransaction();
        }
    }

    /**
     * Invoke the ejb method request/getReply by user
     *
     * @return GVBuffer result
     * @throws Exception
     *         If a throwable occurred
     */
    public GVBuffer requestGetReply() throws Exception
    {
        ensureConnect();
        try {
            startTransaction();
            greenVulcano.request(getGVBuffer());
            GVBuffer result = greenVulcano.getReply(getGVBuffer());
            return result;
        }
        finally {
            endTransaction();
        }
    }

    /**
     * Invoke the EJB method request/getReply by user
     *
     * @return GVBuffer result
     * @throws Exception
     *         If a throwable occurred
     */
    public GVBuffer getRequestSendReply() throws Exception
    {
        ensureConnect();
        try {
            startTransaction();
            greenVulcano.getRequest(getGVBuffer());
            GVBuffer result = greenVulcano.sendReply(getGVBuffer());
            return result;
        }
        finally {
            endTransaction();
        }
    }

    /**
     * Invoke the EJB method requested by user
     *
     * @param forwardName
     *
     * @return GVBuffer result
     * @throws Exception
     *         If a throwable occurred
     */
    public GVBuffer Forward(String forwardName) throws Exception
    {
        ensureConnect();
        try {
            startTransaction();
            GVBuffer result = greenVulcano.forward(getGVBuffer(), forwardName);
            return result;
        }
        finally {
            endTransaction();
        }
    }

    /**
     * Execute the tests
     *
     * @return GVBuffer result
     * @throws Exception
     *         If a throwable occurred
     */
    public Object execute() throws Exception
    {
        GVBuffer data = new GVBuffer(getGVBuffer());
        if (operation.equals("requestreply")) {
            data = this.requestReply();
        }
        else if (operation.equals("request")) {
            data = this.request();
        }
        else if (operation.equals("requestgetreply")) {
            data = this.requestGetReply();
        }
        else if (operation.equals("forward")) {
            data = this.Forward(forward);
        }
        else if (operation.equals("getreply")) {
            data = this.getReply();
        }
        else if (operation.equals("getrequest")) {
            data = this.getRequest();
        }
        else if (operation.equals("getrequestsendreply")) {
            data = this.getRequestSendReply();
        }
        else if (operation.equals("sendreply")) {
            data = this.sendReply();
        }
        else {
            throw new Exception("Operation not implemented");
        }
        return data;
    }

    private void ensureConnect() throws Exception
    {
        if (initialized) {
            return;
        }
        initGreenVulcano();
        if (initialized) {
            return;
        }
        throw lastException;
    }
}
