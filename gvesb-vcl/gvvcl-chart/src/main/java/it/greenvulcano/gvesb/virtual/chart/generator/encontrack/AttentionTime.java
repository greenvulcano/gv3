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
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.data.time.TimeTableXYDataset;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.gvesb.virtual.chart.generator.ChartGenerator;
import it.greenvulcano.util.xml.XMLUtils;

/**
 * @author gianluca
 *
 */
public class AttentionTime extends BaseGenerator implements ChartGenerator {

    /**
     * Creates a new chart.
     *
     * @param the data set
     * @throws Exception
     */
    @Override
    public JFreeChart[] generateCharts(Node xmlData) throws Exception {
        TimeTableXYDataset dataset = createDataset(xmlData);
        JFreeChart chart = createChart(dataset);
        return new JFreeChart[] {chart};
    }

    /**
     * Returns a sample dataset.
     *
     * @return The dataset.
     * @throws Exception
     */
    private TimeTableXYDataset createDataset(Node xmlData) throws Exception {
        TimeTableXYDataset dataset = new TimeTableXYDataset();
        String aggrType = XMLUtils.get_S(xmlData, "/DEFAULT_ROOT/data/report_info/aggregation_type");
        NodeList aggrList = XMLUtils.selectNodeList_S(xmlData, "/DEFAULT_ROOT/data/aggregated");

        for (int i = 0; i < aggrList.getLength(); i++) {
            Node n = aggrList.item(i);

            Float v = Float.parseFloat(XMLUtils.get_S(n, "average_attention_time")) / 60;
            String d = XMLUtils.get_S(n, "date");
            addTSentry(dataset, aggrType, d, v, "Tiempo promedio de atenciòn");

            v = Float.parseFloat(XMLUtils.get_S(n, "average_closing_time")) / 60;
            d = XMLUtils.get_S(n, "date");
            addTSentry(dataset, aggrType, d, v, "Tiempo promedio de cierre");
        }

        return dataset;
    }

    /**
     * Creates a chart.
     *
     * @param dataset  the dataset.
     *
     * @return The chart.
     */
    private JFreeChart createChart(TimeTableXYDataset dataset) {
        //construct the plot
        XYPlot plot = new XYPlot();
        plot.setDataset(0, dataset);

        ValueAxis timeAxis = new DateAxis(null);
        timeAxis.setLowerMargin(0.02);  // reduce the default margins
        timeAxis.setUpperMargin(0.02);

        //customize the plot with renderers and axis
        StackedXYBarRenderer barrenderer = new StackedXYBarRenderer(0.20);
        barrenderer.setSeriesPaint(0, Color.RED);
        barrenderer.setSeriesPaint(1, Color.BLUE);
        barrenderer.setBarAlignmentFactor(0.5);
        barrenderer.setDrawBarOutline(false);
        barrenderer.setShadowVisible(false);
        //barrenderer.setMaximumBarWidth(0.5);
        barrenderer.setGradientPaintTransformer(null);
        barrenderer.setBarPainter(new StandardXYBarPainter());
        plot.setRenderer(0, barrenderer);
        plot.setRangeAxis(0, new NumberAxis("Alertas atendidas"));

        plot.setDomainAxis(timeAxis);

        //Map the data to the appropriate axis
        plot.mapDatasetToRangeAxis(0, 0);

        //generate the chart
        JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        Color bg = new Color(192, 192, 192, 0);
        chart.setBackgroundPaint(bg);
        //chart.getLegend().visible = false;

        return chart;
    }
}
