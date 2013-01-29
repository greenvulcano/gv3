package it.greenvulcano.gvesb.core.bpel.manager;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.bpel.exception.BpelException;
import it.greenvulcano.gvesb.core.bpel.scheduler.SchedulerWrapper;
import it.greenvulcano.gvesb.j2ee.XAHelper;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.iapi.BindingContext;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.daohib.bpel.BpelDAOConnectionFactoryImpl;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.store.ProcessStoreImpl;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class GVBpelEngineServer implements ConfigurationListener, ShutdownEventListener
{
    private BpelServerImpl                     server;
    private ProcessStoreImpl                   store;
    private TransactionManager                 txManager;
    private DataSource                         dataSource;
    private Scheduler                          scheduler;
    private EndpointReferenceContext           eprContext;
    private HashMap<String, EndpointReference> endpoint               = new HashMap<String, EndpointReference>();
    private MessageExchangeContext             mexContext;
    private HashMap<String, QName>             actived                = new HashMap<String, QName>();
    private BindingContext                     bindContext;
    private BpelDAOConnectionFactoryImpl       daoCf;
    private OdeConfigProperties                odeConfig              = null;
    private static GVBpelEngineServer          instance               = null;
    private static Logger                      logger                 = GVLogger.getLogger(GVBpelEngineServer.class);
    private Collection<QName>                  qnameServices          = null;
    private static final String                DEFAULT_CONF_FILE_NAME = "GVCore.xml";
    private XAHelper                           xaHelper               = new XAHelper();
    private Properties                         props                  = null;
    private boolean                            initialized            = false;

    public static synchronized GVBpelEngineServer instance() throws BpelException
    {
        if (instance == null) {
            instance = new GVBpelEngineServer();
            instance.init();
        }

        return instance;
    }

    public GVBpelEngineServer() throws BpelException
    {
        //init();
    }

    public void destroy()
    {
        logger.info("BEGIN - Destroing GVBpelManager");
        try {

        }
        catch (Exception exc) {
        }
        logger.info("END - Destroing GVBpelManager");
    }

    @Override
    public void shutdownStarted(ShutdownEvent event)
    {
        destroy();
    }

    @Override
    public void configurationChanged(ConfigurationEvent evt)
    {
        logger.info("BEGIN - Operation(reload Configuration)");
        if ((evt.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && evt.getFile().equals(DEFAULT_CONF_FILE_NAME)) {
            destroy();
            initialized = false;
            /*
            try {
                init();
            }
            catch (Exception exc) {
                logger.error("Error initializing BpelListenerManager", exc);
            }*/
        }
        logger.info("END - Operation(reload Configuration)");

    }

    private void createEndpointReferenceContext()
    {

        logger.info(" Start create EndpointReferenceContext");

        eprContext = new EndpointReferenceContext() {

            @Override
            public EndpointReference resolveEndpointReference(Element element)
            {
                String service = DOMUtils.getChildCharacterData(element);
                return endpoint.get(service);
            }

            @Override
            public EndpointReference convertEndpoint(QName qName, Element element)
            {
                return null;
            }

            @Override
            public Map<?, ?> getConfigLookup(EndpointReference epr)
            {
                return Collections.EMPTY_MAP;
            }
        };
        logger.info("EndpointReferenceContext created");
    }


    /**
	 * 
	 */
    private void init() throws BpelException
    {
        String deployUnitProcess = "";
        boolean isStart = true;

        if (initialized) {
            return;
        }

        synchronized (instance) {
            if (initialized) {
                return;
            }

            int transactionTimeout = 30;
            XMLConfig.addConfigurationListener(this, DEFAULT_CONF_FILE_NAME);
            props = new Properties();
            try {

                Document globalConfig = null;
                try {
                    globalConfig = XMLConfig.getDocument(DEFAULT_CONF_FILE_NAME);
                }
                catch (XMLConfigException exc) {
                    logger.error("Error reading BPEL configuration from file: " + DEFAULT_CONF_FILE_NAME, exc);
                }

                if (globalConfig == null) {
                    return;
                }

                Node bpelNode = XMLConfig.getNode(DEFAULT_CONF_FILE_NAME, "/GVCore/GVServices/BpelEngineConfiguration");

                isStart = XMLConfig.getBoolean(bpelNode, "@startServer", true);

                if (isStart) {
                    logger.info("***********************************************");
                    logger.info("Bpel engine starting");
                    logger.info("***********************************************");
                    logger.info("bpelNode=" + bpelNode.toString());
                    deployUnitProcess = PropertiesHandler.expand(XMLConfig.get(bpelNode, "@deployMentUnitProcess"),
                            null);
                    transactionTimeout = XMLConfig.getInteger(bpelNode, "@transactionTimeout", 30);
                    Collection<Node> listProp = XMLConfig.getNodeListCollection(bpelNode, "EngineProperties");
                    for (Node property : listProp) {
                        String key = property.getAttributes().getNamedItem("name").getNodeValue();
                        String value = property.getAttributes().getNamedItem("value").getNodeValue();
                        props.setProperty(key, value);
                        logger.info("Property: " + key + "=" + value);
                    }

                    String dbMode = props.getProperty("gv-bpel.db.mode");;
                    if ((dbMode == null) || !dbMode.equals("EXTERNAL")) {
                        logger.error("GreenVulcano support only EXTERNAL db mode please fix it in file: "
                                + DEFAULT_CONF_FILE_NAME);
                        throw new BpelException("GreenVulcano support only EXTERNAL db mode please fix it in file: "
                                + DEFAULT_CONF_FILE_NAME);
                    }
                    String extDatabase = props.getProperty("gv-bpel.db.ext.dataSource");

                    logger.info("deployment directory of Bpel Process: " + deployUnitProcess);

                    setOdeConfig(new OdeConfigProperties(props, "gv-bpel."));
                    startBpelEngine(deployUnitProcess, extDatabase, transactionTimeout);

                    initialized = true;
                }
                else {
                    logger.info("***********************************************");
                    logger.info("Bpel engine not started");
                    logger.info("***********************************************");

                }
            }
            catch (BpelException exc) {
                logger.error("Error initializing GVBpelManager", exc);
                throw exc;
            }
            catch (Exception exc) {
                logger.error("Error initializing GVBpelManager", exc);
                throw new BpelException("GVBPEL_APPLICATION_INIT_ERROR", exc);
            }
        }
    }

    public void startBpelEngine(String deployDir, String extDatadase, int transactionTimeout) throws Exception
    {
        server = new BpelServerImpl();
        createTransactionManager(transactionTimeout);
        createDataSource(extDatadase);
        createScheduler();
        createDAOConnection();

        if (daoCf == null) {
            throw new RuntimeException("No DAO");
        }
        server.setDaoConnectionFactory(daoCf);
        server.setInMemDaoConnectionFactory(new org.apache.ode.bpel.memdao.BpelDAOConnectionFactoryImpl(scheduler));
        server.setDaoConnectionFactory(daoCf);
        if (scheduler == null) {
            throw new RuntimeException("No scheduler");
        }
        createEndpointReferenceContext();
        store = new ProcessStoreImpl(eprContext, dataSource, "hib", odeConfig, true);

        server.setScheduler(scheduler);
        server.setEndpointReferenceContext(eprContext);
        createMessageExchangeContext();
        server.setMessageExchangeContext(mexContext);
        createBindingContext();
        server.setBindingContext(bindContext);
        server.init();
        server.start();

        File rootBpelDirectory = new File(deployDir);
        String[] listSubDirectory = rootBpelDirectory.list();

        logger.info("***********************************************");
        logger.info("DEPLOYED:");
        for (String bpelProcessDir : listSubDirectory) {

            try {
                qnameServices = deploy(new File(rootBpelDirectory + File.separator + bpelProcessDir));
            }
            catch (Exception e) {
                e.printStackTrace();
                logger.error("Error configuration Bpel Process: " + bpelProcessDir);
            }
            logger.info("BPEL PROCESS:" + qnameServices.iterator().next().toString());
        }
        logger.info("***********************************************");
    }


    protected void createTransactionManager(int transactionTimeout) throws Exception
    {
        logger.info("Start create transactionManager");
        txManager = xaHelper.getTransactionManager();
        logger.info("txManager=" + txManager.getClass());
        txManager.setTransactionTimeout(transactionTimeout);
        logger.info("TransactionManager created");
    }


    private void createDataSource(String extDatadase) throws BpelException, NamingException
    {
        logger.info("Start create datasource");
        try {
            Context initContext = new InitialContext();
            dataSource = (DataSource) initContext.lookup(extDatadase);
        }
        catch (Exception ex) {

            logger.error("Error create message", ex);
            throw new BpelException("ERROR", ex);
        }

        logger.info("datasource created");
    }

    protected void createScheduler() throws Exception
    {
        logger.info("Start create scheduler");
        if (server == null) {
            throw new RuntimeException("No BPEL server");
        }
        if (txManager == null) {
            throw new RuntimeException("No transaction manager");
        }
        if (dataSource == null) {
            throw new RuntimeException("No data source");
        }
        scheduler = new SchedulerWrapper(server, txManager);
        logger.info("scheduler created");
    }


    protected void createDAOConnection() throws Exception
    {
        logger.info("Start BpelDAOConnectionFactory created");
        if (txManager == null) {
            throw new RuntimeException("No transaction manager");
        }
        if (dataSource == null) {
            throw new RuntimeException("No data source");
        }

        daoCf = new BpelDAOConnectionFactoryImpl();
        daoCf.setDataSource(dataSource);
        daoCf.setTransactionManager(txManager);
        daoCf.init(props);
        logger.info("End BpelDAOConnectionFactory created");
    }


    protected void createMessageExchangeContext()
    {

        mexContext = new MessageExchangeContext() {
            @Override
            public void invokePartner(PartnerRoleMessageExchange mex)
            {
            }

            @Override
            public void onAsyncReply(MyRoleMessageExchange myRoleMex)
            {
            }
        };

    }


    protected void createBindingContext()
    {
        logger.info("Start BindingContext created");
        bindContext = new BindingContext() {
            @Override
            public EndpointReference activateMyRoleEndpoint(QName processId, Endpoint myRoleEndpoint)
            {
                final Document doc = DOMUtils.newDocument();
                Element serviceRef = doc.createElementNS(EndpointReference.SERVICE_REF_QNAME.getNamespaceURI(),
                        EndpointReference.SERVICE_REF_QNAME.getLocalPart());
                serviceRef.appendChild(doc.createTextNode(myRoleEndpoint.serviceName.toString()));
                doc.appendChild(serviceRef);
                actived.put(myRoleEndpoint.toString(), processId);
                return new EndpointReference() {
                    @Override
                    public Document toXML()
                    {
                        return doc;
                    }

                };

            }

            @Override
            public void deactivateMyRoleEndpoint(Endpoint myRoleEndpoint)
            {
                actived.remove(myRoleEndpoint);
            }

            @Override
            public PartnerRoleChannel createPartnerRoleChannel(QName processId, PortType portType,
                    final Endpoint initialPartnerEndpoint)
            {
                final EndpointReference epr = new EndpointReference() {
                    @Override
                    public Document toXML()
                    {
                        Document doc = DOMUtils.newDocument();
                        Element serviceRef = doc.createElementNS(EndpointReference.SERVICE_REF_QNAME.getNamespaceURI(),
                                EndpointReference.SERVICE_REF_QNAME.getLocalPart());
                        serviceRef.appendChild(doc.createTextNode(initialPartnerEndpoint.serviceName.toString()));
                        doc.appendChild(serviceRef);
                        return doc;
                    }
                };
                endpoint.put(initialPartnerEndpoint.serviceName.toString(), epr);
                return new PartnerRoleChannel() {
                    @Override
                    public EndpointReference getInitialEndpointReference()
                    {
                        return epr;
                    }

                    @Override
                    public void close()
                    {
                    };
                };
            }

            @Override
            public long calculateSizeofService(EndpointReference epr)
            {
                return 0;
            }
        };
    }

    public Collection<QName> deploy(File deploymentUnitDirectory) throws Exception
    {
        logger.info("Start deploy service:" + deploymentUnitDirectory.getName());
        Collection<QName> pids = store.deploy(deploymentUnitDirectory);
        for (QName pid : pids) {
            server.register(store.getProcessConfiguration(pid));
        }
        logger.info("End deploy service:" + deploymentUnitDirectory.getName());
        return pids;
    }


    public GVBuffer invokeASYNCRR(GVBuffer gvBuffer, QName serviceName, String opName, Element body) throws Exception
    {
        MyRoleMessageExchange mex;
        @SuppressWarnings("rawtypes")
        Future responseFuture;

        init();
        try {
            String messageId = new GUID().toString();
            txManager.begin();
            mex = server.getEngine().createMessageExchange("" + messageId, serviceName, opName);
            mex.setProperty("isTwoWay", "true");

            if (mex.getOperation() == null) {
                throw new Exception("Did not find operation " + opName + " on service " + serviceName);
            }
            logger.debug(mex.getMessageExchangePattern());
            Message request = mex.createMessage(mex.getOperation().getInput().getMessage().getQName());
            Element wrapper = body.getOwnerDocument().createElementNS("", "payload");
            wrapper.appendChild(body);
            Element message = body.getOwnerDocument().createElementNS("", "message");
            message.appendChild(wrapper);
            request.setMessage(message);
            responseFuture = mex.invoke(request);
            txManager.commit();
        }
        catch (Exception except) {
            txManager.rollback();
            throw except;
        }
        if (mex.getOperation().getOutput() != null) {
            responseFuture.get(org.apache.ode.utils.Properties.DEFAULT_MEX_TIMEOUT, TimeUnit.MILLISECONDS);
            txManager.begin();
            try {
                mex = (MyRoleMessageExchange) server.getEngine().getMessageExchange(mex.getMessageExchangeId());
                switch (mex.getStatus()) {
                    case FAULT :
                        logger.error("Fault response message: " + mex.getFault());
                        break;
                    case ASYNC :
                    case RESPONSE :
                        Message response = mex.getResponse();
                        gvBuffer.setObject(XMLUtils.serializeDOM_S(response.getMessage()));
                        //logger.info("Response message: " + baos.toString());
                        break;
                    case FAILURE :
                        logger.error("Message exchange failure");
                        throw new Exception("Message exchange failure");
                    default :
                        logger.error("Received ODE message exchange in unexpected state: " + mex.getStatus());
                        throw new Exception("Received ODE message exchange in unexpected state: " + mex.getStatus());
                }
                txManager.commit();
            }
            catch (Exception exc) {
                txManager.rollback();
                throw exc;
            }
        }

        mex.complete();
        return gvBuffer;
    }

    public void invokeASYNCR(GVBuffer gvBuffer, QName serviceName, String opName, Element body) throws Exception
    {
        MyRoleMessageExchange mex;
        init();
        try {
            String messageId = new GUID().toString();
            txManager.begin();
            mex = server.getEngine().createMessageExchange("" + messageId, serviceName, opName);
            mex.setProperty("isTwoWay", "false");

            if (mex.getOperation() == null) {
                throw new Exception("Did not find operation " + opName + " on service " + serviceName);
            }
            logger.error(mex.getMessageExchangePattern());
            Message request = mex.createMessage(mex.getOperation().getInput().getMessage().getQName());
            Element wrapper = body.getOwnerDocument().createElementNS("", "payload");
            wrapper.appendChild(body);
            Element message = body.getOwnerDocument().createElementNS("", "message");
            message.appendChild(wrapper);
            request.setMessage(message);
            txManager.commit();
        }
        catch (Exception except) {
            txManager.rollback();
            throw except;
        }
    }

    public void shutdown()
    {
        try {
            //_database.shutdown();
            dataSource.getConnection().close();
            for (java.util.Iterator<QName> it = qnameServices.iterator(); it.hasNext();) {
                server.unregister(it.next());
            }
            server.stop();
        }
        catch (final Exception ex) {
            server = null;
        }
        try {
            scheduler.stop();
            scheduler.shutdown();
        }
        catch (final Exception ex) {
            scheduler = null;
        }
        try {
            daoCf.shutdown();
        }
        catch (final Exception ex) {
            daoCf = null;
        }
        dataSource = null;
        store.shutdown();
        txManager = null;
        instance = null;
    }

    public BpelServerImpl getServer()
    {
        return server;
    }

    public ProcessStoreImpl getStore()
    {
        return store;
    }

    public OdeConfigProperties getOdeConfig()
    {
        return odeConfig;
    }

    public void setOdeConfig(OdeConfigProperties odeConfig)
    {
        this.odeConfig = odeConfig;
    }

}
