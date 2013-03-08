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
package it.greenvulcano.jboss.jmx;

import it.greenvulcano.jmx.JMXEntryPoint;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * Classe serializzabile per eseguire il binding del TM sul JNDI di JBoss.
 * Questa classe è necessaria poichè JBoss (contro la specifica) non rende
 * reperibile il TM sul JNDI.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class TransactionManagerProxy implements Serializable, TransactionManager
{

    private static final long            serialVersionUID = 300L;
    private transient TransactionManager transactionManager;
    private String                       objectName;
    private String                       attributeName;

    /**
     * @param objectName
     * @param attributeName
     * @throws Exception
     */
    public TransactionManagerProxy(String objectName, String attributeName) throws Exception
    {
        this.objectName = objectName;
        this.attributeName = attributeName;
        init();
    }

    private void init() throws Exception
    {
        System.out.println("PREPARING TRANSACTION MANAGER: name=" + objectName + ", attribute=" + attributeName);
        MBeanServer server = JMXEntryPoint.instance().getServer();
        transactionManager = (TransactionManager) server.getAttribute(new ObjectName(objectName), attributeName);
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        System.out.println("BINDING TRANSACTION MANAGER: name=" + objectName + ", attribute=" + attributeName);
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        try {
            init();
        }
        catch (Exception exc) {
            exc.printStackTrace();
            throw new IOException("" + exc);
        }
    }

    /**
     * @see javax.transaction.TransactionManager#begin()
     */
    public void begin() throws NotSupportedException, SystemException
    {
        transactionManager.begin();
    }

    /**
     * @see javax.transaction.TransactionManager#commit()
     */
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
            SecurityException, IllegalStateException, SystemException
    {
        transactionManager.commit();
    }

    /**
     * @see javax.transaction.TransactionManager#getStatus()
     */
    public int getStatus() throws SystemException
    {
        return transactionManager.getStatus();
    }

    /**
     * @see javax.transaction.TransactionManager#getTransaction()
     */
    public Transaction getTransaction() throws SystemException
    {
        return transactionManager.getTransaction();
    }

    /**
     * @see javax.transaction.TransactionManager#resume(javax.transaction.Transaction
     *      )
     */
    public void resume(Transaction tx) throws InvalidTransactionException, IllegalStateException, SystemException
    {
        transactionManager.resume(tx);
    }

    /**
     * @see javax.transaction.TransactionManager#rollback()
     */
    public void rollback() throws IllegalStateException, SecurityException, SystemException
    {
        transactionManager.rollback();
    }

    /**
     * @see javax.transaction.TransactionManager#setRollbackOnly()
     */
    public void setRollbackOnly() throws IllegalStateException, SystemException
    {
        transactionManager.setRollbackOnly();
    }

    /**
     * @see javax.transaction.TransactionManager#setTransactionTimeout(int)
     */
    public void setTransactionTimeout(int timeout) throws SystemException
    {
        transactionManager.setTransactionTimeout(timeout);
    }

    /**
     * @see javax.transaction.TransactionManager#suspend()
     */
    public Transaction suspend() throws SystemException
    {
        return transactionManager.suspend();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "TransactionManagerProxy[" + objectName + ", " + attributeName + ", " + transactionManager + "]";
    }
}