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
package it.greenvulcano.gvesb.virtual.openspcoop;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.rmi.RemoteException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamSource;

import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis.client.Stub;
import org.apache.axis.utils.ByteArrayOutputStream;
import org.apache.commons.fileupload.MultipartParser;
import org.apache.log4j.Logger;
import org.openspcoop.pdd.services.IntegrationManager;
import org.openspcoop.pdd.services.IntegrationManagerServiceLocator;
import org.openspcoop.pdd.services.SPCoopException;
import org.openspcoop.pdd.services.SPCoopHeaderInfo;
import org.openspcoop.pdd.services.SPCoopMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * 
 * @version 3.2.0 May 15, 2011
 * @author GreenVulcano Developer Team
 */
public class OpenSpCoopCallOperation implements CallOperation
{


    private static Logger       logger                  = GVLogger.getLogger(OpenSpCoopCallOperation.class);

    private OperationKey        key                     = null;
    private String              url                     = null;
    private String              username                = null;
    private String              password                = null;
    private String              comando                 = null;
    private String              tipoDestinatario        = null;
    private String              destinatario            = null;
    private String              tipoServizio            = null;
    private String              servizio                = null;
    private String              azione                  = null;
    private String              nomeServizioApplicativo = null;
    private String              imbustamento            = null;
    private String              locationPD              = null;
    private String              collaborazione          = null;
    private static final String ID_APPLICATIVO          = "ID_APPLICATIVO";
    private static final String ID_RIFERIMENTO          = "ID_RIFERIMENTO";
    private static final String COUNTER                 = "COUNTER";
    private static final String OFFSET                  = "OFFSET";
    private static final String ID_MESSAGGIO            = "ID_MESSAGGIO";
    private static final String TIPO_DESTINATARIO       = "TIPO_DESTINATARIO";
    private static final String TIPO_MITTENTE           = "TIPO_MITTENTE";
    private static final String ID_COLLABORAZIONE       = "ID_COLLABORAZIONE";
    private static final String TIPO_SERVIZIO           = "TIPO_SERVIZIO";
    private static final String SERVIZIO_APPLICATIVO    = "SERVIZIO_APPLICATIVO";
    private static final String IMBUSTAMENTO            = "IMBUSTAMENTO";
    private static final String RIFERIMENTO_MSG         = "RIFERIMENTO_MSG";
    private static final String DESTINATARIO            = "DESTINATARIO";
    private static final String AZIONE                  = "AZIONE";
    private static final String SERVIZIO                = "SERVIZIO";
    private static final String MITTENTE                = "MITTENTE";
    private static final String LOCATION_PD             = "LOCATION_PD";

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        logger.debug("Init start");
        try {
            url = XMLConfig.get(node, "@url");
            username = XMLConfig.get(node, "@username");
            password = XMLConfig.get(node, "@password");
            comando = XMLConfig.get(node, "@comando");
            tipoDestinatario = XMLConfig.get(node, "@tipoDestinatario");
            destinatario = XMLConfig.get(node, "@destinatario");
            servizio = XMLConfig.get(node, "@servizio");
            azione = XMLConfig.get(node, "@azione");
            nomeServizioApplicativo = XMLConfig.get(node, "@servizioApplicativo");
            imbustamento = XMLConfig.get(node, "@imbustamento");
            locationPD = XMLConfig.get(node, "@locationPD");
            collaborazione = XMLConfig.get(node, "@idCollaborazione");
            logger.debug("init - loaded parameters: url = " + url);

            logger.debug("Init stop");
        }
        catch (Exception exc) {
            throw new InitializationException("GV_INIT_SERVICE_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb
     * .buffer.GVBuffer)
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        Object gvobj = gvBuffer.getObject();
        SOAPMessage soapMessage = null;
        SOAPMessage message = null;
        try {
            logger.debug("Request String Message:\n" + gvBuffer.getObject());
            IntegrationManagerServiceLocator locator = new IntegrationManagerServiceLocator();
            locator.setIntegrationManagerEndpointAddress(url);
            IntegrationManager port = locator.getIntegrationManager();
            if ((username != null) && (password != null)) {
                // to use Basic HTTP Authentication:
                ((Stub) port)._setProperty(javax.xml.rpc.Call.USERNAME_PROPERTY, username);
                ((Stub) port)._setProperty(javax.xml.rpc.Call.PASSWORD_PROPERTY, password);
            }
            if (gvBuffer.getProperty(LOCATION_PD) != null) {
                locationPD = gvBuffer.getProperty(LOCATION_PD);
            }
            msgValidate(gvBuffer);
            SPCoopHeaderInfo spcoopHeaderInfo = new SPCoopHeaderInfo();
            if (gvBuffer.getProperty(TIPO_DESTINATARIO) != null) {
                spcoopHeaderInfo.setTipoDestinatario(gvBuffer.getProperty(TIPO_DESTINATARIO));
            }
            else {
                spcoopHeaderInfo.setTipoDestinatario(tipoDestinatario);
            }

            if (gvBuffer.getProperty(DESTINATARIO) != null) {
                spcoopHeaderInfo.setDestinatario(gvBuffer.getProperty(DESTINATARIO));
            }
            else {
                spcoopHeaderInfo.setDestinatario(destinatario);
            }

            if (gvBuffer.getProperty(TIPO_SERVIZIO) != null) {
                spcoopHeaderInfo.setTipoServizio(gvBuffer.getProperty(TIPO_SERVIZIO));
            }
            else {
                spcoopHeaderInfo.setTipoServizio(tipoServizio);
            }

            if (gvBuffer.getProperty(SERVIZIO) != null) {
                spcoopHeaderInfo.setServizio(gvBuffer.getProperty(SERVIZIO));
            }
            else {
                spcoopHeaderInfo.setServizio(servizio);
            }

            if (gvBuffer.getProperty(AZIONE) != null) {
                spcoopHeaderInfo.setAzione(gvBuffer.getProperty(AZIONE));
            }
            else {
                spcoopHeaderInfo.setAzione(azione);
            }

            if (gvBuffer.getProperty(ID_COLLABORAZIONE) != null) {
                spcoopHeaderInfo.setIdCollaborazione(gvBuffer.getProperty(ID_COLLABORAZIONE));
            }
            else {
                spcoopHeaderInfo.setIdCollaborazione(collaborazione);
            }

            String riferimentoMessaggio = gvBuffer.getProperty(RIFERIMENTO_MSG);
            spcoopHeaderInfo.setRiferimentoMessaggio(riferimentoMessaggio);

            SPCoopMessage msg = new SPCoopMessage();
            if (gvBuffer.getProperty(IMBUSTAMENTO) != null) {
                msg.setImbustamento(Boolean.parseBoolean(gvBuffer.getProperty(IMBUSTAMENTO)));
            }
            else {
                msg.setImbustamento(Boolean.parseBoolean(imbustamento));
            }
            String idApplicativo = gvBuffer.getProperty(ID_APPLICATIVO);
            msg.setIDApplicativo(idApplicativo);
            if (gvBuffer.getProperty(SERVIZIO_APPLICATIVO) != null) {
                msg.setServizioApplicativo(nomeServizioApplicativo);
            }
            else {
                msg.setServizioApplicativo(gvBuffer.getProperty(SERVIZIO_APPLICATIVO));
            }
            msg.setSpcoopHeaderInfo(spcoopHeaderInfo);

            logger.debug("Calls =" + gvobj.getClass().getName());
            if (gvobj instanceof java.lang.String) {
                logger.debug("Inizio creazione soap from String\n");
                Document doc = XMLUtils.parseDOM_S((String) gvobj, false, true);
                soapMessage = MessageFactory.newInstance().createMessage();
                SOAPBody body = soapMessage.getSOAPBody();
                body.addDocument(doc);
                soapMessage.saveChanges();
            }
            else if (gvobj instanceof byte[]) {
                logger.debug("Inizio creazione soap from byte[]\n");
                Document doc = XMLUtils.parseDOM_S((byte[]) gvobj, false, true);
                soapMessage = MessageFactory.newInstance().createMessage();
                SOAPBody body = soapMessage.getSOAPBody();
                body.addDocument(doc);
                soapMessage.saveChanges();
            }
            else if (gvobj instanceof org.w3c.dom.Document) {
                logger.debug("Inizio creazione soap from dom\n");
                soapMessage = MessageFactory.newInstance().createMessage();
                SOAPBody body = soapMessage.getSOAPBody();
                body.addDocument((Document) gvobj);
                soapMessage.saveChanges();
            }
            else if (gvobj instanceof javax.xml.soap.SOAPMessage) {
                logger.debug("Inizio creazione soap from SOAPMessage\n");
                soapMessage = (SOAPMessage) gvobj;

            }
            if (gvobj instanceof org.apache.axiom.soap.SOAPEnvelope) {
                logger.debug("Inizio creazione soap from SOAPEnvelope\n");
                SOAPEnvelope envelope = (SOAPEnvelope) gvobj;
                msg.setMessage(envelope.toString().getBytes());
            }
            else {
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                soapMessage.writeTo(byteBuffer);
                logger.debug("set message\n" + byteBuffer.toString());
                msg.setMessage(byteBuffer.toByteArray());
                logger.debug("MESS=" + soapMessage.toString());
            }

            printSPCoopMessage(msg);
            // invoco porta delegata
            SPCoopMessage msgResponse = callOpenSpcoop(port, msg, gvBuffer, riferimentoMessaggio);

            if (msgResponse != null) {
                setRetProperties(msgResponse, gvBuffer);
                if (msgResponse.getMessage() != null) {
                    String msgOut = new String(msgResponse.getMessage());
                    logger.debug("Response XMLMessage:\n" + msgOut);
                    int idxB = msgOut.indexOf("----=_"); // is multipart?
                    if (idxB != -1) {
                        //List<PartDescriptor> parts = MultipartParser.parseToPartList(msgOut);
                        SOAPMessage soapM = MultipartParser.parseToSOAPMessage(msgOut);
                        gvBuffer.setObject(soapM);
                    }
                    else {
                        MessageFactory messageFactory = MessageFactory.newInstance();
                        message = messageFactory.createMessage();
                        ByteArrayInputStream input = new ByteArrayInputStream(msgResponse.getMessage());
                        StreamSource source = new StreamSource(input);
                        message.getSOAPPart().setContent(source);
                        if (gvobj instanceof java.lang.String) {
                            gvBuffer.setObject(message.getSOAPBody().toString());
                        }
                        else if (gvobj instanceof byte[]) {
                            gvBuffer.setObject(message.getSOAPBody().getFirstChild().toString().getBytes());
                        }
                        else if (gvobj instanceof org.w3c.dom.Document) {
                            Document doc = XMLUtils.parseDOM_S(message.getSOAPBody().getFirstChild().toString(), false,
                                    true);
                            gvBuffer.setObject(doc);
                        }
                        else if (gvobj instanceof javax.xml.soap.SOAPMessage) {
                            gvBuffer.setObject(message);
                        }
                        else if (gvobj instanceof org.apache.axiom.soap.SOAPEnvelope) {
                            StringReader inputString = null;
                            OMNode newElement = null;
                            SOAPEnvelope soapEnvelope = (SOAPEnvelope) gvobj;
                            SOAPFactory factory = (org.apache.axiom.soap.SOAPFactory) soapEnvelope.getOMFactory();
                            SOAPEnvelope newSoapEnvelope = factory.createSOAPEnvelope();
                            if (message.getSOAPHeader() != null) {
                                inputString = new StringReader(message.getSOAPHeader().getFirstChild().toString());
                                newElement = org.apache.axis2.util.XMLUtils.toOM(inputString);
                                factory.createSOAPHeader(newSoapEnvelope).addChild(newElement);
                            }
                            if (message.getSOAPBody() != null) {
                                inputString = new StringReader(message.getSOAPBody().getFirstChild().toString());
                                newElement = org.apache.axis2.util.XMLUtils.toOM(inputString);
                                factory.createSOAPBody(newSoapEnvelope).addChild(newElement);
                            }
                            gvBuffer.setObject(newSoapEnvelope);
                        }
                    }
                }
                else {
                    gvBuffer.setObject(null);
                }
            }
        }
        catch (SPCoopException ex) {
            if (gvobj instanceof org.apache.axiom.soap.SOAPEnvelope) {
                try {
                    SOAPEnvelope soapEnvelope = (SOAPEnvelope) gvobj;
                    SOAPFactory factory = (org.apache.axiom.soap.SOAPFactory) soapEnvelope.getOMFactory();
                    SOAPEnvelope newSoapEnvelope = factory.createSOAPEnvelope();
                    org.apache.axiom.soap.SOAPBody soapBody = factory.createSOAPBody(newSoapEnvelope);
                    soapBody.addFault(ex);
                    gvBuffer.setObject(newSoapEnvelope);
                }
                catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else {
                throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                        {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                        {"message", ex.getMessage()}}, ex);
            }

        }
        catch (Exception exc) {
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
        }
        return gvBuffer;
    }

