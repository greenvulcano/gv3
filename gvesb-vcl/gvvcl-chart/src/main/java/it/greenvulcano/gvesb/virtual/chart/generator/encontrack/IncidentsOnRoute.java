/**
 *
 */
package it.greenvulcano.gvesb.virtual.chart.generator.encontrack;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.GridArrangement;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.UnitType;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.IntervalXYDataset;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.chart.generator.ChartGenerator;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.xml.XMLUtils;

/**
 * @author gianluca
 *
 */
public class IncidentsOnRoute extends BaseGenerator implements ChartGenerator{
    private static final Logger logger     = GVLogger.getLogger(IncidentsOnRoute.class);

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void init(Node node) throws InitializationException {
        super.init(node);
        if (this.width.length < 2) {
            int[] tmp = new int[] { this.width[0], -1};
            this.width = tmp;
        }
        if (this.height.length < 2) {
            int[] tmp = new int[] { this.height[0], -1};
            this.height = tmp;
        }
    }

    /**
     * Creates a new chart.
     *
     * @param the data set
     * @throws Exception
     */
    @Override
    public JFreeChart[] generateCharts(Node xmlData) throws Exception {
        JFreeChart chart[] = new JFreeChart[2];
        chart[0] = createChartBarLine(xmlData);
        chart[1] = createChartRing(xmlData);

        return chart;
    }

    /**
     * Returns a sample dataset.
     *
     * @return The dataset.
     * @throws Exception
     */
    private IntervalXYDataset[] createDatasetBarLine(Node xmlData) throws Exception {
        TimeSeries tsN = new TimeSeries("N° vehículos");
        TimeSeries tsE = new TimeSeries("N° incidencias");
        String aggrType = XMLUtils.get_S(xmlData, "/DEFAULT_ROOT/data/report_info/aggregation_type");
        NodeList aggrList = XMLUtils.selectNodeList_S(xmlData, "/DEFAULT_ROOT/data/aggregated");

        for (int i = 0; i < aggrList.getLength(); i++) {
            Node n = aggrList.item(i);

            Float v = Float.parseFloat(XMLUtils.get_S(n, "vehicles"));
            String d = XMLUtils.get_S(n, "event_date");
            addTSentry(tsN, aggrType, d, v);

            int total = 0;
            NodeList eventList = XMLUtils.selectNodeList_S(n, ".//total");
            for (int e = 0; e < eventList.getLength(); e++) {
                total += Integer.parseInt(XMLUtils.get_S(eventList.item(e), "text()"));
            }
            addTSentry(tsE, aggrType, d, total);
        }

        TimeSeriesCollection[] dataset = new TimeSeriesCollection[2];
        dataset[0] = new TimeSeriesCollection();
        dataset[0].addSeries(tsN);
        dataset[1] = new TimeSeriesCollection();
        dataset[1].addSeries(tsE);
        return dataset;
    }

    /**
     * Returns a sample dataset.
     *
     * @return The dataset.
     * @throws Exception
     */
    private int createDatasetRing(Node xmlData, DefaultPieDataset dataset) throws Exception {
        NodeList aggrList = XMLUtils.selectNodeList_S(xmlData, "/DEFAULT_ROOT/data/aggregated/alert_types");

        int total = 0;
        Map<String, Integer> events = new HashMap<String, Integer>();
        for (int i = 0; i < aggrList.getLength(); i++) {
            Node n = aggrList.item(i);

            String event = eventLabel.get(XMLUtils.get_S(n, "id_event_type"));
            Integer count = events.computeIfAbsent(event, s -> 0);
            events.put(event, count + Integer.parseInt(XMLUtils.get_S(n, "total", "0")));
            total += Integer.parseInt(XMLUtils.get_S(n, "total", "0"));
        }

        for (Map.Entry<String, Integer> element : events.entrySet()) {
            dataset.setValue(element.getKey(), element.getValue());
        }

        return total;
    }

    /**
     * Creates a bar/line chart.
     *
     * @param dataset  the dataset.
     *
     * @return The chart.
     * @throws Exception
     */
    private JFreeChart createChartBarLine(Node xmlData) throws Exception {
        IntervalXYDataset[] dataset = createDatasetBarLine(xmlData);
        String aggrType = XMLUtils.get_S(xmlData, "/DEFAULT_ROOT/data/report_info/aggregation_type");
        long delta = getDelta(aggrType);

        //construct the plot
        XYPlot plot = new XYPlot();
        plot.setDataset(1, dataset[0]);
        plot.setDataset(0, dataset[1]);

        ValueAxis timeAxis = new DateAxis(null);
        timeAxis.setLowerMargin(0.02);  // reduce the default margins
        timeAxis.setUpperMargin(0.02);
        timeAxis.setFixedAutoRange((dataset[0].getItemCount(0) +1) * delta);

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
        plot.setRangeAxis(0, new NumberAxis("N° vehículos"));
        ((NumberAxis) plot.getRangeAxis(0)).setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        XYSplineRenderer splinerenderer = new XYSplineRenderer();
        splinerenderer.setSeriesPaint(0, Color.BLUE);
        plot.setRenderer(0, splinerenderer);
        plot.setRangeAxis(1, new NumberAxis("N° incidencias"));
        ((NumberAxis) plot.getRangeAxis(1)).setStandardTickUnits(NumberAxis.createIntegerTickUnits());

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

    /**
     * Creates a ring chart.
     *
     * @param dataset  the dataset.
     *
     * @return The chart.
     * @throws Exception
     */
    private JFreeChart createChartRing(Node xmlData) throws Exception {
        DefaultPieDataset dataset = new DefaultPieDataset();
        int total = createDatasetRing(xmlData, dataset);

        RingPlot plot = new RingPlot(dataset);
        plot.setBackgroundPaint(null);
        plot.setOutlineVisible(false);
        plot.setLabelGenerator(null);
        plot.setSectionDepth(0.35);
        plot.setSectionOutlinesVisible(false);
        plot.setSimpleLabels(true);
        plot.setShadowPaint(null);
        plot.setOuterSeparatorExtension(0);
        plot.setInnerSeparatorExtension(0);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{1}",new DecimalFormat("#"), new DecimalFormat("0%")));
        plot.setSimpleLabelOffset(new RectangleInsets(
                UnitType.RELATIVE, 0.09, 0.09, 0.09, 0.09));
        //plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        //Font font = plot.getLabelFont();

        //generate the chart
        JFreeChart chart = new JFreeChart("Total incidentes " + total, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        Color bg = new Color(192, 192, 192, 0);
        chart.setBackgroundPaint(bg);
        //chart.getLegend().visible = false;

        // remove default legend
        chart.removeLegend();

        // create and add legend
        LegendTitle legend = new LegendTitle(plot, new GridArrangement(1+ (dataset.getItemCount()/2), 2), new GridArrangement(1, 1));
        legend.setPosition(RectangleEdge.BOTTOM);
        chart.addLegend(legend);

        return chart;
    }
}