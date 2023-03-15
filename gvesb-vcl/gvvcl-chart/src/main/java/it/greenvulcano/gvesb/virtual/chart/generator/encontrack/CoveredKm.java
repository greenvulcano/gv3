/**
 *
 */
package it.greenvulcano.gvesb.virtual.chart.generator.encontrack;

import java.awt.Color;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
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
     * Creates a new chart.
     *
     * @param the data set
     * @throws Exception
     */
    @Override
    public JFreeChart[] generateCharts(Node xmlData) throws Exception {
        IntervalXYDataset dataset = createDataset(xmlData);
        JFreeChart chart = createChart(dataset);
        return new JFreeChart[] {chart};
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
        //construct the plot
        XYPlot plot = new XYPlot();
        plot.setDataset(0, dataset);

        ValueAxis timeAxis = new DateAxis(null);
        timeAxis.setLowerMargin(0.02);  // reduce the default margins
        timeAxis.setUpperMargin(0.02);

        //customize the plot with renderers and axis
        XYBarRenderer barrenderer = new XYBarRenderer(0.10);
        barrenderer.setSeriesPaint(0, Color.LIGHT_GRAY);
        barrenderer.setBarAlignmentFactor(0.5);
        barrenderer.setDrawBarOutline(false);
        barrenderer.setShadowVisible(false);
        //barrenderer.setMaximumBarWidth(0.5);
        barrenderer.setGradientPaintTransformer(null);
        barrenderer.setBarPainter(new StandardXYBarPainter());
        plot.setRenderer(0, barrenderer);
        plot.setRangeAxis(0, new NumberAxis("Km recorridos"));

        plot.setDomainAxis(timeAxis);

        //Map the data to the appropriate axis
        plot.mapDatasetToRangeAxis(0, 0);

//        JFreeChart chart = ChartFactory.createXYBarChart(
//            null /* title */, null /* x-axis label*/, true,
//                "Km recorridos" /* y-axis label */, dataset);
/*        Color bg = new Color(192, 192, 192, 0);
        chart.setBackgroundPaint(bg);
        chart.getLegend().visible = false;

        XYBarRenderer barrenderer = (XYBarRenderer) ((XYPlot) chart.getPlot()).getRenderer(0);
        barrenderer.setSeriesPaint(0, Color.LIGHT_GRAY);
        barrenderer.setBarAlignmentFactor(0.5);
        barrenderer.setDrawBarOutline(false);
        barrenderer.setShadowVisible(false);
        //barrenderer.setMaximumBarWidth(0.5);
        barrenderer.setGradientPaintTransformer(null);
        barrenderer.setBarPainter(new StandardXYBarPainter());
        barrenderer.setMargin(0.2);*/

        // generate the chart
        JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        Color bg = new Color(192, 192, 192, 0);
        chart.setBackgroundPaint(bg);
        //chart.getLegend().visible = false;

        return chart;
    }
}
