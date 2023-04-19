/**
 *
 */
package it.greenvulcano.gvesb.virtual.chart.generator.encontrack;

import java.awt.Color;
import java.text.DecimalFormat;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.ColumnArrangement;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
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
public class DrivingRestingTime extends BaseGenerator implements ChartGenerator{

    /**
     * Creates a new chart.
     *
     * @param the data set
     * @throws Exception
     */
    @Override
    public JFreeChart[] generateCharts(Node xmlData) throws Exception {
        JFreeChart chart[] = new JFreeChart[2];

        IntervalXYDataset[] dataset1 = createDatasetBarLine(xmlData);
        chart[0] = createChartBarLine(dataset1);

        PieDataset dataset2 = createDatasetRing(xmlData);
        chart[1] = createChartRing(dataset2);

        return chart;
    }

    /*
    public JPanel generatePanel(Node xmlData) throws Exception {
        JFreeChart chart1 = generateChart(xmlData);
        JFreeChart chart2 = generateChart(xmlData);

        ChartPanel cp1 = new ChartPanel(chart1);
        ChartPanel cp2 = new ChartPanel(chart2);

        //cp.setMaximumDrawHeight(5);
        //cp.setMaximumDrawWidth(5);
        //cp.setZoomOutFactor(.1);
        JPanel panel = new JPanel();
        panel.add(cp1);
        panel.add(cp2);
        //middle.add(graph, BorderLayout.CENTER);

        return panel;
    }*/

    /**
     * Returns a sample dataset.
     *
     * @return The dataset.
     * @throws Exception
     */
    private IntervalXYDataset[] createDatasetBarLine(Node xmlData) throws Exception {
        TimeSeries tsKm = new TimeSeries("Km recorridos");
        TimeSeries tsIdl = new TimeSeries("N° de trayectos");
        String aggrType = XMLUtils.get_S(xmlData, "/DEFAULT_ROOT/data/report_info/aggregation_type");
        NodeList aggrList = XMLUtils.selectNodeList_S(xmlData, "/DEFAULT_ROOT/data/aggregated");

        for (int i = 0; i < aggrList.getLength(); i++) {
            Node n = aggrList.item(i);

            Float v = Float.parseFloat(XMLUtils.get_S(n, "covered_km"));
            String d = XMLUtils.get_S(n, "event_date");
            addTSentry(tsKm, aggrType, d, v);

            v = Float.parseFloat(XMLUtils.get_S(n, "num_trip"));
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
     * Returns a sample dataset.
     *
     * @return The dataset.
     * @throws Exception
     */
    private PieDataset createDatasetRing(Node xmlData) throws Exception {
        int hourDriving = Math.round(Integer.parseInt(XMLUtils.get_S(xmlData, "/DEFAULT_ROOT/data/report_info/average_driving_min")) / 60);
        int hourResting = Math.round(Integer.parseInt(XMLUtils.get_S(xmlData, "/DEFAULT_ROOT/data/report_info/average_resting_min")) / 60);

        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Apagado", hourResting);
        dataset.setValue("Conducciòn", hourResting);

        return dataset;
    }

    /**
     * Creates a bar/line chart.
     *
     * @param dataset  the dataset.
     *
     * @return The chart.
     */
    private JFreeChart createChartBarLine(IntervalXYDataset[] dataset) {
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
        plot.setRangeAxis(0, new NumberAxis("Km recorridos"));

        XYSplineRenderer splinerenderer = new XYSplineRenderer();
        splinerenderer.setSeriesPaint(0, Color.BLUE);
        plot.setRenderer(0, splinerenderer);
        plot.setRangeAxis(1, new NumberAxis("N° de trayectos"));

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
     */
    private JFreeChart createChartRing(PieDataset dataset) {
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
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setSectionPaint("Conducciòn", Color.BLUE);
        plot.setSectionPaint("Apagado", Color.GRAY);

        //generate the chart
        JFreeChart chart = new JFreeChart("Tiempo promedio", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        Color bg = new Color(192, 192, 192, 0);
        chart.setBackgroundPaint(bg);
        //chart.getLegend().visible = false;

        chart.removeLegend();
        LegendTitle legend = new LegendTitle(plot, new ColumnArrangement(), new ColumnArrangement());
        legend.setPosition(RectangleEdge.BOTTOM);
        chart.addLegend(legend);

//        chart.getLegend().setFrame(BlockBorder.NONE);
//        chart.getLegend().setPosition(RectangleEdge.BOTTOM);
//        chart.setBackgroundPaint(java.awt.Color.white);
//        chart.setPadding(new RectangleInsets(4, 8, 2, 2));
/*
        TextTitle t = chart.getTitle();
        t.setHorizontalAlignment(org.jfree.ui.HorizontalAlignment.LEFT);
        t.setPaint(new Color(240, 240, 240));
        t.setFont(new Font("Arial", Font.BOLD, 26));

        Font font=new Font("",0,16);
        plot.setLabelFont(font);
*/

        return chart;
    }
}
