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

import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.w3c.dom.Node;

/**
 *
 * @version 3.1.0 19/dic/2010
 * @author GreenVulcano Developer Team
 */
public class HTMLReportRenderOptions implements ReportRenderOptions
{
    public void init(Node node) throws BIRTException
    {
        try {
            // do nothing
        }
        catch (Exception exc) {
            throw new BIRTException("Error initializing PDF Report Options", exc);
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.birt.report.internal.ReportRenderOptions#getType()
     */
    @Override
    public String getType()
    {
        return "html";
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.birt.report.internal.ReportRenderOptions#getOptions()
     */
    @Override
    public RenderOption getOptions()
    {
        HTMLRenderOption options = new HTMLRenderOption();
        //options.setOutputFileName("/home/gianluca/tmp/java/eclipse/birt-runtime-2_6_1/ReportEngine/output/test.html");
        options.setOutputFormat("html");
        //options.setHtmlRtLFlag(false);
        //options.setEmbeddable(false);
        //options.setImageDirectory("C:\\test\\images");

        return options;
    }
}
