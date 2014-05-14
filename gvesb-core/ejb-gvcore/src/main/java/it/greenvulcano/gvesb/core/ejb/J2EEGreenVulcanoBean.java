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
package it.greenvulcano.gvesb.core.ejb;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVPublicException;
import it.greenvulcano.gvesb.core.GreenVulcano;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;
import it.greenvulcano.gvesb.identity.GVIdentityHelper;
import it.greenvulcano.gvesb.identity.impl.EJBIdentityInfo;
import it.greenvulcano.gvesb.j2ee.XAHelper;
import it.greenvulcano.gvesb.j2ee.XAHelperException;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.log.NMDC;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * J2EEGreenVulcano is a stateless Session Bean. <br/>
 * <br/>
 * This EJB is the physical interface to the GreenVulcano core module. The
 * reason why this EJB exists is that the GreenVulcano core module is made by a
 * class called GreenVulcano that offers all the communication services you may
 * need, but this class cannot be used directly for many reasons. One of these
 * reasons is: <br/>
 * <br/>
 * In a distributed environment, where you have to use global transactional
 * services, be fast and reliable, to encapsulate the use of the class into an
 * EJB is the right choice. <br/>
 * <br/>
 * This EJB exposes only the public methods of the GreenVulcano class. These
 * methods offer, in fact, all the communication paradigms that the client
 * needs. <br/>
 * 
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class J2EEGreenVulcanoBean implements SessionBean
{
    /**
     *
     */
    private static final long   serialVersionUID            = 210L;

    /**
     * the subsystem used
     */
    public static final String  SUBSYSTEM                   = "J2EEGreenVulcano";

    /**
     * Logger.
     */
    private static final Logger logger                      = GVLogger.getLogger(J2EEGreenVulcanoBean.class);

    /**
     * Application server name.
     */
    private String              serverName                  = "";

    /**
     * The session context of the EJB.
     */
    private SessionContext      sessionContext              = null;

    /**
     * The GreenVulcanoPool instance.
     */
    private GreenVulcanoPool    greenVulcanoPool            = null;

    /**
     * When this EJB is invoked from within a transactional context, this
     * configuration flag indicates, if set to <code>true</code>, that the
     * transaction must be rolled-back when the GreenVulcano Core invocation
     * raises an <code>GVPublicException</code>.
     */
    private boolean             rollbackOnGVPublicException = false;

    /**
     * Private instance of <code>XAHelper</code> object to access Application
     * Server's TransactionManager and detect current transaction status.
     */
    private XAHelper            xaHelper                    = null;

    /**
     * This method is required by the EJB Specification, but is not used by this
     * bean.
     */
    @Override
    public void ejbActivate()
    {
        // do nothing
    }

    /**
     * Destroy the GreenVulcano instance.
     */
    @Override
    public void ejbRemove()
    {
        greenVulcanoPool = null;
    }

    /**
     * This method is required by the EJB Specification, but is not used by this
     * bean.
     */
    @Override
    public void ejbPassivate()
    {
        // do nothing
    }

    /**
     * Sets the session context.
     * 
     * @param sessionContext
     *        SessionContext that permits to handle the session
     */
    @Override
    public void setSessionContext(SessionContext sessionContext)
    {
        this.sessionContext = sessionContext;
    }

    /**
     * This method corresponds to the create method in the home interface
     * <code>J2EEGreenVulcanoHome</code>. <br/>
     * When the client (In our case the Adapter or Bridge) calls
     * <code>J2EEGreenVulcanoHome.create()</code>, the container allocates an
     * instance of the J2EEGreenVulcanoBean and calls <code>ejbCreate()</code>.<br/>
     * 
     * @exception javax.ejb.CreateException
     *            if there is a communication or system failure
     * @see it.greenvulcano.gvesb.core.ejb.J2EEGreenVulcano
     */
    public void ejbCreate() throws CreateException
    {
        NMDC.push();
        NMDC.clear();
        NMDC.setSubSystem(SUBSYSTEM);
        Context context = null;
        // Create the GreenVulcano core class
        //
        try {
            // Retrieve environment parameters
            context = new InitialContext();
            String serverNameEntry = (String) context.lookup("java:comp/env/server-name-entry");
            serverName = System.getProperty(serverNameEntry, serverNameEntry);
            rollbackOnGVPublicException = Boolean.valueOf(
                    (String) context.lookup("java:comp/env/rollback-on-exception")).booleanValue();

            logger.debug("serverName                 : " + serverName);
            logger.debug("rollbackOnGVPublicException: " + rollbackOnGVPublicException);

            // Initialize XAHelper object
            xaHelper = new XAHelper();
            xaHelper.setLogger(logger);

            NMDC.setServer(serverName);

            // Initialize GreenVulcano instance pool
            greenVulcanoPool = GreenVulcanoPoolManager.instance().getGreenVulcanoPool(SUBSYSTEM);

        }
        catch (Exception exception) {
            // Log the error
            //
            exception.printStackTrace();
            // Turn the exception to the caller
            //
            throw new CreateException(exception.toString());

        }
        finally {
            if (context != null) {
                try {
                    context.close();

                }
                catch (NamingException exc) {
                    logger.warn("Error while closing JNDI Context: " + exc, exc);
                }
            }

            NMDC.pop();
        }
    }

    /**
     * Invoke the request method of the GreenVulcano class.
     * 
     * @see it.greenvulcano.gvesb.core.GreenVulcano
     * @param gvBuffer
     *        The GreenVulcano data coming from the client
     * @return GVBuffer The GreenVulcano data elaborated by GreenVulcano
     * @throws GVPublicException
     *         if an error occurs at virtual communication layer or core level
     */
    public GVBuffer request(GVBuffer gvBuffer) throws GVPublicException
    {
        GVIdentityHelper.push(new EJBIdentityInfo(sessionContext));
        try {
            GreenVulcano greenVulcano = null;
            try {
                NMDC.push();
                GVBufferMDC.put(gvBuffer);
                NMDC.setServer(serverName);
                NMDC.setSubSystem(SUBSYSTEM);

                dumpCallerPrincipal();

                // Local variables
                //
                GVBuffer gvOutputData = null; // GreenVulcano elaborated data
                // returned
                // by
                // the GreenVulcano core class

                boolean inTransaction = isTransactional();

                // Turn the request directly to the GreenVulcano core main class
                //
                try {
                    greenVulcano = getGreenVulcano(gvBuffer);
                    // Turn the control to the GreenVulcano ESB request
                    //
                    gvOutputData = greenVulcano.request(gvBuffer);
                }
                catch (GVPublicException gvPublicException) {

                    // If required, roll-back the transaction (if any)
                    if (inTransaction && rollbackOnGVPublicException) {
                        logger.error("J2EEGreenVulcanoBean is rolling back the transaction...");
                        sessionContext.setRollbackOnly();
                    }

                    // Log the error condition and let the exception flow unchanged
                    // to the client
                    //
                    throw gvPublicException;
                }

                // The elaborated buffer is returned to the caller (usually an
                // Adapter or a Bridge)
                //
                return gvOutputData;
            }
            finally {
                try {
                    greenVulcanoPool.releaseGreenVulcano(greenVulcano);
                }
                catch (Exception exc) {
                    // do nothing
                }
                NMDC.pop();
            }
        }
        finally {
            GVIdentityHelper.pop();
        }
    }

    /**
     * Invoke the requestReply method of the GreenVulcano class.
     * 
     * @see it.greenvulcano.gvesb.core.GreenVulcano
     * @param gvBuffer
     *        The GreenVulcano data coming from the client
     * @return GVBuffer The GreenVulcano data elaborated by GreenVulcano
     * @throws GVPublicException
     *         if an error occurs at virtual communication layer or core level
     */
    public GVBuffer requestReply(GVBuffer gvBuffer) throws GVPublicException
    {
        GVIdentityHelper.push(new EJBIdentityInfo(sessionContext));
        try {
            GreenVulcano greenVulcano = null;
            try {
                NMDC.push();
                GVBufferMDC.put(gvBuffer);
                NMDC.setServer(serverName);
                NMDC.setSubSystem(SUBSYSTEM);

                dumpCallerPrincipal();

                // Local variables
                //
                GVBuffer gvOutputData = null; // GreenVulcano elaborated data
                // returned
                // by
                // the GreenVulcano core class

                boolean inTransaction = isTransactional();

                // Turn the requestReply directly to the GreenVulcano core main
                // class
                //
                try {
                    greenVulcano = getGreenVulcano(gvBuffer);
                    // Turn the control to the GreenVulcano ESB RequestReply
                    //
                    gvOutputData = greenVulcano.requestReply(gvBuffer);
                }
                catch (GVPublicException gvPublicException) {

                    // If required, roll-back the transaction (if any)
                    if (inTransaction && rollbackOnGVPublicException) {
                        logger.error("J2EEGreenVulcanoBean is rolling back the transaction...");
                        sessionContext.setRollbackOnly();
                    }

                    // Log the error condition and let the exception flow unchanged
                    // to the client
                    //
                    throw gvPublicException;
                }

                // The elaborated buffer is returned to the caller (usually an
                // Adapter or a Bridge)
                //
                return gvOutputData;
            }
            finally {
                try {
                    greenVulcanoPool.releaseGreenVulcano(greenVulcano);
                }
                catch (Exception exc) {
                    // do nothing
                }
                NMDC.pop();
            }
        }
        finally {
            GVIdentityHelper.pop();
        }
    }

    /**
     * Invoke the getReply method of the GreenVulcano class.
     * 
     * @see it.greenvulcano.gvesb.core.GreenVulcano
     * @param gvBuffer
     *        The GreenVulcano data coming from the client
     * @return GVBuffer The GreenVulcano data elaborated by GreenVulcano
     * @throws GVPublicException
     *         if an error occurs at virtual communication layer or core level
     */
    public GVBuffer getReply(GVBuffer gvBuffer) throws GVPublicException
    {
        GVIdentityHelper.push(new EJBIdentityInfo(sessionContext));
        try {
            GreenVulcano greenVulcano = null;
            try {
                NMDC.push();
                GVBufferMDC.put(gvBuffer);
                NMDC.setServer(serverName);
                NMDC.setSubSystem(SUBSYSTEM);

                dumpCallerPrincipal();

                // Local variables
                //
                GVBuffer gvOutputData = null; // GreenVulcano elaborated data
                // returned
                // by
                // the GreenVulcano core class

                boolean inTransaction = isTransactional();

                // Turn the getReply directly to the GreenVulcano core main class
                //
                try {
                    greenVulcano = getGreenVulcano(gvBuffer);
                    // Turn the control to the GreenVulcano ESB getReply
                    //
                    gvOutputData = greenVulcano.getReply(gvBuffer);
                }
                catch (GVPublicException gvPublicException) {

                    // If required, roll-back the transaction (if any)
                    if (inTransaction && rollbackOnGVPublicException) {
                        logger.error("J2EEGreenVulcanoBean is rolling back the transaction...");
                        sessionContext.setRollbackOnly();
                    }

                    // Log the error condition and let the exception flow unchanged
                    // to the client
                    //
                    throw gvPublicException;
                }

                // The elaborated buffer is returned to the caller (usually an
                // Adapter or a Bridge)
                //
                return gvOutputData;
            }
            finally {
                try {
                    greenVulcanoPool.releaseGreenVulcano(greenVulcano);
                }
                catch (Exception exc) {
                    // do nothing
                }
                NMDC.pop();
            }
        }
        finally {
            GVIdentityHelper.pop();
        }
    }

    /**
     * Invoke the sendReply method of the GreenVulcano class.
     * 
     * @see it.greenvulcano.gvesb.core.GreenVulcano
     * @param gvBuffer
     *        The GreenVulcano data coming from the client. It is used to get
     *        the the right request
     * @return GVBuffer The GreenVulcano data returned by GreenVulcano
     * @throws GVPublicException
     *         if an error occurs at virtual communication layer or core level
     */
    public GVBuffer sendReply(GVBuffer gvBuffer) throws GVPublicException
    {
        GVIdentityHelper.push(new EJBIdentityInfo(sessionContext));
        try {
            GreenVulcano greenVulcano = null;
            try {
                NMDC.push();
                GVBufferMDC.put(gvBuffer);
                NMDC.setServer(serverName);
                NMDC.setSubSystem(SUBSYSTEM);

                dumpCallerPrincipal();

                // Local variables
                //
                GVBuffer gvOutputData = null; // GreenVulcano elaborated data
                // returned
                // by
                // the GreenVulcano core class

                boolean inTransaction = isTransactional();

                // Turn the sendReply directly to the GreenVulcano core main class
                //
                try {
                    greenVulcano = getGreenVulcano(gvBuffer);
                    // Turn the control to the GreenVulcano ESB sendReply
                    //
                    gvOutputData = greenVulcano.sendReply(gvBuffer);
                }
                catch (GVPublicException gvPublicException) {

                    // If required, roll-back the transaction (if any)
                    if (inTransaction && rollbackOnGVPublicException) {
                        logger.error("J2EEGreenVulcanoBean is rolling back the transaction...");
                        sessionContext.setRollbackOnly();
                    }

                    // Log the error condition and let the exception flow unchanged
                    // to the client
                    //
                    throw gvPublicException;
                }

                // The elaborated buffer is returned to the caller (usually an
                // Adapter or a Bridge)
                //
                return gvOutputData;
            }
            finally {
                try {
                    greenVulcanoPool.releaseGreenVulcano(greenVulcano);
                }
                catch (Exception exc) {
                    // do nothing
                }
                NMDC.pop();
            }
        }
        finally {
            GVIdentityHelper.pop();
        }
    }

    /**
     * Invoke the getRequest method of the GreenVulcano class.
     * 
     * @see it.greenvulcano.gvesb.core.GreenVulcano
     * @param gvBuffer
     *        The GreenVulcano data coming from the client. It is used to get
     *        the the right request
     * @return GVBuffer The GreenVulcano data returned by GreenVulcano
     * @throws GVPublicException
     *         if an error occurs at virtual communication layer or core level
     */
    public GVBuffer getRequest(GVBuffer gvBuffer) throws GVPublicException
    {
        GVIdentityHelper.push(new EJBIdentityInfo(sessionContext));
        try {
            GreenVulcano greenVulcano = null;
            try {
                NMDC.push();
                GVBufferMDC.put(gvBuffer);
                NMDC.setServer(serverName);
                NMDC.setSubSystem(SUBSYSTEM);

                dumpCallerPrincipal();

                // Local variables
                //
                GVBuffer gvOutputData = null; // GreenVulcano elaborated data
                // returned
                // by
                // the GreenVulcano core class

                boolean inTransaction = isTransactional();

                // Turn the getRequest directly to the GreenVulcano core main class
                //
                try {
                    greenVulcano = getGreenVulcano(gvBuffer);
                    // Turn the control to the GreenVulcano ESB getRequest
                    //
                    gvOutputData = greenVulcano.getRequest(gvBuffer);
                }
                catch (GVPublicException gvPublicException) {

                    // If required, roll-back the transaction (if any)
                    if (inTransaction && rollbackOnGVPublicException) {
                        logger.error("J2EEGreenVulcanoBean is rolling back the transaction...");
                        sessionContext.setRollbackOnly();
                    }

                    // Log the error condition and let the exception flow unchanged
                    // to the client
                    //
                    throw gvPublicException;
                }

                // The elaborated buffer is returned to the caller (usually an
                // Adapter or a Bridge)
                //
                return gvOutputData;
            }
            finally {
                try {
                    greenVulcanoPool.releaseGreenVulcano(greenVulcano);
                }
                catch (Exception exc) {
                    // do nothing
                }
                NMDC.pop();
            }
        }
        finally {
            GVIdentityHelper.pop();
        }
    }

    /**
     * Invoke the forward method of the GreenVulcano class.
     * 
     * @see it.greenvulcano.gvesb.core.GreenVulcano
     * @param gvBuffer
     *        The GreenVulcano data coming from the client. It is used to get
     *        the the right request
     * @param name
     *        The forward name to invoke
     * @return GVBuffer The GreenVulcano data returned by GreenVulcano
     * @throws GVPublicException
     *         if an error occurs at virtual communication layer or core level
     */
    public GVBuffer forward(GVBuffer gvBuffer, String name) throws GVPublicException
    {
        GVIdentityHelper.push(new EJBIdentityInfo(sessionContext));
        try {
            GreenVulcano greenVulcano = null;
            try {
                NMDC.push();
                GVBufferMDC.put(gvBuffer);
                NMDC.setServer(serverName);
                NMDC.setSubSystem(SUBSYSTEM);

                dumpCallerPrincipal();

                // Local variables
                //
                GVBuffer gvOutputData = null; // GreenVulcano elaborated data
                // returned
                // by
                // the GreenVulcano core class

                boolean inTransaction = isTransactional();

                // Turn the forward directly to the GreenVulcano core main class
                //
                try {
                    greenVulcano = getGreenVulcano(gvBuffer);
                    // Turn the control to the GreenVulcano ESB forward
                    //
                    gvOutputData = greenVulcano.forward(gvBuffer, name);
                }
                catch (GVPublicException gvPublicException) {

                    // If required, roll-back the transaction (if any)
                    if (inTransaction && rollbackOnGVPublicException) {
                        logger.error("J2EEGreenVulcanoBean is rolling back the transaction...");
                        sessionContext.setRollbackOnly();
                    }

                    // Log the error condition and let the exception flow unchanged
                    // to the client
                    //
                    throw gvPublicException;
                }

                // The elaborated buffer is returned to the caller (usually an
                // Adapter or a Bridge)
                //
                return gvOutputData;
            }
            finally {
                try {
                    greenVulcanoPool.releaseGreenVulcano(greenVulcano);
                }
                catch (Exception exc) {
                    // do nothing
                }
                NMDC.pop();
            }
        }
        finally {
            GVIdentityHelper.pop();
        }
    }

    /**
     * @param gvBuffer
     * @return
     * @throws GVPublicException
     */
    private GreenVulcano getGreenVulcano(GVBuffer gvBuffer) throws GVPublicException
    {
        GreenVulcano greenVulcano = null;
        try {
            greenVulcano = greenVulcanoPool.getGreenVulcano(gvBuffer);
        }
        catch (Exception exc) {
            greenVulcano = null;
        }
        if (greenVulcano == null) {
            throw new GVPublicException("GV_GENERIC_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()},
                    {"message", "Timeout occurred in GreenVulcanoPool.getGreenVulcano()"}});
        }
        return greenVulcano;
    }

    /**
     * Print the caller principal
     */
    private void dumpCallerPrincipal()
    {
        String principal = sessionContext.getCallerPrincipal().toString();
        Level principalLevel = Level.DEBUG;
        logger.log(principalLevel, "EJB Security pricipal : " + principal);
    }

    /**
     * This method returns <code>true</code> if the EJB is being called from
     * within a transactional context, <code>false</code> otherwise.
     */
    private boolean isTransactional()
    {
        boolean result = false;
        try {
            result = xaHelper.isTransactionActive();
        }
        catch (XAHelperException exc) {
            logger.warn("Error while checking transaction status: " + exc, exc);
            result = false;
        }

        logger.debug("J2EEGreenVulcanoBean is " + (result ? "" : "NOT ")
                + "being called from WITHIN a transactional context");
        return result;
    }
}