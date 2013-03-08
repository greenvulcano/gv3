/*
 * OpenSPCoop - Implementazione della specifica SPCoop 
 * http://www.openspcoop.org
 * 
 * Copyright (c) 2005-2011 Link.it srl (http://link.it). All rights reserved. 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */



package tests.unit.virtual;

import org.openspcoop.pdd.services.*;

import java.io.*;

import org.apache.axis.client.Stub; 

public class IntegrationManagerClient {

	public static void main (String [] argv) {

		org.openspcoop.pdd.services.SPCoopMessage msg = null;
		IntegrationManager port = null;
		String locationPD = null;
		try {
			String url = "http://localhost:8080/openspcoop/IntegrationManager";
			url = url.trim();

			String msgSoap = "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' xmlns:exam='http://www.openspcoop.org/example'>"+
   "<soapenv:Header>"+   
   "</soapenv:Header>"+
   "<soapenv:Body>"+
   "   <exam:comunicazioneVariazione CF='?'>"+
   "      <nome>dd</nome>"+
   "      <cognome>dd</cognome>"+
   "      <codiceFiscale>dd</codiceFiscale>"+
   "      <nascita>dd</nascita>"+
   "      <statoCivile>dd</statoCivile>"+
   "   </exam:comunicazioneVariazione>"+
   "</soapenv:Body>"+
   "</soapenv:Envelope>";
			msgSoap = msgSoap.trim();

			locationPD ="SPCComune/SPCCentroAnagrafico/SPCComunicazioneVariazione";
			
			locationPD = locationPD.trim();

			//	Dati Header Info SPCoop
			String tipoDestinatario ="SPC";
			if(tipoDestinatario!=null){
				tipoDestinatario = tipoDestinatario.trim();
			}
			String destinatario ="CentroAnagrafico";
			if(destinatario!=null){
				destinatario = destinatario.trim();
			}
			String tipoServizio ="SPC";
			if(tipoServizio!=null){
				tipoServizio = tipoServizio.trim();
			}
			String servizio ="Notifica";
			String azione ="Notifica";
			String username ="Comune_SA";
			if(username!=null){
				username = username.trim();
			}
			String password ="123456";
			if(password!=null){
				password = password.trim();
			}


			// Creo il servizio da invocare
			IntegrationManagerServiceLocator locator = new IntegrationManagerServiceLocator();
			locator.setIntegrationManagerEndpointAddress(url);
			port = locator.getIntegrationManager();
			if(username !=null && password!=null){
				// to use Basic HTTP Authentication: 
				((Stub) port)._setProperty(javax.xml.rpc.Call.USERNAME_PROPERTY, username); 
				((Stub) port)._setProperty(javax.xml.rpc.Call.PASSWORD_PROPERTY, password);  
			}


			// Lettura msg di richiesta
			byte [] b = msgSoap.getBytes();
			System.out.println("invocazionePortaDelegata["+locationPD+"] con msgSOAP: "+new String(b));

			// Costruzione Messaggio SPCoop
			msg = new org.openspcoop.pdd.services.SPCoopMessage();
			msg.setMessage(b);
			
			// Costruzione SPCoopHeaderInfo
			SPCoopHeaderInfo spcoopHeaderInfo = new SPCoopHeaderInfo();
			spcoopHeaderInfo.setTipoDestinatario(tipoDestinatario);
			spcoopHeaderInfo.setDestinatario(destinatario);
			spcoopHeaderInfo.setTipoServizio(tipoServizio);
			spcoopHeaderInfo.setServizio(servizio);
			//spcoopHeaderInfo.setAzione(azione);
			msg.setSpcoopHeaderInfo(spcoopHeaderInfo);

		} catch (Exception e) {
			System.out.println("ClientError: "+e.getMessage());
			//e.printStackTrace();
		}


		// Invocazione PD
		try{
			org.openspcoop.pdd.services.SPCoopMessage msgResponse = port.invocaPortaDelegata(locationPD,msg);
			
			// stampa risposta
			printSPCoopMessage(msgResponse);

		}catch(org.openspcoop.pdd.services.SPCoopException ex){
			System.out.println("Ritornato messaggio di errore applicativo con CodiceEccezione["+ex.getCodiceEccezione()+"] ("+
					ex.getOraRegistrazione()+")\n"+   
					"msg di tipo ["+ex.getTipoEccezione()+"] inviato dal dominio["+ex.getIdentificativoPorta()+"] (mod.fun.["
					+ex.getIdentificativoFunzione()+"]): \n"+
					ex.getDescrizioneEccezione());
		}catch(Exception e){
			System.out.println("ClientError: "+e.getMessage());
			//e.printStackTrace();
		}


	}

	// copy method from From E.R. Harold's book "Java I/O"
	public static void copy(InputStream in, OutputStream out) 
	throws IOException {

		// do not allow other threads to read from the
		// input or write to the output while copying is
		// taking place

		synchronized (in) {
			synchronized (out) {

				byte[] buffer = new byte[256];
				while (true) {
					int bytesRead = in.read(buffer);
					if (bytesRead == -1) break;
					out.write(buffer, 0, bytesRead);
				}
			}
		}
	} 
	
	//	 Stampa risposta
	public static void printSPCoopMessage(SPCoopMessage msg){
		SPCoopHeaderInfo spcoopHeaderInfoRisposta = msg.getSpcoopHeaderInfo();
		System.out.println("------------ SPCoopHeaderInfo risposta ------------");
		if(spcoopHeaderInfoRisposta==null){
			System.out.println("Non sono presenti informazioni.");
		}else{
			System.out.println("TipoMittente: "+spcoopHeaderInfoRisposta.getTipoMittente()+
					"   Mittente: "+spcoopHeaderInfoRisposta.getMittente());
			System.out.println("TipoDestinatario: "+spcoopHeaderInfoRisposta.getTipoDestinatario()+
					"   Destinatario: "+spcoopHeaderInfoRisposta.getDestinatario());
			System.out.println("TipoServizio: "+spcoopHeaderInfoRisposta.getTipoServizio()+
					"   Servizio: "+spcoopHeaderInfoRisposta.getServizio());
			if(spcoopHeaderInfoRisposta.getAzione()!=null){
				System.out.println("Azione: "+spcoopHeaderInfoRisposta.getAzione());
			}
			System.out.println("Identificativo e-Gov: "+spcoopHeaderInfoRisposta.getID());
			if(spcoopHeaderInfoRisposta.getIdCollaborazione()!=null){
				System.out.println("Identificativo Collaborazione: "+spcoopHeaderInfoRisposta.getIdCollaborazione());
			}
			if(spcoopHeaderInfoRisposta.getRiferimentoMessaggio()!=null){
				System.out.println("Riferimento Messaggio: "+spcoopHeaderInfoRisposta.getRiferimentoMessaggio());
			}				
		}
		System.out.println("---------------------------------------------------\n");
		System.out.println("--------------- Risposta applicativa --------------");
		System.out.println(new String(msg.getMessage()));
		System.out.println("---------------------------------------------------");
	}
}
