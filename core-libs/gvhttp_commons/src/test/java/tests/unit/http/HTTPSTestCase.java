/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
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
package tests.unit.http;

import it.greenvulcano.jmx.JMXEntryPoint;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * @version 3.0.0 Jul 27, 2010
 * @author GreenVulcano Developer Team
 */
public class HTTPSTestCase extends TestCase
{
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        JMXEntryPoint.instance();
        new HttpsServer().start();
    }

    /**
     * @throws IOException
     * @throws HttpException
     *
     */
    public void testProtocolInitializer() throws HttpException, IOException
    {
        HttpClient httpClient = new HttpClient();
        httpClient.getHostConfiguration().setHost("localhost", 9888, "gvhttps");
        GetMethod method = new GetMethod("/");
        try {
            int status = httpClient.executeMethod(method);
            assertEquals(status, HttpsServer.SERVER_RESPONSE_STATUS);
            assertEquals(method.getResponseBodyAsString().trim(), HttpsServer.SERVER_RESPONSE);
        }
        finally {
            method.releaseConnection();
        }
    }
}
