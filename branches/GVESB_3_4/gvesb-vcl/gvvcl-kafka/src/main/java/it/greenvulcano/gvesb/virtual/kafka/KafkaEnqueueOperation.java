/*
 * Copyright (c) 2009-2021 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.virtual.kafka;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.EnqueueOperation;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.xml.XMLUtils;

/**
 * This class realizes an enqueue mechanism for a Kafka topic.
 *
 * @version 3.4.0 Oct 12, 2021
 * @author GreenVulcano Developer Team
 *
 */
public class KafkaEnqueueOperation implements EnqueueOperation
{

    private static final Logger logger      = GVLogger.getLogger(KafkaEnqueueOperation.class);

    /**
     * The associated key
     */
    protected OperationKey      key            = null;

    /**
     * The operation name
     */
    protected String            name           = null;

    /**
     * true if the connection was established with the Kafka cluster.
     */
    protected boolean           isConnected      = false;

    /**
     * Keeps reference to <code>IDataProvider</code> implementation
     * to be used for key generation.
     */
    protected String            keyDP;
    /**
     * Keeps reference to <code>IDataProvider</code> implementation
     * to be used for value generation.
     */
    protected String            valueDP;

    private ProducerConfig                prdCfg      = null;
    private KafkaProducer<byte[], byte[]> producer;
    private String                        topic;
    private String                        keyPattern;
    private AtomicBoolean                 lastStatus  = new AtomicBoolean(false);


    /**
    *
    * @param node
    *        configuration node
    *
    * @throws InitializationException
    *         if initialization errors occurs
    */
   @Override
public final void init(Node node) throws InitializationException
   {
	   String pName = null;
       try {
           this.name = XMLConfig.get(node, "@name");

           this.keyDP = XMLConfig.get(node, "@key-dp");
           this.valueDP = XMLConfig.get(node, "@value-dp", "");

           this.topic = XMLConfig.get(node, "@topic");
           this.keyPattern = XMLConfig.get(node, "@key-pattern");

           Map<String, Object> producerConfig = new HashMap<String, Object>();
           NodeList props = XMLConfig.getNodeList(node, "ProducerConfig/PropertyDef");

           for (int i = 0; i < props.getLength(); i++) {
        	   Node p = props.item(i);
        	   pName = XMLConfig.get(p, "@name");
        	   String pValue = PropertiesHandler.expand(XMLConfig.get(p, "@value"));
        	   producerConfig.put(pName, pValue);
           }

           if (producerConfig.containsKey("key.serializer") || producerConfig.containsKey("value.serializer")) {
        	   throw new InitializationException("key.serializer and/or value.serializer properties SHOULD NOT be set");
           }

           // set by default
           producerConfig.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
           producerConfig.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
           this.prdCfg = new ProducerConfig(producerConfig);
       }
       catch (XMLConfigException exc) {
           logger.error("An error occurred reading configuration data.", exc);
           throw new InitializationException("GVVCL_XML_CONFIG_ERROR", new String[][]{{"exc", exc.toString()},
                   {"key", "N/A"}}, exc);
       }
       catch (PropertiesHandlerException exc) {
           logger.error("An error occurred decoding configuration param[" + pName + "] value", exc);
           throw new InitializationException("GVVCL_XML_CONFIG_ERROR", new String[][]{{"exc", exc.toString()},
                   {"key", "N/A"}}, exc);
       }

       try {
           if (!this.isConnected) {
               initKafka();
           }
       }
       catch (KafkaConnectionException exc) {
           logger.warn("Cannot establish the connection. The system will retry later to establish the connection.",
                   exc);
       }
   }

