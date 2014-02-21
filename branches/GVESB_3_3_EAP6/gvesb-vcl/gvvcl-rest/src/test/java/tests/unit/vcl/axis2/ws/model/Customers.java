/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package tests.unit.vcl.axis2.ws.model;

import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @version 3.4.0 Jul 18, 2013
 * @author GreenVulcano Developer Team
 * 
 */
@XmlRootElement
public class Customers
{
    private Collection<Customer> customers;

    public Collection<Customer> getCustomer()
    {
        return customers;
    }

    public void setCustomer(Collection<Customer> c)
    {
        this.customers = c;
    }
}