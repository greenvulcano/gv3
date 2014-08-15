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
package it.greenvulcano.gvesb.gvconsole.workbench.plugin;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.core.config.GreenVulcanoConfig;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.jmx.JMXUtils;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.Set;
import java.util.Vector;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The <code>GVMonitoringPlugin</code> plugin is used to monitoring GreenVulcano
 * by JMX tecnology.
 *
 *
 * @version 2.1.0 17 Feb 2010
 * @author GreenVulcano Developer Team
 */
public class GVMonitoringPlugin implements TestPlugin
{
    /**
     * The total hint count for node.
     */
    private Long                      totalHintsNod;

    /**
     * The total hint count for service.
     */
    private Long                      totalHintsSvc;

    /**
     * The max throughput for node.
     */
    private Object[]                  maxThroughputNod;

    /**
     * The max throughput for service.
     */
    private Object[]                  maxThroughputSvc;

    /**
     * The min throughput for node.
     */
    private Object[]                  minThroughputNod;

    /**
     * The min throughput for service.
     */
    private Object[]                  minThroughputSvc;

    /**
     * The throughput for node.
     */
    private Float                     throughputNod;

    /**
     * The throughput for service.
     */
    private Float                     throughputSvc;

    /**
     * The history throughput for service.
     */
    private Float                     historyThroughputSvc;

    /**
     * The history throughput for node.
     */
    private Float                     historyThroughputNod;

    /**
     * Manage the notification object in a vector.
     */
    private static NotificationVector notificationVector    = null;

    /**
     * JMX object.
     */
    private JMXEntryPoint             jmx                   = null;

    /**
     * JMX object.
     */
    private MBeanServer               server                = null;

    /**
     * JMX object.
     */
    private Set<ObjectName>           objects               = null;

    /**
     * The system list values.
     */
    private String[]                  systemValues          = null;

    /**
     * The system name.
     */
    private String                    system                = "";

    /**
     * The service name.
     */
    private String                    service               = "";

    /**
     * The notification type.
     */
    private String                    type                  = "";

    /**
     * The forward name operation.
     */
    private String                    forwardName           = "";

    /**
     * The operation value.
     */
    private String                    operation             = "";

    /**
     * The component value.
     */
    private String                    component             = "";

    /**
     * The location value.
     */
    private String                    location              = "";

    /**
     * The group value.
     */
    private String                    group                 = "";

    /**
     * The query string created.
     */
    private String                    filter                = "";

    /**
     * The throughput wgv application url.
     */
    private String                    throughputUrl         = "";

    /**
     * The wgvlogic domain.
     */
    private String                    domain                = "";

    /**
     * The groups value in the xml.
     */
    private String                    groups                = null;

    /**
     * The systems value in the xml.
     */
    private String                    systems               = null;

    /**
     * The services value in the xml.
     */
    private String                    services              = null;

    /**
     * The services value in the xml.
     */
    private String                    systemServices        = null;

    /**
     * Internal list.
     */
    private String                    internal              = null;

    /**
     * Notification message.
     */
    private String                    messageNotif          = "";

    /**
     * Notification type.
     */
    private String                    notificationType      = "";

    /**
     * Type stamp notification.
     */
    private long                      timeStampNotif        = 0;

    /**
     * The max output notification object to see in the page.
     */
    private int                       maxNotificationObject = 0;

    /**
     * Type stamp notification.
     */
    private String                    jmxNotFilter          = "";

    /**
     * If workbench is registered to keep notification vector.
     */
    private boolean                   registered            = false;

    /**
     * The dump size for gvBuffer.
     */
    private String                    dumpSize              = "0";

    /**
     * The dump size for gvBuffer.
     */
    private Integer                   dump                  = new Integer(0);

    /**
     * Empty constructor.
     *
     * @throws Throwable
     *         If an error occurred
     */
    public GVMonitoringPlugin() throws Throwable
    {
        // do nothing
    }

    /**
     * Accessor setter method for field <tt>system</tt>
     *
     * @param system
     *        The system name request
     */
    public void setSystem(String system)
    {
        this.system = system;
    }

    /**
     * Accessor setter method for field <tt>service</tt>
     *
     * @param service
     *        The service name request
     */
    public void setService(String service)
    {
        this.service = service;
    }

    /**
     * Accessor setter method for field <tt>forwardName</tt>
     *
     * @param forwardName
     *        The forwardName name request
     */
    public void setForwardName(String forwardName)
    {
        this.forwardName = forwardName;
    }

    /**
     * Accessor setter method for field <tt>type</tt>
     *
     * @param type
     *        The forwardName name request
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * Accessor setter method for field <tt>location</tt>
     *
     * @param location
     *        The location name request
     */
    public void setLocation(String location)
    {
        this.location = location;
    }

    /**
     * Accessor setter method for field <tt>group</tt>
     *
     * @param group
     *        The group name request
     */
    public void setGroup(String group)
    {
        this.group = group;
    }

    /**
     * Accessor setter method for field <tt>operation</tt>
     *
     * @param operation
     *        The operation name request
     */
    public void setOperation(String operation)
    {
        this.operation = operation;
    }

    /**
     * Accessor setter method for field <tt>component</tt>
     *
     * @param component
     *        The component name request
     */
    public void setComponent(String component)
    {
        this.component = component;
    }

    /**
     * Accessor setter method for object <tt>jmx</tt>
     *
     * @param jmx
     *        The jmx name request
     */
    public void setJmx(JMXEntryPoint jmx)
    {
        this.jmx = jmx;
    }

    /**
     * Accessor setter method for object <tt>server</tt>
     *
     * @param server
     *        The server name request
     */
    public void setServer(MBeanServer server)
    {
        this.server = server;
    }

    /**
     * Accessor setter method for object <tt>domain</tt>
     *
     * @param domain
     *        The domain name
     */
    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    /**
     * Accessor setter method for Set object <tt>objects</tt>
     *
     * @param objects
     *        The Set Object
     */
    public void setObjects(Set<ObjectName> objects)
    {
        this.objects = objects;
    }

    /**
     * Accessor setter method for Set object <tt>notificationObjects</tt>
     *
     * @param notificationObjects
     *        The Set Object
     */
    public void setNotificationObjects(Vector<Notification> notificationObjects)
    {
        notificationVector.setNotificationObjects(notificationObjects);
    }

    /**
     * This method set the notification type
     *
     * @param notificationTypes
     *        The notification type
     */
    public void setNotificationTypes(Set<Notification> notificationTypes)
    {
        notificationVector.setNotificationTypes(notificationTypes);
    }

    /**
     * Accessor setter method for Set object <tt>systems</tt>
     *
     * @param systemValues
     *        All systems configured
     */
    public void setSystemValues(String[] systemValues)
    {
        this.systemValues = systemValues;
    }

