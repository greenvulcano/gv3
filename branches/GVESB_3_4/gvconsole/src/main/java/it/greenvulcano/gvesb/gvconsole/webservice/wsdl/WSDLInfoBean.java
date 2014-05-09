/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.greenvulcano.gvesb.gvconsole.webservice.wsdl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.struts.action.ActionForm;

import it.greenvulcano.gvesb.gvconsole.webservice.utils.ActionResult;
import it.greenvulcano.gvesb.virtual.ws.monitoring.WSDLManagerProxy;
import it.greenvulcano.jmx.JMXEntryPoint;
/*
 *
 * WSDLInfoBean class
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
*/
public class WSDLInfoBean extends ActionForm
{
    private static final long serialVersionUID = 6052560459167910830L;
    private String            files[]          = {};
    private String            selectedItems[]  = {};
    private String            file             = "";
    private String            nodeList[]       = {};
    private String            selectedNodes[]  = {};
    private String            reload           = "";
    private ActionResult      result           = null;

    public WSDLInfoBean() throws Exception
    {
        try {
            JMXEntryPoint jmx = JMXEntryPoint.instance();
            MBeanServer server = jmx.getServer();

            Set set = server.queryNames(new ObjectName(WSDLManagerProxy.JMX_FILTER), null);
            Iterator iterator = set.iterator();
            ObjectName objectName = (ObjectName) iterator.next();

            files = (String[]) server.getAttribute(objectName, "loadedWSDL");
            Arrays.sort(files);

            Set set1 = server.queryNames(new ObjectName("*:*,Name=DomainInfo_Internal"), null);
            Iterator iterator1 = set1.iterator();
            ObjectName objectName1 = (ObjectName) iterator1.next();
            nodeList = (String[]) server.getAttribute(objectName1, "serversNames");
            Arrays.sort(nodeList);

        }
        catch (Exception e) {

            e.printStackTrace();

        }
    }

    public String getFile()
    {
        return file;
    }

    public void setFile(String file)
    {
        this.file = file;
    }

    public String[] getFiles()
    {
        return files;
    }

    public void setFiles(String[] files)
    {
        this.files = files;
    }

    public String[] getSelectedItems()
    {
        return selectedItems;
    }

    public void setSelectedItems(String[] selectedItems)
    {
        this.selectedItems = selectedItems;
    }

    public String[] getNodeList()
    {
        return nodeList;
    }

    public void setNodeList(String[] nodeList)
    {
        this.nodeList = nodeList;
    }

    public String[] getSelectedNodes()
    {
        return selectedNodes;
    }

    public void setSelectedNodes(String[] selectedNodes)
    {
        this.selectedNodes = selectedNodes;
    }

    public String getReload()
    {
        return reload;
    }

    public void setReload(String reload)
    {
        this.reload = reload;
    }

    public ActionResult getResult()
    {
        return result;
    }
}
