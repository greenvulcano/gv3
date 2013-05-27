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
package it.greenvulcano.util.crypto;

/**
 * KeyStoreUtilsException class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 **/
public class KeyStoreUtilsException extends Exception
{
    private static final long serialVersionUID = -1817756198098014511L;

    /**
     * Constructor
     */
    public KeyStoreUtilsException()
    {
        super();
    }

    /**
     * Constructor
     *
     * @param message
     *        exception message
     */
    public KeyStoreUtilsException(String message)
    {
        super(message);
    }

    /**
     * Constructor
     *
     * @param message
     *        exception message
     * @param exc
     *        cause of the exception
     */
    public KeyStoreUtilsException(String message, Throwable exc)
    {
        super(message, exc);
    }

    /**
     * Constructor
     *
     * @param exc
     *        cause of the exception
     */
    public KeyStoreUtilsException(Throwable exc)
    {
        super(exc);
    }
}
