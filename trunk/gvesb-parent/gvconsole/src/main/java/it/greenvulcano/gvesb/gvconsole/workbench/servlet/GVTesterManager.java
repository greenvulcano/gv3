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
package it.greenvulcano.gvesb.gvconsole.workbench.servlet;

/**
 * Standard Java imports.
 */
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestGVBufferObject;
import it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestManager;
import it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestObject;
import it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin;
import it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPluginWrapper;
import it.greenvulcano.gvesb.identity.GVIdentityHelper;
import it.greenvulcano.gvesb.identity.impl.HTTPIdentityInfo;
import it.greenvulcano.gvesb.log.GVBufferDump;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;

/**
 * <code>GVTesterManager</code> servlet get a method parameter from index.jsp
 * <p>
 * The method value can be :
 * <p>
 * - "Execute Test" Execute required test. - Clear resets the inserted fields on
 * the java server page. - Generate Id generates the Id number. - Reset Reset
 * the Plugin object in session to null.
 * 
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class GVTesterManager extends HttpServlet
{
    private static final long  serialVersionUID = 300L;

    /**
     * The user transaction
     */
    private UserTransaction    userTransaction  = null;

    /**
     * The transaction requested
     */
    private String             transaction      = "";

    /**
     * The GVBuffer object output
     */
    private GVBuffer           output           = null;

    /**
     * The response object
     */
    private String             responseFromHttp = "";


    /**
     * The throwable message received if an error occurred during the test
     * execution
     */
    private String             throwableMsg     = "";

    /**
     * The counter for the execution ok
     */
    private int                countOk          = 0;

    /**
     * The counter for the execution ko
     */
    private int                countKo          = 0;

    /**
     * The File to write output
     */
    private FileWriter         writer           = null;

    /**
     * The String to write in the file
     */
    private String             toBeInsert       = "";

    /**
     * The data pattern to write in the output file
     */
    public static final String DATA_PATTERN     = "yyyy.MM.dd 'at' HH:mm:ss";

    private String             fileNameI        = "";

    /**
     * This method do :</br> <li>call the Plugin method requested by user.</li>
     * <li>Prepare the Initial context</li> <li>Start the transaction</li>
     * 
     * @param request
     *        HttpServletRequest object
     * @param response
     *        HttpServletResponse object
     * @throws ServletException
     *         If an error occurred
     * @throws IOException
     *         If an error occurred
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        HttpSession session = request.getSession();

        // Get the method parameter name to invoke it
        //
        String method = request.getParameter("method");

        String path = "";

        transaction = request.getParameter("transaction");
        String operationName = request.getParameter("operationName");

        fileNameI = request.getParameter("fileNameI");

        String standardAction = request.getParameter("standardAction");

        // Fake object if we go in error (or other methods than "singleTest")
        // before executing the test

        TestManager testManager = new TestManager(request);
        try {
            TestPlugin testPlugin = testManager.getPlugin();
            TestObject testObject = new TestGVBufferObject();
            testObject.setTestManager(testManager);
            testObject.setMethod(method);

            testPlugin.prepareInput(request, testObject, "singleTest", 1);
        }
        catch (Throwable e) {
            // e.printStackTrace();
        }

        // End fake initialization

        if ("setJNDIParamenters".equals(method)) {
            response.sendRedirect("testing/setEjb.jsp");
        }
        else if ("resetData".equals(method)) {
            try {
                testManager.set("inputData", "");
                testManager.set("inputBinaryData", false);
            }
            catch (Throwable e) {
                e.printStackTrace();
            }
            response.sendRedirect("testing/index.jsp");
        }
        else { // singleTest
            manageSingleTest(method, request, session);
            String redirect = (String) session.getAttribute("redirect");
            if (redirect == null) {
                response.sendRedirect("testing/index.jsp?countOk=" + countOk + "&countKo=" + countKo);
            }
            else {
                response.sendRedirect(redirect);
            }
        }

        // if (testType != null) {
        // if (testType.equals("singleTest")) {
        // manageSingleTest(method, request, session);
        // path = "testCase/";
        // }
        // else if (testType.equals("multipleTest")) {
        // manageMultipleTest(method, request, session, response,
        // standardAction);
        // path = "testCase/";
        // }
        // else if (testType.equals("monitoring")) {
        // manageMonitoring(method, request, session);
        // path = "administration/";
        // }
        // else if (testType.equals("jms")) {
        // manageJms(method, request, session);
        // path = "administration/";
        // }
        // else if (testType.equals("jmsSend") ||
        // testType.equals("gvBufferBody")) {
        // manageJms(method, request, session);
        // }
        // else if (testType.equals("dump")) {
        // manageMonitoring(method, request, session);
        // path = "administration/";
        // }
        // }

        // In every case go to the index.jsp of current test
        //
        // if (operationName != null) {
        // String element = request.getParameter("element");
        // String elementOne = request.getParameter("element_one");
        // response.sendRedirect("jsp/operationDetail.jsp?method=SystemServices&element="
        // + element + "&element_one="
        // + elementOne);
        // }
        // else {
        // if (method != null) {
        // if (method.equals("synchronize") ||
        // method.equals("synchronizeSystemServices")
        // || method.equals("resetCounter") ||
        // method.equals("resetSystemServiceCounter")) {
        // method = "SystemServices";
        // }
        //
        // if (method.equals("browseProperties")) {
        // response.sendRedirect("jsp/msgProperties.jsp");
        // }
        // else {
        // if (testType.equals("gvBufferBody")
        // && ((method.equals("resetData") || method.equals("generateID") ||
        // method.equals("resetGVBuffer")))) {
        // response.sendRedirect("testing/gvBufferBody.jsp");
        // }
        // if (testType.equals("gvBufferBody") && (method.equals("goBack"))) {
        // response.sendRedirect("jsp/jmsSender.jsp");
        // }
        // else if (testType.equals("jmsSend") ||
        // (testType.equals("gvBufferBody"))) {
        // response.sendRedirect("jsp/jmsSender.jsp");
        // }
        // else {
        // String currentTest = (String) session.getAttribute("currentTest");
        // response.sendRedirect(path + currentTest + "/index.jsp?method=" +
        // method + "&testType="
        // + testType + "&countOk=" + countOk + "&countKo=" + countKo);
        // }
        // }
        // }
        // else if ((standardAction != null) && testType.equals("multipleTest"))
        // {
        // String currentTest = (String) session.getAttribute("currentTest");
        // response.sendRedirect(path + currentTest + "/index.jsp?method=" +
        // method + "&testType=" + testType
        // + "&countOk=" + countOk + "&countKo=" + countKo);
        // }
        // }
    }

    /**
     * This method invoke the GreenVulcano method and valorize the output
     * corrisponding.
     * 
     * @param testObject
     *        The TestGVBufferObject containing single test information
     * @param wrapper
     *        The TestPluginWrapper object
     * @param testManager
     *        The TestManager object
     * @param currentTest
     *        The requested test
     * @param i
     *        The index representing the execution test
     * @throws Throwable
     *         If an error occurred
     */
    private void executeTest(TestObject testObject, TestPluginWrapper wrapper, TestManager testManager,
            String currentTest, int i) throws Throwable
    {
        String object = "";
        testObject.execute();

        Throwable throwable = wrapper.getThrowable();
        if (throwable != null) {
            throwableMsg = wrapper.getThrowableMessage();
            object = throwableMsg;
        }
        else {
            if (wrapper.getShowsResult()) {
                if (currentTest.equals("gvHttpInbound")) {
                    responseFromHttp = (String) testManager.get("responseContent", false);
                    object = responseFromHttp;
                }
                else if (currentTest.equals("gvxdt")) {
                    // outputInternal = (InternalGVBuffer)
                    // testManager.get("internalGVBufferOutput", false);
                    // GVInternalDump dump = new
                    // GVInternalDump((InternalGVBuffer) outputInternal);
                    // object = dump.toString();
                }
                else {
                    output = (GVBuffer) testManager.get("outputGVBuffer", false);
                    GVBufferDump dump = new GVBufferDump(output);
                    object = dump.toString();
                }
            }
        }

        createString(currentTest, object, i);
    }

    /**
     * This method manages the single test.
     * 
     * @param method
     *        The method requested
     * @param request
     *        The HttpServletRequest object
     * @param session
     *        The HttpSession object
     */
    private void manageSingleTest(String method, HttpServletRequest request, HttpSession session)
    {
        session.setAttribute("mapTestObject", null);

        InitialContext context = null;
        TestManager testManager = new TestManager(request);
        int number = 0;
        countOk = 0;
        countKo = 0;

        String iteratorNumber = request.getParameter("iteratorNumber");
        GVIdentityHelper.push(new HTTPIdentityInfo(request));
        try {
            TestPluginWrapper wrapper = testManager.getWrapper();

            try {
                // Get the plugin class
                //
                TestPlugin testPlugin = testManager.getPlugin();
                if (method != null) {
                    if (!method.equals("savedData")) {
                        if ((!method.equals("")) && (!method.equals("generateID")) && (!method.equals("clear"))
                                && (!method.equals("reset"))) {
                            // Prepare context
                            //
                            context = testPlugin.prepare();
                            prepareTransaction(testPlugin, context);
                            close(context);
                            startTransaction(testPlugin);
                        }

                        if (iteratorNumber != null) {
                            number = Integer.parseInt(iteratorNumber);

                            Map<Integer, TestObject> mapTest = new HashMap<Integer, TestObject>();
                            toBeInsert = "";
                            String currentTest = (String) session.getAttribute("currentTest");
                            for (int i = 0; i < number; i++) {
                                TestObject testObject = new TestGVBufferObject();
                                testObject.setTestManager(testManager);
                                testObject.setMethod(method);

                                testPlugin.prepareInput(request, testObject, "singleTest", number);

                                // Inizio gestione save dati di input
                                //
                                String saveData = request.getParameter("saveData");
                                if (saveData != null) {
                                    if (saveData.equals("on")) {
                                        wrapper.setSaveData(true);

                                        // E' stato richiesto il save dei dati
                                        // di input. Ma non per tutti i submit
                                        // button
                                        // e' significativo salvare i dati
                                        // inseriti quindi controllo il metodo.
                                        //
                                        if (!method.equals("generateID") && (!method.equals("clear"))
                                                && (!method.equals("reset"))) {
                                            testPlugin.saveData(fileNameI);
                                        }
                                    }
                                    else {
                                        wrapper.setSaveData(false);
                                    }
                                }
                                else {
                                    wrapper.setSaveData(false);
                                }

                                // Invoke the Method requested
                                //
                                executeTest(testObject, wrapper, testManager, currentTest, i);
                                setCount(wrapper);
                                mapTest.put(i, testObject);
                            } // END FOR

                            session.setAttribute("mapTestObject", mapTest);

                            // Scrivo l'output del test in un file (sempre)
                            //
                            manageFileWriter(request, testManager, wrapper);
                            wrapper.writeFile(writer, (String) testManager.get("fileNameOutput"), toBeInsert);
                            if (writer != null) {
                                writer.close();
                            }
                        } // end if iterator number
                    }
                    else {
                        // Method = savedData
                        //
                        testPlugin.savedData(fileNameI);
                    }

                    // The standard method clear or reset
                    //
                    manageStandardAction(method, testManager);

                } // end method != null
            }
            catch (Throwable exc) {
                exc.printStackTrace();
                wrapper.setThrowable(exc);
            }
            finally {

                endTransaction();

                try {
                    // Close context
                    //
                    close(context);
                }
                catch (Throwable thr) {
                    thr.printStackTrace();
                }
            }
        }
        catch (Throwable thr) {
            thr.printStackTrace();
        }
        finally {
            GVIdentityHelper.pop();
        }
    }

    /**
     * This method set the count for the test executed
     * 
     * @param wrapper
     *        The TestPluginWrapper object
     */
    private void setCount(TestPluginWrapper wrapper)
    {
        Throwable throwable = wrapper.getThrowable();
        if (throwable != null) {
            countKo++;
        }
        else {
            countOk++;
        }
    }

    /**
     * This method manage the FileWriter for the output
     * 
     * @param request
     *        The HttpServletRequest object
     * @param wrapper
     *        The TestPluginWrapper object
     */
    private void manageFileWriter(HttpServletRequest request, TestManager testM, TestPluginWrapper wrapper)
            throws Throwable
    {
        // File inserito dall'utente per questa request
        //
        String fileNameParam = request.getParameter("fileName");

        // File precedentemente inserito dall'utente
        //
        // String fileName = (String)testM.get("fileNameOutput");

        String append = request.getParameter("append");
        if (append != null) {
            if (append.equals("on")) {
                wrapper.setAppend(true);
            }
            else {
                wrapper.setAppend(false);
            }
        }
        else {
            wrapper.setAppend(false);
        }

        writer = wrapper.createFile(fileNameParam);
        wrapper.setFile(writer);
        testM.set("fileNameOutput", fileNameParam);
    }

    /**
     * This method creates the string to be insert in the output file.
     * 
     * @param currentTest
     *        The test requested
     * @param object
     *        The String to be insert in the file
     * @param i
     *        The index rappresenting the current test
     */
    public void createString(String currentTest, String object, int i)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(DATA_PATTERN);
        Date date = new Date();
        String head = "----------- TEST N. " + i + " " + sdf.format(date) + " -----------";

        toBeInsert = toBeInsert + "\n" + head;

        toBeInsert = "<pre>" + toBeInsert + object + "</pre>";
    }

    /**
     * This method prepare the transaction
     * 
     * @param testPlugin
     *        The TestPlugin object
     * @param initialContext
     *        The InitialContext object
     * @throws Throwable
     *         If an error occurred
     */
    public void prepareTransaction(TestPlugin testPlugin, InitialContext initialContext) throws Throwable
    {
        if (transaction != null) {
            if (transaction.equals("Commit") || transaction.equals("Rollback")) {

                if (initialContext != null) {
                    userTransaction = (UserTransaction) initialContext.lookup("javax.transaction.UserTransaction");
                }
            }
            else {
                transaction = null;
            }
        }
    }

    /**
     * Start the transaction <br>
     * 
     * @param testPlugin
     *        TestPlugin object
     * @throws Throwable
     *         If an error occurred
     */
    public void startTransaction(TestPlugin testPlugin) throws Throwable
    {
        if (transaction != null) {
            userTransaction.begin();
        }
    }

    /**
     * End the transaction in commit mode or in rollback mode depending on
     * request <br>
     * 
     * @throws Throwable
     *         If an error occurred
     */
    public void endTransaction() throws Throwable
    {
        if ((userTransaction != null) && (transaction != null)) {
            if (transaction.equals("Commit")) {
                userTransaction.commit();
            }
            else {
                userTransaction.rollback();
            }
        }
    }

    /**
     * Close the context
     * 
     * @param context
     *        InitialContext object
     * @throws Throwable
     *         If an error occurred
     */
    public void close(InitialContext context) throws Throwable
    {

        if (context != null) {
            context.close();
        }
    }

    /**
     * Execute standard action for each test.
     * 
     * @param method
     *        Name of the method to invoke
     * @param testManager
     *        Utility class to get the method required and invoke it
     * @throws Throwable
     *         If an error occurred
     */
    private void manageStandardAction(String method, TestManager testManager) throws Throwable
    {

        if (method.equals("clear")) {
            clear(testManager);
        }
        else {
            if (method.equals("reset")) {
                reset(testManager);
            }
        }
    }

    /**
     * Clear function the wrapper class contains the exception and the
     * showsresult in this case set to null (clear)
     * 
     * @param testManager
     *        Utility class to get the Wrapper object
     * @throws Throwable
     *         If an error occurred
     */
    private void clear(TestManager testManager) throws Throwable
    {
        TestPluginWrapper wrapper = testManager.getWrapper();
        wrapper.setShowsResult(false);
        wrapper.setThrowable(null);
    }

    /**
     * Reset function to reset the Plugin object to null in the session
     * 
     * @param testManager
     *        Utility class where reset method is implemented
     * @throws Throwable
     *         If an error occurred
     */
    private void reset(TestManager testManager) throws Throwable
    {
        testManager.reset();
    }
}