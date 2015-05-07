/*
 * Copyright (c) 2009-2015 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.gvdicom;

import java.util.HashMap;
import java.util.Map;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.gvdicom.listener.DicomAssociationListener;
import it.greenvulcano.gvesb.gvdicom.support.Association;
import it.greenvulcano.log.GVLogger;

import org.dicom4j.dicom.uniqueidentifiers.TransferSyntax;
import org.dicom4j.network.association.AssociationConnector;
import org.dicom4j.network.association.AssociationConnectorConfiguration;
import org.dicom4j.network.association.associate.AssociatePrimitiveFactory;
import org.dicom4j.network.association.associate.AssociateRequest;
import org.dicom4j.network.association.support.AssociationConnectorConfigurationImpl;
import org.dicom4j.network.association.support.AssociationConnectorImpl;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.apache.log4j.Logger;

/**
 * 
 * @version 3.5.0 Sep 25, 2014
 * @author GreenVulcano Developer Team*
 */
public class GVDicomAssociationManager implements ConfigurationListener, ShutdownEventListener
{
    private static Logger        				logger         		= GVLogger.getLogger(GVDicomAssociationManager.class);

    private static GVDicomAssociationManager 	instance        	= null;
    
    private boolean                             configOK            = false;
    
    private AssociatePrimitiveFactory 			associateFactroy 	= new AssociatePrimitiveFactory();
    
    private AssociationConnector 				connector 			= new AssociationConnectorImpl();
    
    private Map<String, Association>          	associations		= new HashMap<String, Association>();	
    

    public static synchronized GVDicomAssociationManager instance() throws DicomAdapterException
    {
        if (instance == null) {
            instance = new GVDicomAssociationManager();
            instance.init();
        }

        return instance;
    }

    /**
     *
     */
    private GVDicomAssociationManager() throws DicomAdapterException
    {
        XMLConfig.addConfigurationListener(this, Constants.CONF_FILE_NAME);
        ShutdownEventLauncher.addEventListener(this);
    }
    

    
    /*
     * Method for the creation of the association request
     */
    protected AssociateRequest createRequest(String sopClass) throws Exception {
		
    	AssociateRequest lRequest = this.associateFactroy.newAssociateRequest();
		lRequest.setMaximumPDUSize(60000);
		lRequest.setMaximumNumberOperationsInvoked(4);
		lRequest.setMaximumNumberOperationsPerformed(4);
		lRequest.addPresentationContext(sopClass, TransferSyntax.Default);
		
		return lRequest;
	}
    
    /*
     * Method for the creation of the association via socket
     */
    public void createAssociation(String nameAssociation, String sopClass, DicomAssociationListener dal) throws Exception, DicomAdapterException {
    	init();
    	Association ass = this.associations.get(nameAssociation);
    	if (ass == null) {
    		logger.error("Host "+ nameAssociation + " isn't present in configuration");
    		throw new DicomAdapterException("Host "+ nameAssociation + " isn't present in configuration");
    	}
    	try {
    		AssociationConnectorConfiguration lConfig = new AssociationConnectorConfigurationImpl();
            lConfig.setAssociationListener(dal);
    		this.connector.setConfiguration(lConfig);
       		this.connector.connect(ass.getHost(), ass.getPort(), this.createRequest(sopClass));
    	} catch (Exception exc) {
    		throw new DicomAdapterException("GVDICOM_CREATE_ASSOCIATION_ERROR [host: " + ass.getHost() + " port: " + ass.getPort() + " sopClass: " + sopClass + "]",
    				new String[][]{{"message", exc.getMessage()}}, exc);
    	}
    }
    
    /*
     * Method for initialize the association map
     */
    private synchronized void init()  throws DicomAdapterException {
    	if (configOK) return;
    	
    	try {
    		NodeList nlAss = XMLConfig.getNodeList(Constants.CONF_FILE_NAME, "/GVDicomAdapterConfiguration/AssociationList/Association");
    		
    		for (int i=0; i < nlAss.getLength(); i++) {
    			Node node = nlAss.item(i);
    			String name = XMLConfig.get(node, "@name");
	    		String host = XMLConfig.get(node, "@host");
	    		int port = XMLConfig.getInteger(node, "@port");	
	    		
	    		Association asso = new Association(name,host,port);
	    		associations.put(name, asso);
    		}
    		configOK = true;
    	} catch (Exception exc) {
    		logger.error("Error initializing Association Map: ", exc);
    		throw new DicomAdapterException("GVDICOM_ASS_INIT_ERROR", exc);
    	}
    }

    /*
     * 
     */
    public void destroy()
    {
        logger.debug("BEGIN - Destroing GVDicomAssociationManager");
     
       this.associations.clear();
       this.configOK = false;
        
        logger.debug("END - Destroing GVDicomAssociationManager");
    }

    
    
    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.configuration.ConfigurationListener#configurationChanged
     * (it.greenvulcano.configuration.ConfigurationEvent)
     */
    @Override
    public void configurationChanged(ConfigurationEvent evt)
    {
        logger.debug("BEGIN - Operation(reload Configuration)");
        if ((evt.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && evt.getFile().equals(Constants.CONF_FILE_NAME)) {
            destroy();
            // initialize after a delay
            /*Runnable rr = new Runnable() {
                @Override
                public void run()
                {
                    try {
                        Thread.sleep(30000);
                    }
                    catch (InterruptedException exc) {
                        // do nothing
                    }
                    try {
                        init();
                    }
                    catch (Exception exc) {
                        logger.error("Error initializing GVDicomAssociationManager", exc);
                    }
                }
            };

            BaseThread bt = new BaseThread(rr, "Config reloader for GVDicomAssociationManager");
            bt.setDaemon(true);
            bt.start();*/
        }
        logger.debug("END - Operation(reload Configuration)");

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.event.util.shutdown.ShutdownEventListener#shutdownStarted
     * (it.greenvulcano.event.util.shutdown.ShutdownEvent)
     */
    @Override
    public void shutdownStarted(ShutdownEvent event)
    {
    	XMLConfig.removeConfigurationListener(this);
        destroy();
    }

}