    /**
     * Accessor setter method for field <tt>filter</tt>: the query string
     *
     * @param filter
     *        The query string value
     */
    public void setFilter(String filter)
    {
        this.filter = filter;
    }

    /**
     * Accessor setter method for field <tt>systems</tt>
     *
     * @param systems
     *        The systems array value
     */
    public void setSystems(String systems)
    {
        this.systems = systems;
    }

    /**
     * Accessor setter method for field <tt>services</tt>
     *
     * @param services
     *        The services value
     */
    public void setServices(String services)
    {
        this.services = services;
    }

    /**
     * Accessor setter method for field <tt>groups</tt>
     *
     * @param groups
     *        The group value
     */
    public void setGroups(String groups)
    {
        this.groups = groups;
    }

    /**
     * Accessor setter method for field <tt>systemsServices</tt>
     *
     * @param systemServices
     *        The systemServices configured
     */
    public void setSystemServices(String systemServices)
    {
        this.systemServices = systemServices;
    }

    /**
     * Accessor setter method for field <tt>throughputSvc</tt>
     *
     * @param throughputSvc
     *        The throughputSvc value
     */
    public void setThroughputServices(Float throughputSvc)
    {
        this.throughputSvc = throughputSvc;
    }

    /**
     * Accessor setter method for field <tt>totalHintsSvc</tt>
     *
     * @param totalHintsSvc
     *        The totalHintsSvc value
     */
    public void setTotalHintsServices(Long totalHintsSvc)
    {
        this.totalHintsSvc = totalHintsSvc;
    }

    /**
     * Accessor setter method for field <tt>totalHintsNod</tt>
     *
     * @param totalHintsNod
     *        The totalHintsNod value
     */
    public void setTotalHintsNodes(Long totalHintsNod)
    {
        this.totalHintsNod = totalHintsNod;
    }

    /**
     * Accessor setter method for field <tt>throughputNod</tt>
     *
     * @param throughputNod
     *        The throughputNod value
     */
    public void setThroughputNodes(Float throughputNod)
    {
        this.throughputNod = throughputNod;
    }

    /**
     * Accessor setter method for field <tt>historyThroughputSvc</tt>
     *
     * @param historyThroughputSvc
     *        The historyThroughputSvc value
     */
    public void setHistoryThroughputServices(Float historyThroughputSvc)
    {
        this.historyThroughputSvc = historyThroughputSvc;
    }

    /**
     * Accessor setter method for field <tt>historyThroughputNod</tt>
     *
     * @param historyThroughputNod
     *        the historyThroughputNod value
     */
    public void setHistoryThroughputNodes(Float historyThroughputNod)
    {
        this.historyThroughputNod = historyThroughputNod;
    }

    /**
     * Accessor setter method for field <tt>maxThroughputNod</tt>
     *
     * @param maxThroughputNod
     *        The maxThroughputNod value
     */
    public void setMaxThroughputNodes(Object[] maxThroughputNod)
    {
        this.maxThroughputNod = maxThroughputNod;
    }

    /**
     * Accessor setter method for field <tt>maxThroughputSvc</tt>
     *
     * @param maxThroughputSvc
     *        The maxThroughputSvc value
     */
    public void setMaxThroughputServices(Object[] maxThroughputSvc)
    {
        this.maxThroughputSvc = maxThroughputSvc;
    }

    /**
     * Accessor setter method for field <tt>minThroughputNod</tt>
     *
     * @param minThroughputNod
     *        The minThroughputNod value
     */
    public void setMinThroughputNodes(Object[] minThroughputNod)
    {
        this.minThroughputNod = minThroughputNod;
    }

    /**
     * Accessor setter method for field <tt>minThroughputSvc</tt>
     *
     * @param minThroughputSvc
     *        The minThroughputSvc value
     */
    public void setMinThroughputServices(Object[] minThroughputSvc)
    {
        this.minThroughputSvc = minThroughputSvc;
    }

    /**
     * Accessor setter method for field <tt>internal</tt>
     *
     * @param internal
     *        The internal value
     */
    public void setInternal(String internal)
    {
        this.internal = internal;
    }

    /**
     * This method valorizes the notification message
     *
     * @param messageNotif
     *        The notification message
     */
    public void setNotificationMessage(String messageNotif)
    {
        this.messageNotif = messageNotif;
    }

    /**
     * Set the notification type for field <tt>notificationType</tt>
     *
     * @param notificationType
     *        The notification type
     */
    public void setNotificationType(String notificationType)
    {
        this.notificationType = notificationType;
    }

    /**
     * This method set the throughput url
     *
     * @param throughputUrl
     *        The url for the throughput we application
     */
    public void setThroughputUrl(String throughputUrl)
    {
        this.throughputUrl = throughputUrl;
    }

    /**
     * This method set the notification time
     *
     * @param timeStampNotif
     *        The time value
     */
    public void setNotificationTimeStamp(long timeStampNotif)
    {
        this.timeStampNotif = timeStampNotif;
    }

    /**
     * Accessor getter method for field <tt>system</tt>
     *
     * @return system the system name
     */
    public String getSystem()
    {
        return system;
    }

    /**
     * Accessor getter method for field <tt>service</tt>
     *
     * @return service the service name
     */
    public String getService()
    {
        return service;
    }

    /**
     * Accessor getter method for field <tt>forwardName</tt>
     *
     * @return forwardName the forwardName name
     */
    public String getForwardName()
    {
        return forwardName;
    }

    /**
     * Accessor getter method for field <tt>type</tt>
     *
     * @return type the type name
     */
    public String getType()
    {
        return type;
    }

    /**
     * Accessor getter method for field <tt>operation</tt>
     *
     * @return operation the operation name
     */
    public String getOperation()
    {
        return operation;
    }

    /**
     * Accessor getter method for field <tt>component</tt>
     *
     * @return component the component name
     */
    public String getComponent()
    {
        return component;
    }

    /**
     * Accessor getter method for field <tt>group</tt>
     *
     * @return group the group name
     */
    public String getGroup()
    {
        return group;
    }

    /**
     * Accessor getter method for field <tt>location</tt>
     *
     * @return location the location name
     */
    public String getLocation()
    {
        return location;
    }

    /**
     * Accessor getter method for field <tt>jmx</tt>
     *
     * @return jmx the jmx object
     */
    public JMXEntryPoint getJmx()
    {
        return jmx;
    }

    /**
     * Accessor getter method for field <tt>server</tt>
     *
     * @return server the server name
     */
    public MBeanServer getServer()
    {
        return server;
    }

    /**
     * Accessor getter method for field <tt>domain</tt>
     *
     * @return domain the domain
     */
    public String getDomain()
    {
        return domain;
    }

    /**
     * Accessor getter method for field <tt>objects</tt>
     *
     * @return objects the objects object
     */
    public Set<ObjectName> getObjects()
    {
        return objects;
    }

