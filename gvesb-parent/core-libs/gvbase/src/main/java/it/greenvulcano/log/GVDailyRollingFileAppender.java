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
package it.greenvulcano.log;

import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

/**
 * GVDailyRollingFileAppender extends {@link FileAppender} so that the
 * underlying file is rolled over at a user chosen frequency.
 *
 * <p>
 * The rolling schedule is specified by the <b>DatePattern</b> option. This
 * pattern should follow the {@link SimpleDateFormat} conventions. In
 * particular, you <em>must</em> escape literal text within a pair of single
 * quotes. A formatted version of the date pattern is used as the suffix for the
 * rolled file name.
 *
 * <p>
 * For example, if the <b>File</b> option is set to <code>/foo/bar.log</code>
 * and the <b>DatePattern</b> set to <code>'.'yyyy-MM-dd</code>, on
 * 2009-05-21 at midnight, the logging file <code>/foo/bar.log</code> will be
 * copied to <code>/foo/bar.log.2009-05-21</code> and logging for 2009-05-22
 * will continue in <code>/foo/bar.log</code> until it rolls over the next
 * day.
 *
 * <p>
 * Is is possible to specify monthly, weekly, half-daily, daily, hourly, or
 * minutely rollover schedules.
 *
 * <p>
 * <table border="1" cellpadding="2">
 * <tr>
 * <th>DatePattern</th>
 * <th>Rollover schedule</th>
 * <th>Example</th>
 *
 * <tr>
 * <td><code>'.'yyyy-MM</code>
 * <td>Rollover at the beginning of each month</td>
 *
 * <td>At midnight of May 31st, 2009 <code>/foo/bar.log</code> will be copied
 * to <code>/foo/bar.log.2009-05</code>. Logging for the month of June will
 * be output to <code>/foo/bar.log</code> until it is also rolled over the
 * next month.
 *
 * <tr>
 * <td><code>'.'yyyy-ww</code>
 *
 * <td>Rollover at the first day of each week. The first day of the week
 * depends on the locale.</td>
 *
 * <td>Assuming the first day of the week is Sunday, on Saturday midnight, June
 * 9th 2009, the file <i>/foo/bar.log</i> will be copied to
 * <i>/foo/bar.log.2009-23</i>. Logging for the 24th week of 2009 will be
 * output to <code>/foo/bar.log</code> until it is rolled over the next week.
 *
 * <tr>
 * <td><code>'.'yyyy-MM-dd</code>
 *
 * <td>Rollover at midnight each day.</td>
 *
 * <td>At midnight, on March 8th, 2009, <code>/foo/bar.log</code> will be
 * copied to <code>/foo/bar.log.2009-03-08</code>. Logging for the 9th day of
 * March will be output to <code>/foo/bar.log</code> until it is rolled over
 * the next day.
 *
 * <tr>
 * <td><code>'.'yyyy-MM-dd-a</code>
 *
 * <td>Rollover at midnight and midday of each day.</td>
 *
 * <td>At noon, on March 9th, 2009, <code>/foo/bar.log</code> will be copied
 * to <code>/foo/bar.log.2009-03-09-AM</code>. Logging for the afternoon of
 * the 9th will be output to <code>/foo/bar.log</code> until it is rolled over
 * at midnight.
 *
 * <tr>
 * <td><code>'.'yyyy-MM-dd-HH</code>
 *
 * <td>Rollover at the top of every hour.</td>
 *
 * <td>At approximately 11:00.000 o'clock on March 9th, 2009,
 * <code>/foo/bar.log</code> will be copied to
 * <code>/foo/bar.log.2009-03-09-10</code>. Logging for the 11th hour of the
 * 9th of March will be output to <code>/foo/bar.log</code> until it is rolled
 * over at the beginning of the next hour.
 *
 * <tr>
 * <td><code>'.'yyyy-MM-dd-HH-mm</code>
 *
 * <td>Rollover at the beginning of every minute.</td>
 *
 * <td>At approximately 11:23,000, on March 9th, 2009,
 * <code>/foo/bar.log</code> will be copied to
 * <code>/foo/bar.log.2009-03-09-10-22</code>. Logging for the minute of
 * 11:23 (9th of March) will be output to <code>/foo/bar.log</code> until it
 * is rolled over the next minute.
 *
 * </table>
 *
 * <p>
 * Do not use the colon ":" character in anywhere in the <b>DatePattern</b>
 * option. The text before the colon is interpreted as the protocol specification
 * of a URL which is probably not what you want.
 *
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
 *
 *
*/
public class GVDailyRollingFileAppender extends FileAppender
{

