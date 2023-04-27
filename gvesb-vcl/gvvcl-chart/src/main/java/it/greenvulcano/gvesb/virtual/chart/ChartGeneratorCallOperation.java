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
package it.greenvulcano.gvesb.virtual.chart;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.gvesb.virtual.chart.generator.ChartGenerator;
import it.greenvulcano.gvesb.virtual.chart.generator.ChartGeneratorFactory;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.XMLUtils;

/**
 * Generates a JFreeChart graph and returns it as byte[] image or write it into a file.
 *
 * @version 3.4.0 Feb 5, 2023
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ChartGeneratorCallOperation implements CallOperation
{

    private static final Logger logger     = GVLogger.getLogger(ChartGeneratorCallOperation.class);

    /**
     * The instance name.
     */
    private String              name       = null;

    private ChartGeneratorFactory cgFactory = null;

    private int width = 600;
    private int height = 300;

    /**
     * The pathname for the target directory. Can contain placeholders that will
     * be replaced at call time. Must evaluate to an absolute pathname.
     */
    private String              targetPath = null;

    /**
     * Image file name. Can contain placeholders that will be replaced at call
     * time.
     */
    private String              filename   = null;

    /**
     * the operation key
     */
    protected OperationKey      key        = null;


    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            this.name = XMLConfig.get(node, "@name");
            this.targetPath = XMLConfig.get(node, "@targetPath", "");
            this.filename = XMLConfig.get(node, "@fileName", "");
            this.width = XMLConfig.getInteger(node, "@width", this.width);
            this.height = XMLConfig.getInteger(node, "@height", this.height);

            if (!"".equals(this.targetPath)) {
                logger.debug("targetPath : " + this.targetPath);
                logger.debug("filename   : " + this.filename);
            }

            this.cgFactory = new ChartGeneratorFactory();
            this.cgFactory.init(node);

            logger.debug("ChartGeneratorCallOperation " + this.name + " configured");
        }
        catch (XMLConfigException exc) {
            logger.error("An error occurred while reading configuration", exc);
            throw new InitializationException("GV_CONFIGURATION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
        catch (Exception exc) {
            logger.error("A generic error occurred while initializing", exc);
            throw new InitializationException("GV_CONFIGURATION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        try {
            Object data = gvBuffer.getObject();
            if (data == null) {
                throw new InvalidDataException("The GVBuffer content is NULL");
            }

            String chartType = gvBuffer.getProperty("GVCG_TYPE");

            Node xmlData = XMLUtils.parseObject_S(data, false, true);

            ChartGenerator cg = this.cgFactory.getChartGenerator(chartType);
            JFreeChart[] jcs = cg.generateCharts(xmlData);

            for (int i = 0; i < jcs.length; i++) {
                File targetFile = buildTargetPathname(gvBuffer, i);
                if (targetFile != null) {
                    int localWidth = cg.getPreferredWidth()[i] > 0 ? cg.getPreferredWidth()[i] : this.width;
                    int localHeight = cg.getPreferredHeight()[i] > 0 ? cg.getPreferredHeight()[i] : this.height;

                    if (gvBuffer.getProperty("GVCG_WIDTH") != null) {
                        localWidth = Integer.parseInt(gvBuffer.getProperty("GVCG_WIDTH"));
                    }
                    if (gvBuffer.getProperty("GVCG_HEIGHT") != null) {
                        localHeight = Integer.parseInt(gvBuffer.getProperty("GVCG_HEIGHT"));
                    }
                    ChartUtils.saveChartAsPNG(targetFile, jcs[i], localWidth, localHeight);
                    logger.info("Generated chart[" + chartType + "][" + i + "] into " + targetFile);
                }
                else {
                    //gvBuffer.setObject(ChartUtils.encodeAsPNG(jcs[0].createBufferedImage(localWidth, localHeight)));
                }
            }

            return gvBuffer;
        }
        catch (Exception exc) {
            logger.error("An error occurred while generating chart", exc);
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
        }
    }


    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    @Override
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    @Override
    public OperationKey getKey()
    {
        return this.key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getServiceAlias(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

    private File buildTargetPathname(GVBuffer gvBuffer, int idx) throws Exception
    {
        try {
            PropertiesHandler.enableExceptionOnErrors();
            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            params.put("CHART_IDX", String.valueOf(idx));

            String targetDirectory = gvBuffer.getProperty("GVCG_DIRECTORY");
            if (targetDirectory == null) {
                if (this.targetPath != null) {
                    targetDirectory = this.targetPath;
                }
                else {
                    return null;
                }
            }

            targetDirectory = PropertiesHandler.expand(targetDirectory, params, gvBuffer);

            String targetFilename = gvBuffer.getProperty("GVCG_FILE_NAME");
            if (targetFilename == null) {
                if (this.filename != null) {
                    targetFilename = this.filename;
                }
                else {
                    return null;
                }
            }

            targetFilename = PropertiesHandler.expand(targetFilename, params, gvBuffer);

            // create target directory
            (new File(targetDirectory)).mkdirs();

            File targetPathname = new File(targetDirectory, targetFilename);
            if (targetPathname.isAbsolute()) {
                if (!targetPathname.exists() || (targetPathname.exists() && targetPathname.isFile())) {
                    return targetPathname;
                }
                throw new IllegalArgumentException("Pathname " + targetPathname.getPath() + " not a file");
            }
            throw new IllegalArgumentException("Pathname " + targetPathname.getPath() + " is not absolute");
        }
        finally {
            PropertiesHandler.disableExceptionOnErrors();
        }
    }
}