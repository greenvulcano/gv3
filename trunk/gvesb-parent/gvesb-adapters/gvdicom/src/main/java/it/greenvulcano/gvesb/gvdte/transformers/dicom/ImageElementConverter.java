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
import it.greenvulcano.util.bin.BinaryUtils;
import it.greenvulcano.util.file.FileManager;
import it.greenvulcano.util.file.FileProperties;
import it.greenvulcano.util.thread.ThreadMap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dicom4j.data.elements.OtherByteString;
import org.dicom4j.data.elements.OtherWordString;
import org.dicom4j.data.elements.support.DataElement;
import org.dicom4j.dicom.DicomTag;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * 
 * @version 3.5.0 Sep 25, 2014
 * @author GreenVulcano Developer Team*
 */
public class ImageElementConverter implements Converter {
	
	private static final Logger 		logger 		= GVLogger.getLogger(ImageElementConverter.class);

	@Override
	public boolean canConvert(Class clazz) {
		return clazz.equals(OtherWordString.class) || clazz.equals(OtherByteString.class);
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		try {
			if (source instanceof OtherWordString) {
				OtherWordString data = (OtherWordString) source;
				writer.startNode("elementTag");
				context.convertAnother(data.getTag());
				writer.endNode();
				
				writer.startNode("fValueLength");
				writer.setValue(String.valueOf(data.getValueLength()));
				writer.endNode();
				
				writer.startNode("fValueMultiplicity");
				writer.setValue(String.valueOf(data.getValueMultiplicity()));
				writer.endNode();
				
				writer.startNode("ImageData");
				writer.setValue(writeImage(data));
				writer.endNode();
			}
			else if (source instanceof OtherByteString) {
				OtherByteString data = (OtherByteString) source;
				writer.startNode("elementTag");
				context.convertAnother(data.getTag());
				writer.endNode();
				
				writer.startNode("fValueLength");
				writer.setValue(String.valueOf(data.getValueLength()));
				writer.endNode();
				
				writer.startNode("fValueMultiplicity");
				writer.setValue(String.valueOf(data.getValueMultiplicity()));
				writer.endNode();
				
				writer.startNode("ImageData");
				writer.setValue(writeImage(data));
				writer.endNode();
			}
		} catch (IOException exc) {
			// do nothing
		}
	}

	private String writeImage(OtherWordString data) throws IOException {
		
		String file = ThreadMap.get("SOPInstanceUID").toString() + ".data";
		String path = ThreadMap.get("DICOM_Image_Store").toString();
		DataOutputStream dos = null;
		Set<FileProperties> files = null;
		
		try {
			files = FileManager.ls(path, file);
		} catch (Exception exc) {
			throw new IOException(exc);
		}
		
		if (!files.isEmpty()) {
			logger.debug("File " + file + " already exist on path: " + path);
		} else {
		
			try {
				short[] img = data.getShortValues();
				dos = new DataOutputStream(new FileOutputStream(new File(path, file)));
				for (int i = 0; i < img.length; i++) {
					dos.writeShort(img[i]);
				}
				files = FileManager.ls(path, file);
				if (!files.isEmpty()) {
					logger.info("File " + file + " write correctly on path: " + path);
				} else {
					logger.error("File " + file + " doesn't write correctly on path: "+ path);
				}
			}
			catch(Exception exc) {
				throw new RuntimeException(exc);
			}
			finally {
				if (dos != null) {
					try {
						dos.close();
					} catch (Exception e) {
						// do nothing
					}
				}
			}
		}
		return file;
	}
	
	private String writeImage(OtherByteString data) throws IOException {
		String file = ThreadMap.get("SOPInstanceUID").toString() + ".data";
		String path = ThreadMap.get("DICOM_Image_Store").toString();
		Set<FileProperties> files = null;
		
		try {
			files = FileManager.ls(path, file);
		} catch (Exception exc) {
			throw new IOException(exc);
		}
		
		if (!files.isEmpty()) {
			logger.debug("File " + file + " already exist on path: " + path);
		} else {
		
			try {
				byte[] img = data.getByteValues();
				BinaryUtils.writeBytesToFile(img, new File(ThreadMap.get("DICOM_Image_Store").toString(), file));
				
				files = FileManager.ls(path, file);
				if (!files.isEmpty()) {
					logger.info("File " + file + " write correctly on path: " + path);
				} else {
					logger.error("File " + file + " doesn't write correctly on path: "+ path);
				}
			}
			catch(Exception exc) {
				throw new RuntimeException(exc);
			}
		}
		return file;
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		DataElement data = null;
		String type = reader.getNodeName();
		reader.moveDown();
		DicomTag tag = (DicomTag) context.convertAnother(null, DicomTag.class);
		reader.moveUp();
		reader.moveDown(); //fValueLength
		reader.moveUp();
		reader.moveDown(); //fValueMultiplicity
		reader.moveUp();
		reader.moveDown(); //ImageData
		System.out.println(reader.getNodeName());
		String idFile = reader.getValue();
		reader.moveUp();
		try {
			if (type.equals("OtherWordString")) {
				data = new OtherWordString(tag);
				((OtherWordString)data).setValues(readShortImage(idFile));
			}
			else if (type.equals("OtherByteString")) {
				data = new OtherByteString(tag);
				data.setValues(readByteImage(idFile));
			}
		}
		catch(Exception exc) {
			throw new RuntimeException(exc);
		}
		return data;
	}
	
	private byte[] readByteImage(String idFile) {
		try {
			return BinaryUtils.readFileAsBytes(new File(ThreadMap.get("DICOM_Image_Store").toString(), idFile));
		}
		catch(Exception exc) {
			throw new RuntimeException(exc);
		}
	}
	
	private short[] readShortImage(String idFile) {
		File file = new File(ThreadMap.get("DICOM_Image_Store").toString(), idFile);
		DataInputStream dis = null;
		short[] img = null;
		try {
			img = new short[(int) file.length() / 2];
			dis = new DataInputStream(new FileInputStream(file));
			for (int i = 0; i < img.length; i++) {
				img[i] = (short) dis.readUnsignedShort();
			}
		}
		catch(Exception exc) {
			throw new RuntimeException(exc);
		}
		finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (Exception e) {
					// do nothing
				}
			}
		}
		return img;
	}

}