    /**
     * Accessor getter method for field <tt>notificationObjects</tt>
     *
     * @return notificationObjects the objects object
     */
    public Vector<Notification> getNotificationObjects()
    {
        return notificationVector.getNotificationObjects(notificationType);
    }

    /**
     * This method get the notification type
     *
     * @return The notification type in the vector for type
     */
    public Set<String> getNotificationTypes()
    {
        return notificationVector.getNotificationTypes();
    }

    /**
     * Get the groups value
     *
     * @return groups The groups list value
     */
    public String getGroups()
    {
        return groups;
    }

    /**
     * Get the systems value
     *
     * @return systems The systems list value
     */
    public String getSystems()
    {
        return systems;
    }

    /**
     * Get the groups list values
     *
     * @return services the services list value
     */
    public String getServices()
    {
        return services;
    }

    /**
     * Get the systemServices values
     *
     * @return systemServices the systemServices list value
     */
    public String getSystemServices()
    {
        return systemServices;
    }

    /**
     * Get the total hints values for services
     *
     * @return totalHintsSvc the total hints value for services
     */
    public Long getTotalHintsServices()
    {
        return totalHintsSvc;
    }

    /**
     * Get the total hints values for nodes
     *
     * @return totalHintsNod the total hints value for nodes
     */
    public Long getTotalHintsNodes()
    {
        return totalHintsNod;
    }

    /**
     * Get the throughput values for services
     *
     * @return throughput the throughput value for services
     */
    public Float getThroughputServices()
    {
        return throughputSvc;
    }

    /**
     * Get the throughput values for nodes
     *
     * @return throughput the throughput value for nodes
     */
    public Float getThroughputNodes()
    {
        return throughputNod;
    }

    /**
     * Get the average throughput values for Nodes
     *
     * @return historyThroughputNod the average throughput value for Nodes
     */
    public Float getHistoryThroughputNodes()
    {
        return historyThroughputNod;
    }

    /**
     * Get the history average throughput values for Services
     *
     * @return historyThroughputSvc the average throughput value for Services
     */
    public Float getHistoryThroughputServices()
    {
        return historyThroughputSvc;
    }

    /**
     * Get the maxThroughputSvc throughput values for Services
     *
     * @return maxThroughputSvc the max throughput value for Services
     */
    public Object[] getMaxThroughputServices()
    {
        return maxThroughputSvc;
    }

    /**
     * Get the maxThroughputSvc throughput values for Nodes
     *
     * @return maxThroughputSvc the max throughput value for Nodes
     */
    public Object[] getMaxThroughputNodes()
    {
        return maxThroughputNod;
    }

    /**
     * Get the minThroughputSvc throughput values for Services
     *
     * @return minThroughputSvc the min throughput value for Services
     */
    public Object[] getMinThroughputServices()
    {
        return minThroughputSvc;
    }

    /**
     * Get the minThroughputSvc throughput values for Nodes
     *
     * @return minThroughputSvc the min throughput value for Nodes
     */
    public Object[] getMinThroughputNodes()
    {
        return minThroughputNod;
    }

    /**
     * Get the internal values
     *
     * @return internal the internal list value
     */
    public String getInternal()
    {
        return internal;
    }

    /**
     * Get the maxNotificationObject values
     *
     * @return maxNotificationObject the maxNotificationObject value
     */
    public int getMaxNotificationObject()
    {
        return maxNotificationObject;
    }

    /**
     * Get the messageNotif values
     *
     * @return messageNotif the messageNotif value
     */
    public String getNotificationMessage()
    {
        return messageNotif;
    }

    /**
     * Get the typeNotif values
     *
     * @return typeNotif the typeNotif value
     */
    public String getNotificationType()
    {
        return notificationType;
    }

    /**
     * Get the typeNotif values
     *
     * @return typeNotif the typeNotif value
     */
    public long getNotificationTimeStamp()
    {
        return timeStampNotif;
    }

    /**
     * Get the throughput wgv application url values
     *
     * @return throughput the wgv application throughput url
     */
    public String getThroughputUrl()
    {
        return throughputUrl;
    }

    /**
     * Used by test plugin to read parameters from configuration file.
     *
     * @param configNode
     *        A DOM node corresponding to a section within XML configuration
     *        file
     * @throws Throwable
     *         If any error occurs.
     */
    public void init(Node configNode) throws Throwable
    {
        notificationVector = new NotificationVector(configNode);
        location = XMLConfig.get(configNode, "@defaultLocation");
        group = XMLConfig.get(configNode, "@defaultGroup", "");
        operation = XMLConfig.get(configNode, "@defaultOperation", "");
        component = XMLConfig.get(configNode, "@defaultComponent", "");
        internal = XMLConfig.get(configNode, "@internal", "");
        // serverUrl = XMLConfig.get(configNode, "@serverUrl", "");
        // user = XMLConfig.get(configNode, "@user", "");
        // password = XMLConfig.get(configNode, "@password", "");
        domain = XMLConfig.get(configNode, "@domain", "");

        // The throughput wgv application url IP:port
        //
        throughputUrl = XMLConfig.get(configNode, "Throughput/@throughputUrl", "");
        jmxNotFilter = XMLConfig.get(configNode, "@objectName", "");
        jmxNotFilter = PropertiesHandler.expand(jmxNotFilter);

        // Add GVMonitoringPlugin as listener for notification
        //
        if (component.equals("NotificationManagement")) {
            JMXUtils.addNotificationListener(jmxNotFilter, notificationVector, null, null, true, null);
            registered = true;
        }
    }

    /**
     * Used by the test plugin to clear all parameters.
     *
     * @param request
     *        An <tt>HttpServletRequest</tt> encapsulating incoming Http
     *        request.
     * @throws Throwable
     *         If any error occurs.
     */
    public void clear(HttpServletRequest request) throws Throwable
    {
        setSystem("");
        setService("");
        setGroup("management");
        setLocation("All");
        setInternal("No");
        setComponent("GVSystemServiceInfo");
    }

    /**
     * Used by the test plugin to reset all parameters.
     *
     * @param request
     *        An <tt>HttpServletRequest</tt> encapsulating incoming Http
     *        request.
     * @throws Throwable
     *         If any error occurs.
     */
    public void reset(HttpServletRequest request) throws Throwable
    {
        // Does nothing (main reset work done by the framework)
    }