    /**
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
	public GVBuffer perform(GVBuffer data) throws KafkaConnectionException, KafkaEnqueueException, InvalidDataException
    {
    	try {
            try {
                if (!this.isConnected) {
                    initKafka();
                }
                data = sendMessage(data);
            } catch (Exception exc) {
                closeKafka();

                try {
                    initKafka();
                    data = sendMessage(data);
                } catch (Exception exc2) {
                    closeKafka();
                    throw exc2;
                }
            }
            return data;
        }
        catch (KafkaEnqueueException exc) {
            throw exc;
        }
        catch (KafkaConnectionException exc) {
            throw exc;
        }
        catch (InvalidDataException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new KafkaEnqueueException("GVVCL_KAFKA_INTERNAL_ERROR", exc);
        }
    }

    private ProducerRecord<byte[], byte[]> buildMessage(GVBuffer data) throws InvalidDataException {
        try {
        	Map<String, Object>  props = GVBufferPropertiesHelper.getPropertiesMapSO(data, true);
        	byte[] key = null;
        	byte[] value = null;

        	DataProviderManager dataProviderManager = DataProviderManager.instance();
            IDataProvider keyProvider = dataProviderManager.getDataProvider(this.keyDP);
            try {
                logger.debug("Working on Key data provider: " + keyProvider);
                keyProvider.setObject(data);
                key = (byte[]) keyProvider.getResult();
            }
            finally {
                dataProviderManager.releaseDataProvider(this.keyDP, keyProvider);
            }

        	if ((this.valueDP != null) && (this.valueDP.length() > 0)) {
                IDataProvider valueDataProvider = dataProviderManager.getDataProvider(this.valueDP);
                try {
                    logger.debug("Working on Value data provider: " + valueDataProvider);
                    valueDataProvider.setObject(data);
                    value = (byte[]) valueDataProvider.getResult();
                }
                finally {
                    dataProviderManager.releaseDataProvider(this.valueDP, valueDataProvider);
                }
            }
            else {
            	Object d = data.getObject();
            	if (d == null) {
            		throw new InvalidDataException("The GVBuffer content is NULL");
            	}
            	if (d instanceof byte[]) {
            		value = (byte[]) d;
                }
                else if (d instanceof String) {
                	value = ((String) d).getBytes();
                }
                else if (data instanceof Node) {
                	value = XMLUtils.serializeDOMToByteArray_S((Node) d);
                }
                else {
                    throw new InvalidDataException("Invalid GVBuffer content: " + data.getClass().getName());
                }
            }

            ProducerRecord<byte[], byte[]> record = new ProducerRecord<byte[], byte[]>(PropertiesHandler.expand(this.topic, props),
            		key, value);
            return record;
        } catch (Exception exc) {
            logger.error("Error preparing data:\n" + data, exc);
            throw new InvalidDataException("Error preparing data", exc);
        }
    }

    public GVBuffer sendMessage(final GVBuffer data) throws KafkaConnectionException, KafkaEnqueueException, InvalidDataException
    {
        try {
        	ProducerRecord<byte[], byte[]> record = buildMessage(data);
            RecordMetadata meta = this.producer.send(record).get();
    		try {
                data.setProperty("KAFKA_TOPIC", meta.topic());
                data.setProperty("KAFKA_PARTITION", String.valueOf(meta.partition()));
                data.setProperty("KAFKA_OFFSET", String.valueOf(meta.offset()));
                data.setProperty("KAFKA_TIMESTAMP", String.valueOf(meta.timestamp()));
            }
            catch(Exception exc2) {
            	logger.error("Error setting output properties", exc2);
            }
            logger.info("Published data on Topic: " + meta.topic() + " - Partition: "
                    + meta.partition() + " - Offset: " + meta.offset() + " - Timestamp: " + meta.timestamp());

            return data;
        }
        catch (InvalidDataException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new KafkaEnqueueException("GVVCL_KAFKA_INTERNAL_ERROR", exc);
        }
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
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
	public void cleanUp()
    {
        // do nothing
    }

    /**
     * Called when an operation is discarded from cache.
     */
    @Override
	public final void destroy()
    {
    	closeKafka();
    }

    /**
     * Finalization.
     */
    @Override
    protected void finalize()
    {
        destroy();
    }

    /**
     * Return the alias for the given service
     *
     * @param data
     *        the input service data
     * @return the configured alias
     */
    @Override
	public String getServiceAlias(GVBuffer data)
    {
        return data.getService();
    }

    private void initKafka() throws KafkaConnectionException {
        this.producer = new KafkaProducer<byte[], byte[]>(this.prdCfg.originals());
        this.isConnected = true;
    }

    private void closeKafka() {
        this.isConnected = false;
        if (this.producer != null) {
            try {
                this.producer.close(Duration.ofMillis(1000));
            } catch (Exception e) {
                // do nothing
            }
        }
        this.producer = null;
    }
}
