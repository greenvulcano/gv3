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
package it.greenvulcano.gvesb.gvconsole.stats;

/**
 * Standard Java imports.
 */
import it.greenvulcano.gvesb.throughput.ThroughputData;
import it.greenvulcano.jmx.JMXUtils;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <code>StatsServlet</code> read the query string from the applet code.
 * <p>
 * The query string value is useful to
 * <li>register/update/deregister the applet in an HashMap object to manage the
 * TimerTask.
 * <p>
 * <li>Get the JMX throughput value
 *
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class StatsServlet extends HttpServlet
{
    private static final long                  serialVersionUID = 300L;

    /**
     * The updateTimer is the Timer class scheduled by the TimerTask that get
     * Throughput value
     */
    private static Timer                       updateTimer;

    /**
     * The checkTimer is the Timer class scheduled by the TimerTask that manage
     * the servlet timeout
     */
    private static Timer                       checkTimer;

    /**
     * The HashMap to register the applet and time
     */
    private static Map<String, Long>           hashMap          = new HashMap<String, Long>();

    /**
     * The ObjectName filter
     */
    private static String                      filter           = "GreenVulcano:Component=JMXServiceManager,Internal=No,*";

    /**
     * The command requested : register/update/de-register
     */
    private String                             command          = "";

    /**
     * The key for the hash map represented by a Random value.
     */
    private String                             ind              = "";

    /**
     * The location required to create the relative throughput applet
     */
    private String                             location         = "";

    /**
     * The hashMap containing the ThroughputData object
     */
    private static Map<String, ThroughputData> throughputMap    = null;

    /**
     * This method do : </br> <li>Get the parameters from the query string.</li>
     * <li>Get if necessary the JMX throughput value</li> <li>Create the
     * ThroughputData object</li>
     *
     * @param request
     *        HttpServletRequest object
     * @param response
     *        HttpServletResponse object
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        getParameters(request);
        if ((command != null) && (ind != null)) {

            // Insert,Update or remove the object from the HashMap
            //
            manageTimer();
        }

        try {
            // Write throughputData on the output object stream.
            //
            OutputStream os = response.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            if (throughputMap == null) {
                getThroughput();
            }
            ThroughputData throughputData = throughputMap.get(location);
            if (throughputData == null) {
                throughputData = new ThroughputData();
            }
            oos.writeObject(throughputData);
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Read the query string parameters.
     *
     * @param request
     *        The HttpServletRequest object
     */
    private void getParameters(HttpServletRequest request)
    {
        command = request.getParameter("command");
        ind = request.getParameter("ind");
        location = request.getParameter("location");
        if (location == null) {
            location = "GLOBAL";
        }
    }

    /**
     * Insert/Update/Remove the object from the HashMap
     */
    private void manageTimer()
    {

        if (command.equals("register")) {
            hashMap.put(ind, System.currentTimeMillis());
            initTimer();
        }
        else if (command.equals("update")) {
            hashMap.put(ind, System.currentTimeMillis());
            initTimer();
        }
        else if (command.equals("deregister")) {
            hashMap.remove(ind);
            if (hashMap.isEmpty()) {
                resetTimer();
            }
        }
    }

    /**
     * Cancel the Timer thread
     */
    private static void resetTimer()
    {

        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
        if (checkTimer != null) {
            checkTimer.cancel();
            checkTimer = null;
        }
    }

    /**
     * Initialize the Timer object
     */
    private void initTimer()
    {

        if (updateTimer == null) {
            updateTimer = new Timer(true);
            updateTimer.scheduleAtFixedRate(new UpdateValue(), 0, 1000);
        }
        if (checkTimer == null) {
            checkTimer = new Timer(true);
            checkTimer.scheduleAtFixedRate(new CheckIndex(), 0, 30000);
        }
    }

    /**
     * Check/Remove the object from the HashMap
     */
    public static void checkTime()
    {

        Iterator<String> i = hashMap.keySet().iterator();
        while (i.hasNext()) {
            String index = i.next();
            long time = hashMap.get(index);
            if ((time + 30000) < System.currentTimeMillis()) {
                hashMap.remove(index);
            }
        }
        if (hashMap.isEmpty()) {
            resetTimer();
        }
    }

    /**
     * Get the Throughput value from JMX server only for the required fields.
     */
    @SuppressWarnings("unchecked")
    public static void getThroughput()
    {
        try {
            Object[] array = JMXUtils.get(filter, "throughputData", true, null);
            throughputMap = (Map) array[0];
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Destroys the Timer task
     */
    @Override
    public void destroy()
    {
        super.destroy();
        resetTimer();
    }

}