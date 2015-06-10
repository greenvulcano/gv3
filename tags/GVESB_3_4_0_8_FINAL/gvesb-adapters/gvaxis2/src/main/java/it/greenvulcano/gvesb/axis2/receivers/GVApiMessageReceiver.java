/*
 * Copyright (c) 2009-2011 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.axis2.receivers;

import it.greenvulcano.gvesb.core.bpel.manager.GVBpelEngineServer;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.apache.axis2.util.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.ode.bpel.engine.ProcessAndInstanceManagementImpl;
import org.apache.ode.il.DynamicService;
import org.apache.ode.utils.Namespaces;
import org.w3c.dom.Document;

/**
 * @version 3.2.0 Apr 17, 2010
 * @author GreenVulcano Developer Team
 * 
 */
public class GVApiMessageReceiver extends AbstractInMessageReceiver
{
    private static final Logger logger = GVLogger.getLogger(GVApiMessageReceiver.class);

    public GVApiMessageReceiver()
    {
    }

    @Override
    public void invokeBusinessLogic(MessageContext messageContext) throws AxisFault
    {
        SOAPEnvelope envelope = null;
        SOAPFactory soapFactory = null;
        MessageContext outMsgContext = null;
        try {
            String operation = messageContext.getAxisOperation().getName().getLocalPart();
            GVBpelEngineServer server = GVBpelEngineServer.instance();
            outMsgContext = Utils.createOutMessageContext(messageContext);
            outMsgContext.getOperationContext().addMessageContext(outMsgContext);
            soapFactory = getSOAPFactory(messageContext);
            envelope = soapFactory.getDefaultEnvelope();
            outMsgContext.setEnvelope(envelope);
            logger.debug("operation=" + operation);
            if (operation.equals("getProcessDefinition")) {
                String processName = getProcessName(messageContext);
                for (QName process : server.getStore().getProcesses()) {
                    String[] nameVer = process.getLocalPart().split("-");
                    if (processName.equals(nameVer[0])) {
                        File bpelFile = new File(
                                server.getStore().getProcessConfiguration(process).getBaseURI().getPath()
                                        + server.getStore().getProcessConfiguration(process).getBpelDocument());
                        String response = FileUtils.readFileToString(bpelFile);
                        Document xmlResponse = XMLUtils.parseDOM_S(response);
                        OMElement oemResponse = org.apache.axis2.util.XMLUtils.toOM(xmlResponse.getDocumentElement());
                        envelope.getBody().addChild(oemResponse);
                    }
                }
            }
            else if (operation.equals("undeploy")) {
                String processName = getProcessName(messageContext);
                String path = null;
                for (QName process : server.getStore().getProcesses()) {
                    String[] nameVer = process.getLocalPart().split("-");
                    if (processName.equals(nameVer[0])) {
                        path = server.getStore().getProcessConfiguration(process).getBaseURI().getPath();
                    }
                    File deploymentDir = new File(path);
                    Collection<QName> undeployed = server.getStore().undeploy(deploymentDir);
                    FileUtils.deleteDirectory(deploymentDir);
                    OMElement response = soapFactory.createOMElement("response", null);
                    response.setText("" + (undeployed.size() > 0));
                    envelope.getBody().addChild(response);
                }
            }
            else {
                ProcessAndInstanceManagementImpl pm = new ProcessAndInstanceManagementImpl(server.getServer(),
                        server.getStore());
                DynamicService service = new DynamicService(pm);
                OMElement response = service.invoke(messageContext.getAxisOperation().getName().getLocalPart(),
                        messageContext.getEnvelope().getBody().getFirstElement());
                if (response != null) {
                    envelope.getBody().addChild(response);
                }
            }


        }
        catch (Exception e) {
            envelope.getBody().addFault(toSoapFault(e, soapFactory));
        }
        AxisEngine.send(outMsgContext);
    }

    private SOAPFault toSoapFault(Exception e, SOAPFactory soapFactory)
    {
        SOAPFault fault = soapFactory.createSOAPFault();
        SOAPFaultCode code = soapFactory.createSOAPFaultCode(fault);
        code.setText(new QName(Namespaces.SOAP_ENV_NS, "Server"));
        SOAPFaultReason reason = soapFactory.createSOAPFaultReason(fault);
        reason.setText(e.toString());
        OMElement detail = soapFactory.createOMElement(new QName(Namespaces.ODE_PMAPI_NS, e.getClass().getSimpleName()));
        StringWriter stack = new StringWriter();
        e.printStackTrace(new PrintWriter(stack));
        detail.setText(stack.toString());
        SOAPFaultDetail soapDetail = soapFactory.createSOAPFaultDetail(fault);
        soapDetail.addDetailEntry(detail);
        return fault;
    }

    private String getProcessName(MessageContext messageContext)
    {
        OMElement part = messageContext.getEnvelope().getBody().getFirstElement().getFirstElement();
        String pid = part.getText().trim();
        String varName[] = pid.split("-");
        String appoName[] = varName[0].split(":");
        String processName = appoName[1];
        return processName;
    }

}
