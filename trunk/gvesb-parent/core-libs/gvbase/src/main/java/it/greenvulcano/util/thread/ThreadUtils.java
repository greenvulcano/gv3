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
package it.greenvulcano.util.thread;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.log4j.Logger;

/**
 *
 * @version 3.4.0 24/nov/2013
 * @author GreenVulcano Developer Team
 *
 */
public class ThreadUtils
{
    /**
     * @param throwable
     * @return return the stack-trace
     */
    public static String getStackTrace(Throwable throwable)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream pstream = new PrintStream(baos);
        String stack = null;
        throwable.printStackTrace(pstream);
        pstream.flush();
        stack = baos.toString();
        return stack;
    }

    /**
     * Check the given Throwable and recursively his cause to in order to find an
     * InterruptedException instance. If found, the exception is rethrown and the
     * current Thread is interrupted.
     *
     * @param thw
     * @throws InterruptedException
     */
    public static void checkInterrupted(Throwable thw) throws InterruptedException {
        while (thw != null) {
            if (thw instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                throw (InterruptedException) thw;
            }
            thw = thw.getCause();
        }
    }
    
    /**
     * 
     * @param type
     *        component type
     * @param name
     *        component name
     * @param logger
     *        if not null is used to log a message at error level
     * @throws InterruptedException
     */
    public static void checkInterrupted(String type, String name, Logger logger) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            logger.error(type + "[" + name + "] interrupted.");
            throw new InterruptedException(type + "[" + name + "] interrupted.");
        }
    }
    
    /**
     * 
     * @param message
     *        the error message
     * @param logger
     *        if not null is used to log a message at error level
     * @throws InterruptedException
     */
    public static void checkInterrupted(String message, Logger logger) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            logger.error(message);
            throw new InterruptedException(message);
        }
    }

}
