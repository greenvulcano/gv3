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
package it.greenvulcano.gvesb.gvconsole.gvcon.browse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class Stylesheet
{

    /**
     *
     */
    public Stylesheet()
    {
        identifiers = null;
        identifiers = new HashMap<Object, Long>();
        sequence = 0L;
    }

    /**
     * @return
     */
    public synchronized long id()
    {
        return sequence++;
    }

    /**
     * @param node
     * @return
     */
    public synchronized Long id(Node node)
    {
        return getId(node);
    }

    /**
     * @param s
     * @return
     */
    public synchronized Long id(String s)
    {
        return getId(s);
    }

    /**
     * @param nodelist
     * @return
     */
    public synchronized Node[] unique(NodeList nodelist)
    {
        Set<Node> hashset = new HashSet<Node>();
        List<Node> linkedlist = new LinkedList<Node>();
        for (int i = 0; i < nodelist.getLength(); i++) {
            Node node = nodelist.item(i);
            if (!hashset.contains(node)) {
                hashset.add(node);
                linkedlist.add(node);
            }
        }

        Node anode[] = new Node[linkedlist.size()];
        linkedlist.toArray(anode);
        return anode;
    }

    private Long getId(Object obj)
    {
        Long long1 = identifiers.get(obj);
        if (long1 == null) {
            long1 = new Long(sequence++);
            identifiers.put(obj, long1);
        }
        return long1;
    }

    private Map<Object, Long> identifiers;
    private long              sequence;
}
