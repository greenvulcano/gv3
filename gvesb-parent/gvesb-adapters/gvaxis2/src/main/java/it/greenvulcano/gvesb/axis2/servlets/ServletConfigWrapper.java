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
package it.greenvulcano.gvesb.axis2.servlets;

import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * @version 3.0.0 Apr 17, 2010
 * @author nunzio
 *
 */
public class ServletConfigWrapper implements ServletConfig
{
    private ServletConfig servletConfig = null;

    /**
     * @param sc
     */
    public ServletConfigWrapper(ServletConfig sc)
    {
        servletConfig = sc;
    }

    /**
     * @see javax.servlet.ServletConfig#getInitParameter(java.lang.String)
     */
    @Override
    public String getInitParameter(String s)
    {
        String initParam = servletConfig.getInitParameter(s);
        try {
            if (!PropertiesHandler.isExpanded(initParam)) {
                initParam = PropertiesHandler.expand(initParam);
            }
        }
        catch (PropertiesHandlerException exc) {
            exc.printStackTrace();
        }

        return initParam;
    }

    /**
     * @see javax.servlet.ServletConfig#getInitParameterNames()
     */
    @Override
    public Enumeration<?> getInitParameterNames()
    {
        return servletConfig.getInitParameterNames();
    }

    /**
     * @see javax.servlet.ServletConfig#getServletContext()
     */
    @Override
    public ServletContext getServletContext()
    {
        return new ServletContextWrapper(servletConfig.getServletContext());
    }

    /**
     * @see javax.servlet.ServletConfig#getServletName()
     */
    @Override
    public String getServletName()
    {
        return servletConfig.getServletName();
    }

}
