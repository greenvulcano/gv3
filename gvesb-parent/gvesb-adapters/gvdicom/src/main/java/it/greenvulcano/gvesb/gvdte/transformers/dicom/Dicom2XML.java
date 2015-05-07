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
import it.greenvulcano.util.file.FileManager;
import it.greenvulcano.util.file.FileProperties;
import it.greenvulcano.util.thread.ThreadMap;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.File;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

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
import org.dicom4j.dicom.DicomTag;
import org.dicom4j.dicom.DicomTags;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;


/**
 * 
 * @version 3.5.0 Sep 25, 2014
 * @author GreenVulcano Developer Team*
 */
public class Dicom2XML {
	
	private static final Logger 		logger 		= GVLogger.getLogger(Dicom2XML.class);
	
	/*
	 * Method that converts a Dicom file into an XML file, splitting image information 
	 * into binary file.
	 * 
	 * DataSet data must contain the dataset of file Dicom which must be converted .
	 */
	public void convertDicom2XML(DataSet data, String storeDir) throws Exception {
		
		XMLUtils parser = null;
		try {
			
			parser = XMLUtils.getParserInstance();
			
			Document document = parser.newDocument();
			XStream xstream = new XStream(new StaxDriver());
			
			xstream.setMode(XStream.NO_REFERENCES);
			xstream.addImplicitArray(DataSet.class, "dataElements");
			
			xstream.registerConverter(new ImageElementConverter());
			xstream.registerConverter(new DicomStringConverter());
			
			xstream.alias("DataSet", DataSet.class);
			xstream.alias("DicomTag", DicomTag.class);
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
			
			ThreadMap.put("DICOM_Image_Store", storeDir);
			ThreadMap.put("SOPInstanceUID", data.getElement(DicomTags.SOPInstanceUID).getSingleStringValue());
			
			DomWriter dw = new DomWriter(document);
			xstream.marshal(data, dw);
			String xml = XMLUtils.serializeDOM_S(filter(new DOMSource(document)), "UTF-8", false, true);
			
			String nameFile = ThreadMap.get("SOPInstanceUID").toString();
			
			Set<FileProperties> files = null;
			files = FileManager.ls(storeDir, nameFile + ".xml");
			
			if (!files.isEmpty()) {
				logger.debug("File " + nameFile + ".xml already exist on path: " + storeDir);
			} else {
			
				TextUtils.writeFile(xml, storeDir + File.separator + nameFile + ".xml");
				files = FileManager.ls(storeDir, nameFile + ".xml");
				if (!files.isEmpty()) {
					logger.info("File " + nameFile + ".xml write correctly on path: " + storeDir);
				} else {
					logger.error("File " + nameFile + ".xml doesn't write correctly on path: "+ storeDir);
				}
				
			}
		}
		finally {
			ThreadMap.remove("DICOM_Image_Store");
			ThreadMap.remove("SOPInstanceUID");
			XMLUtils.releaseParserInstance(parser);
		}
		
	}
	
	/*
	 * Method filtering node DicomTag to avoid repetition in the XML file generated
	 */
	public static Node filter(Source doc) throws Exception {
		Source transformation = new StreamSource(Dicom2XML.class.getResourceAsStream("DicomFilter.xsl"));
		TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(transformation);
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        DOMResult theDOMResult = new DOMResult();
        transformer.transform(doc, theDOMResult);
        return theDOMResult.getNode();
	}
}
