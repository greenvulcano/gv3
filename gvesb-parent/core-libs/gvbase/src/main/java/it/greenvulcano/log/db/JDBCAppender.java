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
package it.greenvulcano.log.db;

import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.util.clazz.BigQueueObject;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

/**
 * <p>
 * The JDBCAppender writes messages into a database via JDBC. Multiple
 * configuration options and parameters are supported.
 * </p>
 * 
 * <p>
 * The JDBCAppender is configurable at runtime by setting options in a
 * configuration-file.
 * </p>
 * 
 * 
 * <p>
 * <b>Configuration parameters </b>
 * </p>
 * 
 * <p>
 * Generally there are just a few basic things to get the JDBCAppender running :
 * </p>
 * 
 * <p>
 * We need a database connection :specify the URL, password and username. This
 * will create a static connection by JDBCWriter.
 * </p>
 * 
 * <p>
 * We need to know the statement which should be inserted into the database.
 * There are also 2 ways to to this :
 * </p>
 * 
 * <p>
 * a) Specify the table and describe the important columns. Not nullable columns
 * are mandatory to describe! The sql PreparedStatement will be created
 * automatically. We need to know exactly the column-name, the logical
 * columns-logtype and in dependency of that the value. The constants of
 * ColumnType are described below.
 * </p>
 * 
 * <p>
 * b) Same as a) but using Stored Procedure (CallableStatement). Specify
 * procedure name parameter instead of table name.
 * </p>
 * 
 * <p> The class ColumnType provides you several possibilities to describe a
 * columns logtype/wildcard : </p>
 * 
 * <p>
 * <ul>
 * <li>MSG - The column will get the non-layouted log-message. No explicit value
 * necessary.</li>
 * <li>MSG_SIZE - The column will get the log-message length. No explicit value
 * necessary.</li>
 * <li>AUTO_INC - The column gets a number, which begins with 1 and will be
 * incremented with every log-message.</li>
 * <li>SEQUENCE - The column gets the result of a sequence call. Value is
 * sequence call string</li>
 * <li>STATIC - The column always gets this value.</li>
 * <li>THROWABLE - The column gets the throwable/exception information if
 * available.</li>
 * <li>TIMESTAMP - The column gets the leog event timestamp.</li>
 * <li>PRIO - The column gets the priority/level of the log-message.</li>
 * <li>IPRIO - The column gets the integer value of the priority/level of the
 * log-message.</li>
 * <li>CAT - The column gets the categorys name.</li>
 * <li>THREAD - The column gets the threads name.</li>
 * <li>NDC - The column gets the NDC (nested diagnostic context).</li>
 * <li>MDC:key - The column gets the MDC (mapped diagnostic context) for the
 * given key.</li>
 * <li>EMPTY - The column will be ignored.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <pre>
 *   &lt;appender class="it.greenvulcano.log.db.JDBCAppender"
 *             name="DB_LOGGER_FOR_GVCORE">
 *       &lt;param name="qstorage" value="sp{{gv.app.home}}/log/storage"/>
 *       &lt;param name="qthreads" value="10"/>
 *       &lt;param name="url" value="jdbc:hsqldb:hsql://localhost:9001/gvesb"/>
 *       &lt;param name="dbclass" value="org.hsqldb.jdbcDriver"/>
 *       &lt;param name="username" value="gv_log"/>
 *       &lt;param name="password" value="gv_log"/>
 *       &lt;param name="table" value="log_core"/>
 *       &lt;param name="column" value="id_msg#SEQUENCE#log_seq.nextval"/>
 *       &lt;param name="column" value="tstamp#TIMESTAMP"/>
 *       &lt;param name="column" value="prio#PRIO"/>
 *       &lt;param name="column" value="iprio#IPRIO"/>
 *       &lt;param name="column" value="cat#CAT"/>
 *       &lt;param name="column" value="thread#THREAD"/>
 *       &lt;param name="column" value="server#MDC#SERVER"/>
 *       &lt;param name="column" value="id#MDC#ID"/>
 *       &lt;param name="column" value="system_n#MDC#SYSTEM"/>
 *       &lt;param name="column" value="service#MDC#SERVICE"/>
 *       &lt;param name="column" value="operation_n#MDC#OPERATION"/>
 *       &lt;param name="column" value="msg_size#MSG_SIZE"/>
 *       &lt;param name="column" value="msg#MSG"/>
 *       &lt;param name="column" value="throwable#THROWABLE"/>
 *   &lt;/appender>
 * </pre>
 * </p>
 * 
 * @version 3.1.0 24/gen/2011
 * @author GreenVulcano Developer Team
 */