    /**
     * Used by the test plugin to read the input parameters from the http
     * request and set its internal variables.
     *
     * @param request
     *        An <tt>HttpServletRequest</tt> encapsulating incoming Http
     *        request.
     * @throws Throwable
     *         If any error occurs.
     */
    public void prepareInput(HttpServletRequest request) throws Throwable
    {
        String method = request.getParameter("method");
        if (method != null) {
            if (method.equals("Submit")) {
                setInternal(request.getParameter("internalValue"));
                setLocation(request.getParameter("locationValue"));
                group = request.getParameter("groupValue");
                setGroup(group);
                if (group.equals("management")) {
                    setSystem(request.getParameter("systemValue"));
                    setService(request.getParameter("serviceValue"));
                    component = request.getParameter("componentValue");
                    setComponent(component);

                    if (component.equals("GVOperationInfo")) {
                        operation = request.getParameter("operationValue");
                        setOperation(operation);
                        if (operation.equals("Forward")) {
                            setForwardName(request.getParameter("forwardName"));
                        }
                    }
                    else {
                        if (component.equals("NotificationManagement")) {

                            // The type written in the input is principal.
                            // The type chooses in the combobox is secondary.
                            //
                            String notifType = request.getParameter("notificationType");
                            if ((notifType == null) || notifType.equals("")) {
                                notifType = request.getParameter("typeCombo");
                            }
                            setNotificationType(notifType);
                        }
                        else if (component.equals("GVBufferDump")) {
                            dumpSize = request.getParameter("dumpSize");
                            dump = new Integer(dumpSize);
                            setDumpSize(dumpSize);
                        }
                    }
                }
                else {
                    setSystem("");
                    setService("");
                    setComponent("");
                    setOperation("");
                    setForwardName("");
                    setDumpSize("0");
                }
            }
        }
    }

    /**
     * Get the Initial Context only one time Do nothing
     *
     * @return null
     * @throws Throwable
     *         If an error occurred
     */
    public InitialContext prepare() throws Throwable
    {
        return null;
    }

    /**
     * Used by the test plugin to return its available commands.
     *
     * @return A <tt>String</tt> array containing the names of plugin's
     *         available commands.
     */
    public String[] getAvailableCommands()
    {
        return new String[]{"Submit"};
    }

    /**
     * Invoked when the user perform an upload of local data.
     *
     * @param parameters
     *        An <tt>MultipartFormDataParser</tt> object.
     * @throws Throwable
     *         If any error occurs.
     */
    public void upload(MultipartFormDataParser parameters) throws Throwable
    {
        // Do nothing
    }

    /**
     * Used by the test plugin to perform the HTTP request and get the objects
     * as response
     *
     * @param request
     *        An <tt>HttpServletRequest</tt> encapsulating incoming Http
     *        request.
     * @return A <tt>boolean</tt> value indicating that the HTTP response can be
     *         shown.
     * @throws Throwable
     *         If any error occurs.
     */
    public boolean Submit(HttpServletRequest request) throws Throwable
    {
        ObjectName objectName = null;
        if (component.equals("NotificationManagement")) {
            if (!registered) {
                JMXUtils.addNotificationListener(jmxNotFilter, notificationVector, null, null, false, null);
                registered = true;
            }
        }
        else {
            if ((component.equals("ServiceManagement")) || (component.equals("Throughput"))) {
                filter = "GreenVulcano:Component=JMXServiceManager,Internal=No,*";
                if (component.equals("Throughput")) {
                    getThroughput();
                }
            }
            else if (component.equals("GVBufferDump")) {
                filter = "GreenVulcano:Component=GVBufferDump,Internal=No,*";
                JMXUtils.invoke(filter, "setDumpSize", new Object[]{service, system, dump}, new String[]{
                        "java.lang.String", "java.lang.String", "int"}, null);

            }
            else {
                filter = getFilter();
            }

            if (filter != null) {
                objectName = new ObjectName(filter);
                jmx = JMXEntryPoint.instance();
                server = jmx.getServer();
                objects = server.queryNames(objectName, null);
            }
        }
        return true;
    }

    /**
     * This method valorize the throughput counters
     *
     * @throws Throwable
     *         If an error occurred
     */
    private void getThroughput() throws Throwable
    {
        getHistoryThroughputNod();
        getHistoryThroughputSvc();
        getThroughputNod();
        getThroughputSvc();
        getMaxThroughputNod();
        getMaxThroughputSvc();
        getMinThroughputNod();
        getMinThroughputSvc();
        getTotalHintsSvc();
        getTotalHintsNod();
    }

    /**
     * This method creates the query string
     *
     * @return filter the query string
     * @throws Throwable
     *         If an error occurred
     */
    private String getFilter() throws Throwable
    {
        String header = "*:*";
        String filterL = header;
        filterL += ",Internal=" + internal;

        if (!system.equals("")) {
            filterL += ",IDSystem=" + system;
        }

        if (!service.equals("")) {
            filterL += ",IDService=" + service;
        }

        if (!location.equals("") && (!location.equals("All"))) {
            filterL += ",Location=" + location;
        }

        if (!group.equals("")) {
            filterL += ",Group=" + group;
        }

        if (!component.equals("")) {
            filterL += ",Component=" + component;
            if (component.equals("OperationInfo")) {
                if (!operation.equals("") && (!operation.equals("All"))) {
                    if (operation.equals("Forward")) {
                        filterL += ",IDOperation=" + forwardName;
                    }
                    else {
                        filterL += ",IDOperation=" + operation;
                    }
                }
            }
        }
        return filterL;
    }

    /**
     * This method active an GreenVulcano service / system / group
     *
     * @param request
     *        An <tt>HttpServletRequest</tt> encapsulating incoming Http
     *        request.
     * @return A <tt>boolean</tt> value indicating that the HTTP response can be
     *         shown.
     * @throws Throwable
     *         If any error occurs.
     */
    public boolean On(HttpServletRequest request) throws Throwable
    {
        String element = request.getParameter("element");
        if (groups != null) {
            invokeMethod("groupOn", element);
            groups = (String) getAttribute("groups");
            systems = null;
            services = null;
            systemServices = null;
        }

        if (services != null) {
            invokeMethod("serviceOn", element);
            services = (String) getAttribute("services");
            systems = null;
            groups = null;
            systemServices = null;
        }

        if (systems != null) {
            invokeMethod("systemOn", element);
            systems = (String) getAttribute("systems");
            groups = null;
            services = null;
            systemServices = null;
        }

        if (systemServices != null) {
            String element_one = request.getParameter("element_one");
            String operationName = request.getParameter("operationName");
            if (operationName != null) {
                invokeMethod("systemServiceOperationOn", element, element_one, operationName);
            }
            else {
                invokeMethod("systemServiceOn", element, element_one);
            }
            systemServices = (String) getAttribute("systemServices");
            groups = null;
            systems = null;
            services = null;
        }
        return true;
    }

