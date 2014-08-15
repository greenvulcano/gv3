/**
 *
 */
package it.greenvulcano.gvesb.virtual.jbpm;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.compiler.BPMN2ProcessFactory;
import org.drools.compiler.ProcessBuilderFactory;
import org.drools.io.ResourceFactory;
import org.drools.marshalling.impl.ProcessMarshallerFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessRuntimeFactory;
import org.jbpm.bpmn2.BPMN2ProcessProviderImpl;
import org.jbpm.marshalling.impl.ProcessMarshallerFactoryServiceImpl;
import org.jbpm.process.audit.WorkingMemoryDbLogger;
import org.jbpm.process.builder.ProcessBuilderFactoryServiceImpl;
import org.jbpm.process.instance.ProcessRuntimeFactoryServiceImpl;
import org.jbpm.process.workitem.wsht.WSHumanTaskHandler;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.j2ee.XAHelper;
import it.greenvulcano.gvesb.jbpmhumantask.listener.JBPMAdapterException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;

/**
 * E' la classe che gestisce i report per dialogare con la pagina JSP.
 *
 * @author Ciro Romano
 * @version 1.0
 */
public class JBPMSessionManager implements ShutdownEventListener, ConfigurationListener
{

    private static Logger                    logger           = GVLogger.getLogger(JBPMSessionManager.class);

    private static JBPMSessionManager             instance         = null;
    public static String                          JBPM_CFG_FILE    = "GVJBPM-Configuration.xml";
	private String dirFileBpmn = null;
	private String ipHumanTaskHandler = null;
	private int portHumanTaskHandler = -1;
	private XAHelper xaHelper = new XAHelper();
	private StatefulKnowledgeSession ksession=null;
    // private String reportHome = null;

    /**
     * Legge il file xml di configrazione dei report e inizializza il
     * {@link ReportManager} secondo i parametri di tale file. Crea la lista dei
     * {@link Group} e inizializza i singoli gruppi.
     *
     * @throws Exception
     *         in caso di errori
     */
    private JBPMSessionManager() throws JBPMAdapterException
    {
        init();
    }

    public static synchronized JBPMSessionManager instance() throws JBPMAdapterException
    {
        if (instance == null) {
            instance = new JBPMSessionManager();
            ShutdownEventLauncher.addEventListener(instance);
            XMLConfig.addConfigurationListener(instance, JBPM_CFG_FILE);
        }
        return instance;
    }

    private void init() throws JBPMAdapterException
    {
        try {
        	logger.debug("init");
        	Node confNode = XMLConfig.getNode(JBPM_CFG_FILE, "/GVJBPMConfiguration");
        	init(confNode);
    		ksession = createSession();
    		WorkingMemoryDbLogger jBpmlogger = new WorkingMemoryDbLogger(ksession);
    	    WSHumanTaskHandler wsHumanTaskHandler = new WSHumanTaskHandler();
    	    wsHumanTaskHandler.setConnection(ipHumanTaskHandler, portHumanTaskHandler);
    	    ksession.getWorkItemManager().registerWorkItemHandler("Human Task", wsHumanTaskHandler);
    	    logger.debug("end init");
        }
        catch (Exception exc) {
            throw new JBPMAdapterException("Error initializing JBPM Engine", exc);
        }
    }
    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws InitializationException
    {
        try {
    	    this.dirFileBpmn = XMLConfig.get(node, "@repository-bpmn");
    	    logger.debug("dirFileBpmn="+dirFileBpmn);
    	    this.ipHumanTaskHandler =  XMLConfig.get(node, "@hostHumanTaskHandler","localhost");
    	    logger.debug("hostHumanTaskHandler="+ipHumanTaskHandler);
    	    this.portHumanTaskHandler = XMLConfig.getInteger(node, "@portHumanTaskHandler",9123);
    		logger.debug("portHumanTaskHandler="+portHumanTaskHandler);
        }
        catch (Exception exc) {
            logger.error("ERROR JbpmCall initialization", exc);
            throw new InitializationException("GV_CONF_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    public StatefulKnowledgeSession getSession() throws Exception
    {
      return ksession;
    }
    private  KnowledgeBase readKnowledgeBase() throws Exception {
    	String localDirFileBpmn = PropertiesHandler.expand(dirFileBpmn);
    	File file = new File(localDirFileBpmn);
		if (!file.exists()) {
			throw new IllegalArgumentException("Could not find " + localDirFileBpmn);
		}
		if (!file.isDirectory()) {
			throw new IllegalArgumentException(localDirFileBpmn + " is not a directory");
		}
		ProcessBuilderFactory.setProcessBuilderFactoryService(new ProcessBuilderFactoryServiceImpl());
		ProcessMarshallerFactory.setProcessMarshallerFactoryService(new ProcessMarshallerFactoryServiceImpl());
		ProcessRuntimeFactory.setProcessRuntimeFactoryService(new ProcessRuntimeFactoryServiceImpl());
		BPMN2ProcessFactory.setBPMN2ProcessProvider(new BPMN2ProcessProviderImpl());
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		for (File subfile: file.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".bpmn");
			}})) {
		logger.debug("Loading process " + subfile.getName());
		kbuilder.add(ResourceFactory.newFileResource(subfile), ResourceType.BPMN2);
	    }
		return kbuilder.newKnowledgeBase();
	}

	private StatefulKnowledgeSession createSession() throws Exception{
		StatefulKnowledgeSession ksession = null;
		try {

			KnowledgeBase kbase = readKnowledgeBase();
			EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");
		    Environment env = KnowledgeBaseFactory.newEnvironment();
		    env.set( EnvironmentName.ENTITY_MANAGER_FACTORY, emf );
		    env.set( EnvironmentName.TRANSACTION_MANAGER, xaHelper.getTransactionManager());
		    Context ctx = null;
		    try {
		        ctx = new InitialContext();
		        env.set( EnvironmentName.TRANSACTION, ctx.lookup("UserTransaction"));
		    }
		    finally {
		        try {
                    if (ctx != null) {
                        ctx.close();
                    }
                }
                catch (Exception exc) {
                    exc.printStackTrace();
                }
		    }
		    Properties properties = new Properties();
		    properties.put("drools.processInstanceManagerFactory", "org.jbpm.persistence.processinstance.JPAProcessInstanceManagerFactory");
		    properties.put("drools.processSignalManagerFactory", "org.jbpm.persistence.processinstance.JPASignalManagerFactory");
		    KnowledgeSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration(properties);
		    ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, config, env);
		} catch (Exception e) {
			logger.debug("ERRORE CREAZIONE SESSIONE");
			e.printStackTrace();
			throw e;
		}
		return ksession;

	}

    private void destroy()
    {
        ShutdownEventLauncher.removeEventListener(instance);
        XMLConfig.removeConfigurationListener(instance);
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
        destroy();
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    @Override
    public void configurationChanged(ConfigurationEvent event)
    {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && event.getFile().equals(JBPM_CFG_FILE)) {
        	logger.debug("configurationChanged");
        	destroy();
        	ksession.dispose();
        	instance=null;

        }
    }
}