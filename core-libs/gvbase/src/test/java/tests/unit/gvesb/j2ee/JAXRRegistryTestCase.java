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
package tests.unit.gvesb.j2ee;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.j2ee.xmlRegistry.impl.RegistryImpl;
import junit.framework.TestCase;

import org.w3c.dom.Node;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class JAXRRegistryTestCase extends TestCase
{
    private static final String ORGANIZATION = "GreenVulcano";
    private static final String SERVICE      = "GVTestService";

    /**
     *
     */
    protected RegistryImpl      registryImpl = null;

    /**
     * Reads configuration, and creates a connection using these properties.
     *
     * @throws Exception
     *
     */
    public void setUp() throws Exception
    {
        Node uddiNode = XMLConfig.getNode("GVWebServices.xml", "/GVWebServices/UDDI");

        Node registryNode = XMLConfig.getNode(uddiNode, "*[@type='xmlregistry']");
        registryImpl = new RegistryImpl();
        registryImpl.init(registryNode, null);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
    }

    /**
     * @throws XMLConfigException
     */
    public void testJAXRRegistry() throws XMLConfigException
    {
        boolean result = registryImpl.saveBusiness(ORGANIZATION, "Test GV JAXR registry", "test@greenvulcano.com",
                "GreenVulcano Developer Team", "+39061234567");
        assertTrue(result);
        Map<String, String> businessByName = registryImpl.findBusinessByName(ORGANIZATION);
        assertNotNull(businessByName);
        Set<Entry<String, String>> entrySet = businessByName.entrySet();
        String orgKey = null;
        for (Entry<String, String> entry : entrySet) {
            if (entry.getValue().equals(ORGANIZATION)) {
                orgKey = entry.getKey();
            }
        }
        assertNotNull(orgKey);
        result = registryImpl.saveService(orgKey, SERVICE, "GreenVulcano JAXR test service",
                "http://localhost:8080/test/Test.wsdl");
        assertTrue(result);
        Map<String, String> serviceByName = registryImpl.findServiceByName(orgKey);
        assertNotNull(serviceByName);
        Set<Entry<String, String>> serviceSet = serviceByName.entrySet();
        String serviceKey = null;
        for (Entry<String, String> entry : serviceSet) {
            if (entry.getValue().equals(SERVICE)) {
                serviceKey = entry.getKey();
            }
        }
        assertNotNull(serviceKey);
        result = registryImpl.delService(serviceKey);
        assertTrue(result);

        result = registryImpl.delBusiness(orgKey);
        assertTrue(result);
    }
}
