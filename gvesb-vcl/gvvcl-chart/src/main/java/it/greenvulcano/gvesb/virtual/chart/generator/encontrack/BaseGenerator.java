/**
 *
 */
package it.greenvulcano.gvesb.virtual.chart.generator.encontrack;

import java.util.Locale;
import java.util.TimeZone;

import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.Week;

import it.greenvulcano.gvesb.virtual.chart.generator.ChartGenerator;
import it.greenvulcano.util.txt.DateUtils;

/**
 * @author gianluca
 *
 */
public abstract class BaseGenerator implements ChartGenerator {
    protected Locale loc = Locale.forLanguageTag("es_MT");
    protected TimeZone tz = TimeZone.getDefault();

    protected void addTSentry(TimeSeries ts, String aggrType, String d, Float v) throws Exception {
        if ("H".equals(aggrType)) {
            ts.add(new Hour(DateUtils.stringToDate(d, "yyyy-MM-dd HH"), this.tz, this.loc), v);
        }
        else if ("D".equals(aggrType)) {
            ts.add(new Day(DateUtils.stringToDate(d, "yyyy-MM-dd"), this.tz, this.loc), v);
        }
        else if ("W".equals(aggrType)) {
            ts.add(new Week(DateUtils.stringToDate(d, "yyyy-MM-dd"), this.tz, this.loc), v);
        }
        else if ("M".equals(aggrType)) {
            ts.add(new Month(DateUtils.stringToDate(d, "yyyy-MM"), this.tz, this.loc), v);
        }
        else if ("Y".equals(aggrType)) {
            ts.add(new Month(DateUtils.stringToDate(d, "yyyy"), this.tz, this.loc), v);
        }
        else {
            throw new Exception("Invalid aggregation type [" + aggrType + "]");
        }
    }
}
