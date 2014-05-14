/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.greenvulcano.gvesb.gvconsole.webservice.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * FileBean class
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
*/
public class FileBean
{
    /**
     * The file name
     */
    private String  name;

    /**
     * The file extension
     */
    private String  extension;

    /**
     * The data create
     */
    private String  data;

    /**
     * The flag for WSDL consistence
     */
    private boolean flag = false;

    /**
     * Default constructor.
     */
    public FileBean()
    {
        name = "";
        extension = "";
        data = "";
        flag = false;
    }

    /**
     * Constructor with parameters
     *
     * @param name
     * @param extension
     * @param data
     */
    public FileBean(String name, String extension, long data, boolean flag)
    {
        this.name = name;
        this.extension = extension;
        Date d = new Date(data);
        SimpleDateFormat format = new SimpleDateFormat("d MMM yyyy HH:mm:ss");
        this.data = format.format(d);
        this.flag = flag;
    }


    /**
     * @return Returns the data.
     */
    public String getData()
    {
        return data;
    }

    /**
     * @param data
     *        The data to set.
     */
    public void setData(String data)
    {
        this.data = data;
    }

    /**
     * @param flag
     *        for WSDL consistence
     */
    public void setFlag(boolean flag)
    {
        this.flag = flag;
    }

    /**
     * @return Returns flag.
     */
    public boolean getFlag()
    {
        return flag;
    }

    /**
     * @return Returns the extension.
     */
    public String getExtension()
    {
        return extension;
    }

    /**
     * @param extension
     *        The extension to set.
     */
    public void setExtension(String extension)
    {
        this.extension = extension;
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *        The name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }
}
