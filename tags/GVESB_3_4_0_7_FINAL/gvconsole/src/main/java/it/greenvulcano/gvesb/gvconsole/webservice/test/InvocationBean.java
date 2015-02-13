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
package it.greenvulcano.gvesb.gvconsole.webservice.test;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.log.GVLogger;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public abstract class InvocationBean
{

    /**
     * The transaction mode
     */
    protected String              transactionMode = "NoTransaction";

    /**
     * Logger definition.
     */
    protected static final Logger logger          = GVLogger.getLogger(InvocationBean.class);

    /**
     * JNDI Name of the object
     */
    protected String              jndiName        = "";

    /**
     * JNDI Helper object
     */
    protected JNDIHelper          jndiHelper;

    /**
     * The buffer object
     */
    protected GVBuffer            buffer;

    /**
     * Output file name
     */
    protected String              outputFileName  = "";

    /**
     * Configuration file name
     */
    protected String              configFile      = "InvocationBeanConfig.xml";

    /**
     * Append into file
     */
    protected boolean             append          = false;

    /**
     * Iterations field
     */
    protected int                 iterations      = 1;

    /**
     * Operation
     */
    protected String              operation;

    /**
     * chain test
     */
    protected boolean             chainTest       = false;
    /**
     * operations
     */
    String[]                      operations;

    /**
     * Constructor
     *
     * @throws Exception
     */
    public InvocationBean() throws Exception
    {
        this.buffer = new GVBuffer();
    }

    /**
     * Start the transaction <br>
     *
     * @throws Exception
     *         If an error occurred
     */
    public void startTransaction() throws Exception
    {
        if (transactionMode != null && (transactionMode.equals("Commit") || transactionMode.equals("Rollback"))) {
            //VTxM.instance().begin();
        }
    }

    /**
     * End the transaction in commit mode or in roll-back mode depending on
     * request <br>
     *
     * @throws Exception
     *         If an error occurred
     */
    public void endTransaction() throws Exception
    {
        if (transactionMode != null) {
            if (transactionMode.equals("Commit")) {
                //VTxM.instance().commit();;
            }
            else if (transactionMode.equals("Rollback")) {
                //VTxM.instance().rollback();
            }
        }
    }

    /**
     * @return
     */
    public boolean isChainTest()
    {
        return chainTest;
    }

    /**
     * @param chainTest
     */
    public void setChainTest(boolean chainTest)
    {
        this.chainTest = chainTest;
    }

    /**
     * @return
     */
    public String getOperation()
    {
        return operation;
    }

    /**
     * @param operation
     */
    public void setOperation(String operation)
    {
        this.operation = operation;
    }

    /**
     * @return
     */
    public GVBuffer getGVBuffer()
    {
        return buffer;
    }

    /**
     * @param gvBuffer
     */
    public void setGVBuffer(GVBuffer gvBuffer)
    {
        this.buffer = gvBuffer;
    }

    /**
     * @return
     */
    public boolean isAppend()
    {
        return append;
    }


    /**
     * @param append
     */
    public void setAppend(boolean append)
    {
        this.append = append;
    }


    /**
     * @return
     */
    public String getOutputFileName()
    {
        return outputFileName;
    }


    /**
     * @param outputFileName
     */
    public void setOutputFileName(String outputFileName)
    {
        this.outputFileName = outputFileName;
    }

    /**
     * @return
     */
    public int getIterations()
    {
        return iterations;
    }


    /**
     * @param iterations
     */
    public void setIterations(int iterations)
    {
        this.iterations = iterations;
    }

    /**
     * @return
     */
    public String[] getOperations()
    {
        return operations;
    }

    /**
     * @param operations
     */
    public void setOperations(String[] operations)
    {
        this.operations = operations;
    }

    /**
     * Get JNDIHelper
     *
     * @return
     */
    public JNDIHelper getJNDIHelper()
    {
        return jndiHelper;
    }

    /**
     * Set the JNDIHelper
     *
     * @param jndiHelper
     * @throws Exception
     */
    public void setJndiContext(JNDIHelper jndiHelper) throws Exception
    {
        this.jndiHelper = jndiHelper;
    }

    /**
     * @return
     */
    public String getTransactionMode()
    {
        return transactionMode;
    }

    /**
     * @param transactionMode
     */
    public void setTransactionMode(String transactionMode)
    {
        this.transactionMode = transactionMode;
    }

    /**
     * @return
     */
    public String getJndiName()
    {
        return jndiName;
    }

    /**
     * @param jndiName
     */
    public void setJndiName(String jndiName)
    {
        this.jndiName = jndiName;
    }

    /**
     * Init with configuration node
     *
     * @param configuration
     * @throws Exception
     */
    public abstract void init(Node configuration) throws Exception;

    /**
     * Init with JNDIHelper object
     *
     * @param jndiHelper
     * @throws Exception
     */
    public abstract void init(JNDIHelper jndiHelper) throws Exception;

    /**
     * start execution test
     *
     * @return
     * @throws Exception
     */
    public abstract Object execute() throws Exception;

    /**
     * Executing the tests
     *
     * @return
     * @throws Exception
     */
    public Object startTest() throws Exception
    {
        if (iterations == 0) {
            throw new Exception("The number iterations it's 0");
        }
        Object data = null;
        for (int i = 0; i < iterations; i++) {
            logger.debug("execution test: " + i);
            data = execute();
            logger.debug("Chain test: " + chainTest + " - GVBuffer result:");
            logger.debug(data);
            if (chainTest) {
                 buffer = (GVBuffer)data;
            }
            buffer.setId(new Id());
        }
        return data;
    }
}