    /**
     * This method disactive an GreenVulcano service / system / group
     *
     * @param request
     *        An <tt>HttpServletRequest</tt> encapsulating incoming Http
     *        request.
     * @return A <tt>boolean</tt> value indicating that the HTTP response can be
     *         shown.
     * @throws Throwable
     *         If any error occurs.
     */
    public boolean Off(HttpServletRequest request) throws Throwable
    {
        String element = request.getParameter("element");
        if (groups != null) {
            invokeMethod("groupOff", element);
            groups = (String) getAttribute("groups");
            systems = null;
            services = null;
            systemServices = null;
        }

        if (services != null) {
            invokeMethod("serviceOff", element);
            services = (String) getAttribute("services");
            groups = null;
            systems = null;
            systemServices = null;
        }

        if (systems != null) {
            invokeMethod("systemOff", element);
            systems = (String) getAttribute("systems");
            groups = null;
            services = null;
            systemServices = null;
        }

        if (systemServices != null) {
            String element_one = request.getParameter("element_one");
            String operationName = request.getParameter("operationName");
            if (operationName != null) {
                invokeMethod("systemServiceOperationOff", element, element_one, operationName);
            }
            else {
                invokeMethod("systemServiceOff", element, element_one);
            }
            systemServices = (String) getAttribute("systemServices");
            groups = null;
            systems = null;
            services = null;
        }
        return true;
    }

