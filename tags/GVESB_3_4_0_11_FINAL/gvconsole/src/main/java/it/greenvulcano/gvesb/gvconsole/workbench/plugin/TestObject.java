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

import it.greenvulcano.configuration.XMLConfigException;

import org.w3c.dom.Node;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public interface TestObject
{
    /**
     * Initialize the information test
     *
     * @param node
     *        the configuration node
     * @throws XMLConfigException
     *         if an error occurred reading configuration file
     */
    void init(Node node) throws XMLConfigException;

    /**
     * If the test is enabled or not
     *
     * @return the enabled value
     */
    String getEnabled();

    /**
     * Set the enabled value for test
     *
     * @param enabled
     *        the enabled value for test
     */
    void setEnabled(String enabled);

    /**
     * Set the testManager to work with the interface testObject
     *
     * @param testManager
     *        the TestManager object
     * @throws Throwable
     *         if an error occurred
     */
    void setTestManager(TestManager testManager) throws Throwable;

    /**
     * Executes the method requested
     *
     * @throws Throwable
     *         if an error occurred
     */
    void execute() throws Throwable;

    /**
     * Get the field value with the read method introspection mode
     *
     * @param paramName
     *        The paramName to get value
     * @return object
     * @throws Throwable
     *         if an error occurred
     */
    Object getParameters(String paramName) throws Throwable;

    /**
     * Set the field value with the write method introspection mode
     *
     * @param paramName
     *        The field name to value
     * @param paramValue
     *        The value to set field with
     * @return object
     * @throws Throwable
     *         If an error occurred
     */
    Object setParameters(String paramName, Object paramValue) throws Throwable;

    /**
     * Get the method
     *
     * @return method The method
     * @throws Throwable
     *         if an error occurred
     */
    String getMethod() throws Throwable;

    /**
     * Set the method
     *
     * @param method
     *        The method
     * @throws Throwable
     *         if an error occurred
     */
    void setMethod(String method) throws Throwable;

    /**
     * Initialize a new object in a multiple test request.
     *
     * @throws Throwable
     *         if an error occurred
     */
    void initNewObject() throws Throwable;

    /**
     * Get the Throwable message of Throwable object
     *
     * @return message The Throwable Message
     * @throws Throwable
     *         If an error occurred
     */
    String getThrowableMsg() throws Throwable;

    /**
     * Get a string value to know if the test is finished or not
     *
     * @return result The result at end test
     * @throws Throwable
     *         if an error occurred
     */
    String getResult() throws Throwable;
}
