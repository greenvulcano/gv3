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
package it.greenvulcano.gvesb.virtual.jca;

import it.greenvulcano.log.GVLogger;

import java.beans.Beans;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.rmi.PortableRemoteObject;

import org.apache.log4j.Logger;

/**
 * <p>
 * Title: ClientCCI
 * </p>
 * <p>
 * Description: Libreria di funzioni per l'invocazione di un connettore
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class JCAClientCCI
{

    /** Connector Log */
    private static Logger             logger                     = GVLogger.getLogger(JCAClientCCI.class);

    // ConnectionFactory per la connessione JCA
    private ConnectionFactory         connectionFactory          = null;
    // Connessione con la risorsa EIS
    private Connection                connection                 = null;
    // Nome della classe InteractionSpec
    private String                    classCciInteractionSpec    = null;
    // Nome della classe ConnectionSpec
    private String                    classCciConnectionSpec     = null;
    // Nome della classe ConnectionMetaData
    private String                    classCciConnectionMetaData = null;
    // Nome jndi del connettore JCA
    private String                    jndinameeis                = null;
    private Hashtable<String, String> listparamConnectionSpec    = null;
    // Metadata relativi al recource adapter
    private ResourceAdapterMetaData   raMetaData                 = null;
    // Metadata relativi all EIS
    private ConnectionMetaData        metadata                   = null;
    private Context                   context                    = null;

    /**
     * Costruttore di default
     */
    public JCAClientCCI()
    {
    }

    /**
     * Init Inizializza i parametri del connettore e crea una Connection Factory
     * al Connector
     *
     * @param classCciInteractionSpec
     *        Classe CciInteractionSpec del connettore JCA
     * @param classCciConnectionSpec
     *        Classe CciConnectionSpec del connettore JCA
     * @param classCciConnectionMetaData
     *        Classe CciConnectionMetaData del connettore JCA
     * @param jndinameeis
     *        jndi name dell'eis interfacciato dal connettore
     * @param listparamConnectionSpec
     *        Tabella dei parametri del connettore
     */
    public void init(String classCciInteractionSpec, String classCciConnectionSpec, String classCciConnectionMetaData,
            String jndinameeis, Hashtable<String, String> listparamConnectionSpec)
    {
        this.classCciInteractionSpec = classCciInteractionSpec;
        this.classCciConnectionSpec = classCciConnectionSpec;
        this.classCciConnectionMetaData = classCciConnectionMetaData;
        this.listparamConnectionSpec = listparamConnectionSpec;
        this.jndinameeis = jndinameeis;
    }

    /**
     * createConnection Crea la connessione al connettore
     *
     * @throws ResourceException
     *         Connessione non riuscita
     */
    public void createConnectionFactory() throws ResourceException
    {
        try {
            context = getInitialContext();

            // Recupero del ConnectionFactory
            connectionFactory = getCCIConnectionFactory();

            // Recupero del RA metadata
            raMetaData = createResourceAdapterMetaData();

            logger.debug("End createConnection");
        }
        catch (NamingException exc) {
            logger.error("NamingException in createConnection:", exc);
            throw new ResourceException("Nome errato");
        }
    }

    /**
     * createConnection Crea la connessione al connettore
     *
     * @throws ResourceException
     *         Connessione non riuscita
     */
    public void createConnection() throws ResourceException
    {
        try {
            // si ottiene la connessione alla risorsa EIS
            connection = getCCIConnection();

            // Recupero dei metadata
            metadata = createConnectionMetaData();

            logger.debug("End createConnection");
        }
        catch (IllegalAccessException exc) {
            logger.error("IOException in createConnection:", exc);
            throw new ResourceException("Nome errato:" + exc.getMessage());
        }
        catch (IOException exc) {
            logger.error("IOException in createConnection:", exc);
            throw new ResourceException("Errore IO: " + exc.getMessage());
        }
        catch (ClassNotFoundException exc) {
            logger.error("ResourceException in performRead:", exc);
            throw new ResourceException("Classe inesistente: " + exc.getMessage());
        }
        catch (IntrospectionException exc) {
            logger.error("ResourceException in performRead:", exc);
            throw new ResourceException("Metodo inesistente:" + exc.getMessage());
        }
        catch (InvocationTargetException exc) {
            logger.error("ResourceException in performRead:", exc);
            throw new ResourceException("Errore invocazione metodo:" + exc.getMessage());
        }
    }

    /**
     * Invoca la funzione del connettore e ritorna il buffer di output
     *
     * @param msgIn
     *        buffer di input al connettore
     * @param functionName
     *        nome della funzione da invocare
     * @return Output del connettore
     * @throws ResourceException
     */
    @SuppressWarnings("unchecked")
    public Object callFunctionRecord(Object msgIn, String functionName) throws ResourceException
    {
        logger.debug("Init callFunctionRecord");
        logger.debug("BEGIN callFunctionRecord =" + functionName);
        Object msgOut = null;
        try {
            // Create Interaction
            Interaction ix = connection.createInteraction();

            Class<?> iSpecClass = Class.forName(classCciInteractionSpec);
            Object iSpec = iSpecClass.newInstance();

            Class<?>[] args = {functionName.getClass()};
            Object[] values = {functionName};
            Method metodsetFunctionName = iSpec.getClass().getDeclaredMethod("setFunctionName", args);

            metodsetFunctionName.invoke(iSpec, values);

            RecordFactory rf = connectionFactory.getRecordFactory();

            IndexedRecord iRec = rf.createIndexedRecord("InputRecord");
            iRec.add(msgIn);

            Record oRec = ix.execute((InteractionSpec) iSpec, iRec);

            Iterator<?> iterator = ((IndexedRecord) oRec).iterator();
            int count = 0;
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                ++count;
                logger.debug("Inside while: Cycle [" + count + "] : " + obj + " - " + obj.getClass().getName());
                msgOut = obj;
            }
            logger.debug("Iterator [" + iterator + "]-class[" + iterator.getClass().getName() + "]...");
            ix.close();
        }
        catch (ClassNotFoundException exc) {
            logger.error("ResourceException in performRead: ", exc);
            throw new ResourceException("Classe non trovata:" + exc.getMessage());
        }
        catch (InstantiationException exc) {
            logger.error("ResourceException in performRead: ", exc);
            throw new ResourceException("Istanziazione classe non riuscita: " + exc.getMessage());
        }
        catch (IllegalAccessException exc) {
            logger.error("ResourceException in performRead: ", exc);
            throw new ResourceException("Accesso illegale alla classe: " + exc.getMessage());
        }
        catch (InvocationTargetException exc) {
            logger.error("ResourceException in performRead: ", exc);
            throw new ResourceException("Connessione non raggiungibile: " + exc.getMessage());
        }
        catch (NoSuchMethodException exc) {
            logger.error("ResourceException in performRead: ", exc);
            throw new ResourceException("Metodo inesistente: " + exc.getMessage());
        }

        logger.debug("returning [" + msgOut + "]...");
        logger.debug("End callFunctionRecord");
        return msgOut;
    }

    /**
     * getCCIConnectionFactory Crea la connection Factory all'AS
     *
     * @return the CCI ConnectionFactory
     **/
    private ConnectionFactory getCCIConnectionFactory() throws NamingException
    {
        logger.debug("Init getCCIConnectionFactory");
        logger.debug("Looking up for [" + jndinameeis + "]...");
        ConnectionFactory cf = (ConnectionFactory) PortableRemoteObject.narrow(context.lookup(jndinameeis),
                ConnectionFactory.class);
        logger.debug("End getCCIConnectionFactory");
        return cf;
    }

    /**
     * getCCIConnection ritorna la connessione al connettore JCA
     *
     * @return la connessione alla risorsa EIS
     * @throws IOException
     *         ,
     *         IntrospectionException,ClassNotFoundException,IllegalAccessException
     *         ,InvocationTargetException,ResourceException
     **/
    private Connection getCCIConnection() throws IOException, IntrospectionException, ClassNotFoundException,
            IllegalAccessException, InvocationTargetException, ResourceException
    {
        logger.debug("Init getCCIConnection");
        Connection con = null;
        if (classCciConnectionSpec != null) {
            // Beans beancciConnectionSpec = new Beans();
            ClassLoader cls = Class.forName(classCciConnectionSpec).getClassLoader();
            Object cciConnectionSpec = Beans.instantiate(cls, classCciConnectionSpec);

            Enumeration<String> e = listparamConnectionSpec.keys();
            while (e.hasMoreElements()) {
                String propertyName = e.nextElement();
                String valueName = (String) listparamConnectionSpec.get(propertyName);
                PropertyDescriptor property = new PropertyDescriptor(propertyName, cciConnectionSpec.getClass());
                Class<?> type = property.getPropertyType();
                logger.debug("propertyType = " + type.getName());
                logger.debug("Function: '" + propertyName + "' Param: " + valueName);
                Object value = getObject(type.getName(), valueName);
                logger.debug("value: " + value);
                Method metodsetFunctionName = property.getWriteMethod();
                Object[] values = {value};
                logger.debug("metodsetFunctionName: " + metodsetFunctionName);
                metodsetFunctionName.invoke(cciConnectionSpec, values);
            }
            con = connectionFactory.getConnection((ConnectionSpec) cciConnectionSpec);
        }
        else {
            con = connectionFactory.getConnection();
        }
        logger.debug("End getCCIConnection");
        return con;
    }

    /**
     * chiude la connessione CCI
     **/
    public void closeCCIConnection()
    {
        logger.debug("Init closeCCIConnection");
        try {
            connection.close();
        }
        catch (ResourceException exc) {
            logger.error("ResourceException in performRead: ", exc);
        }
        logger.debug("End closeCCIConnection");
    }

    /**
     * Create i ResourceAdapterMetaData del Connector
     *
     * @return ResourceAdapterMetaData AdapterMetaData del Connector
     * @throws ResourceException
     */
    private ResourceAdapterMetaData createResourceAdapterMetaData() throws ResourceException
    {
        ResourceAdapterMetaData raMetaDataLoc = connectionFactory.getMetaData();
        logger.info("AdpaterName : " + raMetaDataLoc.getAdapterName());
        logger.info("AdapterShortDescription: " + raMetaDataLoc.getAdapterShortDescription());
        logger.info("AdpaterVendorName: " + raMetaDataLoc.getAdapterVendorName());
        logger.info("AdpaterVersion: " + raMetaDataLoc.getAdapterVersion());
        logger.info("InteractionSpecsSupported: " + raMetaDataLoc.getInteractionSpecsSupported());
        logger.info("SpecVersion: " + raMetaDataLoc.getSpecVersion());
        logger.info("supportsExecuteWithInputAndOutputRecord: "
                + raMetaDataLoc.supportsExecuteWithInputAndOutputRecord());
        logger.info("SupportsExecuteWithInputRecordOnly: " + raMetaDataLoc.supportsExecuteWithInputRecordOnly());
        logger.info("SupportsLocalTransactionDemarcation: " + raMetaDataLoc.supportsLocalTransactionDemarcation());
        String[] array = raMetaDataLoc.getInteractionSpecsSupported();
        for (int i = 0; i < array.length; ++i) {
            logger.info("==>" + array[i]);
        }
        return raMetaData;
    }

    /**
     * Ritorna i ResourceAdapterMetaData del Connector
     *
     * @return ResourceAdapterMetaData RA Metadata
     */
    public ResourceAdapterMetaData getResourceAdapterMetaData()
    {
        return raMetaData;
    }

    /**
     * Ritorna i ConnectionMetaData del Connector
     *
     * @return ConnectionMetaData Metadata
     */
    public ConnectionMetaData getConnectorMetaData()
    {
        return metadata;
    }

    /**
     * Crea ConnectionMetaData del Connector
     *
     * @return ConnectionMetaData Metadata
     * @throws ResourceException
     */
    private ConnectionMetaData createConnectionMetaData() throws ResourceException
    {
        ConnectionMetaData metadataLoc = null;
        try {
            // Method metodsetFunctionName = null;
            // Class iconMetaDataClass =
            // Class.forName(classCciConnectionMetaData);
            Class.forName(classCciConnectionMetaData);
            Object conMetaData = connection.getMetaData();
            metadataLoc = (ConnectionMetaData) conMetaData;
            logger.info("EISProductVersion = " + metadataLoc.getEISProductVersion());
            logger.info("EISProductName = " + metadataLoc.getEISProductName());
            logger.info("UserName = " + metadataLoc.getUserName());
        }
        catch (ClassNotFoundException exc) {
            logger.error("ResourceException in performRead: ", exc);
            throw new ResourceException("Parametro nome classe errato");
        }
        return metadataLoc;
    }

    /**
     * getInitialContext Crea la connessione all'AS
     *
     * @return InitialContext Context dell'AS
     * @throws ResourceException
     */
    private InitialContext getInitialContext() throws ResourceException
    {
        InitialContext ic = null;
        try {
            ic = new InitialContext();
        }
        catch (NamingException exc) {
            logger.error("ResourceException in performRead: ", exc);
            throw new ResourceException("Errore inizializzazione contesto");
        }
        return ic;
    }

    /**
     * Ritorna l'oggetto parametro della ConnectionSpec
     *
     * @param nameobject
     *        Stringa nome dell'oggetto
     * @param valueName
     *        Valore da assegnare all'oggetto
     * @return Object parametro della ConnectionSpec
     */
    private Object getObject(String nameobject, String valueName)
    {
        Object value = null;
        if (nameobject.equals("java.lang.String")) {
            value = valueName;
        }
        else if (nameobject.equals("java.lang.Integer") || nameobject.equals("int")) {
            value = new Integer(valueName);
        }
        else if (nameobject.equals("java.lang.Boolean") || nameobject.equals("boolean")) {
            value = new Boolean(valueName);
        }
        else if (nameobject.equals("java.lang.Byte") || nameobject.equals("byte")) {
            value = new Byte(valueName);
        }
        else if (nameobject.equals("java.lang.Character") || nameobject.equals("char")) {
            value = new Character(valueName.toCharArray()[0]);
        }
        else if (nameobject.equals("java.lang.Long") || nameobject.equals("long")) {
            value = new Long(valueName);
        }
        else if (nameobject.equals("java.lang.Double") || nameobject.equals("double")) {
            value = new Double(valueName);
        }
        else if (nameobject.equals("java.lang.Float") || nameobject.equals("float")) {
            value = new Double(valueName);
        }
        else if (nameobject.equals("java.lang.Short") || nameobject.equals("short")) {
            value = new Double(valueName);
        }
        return value;
    }
}