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
package it.greenvulcano.gvesb.gvdte.transformers.dicom;

import it.greenvulcano.gvesb.gvdicom.DicomAdapterException;

import java.io.File;

import javax.imageio.ImageIO;

import org.dicom4j.data.DataSet;
import org.dicom4j.images.DicomFrame;
import org.dicom4j.images.DicomImage;
import org.dicom4j.images.DicomImageReader;


/**
 * 
 * @version 3.5.0 Sep 25, 2014
 * @author GreenVulcano Developer Team*
 */
public class WriteImage {
	
	public void exportImageRGB(DataSet data) throws Exception, DicomAdapterException {
		
		try {

			DicomImage dImage = DicomImageReader.createDicomFrames(data);
			
			int fSize = dImage.count();
			for (int i = 0; i < fSize; i++) {
				DicomFrame dFrame = dImage.getFrame(i);
				System.out.println("Writing image[" + i + "]");
				File outputfile = new File("image_" + i + ".jpg");
				ImageIO.write(dFrame, "jpg", outputfile);
			}
			
			System.out.println("Done");
		} catch (Exception exc) {
			throw new DicomAdapterException("ERROR_IN_FILE_WRITING", new String[][]{{"message", exc.getMessage()}}, exc);
		}
	}

}
