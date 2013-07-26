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
package it.greenvulcano.jboss.jmx;

import it.greenvulcano.gvesb.j2ee.JNDIHelper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.loading.ClassLoaderRepository;
import javax.naming.Context;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.security.auth.callback.UsernamePasswordHandler;

/**
 * Every node in the cluster should have an instance of this class running
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class DistributedMBeanServer implements MBeanServer
{

    private String                               rmiAdaptorName;
    private ObjectName                           hajndiMBeanName;
    private ObjectName                           haPartition;

    private String                               userName;
    private String                               password;
    private String                               loginContext;

    private JNDIHelper                           jndiContext;

    /**
     *
     */
    protected MBeanServer                        jbossLocalMBeanServer = null;

    /**
     *
     */
    protected Map<String, MBeanServerConnection> jbossMBeanServers;

    /**
     * JAAS Subject to use in order to access the MBeanServer.
     */
    private Subject                              subject               = new Subject();

    /**
     * @param jndiContext
     * @param hajndiMBeanName
     * @param rmiAdaptorName
     * @param userName
     * @param password
     * @param loginContext
     * @throws Exception
     */
    public DistributedMBeanServer(JNDIHelper jndiContext, String hajndiMBeanName, String rmiAdaptorName,
            String userName, String password, String loginContext) throws Exception
    {
        this.hajndiMBeanName = new ObjectName(hajndiMBeanName);
        this.rmiAdaptorName = rmiAdaptorName;
        this.jndiContext = jndiContext;
        this.userName = userName;
        this.password = password;
        this.loginContext = loginContext;

        buildSubject();

        findMBeanServers();
    }

    // --------------------------------------------------------------------------
    // MBeanServer interface
    // --------------------------------------------------------------------------

    /**
     * Adds a listener to an MBean registered on the DistributedMBeanServer.
     * <p>
     * A notification emitted by an MBean will be forwarded by the MBeanServer
     * to the listener, the MBeanServer will substitute the notification source
     * by the emitter's ObjectName, if the the orginal source of the
     * notification is not an ObjectName.
     * 
     * @param name
     *        The name of the MBean on which the listener should be added.
     * @param listener
     *        The listener object which will handle the notifications emitted by
     *        the registered MBean.
     * @param filter
     *        The filter object. If filter is null, no filtering will be
     *        performed before handling notifications.
     * @param handback
     *        The context to be sent to the listener when a notification is
     *        emitted
     * @throws InstanceNotFoundException
     *         The MBean name provided does not match any of the registered
     *         MBeans
     */
    @Override
    public void addNotificationListener(final ObjectName name, final NotificationListener listener,
            final NotificationFilter filter, final Object handback) throws InstanceNotFoundException
    {
        try {
            Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws RuntimeException, InstanceNotFoundException, IOException
                {
                    MBeanServerConnection theMBeanServer = getOwningMBeanServer(name);
                    theMBeanServer.addNotificationListener(name, listener, filter, handback);
                    return null;
                }
            });
        }
        catch (PrivilegedActionException paex) {
            Exception exc = paex.getException();
            if (exc instanceof RuntimeException) {
                invalidateServer(name);
                throw new InstanceNotFoundException("" + exc);
            }
            else if (exc instanceof IOException) {
                invalidateServer(name);
                throw new InstanceNotFoundException("" + exc);
            }
            managePrivilegedActionException6(paex);
        }
    }

    /**
     * Adds a listener to an MBean registered on the DistributedMBeanServer.
     * <p>
     * A notification emitted by an MBean will be forwarded by the MBeanServer
     * to the listener, the MBeanServer will substitute the notification source
     * by the emitter's ObjectName, if the the orginal source of the
     * notification is not an ObjectName.
     * 
     * @param name
     *        The name of the MBean on which the listener should be added.
     * @param listener
     *        The object name of the listener which will handle the
     *        notifications emitted by the registered MBean
     * @param filter
     *        The filter object. If filter is null, no filtering will be
     *        performed before handling notifications.
     * @param handback
     *        The context to be sent to the listener when a notification is
     *        emitted
     * @throws InstanceNotFoundException
     *         The MBean name provided does not match any of the registered
     *         MBeans
     */
    @Override
    public void addNotificationListener(final ObjectName name, final ObjectName listener,
            final NotificationFilter filter, final Object handback) throws InstanceNotFoundException
    {
        try {
            Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws RuntimeException, InstanceNotFoundException, IOException
                {
                    MBeanServerConnection theMBeanServer = getOwningMBeanServer(name);
                    theMBeanServer.addNotificationListener(name, listener, filter, handback);
                    return null;
                }
            });
        }
        catch (PrivilegedActionException paex) {
            Exception exc = paex.getException();
            if (exc instanceof RuntimeException) {
                invalidateServer(name);
                throw new InstanceNotFoundException("" + exc);
            }
            else if (exc instanceof IOException) {
                invalidateServer(name);
                throw new InstanceNotFoundException("" + exc);
            }
            managePrivilegedActionException6(paex);
        }
    }

    /**
     * Instantiates and registers an MBean on the LOCAL MBean server. The MBean
     * server will use its Default Loader Repository to load the class of the
     * MBean. An object name is associated to the MBean. If the object name
     * given is null, the MBean can automatically provide its own name by
     * implementing the <tt>MBeanRegistration</tt> interface. The call returns
     * an <tt>ObjectInstance</tt> object representing the newly created MBean.
     * 
     * @param className
     *        The class name of the MBean to be instantiated.
     * @param name
     *        The object name of the MBean. May be null.
     * @return An <tt>ObjectInstance</tt>, containing the <tt>ObjectName</tt>
     *         and the Java class name of the newly instantiated MBean.
     * @throws ReflectionException
     *         Wraps a <tt>java.lang.ClassNotFoundException</tt> or a
     *         <tt>java.lang.Exception</tt> that occurred when trying to invoke
     *         the MBean's constructor.
     * @throws InstanceAlreadyExistsException
     *         The MBean is already under the control of the Distributed MBean
     *         server.
     * @throws MBeanRegistrationException
     *         The <tt>preRegister</tt> (<tt>MBeanRegistration</tt> interface)
     *         method of the MBean has thrown an exception. The MBean will not
     *         be registered.
     * @throws MBeanException
     *         The constructor of the MBean has thrown an exception
     * @throws NotCompliantMBeanException
     *         This class is not a JMX compliant MBean
     * @throws RuntimeOperationsException
     *         Wraps a <tt>java.lang.IllegalArgumentException</tt>: The
     *         className passed in parameter is null, the <tt>ObjectName</tt>
     *         passed in parameter contains a pattern or no <tt>ObjectName</tt>
     *         is specified for the MBean.
     */
    @Override
    public ObjectInstance createMBean(final String className, final ObjectName name) throws ReflectionException,
            InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<ObjectInstance>() {
                @Override
                public ObjectInstance run() throws ReflectionException, InstanceAlreadyExistsException,
                        MBeanRegistrationException, MBeanException, NotCompliantMBeanException
                {

                    MBeanServerConnection theMBeanServer = null;
                    try {
                        theMBeanServer = getOwningMBeanServer(name);
                    }
                    catch (InstanceNotFoundException exc) {
                        throw new ReflectionException(exc);
                    }

                    try {
                        return theMBeanServer.createMBean(className, name);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(name);
                        throw new ReflectionException(exc);
                    }
                    catch (IOException exc) {
                        invalidateServer(name);
                        throw new ReflectionException(exc);
                    }
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException2(paex);
            managePrivilegedActionException3(paex);
            return null;
        }
    }

    /**
     * Instantiates and registers an MBean in the MBean server. The class loader
     * to be used is identified by its object name. An object name is associated
     * to the MBean. If the object name of the loader is null, the ClassLoader
     * that loaded the MBean server will be used. If the MBean's object name
     * given is null, the MBean can automatically provide its own name by
     * implementing the <tt>MBeanRegistration</tt> interface. The call returns
     * an <tt>ObjectInstance</tt> object representing the newly created MBean.
     * 
     * @param className
     *        The class name of the MBean to be instantiated.
     * @param name
     *        The object name of the MBean. May be null.
     * @param loaderName
     * @return An <tt>ObjectInstance</tt>, containing the <tt>ObjectName</tt>
     *         and the Java class name of the newly instantiated MBean.
     * @throws ReflectionException
     *         Wraps a <tt>java.lang.ClassNotFoundException</tt> or a
     *         <tt>java.lang.Exception</tt> that occurred when trying to invoke
     *         the MBean's constructor.
     * @throws InstanceAlreadyExistsException
     *         The MBean is already under the control of the MBean server.
     * @throws MBeanRegistrationException
     *         The <tt>preRegister</tt> (<tt>MBeanRegistration</tt> interface)
     *         method of the MBean has thrown an exception. The MBean will not
     *         be registered.
     * @throws MBeanException
     *         The constructor of the MBean has thrown an exception
     * @throws NotCompliantMBeanException
     *         This class is not a JMX compliant MBean
     * @throws InstanceNotFoundException
     *         The specified class loader is not registered in the MBean server.
     * @throws RuntimeOperationsException
     *         Wraps a <tt>java.lang.IllegalArgumentException</tt>: The
     *         className passed in parameter is null, the <tt>ObjectName</tt>
     *         passed in parameter contains a pattern or no <tt>ObjectName</tt>
     *         is specified for the MBean.
     */
    @Override
    public ObjectInstance createMBean(final String className, final ObjectName name, final ObjectName loaderName)
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
            NotCompliantMBeanException, InstanceNotFoundException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<ObjectInstance>() {
                @Override
                public ObjectInstance run() throws ReflectionException, InstanceAlreadyExistsException,
                        MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
                        InstanceNotFoundException
                {

                    MBeanServerConnection theMBeanServer = getOwningMBeanServer(name);
                    try {
                        return theMBeanServer.createMBean(className, name, loaderName);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    catch (IOException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException1(paex);
            managePrivilegedActionException3(paex);
            return null;
        }
    }

    /**
     * @param className
     * @param name
     * @param loaderName
     * @param params
     * @param signature
     * @return the <code>ObjectInstance</code> object
     * @throws ReflectionException
     * @throws InstanceAlreadyExistsException
     * @throws MBeanRegistrationException
     * @throws MBeanException
     * @throws NotCompliantMBeanException
     * @throws InstanceNotFoundException
     * 
     */
    @Override
    public ObjectInstance createMBean(final String className, final ObjectName name, final ObjectName loaderName,
            final Object[] params, final String[] signature) throws ReflectionException,
            InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
            InstanceNotFoundException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<ObjectInstance>() {
                @Override
                public ObjectInstance run() throws ReflectionException, InstanceAlreadyExistsException,
                        MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
                        InstanceNotFoundException
                {

                    MBeanServerConnection theMBeanServer = getOwningMBeanServer(name);
                    try {
                        return theMBeanServer.createMBean(className, name, loaderName, params, signature);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    catch (IOException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException1(paex);
            managePrivilegedActionException3(paex);
            return null;
        }
    }

    /**
     * @param className
     * @param name
     * @param params
     * @param signature
     * @return the <code>ObjectInstance</code> object
     * @throws ReflectionException
     * @throws InstanceAlreadyExistsException
     * @throws MBeanRegistrationException
     * @throws MBeanException
     * @throws NotCompliantMBeanException
     */
    @Override
    public ObjectInstance createMBean(final String className, final ObjectName name, final Object[] params,
            final String[] signature) throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException, NotCompliantMBeanException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<ObjectInstance>() {
                @Override
                public ObjectInstance run() throws ReflectionException, InstanceAlreadyExistsException,
                        MBeanRegistrationException, MBeanException, NotCompliantMBeanException
                {

                    MBeanServerConnection theMBeanServer = null;
                    try {
                        theMBeanServer = getOwningMBeanServer(name);
                    }
                    catch (InstanceNotFoundException exc) {
                        throw new ReflectionException(exc);
                    }

                    try {
                        return theMBeanServer.createMBean(className, name, params, signature);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(name);
                        throw new ReflectionException(exc);
                    }
                    catch (IOException exc) {
                        invalidateServer(name);
                        throw new ReflectionException(exc);
                    }
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException2(paex);
            managePrivilegedActionException3(paex);
            return null;
        }
    }

    /**
     * @param className
     * @param loaderName
     * @param data
     * @return the <code>ObjectInputStream</code> object
     * @throws InstanceNotFoundException
     * @throws OperationsException
     * @throws ReflectionException
     * 
     */
    @Override
    @Deprecated
    public ObjectInputStream deserialize(final String className, final ObjectName loaderName, final byte[] data)
            throws InstanceNotFoundException, OperationsException, ReflectionException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<ObjectInputStream>() {
                @Override
                public ObjectInputStream run() throws InstanceNotFoundException, OperationsException,
                        ReflectionException
                {

                    MBeanServer theMBeanServer = jbossLocalMBeanServer;
                    try {
                        return theMBeanServer.deserialize(className, loaderName, data);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(loaderName);
                        throw new InstanceNotFoundException("" + exc);
                    }
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException1(paex);
            managePrivilegedActionException4(paex);
            return null;
        }
    }

    /**
     * @param className
     * @param data
     * @return the <code>ObjectInputStream</code> object
     * @throws OperationsException
     * @throws ReflectionException
     * @deprecated
     * 
     * @deprecated
     */
    @Override
    @Deprecated
    public ObjectInputStream deserialize(final String className, final byte[] data) throws OperationsException,
            ReflectionException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<ObjectInputStream>() {
                @Override
                public ObjectInputStream run() throws OperationsException, ReflectionException
                {
                    return jbossLocalMBeanServer.deserialize(className, data);
                }
            });
        }
        catch (PrivilegedActionException paex) {
            Exception exc = paex.getException();
            if (exc instanceof OperationsException) {
                throw (OperationsException) exc;
            }
            else if (exc instanceof ReflectionException) {
                throw (ReflectionException) exc;
            }
            return null;
        }
    }

    /**
     * @param name
     * @param data
     * @return the <code>ObjectInputStream</code> object
     * @throws InstanceNotFoundException
     * @throws OperationsException
     */
    @Override
    @Deprecated
    public ObjectInputStream deserialize(final ObjectName name, final byte[] data) throws InstanceNotFoundException,
            OperationsException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<ObjectInputStream>() {
                @Override
                public ObjectInputStream run() throws InstanceNotFoundException, OperationsException
                {

                    MBeanServer theMBeanServer = jbossLocalMBeanServer;
                    try {
                        return theMBeanServer.deserialize(name, data);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException4(paex);
            return null;
        }
    }

    /**
     * @param name
     * @param attribute
     * @return the attribute value
     * @throws MBeanException
     * @throws AttributeNotFoundException
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     * 
     */
    @Override
    public Object getAttribute(final ObjectName name, final String attribute) throws MBeanException,
            AttributeNotFoundException, InstanceNotFoundException, ReflectionException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws MBeanException, AttributeNotFoundException, InstanceNotFoundException,
                        ReflectionException
                {
                    MBeanServerConnection theMBeanServer = getOwningMBeanServer(name);
                    try {
                        return theMBeanServer.getAttribute(name, attribute);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    catch (IOException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException1(paex);
            managePrivilegedActionException2(paex);
            Exception exc = paex.getException();
            if (exc instanceof AttributeNotFoundException) {
                throw (AttributeNotFoundException) exc;
            }
            return null;
        }
    }

    /**
     * TBW - Write Javadocs
     * 
     * @param name
     * @param attributes
     * @return the attribute values
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     */
    @Override
    public AttributeList getAttributes(final ObjectName name, final String[] attributes)
            throws InstanceNotFoundException, ReflectionException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<AttributeList>() {
                @Override
                public AttributeList run() throws InstanceNotFoundException, ReflectionException
                {

                    MBeanServerConnection theMBeanServer = getOwningMBeanServer(name);
                    try {
                        return theMBeanServer.getAttributes(name, attributes);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    catch (IOException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException1(paex);
            return null;
        }
    }

    /**
     * @return the default domain
     */
    @Override
    public String getDefaultDomain()
    {
        return jbossLocalMBeanServer.getDefaultDomain();
    }

    /**
     * @return the mbeans count
     */
    @Override
    public Integer getMBeanCount()
    {
        ObjectName objName = null;
        try {
            objName = new ObjectName("*:*");

        }
        catch (MalformedObjectNameException ex) {
            // This will never happen because "*:*" is a well-formed ObjectName
        }
        Set<?> namesSet = queryNames(objName, null);
        return new Integer(namesSet.size());
    }

    /**
     * @param name
     * @return the MBeanInfo
     * @throws InstanceNotFoundException
     * @throws IntrospectionException
     * @throws ReflectionException
     * @see MBeanServer#getMBeanInfo(ObjectName)
     */
    @Override
    public MBeanInfo getMBeanInfo(final ObjectName name) throws InstanceNotFoundException, IntrospectionException,
            ReflectionException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<MBeanInfo>() {
                @Override
                public MBeanInfo run() throws InstanceNotFoundException, IntrospectionException, ReflectionException
                {
                    MBeanServerConnection theMBeanServer = getOwningMBeanServer(name);
                    try {
                        return theMBeanServer.getMBeanInfo(name);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    catch (IOException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException1(paex);
            Exception exc = paex.getException();
            if (exc instanceof IntrospectionException) {
                throw (IntrospectionException) exc;
            }
            return null;
        }
    }

    /**
     * @param name
     * @return the ObjectInstance object
     * @throws InstanceNotFoundException
     * @see MBeanServer#getObjectInstance(ObjectName)
     */
    @Override
    public ObjectInstance getObjectInstance(final ObjectName name) throws InstanceNotFoundException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<ObjectInstance>() {
                @Override
                public ObjectInstance run() throws InstanceNotFoundException
                {
                    MBeanServerConnection theMBeanServer = getOwningMBeanServer(name);
                    try {
                        return theMBeanServer.getObjectInstance(name);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    catch (IOException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException6(paex);
            return null;
        }
    }

    /**
     * @param className
     * @return the instantiated object
     * @throws ReflectionException
     * @throws MBeanException
     * @see MBeanServer#instantiate(String)
     */
    @Override
    public Object instantiate(final String className) throws ReflectionException, MBeanException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws ReflectionException, MBeanException
                {
                    return jbossLocalMBeanServer.instantiate(className);
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException2(paex);
            return null;
        }
    }

    /**
     * @param className
     * @param loaderName
     * @return the instantiated object
     * @throws ReflectionException
     * @throws MBeanException
     * @throws InstanceNotFoundException
     * @see MBeanServer#instantiate(String, ObjectName)
     */
    @Override
    public Object instantiate(final String className, final ObjectName loaderName) throws ReflectionException,
            MBeanException, InstanceNotFoundException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws ReflectionException, MBeanException, InstanceNotFoundException
                {
                    MBeanServer theMBeanServer = jbossLocalMBeanServer; // getOwningMBeanServer(loaderName);
                    try {
                        return theMBeanServer.instantiate(className, loaderName);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(loaderName);
                        throw new InstanceNotFoundException("" + exc);
                    }
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException1(paex);
            managePrivilegedActionException2(paex);
            return null;
        }
    }

    /**
     * @param className
     * @param loaderName
     * @param params
     * @param signature
     * @return the instantiated object
     * @throws ReflectionException
     * @throws MBeanException
     * @throws InstanceNotFoundException
     * @see MBeanServer#instantiate(String, ObjectName, Object[], String[])
     */
    @Override
    public Object instantiate(final String className, final ObjectName loaderName, final Object[] params,
            final String[] signature) throws ReflectionException, MBeanException, InstanceNotFoundException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws ReflectionException, MBeanException, InstanceNotFoundException
                {

                    MBeanServer theMBeanServer = jbossLocalMBeanServer;
                    try {
                        return theMBeanServer.instantiate(className, loaderName, params, signature);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(loaderName);
                        throw new InstanceNotFoundException("" + exc);
                    }
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException1(paex);
            managePrivilegedActionException2(paex);
            return null;
        }
    }

    /**
     * @param className
     * @param params
     * @param signature
     * @return the instantiated object
     * @throws ReflectionException
     * @throws MBeanException
     * @see MBeanServer#instantiate(String, Object[], String[])
     */
    @Override
    public Object instantiate(final String className, final Object[] params, final String[] signature)
            throws ReflectionException, MBeanException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws ReflectionException, MBeanException
                {

                    return jbossLocalMBeanServer.instantiate(className, params, signature);
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException2(paex);
            return null;
        }
    }

    /**
     * @param name
     * @param operationName
     * @param params
     * @param signature
     * @return the invocation result
     * @throws InstanceNotFoundException
     * @throws MBeanException
     * @throws ReflectionException
     * @see MBeanServer#invoke(ObjectName, String, Object[], String[])
     */
    @Override
    public Object invoke(final ObjectName name, final String operationName, final Object[] params,
            final String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws InstanceNotFoundException, MBeanException, ReflectionException
                {

                    MBeanServerConnection theMBeanServer = getOwningMBeanServer(name);
                    try {
                        return theMBeanServer.invoke(name, operationName, params, signature);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("ObjectName: " + name + ". " + exc);
                    }
                    catch (IOException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("ObjectName: " + name + ". " + exc);
                    }
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException1(paex);
            managePrivilegedActionException2(paex);
            return null;
        }
    }

    /**
     * @param name
     * @param className
     * @return the instanceof operator result on MBeanServer objects
     * @throws InstanceNotFoundException
     */
    @Override
    public boolean isInstanceOf(final ObjectName name, final String className) throws InstanceNotFoundException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<Boolean>() {
                @Override
                public Boolean run() throws InstanceNotFoundException
                {

                    MBeanServerConnection theMBeanServer = getOwningMBeanServer(name);
                    try {
                        return theMBeanServer.isInstanceOf(name, className);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    catch (IOException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException6(paex);
            return false;
        }
    }

    /**
     * Checks whether an MBean, identified by its object name, is already
     * registered on the <tt>DistributedMBeanServer</tt> (that is to say, is
     * already registered on the <tt>MBeanServer</tt> of one of the active
     * cluster nodes)
     * 
     * @param name
     *        The object name of the MBean to be checked
     * @return True if the MBean is already registered in the MBean server,
     *         false otherwise
     * @throws RuntimeOperationsException
     *         Wraps a <tt>java.lang.IllegalArgumentException</tt>: The object
     *         name in parameter is null.
     */
    @Override
    public boolean isRegistered(final ObjectName name)
    {
        return Subject.doAs(subject, new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run()
            {

                MBeanServerConnection theMBeanServer = null;
                try {
                    theMBeanServer = getOwningMBeanServer(name);
                }
                catch (InstanceNotFoundException exc) {
                    return false;
                }

                try {
                    return theMBeanServer.isRegistered(name);
                }
                catch (RuntimeException exc) {
                    invalidateServer(name);
                    return false;
                }
                catch (IOException exc) {
                    invalidateServer(name);
                    return false;
                }
            }
        });
    }

    /**
     * @param name
     * @param query
     * @return the set of <code>ObjectName</code>
     */
    @Override
    @SuppressWarnings("unchecked")
    public Set queryMBeans(final ObjectName name, final QueryExp query)
    {
        return Subject.doAs(subject, new PrivilegedAction<Set>() {
            @Override
            public Set run()
            {

                if (name != null) {

                    // Se e' fornito un nome con una location, proviamo un
                    // algoritmo piu' furbo...
                    //
                    String location = name.getKeyProperty("Location");
                    if (location != null) {
                        try {
                            MBeanServerConnection theMBeanServer = getOwningMBeanServer(name);
                            return theMBeanServer.queryMBeans(name, query);
                        }
                        catch (InstanceNotFoundException exc) {
                            exc.printStackTrace();
                            // Then continue with the other algorithm
                        }
                        catch (RuntimeException exc) {
                            invalidateServer(name);
                            // Continue with the other algoritthm
                        }
                        catch (IOException exc) {
                            invalidateServer(name);
                            // Continue with the other algoritthm
                        }
                    }
                }

                // ... altrimenti interroghiamo tutti i servers

                Set<ObjectName> distinctMBeans = new HashSet<ObjectName>();
                List<MBeanServerConnection> activeMBeanServers = getActiveMBeanServerList();
                for (MBeanServerConnection currMBeanServer : activeMBeanServers) {
                    try {
                        Set currMBeanServerMBeans = currMBeanServer.queryMBeans(name, query);
                        distinctMBeans.addAll(currMBeanServerMBeans);
                    }
                    catch (Exception exc) {
                        invalidateServer(name);
                    }
                }
                return distinctMBeans;
            }
        });
    }

    /**
     * @param name
     * @param query
     * @return the set of
     */
    @Override
    @SuppressWarnings("unchecked")
    public Set queryNames(final ObjectName name, final QueryExp query)
    {
        return Subject.doAs(subject, new PrivilegedAction<Set>() {
            @Override
            public Set run()
            {
                if (name != null) {
                    // Se e' fornito un nome con una location, proviamo un
                    // algoritmo piu' furbo...
                    //
                    String location = name.getKeyProperty("Location");
                    if (location != null) {
                        try {
                            MBeanServerConnection theMBeanServer = getOwningMBeanServer(name);
                            return theMBeanServer.queryNames(name, query);
                        }
                        catch (InstanceNotFoundException exc) {
                            exc.printStackTrace();
                            // Then continue with the other algorithm
                        }
                        catch (RuntimeException exc) {
                            invalidateServer(name);
                            // Then continue with the other algorothm
                        }
                        catch (IOException exc) {
                            invalidateServer(name);
                            // Then continue with the other algorothm
                        }
                    }
                }

                // ... altrimenti interroghiamo tutti i servers

                Set<ObjectName> distinctNames = new HashSet<ObjectName>();
                List<MBeanServerConnection> activeMBeanServers = getActiveMBeanServerList();
                for (MBeanServerConnection currMBeanServer : activeMBeanServers) {
                    try {
                        Set<ObjectName> currMBeanServerNames = currMBeanServer.queryNames(name, query);
                        distinctNames.addAll(currMBeanServerNames);
                    }
                    catch (Exception exc) {
                        invalidateServer(name);
                    }
                }
                return distinctNames;
            }
        });
    }

    /**
     * @param object
     * @param name
     * @return the registered mbean
     * @throws InstanceAlreadyExistsException
     * @throws MBeanRegistrationException
     * @throws NotCompliantMBeanException
     * @see MBeanServer#registerMBean(Object, ObjectName)
     */
    @Override
    public ObjectInstance registerMBean(final Object object, final ObjectName name)
            throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<ObjectInstance>() {
                @Override
                public ObjectInstance run() throws InstanceAlreadyExistsException, MBeanRegistrationException,
                        NotCompliantMBeanException
                {
                    MBeanServer theMBeanServer = jbossLocalMBeanServer;
                    try {
                        return theMBeanServer.registerMBean(object, name);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(name);
                        throw new MBeanRegistrationException(exc);
                    }
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException3(paex);
            return null;
        }
    }

    /**
     * @param name
     * @param listener
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @see MBeanServerConnection#removeNotificationListener(ObjectName,
     *      NotificationListener)
     */
    @Override
    public void removeNotificationListener(final ObjectName name, final NotificationListener listener)
            throws InstanceNotFoundException, ListenerNotFoundException
    {
        try {
            Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws InstanceNotFoundException, ListenerNotFoundException
                {

                    MBeanServerConnection theMBeanServer = getOwningMBeanServer(name);
                    try {
                        theMBeanServer.removeNotificationListener(name, listener);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    catch (IOException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    return null;
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException5(paex);
        }
    }

    /**
     * @param name
     * @param listener
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @see MBeanServerConnection#removeNotificationListener(ObjectName,
     *      ObjectName)
     */
    @Override
    public void removeNotificationListener(final ObjectName name, final ObjectName listener)
            throws InstanceNotFoundException, ListenerNotFoundException
    {
        try {
            Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws InstanceNotFoundException, ListenerNotFoundException
                {

                    MBeanServerConnection theMBeanServer = getOwningMBeanServer(name);
                    try {
                        theMBeanServer.removeNotificationListener(name, listener);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    catch (IOException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    return null;
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException5(paex);
        }
    }

    /**
     * @param name
     * @param attribute
     * @throws InstanceNotFoundException
     * @throws AttributeNotFoundException
     * @throws InvalidAttributeValueException
     * @throws MBeanException
     * @throws ReflectionException
     * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
     */
    @Override
    public void setAttribute(final ObjectName name, final Attribute attribute) throws InstanceNotFoundException,
            AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
        try {
            Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws InstanceNotFoundException, AttributeNotFoundException,
                        InvalidAttributeValueException, MBeanException, ReflectionException
                {
                    MBeanServerConnection theMBeanServer = getOwningMBeanServer(name);
                    try {
                        theMBeanServer.setAttribute(name, attribute);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    catch (IOException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    return null;
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException1(paex);
            managePrivilegedActionException2(paex);
            Exception exc = paex.getException();
            if (exc instanceof AttributeNotFoundException) {
                throw (AttributeNotFoundException) exc;
            }
            else if (exc instanceof InvalidAttributeValueException) {
                throw (InvalidAttributeValueException) exc;
            }
        }
    }

    /**
     * @param name
     * @param attributes
     * @return the attributes list set
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     * @see MBeanServerConnection#setAttributes(ObjectName, AttributeList)
     */
    @Override
    public AttributeList setAttributes(final ObjectName name, final AttributeList attributes)
            throws InstanceNotFoundException, ReflectionException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<AttributeList>() {
                @Override
                public AttributeList run() throws InstanceNotFoundException, ReflectionException
                {
                    MBeanServerConnection theMBeanServer = getOwningMBeanServer(name);
                    try {
                        return theMBeanServer.setAttributes(name, attributes);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    catch (IOException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException1(paex);
            return null;
        }
    }

    /**
     * @param name
     * @throws InstanceNotFoundException
     * @throws MBeanRegistrationException
     * @see MBeanServerConnection#unregisterMBean(ObjectName)
     */
    @Override
    public void unregisterMBean(final ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException
    {
        try {
            Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws InstanceNotFoundException, MBeanRegistrationException
                {
                    MBeanServerConnection theMBeanServer = getOwningMBeanServer(name);
                    try {
                        theMBeanServer.unregisterMBean(name);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    catch (IOException exc) {
                        invalidateServer(name);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    return null;
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException6(paex);
            Exception exc = paex.getException();
            if (exc instanceof MBeanRegistrationException) {
                throw (MBeanRegistrationException) exc;
            }
        }
    }

    /**
     * Helper method for managing of the exception encapsulated into the given
     * PrivilegedActionException.
     */
    private void managePrivilegedActionException1(PrivilegedActionException paex) throws ReflectionException,
            InstanceNotFoundException
    {
        Exception exc = paex.getException();
        if (exc instanceof ReflectionException) {
            throw (ReflectionException) exc;
        }
        else if (exc instanceof InstanceNotFoundException) {
            throw (InstanceNotFoundException) exc;
        }
    }

    /**
     * Helper method for managing of the exception encapsulated into the given
     * PrivilegedActionException.
     */
    private void managePrivilegedActionException2(PrivilegedActionException paex) throws ReflectionException,
            MBeanException
    {
        Exception exc = paex.getException();
        if (exc instanceof ReflectionException) {
            throw (ReflectionException) exc;
        }
        else if (exc instanceof MBeanException) {
            throw (MBeanException) exc;
        }
    }

    /**
     * Helper method for managing of the exception encapsulated into the given
     * PrivilegedActionException.
     */
    private void managePrivilegedActionException3(PrivilegedActionException paex)
            throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException
    {
        Exception exc = paex.getException();
        if (exc instanceof InstanceAlreadyExistsException) {
            throw (InstanceAlreadyExistsException) exc;
        }
        else if (exc instanceof MBeanRegistrationException) {
            throw (MBeanRegistrationException) exc;
        }
        else if (exc instanceof NotCompliantMBeanException) {
            throw (NotCompliantMBeanException) exc;
        }
    }

    /**
     * Helper method for managing of the exception encapsulated into the given
     * PrivilegedActionException.
     */
    private void managePrivilegedActionException4(PrivilegedActionException paex) throws InstanceNotFoundException,
            OperationsException
    {
        Exception exc = paex.getException();
        if (exc instanceof InstanceNotFoundException) {
            throw (InstanceNotFoundException) exc;
        }
        else if (exc instanceof OperationsException) {
            throw (OperationsException) exc;
        }
    }

    /**
     * Helper method for managing of the exception encapsulated into the given
     * PrivilegedActionException.
     */
    private void managePrivilegedActionException5(PrivilegedActionException paex) throws InstanceNotFoundException,
            ListenerNotFoundException
    {
        Exception exc = paex.getException();
        if (exc instanceof InstanceNotFoundException) {
            throw (InstanceNotFoundException) exc;
        }
        else if (exc instanceof ListenerNotFoundException) {
            throw (ListenerNotFoundException) exc;
        }
    }

    /**
     * Helper method for managing of the exception encapsulated into the given
     * PrivilegedActionException.
     */
    private void managePrivilegedActionException6(PrivilegedActionException paex) throws InstanceNotFoundException
    {
        Exception exc = paex.getException();
        if (exc instanceof InstanceNotFoundException) {
            throw (InstanceNotFoundException) exc;
        }
    }

    /**
     * @param name
     */
    protected void invalidateServer(ObjectName name)
    {
        if (name == null) {
            return;
        }
        String location = name.getKeyProperty("Location");
        if (location != null) {
            jbossMBeanServers.remove(location);
        }
    }

    /**
     * Returns the MBeanServer within the cluster on which an MBean (whose
     * <tt>ObjectName</tt> is <tt>name</tt>) is registered, or null if the MBean
     * is not registered on any of the MBeanServer within the cluster.
     * 
     * @param name
     *        a given <tt>ObjectName</tt>
     * @return the MBeanServer within the cluster on which an MBean (whose
     *         <tt>ObjectName</tt> is <tt>name</tt>) is registered, or null if
     *         the MBean is not registered on any of the MBeanServer within the
     *         cluster.
     * @throws InstanceNotFoundException
     */
    protected MBeanServerConnection getOwningMBeanServer(ObjectName name) throws InstanceNotFoundException
    {

        // Il nome del server � contenuto nella property 'Location'
        //
        String location = name.getKeyProperty("Location");
        if (location == null) {

            // Se la property 'Location' non � definita, allora restituiamo il
            // server
            // locale.
            //
            return jbossLocalMBeanServer;
        }

        MBeanServerConnection server = jbossMBeanServers.get(location);
        if (server == null) {
            try {
                server = findMBeanServer(location);
                if (server == null) {
                    throw new InstanceNotFoundException("Cannot find server " + location);
                }
            }
            catch (Exception exc) {
                exc.printStackTrace();
                throw new InstanceNotFoundException("Cannot find server " + location + ". Cause: " + exc);
            }
        }
        return server;
    }

    /**
     * Finds the MBeanServers for all JBoss servers and initializes the internal
     * structures.
     */
    private void findMBeanServers() throws Exception
    {
        jbossMBeanServers = new HashMap<String, MBeanServerConnection>();
        jbossLocalMBeanServer = MBeanServerLocator.locateJBoss();

        String partitionName = (String) jbossLocalMBeanServer.getAttribute(hajndiMBeanName, "PartitionName");
        haPartition = new ObjectName("jboss:service=HAPartition,partition=" + partitionName);

        Vector<?> currentView = (Vector<?>) jbossLocalMBeanServer.getAttribute(haPartition, "CurrentView");
        System.out.println("DistributedMBeanServer - list: " + currentView);

        ObjectName serverConfig = new ObjectName("jboss.system:type=ServerConfig");
        for (Iterator<?> it = currentView.iterator(); it.hasNext();) {
            try {
                String clusterNode = (String) it.next();
                jndiContext.setProperty(Context.PROVIDER_URL, "jnp://" + clusterNode);

                MBeanServerConnection rmiAdaptor = null;
                try {
                    rmiAdaptor = (MBeanServerConnection) jndiContext.lookup(rmiAdaptorName);
                }
                finally {
                    jndiContext.close();
                }

                String serverName = (String) rmiAdaptor.getAttribute(serverConfig, "ServerName");
                System.out.println("DistributedMBeanServer - jbossMBeanServers add: " + serverName);
                jbossMBeanServers.put(serverName, rmiAdaptor);
            }
            catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    private MBeanServerConnection findMBeanServer(String searchedServerName) throws Exception
    {
        findMBeanServers();
        return jbossMBeanServers.get(searchedServerName);
    }

    /**
     * @return the list of active MBeanServers
     */
    protected List<MBeanServerConnection> getActiveMBeanServerList()
    {
        try {
            findMBeanServers();
        }
        catch (Exception exc) {
            exc.printStackTrace();
            return new LinkedList<MBeanServerConnection>();
        }
        return new LinkedList<MBeanServerConnection>(jbossMBeanServers.values());
    }

    /**
     * @return null
     * @throws InstanceNotFoundException
     */
    protected MBeanServer getAdminMBeanServer() throws InstanceNotFoundException
    {
        return null;
    }

    private void buildSubject() throws Exception
    {
        UsernamePasswordHandler handler = new UsernamePasswordHandler(userName, password.toCharArray());
        LoginContext lc = new LoginContext(loginContext, handler);
        lc.login();
        subject = lc.getSubject();
    }

    /**
     * @see javax.management.MBeanServer#getClassLoader(javax.management.ObjectName)
     */
    @Override
    public ClassLoader getClassLoader(final ObjectName objectName) throws InstanceNotFoundException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<ClassLoader>() {
                @Override
                public ClassLoader run() throws RuntimeException, InstanceNotFoundException, IOException
                {
                    MBeanServer theMBeanServer = jbossLocalMBeanServer;
                    return theMBeanServer.getClassLoader(objectName);
                }
            });
        }
        catch (PrivilegedActionException paex) {
            Exception exc = paex.getException();
            if (exc instanceof RuntimeException) {
                invalidateServer(objectName);
                throw new InstanceNotFoundException("" + exc);
            }
            else if (exc instanceof IOException) {
                invalidateServer(objectName);
                throw new InstanceNotFoundException("" + exc);
            }
            managePrivilegedActionException6(paex);
            return null;
        }
    }

    /**
     * @see javax.management.MBeanServer#getClassLoaderFor(javax.management.ObjectName)
     */
    @Override
    public ClassLoader getClassLoaderFor(final ObjectName objectName) throws InstanceNotFoundException
    {
        try {
            return Subject.doAs(subject, new PrivilegedExceptionAction<ClassLoader>() {
                @Override
                public ClassLoader run() throws RuntimeException, InstanceNotFoundException, IOException
                {
                    MBeanServer theMBeanServer = jbossLocalMBeanServer;
                    return theMBeanServer.getClassLoader(objectName);
                }
            });
        }
        catch (PrivilegedActionException paex) {
            Exception exc = paex.getException();
            if (exc instanceof RuntimeException) {
                invalidateServer(objectName);
                throw new InstanceNotFoundException("" + exc);
            }
            else if (exc instanceof IOException) {
                invalidateServer(objectName);
                throw new InstanceNotFoundException("" + exc);
            }
            managePrivilegedActionException6(paex);
            return null;
        }
    }

    /**
     * @see javax.management.MBeanServer#getClassLoaderRepository()
     */
    @Override
    public ClassLoaderRepository getClassLoaderRepository()
    {
        return jbossLocalMBeanServer.getClassLoaderRepository();
    }

    /**
     * @see javax.management.MBeanServerConnection#getDomains()
     */
    @Override
    public String[] getDomains()
    {
        return jbossLocalMBeanServer.getDomains();
    }

    /**
     * @see javax.management.MBeanServerConnection#removeNotificationListener(javax
     *      .management.ObjectName, javax.management.NotificationListener,
     *      javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void removeNotificationListener(final ObjectName objectName,
            final NotificationListener notificationListener, final NotificationFilter notificationFilter,
            final Object object) throws InstanceNotFoundException, ListenerNotFoundException
    {
        try {
            Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws InstanceNotFoundException, ListenerNotFoundException
                {

                    MBeanServerConnection theMBeanServer = getOwningMBeanServer(objectName);
                    try {
                        theMBeanServer.removeNotificationListener(objectName, notificationListener, notificationFilter,
                                object);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(objectName);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    catch (IOException exc) {
                        invalidateServer(objectName);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    return null;
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException5(paex);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#removeNotificationListener(javax
     *      .management.ObjectName, javax.management.ObjectName,
     *      javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void removeNotificationListener(final ObjectName objectName, final ObjectName notificationListenerName,
            final NotificationFilter notificationFilter, final Object object) throws InstanceNotFoundException,
            ListenerNotFoundException
    {
        try {
            Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws InstanceNotFoundException, ListenerNotFoundException
                {

                    MBeanServerConnection theMBeanServer = getOwningMBeanServer(objectName);
                    try {
                        theMBeanServer.removeNotificationListener(objectName, notificationListenerName,
                                notificationFilter, object);
                    }
                    catch (RuntimeException exc) {
                        invalidateServer(objectName);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    catch (IOException exc) {
                        invalidateServer(objectName);
                        throw new InstanceNotFoundException("" + exc);
                    }
                    return null;
                }
            });
        }
        catch (PrivilegedActionException paex) {
            managePrivilegedActionException5(paex);
        }
    }
}