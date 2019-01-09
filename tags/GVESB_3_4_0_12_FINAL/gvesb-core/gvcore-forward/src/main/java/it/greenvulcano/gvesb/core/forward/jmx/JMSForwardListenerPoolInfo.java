/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.core.forward.jmx;

import it.greenvulcano.gvesb.core.forward.jms.JMSForwardData;

import java.lang.ref.WeakReference;

/**
 * JMSForwardListenerPoolInfo class.
 * 
 * @version 3.2.0 18/gen/2012
 * @author GreenVulcano Developer Team
 * 
 */
public class JMSForwardListenerPoolInfo
{
    /**
     * the object JMX descriptor.
     */
    public static final String            DESCRIPTOR_NAME = "JMSForwardListenerPoolInfo";

    private WeakReference<JMSForwardData> data            = null;

    /**
     * @param data
     */
    public JMSForwardListenerPoolInfo(JMSForwardData data)
    {
        this.data = new WeakReference<JMSForwardData>(data);
    }

    /**
     * @return the initialSize
     */
    public int getInitialSize()
    {
        return data.get().getInitialSize();
    }

    /**
     * Set the initialSize
     */
    public void setInitialSize(int initialSize)
    {
        data.get().setInitialSize(initialSize);
    }

    /**
     * @return the maximumSize
     */
    public int getMaximumSize()
    {
        return data.get().getMaximumSize();
    }

    /**
     * Set the maximumSize
     */
    public void setMaximumSize(int maximumSize)
    {
        data.get().setMaximumSize(maximumSize);
    }

    /**
     * @return Returns the maxCreated.
     */
    public int getMaxCreated()
    {
        return data.get().getMaxCreated();
    }

    /**
     * @return Returns the pooled instance count.
     */
    public int getPooledCount()
    {
        return data.get().getPooledCount();
    }

    /**
     * @return Returns the working instance count.
     */
    public int getWorkingCount()
    {
        return data.get().getWorkingCount();
    }


    /**
     * @return the forwardName
     */
    public String getForwardName()
    {
        return data.get().getForwardName();
    }


    /**
     * @return the serverName
     */
    public String getServerName()
    {
        return data.get().getServerName();
    }


    /**
     * @return the flowSystem
     */
    public String getFlowSystem()
    {
        return data.get().getFlowSystem();
    }


    /**
     * @return the flowService
     */
    public String getFlowService()
    {
        return data.get().getFlowService();
    }


    /**
     * @return the destinationName
     */
    public String getDestinationName()
    {
        return data.get().getDestinationName();
    }

    /**
     * @return the connectionFactory
     */
    public String getConnectionFactory()
    {
        return data.get().getConnectionFactory();
    }

    /**
     * @return the messageSelector
     */
    public String getMessageSelector()
    {
        return data.get().getMessageSelector();
    }

    /**
     * @return the reconnectInterval
     */
    public long getReconnectInterval()
    {
        return data.get().getReconnectInterval();
    }

    /**
     * Set the reconnectInterval
     */
    public void setReconnectInterval(long reconnectInterval)
    {
        data.get().setReconnectInterval(reconnectInterval);
    }

    /**
     * @return the readBlockCount
     */
    public int getReadBlockCount()
    {
        return data.get().getReadBlockCount();
    }

    /**
     * Set the readBlockCount
     */
    public void setReadBlockCount(int readBlockCount)
    {
        data.get().setReadBlockCount(readBlockCount);
    }

    /**
     * @return the refDP
     */
    public String getRefDataProvider()
    {
        return data.get().getRefDP();
    }


    /**
     * @return the sleepTimeout
     */
    public long getSleepTimeout()
    {
        return data.get().getSleepTimeout();
    }

    /**
     * Set the initialSize
     */
    public void setSleepTimeout(long sleepTimeout)
    {
        data.get().setSleepTimeout(sleepTimeout);
    }

    /**
     * @return the receiveTimeout
     */
    public long getReceiveTimeout()
    {
        return data.get().getReceiveTimeout();
    }

    /**
     * Set the receiveTimeout
     */
    public void setReceiveTimeout(long receiveTimeout)
    {
        data.get().setReceiveTimeout(receiveTimeout);
    }

    /**
     * @return the transacted
     */
    public boolean getTransacted()
    {
        return data.get().isTransacted();
    }


    /**
     * @return the transactionTimeout
     */
    public int getTransactionTimeout()
    {
        return data.get().getTransactionTimeout();
    }

    /**
     * Set the transactionTimeout
     */
    public void setTransactionTimeout(int transactionTimeout)
    {
        data.get().setTransactionTimeout(transactionTimeout);
    }

    /**
     * @return the dumpMessage
     */
    public boolean getDumpMessage()
    {
        return data.get().isDumpMessage();
    }

    public void setDumpMessage(boolean dumpMessage)
    {
        data.get().setDumpMessage(dumpMessage);
    }

    /**
     * @return the debug
     */
    public boolean getDebug()
    {
        return data.get().isDebug();
    }

    /**
     * @return the debug
     */
    public void setDebug(boolean debug)
    {
        data.get().setDebug(debug);
    }

    /**
     * @return the active
     */
    public boolean getActive()
    {
        return data.get().isActive();
    }

    public void start()
    {
        data.get().start();
    }

    public void stop()
    {
        data.get().stop();
    }
}
