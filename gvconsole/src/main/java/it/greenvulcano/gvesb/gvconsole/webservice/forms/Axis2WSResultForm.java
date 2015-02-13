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
package it.greenvulcano.gvesb.gvconsole.webservice.forms;


import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.struts.action.ActionForm;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class Axis2WSResultForm extends ActionForm
{

    private static final long serialVersionUID = 300L;
    private Throwable         exception;
    private String            url              = "";
    private String            service          = "";
    private String            operation        = "";
    private String            result           = "";
    private String            command          = "";
    private String            portname         = "";


    /**
     *
     */
    public Axis2WSResultForm()
    {
        super();
    }

    /**
     * @param result
     */
    public Axis2WSResultForm(String result)
    {
        this.result = result;
    }

    /**
     * @param result
     */
    public Axis2WSResultForm(Axis2WSResultForm result)
    {
        if (result.getException().length() > 0) {
            exception = new Throwable(result.getException());
        }
        else {
            // risultato del test
            this.result = result.getResult();
        }
    }

    /**
     * @param exception
     */
    public Axis2WSResultForm(Throwable exception)
    {
        this.exception = exception;
    }

    /**
     * @return
     */
    public boolean isError()
    {
        return exception != null;
    }

    /**
     * @return
     */
    public String getException()
    {
        StringBuffer buff = new StringBuffer();

        Throwable thr = exception;
        while (thr != null) {
            StringWriter writer = new StringWriter();
            PrintWriter out = new PrintWriter(writer);
            thr.printStackTrace(out);
            out.flush();
            buff.append(writer);
            thr = thr.getCause();
            if (thr != null) {
                buff.append("\r\n\r\n\r\nCAUSED BY:\r\n\r\n");
            }
        }

        return buff.toString();
    }

    /**
     * @param exception
     */
    public void setException(Throwable exception)
    {
        this.exception = exception;
    }


    /**
     * @return
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
     * @return
     */
    public String getService()
    {
        return service;
    }

    /**
     * @param service
     */
    public void setService(String service)
    {
        this.service = service;
    }

    /**
     * @return
     */
    public String getOperation()
    {
        return operation;
    }

    /**
     * @param operation
     */
    public void setOperation(String operation)
    {
        this.operation = operation;
    }

    /**
     * @return
     */
    public String getResult()
    {
        return result;
    }

    /**
     * @param result
     */
    public void setResult(String result)
    {
        this.result = result;
    }

    /**
     * @return
     */
    public String getCommand()
    {
        return command;
    }

    /**
     * @param command
     */
    public void setCommand(String command)
    {
        this.command = command;
    }

    /**
     * @return
     */
    public String getPortname()
    {
        return portname;
    }

    /**
     * @param portname
     */
    public void setPortname(String portname)
    {
        this.portname = portname;
    }

}
