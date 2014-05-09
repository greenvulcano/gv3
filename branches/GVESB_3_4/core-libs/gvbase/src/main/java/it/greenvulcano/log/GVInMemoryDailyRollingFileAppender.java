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

import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.util.thread.BaseThread;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.apache.log4j.Layout;
import org.apache.log4j.MDC;
import org.apache.log4j.spi.LoggingEvent;

/**
 * GVInMemoryDailyRollingFileAppender extends {@link GVDailyRollingFileAppender}
 * so that the underlying file is rolled over at a user chosen frequency.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GVInMemoryDailyRollingFileAppender extends GVDailyRollingFileAppender implements ShutdownEventListener
{

    private boolean mustStop = false;

    private class QueueHandler implements Runnable
    {
        public void run()
        {
            while (!mustStop) {
                MessageWrapper mw = null;
                do {
                    try {
                        if (mw != null) {
                            LoggingEvent event = mw.getMessage();
                            Hashtable<String, Object> context = mw.getMdcContext();
                            for (Entry<String, Object> iterator : context.entrySet()) {
                                MDC.put(iterator.getKey(), iterator.getValue());
                            }
                            appendLogInternal(event);
                            counter--;
                        }
                        mw = getMessage();
                    }
                    catch (Exception exc) {
                        System.err.println("Cannot write log message: ");
                        exc.printStackTrace();
                    }
                }
                while (mw != null);
            }
        }
    }

    private LinkedList<MessageWrapper> queue    = new LinkedList<MessageWrapper>();

    private Thread                     qHandler = null;
    private QueueHandler               qh       = null;

    private int                        counter;

    /**
     * The default constructor does nothing.
     */
    public GVInMemoryDailyRollingFileAppender()
    {
        // do nothing
    }

    /**
     * Instantiate a <code>GVInMemoryDailyRollingFileAppender</code> and open
     * the file designated by <code>filename</code>. The opened filename will
     * become the output destination for this appender.
     *
     * @param layout
     * @param filename
     * @param datePattern
     * @throws Exception
     */
    public GVInMemoryDailyRollingFileAppender(Layout layout, String filename, String datePattern) throws Exception
    {
        super(layout, filename, datePattern);
    }

    /**
     * This method differentiates GVInMemoryDailyRollingFileAppender from its
     * super class.
     *
     * @param message
     */
    @SuppressWarnings("unchecked")
    protected void subAppend(LoggingEvent message)
    {
        if (qHandler == null) {
            createQHandler();
        }

        // To trigger the conversion of the Object
        // message to a String message BEFORE
        // enqueueing
        message.getRenderedMessage();

        MessageWrapper mw = new MessageWrapper();
        mw.setMdcContext(MDC.getContext() != null
                ? new Hashtable<String, Object>(MDC.getContext())
                : new Hashtable<String, Object>());
        mw.setMessage(message);
        enqueue(mw);
    }

    /**
     * @param message
     */
    protected void appendLogInternal(LoggingEvent message)
    {
        super.subAppend(message);
    }

    /**
     * Enqueues a message in an in-memory queue to be dequeued from internal
     * <code>QueueHandler</code> class.
     *
     * @param message
     *        the message to log
     */
    private void enqueue(MessageWrapper message)
    {
        counter++;
        synchronized (queue) {
            queue.addLast(message);
            queue.notifyAll();
        }
    }

    /**
     * This method is used from internal QueueHandler class.
     *
     * @return a LoggingEvent recovered from the in-memory queue.
     */
    private MessageWrapper getMessage()
    {
        synchronized (queue) {
            if (queue.isEmpty()) {
                try {
                    queue.wait(1000);
                }
                catch (InterruptedException exc) {
                    exc.printStackTrace();
                }
            }
        }
        MessageWrapper obj = null;
        synchronized (queue) {
            if (!queue.isEmpty()) {
                obj = queue.removeFirst();
            }
        }
        return obj;
    }

    private synchronized void createQHandler()
    {
        if (qHandler == null) {
            qh = new QueueHandler();
            qHandler = new BaseThread(qh, "GVInMemoryDailyRollingFileAppender");
            qHandler.setDaemon(true);
            qHandler.start();
        }
    }

    /**
     * Called during shutdown event dispatching.
     *
     * @param event
     *        the dispatched event
     * @see it.greenvulcano.event.util.shutdown.ShutdownEventListener#shutdownStarted(it.greenvulcano.event.util.shutdown.ShutdownEvent)
     */
    public void shutdownStarted(ShutdownEvent event)
    {
        close();
    }

    /**
     * @see org.apache.log4j.WriterAppender#close()
     */
    public void close()
    {
        mustStop = true;

        MessageWrapper event = null;
        do {
            if (event != null) {
                appendLogInternal(event.getMessage());
                counter--;
            }
            synchronized (queue) {
                if (!queue.isEmpty()) {
                    event = queue.removeFirst();
                }
                else {
                    event = null;
                }
            }
        }
        while (event != null);

        if (counter > 0) {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException exc) {
                // do nothing
            }
        }
        super.close();
    }
}
