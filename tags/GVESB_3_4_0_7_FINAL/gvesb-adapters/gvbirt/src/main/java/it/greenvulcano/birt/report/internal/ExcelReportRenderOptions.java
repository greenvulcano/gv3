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
package it.greenvulcano.birt.report.internal;

import it.greenvulcano.birt.report.exception.BIRTException;
import it.greenvulcano.configuration.XMLConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @version 3.1.0 19/dic/2010
 * @author GreenVulcano Developer Team
 */
public class ExcelReportRenderOptions implements ReportRenderOptions
{
    private Map<String, String> optionsMap;

    /**
     * @see it.greenvulcano.birt.report.internal.ReportRenderOptions#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws BIRTException
    {
        try {
            NodeList options = XMLConfig.getNodeList(node, "Options/Option");
            if ((options != null) && (options.getLength() > 0)) {
                optionsMap = new HashMap<String, String>();
                for (int i = 0; i < options.getLength(); i++) {
                    Node optionNode = options.item(i);
                    optionsMap.put(XMLConfig.get(optionNode, "@name"), XMLConfig.get(optionNode, "@value", ""));
                }
            }
        }
        catch (Exception exc) {
            throw new BIRTException("Error initializing Excel Report Options", exc);
        }
    }

    /**
     * @see it.greenvulcano.birt.report.internal.ReportRenderOptions#getType()
     */
    @Override
    public String getType()
    {
        return "excel";
    }


    /**
     * @see it.greenvulcano.birt.report.internal.ReportRenderOptions#getOptions()
     */
    @Override
    public RenderOption getOptions()
    {
        EXCELRenderOption options = new EXCELRenderOption();
        options.setOutputFormat("xls");
        if (optionsMap != null && optionsMap.size() > 0) {
            Set<Entry<String, String>> keys = optionsMap.entrySet();
            for (Entry<String, String> entry : keys) {
                options.setOption(entry.getKey(), entry.getValue());
            }
        }
        return options;
    }
}
