/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:55 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/util/Indices.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Id: Indices.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.util;

import java.util.*;


/**
 * Uso:
 * <li>Instanziare un oggetto Indices
 * <li>Modificare i fields low, middle e high
 * <li>toString() per ottenere un indice sotto forma di String
 * <li>toString(boolean) per ottenere un indice sotto forma di array di String
 * <li>toVector(boolean) per ottenere un vettore contenente gli indici
 */
public class Indices
{
    protected int low;
    protected int middle;
    protected int high;

    public Indices(int low, int middle, int high)
    {
        this.low = Integer.MIN_VALUE;
        this.high = Integer.MAX_VALUE;
        setLow(low);
        setHigh(high);
        setMiddle(middle);
    }

    public final int getLow()
    {
        return low;
    }

    public final int getHigh()
    {
        return high;
    }

    public final int getMiddle()
    {
        return middle;
    }

    public final void setLow(int l)
    {
        low = l;
        if(low > high) low = high;
    }

    public final void setHigh(int h)
    {
        high = h;
        if(low > high) high = low;
    }

    public final void setMiddle(int m)
    {
        middle = m;
        if(low > middle) middle = low;
        if(high < middle) middle = high;
    }

    public final int[] calculate()
    {
        int m = middle;
        if(m < low) m = low;
        if(m > high) m = high;
        int ret[] = calculate(low, m, high);
        return normalize(ret);
        //return ret;
    }

    public int[] calculate(int l, int m, int h)
    {
        int left[] = calculate(l, m, factor(m - l), true);
        int right[] = calculate(m + 1, h, factor(h - m), false);
        int ret[] = new int[left.length + right.length];
        System.arraycopy(left, 0, ret, 0, left.length);
        System.arraycopy(right, 0, ret, left.length, right.length);
        return ret;
    }

    public int[] calculate(int a, int b, int f, boolean up)
    {
        if(a > b) return new int[0];
        if(a == b) return new int[] { a };
        if(a == b - 1) return new int[] { a, b };

        int d = b - a;
        if(d < 7) f = 2;

        Vector v = new Vector();
        if(up) {
            int m = b - d / f;
            v.add(new Integer(a));
            int r[] = calculate(m, b, f, up);
            for(int i = 0; i < r.length; ++i)
                v.add(new Integer(r[i]));
        }
        else {
            int m = a + d / f;
            int r[] = calculate(a, m, f, up);
            for(int i = 0; i < r.length; ++i)
                v.add(new Integer(r[i]));
            v.add(new Integer(b));
        }
        int ret[] = new int[v.size()];
        for(int i = 0; i < ret.length; ++i)
            ret[i] = ((Integer)v.elementAt(i)).intValue();

        return ret;
    }

    public int factor(int d)
    {
        int f = (int)Math.round(Math.log(d) / Math.log(4D));
        if(f < 2) return 2;
        return f;
    }

    private static final int ranges[] = {500, 200, 100, 50, 20, 10, 5};
    private static final int round[] =  {250, 100,  50, 25, 10,  5, 3};

    public final int[] normalize(int v[])
    {
        int N = v.length;
        if(N < 2) return v;
        int ret[] = new int[N];
        ret[0] = v[0];
        ret[N - 1] = v[N - 1];
        for(int i = 1; i < N - 1; ++i) {
            boolean found = false;
            for(int j = 0; j < ranges.length && !found; ++j) {
                if((v[i] - v[i - 1] >= ranges[j]) &&
                   (v[i + 1] - v[i] >= ranges[j])) {
                    ret[i] = ((v[i] + round[j]) / ranges[j]) * ranges[j];
                    found = true;
                }
            }
            if(!found) ret[i] = v[i];
        }
        return ret;
    }

    public final Vector toVector(boolean dots)
    {
        Vector v = new Vector();
        int ret[] = calculate();
        int N = ret.length;
        if(N == 0) return v;

        v.add("" + ret[0]);
        int prev = ret[0];
        for(int i = 1; i < ret.length; ++i) {
            if(ret[i] - prev > 2) {
                if(dots) v.add("...");
            }
            else if(ret[i] - prev == 2) {
                v.add("" + (ret[i] - 1));
            }
            v.add("" + ret[i]);
            prev = ret[i];
        }
        return v;
    }

    public final String[] toString(boolean dots)
    {
        Vector v = toVector(dots);
        String ret[] = new String[v.size()];
        for(int i = 0; i < ret.length; ++i) {
            ret[i] = (String)v.elementAt(i);
        }
        return ret;
    }

    public final String toString()
    {
        int ret[] = calculate();
        int N = ret.length;
        if(N == 0) return "";

        StringBuffer buf = new StringBuffer();
        buf.append(ret[0]);
        int prev = ret[0];
        for(int i = 1; i < ret.length; ++i) {
            if(ret[i] - prev > 2) {
                buf.append(" ...");
            }
            else if(ret[i] - prev == 2) {
                buf.append(" ").append(ret[i] - 1);
            }
            buf.append(" ").append(ret[i]);
            prev = ret[i];
        }
        return buf.toString();
    }

    public static void main(String args[])
    {
        int low = Integer.parseInt(args[0]);
        int middle = Integer.parseInt(args[1]);
        int high = Integer.parseInt(args[2]);

        Indices idx = new Indices(low, middle, high);
        String arr[] = idx.toString(true);

        for(int i = 0; i < arr.length; ++i) {
            System.out.print(arr[i]);
            System.out.print(" ");
        }
        System.out.println("");
    }
}