    /**
     * The code assumes that the following constants are in a increasing
     * sequence.
     */
    static final int      TOP_OF_TROUBLE = -1;
    static final int      TOP_OF_MINUTE  = 0;
    static final int      TOP_OF_HOUR    = 1;
    static final int      HALF_DAY       = 2;
    static final int      TOP_OF_DAY     = 3;
    static final int      TOP_OF_WEEK    = 4;
    static final int      TOP_OF_MONTH   = 5;

    /**
     * File name.
     */
    private String        origFileName   = null;

    /**
     * The date pattern. By default, the pattern is set to "'.'yyyy-MM-dd"
     * meaning daily rollover.
     */
    private String        datePattern    = "'.'yyyy-MM-dd";

    /**
     * The log file will be renamed to the value of the scheduledFilename
     * variable when the next interval is entered. For example, if the rollover
     * period is one hour, the log file will be renamed to the value of
     * "scheduledFilename" at the beginning of the next hour.
     *
     * The precise time when a rollover occurs depends on logging activity.
     */
    private String        scheduledFilename;

    /**
     * The next time we estimate a rollover should occur.
     */
    private long          nextCheck      = System.currentTimeMillis() - 1;

    Date                  now            = new Date();

    SimpleDateFormat      sdf;

    RollingCalendar       rc             = new RollingCalendar();

    int                   checkPeriod    = TOP_OF_TROUBLE;

    /**
     * The gmtTimeZone is used only in computeCheckPeriod() method.
     */
    static final TimeZone gmtTimeZone    = TimeZone.getTimeZone("GMT");

    /**
     * The default constructor does nothing.
     */
    public GVDailyRollingFileAppender()
    {
        // do nothing
    }

    /**
     * Instantiate a <code>GVDailyRollingFileAppender</code> and open the file
     * designated by <code>filename</code>. The opened filename will become the
     * output destination for this appender.
     *
     * @param layout
     * @param filename
     * @param datePattern
     * @throws IOException
     * @throws PropertiesHandlerException
     */
    public GVDailyRollingFileAppender(Layout layout, String filename, String datePattern) throws IOException,
            PropertiesHandlerException
    {
        super(layout, PropertiesHandler.expand(filename), true);
        this.datePattern = datePattern;
        activateOptions();
    }

    /**
     * The <b>DatePattern</b> takes a string in the same format as expected by
     * {@link SimpleDateFormat}. This options determines the rollover schedule.
     *
     * @param pattern
     */
    public void setDatePattern(String pattern)
    {
        datePattern = pattern;
    }

    /**
     * Returns the value of the <b>DatePattern</b> option.
     *
     * @return the value of the <b>DatePattern</b> option.
     */
    public String getDatePattern()
    {
        return datePattern;
    }

    /**
     * Create the log file with the name found on the configuration file.
     */
    @Override
    public void activateOptions()
    {
        if ((datePattern != null) && (origFileName != null)) {
            now.setTime(System.currentTimeMillis());
            sdf = new SimpleDateFormat(datePattern);
            int type = computeCheckPeriod();
            printPeriodicity(type);
            rc.setType(type);

            super.setFile(completeFileName(now));
            super.activateOptions();

            File file = new File(fileName);
            scheduledFilename = fileName + sdf.format(new Date(file.lastModified()));
        }
        else {
            LogLog.error("Either File or DatePattern options are not set for appender [" + name + "].");
        }
    }

    void printPeriodicity(int type)
    {
        switch (type) {
            case TOP_OF_MINUTE :
                LogLog.debug("Appender [" + name + "] to be rolled every minute.");
                break;
            case TOP_OF_HOUR :
                LogLog.debug("Appender [" + name + "] to be rolled on top of every hour.");
                break;
            case HALF_DAY :
                LogLog.debug("Appender [" + name + "] to be rolled at midday and midnight.");
                break;
            case TOP_OF_DAY :
                LogLog.debug("Appender [" + name + "] to be rolled at midnight.");
                break;
            case TOP_OF_WEEK :
                LogLog.debug("Appender [" + name + "] to be rolled at start of week.");
                break;
            case TOP_OF_MONTH :
                LogLog.debug("Appender [" + name + "] to be rolled at start of every month.");
                break;
            default :
                LogLog.warn("Unknown periodicity for appender [" + name + "].");
        }
    }

