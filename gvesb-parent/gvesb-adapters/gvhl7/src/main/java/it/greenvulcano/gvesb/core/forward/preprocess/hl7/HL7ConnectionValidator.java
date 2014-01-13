/*
 * Copyright (c) 2009-2014 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.core.forward.preprocess.hl7;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.core.forward.JMSForwardException;
import it.greenvulcano.gvesb.core.forward.preprocess.Validator;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @version 3.4.0 07/gen/2014
 * @author GreenVulcano Developer Team
 *
 */
public class HL7ConnectionValidator implements Validator
{
    private String name;
    private List<String> urls = new ArrayList<String>();

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.core.forward.preprocess.Validator#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws JMSForwardException {
        try {
            name = XMLConfig.get(node, "@name");

            NodeList nl = XMLConfig.getNodeList(node, "HL7Server");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                urls.add(XMLConfig.get(n, "@host") + ":" + XMLConfig.get(n, "@port"));
            }
        }
        catch (Exception exc) {
            throw new JMSForwardException("Error initializing HL7ConnectionValidator", exc);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.core.forward.preprocess.Validator#isValid()
     */
    @Override
    public boolean isValid() throws JMSForwardException {
        return HL7ConnectionValidatorHolder.instance().isValid(name, urls);
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.core.forward.preprocess.Validator#reset()
     */
    @Override
    public void reset() {
        HL7ConnectionValidatorHolder.instance().reset(name, urls);
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.core.forward.preprocess.Validator#destroy()
     */
    @Override
    public void destroy() {
        HL7ConnectionValidatorHolder.instance().reset(name, urls);
        urls.clear();
    }
}
