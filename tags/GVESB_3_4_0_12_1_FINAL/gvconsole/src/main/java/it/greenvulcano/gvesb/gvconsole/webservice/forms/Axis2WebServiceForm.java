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


import it.greenvulcano.gvesb.utils.StringUtility;
import it.greenvulcano.log.GVLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.servlet.ServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.ValidatorForm;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class Axis2WebServiceForm extends ValidatorForm
{
    /**
     *
     */
    private static final long   serialVersionUID = 300L;

    /**
     * Logger definition.
     */
    private static final Logger logger           = GVLogger.getLogger(Axis2WebServiceForm.class);

    private String              service;
    private String              invoke;
    private String              dataStr;
    private String              submit;
    private String              context;
    private String              url;
    private String              operation;
    private String              portname         = "";
    private String              timeout;
    private String              newField;
    private String              newValue;
    private Map<String, String> fieldValues;
    private Map<String, String> delValues;

    /**
     *
     */
    public Axis2WebServiceForm()
    {
        init();
    }

    /**
     * @param axis2DataForm
     */
    public Axis2WebServiceForm(Axis2WebServiceForm axis2DataForm)
    {

        this.context = axis2DataForm.getContext();
        this.dataStr = axis2DataForm.getData();
        this.invoke = axis2DataForm.getInvoke();
        this.service = axis2DataForm.getService();
        this.submit = axis2DataForm.getSubmit();
        this.url = axis2DataForm.getUrl();
        this.operation = axis2DataForm.getOperation();
        this.portname = axis2DataForm.getPortname();
        this.timeout = axis2DataForm.getTimeout();
        this.fieldValues = axis2DataForm.getFieldValues();
        this.delValues = axis2DataForm.getDelValues();
        this.timeout = axis2DataForm.getTimeout();
        this.newValue = axis2DataForm.getNewValue();
        this.newField = axis2DataForm.getNewField();

        logger.debug("context " + this.context + "\n");
        logger.debug("data " + this.dataStr + "\n");
        logger.debug("invoke " + this.invoke + "\n");
        logger.debug("service " + this.service + "\n");
        logger.debug("submit" + this.submit + "\n");
        logger.debug("url " + this.url + "\n");
        logger.debug("operation " + this.operation + "\n");
        logger.debug("portname" + this.portname + "\n");
        logger.debug("timeout " + this.timeout + "\n");
    }

    /**
     *
     */
    public void init()
    {
        service = "";
        dataStr = "";
        submit = "";
        context = "";
        url = "";
        invoke = "";
        operation = "";
        portname = "";
        fieldValues = new TreeMap<String, String>();
        newField = "";
        timeout = "";
        newValue = "";
        delValues = new HashMap<String, String>();
    }


    /**
     *
     */
    public void reset()
    {
        init();
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
    public String getData()
    {
        return dataStr;
    }

    /**
     * @param dataStr
     */
    public void setData(String dataStr)
    {
        this.dataStr = dataStr;
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
    public String getInvoke()
    {
        return invoke;
    }

    /**
     * @param invoke
     */
    public void setInvoke(String invoke)
    {
        this.invoke = invoke;
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
     * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping,
     *      javax.servlet.ServletRequest)
     */
    public void reset(ActionMapping arg0, ServletRequest arg1)
    {
        super.reset(arg0, arg1);
        reset();
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

    /**
     * @return
     */
    public String getTimeout()
    {
        return timeout;
    }

    /**
     * @param timeout
     */
    public void setTimeout(String timeout)
    {
        this.timeout = timeout;
    }

    /**
     * @return
     */
    public Map<String, String> getFieldValues()
    {
        return fieldValues;
    }

    /**
     * @param fieldValues
     */
    public void setFieldValues(Map<String, String> fieldValues)
    {
        this.fieldValues = fieldValues;
    }

    /**
     * @return
     */
    public String getNewField()
    {
        return newField;
    }

    /**
     * @param newField
     */
    public void setNewField(String newField)
    {
        this.newField = newField;
    }

    /**
     * @return
     */
    public String getNewValue()
    {
        return newValue;
    }

    /**
     * @param newValue
     */
    public void setNewValue(String newValue)
    {
        this.newValue = newValue;
    }

    /**
     * @param key
     */
    public void removeField(String key)
    {
        fieldValues.remove(key);
    }

    /**
     *
     */
    public void addField()
    {
        if (!newField.equals("")) {

            if (fieldValues.containsValue(newField)) {
                return;
            }

            fieldValues.put(newField, newValue);
            newField = "";
            newValue = "";
        }
    }

    /**
     * @return
     */
    public Map<String, String> getDelValues()
    {
        return delValues;
    }

    /**
     * @param delValues
     */
    public void setDelValues(Map<String, String> delValues)
    {
        this.delValues = delValues;
    }

    private static final String SEPARATOR_PARAM = ",";

    /**
     * Metodo per la creazione di una stringa contenente alcuni dati del form di
     * interesse per la sicurezza
     *
     * @return stringa
     *
     */
    public String toString_Security()
    {
        String result = "";

        result += StringUtility.addIfNotEmpty(url, SEPARATOR_PARAM);
        result += StringUtility.addIfNotEmpty(service, SEPARATOR_PARAM);
        result += StringUtility.addIfNotEmpty(operation, SEPARATOR_PARAM);
        result += StringUtility.addIfNotEmpty(portname);

        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 1);
        }

        // Se sono stati inseriti WS Argument
        result += ",WSArguments{";
        for (Entry<String, String> entry : fieldValues.entrySet()) {
            result += StringUtility.addIfNotEmpty(entry.getKey() + "=" + entry.getValue(), SEPARATOR_PARAM);
        }
        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 1);
        }
        result += "}";

        return result;
    }
}