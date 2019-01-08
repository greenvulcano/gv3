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
package it.greenvulcano.gvesb.statistics.ejb;

import it.greenvulcano.gvesb.statistics.utils.JMSStatisticsMessageListener;
import it.greenvulcano.log.GVLogger;

import javax.ejb.CreateException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class MDBStatisticsReaderBean implements MessageDrivenBean, MessageListener
{
    /**
     *
     */
    private static final long            serialVersionUID = 234657448610L;

    private static final Logger          logger           = GVLogger.getLogger(MDBStatisticsReaderBean.class);

    private JMSStatisticsMessageListener statisticsReader = null;

    private boolean                      initialized      = false;

    /**
     * This method is required by the EJB Specification, but is not used by this
     * example.
     */
    public void ejbRemove()
    {
        if (statisticsReader != null) {
            statisticsReader.destroy();
        }
    }

    /**
     * Sets the session context.
     *
     * @param ctx
     *        MessageDrivenContext Context for session
     */
    public void setMessageDrivenContext(MessageDrivenContext ctx)
    {
        // do nothing
    }

    /**
     * Create the Message Driven Bean
     *
     * @throws CreateException
     *
     */
    public void ejbCreate()
    {
        // do nothing
    }

    /**
     * This method gets the message
     *
     * @param message
     *        Message object
     */
    public void onMessage(Message message)
    {
        init();
        statisticsReader.onMessage(message);
    }

    private void init() throws RuntimeException
    {
        if (!initialized) {
            Context ctx = null;
            logger.debug("INITIALIZING MDBStatisticsReaderBean");
            try {
                ctx = new InitialContext();
                String targetWriter = (String) ctx.lookup("java:comp/env/StatisticsWriterID");
                logger.debug("Statistics writer ID         : " + targetWriter);
                statisticsReader = new JMSStatisticsMessageListener(targetWriter);
                initialized = true;
            }
            catch (Exception exc) {
                logger.error("An error occurred the initialization statistics engine.", exc);
                throw new RuntimeException(exc);
            }
            finally {
                try {
                    if (ctx != null) {
                        ctx.close();
                    }
                }
                catch (Exception e) {
                    // do nothing
                }
            }
        }
    }
}