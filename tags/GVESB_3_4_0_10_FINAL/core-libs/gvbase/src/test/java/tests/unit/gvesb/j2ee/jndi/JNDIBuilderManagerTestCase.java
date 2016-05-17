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
package tests.unit.gvesb.j2ee.jndi;

import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.gvesb.j2ee.jndi.JNDIBuilderManager;

import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.junit.Test;

/**
 *
 * @version 3.0.0 05/lug/2010
 * @author GreenVulcano Developer Team
 */
public class JNDIBuilderManagerTestCase extends TestCase
{

    @Test
    public void testConfiguration() throws Exception
    {
        JNDIHelper jh = new JNDIHelper();
        Object obj = null;
        try {
            obj = jh.lookup("openejb:Resource/testQueue");
        }
        catch (Exception exc) {
            // TODO: handle exception
        }
        assertNotNull(obj);

        JNDIBuilderManager jbm = new JNDIBuilderManager();

        Object obj2 = null;
        try {
            obj2 = (new InitialContext()).lookup("openejb:Resource/test/queue/testQueue");
        }
        catch (Exception exc) {
            // TODO: handle exception
        }
        //assertNotNull(obj2);
        //assertEquals(obj, obj2);
    }

}