    /**
     * This method active All GreenVulcano service / system / group
     *
     * @param request
     *        An <tt>HttpServletRequest</tt> encapsulating incoming Http
     *        request.
     * @return A <tt>boolean</tt> value indicating that the HTTP response can be
     *         shown.
     * @throws Throwable
     *         If any error occurs.
     */
    public boolean allOn(HttpServletRequest request) throws Throwable
    {
        XMLUtils parser = null;
        String xpathList = "";
        String xpathLabel = "";
        Document document = null;
        try {
            parser = XMLUtils.getParserInstance();
            if (groups != null) {
                document = parser.parseDOM(groups);
                xpathList = "/GreenVulcanoStatus/Groups/Group";
                xpathLabel = "@group";

                NodeList nodeList = parser.selectNodeList(document, xpathList);
                for (int i = 0; i < nodeList.getLength(); ++i) {
                    Node node = nodeList.item(i);
                    String nome = parser.get(node, xpathLabel);
                    invokeMethod("groupOn", nome);
                }
                groups = (String) getAttribute("groups");
                systems = null;
                services = null;
                systemServices = null;
            }

            if (services != null) {
                document = parser.parseDOM(services);
                xpathList = "/GreenVulcanoStatus/Services/Service";
                xpathLabel = "@service";

                NodeList nodeList = parser.selectNodeList(document, xpathList);
                for (int i = 0; i < nodeList.getLength(); ++i) {
                    Node node = nodeList.item(i);
                    String nome = parser.get(node, xpathLabel);
                    invokeMethod("serviceOn", nome);
                }
                services = (String) getAttribute("services");
                systems = null;
                groups = null;
                systemServices = null;
            }

            if (systems != null) {
                document = parser.parseDOM(systems);
                xpathList = "/GreenVulcanoStatus/Systems/System";
                xpathLabel = "@system";

                NodeList nodeList = parser.selectNodeList(document, xpathList);
                for (int i = 0; i < nodeList.getLength(); ++i) {
                    Node node = nodeList.item(i);
                    String nome = parser.get(node, xpathLabel);
                    invokeMethod("systemOn", nome);
                }
                systems = (String) getAttribute("systems");
                groups = null;
                services = null;
                systemServices = null;
            }

            if (systemServices != null) {
                document = parser.parseDOM(systemServices);
                xpathList = "/GreenVulcanoStatus/SystemServices/SystemService";
                xpathLabel = "@system";
                String xpathLabelOne = "@service";

                NodeList nodeList = parser.selectNodeList(document, xpathList);
                for (int i = 0; i < nodeList.getLength(); ++i) {
                    Node node = nodeList.item(i);
                    String nome = parser.get(node, xpathLabel);
                    String nomeOne = parser.get(node, xpathLabelOne);
                    invokeMethod("systemServiceOn", nome, nomeOne);
                }
                systemServices = (String) getAttribute("systemServices");
                groups = null;
                services = null;
                systems = null;
            }
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
        return true;
    }

    /**
     * This method disable All GreenVulcano service / system / group
     *
     * @param request
     *        An <tt>HttpServletRequest</tt> encapsulating incoming HTTP
     *        request.
     * @return A <tt>boolean</tt> value indicating that the HTTP response can be
     *         shown.
     * @throws Throwable
     *         If any error occurs.
     */
    public boolean allOff(HttpServletRequest request) throws Throwable
    {
        XMLUtils parser = null;
        String xpathList = "";
        String xpathLabel = "";
        Document document = null;
        try {
            parser = XMLUtils.getParserInstance();

            if (groups != null) {
                document = parser.parseDOM(groups);
                xpathList = "/GreenVulcanoStatus/Groups/Group";
                xpathLabel = "@group";

                NodeList nodeList = parser.selectNodeList(document, xpathList);
                for (int i = 0; i < nodeList.getLength(); ++i) {
                    Node node = nodeList.item(i);
                    String nome = parser.get(node, xpathLabel);
                    invokeMethod("groupOff", nome);
                }
                groups = (String) getAttribute("groups");
                systems = null;
                services = null;
                systemServices = null;
            }

            if (services != null) {
                document = parser.parseDOM(services);
                xpathList = "/GreenVulcanoStatus/Services/Service";
                xpathLabel = "@service";

                NodeList nodeList = parser.selectNodeList(document, xpathList);
                for (int i = 0; i < nodeList.getLength(); ++i) {
                    Node node = nodeList.item(i);
                    String nome = parser.get(node, xpathLabel);
                    invokeMethod("serviceOff", nome);
                }
                services = (String) getAttribute("services");
                systems = null;
                groups = null;
                systemServices = null;
            }

            if (systems != null) {
                document = parser.parseDOM(systems);
                xpathList = "/GreenVulcanoStatus/Systems/System";
                xpathLabel = "@system";

                NodeList nodeList = parser.selectNodeList(document, xpathList);
                for (int i = 0; i < nodeList.getLength(); ++i) {
                    Node node = nodeList.item(i);
                    String nome = parser.get(node, xpathLabel);
                    invokeMethod("systemOff", nome);
                }
                systems = (String) getAttribute("systems");
                groups = null;
                services = null;
                systemServices = null;
            }

            if (systemServices != null) {
                document = parser.parseDOM(systemServices);
                xpathList = "/GreenVulcanoStatus/SystemServices/SystemService";
                xpathLabel = "@system";
                String xpathLabelOne = "@service";

                NodeList nodeList = parser.selectNodeList(document, xpathList);
                for (int i = 0; i < nodeList.getLength(); ++i) {
                    Node node = nodeList.item(i);
                    String nome = parser.get(node, xpathLabel);
                    String nomeOne = parser.get(node, xpathLabelOne);
                    invokeMethod("systemServiceOff", nome, nomeOne);
                }
                systemServices = (String) getAttribute("systemServices");
                groups = null;
                services = null;
                systems = null;
            }
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
        return true;
    }

    /**
     * This method active All GreenVulcano statistics
     *
     * @param request
     *        An <tt>HttpServletRequest</tt> encapsulating incoming Http
     *        request.
     * @return A <tt>boolean</tt> value indicating that the HTTP response can be
     *         shown.
     * @throws Throwable
     *         If any error occurs.
     */
    public boolean statisticsAllOn(HttpServletRequest request) throws Throwable
    {
        XMLUtils parser = null;
        String xpathList = "";
        String xpathLabel = "";
        Document document = null;
        try {
            parser = XMLUtils.getParserInstance();

            if (systemServices != null) {
                document = parser.parseDOM(systemServices);
                xpathList = "/GreenVulcanoStatus/SystemServices/SystemService";
                xpathLabel = "@system";
                String xpathLabelOne = "@service";

                NodeList nodeList = parser.selectNodeList(document, xpathList);
                for (int i = 0; i < nodeList.getLength(); ++i) {
                    Node node = nodeList.item(i);
                    String nome = parser.get(node, xpathLabel);
                    String nomeOne = parser.get(node, xpathLabelOne);
                    invokeMethod("statisticsOn", nome, nomeOne);
                }
                systemServices = (String) getAttribute("systemServices");
                groups = null;
                services = null;
                systems = null;
            }
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
        return true;
    }

    /**
     * This method disactive All GreenVulcano statistics
     *
     * @param request
     *        An <tt>HttpServletRequest</tt> encapsulating incoming Http
     *        request.
     * @return A <tt>boolean</tt> value indicating that the HTTP response can be
     *         shown.
     * @throws Throwable
     *         If any error occurs.
     */
    public boolean statisticsAllOff(HttpServletRequest request) throws Throwable
    {
        XMLUtils parser = null;
        String xpathList = "";
        String xpathLabel = "";
        Document document = null;
        try {
            parser = XMLUtils.getParserInstance();

            if (systemServices != null) {
                document = parser.parseDOM(systemServices);
                xpathList = "/GreenVulcanoStatus/SystemServices/SystemService";
                xpathLabel = "@system";
                String xpathLabelOne = "@service";

                NodeList nodeList = parser.selectNodeList(document, xpathList);
                for (int i = 0; i < nodeList.getLength(); ++i) {
                    Node node = nodeList.item(i);
                    String nome = parser.get(node, xpathLabel);
                    String nomeOne = parser.get(node, xpathLabelOne);
                    invokeMethod("statisticsOff", nome, nomeOne);
                }
                systemServices = (String) getAttribute("systemServices");
                groups = null;
                services = null;
                systems = null;
            }
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
        return true;
    }

    /**
     * This method disactive All GreenVulcano operation for the system service
     * requested
     *
     * @param request
     *        An <tt>HttpServletRequest</tt> encapsulating incoming Http
     *        request.
     * @return A <tt>boolean</tt> value indicating that the HTTP response can be
     *         shown.
     * @throws Throwable
     *         If any error occurs.
     */
    public boolean allOperationOff(HttpServletRequest request) throws Throwable
    {
        String element = request.getParameter("element");
        String elementOne = request.getParameter("element_one");
        String xpathOperation = "/GreenVulcanoStatus/SystemServices/SystemService[(@system='" + element
                + "') and (@service='" + elementOne + "') ]/Operation";
        String xpathOperationName = "@operation";
        XMLUtils parser = XMLUtils.getParserInstance();
        try {
            Document document = parser.parseDOM(systemServices);

            NodeList nodeList = parser.selectNodeList(document, xpathOperation);
            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                String operationName = parser.get(node, xpathOperationName);
                invokeMethod("systemServiceOperationOff", element, elementOne, operationName);
            }
            systemServices = (String) getAttribute("systemServices");
            groups = null;
            services = null;
            systems = null;
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
        return true;
    }

    /**
     * This method active All GreenVulcano operation for the system service
     * requested
     *
     * @param request
     *        An <tt>HttpServletRequest</tt> encapsulating incoming Http
     *        request.
     * @return A <tt>boolean</tt> value indicating that the HTTP response can be
     *         shown.
     * @throws Throwable
     *         If any error occurs.
     */
    public boolean allOperationOn(HttpServletRequest request) throws Throwable
    {
        String element = request.getParameter("element");
        String elementOne = request.getParameter("element_one");
        String xpathOperation = "/GreenVulcanoStatus/SystemServices/SystemService[(@system='" + element
                + "') and (@service='" + elementOne + "') ]/Operation";
        String xpathOperationName = "@operation";
        XMLUtils parser = XMLUtils.getParserInstance();
        try {
            Document document = parser.parseDOM(systemServices);

            NodeList nodeList = parser.selectNodeList(document, xpathOperation);
            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                String operationName = parser.get(node, xpathOperationName);
                invokeMethod("systemServiceOperationOn", element, elementOne, operationName);
            }
            systemServices = (String) getAttribute("systemServices");
            groups = null;
            services = null;
            systems = null;
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
        return true;
    }

    /**
     * This method active All GreenVulcano operation for the system service
     * requested
     *
     * @param request
     *        An <tt>HttpServletRequest</tt> encapsulating incoming Http
     *        request.
     * @return A <tt>boolean</tt> value indicating that the HTTP response can be
     *         shown.
     * @throws Throwable
     *         if any error occurs.
     */
    public boolean statisticsOn(HttpServletRequest request) throws Throwable
    {
        String element = request.getParameter("element");
        String elementOne = request.getParameter("element_one");
        invokeMethod("statisticsOn", element, elementOne);
        systemServices = (String) getAttribute("systemServices");
        groups = null;
        services = null;
        systems = null;

        return true;
    }

    /**
     * This method active All GreenVulcano operation for the system service
     * requested
     *
     * @param request
     *        An <tt>HttpServletRequest</tt> encapsulating incoming Http
     *        request.
     * @return A <tt>boolean</tt> value indicating that the HTTP response can be
     *         shown.
     * @throws Throwable
     *         If any error occurs.
     */
    public boolean statisticsOff(HttpServletRequest request) throws Throwable
    {
        String element = request.getParameter("element");
        String elementOne = request.getParameter("element_one");
        invokeMethod("statisticsOff", element, elementOne);
        systemServices = (String) getAttribute("systemServices");
        groups = null;
        services = null;
        systems = null;

        return true;
    }

    /**
     * This method reads the serviceConfig.xml file to find the list of system
     * configured.
     *
     * @return systemValues the configured system list
     * @throws Throwable
     *         If an error occurred
     */
    public String[] getSystemValues() throws Throwable
    {
        String systemXPath = "/GVSystems/Systems/System/@id-system";
        NodeList node = null;
        NodeList nodeList = null;
        int nodeListLenght = 0;

        try {
            node = XMLConfig.getNodeList(GreenVulcanoConfig.getSystemsConfigFileName(), systemXPath);
            nodeList = node;
            nodeListLenght = nodeList.getLength();
            systemValues = new String[nodeListLenght];
            for (int ind = 0; ind < nodeListLenght; ++ind) {
                String val = XMLConfig.getNodeValue(nodeList.item(ind));
                if (val != null) {
                    systemValues[ind] = val;
                }
            }
        }
        catch (XMLConfigException exc) {
            // do nothing
        }
        return systemValues;
    }

    /**
     * Should not be called, does nothing. Only exist to work with beans.
     *
     * @param serviceValues
     */
    public void setServiceValues(String[] serviceValues)
    {
    }

    /**
     * This method reads serviceConfig.xml file to find the list of system
     * configured.
     *
     * @return serviceValues the configured service list
     * @throws Throwable
     *         If an error occurred
     */
    public String[] getServiceValues() throws Throwable
    {
        String[] serviceValuesL = null;
        String serviceXPath = "/GVServices/Services/Service/@id-service";
        NodeList node = null;
        NodeList nodeList = null;
        int nodeListLenght = 0;

        try {
            node = XMLConfig.getNodeList(GreenVulcanoConfig.getServicesConfigFileName(), serviceXPath);
            nodeList = node;
            nodeListLenght = nodeList.getLength();
            serviceValuesL = new String[nodeListLenght];
            for (int ind = 0; ind < nodeListLenght; ++ind) {
                String val = XMLConfig.getNodeValue(nodeList.item(ind));
                if (val != null) {
                    serviceValuesL[ind] = val;
                }
            }
        }
        catch (XMLConfigException exc) {
            // do nothing
        }

        return serviceValuesL;
    }

    /**
     * This method set a boolean to know which is the requested list.
     *
     * @param request
     *        The HttpServletRequest
     * @return systemValues The service list is requested
     * @throws Throwable
     *         If an error occurred
     */
    public boolean Services(HttpServletRequest request) throws Throwable
    {
        services = (String) getAttribute("services");
        systems = null;
        groups = null;
        systemServices = null;
        return true;
    }

    /**
     * This method reads serviceConfig.xml file to find the list of system
     * configured.
     *
     * @param request
     *        the HttpServletRequest
     * @return systemValues the configured system list
     * @throws Throwable
     *         If an error occurred
     */
    public boolean Systems(HttpServletRequest request) throws Throwable
    {
        systems = (String) getAttribute("systems");
        services = null;
        groups = null;
        systemServices = null;
        return true;
    }

    /**
     * This method reads serviceConfig.xml file to find the list of group
     * configured.
     *
     * @param request
     *        The HttpServletRequest
     * @return groups true if the groups list is requested
     * @throws Throwable
     *         If an error occurred
     */
    public boolean Groups(HttpServletRequest request) throws Throwable
    {
        groups = (String) getAttribute("groups");
        systems = null;
        services = null;
        systemServices = null;
        return true;
    }

    /**
     * This method reads the serviceConfig.xml file to find the list of
     * systemServices.
     *
     * @param request
     *        The HttpServletRequest
     * @throws Throwable
     *         If an error occurred
     * @return true if the systemservice list is requested
     */
    public boolean SystemServices(HttpServletRequest request) throws Throwable
    {
        systemServices = (String) getAttribute("systemServices");
        systems = null;
        services = null;
        groups = null;
        return true;
    }

    /**
     * Synchronize all systems and services status in all servers.
     *
     * @param request
     *        HttpServletRequest object
     * @return true if a syncronized action is executed
     * @throws Throwable
     *         If an error occurred
     */
    public boolean synchronize(HttpServletRequest request) throws Throwable
    {
        invokeMethod("synchronizeStatus");
        return true;
    }

    /**
     * Synchronize system and service selected status in all servers.
     *
     * @param request
     *        HttpServletRequest object
     * @return true if a syncronize action is executed
     * @throws Throwable
     *         If an error occurred
     */
    public boolean synchronizeSystemServices(HttpServletRequest request) throws Throwable
    {
        String systemInput = request.getParameter("element");
        String serviceInput = request.getParameter("element_one");
        invokeMethod("synchronizeStatus", systemInput, serviceInput);
        return true;
    }

    /**
     * Reset all systems and services counters in all servers.
     *
     * @param request
     *        HttpServletRequest object
     * @return true if a reset counter action is executed
     * @throws Throwable
     *         If an error occurred
     */
    public boolean resetCounter(HttpServletRequest request) throws Throwable
    {
        invokeMethod("resetCounter");
        return true;
    }

    /**
     * Reset counter for systems and services selected in all servers.
     *
     * @param request
     *        HttpServletRequest object
     * @return true if the rest counter for system and services is executed
     * @throws Throwable
     *         If an error occurred
     */
    public boolean resetSystemServiceCounter(HttpServletRequest request) throws Throwable
    {
        String systemInput = request.getParameter("element");
        String serviceInput = request.getParameter("element_one");
        invokeMethod("resetCounter", systemInput, serviceInput);
        return true;
    }

    /**
     * Invoke the requested method by JMX this method keep in input: <br>
     * The query string created, The method to execute, The param value, The
     * param type
     *
     * @param methodName
     *        The operation name
     * @return the jmx object
     * @throws Throwable
     *         If an error occurred
     */
    private Object invokeMethod(String methodName) throws Throwable
    {
        Object[] params = new Object[0];
        String[] signature = new String[0];
        Object object = JMXUtils.invoke(filter, methodName, params, signature, null);
        return object;
    }

    /**
     * Invoke the requested method by JMX this method keep in input: <br>
     * The query string created, The method to execute, The param value, The
     * param type
     *
     * @param methodName
     *        The operation name
     * @param param
     *        The input parameter for the jmx operation
     * @return The jmx object
     * @throws Throwable
     *         If an error occurred
     */
    private Object invokeMethod(String methodName, Object param) throws Throwable
    {
        Object[] params = new Object[]{param};
        String[] signature = new String[]{"java.lang.String"};
        Object object = JMXUtils.invoke(filter, methodName, params, signature, null);
        return object;
    }

    /**
     * Invoke the requested method by JMX this method keep in input: <br>
     * The query string created, The method to execute, The param value, The
     * param type
     *
     * @param methodName
     *        The operation name
     * @param paramOne
     *        The input parameter for the jmx operation
     * @param paramTwo
     *        The input parameter for the jmx operation
     * @return The jmx object
     * @throws Throwable
     *         If an error occurred
     */
    private Object invokeMethod(String methodName, Object paramOne, Object paramTwo) throws Throwable
    {
        Object[] params = new Object[]{paramOne, paramTwo};
        String[] signature = new String[]{"java.lang.String", "java.lang.String"};
        Object object = JMXUtils.invoke(filter, methodName, params, signature, null);
        return object;
    }

    /**
     * Invoke the requested method by JMX this method keep in input: <br>
     * The query string created, The method to execute, The param value, The
     * param type
     *
     * @param methodName
     *        the operation name
     * @param paramOne
     *        the input parameter for the jmx operation
     * @param paramTwo
     *        the input parameter for the jmx operation
     * @param paramThree
     *        the input parameter for the jmx operation
     * @return The jmx object
     * @throws Throwable
     *         If an error occurred
     */
    private Object invokeMethod(String methodName, Object paramOne, Object paramTwo, Object paramThree)
            throws Throwable
    {
        Object[] params = new Object[]{paramOne, paramTwo, paramThree};
        String[] signature = new String[]{"java.lang.String", "java.lang.String", "java.lang.String"};
        Object object = JMXUtils.invoke(filter, methodName, params, signature, null);
        return object;
    }

    private Object getAttribute(String attributeName) throws Throwable
    {
        Object[] result = JMXUtils.get(filter, attributeName, null);
        if (result == null) {
            return null;
        }
        if (result.length == 0) {
            return null;
        }
        return result[0];
    }

    /**
     * This method get the history Average throughput fo nodes
     *
     * @return historyThroughputNod The history average throughput for nodes
     * @throws Throwable
     *         If an error occurred
     */
    public boolean getHistoryThroughputNod() throws Throwable
    {
        historyThroughputNod = (Float) getAttribute("historyThroughputNod");
        return true;
    }

    /**
     * This method get the history Average throughput fo services
     *
     * @return historyThroughputSvc The history average throughput for services
     * @throws Throwable
     *         If an error occurred
     */
    public boolean getHistoryThroughputSvc() throws Throwable
    {
        historyThroughputSvc = (Float) getAttribute("historyThroughputSvc");
        return true;
    }

    /**
     * This method get the throughput fo services
     *
     * @return throughput The throughput for services
     * @throws Throwable
     *         If an error occurred
     */
    public boolean getThroughputSvc() throws Throwable
    {
        throughputSvc = (Float) getAttribute("throughputSvc");
        return true;
    }

    /**
     * This method get the throughput fo nodes
     *
     * @return throughput The throughput for nodes
     * @throws Throwable
     *         If an error occurred
     */
    public boolean getThroughputNod() throws Throwable
    {
        throughputNod = (Float) getAttribute("throughputNod");
        return true;
    }

    /**
     * This method get the max throughput for nodes
     *
     * @return maxThroughput The max throughput for nodes
     * @throws Throwable
     *         If an error occurred
     */
    public boolean getMaxThroughputNod() throws Throwable
    {
        maxThroughputNod = (Object[]) getAttribute("maxThroughputNod");
        return true;
    }

    /**
     * This method get the max throughput for services
     *
     * @return maxThroughput The max throughput for services
     * @throws Throwable
     *         If an error occurred
     */
    public boolean getMaxThroughputSvc() throws Throwable
    {
        maxThroughputSvc = (Object[]) getAttribute("maxThroughputSvc");
        return true;
    }

    /**
     * This method get the min throughput for nodes
     *
     * @return minThroughput The min throughput for nodes
     * @throws Throwable
     *         If an error occurred
     */
    public boolean getMinThroughputNod() throws Throwable
    {
        minThroughputNod = (Object[]) getAttribute("minThroughputNod");
        return true;
    }

    /**
     * This method get the min throughput for services
     *
     * @return minThroughput The min throughput for nodes
     * @throws Throwable
     *         If an error occurred
     */
    public boolean getMinThroughputSvc() throws Throwable
    {
        minThroughputSvc = (Object[]) getAttribute("minThroughputSvc");
        return true;
    }

    /**
     * This method get the total Hints for Services
     *
     * @return totalHintsSvc The min total Hints for services
     * @throws Throwable
     *         If an error occurred
     */
    public boolean getTotalHintsSvc() throws Throwable
    {
        totalHintsSvc = (Long) getAttribute("totalHintsSvc");
        return true;
    }

    /**
     * This method get the total Hints for Nodes
     *
     * @return totalHintsNod The min total Hints for nodes
     * @throws Throwable
     *         If an error occurred
     */
    public boolean getTotalHintsNod() throws Throwable
    {
        totalHintsNod = (Long) getAttribute("totalHintsNod");
        return true;
    }

    /**
     * @param request
     * @return null
     * @throws Throwable
     */
    public InitialContext prepare(HttpServletRequest request) throws Throwable
    {
        return null;
    }

    /**
     * @param request
     * @param testType
     * @throws Throwable
     */
    public void prepareInput(HttpServletRequest request, String testType) throws Throwable
    {
        // do nothing
    }

    /**
     * @param request
     * @param testObject
     * @param testType
     * @throws Throwable
     */
    public void prepareInput(HttpServletRequest request, TestGVBufferObject testObject, String testType)
            throws Throwable
    {
        // do nothing
    }

    /**
     * @param parameters
     * @param testObject
     * @throws Throwable
     */
    public void uploadMultiple(MultipartFormDataParser parameters, TestGVBufferObject testObject) throws Throwable
    {
        // do nothing
    }

    /**
     * @param testObject
     * @return null
     * @throws Throwable
     */
    public InitialContext prepare(TestGVBufferObject testObject) throws Throwable
    {
        return null;
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#updateDataInput(java.lang.String,
     *      java.lang.String)
     */
    public void updateDataInput(String data, String encoding) throws Throwable
    {
        // do nothing
    }

    /**
     * @param request
     * @param testObject
     * @param testType
     * @param number
     * @throws Throwable
     */
    public void prepareInput(HttpServletRequest request, TestGVBufferObject testObject, String testType, int number)
            throws Throwable
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#prepareInput(javax.servlet.http.HttpServletRequest,
     *      it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestObject,
     *      java.lang.String, int)
     */
    public void prepareInput(HttpServletRequest request, TestObject testObject, String testType, int number)
            throws Throwable
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#uploadMultiple(it.greenvulcano.gvesb.gvconsole.workbench.plugin.MultipartFormDataParser,
     *      it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestObject)
     */
    public void uploadMultiple(MultipartFormDataParser parameters, TestObject testObject) throws Throwable
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#prepare(it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestObject)
     */
    public InitialContext prepare(TestObject testObject) throws Throwable
    {
        return null;
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#setResetValue(java.lang.String)
     */
    public void setResetValue(String resetValue)
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#getResetValue()
     */
    public String getResetValue()
    {
        return null;
    }

    /**
     * @param fileNameI
     * @throws Throwable
     */
    public void getDataInput(String fileNameI) throws Throwable
    {
        // do nothing

    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#saveData(java.lang.String)
     */
    public void saveData(String fileNameI) throws Throwable
    {
        // do nothing

    }

    /**
     * @see it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin#savedData(java.lang.String)
     */
    public void savedData(String fileNameI) throws Throwable
    {
        // do nothing

    }

    /**
     * @return Returns the dumpSize.
     */
    public String getDumpSize()
    {
        return dumpSize;
    }

    /**
     * @param dumpSize
     *        The dumpSize to set.
     */
    public void setDumpSize(String dumpSize)
    {
        this.dumpSize = dumpSize;
    }
}