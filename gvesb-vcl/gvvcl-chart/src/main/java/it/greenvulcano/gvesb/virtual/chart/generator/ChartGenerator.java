package it.greenvulcano.gvesb.virtual.chart.generator;

import org.jfree.chart.JFreeChart;
import org.w3c.dom.Node;

public interface ChartGenerator {

    /**
     * Creates a new demo instance.
     *
     * @param title  the frame title.
     * @throws Exception
     */
    JFreeChart generateChart(Node xmlData) throws Exception;

}