/**
 *
 */
package it.greenvulcano.gvesb.virtual.chart.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.log.GVLogger;

/**
 *
 * @version 3.4.0 Feb 5, 2023
 * @author GreenVulcano Developer Team
 *
 */
public class ChartGeneratorFactory {
    private static final Logger logger     = GVLogger.getLogger(ChartGeneratorFactory.class);

    private Map<String, ChartGenerator> generators = new HashMap<String, ChartGenerator>();

    public void init(Node node) throws InitializationException
    {
        try {
            Collection<Node> genList = XMLConfig.getNodeListCollection(node, "generator");
            for (Node n : genList) {
                String type = XMLConfig.get(n, "@type");
                String cls = XMLConfig.get(n, "@class");
                logger.debug("Adding ChartGenerator[" + type + "]: " + cls);
                ChartGenerator cg = (ChartGenerator) Class.forName(cls).newInstance();
                cg.init(n);
                this.generators.put(type, cg);
            }
            logger.debug("ChartGeneratorFactory configured");
        }
        catch (Exception exc) {
            logger.error("A generic error occurred while initializing", exc);
            throw new InitializationException("GV_CONFIGURATION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
    }

    public ChartGenerator getChartGenerator(String type) throws Exception {
        ChartGenerator cg = this.generators.get(type);
        if (cg == null) {
            throw new Exception("Invalid ChartGenerator type: " + type);
        }
        return cg;
    }
}
