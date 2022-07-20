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

import java.io.IOException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.RollingFileAppender;

import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;

/**
 * GVSizeRollingFileAppender extends {@link FileAppender} so that the
 * underlying file is rolled over at a user chosen size, keeping a max number of back-ups.
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
 *
 *
*/
public class GVSizeRollingFileAppender extends RollingFileAppender
{

    /**
     * File name.
     */
    private String        origFileName   = null;

    /**
     * The default constructor does nothing.
     */
    public GVSizeRollingFileAppender()
    {
    	super();
    }

    /**
     Instantiate a <code>GVSizeRollingFileAppender</code> and open the file
      designated by <code>filename</code>. The opened filename will become the
      output destination for this appender.

     <p>The file will be appended to.  */
    public GVSizeRollingFileAppender(Layout layout, String filename) throws IOException, PropertiesHandlerException
    {
    	super(layout, PropertiesHandler.expand(filename));
    }
  
    /**
      Instantiate a <code>GVSizeRollingFileAppender</code> and open the file
      designated by <code>filename</code>. The opened filename will become the
      output destination for this appender.
     
     <p>If the <code>append</code> parameter is true, the file will be
     appended to. Otherwise, the file desginated by
     <code>filename</code> will be truncated before being opened.
    
      @param layout
      @param filename
      @param append
      @throws IOException
      @throws PropertiesHandlerException
     */
    public GVSizeRollingFileAppender(Layout layout, String filename, boolean append) throws IOException, PropertiesHandlerException
    {
        super(layout, PropertiesHandler.expand(filename), append);
    }

    /**
     * 
     */
    @Override
    public synchronized void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize) throws IOException {
        origFileName = fileName;

        try {
            origFileName = PropertiesHandler.expand(fileName);
        }
        catch (PropertiesHandlerException exc) {
            exc.printStackTrace();
        }
        super.setFile(origFileName, append, bufferedIO, bufferSize);
    }
    

    /**
     * Create the log file name
     */
    @Override
    public void setFile(String fileName)
    {
        origFileName = fileName;

        try {
            origFileName = PropertiesHandler.expand(fileName);
        }
        catch (PropertiesHandlerException exc) {
            exc.printStackTrace();
        }
        super.setFile(origFileName);
    }

}
