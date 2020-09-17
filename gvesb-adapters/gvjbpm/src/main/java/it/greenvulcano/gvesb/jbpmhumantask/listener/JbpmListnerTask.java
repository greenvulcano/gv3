package it.greenvulcano.gvesb.jbpmhumantask.listener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.log.GVLogger;

import java.io.IOException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jbpm.task.Group;
import org.jbpm.task.User;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.mina.MinaTaskServer;
import org.apache.log4j.Logger;
import org.drools.SystemEventListenerFactory;

public class JbpmListnerTask{

	private static Logger                  logger       = GVLogger.getLogger(JbpmListnerTask.class);
    /**
     * Socket timeout
     */
    private static int port;
    private String name;
    private static TaskService taskService = null;
    private MinaTaskServer server = null;
    private TaskServiceSession taskSession = null;

    public void init(Node node) throws JBPMAdapterException
    {
        try {
            name = XMLConfig.get(node, "@name");
            port = XMLConfig.getInteger(node, "@port");
            logger.debug("JBPMListener name: " + name);
            logger.debug("JBPMListener port: " + port);
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.task");
            taskService = new TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
            taskSession = taskService.createSession();

            // Add users
            NodeList pnl = XMLConfig.getNodeList(node, "UsersJbpm/userJbpm");
            if ((pnl != null) && (pnl.getLength() > 0)) {
                for (int i = 0; i < pnl.getLength(); i++) {
                    Node n = pnl.item(i);
                    String uname = XMLConfig.get(n, "@user");
                    logger.debug("User name: " + uname);
                    User user = new User(uname);
                    taskSession.addUser(user);
                }
            }
           // Add users
            pnl = XMLConfig.getNodeList(node, "GroupsJbpm/groupJbpm");
            if ((pnl != null) && (pnl.getLength() > 0)) {
                for (int i = 0; i < pnl.getLength(); i++) {
                    Node n = pnl.item(i);
                    String gname = XMLConfig.get(n, "@group");
                    logger.debug("Group name: " + gname);
                    Group group = new Group();
                    group.setId(gname);
                    taskSession.addGroup(group);
                }
            }
        }
        catch (Exception exc) {
        	exc.printStackTrace();
        	throw new JBPMAdapterException("JBPM_LISTENER_INIT_ERROR", exc);
        }
    }
    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }
    /**
     * @return the name
     * @throws IOException
     */
    public void start() throws GVJbpmHumanTaskAdapterException
    {
    	server = new MinaTaskServer(taskService,port);
    	try {
    		server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new GVJbpmHumanTaskAdapterException("JBPM_LISTENER_START_ERROR",e);
		}
    }
    public void destroy()
    {
        logger.debug("BEGIN - Destroing JBPMListener[" + name + "]");
        try {
        	if(taskSession != null){
        		logger.debug("taskSession.dispose();");
        		taskSession.dispose();
        	}
        	if(server != null){
        		logger.debug("taskSession.dispose();");
        		server.stop();
        	}
        }
        catch (Exception exc) {
            // do nothing
        }
        logger.debug("END - Destroing JBPMListener[" + name + "]");
    }

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.task");
        TaskService taskService = new TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
        TaskServiceSession taskSession = taskService.createSession();
        // Add users
        User user = new User("krisv");
        taskSession.addUser( user );
        user = new User("mary");
        taskSession.addUser( user );
        user = new User("john");
        taskSession.addUser( user );
        user = new User("Administrator");
        taskSession.addUser( user );
        Group group = new Group();
        group.setId("knightsTempler");
        taskSession.addGroup( group );
        group = new Group();
        group.setId("crusaders");
        taskSession.addGroup( group );

        // start server
        MinaTaskServer server = new MinaTaskServer(taskService,9123);
        Thread thread = new Thread(server);
        thread.start();
        taskSession.dispose();
        System.out.println("Task service started correctly !");
        System.out.println("Task service running ...");
    }

}