    /**
     * This method computes the roll over period by looping over the periods,
     * starting with the shortest, and stopping when the r0 is different from
     * from r1, where r0 is the epoch formatted according the datePattern
     * (supplied by the user) and r1 is the epoch+nextMillis(i) formatted
     * according to datePattern. All date formatting is done in GMT and not
     * local format because the test logic is based on comparisons relative to
     * 1970-01-01 00:00:00 GMT (the epoch).
     */
    int computeCheckPeriod()
    {
        RollingCalendar rollingCalendar = new RollingCalendar(gmtTimeZone, Locale.ENGLISH);

        // set sate to 1970-01-01 00:00:00 GMT
        //
        Date epoch = new Date(0);

        if (datePattern != null) {
            for (int i = TOP_OF_MINUTE; i <= TOP_OF_MONTH; i++) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);

                // do all date formatting in GMT
                //
                simpleDateFormat.setTimeZone(gmtTimeZone);
                String r0 = simpleDateFormat.format(epoch);
                rollingCalendar.setType(i);
                Date next = new Date(rollingCalendar.getNextCheckMillis(epoch));

                String r1 = simpleDateFormat.format(next);

                if ((r0 != null) && (r1 != null) && !r0.equals(r1)) {
                    return i;
                }
            }
        }
        // Deliberately head for trouble...
        //
        return TOP_OF_TROUBLE;
    }

    /**
     * Rollover the current file to a new file.
     */
    void rollOver() throws IOException
    {
        // Compute filename, but only if datePattern is specified
        //
        if (datePattern == null) {
            errorHandler.error("Missing DatePattern option in rollOver().");
            return;
        }
        String datedFilename = completeFileName(now);

        // It is too early to roll over because we are still within the
        // bounds of the current interval. Rollover will occur once the
        // next interval is reached.
        //
        if (scheduledFilename.equals(datedFilename)) {
            return;
        }

        // close current file, and rename it to datedFilename
        closeFile();

        try {
            // This will also close the file. This is OK since multiple
            // close operations are safe.
            //
            this.setFile(datedFilename, fileAppend, bufferedIO, bufferSize);
        }
        catch (IOException e) {
            errorHandler.error("setFile(" + fileName + ", false) call failed.");
        }
        scheduledFilename = datedFilename;
    }

    /**
     * This method differentiates GVDailyRollingFileAppender from its super
     * class.
     *
     * <p>
     * Before actually logging, this method will check whether it is time to do
     * a rollover. If it is, it will schedule the next rollover time and then
     * rollover.
     */
    @Override
    protected void subAppend(LoggingEvent event)
    {
        long n = System.currentTimeMillis();
        if (n >= nextCheck) {
            now.setTime(n);
            nextCheck = rc.getNextCheckMillis(now);
            try {
                rollOver();
            }
            catch (IOException ioe) {
                LogLog.error("rollOver() failed.", ioe);
            }
        }
        super.subAppend(event);
    }

    /**
     * Create the log file name
     */
    @Override
    public void setFile(String file)
    {
        origFileName = file;

        try {
            origFileName = PropertiesHandler.expand(file);
        }
        catch (PropertiesHandlerException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Create the log file name
     *
     * @param file
     */
    public void setFileName(String file)
    {
        origFileName = file;

        try {
            origFileName = PropertiesHandler.expand(file);
        }
        catch (PropertiesHandlerException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Create the file name with the date pattern inserted on the configuration
     * file
     */
    String completeFileName(Date date)
    {
        return encode(date);
    }

    /**
     * encode a String replacing the "$" with dataPattern<br>
     *
     * @param string
     *        the string to encode
     * @return string the encoded string
     */
    private String encode(Date date)
    {
        StringBuilder output = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(origFileName, "$", true);
        boolean found = false;

        while (tokenizer.hasMoreTokens()) {
            String tk = tokenizer.nextToken();
            if (tk.equals("$")) {
                found = true;
                output.append(sdf.format(date));
            }
            else {
                output.append(tk);
            }
        }

        if (!found) {
            output.append(sdf.format(date));
        }

        return output.toString();
    }

    /**
     * Create the file name with the property inserted on the configuration file
     * For Example with the server name
     *
     * @param name
     * @return the translated file name
     */
    protected String _translateFileName(String name)
    {
        String beginParam = "${";
        String endParam = "}";

        String resultName = name;

        while (true) {
            int beginIndex = resultName.indexOf(beginParam);
            if (beginIndex != -1) {
                int endIndex = resultName.indexOf(endParam, beginIndex);
                if (endIndex != -1) {
                    String propName = resultName.substring(beginIndex + 2, endIndex);
                    String paramValue = System.getProperty(propName, "Property '" + propName + "' not found");
                    resultName = resultName.substring(0, beginIndex) + paramValue + resultName.substring(endIndex);
                }
                else {
                    LogLog.error("translateFileName(): Invalid property substitution required ("
                            + resultName.substring(beginIndex, resultName.length() - 1) + ")");
                    break;
                }
            }
            else {
                break;
            }
        }
        return resultName;
    }
}


/**
 * RollingCalendar is a helper class to GVDailyRollingFileAppender. Given a
 * periodicity type and the current time, it computes the start of the next
 * interval.
 */
class RollingCalendar extends GregorianCalendar
{
    private static final long serialVersionUID = -8473821605944425777L;
    int                       type             = GVDailyRollingFileAppender.TOP_OF_TROUBLE;

    RollingCalendar()
    {
        super();
    }

    RollingCalendar(TimeZone tz, Locale locale)
    {
        super(tz, locale);
    }

    void setType(int type)
    {
        this.type = type;
    }

    public long getNextCheckMillis(Date now)
    {
        return getNextCheckDate(now).getTime();
    }

    public Date getNextCheckDate(Date now)
    {
        setTime(now);

        switch (type) {
            case GVDailyRollingFileAppender.TOP_OF_MINUTE :
                this.set(Calendar.SECOND, 0);
                this.set(Calendar.MILLISECOND, 0);
                add(Calendar.MINUTE, 1);
                break;
            case GVDailyRollingFileAppender.TOP_OF_HOUR :
                this.set(Calendar.MINUTE, 0);
                this.set(Calendar.SECOND, 0);
                this.set(Calendar.MILLISECOND, 0);
                add(Calendar.HOUR_OF_DAY, 1);
                break;
            case GVDailyRollingFileAppender.HALF_DAY :
                this.set(Calendar.MINUTE, 0);
                this.set(Calendar.SECOND, 0);
                this.set(Calendar.MILLISECOND, 0);
                int hour = get(Calendar.HOUR_OF_DAY);
                if (hour < 12) {
                    this.set(Calendar.HOUR_OF_DAY, 12);
                }
                else {
                    this.set(Calendar.HOUR_OF_DAY, 0);
                    add(Calendar.DAY_OF_MONTH, 1);
                }
                break;
            case GVDailyRollingFileAppender.TOP_OF_DAY :
                this.set(Calendar.HOUR_OF_DAY, 0);
                this.set(Calendar.MINUTE, 0);
                this.set(Calendar.SECOND, 0);
                this.set(Calendar.MILLISECOND, 0);
                add(Calendar.DATE, 1);
                break;
            case GVDailyRollingFileAppender.TOP_OF_WEEK :
                this.set(Calendar.DAY_OF_WEEK, getFirstDayOfWeek());
                this.set(Calendar.HOUR_OF_DAY, 0);
                this.set(Calendar.SECOND, 0);
                this.set(Calendar.MILLISECOND, 0);
                add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case GVDailyRollingFileAppender.TOP_OF_MONTH :
                this.set(Calendar.DATE, 1);
                this.set(Calendar.HOUR_OF_DAY, 0);
                this.set(Calendar.SECOND, 0);
                this.set(Calendar.MILLISECOND, 0);
                add(Calendar.MONTH, 1);
                break;
            default :
                throw new IllegalStateException("Unknown periodicity type.");
        }

        return getTime();
    }
}
