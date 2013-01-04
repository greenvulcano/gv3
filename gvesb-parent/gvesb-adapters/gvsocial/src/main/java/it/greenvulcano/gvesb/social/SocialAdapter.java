/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.social;


import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * Represents a Social Adapter towards a Social platform. Contains all
 * the Account configured for that platform.
 * 
 * @version 3.3.0 Sep, 2012
 * @author mb
 * 
 * 
 */
public abstract class SocialAdapter {
	
	private static Logger logger = GVLogger.getLogger(SocialAdapter.class);
	
	/**
	 * Initialization method.
	 * 
	 * @param in
	 * @throws SocialAdapterException 
	 */
	public abstract void init(Node in) throws SocialAdapterException;
	
	/**
	 * Returns the account object already initialized, given the account name.
	 * @param accountName
	 * @return
	 * @throws SocialAdapterException
	 */
	public abstract SocialAdapterAccount getAccount(String accountName) throws SocialAdapterException;
	
	public void directExecute(SocialOperation op) throws SocialAdapterException{
		op.execute(getAccount(op.getAccountName()));
	}

	/**
	 * Executes and build the response. The response XML has the following structure:
	 * <br><br>
	 * &lt;SocialServiceResponse&gt; <br>
	 *   &lt;operationName account="[accountName]"&gt; <br>
	 *      &lt;result type="[java.lang.String]"&gt;[result text/value]&lt;/result&gt; <br>
	 *      &lt;error&gt;&lt;/error&gt; <br>
	 *   &lt;/operationName&gt; <br>
	 *   &lt;flagGlobalErrors/&gt; <br>
	 * &lt;/SocialServiceResponse&gt; <br>

	 * @param buffer
	 * @return
	 * @throws SocialAdapterException 
	 */
	protected GVBuffer execute(GVBuffer buffer) throws SocialAdapterException {
		GVBuffer output = null;
		XMLUtils parser = null;
		try {
			output = buffer;
			// ogni nodo corrisponde ad un'operazione su un account e contiene i sottonodi attribute
			Node input = XMLUtils.parseObject_S(buffer.getObject(), false, true);
			NodeList operations = (NodeList) XMLConfig.getNodeList(input, "/*/*[@account]") ;
			SocialAdapterAccount account;
			// Map per memorizzare i metodi evitando di riusare la reflection
			Map<String, Method> methods = new HashMap<String, Method>(); 
			// creazione oggetto per l'XML di risposta
		    parser = XMLUtils.getParserInstance();
			Document doc = parser.newDocument("SocialServiceResponse");
			Element rootElement = doc.getDocumentElement();
			for (int i = 0; i < operations.getLength(); i++){
				Node actualNode = operations.item(i);
				String operationName = actualNode.getNodeName();
				// selezione del nome account
				logger.debug("Operation: " + operationName);
				String acc = XMLConfig.get(actualNode, "@account");
				logger.debug("Account: " + acc);
				Object[] parameterValues;
				Object proxyObject;
				// ricerca nella Map
				Method method = null;
				try {
					account = getAccount(acc);
					// selezione di tutti i nodi <attribute>
					NodeList params = XMLConfig.getNodeList(actualNode, "attribute");
					Class[] parameterTypes = new Class[params.getLength()];
					parameterValues = new Object[params.getLength()];
					// lettura delle classi degli attributi
					for (int j = 0; j < params.getLength(); j++){
						parameterTypes[j] = Class.forName(XMLConfig.get(params.item(j), "@type"));
						parameterValues[j] = XMLConfig.get(params.item(j), ".");
					}
					logger.debug("Parameters: " + Arrays.toString(parameterTypes));
					logger.debug("Parameters values: " + Arrays.toString(parameterValues));
					proxyObject = account.getProxyObject();
					logger.info("Invoking operation on " + proxyObject.getClass().getName());
					method = (Method) methods.get(operationName);
					if (method == null){
						// chiamata al metodo con passaggio dei parametri
						method = proxyObject.getClass().getMethod(operationName, parameterTypes);
						method.setAccessible(true);
						methods.put(operationName, method);
					}
				} catch (Exception e) {
			    	logger.error("Error calling operation.", e);
			    	createResponse(parser, doc, rootElement, acc, operationName,
							e);
			    	continue;
				}
		    	logger.debug("Calling " + method.getName());
		    	createResponse(parser, doc, rootElement, acc, parameterValues,
						proxyObject, method);
		    	logger.debug("Call to "  + method.getName() + " OK");
			}
			output.setObject(doc);
		} catch (Exception e) {
			logger.error(e);
			throw new SocialAdapterException("Call to Social Adapter[" + getSocialName() +"] failed.", e);
		} finally {
		    XMLUtils.releaseParserInstance(parser);
		}
		return output;
	}
	
	private void createResponse(XMLUtils parser, Document doc,
			Element rootElement, String acc, String operationName, Exception e) throws XMLUtilsException {
		// elemento relativo all'operazione
		Element operationNode = parser.insertElement(rootElement, operationName);
		parser.setAttribute(operationNode, "account", acc);
		parser.insertText(operationNode, e.getMessage());
		if (!parser.existNode(rootElement, "//flagGlobalErrors")){
			parser.insertElement(rootElement, "flagGlobalErrors");
		}
	}
	
	/**
	 * Returns the name of the social platform.
	 * @return
	 */
	protected abstract String getSocialName();
	
	private void createResponse(XMLUtils parser, Document doc, Element rootElement, String acc,
			Object[] parameterValues, Object proxyObject, Method method) throws XMLUtilsException {
		String error = null;
		Object toReturn = null;
		boolean hasErrors = false;
		try{
			// invocazione metodo
			toReturn = method.invoke(proxyObject, parameterValues);
		} catch (Exception e) {
			logger.error("Error invoking operation", e);
			hasErrors = true;
			error = new String(e.getMessage());
		} finally{
			// operation element
			Element operationNode = parser.insertElement(rootElement, method.getName());
			parser.setAttribute(operationNode, "account", acc);
			
			// operation result element
			if (toReturn instanceof Object){
		    	Element resultNode = parser.insertElement(operationNode, "result");
		    	parser.setAttribute(resultNode, "type", toReturn.getClass().getName());
		    	parser.insertText(resultNode, toReturn.toString());
			}
			// element describing the error eventually occurred
			if (error instanceof String){
		    	Element errorNode = parser.insertElement(operationNode, "error");
		    	parser.insertText(errorNode, error);
			}
			if (hasErrors && !parser.existNode(rootElement, "//flagGlobalErrors")){
				parser.insertElement(rootElement, "flagGlobalErrors");
			}
		}
	}
	
    /**
     * Returns the names configured for one account.
     * @param authorized filters the accounts requested by the authorization status.  
     * @return
     */
	public abstract Set<String> getAccountNames(boolean authorized);	

}
