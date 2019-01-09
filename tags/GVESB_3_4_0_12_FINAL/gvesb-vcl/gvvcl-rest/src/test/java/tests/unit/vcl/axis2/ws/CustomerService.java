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

import javax.jws.WebResult;
import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import tests.unit.vcl.axis2.ws.model.Customer;
import tests.unit.vcl.axis2.ws.model.Customers;

/**
 * @version 3.4.0 Jul 18, 2013
 * @author GreenVulcano Developer Team
 * 
 */
@WebService(targetNamespace = "http://www.greenvulcano.com/gvesb/webservices")
@Path("/test/api")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public interface CustomerService
{

    @GET
    @Path("/customers")
    @WebResult(name = "Customers")
    Customers getCustomers();

    @GET
    @Path("/customers/{id}")
    @WebResult(name = "Customer")
    Customer getCustomer(@PathParam("id") long id) throws CustomerNotFoundFault;

    @GET
    @Path("/customers?id={id}")
    @WebResult(name = "Customer")
    Customer getCustomer2(@PathParam("id") long id) throws CustomerNotFoundFault;

    @PUT
    @Path("/customers")
    Response updateCustomer(Customer c);

    @POST
    @Path("/customers")
    Customer addCustomer(Customer c);

    @DELETE
    @Path("/customers/{id}")
    Response deleteCustomer(@PathParam("id") long id) throws CustomerNotFoundFault;

}