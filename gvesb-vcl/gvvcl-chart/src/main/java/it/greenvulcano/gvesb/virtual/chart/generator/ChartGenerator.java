package it.greenvulcano.gvesb.virtual.chart.generator;

import org.jfree.chart.JFreeChart;
import org.w3c.dom.Node;

public interface ChartGenerator {

    /**
     * Creates a list of charts.
     *
     * @param the data set
     * @throws Exception
     */
    JFreeChart[] generateCharts(Node xmlData) throws Exception;

}