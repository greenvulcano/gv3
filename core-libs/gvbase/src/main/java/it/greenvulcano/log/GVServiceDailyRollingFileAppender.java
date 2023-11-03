/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;

/**
 *
 * @version 3.4.0 28/mag/2013
 * @author GreenVulcano Developer Team
 *
 */
public class GVServiceDailyRollingFileAppender extends AppenderSkeleton
{
    public static String          SERVICE_KEY        = "SERVICE";
    public static String          MASTER_SERVICE_KEY = "MASTER_SERVICE";

    public static String          DEFAULT_SERVICE    = "Default";

    protected boolean             immediateFlush     = true;

    protected String              encoding           = null;

    private String                datePattern        = "'.'yyyy-MM-dd";

    protected boolean             fileAppend         = true;

    protected String              fileName           = null;

    protected boolean             bufferedIO         = false;

    protected int                 bufferSize         = 8 * 1024;

    protected boolean             useMasterService   = true;

    private String                serviceKey         = MASTER_SERVICE_KEY;


    private Map<String, Appender> appenders          = new HashMap<String, Appender>();

    /**
     *
     */
    public GVServiceDailyRollingFileAppender() {
        // do nothing
    }

    /**
     * Instantiate a <code>GVServiceDailyRollingFileAppender</code>.
     *
     * @param layout
     * @param fileName
     * @param datePattern
     * @throws IOException
     * @throws PropertiesHandlerException
     */
    public GVServiceDailyRollingFileAppender(Layout layout, String fileName, String datePattern) throws IOException,
            PropertiesHandlerException {
        this.fileName = fileName;
        this.datePattern = datePattern;
        activateOptions();
    }


    @Override
    public void activateOptions() {
        if (this.useMasterService) {
            this.serviceKey = MASTER_SERVICE_KEY;
        }
        else {
            this.serviceKey = SERVICE_KEY;
        }
        if ((this.datePattern != null) && (this.fileName != null)) {
            try {
                this.appenders.put(DEFAULT_SERVICE, buildAppender(DEFAULT_SERVICE));
            }
            catch (Exception exc) {
                LogLog.error("Error initializing appender [" + this.name + "].", exc);
            }
        }
        else {
            LogLog.error("Either File or DatePattern options are not set for appender [" + this.name + "].");
        }
    }


    @Override
    protected void append(LoggingEvent event) {
        Object s = event.getMDC(this.serviceKey);
        String service = (s == null) ? DEFAULT_SERVICE : s.toString();

        Appender app = this.appenders.get(service);
        if (app == null) {
            app = buildAppender(service);
            this.appenders.put(service, app);
        }

        app.doAppend(event);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.log4j.Appender#close()
     */
    @Override
    public void close() {
        for (Iterator<String> it = this.appenders.keySet().iterator(); it.hasNext();) {
            Appender app = this.appenders.get(it.next());
            app.close();
        }
    }

    /**
     * The <b>DatePattern</b> takes a string in the same format as expected by
     * {@link SimpleDateFormat}. This options determines the rollover schedule.
     *
     * @param pattern
     */
    public void setDatePattern(String pattern) {
        this.datePattern = pattern;
    }

    /**
     * Returns the value of the <b>DatePattern</b> option.
     *
     * @return the value of the <b>DatePattern</b> option.
     */
    public String getDatePattern() {
        return this.datePattern;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public void setEncoding(String value) {
        this.encoding = value;
    }

    public void setImmediateFlush(boolean value) {
        this.immediateFlush = value;
    }

    public boolean getImmediateFlush() {
        return this.immediateFlush;
    }

    public void setFile(String file) {
        String val = file.trim();
        this.fileName = val;
    }

    public boolean getAppend() {
        return this.fileAppend;
    }

    public String getFile() {
        return this.fileName;
    }

    public boolean getBufferedIO() {
        return this.bufferedIO;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public void setAppend(boolean flag) {
        this.fileAppend = flag;
    }

    public void setBufferedIO(boolean bufferedIO) {
        this.bufferedIO = bufferedIO;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public boolean getUseMasterService() {
        return this.useMasterService;
    }

    public void setUseMasterService(boolean useMasterService) {
        this.useMasterService = useMasterService;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.log4j.Appender#requiresLayout()
     */
    @Override
    public boolean requiresLayout() {
        return true;
    }

    /**
     * encode a String replacing the "#" with Service name<br>
     *
     * @param string
     *        the string to encode
     * @return string the encoded string
     */
    private String encode(String service) {
        StringBuilder output = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(this.fileName, "#", true);
        boolean found = false;

        while (tokenizer.hasMoreTokens()) {
            String tk = tokenizer.nextToken();
            if (tk.equals("#")) {
                found = true;
                output.append(service);
            }
            else {
                output.append(tk);
            }
        }

        if (!found) {
            output.append(service);
        }

        try {
            return PropertiesHandler.expand(output.toString());
        }
        catch (Exception exc) {
            exc.printStackTrace();
            return output.toString();
        }
    }

    /**
     * @return
     */
    private Appender buildAppender(String service) {
        GVDailyRollingFileAppender app = new GVDailyRollingFileAppender();
        app.setName(service);
        app.setLayout(this.layout);
        app.setEncoding(this.encoding);
        app.setFileName(encode(service));
        app.setDatePattern(this.datePattern);
        app.setAppend(this.fileAppend);
        app.setBufferedIO(this.bufferedIO);
        app.setBufferSize(this.bufferSize);
        app.setImmediateFlush(this.immediateFlush);
        app.activateOptions();
        return app;
    }

}
