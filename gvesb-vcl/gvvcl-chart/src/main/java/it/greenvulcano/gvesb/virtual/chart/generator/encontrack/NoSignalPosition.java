/**
 *
 */
package it.greenvulcano.gvesb.virtual.chart.generator.encontrack;

import java.awt.Color;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
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
public class NoSignalPosition extends BaseGenerator implements ChartGenerator{

    /**
     * Creates a new chart.
     *
     * @param the data set
     * @throws Exception
     */
    @Override
    public JFreeChart[] generateCharts(Node xmlData) throws Exception {
        IntervalXYDataset[] dataset = createDataset(xmlData);
        JFreeChart chart = createChart(dataset);
        return new JFreeChart[] {chart};
    }

    /**
     * Returns a sample dataset.
     *
     * @return The dataset.
     * @throws Exception
     */
    private IntervalXYDataset[] createDataset(Node xmlData) throws Exception {
        TimeSeries tsKm = new TimeSeries("Numero de vehiculos");
        TimeSeries tsIdl = new TimeSeries("Tiempo sin posicion");
        String aggrType = XMLUtils.get_S(xmlData, "/DEFAULT_ROOT/data/report_info/aggregation_type");
        NodeList aggrList = XMLUtils.selectNodeList_S(xmlData, "/DEFAULT_ROOT/data/aggregated");

        for (int i = 0; i < aggrList.getLength(); i++) {
            Node n = aggrList.item(i);

            Float v = Float.parseFloat(XMLUtils.get_S(n, "num_vehicle"));
            String d = XMLUtils.get_S(n, "event_date");
            addTSentry(tsKm, aggrType, d, v);

            v = Float.parseFloat(XMLUtils.get_S(n, "total_no_pos_min")) / 60;
            d = XMLUtils.get_S(n, "event_date");
            addTSentry(tsIdl, aggrType, d, v);
        }

        TimeSeriesCollection[] dataset = new TimeSeriesCollection[2];
        dataset[0] = new TimeSeriesCollection();
        dataset[0].addSeries(tsKm);
        dataset[1] = new TimeSeriesCollection();
        dataset[1].addSeries(tsIdl);
        return dataset;
    }

    /**
     * Creates a sample chart.
     *
     * @param dataset  the dataset.
     *
     * @return The chart.
     */
    private JFreeChart createChart(IntervalXYDataset[] dataset) {
        //construct the plot
        XYPlot plot = new XYPlot();
        plot.setDataset(1, dataset[0]);
        plot.setDataset(0, dataset[1]);

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
        plot.setRenderer(1, barrenderer);
        plot.setRangeAxis(0, new NumberAxis("N° vehiculos"));
        ((NumberAxis) plot.getRangeAxis(0)).setTickUnit(new NumberTickUnit(1.0));

        XYSplineRenderer splinerenderer = new XYSplineRenderer();
        splinerenderer.setSeriesPaint(0, Color.BLUE);
        plot.setRenderer(0, splinerenderer);
        plot.setRangeAxis(1, new NumberAxis("N° de horas"));

        plot.setDomainAxis(timeAxis);

        //Map the data to the appropriate axis
        plot.mapDatasetToRangeAxis(0, 1);
        plot.mapDatasetToRangeAxis(1, 0);

        //generate the chart
        JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        Color bg = new Color(192, 192, 192, 0);
        chart.setBackgroundPaint(bg);
        //chart.getLegend().visible = false;

        return chart;
    }
}
