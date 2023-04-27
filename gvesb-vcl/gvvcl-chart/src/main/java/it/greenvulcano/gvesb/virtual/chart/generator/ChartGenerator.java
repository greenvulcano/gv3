package it.greenvulcano.gvesb.virtual.chart.generator;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.Node;

import it.greenvulcano.gvesb.virtual.InitializationException;

public interface ChartGenerator {

    void init(Node node) throws InitializationException;

    Logger getLogger();

    /**
     * Creates a list of charts.
     *
     * @param the data set
     * @throws Exception
     */
    JFreeChart[] generateCharts(Node xmlData) throws Exception;

    String getType();
    int[] getPreferredWidth();
    int[] getPreferredHeight();
}