public class JDBCAppender extends AppenderSkeleton implements ShutdownEventListener
{

    /**
     * Stores a database url
     */
    private String                   url        = null;

    /**
     * Stores the database user
     */
    private String                   username   = null;

    /**
     * Stores the database password
     */
    private String                   password   = null;

    /**
     * Stores the table in which the logging will be done
     */
    private String                   table      = null;

    /**
     * Stores the procedure which will be called
     */
    private String                   procedure  = null;

    /**
     * Defines the class load string necessary to access the database driver
     */
    private String                   dbclass    = null;

    /**
     * Defines how many messages will be buffered until they will be updated to
     * the database
     */
    private int                      bufferSize = 1;

    /**
     * Stores message-events. When the buffer_size is reached, the buffer will
     * be flushed and the messages will inserted to the database.
     */
    private List<LoggingEvent>       buffer     = new ArrayList<LoggingEvent>();

    /**
     * Stores the columns, which will be used in the statement
     */
    private List<Column>             columns    = new ArrayList<Column>();

    /**
     * The Queue storage folder name
     */
    private String                   queueStorage = null;
    
    /**
     * This class encapsulate the logic necessary to log into a table
     */
    private JDBCWriter               logWriter  = new JDBCWriter();

    /**
     * A flag to indicate configuration status
     */
    private boolean                  configured = false;

    /**
     * A flag to indicate that everything is ready to execute append()-commands
     */
    private boolean                  isReady    = false;


    private BigQueueObject<LoggingEvent> queue  = null;

    private Set<Thread>              qHandler   = new HashSet<Thread>();
    
    private int                      qHandlerThreadNum = 5;

    private boolean                  mustStop   = false;
    
    private boolean                  logState   = false;

    // private static boolean shutdownInProgress = false;

    private class QueueHandler implements Runnable
    {
        private JDBCWriter logWriterLoc = null;
        
        /**
         * 
         */
        public QueueHandler(JDBCWriter logWriter)
        {
            logWriterLoc = logWriter.getCopy();
        }
        
        public void run()
        {
            int nullCount = 0;
            int msgCount = 0;
            Thread thr = Thread.currentThread();
            String thrN = thr.getName() + "_" + System.currentTimeMillis();

            try {
                logState("JDBCAppender[" + thrN + "]::QueueHandler Start - queue size : " + queue.size() + " - " + new Date());

                logWriterLoc.prepareConnection();
                // while (!mustStop && !shutdownInProgress) {
                while (!mustStop && (nullCount < 6)) {
                    // while (true) {
                    LoggingEvent event = null;
                    do {
                        if (event != null) {
                            nullCount = 0;
                            msgCount++;
                            try {
                                logWriterLoc.append(event, layout);
                                // logWriter.appendConfirm();
                            }
                            catch (Exception exc) {
                                logState("JDBCAppender[" + thrN + "]::run(), : " + logWriterLoc.getErrorMsg());
                                String errorMsg = "JDBCAppender[" + thrN + "]::run(), : " + logWriterLoc.getErrorMsg();
                                LogLog.error(errorMsg, exc);
                                errorHandler.error(errorMsg, exc, 0);
                            }
                        }
                        else {
                            try {
                                Thread.sleep(10000);
                            }
                            catch (InterruptedException exc) {
                                // do nothing
                            }
                        }
                        event = dequeue();
                    }
                    while ((event != null) && !mustStop);

                    nullCount++;
                }
            }
            catch (Exception exc) {
                String errorMsg = "JDBCAppender[" + thrN + "]::run(), : " + logWriterLoc.getErrorMsg();
                LogLog.error(errorMsg, exc);
                errorHandler.error(errorMsg, exc, 0);
            }
            finally {
                try {
                    logWriterLoc.freeConnection();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }

            logState("JDBCAppender[" + thrN + "]::QueueHandler End (" + msgCount + ") - queue size : " + queue.size() + " - " + new Date());

            qHandler.remove(thr);
        }
    }

