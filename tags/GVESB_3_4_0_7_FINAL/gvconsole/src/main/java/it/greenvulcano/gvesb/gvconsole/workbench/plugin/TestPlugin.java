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

import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Node;

/**
 * Plugins interface. The test plug-in implements other methods with these
 * specifics:
 *
 * <pre>
 * public boolean method(HttpServletRequest request) throws Exception;
 *
 * public void method(HttpServletRequest request) throws Exception;
 * </pre>
 *
 * The return value represents how setting the showResult flag. If the method is
 * a void method the showResult method not change.
 *
 * @see TestPluginWrapper
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public interface TestPlugin
{

    /**
     * Here the test plug-in has the ability to read the configuration
     * parameters.
     *
     * @param configNode
     *        the element test node
     * @throws Throwable
     *         if an error occurred
     */
    void init(Node configNode) throws Throwable;

    /**
     * The test plug-in must read the input parameter from the given request and
     * set its internal variables.
     *
     * @param request
     *        HttpServletRequest
     * @param testObject
     *        TestObject object
     * @param testType
     *        the test type requested (single/multiple)
     * @param number
     * @throws Throwable
     *         if an error occurred
     */
    void prepareInput(HttpServletRequest request, TestObject testObject, String testType, int number) throws Throwable;

    /**
     * @param request
     * @throws Throwable
     */
    void prepareInput(HttpServletRequest request) throws Throwable;

    /**
     * The test plug-in must clear all parameters.
     *
     * @param request
     *        HttpServletRequest
     * @throws Throwable
     *         if an error occurred
     */
    void clear(HttpServletRequest request) throws Throwable;

    /**
     * The test plug-in has the ability to release the allocated resources.
     *
     * @param request
     *        HttpServletRequest
     * @throws Throwable
     *         if an error occurred
     */
    void reset(HttpServletRequest request) throws Throwable;

    /**
     * @param fileNameI
     * @throws Throwable
     */
    void savedData(String fileNameI) throws Throwable;

    /**
     * The test plug-in must return the available commands. (Button value)
     *
     * @return arrayString arrayString with the possible action to execute test
     */
    String[] getAvailableCommands();

    /**
     * Invoked when the user perform an upload of local data.
     *
     * @param parameters
     *        parameters to execute the upload action
     * @see MultipartFormDataParser
     * @throws Throwable
     *         if an error occurred
     */
    void upload(MultipartFormDataParser parameters) throws Throwable;

    /**
     * Invoked when the user perform an upload of local data.
     *
     * @param parameters
     *        parameters to execute the upload action
     * @param testObject
     * @see MultipartFormDataParser
     * @throws Throwable
     *         if an error occurred
     */
    void uploadMultiple(MultipartFormDataParser parameters, TestObject testObject) throws Throwable;

    /**
     * Prepare the context
     *
     * @return The initial context
     * @throws Throwable
     *         if an error occurred
     */
    InitialContext prepare() throws Throwable;

    /**
     * Prepare the context
     *
     * @param testObject
     *
     * @return The initial context
     * @throws Throwable
     *         if an error occurred
     */
    InitialContext prepare(TestObject testObject) throws Throwable;

    /**
     *
     * @param data
     * @param encoding
     * @throws Throwable
     */
    void updateDataInput(String data, String encoding) throws Throwable;

    /**
     * @param resetValue
     */
    void setResetValue(String resetValue);

    /**
     * @return the resetValue
     */
    String getResetValue();

    /**
     * @param fileNameI
     * @throws Throwable
     */
    void saveData(String fileNameI) throws Throwable;
}