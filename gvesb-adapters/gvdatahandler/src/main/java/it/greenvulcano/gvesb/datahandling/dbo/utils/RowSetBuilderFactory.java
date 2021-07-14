/**
 *
 */
package it.greenvulcano.gvesb.datahandling.dbo.utils;

import org.apache.log4j.Logger;

import it.greenvulcano.gvesb.datahandling.DBOException;

/**
 * @author gianluca
 *
 */
public final class RowSetBuilderFactory {
	public static RowSetBuilder getRowSetBuilder(String type, String dboName, Logger logger) throws DBOException {
		RowSetBuilder rowSetBuilder = null;
        if (type.equals("extended")) {
            rowSetBuilder = new ExtendedRowSetBuilder();
        }
        else if (type.equals("extended-uppercase")) {
            rowSetBuilder = new ExtendedUppercaseRowSetBuilder();
        }
        else if (type.equals("standard")) {
            rowSetBuilder = new StandardRowSetBuilder();
        }
        else {
            throw new DBOException("Invalid RowSetBuilder type: " + type);
        }

        rowSetBuilder.setName(dboName);
        rowSetBuilder.setLogger(logger);

        return rowSetBuilder;
	}
}
