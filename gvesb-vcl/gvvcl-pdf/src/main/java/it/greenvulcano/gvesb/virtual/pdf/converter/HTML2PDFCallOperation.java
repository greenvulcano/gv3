/*
 * Copyright (c) 2009-2023 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.virtual.pdf.converter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.xhtmlrenderer.pdf.ITextRenderer;

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
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;

/**
 * Convert an XHtml document (from file or GVBuffer.object) and put it in a file or GVBuffer.object.
 *
 * @version 3.4.0 Mar 15, 2023
 * @author GreenVulcano Developer Team
 *
 *
 */
public class HTML2PDFCallOperation implements CallOperation
{
    private static final Logger logger   = GVLogger.getLogger(HTML2PDFCallOperation.class);

    /**
     * The instance name.
     */
    private String              name     = null;

    /**
     * The pathname for the source XHtml file. Can contain placeholders that will
     * be replaced at call time. Must evaluate to an absolute pathname.
     */
    private String              srcPath  = null;

    /**
     * The pathname for the destination Pdf file. Can contain placeholders that will
     * be replaced at call time. Must evaluate to an absolute pathname.
     */
    private String              trgPath = null;

    /**
     * The configured operation's key.
     */
    protected OperationKey      key      = null;

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            this.name = XMLConfig.get(node, "@name");
            this.srcPath = XMLConfig.get(node, "@source", "");
            this.trgPath = XMLConfig.get(node, "@target", "");

            logger.debug("source : " + this.srcPath);
            logger.debug("target : " + this.trgPath);

            logger.debug("HTML2PDF " + this.name + " configured");
        }
        catch (XMLConfigException exc) {
            logger.error("An error occurred while configuring", exc);
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
            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);

            ITextRenderer renderer = new ITextRenderer();
            String srcFile = PropertiesHandler.expand(this.srcPath, params, gvBuffer);
            String trgFile = PropertiesHandler.expand(this.trgPath, params, gvBuffer);

            if ("".equals(srcFile)) {
                logger.debug("Reading input XHTML from GVBuffer ...");

                byte[] htmlData = new byte[] {};
                if (gvBuffer.getObject() != null) {
                    if (gvBuffer.getObject() instanceof byte[]) {
                        htmlData = (byte[]) gvBuffer.getObject();
                    } else if (gvBuffer.getObject() instanceof String) {
                        String charset = Optional.ofNullable(gvBuffer.getProperty("OBJECT_ENCODING")).orElse("UTF-8");
                        htmlData = gvBuffer.getObject().toString().getBytes(charset);
                    } else {
                        logger.error("Invalid input data: " + gvBuffer.getObject());
                        throw new IllegalArgumentException("Invalid input data");
                    }
                }
                renderer.setDocument(htmlData);
            }
            else {
                logger.debug("Reading input XHTML from file: " + srcFile);

                if (srcFile.startsWith("http")) {
                    renderer.setDocument(srcFile);
                } else {
                    renderer.setDocument(new File(srcFile));
                }
            }

            renderer.layout();

            if ("".equals(trgFile)) {
                logger.debug("Writing output PDF into GVBuffer...");

                ByteArrayOutputStream output = new ByteArrayOutputStream();
                renderer.createPDF(output);
                gvBuffer.setObject(output.toByteArray());
                output.close();
            } else {
                logger.debug("Writing output PDF into file: " + trgFile);

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(trgFile);
                    renderer.createPDF(fos);
                }
                finally {
                    if (fos != null) {
                        fos.close();
                    }
                }
            }

            return gvBuffer;
        }
        catch (Exception exc) {
            logger.error("An error occurred while converting to pdf", exc);
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
        // do nothig
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
}
