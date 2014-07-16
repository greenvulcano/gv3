package it.greenvulcano.gvesb.virtual.jbpm;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.expression.ExpressionEvaluator;
import it.greenvulcano.expression.ExpressionEvaluatorHelper;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.drools.SystemEventListenerFactory;
import org.jbpm.process.workitem.wsht.BlockingGetTaskResponseHandler;
import org.jbpm.task.AccessType;
import org.jbpm.task.Status;
import org.jbpm.task.Task;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.TaskClient;
import org.jbpm.task.service.mina.MinaTaskClientConnector;
import org.jbpm.task.service.mina.MinaTaskClientHandler;
import org.jbpm.task.service.responsehandlers.BlockingTaskOperationResponseHandler;
import org.jbpm.task.service.responsehandlers.BlockingTaskSummaryResponseHandler;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JbpmTaskCallOperation implements CallOperation
{

    private String              ipAddress  = "127.0.0.1";
    private String              operation  = "";
    private int                 port       = 9123;
    private TaskClient          client;
    private Map<String, String> parameters = new HashMap<String, String>();
    private static Logger       logger     = GVLogger.getLogger(JbpmTaskCallOperation.class);
    /**
     * the operation key
     */
    protected OperationKey      key        = null;

    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            init(XMLConfig.get(node, "@hostHumanTaskHandler", "localhost"),
                    XMLConfig.getInteger(node, "@portHumanTaskHandler", 9123), XMLConfig.get(node, "@operation"));
            NodeList pnl = XMLConfig.getNodeList(node, "ParamsTasks/ParamTask");
            if ((pnl != null) && (pnl.getLength() > 0)) {
                for (int i = 0; i < pnl.getLength(); i++) {
                    Node n = pnl.item(i);
                    String name = XMLConfig.get(n, "@name");
                    String expression = XMLConfig.get(n, "@expression");
                    parameters.put(name, expression);
                    logger.debug("name " + name + "=" + expression);
                }
            }
        }
        catch (Exception exc) {
            logger.error("ERROR JbpmCall initialization", exc);
            throw new InitializationException("GV_CONF_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
    }

    public void init(String ipAddress, int port, String operation)
    {
        this.ipAddress = ipAddress;
        this.port = port;
        this.operation = operation;
    }

    private void connect()
    {
        if (client == null) {
            client = new TaskClient(new MinaTaskClientConnector("org.drools.process.workitem.wsht.WSHumanTaskHandler",
                    new MinaTaskClientHandler(SystemEventListenerFactory.getSystemEventListener())));
            boolean connected = client.connect(ipAddress, port);
            if (!connected) {
                throw new IllegalArgumentException("Could not connect task client");
            }
        }
    }

    private void disconnect() throws Exception
    {
        client.disconnect();
    }

    private Task getTaskById(long taskId)
    {
        BlockingGetTaskResponseHandler responseHandler = new BlockingGetTaskResponseHandler();
        client.getTask(taskId, responseHandler);
        Task task = responseHandler.getTask();
        return task;
    }

    public GVBuffer getTaskById(GVBuffer gvBuffer) throws Exception
    {
        String srTaskId = gvBuffer.getProperty("taskId");
        long taskId = Integer.parseInt(srTaskId);
        Task task = getTaskById(taskId);
        gvBuffer.setObject(task);
        return gvBuffer;
    }

    private void assignTask(long taskId, String idRef, String userId)
    {
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        if (idRef == null) {
            client.release(taskId, userId, responseHandler);
        }
        else if (idRef.equals(userId)) {
            client.claim(taskId, idRef, responseHandler);
        }
        else {
            client.delegate(taskId, userId, idRef, responseHandler);
        }
        responseHandler.waitTillDone(5000);
    }

    public void assignTask(GVBuffer gvBuffer) throws Exception
    {
        String userId = gvBuffer.getProperty("userId");
        String idRef = gvBuffer.getProperty("idRef");
        String srTaskId = gvBuffer.getProperty("taskId");
        long taskId = Integer.parseInt(srTaskId);
        assignTask(taskId, idRef, userId);

    }

    private void completeTask(long taskId, Map<String, Object> data, String userId)
    {
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.start(taskId, userId, responseHandler);
        responseHandler.waitTillDone(5000);
        responseHandler = new BlockingTaskOperationResponseHandler();
        ContentData contentData = null;
        if (data != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out;
            try {
                out = new ObjectOutputStream(bos);
                out.writeObject(data);
                out.close();
                contentData = new ContentData();
                contentData.setContent(bos.toByteArray());
                contentData.setAccessType(AccessType.Inline);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        client.complete(taskId, userId, contentData, responseHandler);
        responseHandler.waitTillDone(5000);
    }

    private void completeTask(long taskId, String outcome, Map<String, Object> data, String userId)
    {
        data.put("outcome", outcome);
        completeTask(taskId, data, userId);
    }

    private void releaseTask(long taskId, String userId)
    {
        // TODO: this method is not being invoked, it's using
        // assignTask with null parameter instead
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.release(taskId, userId, responseHandler);
        responseHandler.waitTillDone(5000);
    }

    private List<TaskSummary> getAssignedTasks(String idRef)
    {
        List<TaskSummary> result = new ArrayList<TaskSummary>();
        try {
            BlockingTaskSummaryResponseHandler responseHandler = new BlockingTaskSummaryResponseHandler();
            client.getTasksOwned(idRef, "en-UK", responseHandler);
            List<TaskSummary> tasks = responseHandler.getResults();
            for (TaskSummary task : tasks) {
                if (task.getStatus() == Status.Reserved) {
                    result.add(task);
                }
            }
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
        return result;
    }

    public GVBuffer getAssignedTasks(GVBuffer gvBuffer) throws Exception
    {
        String idRef = gvBuffer.getProperty("idRef");
        List<TaskSummary> tasks = getAssignedTasks(idRef);
        gvBuffer.setObject(tasks);
        return gvBuffer;
    }

    private List<TaskSummary> getUnassignedTasks(String idRef, String participationType)
    {
        List<TaskSummary> result = new ArrayList<TaskSummary>();
        try {
            BlockingTaskSummaryResponseHandler responseHandler = new BlockingTaskSummaryResponseHandler();
            client.getTasksAssignedAsPotentialOwner(idRef, "en-UK", responseHandler);
            List<TaskSummary> tasks = responseHandler.getResults();
            for (TaskSummary task : tasks) {
                result.add(task);
            }
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
        return result;
    }

    public GVBuffer getUnassignedTasks(GVBuffer gvBuffer) throws Exception
    {
        String idRef = gvBuffer.getProperty("idRef");
        String participationType = gvBuffer.getProperty("participationType");
        List<TaskSummary> tasks = getUnassignedTasks(idRef, participationType);
        gvBuffer.setObject(tasks);
        return gvBuffer;
    }

    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        try {
            connect();
            if (operation.equals("completeTask")) {
                logger.debug("action completeTask");
                completeTask(gvBuffer);
            }
            else if (operation.equals("assignTask")) {
                logger.debug("action assignTask");
                assignTask(gvBuffer);
            }
            else if (operation.equals("getAssignedTasks")) {
                logger.debug("action getAssignedTasks");
                gvBuffer = getAssignedTasks(gvBuffer);
            }
            else if (operation.equals("getUnassignedTasks")) {
                logger.debug("action getUnassignedTasks");
                gvBuffer = getUnassignedTasks(gvBuffer);
            }
            else if (operation.equals("getTaskById")) {
                logger.debug("action getTaskById");
                gvBuffer = getTaskById(gvBuffer);
            }
            disconnect();
        }
        catch (Exception exc) {
            logger.error("ERROR jbpm execution", exc);
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
        }
        return gvBuffer;
    }

    public void completeTask(GVBuffer gvBuffer) throws Exception
    {
        Map<String, Object> params = new HashMap<String, Object>();
        String userId = gvBuffer.getProperty("userId");
        String srTaskId = gvBuffer.getProperty("taskId");
        long taskId = Integer.parseInt(srTaskId);
        if (!parameters.isEmpty()) {
            ExpressionEvaluatorHelper.startEvaluation();
            try {
                ExpressionEvaluatorHelper.addToContext("params", gvBuffer);
                for (Map.Entry<String, String> p : parameters.entrySet()) {
                    String value = p.getValue();
                    ExpressionEvaluator expressionEvaluator = ExpressionEvaluatorHelper.getExpressionEvaluator(ExpressionEvaluatorHelper.OGNL_EXPRESSION_LANGUAGE);
                    String nomeParam = p.getKey();
                    Object obj = expressionEvaluator.getValue(value, gvBuffer);
                    params.put(p.getKey(), obj);
                    System.out.println("param " + nomeParam + "=" + obj.toString());
                }
            }
            finally {
                ExpressionEvaluatorHelper.endEvaluation();
            }
        }
        completeTask(taskId, params, userId);

    }

    @Override
    public void cleanUp()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void destroy()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

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
}
