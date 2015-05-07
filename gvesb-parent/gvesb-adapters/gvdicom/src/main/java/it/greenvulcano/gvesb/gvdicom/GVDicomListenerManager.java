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

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.gvdicom.listener.DicomAssociateRequestHandler;
import it.greenvulcano.gvesb.gvdicom.listener.DicomListener;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.thread.BaseThread;

import org.dicom4j.network.association.AssociationAcceptor;
import org.dicom4j.network.association.AssociationAcceptorConfiguration;
import org.dicom4j.network.association.support.AssociationAcceptorConfigurationImpl;
import org.dicom4j.network.association.support.AssociationAcceptorImpl;
import org.dolmen.network.transport.acceptor.TransportAcceptor;
import org.dolmen.network.transport.acceptor.TransportAcceptorConfiguration;
import org.apache.log4j.Logger;

/**
 * 
 * @version 3.5.0 Sep 25, 2014
 * @author GreenVulcano Developer Team*
 */
public class GVDicomListenerManager implements ConfigurationListener, ShutdownEventListener
{
    private static Logger        			logger       	= GVLogger.getLogger(GVDicomListenerManager.class);

    private static GVDicomListenerManager 	instance        = null;
    
    private int 							lport;
    
    
    public static synchronized GVDicomListenerManager instance() throws DicomAdapterException
    {
        if (instance == null) {
            instance = new GVDicomListenerManager();
            instance.init();
        }

        return instance;
    }

    /**
     *
     */
    private GVDicomListenerManager() throws DicomAdapterException
    {
        XMLConfig.addConfigurationListener(this, Constants.CONF_FILE_NAME);
        ShutdownEventLauncher.addEventListener(this);
    }
    
    /**
     *
     */
    private void init() throws DicomAdapterException {
    	lport = XMLConfig.getInteger(Constants.CONF_FILE_NAME, "/GVDicomAdapterConfiguration/Local/@listenerPort", Constants.DEF_LISTENER_PORT);
    	
    	TransportAcceptorConfiguration lTransportAcceptorConfiguration = new TransportAcceptorConfiguration();
     	TransportAcceptor lAcceptor = new TransportAcceptor();
     	lAcceptor.setConfiguration(lTransportAcceptorConfiguration);
     	
     	AssociationAcceptorConfiguration lAssocConfig = new AssociationAcceptorConfigurationImpl();
     	lAssocConfig.setAssociateRequestHandler(new DicomAssociateRequestHandler());
     	lAssocConfig.setAssociationListener(new DicomListener());
     	lAssocConfig.setTransportAcceptor(lAcceptor);
     	
     	AssociationAcceptor lAssoc = new AssociationAcceptorImpl();
     	lAssoc.setConfiguration(lAssocConfig);
     	
     	try {
			lAssoc.bind(lport);
			logger.info("waiting associations on port : " + lport);
			
		} catch (Exception exc) {
			throw new DicomAdapterException("GVDICOM_LISTENER_BIND_ERROR [port: " + lport + "]",
					new String[][]{{"message", exc.getMessage()}}, exc);
		}
    }
    
    
    /*
     * 
     */
    public void destroy()
    {
        logger.debug("BEGIN - Destroing GVDicomListenerManager");
        
        try {
			
        }
        catch (Exception exc) {
            logger.warn("Error while shutting down DICOM", exc);
        }
        
        logger.debug("END - Destroing GVDicomListenerManager");
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
            Runnable rr = new Runnable() {
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
                        logger.error("Error initializing GVDicomListenerManager", exc);
                    }
                }
            };

            BaseThread bt = new BaseThread(rr, "Config reloader for GVDicomListenerManager");
            bt.setDaemon(true);
            bt.start();
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
