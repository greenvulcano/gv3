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

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.ValidatorForm;


/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class Axis2RequestFormList extends ValidatorForm
{
    /**
     *
     */
    private static final long  serialVersionUID = 300L;

    /**
     * index
     */
    private String             index            = "0";

    /**
     * submit field
     */
    private String             submit           = "";
    /**
     *
     */
    private String             listCommand      = "";

    private LinkedList<Object> list;

    private String[]           executeOptions;

    /**
     * Context
     */
    private String             context;

    /**
     * @return
     */
    public String getIndex()
    {
        return index;
    }

    /**
     * @param index
     */
    public void setIndex(String index)
    {
        this.index = index;
    }

    /**
     * @param index
     */
    public void delete(int index)
    {

        list.remove(index);
    }

    /**
     * The constructor
     */
    public Axis2RequestFormList()
    {
        list = new LinkedList<Object>();
    }

    /**
     * Getting the object from a index
     *
     * @param index
     *
     *@return
     */
    public Object get(int index)
    {
        return list.get(index);
    }

    /**
     * Check elements in list
     *
     * @return
     */
    public boolean isEmpty()
    {
        return list.isEmpty();
    }

    /**
     * Insert element in list
     *
     * @param index
     * @param value
     * @return
     */
    public void add(int index, Object value)
    {
        list.add(index, value);
    }

    /**
     * Append the element
     *
     * @param value
     */
    public void append(Object value)
    {
        if (!list.contains(value)) {
            list.addLast(value);
        }
    }

    /**
     * @param arg0
     * @return
     */
    public boolean contains(Object arg0)
    {
        return list.contains(arg0);
    }

    /**
     * @return
     */
    public List<Object> getList()
    {
        return list;
    }

    /**
     * @param list
     */
    public void setList(List<Object> list)
    {
        this.list = new LinkedList<Object>(list);
    }

    /**
     * @return
     */
    public String getSubmit()
    {
        return submit;
    }

    /**
     * @param submit
     */
    public void setSubmit(String submit)
    {
        this.submit = submit;
    }

    /**
     * @return
     */
    public String getContext()
    {
        return context;
    }


    /**
     * @param context
     */
    public void setContext(String context)
    {
        this.context = context;
    }

    /**
     * @return
     */
    public String[] getExecuteOptions()
    {
        return executeOptions;
    }

    /**
     * @param selectedOptions
     */
    public void setExecuteOptions(String[] selectedOptions)
    {
        this.executeOptions = selectedOptions;
    }

    /**
     * @see org.apache.struts.validator.ValidatorForm#reset(org.apache.struts.action.ActionMapping,
     *      javax.servlet.http.HttpServletRequest)
     */
    public void reset(ActionMapping arg0, HttpServletRequest arg1)
    {
        executeOptions = new String[0];
    }

    /**
     * @param idxSource
     * @param idxDest
     * @throws Exception
     */
    public void moveItem(int idxSource, int idxDest) throws Exception
    {
        if (idxSource < 0 || idxSource >= list.size() || idxDest < 0) {
            throw new Exception("Out bound of index");
        }
        else if (idxDest >= list.size()) {
            return;
        }
        Object item = list.remove(idxSource);
        list.add(idxDest, item);
    }

    /**
     * @return
     */
    public String getListCommand()
    {
        return listCommand;
    }

    /**
     * @param listCommand
     */
    public void setListCommand(String listCommand)
    {
        this.listCommand = listCommand;
    }
}
