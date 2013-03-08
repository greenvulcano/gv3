/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.axis2.dispatchers;

import it.greenvulcano.log.GVLogger;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingFaultsHelper;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.dispatchers.ActionBasedOperationDispatcher;
import org.apache.axis2.engine.AbstractDispatcher;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.LoggingControl;
import org.apache.log4j.Logger;


/**
 * @version 3.2.0 03/mag/2012
 * @author GreenVulcano Developer Team
 */
public class AddressingBasedDispatcher extends AbstractDispatcher implements AddressingConstants
{
    public static final String               NAME  = "GVAddressingBasedDispatcher";
    private static final Logger              log   = GVLogger.getLogger(AddressingBasedDispatcher.class);
    private RequestURIBasedServiceDispatcher rubsd = null;
    private ActionBasedOperationDispatcher   abod  = null;

    public AddressingBasedDispatcher()
    {
        rubsd = new RequestURIBasedServiceDispatcher();
        abod = new ActionBasedOperationDispatcher();
    }

    @Override
    public AxisOperation findOperation(AxisService service, MessageContext messageContext) throws AxisFault
    {
        return abod.findOperation(service, messageContext);
    }

    @Override
    public AxisService findService(MessageContext messageContext) throws AxisFault
    {
        return rubsd.findService(messageContext);
    }

    @Override
    public void initDispatcher()
    {
        init(new HandlerDescription(NAME));
    }

    @Override
    public org.apache.axis2.engine.Handler.InvocationResponse invoke(MessageContext msgctx) throws AxisFault
    {
        org.apache.axis2.engine.Handler.InvocationResponse response = org.apache.axis2.engine.Handler.InvocationResponse.CONTINUE;
        if (msgctx.getRelatesTo() != null) {
            String relatesTo = msgctx.getRelatesTo().getValue();
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug((new StringBuilder()).append(msgctx.getLogIDString()).append(" ").append(
                        Messages.getMessage("checkingrelatesto", relatesTo)).toString());
            }
            if ((relatesTo != null) && !"".equals(relatesTo) && (msgctx.getOperationContext() == null)) {
                OperationContext operationContext = msgctx.getConfigurationContext().getOperationContext(relatesTo);
                if (operationContext != null) {
                    msgctx.setAxisOperation(operationContext.getAxisOperation());
                    msgctx.setOperationContext(operationContext);
                    msgctx.setServiceContext((ServiceContext) operationContext.getParent());
                    msgctx.setAxisService(((ServiceContext) operationContext.getParent()).getAxisService());
                    msgctx.getAxisOperation().registerMessageContext(msgctx, operationContext);
                    msgctx.setServiceGroupContextId(((ServiceGroupContext) msgctx.getServiceContext().getParent()).getId());
                    if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                        log.debug((new StringBuilder()).append(msgctx.getLogIDString()).append(
                                " Dispatched successfully on the RelatesTo. operation=").append(
                                operationContext.getAxisOperation()).toString());
                    }
                }
            }
        }
        else {
            response = super.invoke(msgctx);
            Object flag = msgctx.getLocalProperty("IsAddressingProcessed");
            if (log.isTraceEnabled()) {
                log.trace((new StringBuilder()).append("invoke: IS_ADDR_INFO_ALREADY_PROCESSED=").append(flag).toString());
            }
            if (JavaUtils.isTrueExplicitly(flag)
                    && JavaUtils.isTrue(msgctx.getProperty("addressing.validateAction"), true)) {
                checkAction(msgctx);
            }
        }
        return response;
    }

    private void checkAction(MessageContext msgContext) throws AxisFault
    {
        if ((msgContext.getAxisService() == null) || (msgContext.getAxisOperation() == null)) {
            AddressingFaultsHelper.triggerActionNotSupportedFault(msgContext, msgContext.getWSAAction());
        }
    }
}
