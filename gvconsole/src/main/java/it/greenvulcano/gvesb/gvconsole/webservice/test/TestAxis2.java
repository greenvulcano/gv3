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
package it.greenvulcano.gvesb.gvconsole.webservice.test;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import it.greenvulcano.log.GVLogger;
import it.greenvulcano.gvesb.gvconsole.webservice.forms.Axis2WSResultForm;
import it.greenvulcano.gvesb.gvconsole.webservice.forms.Axis2WebServiceForm;
import it.greenvulcano.gvesb.virtual.ws.dynamic.invoker.DynamicInvoker;

/**
 * Class to manage Tests in Axis2 Dynamic Invoker
 *
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class TestAxis2
{
    private Axis2WebServiceForm form           = null;
    private Axis2WSResultForm   resultForm     = null;
    private InvocationBean      invocationBean = null;

    /**
     * Logger definition.
     */
    private static final Logger logger         = GVLogger.getLogger(TestAxis2.class);


    /**
     * @param form
     * @param bean
     * @throws Exception
     */
    public TestAxis2(Axis2WebServiceForm form, InvocationBean bean) throws Exception
    {
        this.form = new Axis2WebServiceForm(form);
        invocationBean = bean;
    }

    /**
     * @param request
     * @param formaxis2
     * @throws Exception
     */
    public void startTest(HttpServletRequest request, Axis2WebServiceForm formaxis2) throws Exception
    {
        DynamicInvoker dynamicInvoker = null;
        try {
            String wsdlUrl = formaxis2.getUrl();
            String serviceNS = "";
            String service = formaxis2.getService();
            String operation = formaxis2.getOperation();
            String portName = formaxis2.getPortname();
            String timeout = formaxis2.getTimeout();

            Integer intTime = new Integer(timeout);
            int intTO = intTime.intValue();

            dynamicInvoker = DynamicInvoker.getInvoker(wsdlUrl);
            dynamicInvoker.setService(serviceNS, service, null);
            dynamicInvoker.setOperation(operation, portName);
            dynamicInvoker.setTimeout(intTO);

            Map<?, ?> mapResult = new HashMap();

            logger.debug("*** Invocation BASIC-AXISDynamicInvoker...");

            // Visualizzazione del risultato
            //

            StringBuffer bufObject = new StringBuffer("\n");


            if (mapResult.isEmpty()) {
                bufObject.append("\tThe mapResult is empty\n");
                logger.debug("\tThe mapResult is empty\n");
            }
            else {
                bufObject.append("\tThe name/value/type are:\n");

                Set keyResultSet = mapResult.keySet();
                Iterator iterPositionSet = keyResultSet.iterator();
                String key = "";
                String value = "";
                while (iterPositionSet.hasNext()) {
                    key = (String) iterPositionSet.next();
                    Object param = (Object) mapResult.get(key);
                    if (param instanceof byte[]) {
                        value = new String((byte[]) param);
                    }
                    else {
                        value = "" + param;
                    }
                    bufObject.append("\t\t").append(key).append("\t/\t").append(value).append("\t/\t").append(
                            param.getClass()).append("\t(").append(")\n");
                }
                bufObject.append("\n");
            }

            String result = bufObject.toString();
            logger.debug("RISULTATO AXIS2 TEST : " + bufObject.toString());

            // parametri di input configurazione del WS
            resultForm = new Axis2WSResultForm(result);
            resultForm.setService(formaxis2.getService());
            resultForm.setOperation(formaxis2.getOperation());
            resultForm.setPortname(formaxis2.getPortname());

            // set variabile di sessione per far caricare dal render il result
            // form
            request.getSession().setAttribute("Axis2Result", resultForm);

            logger.debug("FINE AXIS2 TEST");

        }
        catch (Exception exc) {

            if (exc instanceof java.net.ConnectException) {

                logger.error("ConnectException : " + exc);
                resultForm = new Axis2WSResultForm(exc);

                // set variabile di sessione per far caricare dal render il
                // result form
                request.getSession().setAttribute("Axis2Result", resultForm);
            }
            else {

                logger.error("Exception : " + exc);
                resultForm = new Axis2WSResultForm(exc);

                // set variabile di sessione per far caricare dal render il
                // result form
                request.getSession().setAttribute("Axis2Result", resultForm);
            }
        }
        finally {
            if (dynamicInvoker != null) {
                DynamicInvoker.returnInvoker(dynamicInvoker);
            }
        }

    }

    /**
     * @return
     */
    public Axis2WebServiceForm getForm()
    {
        return form;
    }

    /**
     * @param form
     */
    public void setForm(Axis2WebServiceForm form)
    {
        this.form = form;
    }

    /**
     * @return
     */
    public Axis2WSResultForm getResultForm()
    {
        return resultForm;
    }

    /**
     * @param resultForm
     */
    public void setResultForm(Axis2WSResultForm resultForm)
    {
        if (resultForm == null) {
            this.resultForm = null;

        }
        else {
            this.resultForm = new Axis2WSResultForm(resultForm);
        }
    }

    /**
     *
     */
    public void clean()
    {
        resultForm = null;
    }

    /**
     *
     */
    public void reset()
    {

        form.reset();
    }


    /**
     * @return
     */
    public InvocationBean getInvocationBean()
    {
        return invocationBean;
    }

    /**
     * @param invocationBean
     */
    public void setInvocationBean(InvocationBean invocationBean)
    {
        this.invocationBean = invocationBean;
    }
}
