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
package tests.unit.vcl.axis2.ws;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.ws.rs.core.Response;

import tests.unit.vcl.axis2.ws.model.Customer;
import tests.unit.vcl.axis2.ws.model.Customers;

/**
 * @version 3.4.0 Jul 18, 2013
 * @author GreenVulcano Developer Team
 * 
 */
@Stateless
@WebService(endpointInterface = "tests.unit.vcl.axis2.ws.CustomerService")
public class CustomerServiceImpl implements CustomerService
{
    Map<Long, Customer> customers = new HashMap<Long, Customer>();

    public CustomerServiceImpl()
    {
        Customer customer = createCustomer("Mario", customers.size() + 1);
        customers.put(customer.getId(), customer);
    }

    public Customers getCustomers()
    {
        Customers c = new Customers();
        c.setCustomer(customers.values());
        return c;
    }

    public Customer getCustomer(long id) throws CustomerNotFoundFault
    {
        Customer c = customers.get(id);
        if (c == null) {
            CustomerNotFoundDetails details = new CustomerNotFoundDetails();
            details.setId(id);
            throw new CustomerNotFoundFault(details);
        }
        return c;
    }

    public Customer getCustomer2(long id) throws CustomerNotFoundFault
    {
        Customer c = customers.get(id);
        if (c == null) {
            CustomerNotFoundDetails details = new CustomerNotFoundDetails();
            details.setId(id);
            throw new CustomerNotFoundFault(details);
        }
        return c;
    }

    public Response updateCustomer(Customer c)
    {
        customers.put(c.getId(), c);
        return Response.status(Response.Status.OK).build();
    }

    public Customer addCustomer(Customer c)
    {
        long id = customers.size() + 1;
        c.setId(id);

        customers.put(id, c);

        return c;
    }

    public Response deleteCustomer(long id)
    {
        customers.remove(id);
        return Response.status(Response.Status.OK).build();
    }

    final Customer createCustomer(String name, long id)
    {
        Customer c = new Customer();
        c.setName(name);
        c.setId(id);
        return c;
    }
}