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
package it.greenvulcano.management.component;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.management.DomainAction;

import org.w3c.dom.Node;

/**
 *
 * StartComponentAction class
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
 *
 *
**/
public class StartComponentAction extends DomainAction {
    private static final long  serialVersionUID = 4059782204244441878L;
    /**
     * Internal parameter name.
     */
    public static final String COMPONENT_NAME   = "component_name";

    /**
     * Constuctor.
     */
    public StartComponentAction() {
        super();
    }

    /**
     * Constructor.
     *
     * @param name
     *            the component name
     */
    public StartComponentAction(String name) {
        super(name);
    }

    /**
     * @see it.greenvulcano.management.DomainAction#internalInit(org.w3c.dom.Node)
     */
    @Override
    protected final void internalInit(Node node) throws XMLConfigException {
        String component = XMLConfig.get(node, "@component");
        setComponent(component);
    }

    /**
     * Set the component name.
     *
     * @param component
     *            the component name
     */
    public final void setComponent(String component) {
        params.put(COMPONENT_NAME, component);
    }

    /**
     * @return the component name
     */
    public final String getComponent() {
        return (String) params.get(COMPONENT_NAME);
    }
}