    private SPCoopMessage callOpenSpcoop(IntegrationManager port, SPCoopMessage msg, GVBuffer gvBuffer,
            String riferimentoMessaggio) throws CallException, SPCoopException, RemoteException
    {
        SPCoopMessage msgResponse = null;
        if (comando.equals("invocazionePortaDelegata")) {
            msgResponse = port.invocaPortaDelegata(locationPD, msg);
        }
        else if (comando.equals("invocazionePortaDelegataPerRiferimento")) {
            String idPerRiferimento = gvBuffer.getProperty(ID_RIFERIMENTO);
            msgResponse = port.invocaPortaDelegataPerRiferimento(locationPD, msg, idPerRiferimento);
        }
        else if (comando.equals("sendRispostaAsincronaSimmetrica")) {
            msgResponse = port.sendRispostaAsincronaSimmetrica(locationPD, msg);
        }
        else if (comando.equals("sendRichiestaStatoAsincronaAsimmetrica")) {
            msgResponse = port.sendRichiestaStatoAsincronaAsimmetrica(locationPD, msg);
        }
        else if (comando.equals("getAllMessagesId")) {
            logger.debug("getAllMessagesId()");
            String[] ids = port.getAllMessagesId();
            logger.debug(ids.length + " messaggi presenti");
            for (int i = 0; i < ids.length; i++) {
                logger.debug("Messaggio num" + (i + 1) + " con ID: " + ids[i]);
            }
        }
        else if (comando.equals("getAllMessagesIdByService")) {
            logger.debug("getAllMessagesIdByService()");
            String[] ids = port.getAllMessagesIdByService(tipoServizio, servizio, azione);
            logger.debug(ids.length + " messaggi presenti");
            for (int i = 0; i < ids.length; i++) {
                logger.debug("Messaggio num" + (i + 1) + " con ID: " + ids[i]);
            }
        }
        else if (comando.equals("getNextMessagesId")) {
            String strCounter = gvBuffer.getProperty(COUNTER);
            int counter = Integer.parseInt(strCounter);
            if (counter <= 0) {
                logger.error("ERROR : Numero di id (counter) non definito");
                throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                        {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                        {"message", "ERROR NUMERO ID NON DEFINITO"}}, null);
            }
            logger.debug("getNextMessagesId()");
            String[] ids = port.getNextMessagesId(counter);
            logger.debug(ids.length + " messaggi presenti");
            for (int i = 0; i < ids.length; i++) {
                logger.debug("Messaggio num" + (i + 1) + " con ID: " + ids[i]);
            }
        }
        // getNextMessagesIdByService
        else if (comando.equals("getNextMessagesIdByService")) {
            String strCounter = gvBuffer.getProperty(COUNTER);
            int counter = Integer.parseInt(strCounter);
            if (counter <= 0) {
                logger.error("ERROR : Numero di id (counter) non definito");
                throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                        {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                        {"message", "ERROR NUMERO ID NON DEFINITO"}}, null);
            }
            logger.debug("getNextMessagesIdByService()");
            String[] ids = port.getNextMessagesIdByService(counter, tipoServizio, servizio, azione);
            logger.debug(ids.length + " messaggi presenti");
            for (int i = 0; i < ids.length; i++) {
                logger.debug("Messaggio num" + (i + 1) + " con ID: " + ids[i]);
            }
        }
        // getMessagesIdArray
        else if (comando.equals("getMessagesIdArray")) {
            String strCounter = gvBuffer.getProperty(COUNTER);
            int counter = Integer.parseInt(strCounter);
            if (counter <= 0) {
                logger.error("ERROR : Numero di id (counter) non definito");
                throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                        {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                        {"message", "ERROR NUMERO ID NON DEFINITO"}}, null);
            }
            String strOffset = gvBuffer.getProperty(OFFSET);
            int offset = Integer.parseInt(strOffset);
            if (offset < 0) {
                logger.error("ERROR : OFFSET non definito");
                throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                        {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                        {"message", "ERROR OFFSET NON DEFINITO"}}, null);
            }
            logger.debug("getMessagesIdArray()");
            String[] ids = port.getMessagesIdArray(offset, counter);
            logger.debug(ids.length + " messaggi presenti");
            for (int i = 0; i < ids.length; i++) {
                logger.debug("Messaggio num" + (i + 1) + " con ID: " + ids[i]);
            }
        }
        // getMessagesIdArrayByService
        else if (comando.equals("getMessagesIdArrayByService")) {
            String strCounter = gvBuffer.getProperty(COUNTER);
            int counter = Integer.parseInt(strCounter);
            if (counter <= 0) {
                logger.error("ERROR : Numero di id (counter) non definito");
                throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                        {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                        {"message", "ERROR NUMERO ID NON DEFINITO"}}, null);
            }
            String strOffset = gvBuffer.getProperty(OFFSET);
            int offset = Integer.parseInt(strOffset);
            if (offset < 0) {
                logger.error("ERROR : OFFSET non definito");
                throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                        {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                        {"message", "ERROR OFFSET NON DEFINITO"}}, null);
            }
            logger.debug("getMessagesIdArrayByService()");
            String[] ids = port.getMessagesIdArrayByService(offset, counter, tipoServizio, servizio, azione);
            logger.debug(ids.length + " messaggi presenti");
            for (int i = 0; i < ids.length; i++) {
                logger.debug("Messaggio num" + (i + 1) + " con ID: " + ids[i]);
            }
        }
        // getMessage
        else if (comando.equals("getMessage")) {
            String idMessaggio = gvBuffer.getProperty(ID_MESSAGGIO);
            if (idMessaggio == null) {
                logger.error("ERROR : id EGov non definito");
                throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                        {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                        {"message", "id EGov NON DEFINITO"}}, null);
            }
            logger.debug("getMessage()");
            msgResponse = port.getMessage(idMessaggio);
        }
        // getMessageByReference
        else if (comando.equals("getMessageByReference")) {
            if (riferimentoMessaggio == null) {
                logger.error("ERROR : Riferimento Messaggio non definito");
                throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                        {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                        {"message", "Riferimento Messaggio NON DEFINITO"}}, null);
            }
            logger.debug("getMessageByReference()");
            msgResponse = port.getMessageByReference(riferimentoMessaggio);
        }
        // deleteMessage
        else if (comando.equals("deleteMessage")) {
            String idMessaggio = gvBuffer.getProperty("ID_MESSAGGIO");
            if (idMessaggio == null) {
                System.out.println("ERROR : id EGov non definito all'interno del file 'IntegrationManagerClient.properties'");
            }
            logger.debug("deleteMessage()");
            port.deleteMessage(idMessaggio);
        }
        // deleteMessageByReference
        else if (comando.equals("deleteMessageByReference")) {
            if (riferimentoMessaggio == null) {
                System.out.println("ERROR : Riferimento Messaggio non definito all'interno del file 'IntegrationManagerClient.properties'");
            }
            logger.debug("deleteMessageByReference()");
            port.deleteMessageByReference(riferimentoMessaggio);
        }
        // deleteMessages
        else if (comando.equals("deleteAllMessages")) {
            logger.debug("deleteAllMessages()");
            port.deleteAllMessages();
        }
        else {
            logger.error("ERROR, comando non definito");
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                    {"message", "ERROR COMANDO NON DEFINITO"}}, null);
        }
        return msgResponse;
    }


    private void setRetProperties(SPCoopMessage msgResponse, GVBuffer gvBuffer) throws GVException
    {
        logger.debug("------------ SPCoop risposta ------------");
        logger.debug("ID APPLICATIVO: " + msgResponse.getIDApplicativo());
        if (msgResponse.getIDApplicativo() != null) {
            gvBuffer.setProperty(SERVIZIO_APPLICATIVO, msgResponse.getIDApplicativo());
        }
        logger.debug("SERVIZIO APPLICATIVO: " + msgResponse.getServizioApplicativo());
        if (msgResponse.getServizioApplicativo() != null) {
            gvBuffer.setProperty(ID_APPLICATIVO, msgResponse.getServizioApplicativo());
        }
        logger.debug("IMBUSTAMENTO: " + msgResponse.getServizioApplicativo());
        gvBuffer.setProperty(IMBUSTAMENTO, Boolean.toString(msgResponse.isImbustamento()));

        SPCoopHeaderInfo spcoopHeaderInfoRisposta = msgResponse.getSpcoopHeaderInfo();
        logger.debug("------------ SPCoopHeaderInfo risposta ------------");

        if (spcoopHeaderInfoRisposta == null) {
            logger.debug("Non sono presenti informazioni.");
        }
        else {
            logger.debug("Azione: " + spcoopHeaderInfoRisposta.getAzione());
            if (spcoopHeaderInfoRisposta.getAzione() != null) {
                gvBuffer.setProperty(AZIONE, spcoopHeaderInfoRisposta.getAzione());
            }
            logger.debug("Destinatario: " + spcoopHeaderInfoRisposta.getDestinatario());
            if (spcoopHeaderInfoRisposta.getDestinatario() != null) {
                gvBuffer.setProperty(DESTINATARIO, spcoopHeaderInfoRisposta.getDestinatario());
            }
            logger.debug("Mittente: " + spcoopHeaderInfoRisposta.getMittente());
            if (spcoopHeaderInfoRisposta.getMittente() != null) {
                gvBuffer.setProperty(MITTENTE, spcoopHeaderInfoRisposta.getMittente());
            }
            logger.debug("Riferimento Messaggio: " + spcoopHeaderInfoRisposta.getMittente());
            if (spcoopHeaderInfoRisposta.getRiferimentoMessaggio() != null) {
                gvBuffer.setProperty(RIFERIMENTO_MSG, spcoopHeaderInfoRisposta.getRiferimentoMessaggio());
            }
            logger.debug("Identificativo Collaborazione: " + spcoopHeaderInfoRisposta.getIdCollaborazione());
            if (spcoopHeaderInfoRisposta.getIdCollaborazione() != null) {
                gvBuffer.setProperty(ID_COLLABORAZIONE, spcoopHeaderInfoRisposta.getIdCollaborazione());
            }
            logger.debug("Identificativo e-Gov: " + spcoopHeaderInfoRisposta.getID());
            if (spcoopHeaderInfoRisposta.getID() != null) {
                gvBuffer.setProperty(ID_MESSAGGIO, spcoopHeaderInfoRisposta.getID());
            }
            logger.debug("TipoMittente: " + spcoopHeaderInfoRisposta.getTipoMittente());
            if (spcoopHeaderInfoRisposta.getTipoMittente() != null) {
                gvBuffer.setProperty(TIPO_MITTENTE, spcoopHeaderInfoRisposta.getTipoMittente());
            }
            logger.debug("TipoDestinatario: " + spcoopHeaderInfoRisposta.getTipoDestinatario());
            if (spcoopHeaderInfoRisposta.getTipoDestinatario() != null) {
                gvBuffer.setProperty(TIPO_DESTINATARIO, spcoopHeaderInfoRisposta.getTipoDestinatario());
            }
            logger.debug("TipoServizio: " + spcoopHeaderInfoRisposta.getTipoServizio());
            if (spcoopHeaderInfoRisposta.getTipoServizio() != null) {
                gvBuffer.setProperty(TIPO_SERVIZIO, spcoopHeaderInfoRisposta.getTipoServizio());
            }
            logger.debug("Servizio: " + spcoopHeaderInfoRisposta.getServizio());
            if (spcoopHeaderInfoRisposta.getServizio() != null) {
                gvBuffer.setProperty(SERVIZIO, spcoopHeaderInfoRisposta.getServizio());
            }
        }

    }

    private void printSPCoopMessage(SPCoopMessage msg)
    {
        logger.debug("------------ SPCoop  ------------");
        logger.debug("MSG APPLICATIVO: " + new String(msg.getMessage()));
        logger.debug("ID APPLICATIVO: " + msg.getIDApplicativo());
        logger.debug("SERVIZIO APPLICATIVO: " + msg.getServizioApplicativo());
        logger.debug("IMBUSTAMENTO: " + msg.getServizioApplicativo());

        SPCoopHeaderInfo spcoopHeaderInfoRisposta = msg.getSpcoopHeaderInfo();
        logger.debug("------------ SPCoopHeaderInfo ------------");

        if (spcoopHeaderInfoRisposta == null) {
            logger.debug("Non sono presenti informazioni.");
        }
        else {
            if (spcoopHeaderInfoRisposta.getAzione() != null) {
                logger.debug("Azione: " + spcoopHeaderInfoRisposta.getAzione());
            }
            logger.debug("Destinatario: " + spcoopHeaderInfoRisposta.getDestinatario());

            logger.debug("Mittente: " + spcoopHeaderInfoRisposta.getMittente());
            if (spcoopHeaderInfoRisposta.getRiferimentoMessaggio() != null) {
                logger.debug("Riferimento Messaggio: " + spcoopHeaderInfoRisposta.getRiferimentoMessaggio());
            }
            if (spcoopHeaderInfoRisposta.getIdCollaborazione() != null) {
                logger.debug("Identificativo Collaborazione: " + spcoopHeaderInfoRisposta.getIdCollaborazione());
            }
            logger.debug("Identificativo e-Gov: " + spcoopHeaderInfoRisposta.getID());
            logger.debug("TipoMittente: " + spcoopHeaderInfoRisposta.getTipoMittente() + "   Mittente: "
                    + spcoopHeaderInfoRisposta.getMittente());
            logger.debug("TipoDestinatario: " + spcoopHeaderInfoRisposta.getTipoDestinatario());
            logger.debug("TipoServizio: " + spcoopHeaderInfoRisposta.getTipoServizio());
            logger.debug("Servizio: " + spcoopHeaderInfoRisposta.getServizio());
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy()
    {
        // do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.gvesb.virtual.Operation#getServiceAlias(it.greenvulcano
     * .gvesb.buffer.GVBuffer)
     */
    @Override
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.
     * virtual.OperationKey)
     */
    @Override
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    @Override
    public OperationKey getKey()
    {
        return key;
    }

    private void msgValidate(GVBuffer gvBuffer) throws CallException
    {
        Object msg = gvBuffer.getObject();
        String messaggio = null;
        if (("invocazionePortaDelegata".equals(comando)) && (msg == null)) {
            logger.error("ERROR : Messaggio di richiesta del Servizio non definito");
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                    {"message", "Messaggio di richiesta del Servizio non definito"}}, null);
        }
        if (("invocazionePortaDelegata".equals(comando)) && (locationPD == null)) {
            messaggio = "ERROR : porta delegata non definita";
        }

        // Parametri invocazione porta delegata per riferimento
        String idPerRiferimento = gvBuffer.getProperty("ID_PER_RIFERIMENTO");
        if (("invocazionePortaDelegataPerRiferimento".equals(comando)) && (idPerRiferimento == null)) {
            messaggio = "ERROR : Identificatore Messaggio (getMessageByReference) non definito";
        }
        if (("invocazionePortaDelegataPerRiferimento".equals(comando)) && (locationPD == null)) {
            messaggio = "ERROR : porta delegata non definita";
        }

        // Parametri invocazione sendRispostaAsincrona
        if (("sendRispostaAsincronaSimmetrica".equals(comando)) && (msg == null)) {
            messaggio = "ERROR : Messaggio di richiesta del Servizio";
        }
        if (("sendRispostaAsincronaSimmetrica".equals(comando)) && (locationPD == null)) {
            messaggio = "ERROR : porta delegata non definita";
        }
        String riferimentoMessaggio = gvBuffer.getProperty("RIFERIMENTO_MSG");
        if (("sendRispostaAsincronaSimmetrica".equals(comando)) && (riferimentoMessaggio == null)) {
            messaggio = "ERROR : Identificatore di Correlazione non definito";
        }

        if (("sendRichiestaStatoAsincronaAsimmetrica".equals(comando)) && (msg == null)) {
            messaggio = "ERROR : Messaggio di richiesta del Servizio non definito";
        }
        if (("sendRichiestaStatoAsincronaAsimmetrica".equals(comando)) && (locationPD == null)) {
            messaggio = "ERROR : porta delegata non definita";
        }
        if (("sendRichiestaStatoAsincronaAsimmetrica".equals(comando)) && (riferimentoMessaggio == null)) {
            messaggio = "ERROR : Identificatore di Correlazione non definito";
        }
        if ((username == null)
                && (("invocazionePortaDelegata".equals(comando) == false)
                        && ("invocazionePortaDelegataPerRiferimento".equals(comando) == false)
                        && ("sendRispostaAsincronaSimmetrica".equals(comando) == false) && ("sendRichiestaStatoAsincronaAsimmetrica".equals(comando) == false))) {
            messaggio = "ERROR : Username non definito";
        }
        if ((password == null)
                && (("invocazionePortaDelegata".equals(comando) == false)
                        && ("invocazionePortaDelegataPerRiferimento".equals(comando) == false)
                        && ("sendRispostaAsincronaSimmetrica".equals(comando) == false) && ("sendRichiestaStatoAsincronaAsimmetrica".equals(comando) == false))) {
            messaggio = "ERROR : Password non definita";
        }
        if (messaggio != null) {
            logger.error("messaggio");
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()}, {"message", messaggio}},
                    null);
        }

    }

}
