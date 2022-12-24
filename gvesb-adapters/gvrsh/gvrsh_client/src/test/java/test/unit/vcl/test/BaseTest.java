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
package test.unit.vcl.test;

import it.greenvulcano.gvesb.rsh.server.RSHServer;

import java.io.File;

import org.apache.commons.io.FileUtils;

import junit.framework.TestCase;

/**
 * @version 3.5.0 15/05/2015
 * @author GreenVulcano Developer Team
 */
public abstract class BaseTest extends TestCase {
	protected static final String TEST_FILE_RESOURCES      = System.getProperty("user.dir") + File.separator
                                                                + "target" + File.separator + "test-classes";
	protected static final String TEST_FILE_DIR            = "TestFileManager";
	protected static final String TEST_FILE_DEST_RESOURCES = System.getProperty("java.io.tmpdir") + File.separator
                                                                + TEST_FILE_DIR;
	protected static final String TEST_FILE                = "test_shell.txt";
	protected static final String TEST_FILE_CONTENT        = "Test file for Virtual Communication Layer Shell plugin test cases.";

	protected static String       channel                  = File.separator.equals("/") ? "TEST_CHANNEL" : "TEST_CHANNEL_WIN";
	protected static RSHServer    instance                 = null;
	
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        if (instance == null) {
            instance = new RSHServer(3099, 3199);
            instance.startUp();
        }
        FileUtils.deleteQuietly(new File(TEST_FILE_DEST_RESOURCES));
        FileUtils.forceMkdir(new File(TEST_FILE_DEST_RESOURCES));
        assertTrue("System property 'it.greenvulcano.util.xpath.search.XPathAPIFactory.cfgFileXPath' not set.",
                System.getProperty("it.greenvulcano.util.xpath.search.XPathAPIFactory.cfgFileXPath") != null);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        /*if (instance != null) {
            instance.shutDown();
            Thread.sleep(2000);
        }*/
        FileUtils.deleteQuietly(new File(TEST_FILE_DEST_RESOURCES));
        super.tearDown();
    }

}
