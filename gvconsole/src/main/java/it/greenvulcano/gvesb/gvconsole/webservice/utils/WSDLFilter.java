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
package it.greenvulcano.gvesb.gvconsole.webservice.utils;

import java.io.File;
import java.io.FileFilter;

/**
 * This filter accepts only file with extension 'WSDL' or 'wsdl'
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
*/
public final class WSDLFilter implements FileFilter
{
    /*
     * (non-Javadoc)
     *
     * @see java.io.FileFilter#accept(java.io.File)
     */
    public boolean accept(File f)
    {
        if (FileUtility.getExtension(f).equalsIgnoreCase("WSDL")) {
            return true;
        }
        return false;
    }
}
