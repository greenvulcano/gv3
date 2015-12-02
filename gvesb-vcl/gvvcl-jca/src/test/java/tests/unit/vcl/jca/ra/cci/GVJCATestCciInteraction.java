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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;
import javax.resource.cci.ResourceWarning;

/**
 * @version 3.0.0 Mar 23, 2010
 * @author GreenVulcano Developer Team
 *
 */
class GVJCATestCciInteraction implements Interaction
{

    private GVJCATestCciConnection connection;

    /**
     * @param connection
     */
    public GVJCATestCciInteraction(GVJCATestCciConnection connection)
    {
        this.connection = connection;
    }

    /**
     * @see javax.resource.cci.Interaction#clearWarnings()
     */
    @Override
    public void clearWarnings() throws ResourceException
    {
    }

    /**
     * @see javax.resource.cci.Interaction#close()
     */
    @Override
    public void close() throws ResourceException
    {
        connection = null;
    }

    /**
     * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec,
     *      javax.resource.cci.Record)
     */
    @Override
    public Record execute(InteractionSpec interactionspec, Record input) throws ResourceException
    {
        IndexedRecord output = new GVJCATestCciIndexedRecord();
        perform(interactionspec, input, output);
        return output;
    }

    /**
     * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec,
     *      javax.resource.cci.Record, javax.resource.cci.Record)
     */
    @Override
    public boolean execute(InteractionSpec interactionspec, Record input, Record output) throws ResourceException
    {
        return perform(interactionspec, input, output);
    }

    /**
     * @param record
     * @param operation
     * @param numLines
     * @param oRec
     * @return
     * @throws ResourceException
     */
    @SuppressWarnings("unchecked")
    private boolean perform(InteractionSpec interactionspec, Record input, Record output) throws ResourceException
    {
        if (interactionspec == null || (!(interactionspec instanceof GVJCATestCciInteractionSpec))) {
            throw new ResourceException("Invalid InteractionSpec class");
        }
        GVJCATestCciInteractionSpec ispec = (GVJCATestCciInteractionSpec) interactionspec;
        String operation = ispec.getOperation();
        int numLines = ispec.getNumOfLines();

        IndexedRecord oRec = null;
        if (output instanceof IndexedRecord) {
            oRec = (IndexedRecord) output;
        }
        else {
            throw new ResourceException("Output record is not an IndexedRecord");
        }

        try {
            File file = connection.getManagedConnection().getFile();
            if (file == null)
                throw new ResourceException("file is NULL!");

            if (GVJCATestCciInteractionSpec.READ.equals(operation)) {
                if (!file.exists())
                    throw new ResourceException("READ required but file [" + file.getAbsolutePath()
                            + "] does NOT exist !");

                BufferedReader br = new BufferedReader(new FileReader(file));
                try {
                    if (numLines == GVJCATestCciInteractionSpec.READ_ALL_FILE) {
                        while (br.ready()) {
                            String temp = br.readLine();
                            oRec.add(temp);
                        }
                    }
                    else {
                        for (int i = 0; i < numLines && (br.ready()); ++i) {
                            String temp = br.readLine();
                            oRec.add(temp);
                        }
                    }
                }
                finally {
                    try {
                        br.close();
                    }
                    catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
            else if (GVJCATestCciInteractionSpec.WRITE.equals(operation)) {
                IndexedRecord iRec = null;
                if (!(input instanceof IndexedRecord)) {
                    throw new ResourceException("Input record is not an IndexedRecord");
                }
                iRec = (IndexedRecord) input;

                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file.getAbsolutePath(),
                        file.exists())));
                try {
                    for (int i = 0; i < iRec.size(); ++i) {
                        pw.println(iRec.get(i));
                        pw.flush();
                    }
                }
                finally {
                    pw.close();
                }
            }
            else {
                throw new ResourceException("Wrong operation");
            }
            return true;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new ResourceException(ex.getMessage());
        }
    }

    /**
     * @see javax.resource.cci.Interaction#getConnection()
     */
    @Override
    public Connection getConnection()
    {
        return connection;
    }

    /**
     * @see javax.resource.cci.Interaction#getWarnings()
     */
    @Override
    public ResourceWarning getWarnings() throws ResourceException
    {
        return new ResourceWarning("getWarnings called");
    }

}
