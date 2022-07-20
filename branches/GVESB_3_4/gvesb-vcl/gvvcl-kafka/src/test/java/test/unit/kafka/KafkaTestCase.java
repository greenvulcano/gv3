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
package test.unit.kafka;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.w3c.dom.Node;

import com.salesforce.kafka.test.KafkaTestCluster;
import com.salesforce.kafka.test.KafkaTestUtils;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.kafka.KafkaEnqueueOperation;
import junit.framework.TestCase;

/**
 * @version 3.4.0 Oct 12, 2021
 * @author GreenVulcano Developer Team
 *
 */
public class KafkaTestCase extends TestCase
{
	// Create a test cluster
    private KafkaTestCluster kafkaTestCluster = null;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        this.kafkaTestCluster = new KafkaTestCluster(1);
        this.kafkaTestCluster.start();

        System.setProperty("kafka.url", this.kafkaTestCluster.getKafkaConnectString());
        System.setProperty("zookeeper.url", this.kafkaTestCluster.getZookeeperConnectString());
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
    	if (this.kafkaTestCluster != null) {
    		this.kafkaTestCluster.close();
    	}
        System.clearProperty("kafka.url");
        System.clearProperty("zookeeper.url");
    }

    /**
     * @throws Exception
     *
     */
    public void testPublishStringToBytes() throws Exception
    {
        final KafkaTestUtils kafkaTestUtils = new KafkaTestUtils(this.kafkaTestCluster);
        Set<String> ts = new HashSet<String>();
        ts.add("test.topic");
        kafkaTestUtils.getAdminClient().deleteTopics(ts);
        kafkaTestUtils.createTopic("test.topic", 1, (short) 1);

        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/kafka-enqueue[@name='publishString']");
        KafkaEnqueueOperation kenq = new KafkaEnqueueOperation();
        kenq.init(node);
        GVBuffer gvBuffer = new GVBuffer("GVESB", "TOUPPER");
        gvBuffer.setObject("hello world");
        GVBuffer result = kenq.perform(gvBuffer);
        assertNotNull("Missing KAFKA_TOPIC", result.getProperty("KAFKA_TOPIC"));
        assertNotNull("Missing KAFKA_PARTITION", result.getProperty("KAFKA_PARTITION"));
        assertNotNull("Missing KAFKA_OFFSET", result.getProperty("KAFKA_OFFSET"));
        assertNotNull("Missing KAFKA_TIMESTAMP", result.getProperty("KAFKA_TIMESTAMP"));

        final List<ConsumerRecord<byte[], byte[]>> consumerRecords = kafkaTestUtils.consumeAllRecordsFromTopic("test.topic", ByteArrayDeserializer.class, ByteArrayDeserializer.class);

        assertNotNull("Should have non-null result.", consumerRecords);
        assertEquals("Should have 1 record.", 1, consumerRecords.size());

        String val = "";
        // Log the records we found.
        for (final ConsumerRecord<byte[], byte[]> consumerRecord : consumerRecords) {
        	val = new String(consumerRecord.value());
            System.out.println("Found Key: [" + new String(consumerRecord.key()) + "] on Partition: " + consumerRecord.partition() + " with Value: [" + val + "]");
        }
        assertEquals("Wrong value read from topic", "hello world", val);
    }
}
