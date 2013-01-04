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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * @version 3.0.0 Apr 17, 2010
 * @author nunzio
 *
 */
public class ServletContextWrapper implements ServletContext
{
    private ServletContext servletContext = null;

    /**
     * @param sc
     */
    public ServletContextWrapper(ServletContext sc)
    {
        servletContext = sc;
    }

    /**
     * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String s)
    {

        return servletContext.getAttribute(s);
    }

    /**
     * @see javax.servlet.ServletContext#getAttributeNames()
     */
    @Override
    public Enumeration<?> getAttributeNames()
    {

        return servletContext.getAttributeNames();
    }

    /**
     * @see javax.servlet.ServletContext#getContext(java.lang.String)
     */
    @Override
    public ServletContext getContext(String s)
    {

        return servletContext.getContext(s);
    }

    /**
     * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
     */
    @Override
    public String getInitParameter(String s)
    {

        return servletContext.getInitParameter(s);
    }

    /**
     * @see javax.servlet.ServletContext#getInitParameterNames()
     */
    @Override
    public Enumeration<?> getInitParameterNames()
    {

        return servletContext.getInitParameterNames();
    }

    /**
     * @see javax.servlet.ServletContext#getMajorVersion()
     */
    @Override
    public int getMajorVersion()
    {

        return servletContext.getMajorVersion();
    }

    /**
     * @see javax.servlet.ServletContext#getMimeType(java.lang.String)
     */
    @Override
    public String getMimeType(String s)
    {

        return servletContext.getMimeType(s);
    }

    /**
     * @see javax.servlet.ServletContext#getMinorVersion()
     */
    @Override
    public int getMinorVersion()
    {

        return servletContext.getMinorVersion();
    }

    /**
     * @see javax.servlet.ServletContext#getNamedDispatcher(java.lang.String)
     */
    @Override
    public RequestDispatcher getNamedDispatcher(String s)
    {

        return servletContext.getNamedDispatcher(s);
    }

    /**
     * @see javax.servlet.ServletContext#getRealPath(java.lang.String)
     */
    @Override
    public String getRealPath(String s)
    {

        return servletContext.getRealPath(s);
    }

    /**
     * @see javax.servlet.ServletContext#getRequestDispatcher(java.lang.String)
     */
    @Override
    public RequestDispatcher getRequestDispatcher(String s)
    {

        return servletContext.getRequestDispatcher(s);
    }

    /**
     * @see javax.servlet.ServletContext#getResource(java.lang.String)
     */
    @Override
    public URL getResource(String s) throws MalformedURLException
    {

        return servletContext.getResource(s);
    }

    /**
     * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
     */
    @Override
    public InputStream getResourceAsStream(String s)
    {

        return servletContext.getResourceAsStream(s);
    }

    /**
     * @see javax.servlet.ServletContext#getResourcePaths(java.lang.String)
     */
    @Override
    public Set<?> getResourcePaths(String s)
    {

        return servletContext.getResourcePaths(s);
    }

    /**
     * @see javax.servlet.ServletContext#getServerInfo()
     */
    @Override
    public String getServerInfo()
    {

        return servletContext.getServerInfo();
    }

    /**
     * @see javax.servlet.ServletContext#getServlet(java.lang.String)
     */
    @SuppressWarnings("deprecation")
    @Override
    public Servlet getServlet(String s) throws ServletException
    {

        return servletContext.getServlet(s);
    }

    /**
     * @see javax.servlet.ServletContext#getServletContextName()
     */
    @Override
    public String getServletContextName()
    {

        return servletContext.getServletContextName();
    }

    /**
     * @see javax.servlet.ServletContext#getServletNames()
     */
    @SuppressWarnings("deprecation")
    @Override
    public Enumeration<?> getServletNames()
    {

        return servletContext.getServletNames();
    }

    /**
     * @see javax.servlet.ServletContext#getServlets()
     */
    @SuppressWarnings("deprecation")
    @Override
    public Enumeration<?> getServlets()
    {

        return servletContext.getServlets();
    }

    /**
     * @see javax.servlet.ServletContext#log(java.lang.String)
     */
    @Override
    public void log(String s)
    {

        servletContext.log(s);
    }

    /**
     * @see javax.servlet.ServletContext#log(java.lang.Exception,
     *      java.lang.String)
     */
    @SuppressWarnings("deprecation")
    @Override
    public void log(Exception exception, String s)
    {

        servletContext.log(exception, s);
    }

    /**
     * @see javax.servlet.ServletContext#log(java.lang.String,
     *      java.lang.Throwable)
     */
    @Override
    public void log(String s, Throwable throwable)
    {

        servletContext.log(s, throwable);
    }

    /**
     * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
     */
    @Override
    public void removeAttribute(String s)
    {

        servletContext.removeAttribute(s);
    }

    /**
     * @see javax.servlet.ServletContext#setAttribute(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public void setAttribute(String s, Object obj)
    {

        servletContext.setAttribute(s, obj);
    }

}
