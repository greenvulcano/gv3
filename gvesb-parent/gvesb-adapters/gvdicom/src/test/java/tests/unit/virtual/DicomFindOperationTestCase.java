/*
 * Copyright (c) 2009-2015 GreenVulcano ESB Open Source Project. All rights
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
package tests.unit.virtual;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
//import it.greenvulcano.gvesb.gvdicom.GVDicomListenerManager;
import it.greenvulcano.gvesb.virtual.dicom.DicomFindCallOperation;

import org.junit.Test;
import org.w3c.dom.Node;

import junit.framework.TestCase;


/**
 * 
 * @version 3.5.0 Sep 25, 2014
 * @author GreenVulcano Developer Team*
 */
public class DicomFindOperationTestCase extends TestCase {
    

    /**
     * @throws Exception
     */
	@Test
    public void testCall() throws Exception {
        
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/dicom-find-call[@name='test_find']");
        
        assertTrue(node!=null);
        DicomFindCallOperation dicomFind = new DicomFindCallOperation();
        dicomFind.init(node);
        GVBuffer gvBuffer = new GVBuffer();
        GVBuffer result = dicomFind.perform(gvBuffer);
        System.out.println(result);
        
    }
}
