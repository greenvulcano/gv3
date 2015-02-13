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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The <code>NotificationVector</code> class manages the Notification object.
 * Every notification type select its own notification vector in an hashmap.
 * There is one vector for every notification type.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class NotificationVector implements NotificationListener, Serializable
{

    private static final long                 serialVersionUID = 300L;

    /**
     * HashMap containing the notification fields of configuration file. Type
     * and maxNotificationObject for the type
     */
    private Map<String, Integer>              configMap        = new HashMap<String, Integer>();

    /**
     * HashMap containing the notification vector for types.
     */
    private Map<String, Vector<Notification>> notificationMap  = new HashMap<String, Vector<Notification>>();

    /**
     * The constructor read the notification information in the configuration
     * file. The notification type and the maximum notification number for every
     * type. These information was inserted in a hashmap with type as key. For
     * every notification type found a new Vector is created (with no element).
     *
     * @param node
     *        The configuration node
     * @throws Throwable
     *         If an error occurred
     */
    public NotificationVector(Node node) throws Throwable
    {
        try {
            NodeList notificationNodes = XMLConfig.getNodeList(node, "Notification");
            if (notificationNodes != null) {
                int nodesNum = notificationNodes.getLength();
                if (nodesNum > 0) {
                    for (int i = 0; i < nodesNum; i++) {
                        String notFilterType = XMLConfig.get(notificationNodes.item(i), "@notFilterType");
                        int maxNotificationObject = XMLConfig.getInteger(notificationNodes.item(i),
                                "@maxNotificationObject", 0);

                        // valorize an hashmap object with type as key and the
                        // maximum element number for the type
                        //
                        configMap.put(notFilterType, maxNotificationObject);

                        // Create a new vector for every type
                        //
                        getNotificationObjects(notFilterType);
                    }
                }
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Implemented method of notification listener. Valorize the vector for
     * every notification type, but inserts element in the specific vector only
     * until the maximum notification number (read in the configuration file) is
     * reached.
     *
     * @param notification
     *        The notification object
     * @param obj
     *        the handback object
     */
    public void handleNotification(Notification notification, Object obj)
    {
        String notificationType = notification.getType();

        // Get the notification form the vector for the type requested
        //
        Vector<Notification> notifVector = notificationMap.get(notificationType);
        if (notifVector == null) {
            notifVector = new Vector<Notification>();
            notificationMap.put(notificationType, notifVector);
        }

        notifVector.add(notification);

        // Check if the maximum number is reached
        //
        Integer maxI = configMap.get(notificationType);

        int maxType = 0;
        if (maxI != null) {
            maxType = maxI.intValue();
        }
        else {
            // The default notification number in the vector is 20
            //
            configMap.put(notificationType, 20);
            maxType = 20;
        }

        // Remove the old notification objects
        //
        while (notifVector.size() > maxType) {
            notifVector.remove(0);
        }
    }

    /**
     * Accessor getter method for field <tt>notificationVector</tt> if no vector
     * is found for the requested type a new vector is created.
     *
     * @param type
     *        The notification type request by user in the web page
     * @return notificationVector the notification vector for the requested type
     */
    public Vector<Notification> getNotificationObjects(String type)
    {
        if ((type == null) || type.equals("")) {
            return new Vector<Notification>();
        }
        Vector<Notification> notifVector = notificationMap.get(type);

        if (notifVector == null) {
            notifVector = new Vector<Notification>();
            notificationMap.put(type, notifVector);
        }

        return notifVector;
    }

    /**
     * Get all notification types.
     *
     * @return notificationTypes the key set
     */
    public Set<String> getNotificationTypes()
    {
        return notificationMap.keySet();
    }

    /**
     * Do Nothing
     *
     * @param notificationObjects
     *        The vector containing notification object
     */
    public void setNotificationObjects(Vector<Notification> notificationObjects)
    {
        // do nothing
    }

    /**
     * Do Nothing
     *
     * @param notificationTypes
     *        The notification types
     */
    public void setNotificationTypes(Set<Notification> notificationTypes)
    {
        // do nothing
    }

    /**
     * Clear the element in the vector contained in the hashmap object.
     */
    @Override
    public void finalize()
    {

        Collection<Vector<Notification>> collection = notificationMap.values();
        for (Vector<Notification> vector : collection) {
            vector.clear();
        }
        notificationMap.clear();
    }
}