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
package tests.unit.vcl.jca.ra.cci;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.RecordFactory;

/**
 * @version 3.0.0 Mar 23, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class GVJCATestCciRecordFactory implements RecordFactory
{

    /**
     * @see javax.resource.cci.RecordFactory#createIndexedRecord(java.lang.String)
     */
    @Override
    public IndexedRecord createIndexedRecord(String s) throws ResourceException
    {
        return new GVJCATestCciIndexedRecord(s);
    }

    /**
     * @see javax.resource.cci.RecordFactory#createMappedRecord(java.lang.String)
     */
    @Override
    public MappedRecord createMappedRecord(String s) throws ResourceException
    {
        throw new ResourceException("MappedRecord not supported");
    }

}
