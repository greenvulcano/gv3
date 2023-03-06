/**
 *
 */
package it.greenvulcano.gvesb.virtual.chart.generator.encontrack;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.IntervalXYDataset;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.gvesb.virtual.chart.generator.ChartGenerator;
import it.greenvulcano.util.xml.XMLUtils;

/**
 * @author gianluca
 *
 */
public class CoveredKm extends BaseGenerator implements ChartGenerator{
    /**
     * Creates a new demo instance.
     *
     * @param title  the frame title.
     * @throws Exception
     */
    @Override
    public JFreeChart generateChart(Node xmlData) throws Exception {
        IntervalXYDataset dataset = createDataset(xmlData);
        JFreeChart chart = createChart(dataset);
        return chart;
    }

    /**
     * Returns a sample dataset.
     *
     * @return The dataset.
     * @throws Exception
     */
    private IntervalXYDataset createDataset(Node xmlData) throws Exception {
        TimeSeries ts = new TimeSeries("CoveredKm");
        String aggrType = XMLUtils.get_S(xmlData, "/DEFAULT_ROOT/data/report_info/aggregation_type");
        NodeList aggrList = XMLUtils.selectNodeList_S(xmlData, "/DEFAULT_ROOT/data/aggregated");

        for (int i = 0; i < aggrList.getLength(); i++) {
            Node n = aggrList.item(i);
            Float v = Float.parseFloat(XMLUtils.get_S(n, "covered_km"));
            String d = XMLUtils.get_S(n, "event_date");

            addTSentry(ts, aggrType, d, v);
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(ts);
        return dataset;
    }

    /**
     * Creates a sample chart.
     *
     * @param dataset  the dataset.
     *
     * @return The chart.
     */
    private JFreeChart createChart(IntervalXYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYBarChart(
            null /* title */, null /* x-axis label*/, true,
                "Km recorridos" /* y-axis label */, dataset);
        Color bg = new Color(192, 192, 192, 0);
        chart.setBackgroundPaint(bg);
        chart.getLegend().visible = false;

        XYBarRenderer barrenderer = (XYBarRenderer) ((XYPlot) chart.getPlot()).getRenderer(0);
        barrenderer.setSeriesPaint(0, Color.GRAY);
        barrenderer.setBarAlignmentFactor(0.5);
        barrenderer.setMargin(0.2);

        return chart;
    }
}
