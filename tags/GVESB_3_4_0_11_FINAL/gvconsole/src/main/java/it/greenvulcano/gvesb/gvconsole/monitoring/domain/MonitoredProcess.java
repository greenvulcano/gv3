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
package it.greenvulcano.gvesb.gvconsole.monitoring.domain;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author Angelo
 *
 */
public class MonitoredProcess
{

    private String name;
    private String url;
    private String user;
    private String password;

    /**
     * @return the name of this monitored process
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the URL to connect to the process
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @param url
     */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /**
     * @return the user to connect to the process
     */
    public String getUser()
    {
        return user;
    }

    /**
     * @param user
     */
    public void setUser(String user)
    {
        this.user = user;
    }

    /**
     * @return the password to connect to the process
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder out = new StringBuilder();
        out.append("\n[" + this.getClass().getName() + "]\n");
        out.append("name    :->" + name + "<-\n");
        out.append("url     :->" + url + "<-\n");
        out.append("user    :->" + (user != null ? user : "") + "<-\n");
        out.append("password:->" + (password != null ? password : "") + "<-\n");
        out.append("[/" + this.getClass().getName() + "]\n");
        return out.toString();
    }

}