    /*
     * private void enqueue(List<LoggingEvent> events) { synchronized (queue) {
     * queue.addAll(events); queue.notifyAll(); } }
     */

    private void enqueue(LoggingEvent event) throws IOException
    {
        queue.writeObject(event);
        //queue.flush();
    }

    private LoggingEvent dequeue() throws IOException
    {
        LoggingEvent event = queue.readObject();
        return event;
    }

    private synchronized void createQHandler()
    {
        int qhs = qHandler.size();
        if ((qhs == 0) || ((qhs < Math.round(queue.size() / 20)) && (qhs < qHandlerThreadNum))) {
            Thread qHThr = new Thread(new QueueHandler(logWriter), getName() + "_" + (qhs+1));
            qHThr.setDaemon(false);
            qHThr.start();
            qHandler.add(qHThr);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.greenvulcano.event.util.shutdown.ShutdownEventListener#shutdownStarted
     * (it.greenvulcano.event.util.shutdown.ShutdownEvent)
     */
    @Override
    public void shutdownStarted(ShutdownEvent event)
    {
        close();
        try {
            Thread.sleep(5000);
        }
        catch (InterruptedException exc) {
            // do nothing
        }
    }

    private void logState(String msg) {
        if (logState) {
            System.out.println(msg);
        }
    }
    
    /**
     * Constructor for the JDBCAppender object
     */
    public JDBCAppender()
    {
        super();
        ShutdownEventLauncher.addEventListener(this);
        logState = Boolean.getBoolean("it.greenvulcano.log.db.JDBCAppender.logState");
        logState("JDBCAppender created");
    }

    /**
     * Constructor for the JDBCAppender object
     * 
     * @param layout
     *        Allows you to set your Layout-instance
     */
    public JDBCAppender(Layout layout)
    {
        super();
        logState = Boolean.getBoolean("it.greenvulcano.log.db.JDBCAppender.logState");
        this.setLayout(layout);
        ShutdownEventLauncher.addEventListener(this);
        logState("JDBCAppender created");
    }

    /**
     * Sets the Layout attribute of the JDBCAppender object
     * 
     * @param layout
     *        The new Layout value
     */
    public void setLayout(Layout layout)
    {
        super.setLayout(layout);
    }

    /**
     * Sets database url of the form jdbc:subprotocol:subname
     * 
     * @param value
     *        The new Url value
     */
    public void setUrl(String value)
    {
        if (value == null) {
            return;
        }

        value = value.trim();

        if (value.length() == 0) {
            return;
        }

        url = value;
    }

    /**
     * Sets the database user
     * 
     * @param value
     *        The new Username value
     */
    public void setUsername(String value)
    {
        if (value == null) {
            return;
        }

        value = value.trim();

        if (value.length() == 0) {
            return;
        }

        username = value;
    }

    /**
     * Sets the database password
     * 
     * @param value
     *        The new Password value
     */
    public void setPassword(String value)
    {
        if (value == null) {
            return;
        }

        value = value.trim();

        password = value;
    }

    /**
     * Specify a database class loader string. E.g.
     * oracle.jdbc.driver.OracleDriver
     * 
     * @param value
     *        The new database driver class
     */
    public void setDbclass(String value)
    {
        if (value == null) {
            return;
        }

        value = value.trim();

        if (value.length() == 0) {
            return;
        }

        dbclass = value;
    }


    /**
     * Specify the table, when you also describe all columns by setColumn()
     * 
     * @param value
     *        The new Table value
     */
    public void setTable(String value)
    {
        if (value == null) {
            return;
        }

        value = value.trim();

        if (value.length() == 0) {
            return;
        }

        table = value;
    }

    /**
     * Defines one colum in the format column#logtype#value#typeName#sqlType,
     * typeName and sqlType are required, if JDBC driver does not support
     * Statement.getParamaterMetaData, e.g. Oracle.
     * 
     * @param value
     *        Concatenated string column#logtype#value#typeName
     */
    public void setColumn(String value)
    {
        if (table == null && procedure == null) {
            String errorMsg = "JDBCAppender[" + getName() + "]::setColumn(), table or procedure has to be set before!";
            LogLog.error(errorMsg);
            errorHandler.error(errorMsg, null, 0);
            return;
        }

        if (value == null) {
            return;
        }

        value = value.trim();

        if (value.length() == 0) {
            return;
        }

        String name = null;
        ColumnType logtype = ColumnType.EMPTY;
        String typeName = null;
        int sqlType = -1;
        String arg = null;
        int numArgs = 0;
        StringTokenizer stArg;

        // Arguments are #-separated
        stArg = new StringTokenizer(value, "#");

        numArgs = stArg.countTokens();

        if (numArgs < 2 || numArgs > 5) {
            String errorMsg = "JDBCAppender[" + getName() + "]::setColumn(), Invalid column-option value : " + value + " !";
            LogLog.error(errorMsg);
            errorHandler.error(errorMsg, null, 0);
            return;
        }

        for (int j = 1; j <= numArgs; j++) {
            arg = stArg.nextToken();

            if (j == 1) {
                name = arg;
            }
            else if (j == 2) {
                try {
                    logtype = ColumnType.valueOf(arg);
                }
                catch (Exception e) {
                    String errorMsg = "JDBCAppender[" + getName() + "]::setColumn(), Invalid column-option JDBCLogType : " + arg + " !";
                    LogLog.error(errorMsg);
                    errorHandler.error(errorMsg, null, 0);
                    return;
                }
            }
            else if (j == 3) {
                value = arg;
            }
            else if (j == 4) {
                typeName = arg;
            }
            else if (j == 5) {
                try {
                    sqlType = Integer.parseInt(arg);
                }
                catch (NumberFormatException nfe) {
                    String errorMsg = "JDBCAppender[" + getName() + "]::setColumn(), Invalid column-option sqlType : " + arg + " !";
                    LogLog.error(errorMsg);
                    errorHandler.error(errorMsg, null, 0);
                    return;
                }
            }
        }

        if (numArgs == 2) {
            value = null;
        }

        try {
            columns.add(new Column(name, logtype, value));
        }
        catch (Exception e) {
            String errorMsg = "JDBCAppender[" + getName() + "]::setColumn(), Invalid column definition : " + value + " !";
            LogLog.error(errorMsg);
            errorHandler.error(errorMsg, e, 0);
            return;
        }
    }

    /**
     * Defines how many messages will be buffered until they will be updated to
     * the database.
     * 
     * @param value
     *        The new Buffer value
     */
    public void setBuffer(String value)
    {
        if (value == null) {
            return;
        }

        value = value.trim();

        if (value.length() == 0) {
            return;
        }

        try {
            bufferSize = Integer.parseInt(value);
        }
        catch (Exception e) {
            String errorMsg = "JDBCAppender[" + getName() + "]::setBuffer(), Invalid BUFFER option value : " + value + " !";
            LogLog.error(errorMsg);
            errorHandler.error(errorMsg, null, 0);
            return;
        }
    }

    
    /**
     * @param qHandlerThreadNum the qHandlerThreadNum to set
     */
    public void setqthreads(int qHandlerThreadNum)
    {
        this.qHandlerThreadNum = qHandlerThreadNum;
    }
    
    /**
     * @param queueStorage the QueueStorage folder name to set
     */
    public void setQstorage(String queueStorage)
    {
        this.queueStorage = queueStorage;
    }
    
    /**
     * Gets the Url attribute of the JDBCAppender object
     * 
     * @return The Url value
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Gets the Username attribute of the JDBCAppender object
     * 
     * @return The Username value
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Gets the Password attribute of the JDBCAppender object
     * 
     * @return The Password value
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Gets the class loader string
     * 
     * @return The db class loader string
     */
    public String getDbclass()
    {
        return dbclass;
    }

    /**
     * Gets the Table attribute of the JDBCAppender object
     * 
     * @return The Table value
     */
    public String getTable()
    {
        return table;
    }

    /**
     * Gets the Buffer attribute of the JDBCAppender object
     * 
     * @return The Buffer value
     */
    public String getBuffer()
    {
        return Integer.toString(bufferSize);
    }

    /**
     * @return the qHandlerThreadNum
     */
    public int getqthreads()
    {
        return this.qHandlerThreadNum;
    }
    
    /**
     * @return the QueueStorage folder name
     */
    public String getQstorage()
    {
        return this.queueStorage;
    }
    
    /**
     * If program terminates close the database-connection and flush the buffer
     */
    public void finalize()
    {
        close();
        super.finalize();
    }

    /**
     * Internal method. Returns true, you may define your own layout...
     * 
     * @return Description of the Returned Value
     */
    public boolean requiresLayout()
    {
        return true;
    }

    /**
     * Internal method. Close the database connection & flush the buffer.
     */
    public void close()
    {
        ShutdownEventLauncher.removeEventListener(this);
        logState("JDBCAppender[" + getName() + "] Closing");
        mustStop = true;
        flushBuffer();

        logState("JDBCAppender[" + getName() + "] queue size : " + queue.size() + " - " + new Date());

        try {
            logWriter.prepareConnection();
            LoggingEvent event = null;
            do {
                if (event != null) {
                    try {
                        logWriter.append(event, layout);
                        // logWriter.appendConfirm();
                    }
                    catch (Exception e) {
                        String errorMsg = "JDBCAppender[" + getName() + "]::close(), : " + logWriter.getErrorMsg();
                        LogLog.error(errorMsg, e);
                        errorHandler.error(errorMsg, e, 0);
                    }
                }
                event = queue.readObject();
            }
            while (event != null);
        }
        catch (Exception e) {
            String errorMsg = "JDBCAppender[" + getName() + "]::close(), : " + logWriter.getErrorMsg();
            LogLog.error(errorMsg, e);
            errorHandler.error(errorMsg, e, 0);
        }
        finally {
            try {
                logWriter.freeConnection();
            }
            catch (Exception exc) {
                // do nothing
            }
            try {
                if (this.queue != null) {
                    this.queue.close();
                }
            }
            catch (Exception exc) {
                // do nothign
            }
        }

        logState("JDBCAppender[" + getName() + "][" + getName() + "] Closed - queue size : " + queue.size() + " - " + new Date());

        this.closed = true;
    }

    /**
     * Internal method. Appends the message to the database table.
     * 
     * @param event
     *        Description of Parameter
     */
    public void append(LoggingEvent event)
    {
        try {
            if (!isReady) {
                if (!isReady()) {
                    logState("JDBCAppender[" + getName() + "]::append(), Not ready to append !");
                    String errorMsg = "JDBCAppender[" + getName() + "]::append(), Not ready to append !";
                    LogLog.error(errorMsg);
                    errorHandler.error(errorMsg, null, 0);
                    return;
                }
            }

            // Set the NDC and thread name for the calling thread as these
            // LoggingEvent fields were not set at event creation time.
            event.getNDC();
            event.getThreadName();
            // Get a copy of this thread's MDC.
            event.getMDCCopy();
            // make sure to also remember locationinfo in the event
            event.getLocationInformation();

            enqueue(event);
            createQHandler();
        }
        catch (Exception exc) {
            logState("JDBCAppender[" + getName() + "]::append(), Not ready to append!");
            exc.printStackTrace();
            String errorMsg = "JDBCAppender[" + getName() + "]::append() Error in append";
            LogLog.error(errorMsg, exc);
            errorHandler.error(errorMsg, exc, 0);
        }
    }

    /**
     * Internal method. Flushes the buffer.
     */
    private void flushBuffer()
    {
        int size = buffer.size();

        if (size < 1) {
            return;
        }

        logState("JDBCAppender[" + getName() + "]::flushBuffer_1 buffer : " + buffer.size());

        buffer.clear();
        logState("JDBCAppender[" + getName() + "]::flushBuffer_2 buffer : " + buffer.size());

        createQHandler();
    }

    /**
     * Internal method. Returns true, when the JDBCAppender is ready to append
     * messages to the database, else false.
     * 
     * @return Description of the Returned Value
     */
    private boolean isReady()
    {
        if (isReady) {
            return true;
        }

        if (!configured) {
            if (!configure()) {
                return false;
            }
        }

        isReady = logWriter.isReady();

        if (!isReady) {
            errorHandler.error(logWriter.getErrorMsg(), null, 0);
        }

        // Default Message-Layout
        if (layout == null) {
            layout = new PatternLayout("%m");
        }

        return isReady;
    }

    /**
     * Internal method. Configures for appending...
     * 
     * @return Description of the Returned Value
     */
    protected boolean configure()
    {
        try {
            if (configured) {
                return true;
            }

            if (qHandlerThreadNum < 1) {
                throw new Exception("JDBCAppender[" + getName() + "]::configure(), QTHREADS incorrect value.");
            }

            if (this.getDbclass() != null) {
                Class.forName(this.getDbclass());
            }

            if (url == null) {
                throw new Exception("JDBCAppender[" + getName() + "]::configure(), No URL defined.");
            }

            if (username == null) {
                throw new Exception("JDBCAppender[" + getName() + "]::configure(), No USERNAME defined.");
            }

            if (password == null) {
                throw new Exception("JDBCAppender[" + getName() + "]::configure(), No PASSWORD defined.");
            }

            // set options in logWriter
            logWriter.setConnectionParams(url, username, password);

            if (table != null) {
                logWriter.setTable(table);
            }
            else if (procedure != null) {
                logWriter.setProcedure(procedure, columns);
            }
            else {
                String errorMsg = "JDBCAppender[" + getName() + "]::configure(), No table or procedure option given !";
                LogLog.error(errorMsg);
                errorHandler.error(errorMsg, null, 0);
                return false;
            }

            logWriter.setLogColumns(columns);

            if (queueStorage == null) {
                throw new Exception("JDBCAppender[" + getName() + "]::configure(), No QSTORAGE defined.");
            }
            this.queue = new BigQueueObject<LoggingEvent>(PropertiesHandler.expand(queueStorage), getName());
        }
        catch (Exception e) {
            String errorMsg = "JDBCAppender[" + getName() + "]::configure()";
            LogLog.error(errorMsg, e);
            errorHandler.error(errorMsg, e, 0);
            return false;
        }

        configured = true;

        return true;
    }

    /**
     * Returns the procedure.
     * 
     * @return Returns the procedure.
     */
    public String getProcedure()
    {
        return procedure;
    }

    /**
     * Stores the procedure which will be called.
     * 
     * @param procedure
     *        The procedure to set.
     */
    public void setProcedure(String procedure)
    {
        this.procedure = procedure;
    }

}
