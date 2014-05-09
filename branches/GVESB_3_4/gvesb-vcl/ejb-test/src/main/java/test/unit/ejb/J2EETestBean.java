/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package test.unit.ejb;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVPublicException;
import it.greenvulcano.util.txt.DateUtils;

import java.util.Arrays;
import java.util.Date;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/**
 * @version 3.2.0 Gen 31, 2012
 * @author GreenVulcano Developer Team
 */
public class J2EETestBean implements SessionBean
{
    /**
     * 
     */
    private static final long serialVersionUID = 280295336104881137L;

    /**
     * The session context of the EJB.
     */
    private SessionContext    sessionContext   = null;

    @Override
    public void ejbActivate()
    {
        // do nothing
    }

    @Override
    public void ejbRemove()
    {
        // do nothing
    }

    @Override
    public void ejbPassivate()
    {
        // do nothing
    }

    @Override
    public void setSessionContext(SessionContext sessionContext)
    {
        this.sessionContext = sessionContext;
    }

    public void ejbCreate() throws CreateException
    {
        // do nothing
    }

    public GVBuffer toupper(GVBuffer gvBuffer) throws GVPublicException
    {
        try {
            String str = (String) gvBuffer.getObject();
            System.out.println("J2EETestBean.toupperGVB INPUT: " + str);
            str = str.toUpperCase();
            System.out.println("J2EETestBean.toupperGVB OUTPUT: " + str);
            gvBuffer.setObject(str);
            return gvBuffer;
        }
        catch (Exception exc) {
            throw new GVPublicException("" + exc);
        }
    }

    public String[] toupper(String[] strings)
    {
        System.out.println("J2EETestBean.toupper[] INPUT: " + Arrays.toString(strings));
        String[] out = new String[strings.length];
        for (int i = 0; i < strings.length; i++) {
            out[i] = strings[i].toUpperCase();
        }
        System.out.println("J2EETestBean.toupper[] OUTPUT: " + Arrays.toString(out));
        return out;
    }

    public int sum(int i1, int i2)
    {
        System.out.println("J2EETestBean.sum(" + i1 + " + " + i2 + "): " + (i1 + i2));
        return i1 + i2;
    }

    public Date addTime(Date date, int type, int val)
    {
        System.out.println("J2EETestBean.addTime INPUT: " + DateUtils.dateToString(date, "dd/MM/yyyy HH:mm:ss")
                + " - field: " + type + " - val: " + val);
        Date out = DateUtils.addTime(date, type, val);
        System.out.println("J2EETestBean.addTime OUTPUT: " + DateUtils.dateToString(out, "dd/MM/yyyy HH:mm:ss"));
        return out;
    }

}