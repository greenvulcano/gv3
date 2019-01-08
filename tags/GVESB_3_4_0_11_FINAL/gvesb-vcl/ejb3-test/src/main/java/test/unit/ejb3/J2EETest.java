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
package test.unit.ejb3;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;

import java.util.Date;

/**
 * Interface of the J2EETest EJB.
 * 
 * 
 * @version 3.2.0 Gen 31, 2012
 * @author GreenVulcano Developer Team
 */
public interface J2EETest
{
    public GVBuffer toupper(GVBuffer gvBuffer) throws GVException;

    public String[] toupper(String[] strings) throws GVException;

    public int sum(int i1, int i2) throws GVException;

    public Date addTime(Date date, int type, int val) throws GVException;
}
