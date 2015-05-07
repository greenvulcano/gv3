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

import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.thread.ThreadMap;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.log4j.Logger;
import org.dicom4j.data.DataSet;
import org.dicom4j.data.elements.AgeString;
import org.dicom4j.data.elements.CodeString;
import org.dicom4j.data.elements.DateElement;
import org.dicom4j.data.elements.DecimalString;
import org.dicom4j.data.elements.IntegerString;
import org.dicom4j.data.elements.LongString;
import org.dicom4j.data.elements.LongText;
import org.dicom4j.data.elements.OtherByteString;
import org.dicom4j.data.elements.OtherWordString;
import org.dicom4j.data.elements.PersonName;
import org.dicom4j.data.elements.ShortString;
import org.dicom4j.data.elements.SignedShort;
import org.dicom4j.data.elements.Time;
import org.dicom4j.data.elements.UniqueIdentifier;
import org.dicom4j.data.elements.UnsignedLong;
import org.dicom4j.data.elements.UnsignedShort;
import org.dicom4j.data.elements.support.DataElement;
import org.dicom4j.dicom.DicomTag;
import org.dicom4j.dicom.uniqueidentifiers.TransferSyntax;
import org.dicom4j.io.file.DicomFileWriter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.DomReader;


/**
 * 
 * @version 3.5.0 Sep 25, 2014
 * @author GreenVulcano Developer Team*
 */
public class XML2Dicom {
	
	private static final Logger 		logger 		= GVLogger.getLogger(Dicom2XML.class);
	
	/*
	 * Method that converts an XML file into a Dicom file, retrieving image information
	 * from a binary file associated.
	 * 
	 * String path must contain the path of the xml file.
	 * Path structure MUST BE: $store_dir/<patient_id>/<sop_instance_uid>.xml
	 */
	public void convertXML2Dicom(String path) throws Exception {
		
		XStream xstream = new XStream(new DomDriver());
		
		xstream.setMode(XStream.NO_REFERENCES);
		xstream.addImplicitArray(DataSet.class, "dataElements");
		xstream.registerConverter(new ImageElementConverter());
		xstream.registerConverter(new DicomStringConverter());
		
		xstream.alias("DataSet", DataSet.class);
		xstream.alias("DicomTag", DicomTag.class);
		xstream.alias("elementTag", DicomTag.class);
		xstream.alias("CodeString", CodeString.class);
		xstream.alias("UniqueIdentifier", UniqueIdentifier.class);
		xstream.alias("DateElement", DateElement.class);
		xstream.alias("Time", Time.class);
		xstream.alias("ShortString", ShortString.class);
		xstream.alias("LongString", LongString.class);
		xstream.alias("PersonName", PersonName.class);
		xstream.alias("AgeString", AgeString.class);
		xstream.alias("DecimalString", DecimalString.class);
		xstream.alias("LongText", LongText.class);
		xstream.alias("IntegerString", IntegerString.class);
		xstream.alias("UnsignedShort", UnsignedShort.class);
		xstream.alias("UnsignedLong", UnsignedLong.class);
		xstream.alias("SignedShort", SignedShort.class);
		xstream.alias("OtherWordString", OtherWordString.class);
		xstream.alias("OtherByteString", OtherByteString.class);
		
		File file = new File(path);
		ThreadMap.put("DICOM_Image_Store", file.getParent());
		
		DataSet d2 = new DataSet();
		Node doc = XMLUtils.parseObject_S(TextUtils.readFile(path), false, true);
		NodeList nl = XMLUtils.selectNodeList_S(doc, "/DataSet/entry/*");
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			logger.info("Process node[" + i + "]: " + n.getLocalName());
			DataElement de = (DataElement) xstream.unmarshal(new DomReader((Element) n));
			d2.addElement(de);
			
			logger.info("" + de);
			logger.info("--------------------");
		}
		DicomFileWriter f = new DicomFileWriter(new FileOutputStream(path.substring(0, path.lastIndexOf(".xml")) + ".dcm")); 
		f.write(d2, TransferSyntax.Default);
		f.close();
		logger.info("File Dicom generated succesfully");
		
	}
	
}
