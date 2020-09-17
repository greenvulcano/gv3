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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class UserTransactionProxy implements Serializable, UserTransaction
{

    /**
     *
     */
    private static final long         serialVersionUID = 300L;
    transient private UserTransaction userTransaction;

    /**
     * @throws Exception
     */
    public UserTransactionProxy() throws Exception
    {
        init();
    }

    private void init() throws Exception
    {
        Context ctx = null;
        try {
            ctx = new InitialContext();
            userTransaction = (UserTransaction) ctx.lookup("UserTransaction");

        }
        finally {
            if (ctx != null) {
                try {
                    ctx.close();

                }
                catch (NamingException exc) {
                    // Do nothing
                }
            }
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
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
     * @see javax.transaction.UserTransaction#begin()
     */
    public void begin() throws NotSupportedException, SystemException
    {
        userTransaction.begin();
    }

    /**
     * @see javax.transaction.UserTransaction#commit()
     */
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
            SecurityException, IllegalStateException, SystemException
    {
        userTransaction.commit();
    }

    /**
     * @see javax.transaction.UserTransaction#rollback()
     */
    public void rollback() throws IllegalStateException, SecurityException, SystemException
    {
        userTransaction.rollback();
    }

    /**
     * @see javax.transaction.UserTransaction#setRollbackOnly()
     */
    public void setRollbackOnly() throws IllegalStateException, SystemException
    {
        userTransaction.setRollbackOnly();
    }

    /**
     * @see javax.transaction.UserTransaction#getStatus()
     */
    public int getStatus() throws SystemException
    {
        return userTransaction.getStatus();
    }

    /**
     * @see javax.transaction.UserTransaction#setTransactionTimeout(int)
     */
    public void setTransactionTimeout(int timeout) throws SystemException
    {
        userTransaction.setTransactionTimeout(timeout);
    }
}