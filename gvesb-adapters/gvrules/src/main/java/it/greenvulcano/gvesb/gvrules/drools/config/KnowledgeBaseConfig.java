/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.gvrules.drools.config;

import it.greenvulcano.gvesb.gvrules.drools.RulesException;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.w3c.dom.Node;

/**
 * @version 3.2.0 12/feb/2012
 * @author GreenVulcano Developer Team
 */
public interface KnowledgeBaseConfig
{
    public void init(Node node) throws RulesException;

    public String getName();

    public StatelessKnowledgeSession getStatelessKnowledgeSession() throws RulesException;

    public StatefulKnowledgeSession getStatefulKnowledgeSession() throws RulesException;

    public void destroy();
}
