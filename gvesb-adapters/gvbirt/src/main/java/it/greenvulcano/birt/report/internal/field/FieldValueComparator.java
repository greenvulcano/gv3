/**
 *
 */
package it.greenvulcano.birt.report.internal.field;

import java.util.Comparator;

/**
 * @author E@I
 *
 */
public class FieldValueComparator implements Comparator<LabelValueBean> {

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(LabelValueBean o1, LabelValueBean o2)
    {
        return o1.getLabel().compareTo(o2.getLabel());
    }

}
