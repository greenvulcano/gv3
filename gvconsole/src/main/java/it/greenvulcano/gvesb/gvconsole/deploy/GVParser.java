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
package it.greenvulcano.gvesb.gvconsole.deploy;

import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.log.GVLogger;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;


/**
 *
 * GVParser class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class GVParser
{
    private GVCoreParser    gvCoreParser    = null;
    private GVAdapterParser gvAdapterParser = null;
    private GVSupportParser gvSupportParser = null;
    private static Logger   logger          = GVLogger.getLogger(GVParser.class);

    /**
     * @throws IOException
     * @throws XmlException
     * @throws XMLConfigException
     */
    public GVParser() throws Exception
    {
        logger.debug("Init GVParser");
        gvCoreParser = new GVCoreParser();
        gvAdapterParser = new GVAdapterParser();
        gvCoreParser.setAdapterParser(gvAdapterParser);
        gvAdapterParser.setCoreParser(gvCoreParser);
        gvSupportParser = new GVSupportParser();
        gvCoreParser.loadParser();
        gvAdapterParser.loadParser();
        gvSupportParser.loadParser();
        logger.debug("End GVParser");
    }

    /**
     * @return the GVCore parser
     */
    public GVCoreParser getGVCoreParser()
    {
        return gvCoreParser;
    }

    /**
     * @return the GVAdapter parser
     */
    public GVAdapterParser getGVAdapterParser()
    {
        return gvAdapterParser;
    }

    /**
     * @return the GVSupport parser
     */
    public GVSupportParser getGVSupportParser()
    {
        return gvSupportParser;
    }

}
