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

import it.greenvulcano.gvesb.core.ejb.J2EEGreenVulcano;
import it.greenvulcano.gvesb.core.ejb.J2EEGreenVulcanoHome;

import javax.rmi.PortableRemoteObject;
import javax.servlet.http.HttpServletRequest;

/**
 * <code>EjbCorePlugin</code> Plugin class to invoke the GVCore ejb
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class EjbCorePlugin extends EjbCallPlugin
{

    /**
     * The EJB core object
     */
    private J2EEGreenVulcano esb = null;

    /**
     * Empty Constructor
     *
     * @throws Throwable
     *         If an error occurred
     */
    public EjbCorePlugin() throws Throwable
    {
        // do nothing
    }

    /**
     * This method get the home and the EJB core object.
     *
     * @throws Throwable
     *         If an error occurred
     */
    @Override
    protected void internalPrepare() throws Throwable
    {
        // Lookup of context
        //
        getHome();
        getGreenVulcano();
    }

    /**
     * Get the GreenVulcano Ejb Object executing narrow method. <br>
     *
     * @throws Throwable
     *         If a throwable occurred
     */
    private void getGreenVulcano() throws Throwable
    {

        // Keep the J2EEGreenVulcanoHome of EJB Object.
        //
        J2EEGreenVulcanoHome gvHome = (J2EEGreenVulcanoHome) PortableRemoteObject.narrow(home,
                J2EEGreenVulcanoHome.class);

        // Create the GreenVulcano EJB Object.
        //
        esb = gvHome.create();
    }

    /**
     * Set an Array string with all the possibles test method <br>
     * With this array String the client creates for each a submit button. <br>
     *
     * @return stringArray Containing the possibles action to execute the test
     */
    public String[] getAvailableCommands()
    {

        return new String[]{"request", "requestReply", "sendReply", "getReply", "getRequest", "requestGetReply",
                "getRequestSendReply", "savedData"};
    }

    // ------------------------------------------------------------------------------------------------------------------
    // GreenVulcano METHODS
    // ------------------------------------------------------------------------------------------------------------------

    /**
     * Invoke the GreenVulcano ejb method requested by user
     *
     * @param request
     *        HttpServletRequest
     * @return boolean this flag value is true (shows result)
     * @throws Throwable
     *         If a throwable occurred
     */
    public boolean request(HttpServletRequest request) throws Throwable
    {

        gvBufferOutput = esb.request(gvBufferInput);
        return true;
    }

    /**
     * Invoke the GreenVulcano ejb method requested by user
     *
     * @param request
     *        HttpServletRequest
     * @return boolean this flag value is true (shows result)
     * @throws Throwable
     *         If a throwable occurred
     */
    public boolean requestReply(HttpServletRequest request) throws Throwable
    {
        gvBufferOutput = esb.requestReply(gvBufferInput);
        return true;
    }

    /**
     * Invoke the GreenVulcano ejb method requested by user
     *
     * @param request
     *        HttpServletRequest
     * @return boolean this flag value is true (shows result)
     * @throws Throwable
     *         If a throwable occurred
     */
    public boolean sendReply(HttpServletRequest request) throws Throwable
    {

        gvBufferOutput = esb.sendReply(gvBufferInput);
        return true;
    }

    /**
     * Invoke the GreenVulcano ejb method requested by user
     *
     * @param request
     *        HttpServletRequest
     * @return boolean this flag value is true (shows result)
     * @throws Throwable
     *         If a throwable occurred
     */
    public boolean getReply(HttpServletRequest request) throws Throwable
    {

        gvBufferOutput = esb.getReply(gvBufferInput);
        return true;
    }

    /**
     * Invoke the GreenVulcano ejb method requested by user
     *
     * @param request
     *        HttpServletRequest
     * @return boolean this flag value is true (shows result)
     * @throws Throwable
     *         If a throwable occurred
     */
    public boolean getRequest(HttpServletRequest request) throws Throwable
    {
        gvBufferOutput = esb.getRequest(gvBufferInput);
        return true;
    }

    /**
     * Invoke the GreenVulcano ejb method request/getReply by user
     *
     * @param request
     *        HttpServletRequest
     * @return boolean this flag value is true (shows result)
     * @throws Throwable
     *         If a throwable occurred
     */
    public boolean requestGetReply(HttpServletRequest request) throws Throwable
    {
        esb.request(gvBufferInput);
        gvBufferOutput = esb.getReply(gvBufferInput);
        return true;
    }

    /**
     * Invoke the GreenVulcano ejb method request/getReply by user
     *
     * @param request
     *        HttpServletRequest
     * @return boolean this flag value is true (shows result)
     * @throws Throwable
     *         If a throwable occurred
     */
    public boolean getRequestSendReply(HttpServletRequest request) throws Throwable
    {
        esb.getRequest(gvBufferInput);
        gvBufferOutput = esb.sendReply(gvBufferInput);
        return true;
    }

    /**
     * Invoke the GreenVulcano ejb method requested by user
     *
     * @param request
     *        HttpServletRequest
     * @return boolean this flag value is true (shows result)
     * @throws Throwable
     *         If a throwable occurred
     */
    public boolean forward(HttpServletRequest request) throws Throwable
    {
        gvBufferOutput = esb.forward(gvBufferInput, super.forwardName);
        return true;
    }
}