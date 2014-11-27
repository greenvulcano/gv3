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
package it.greenvulcano.gvesb.gvconsole.gvcon.property;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * PropertiesEditor class
 * 
 * @version 3.4.0 Dec 12, 2010
 * @author GreenVulcano Developer Team
 */
public class PropertiesEditor {
	// private static Logger logger =
	// GVLogger.getLogger(PropertiesEditor.class);

	private Set<String> coreProps = null;
	private Set<String> adapterProps = null;
	private Set<String> supportProps = null;
	private Set<String> allProps = null;

	private List<GlobalProperty> xmlConfProps = null;

	/**
	 * @throws IOException
	 * @throws XMLConfigException
	 * @throws XMLUtilsException
	 */
	public PropertiesEditor() throws IOException, XMLConfigException,
			XMLUtilsException {
		populateAll();
	}

	/**
	 * Populates a collection containing the names of properties in the three
	 * configuration XML files
	 * 
	 * @param fileName
	 *            The configuration file name
	 * @return A set of property names found in the XML file named
	 *         <tt>fileName</tt>
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private Set<String> populateCollection(String fileName)
			throws FileNotFoundException, IOException {

		Set<String> app = new TreeSet<String>();
		String payload = TextUtils.readFileFromCP(fileName);

		String phPrefix = "xmlp{{";
		String phSuffix = "}}";
		int phPlen = phPrefix.length();
		int phSlen = phSuffix.length();
		int startNextToken = 0;
		int endNextToken = 0;
		int maxPosition = payload.length();

		while (startNextToken < maxPosition) {
			endNextToken = payload.indexOf(phPrefix, startNextToken);
			if (endNextToken != -1) {
				int endPH = payload.indexOf(phSuffix, endNextToken + phPlen);
				String phName = payload.substring(endNextToken + phPlen, endPH);
				app.add(phName);
				startNextToken = endPH + phSlen;
			} else {
				startNextToken = maxPosition;
			}
		}
		return app;
	}

	/**
	 * Populates a collection containing the properties found in the .properties
	 * files
	 * 
	 * @param fileName
	 *            The .properties file name
	 * @return A list of GlobalProperty objects containing informations found in
	 *         the specified <tt>fileName</tt>
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws XMLConfigException
	 */
	private List<GlobalProperty> populateConfCollection(String fileName)
			throws FileNotFoundException, IOException, XMLConfigException {

		List<GlobalProperty> app = new ArrayList<GlobalProperty>();
		Set<String> localAllProps = new TreeSet<String>();
		localAllProps.addAll(allProps);

		URL url = ClassLoader.getSystemResource(fileName);
		if (url == null) {
			url = PropertiesEditor.class.getClassLoader().getResource(fileName);
		}
		if (url == null) {
			throw new IOException("File " + fileName
					+ " not found in ClassPath");
		}

		String line;
		BufferedReader reader = new BufferedReader(
				new FileReader(url.getFile()));
		try {
			String description = "";
			int count = 0;
			while ((line = reader.readLine()) != null) {
				count++;
				// logger.debug(fileName + " line [" + ++count + "]: " + line);
				if (line.startsWith("#Modified by GVConsole on ")
						|| (line.trim().length() == 0)) {
					continue;
				} else if (line.startsWith("#")) {
					description = line.substring(1);
					continue;
				} else if (line.contains("=")) {
					int idx = line.indexOf("=");
					String pName = line.substring(0, idx).trim();
					String pVal = line.substring(idx + 1);
					pVal = XMLUtils.replaceXMLEntities(pVal);
					GlobalProperty prop = new GlobalProperty();
					prop.setPresent(true);
					prop.setName(pName);
					String decVal = XMLConfig.getDecrypted(pVal);
					if (pVal.equals(decVal)) {
						prop.setEncrypted(false);
						prop.setValue(pVal);
					} else {
						prop.setEncrypted(true);
						prop.setValue(decVal);
					}
					prop.setUsedIn(getUsedIn(pName));
					prop.setDescription(description);
					description = "";
					localAllProps.remove(pName);
					app.add(prop);
				} else {
					throw new XMLConfigException("The " + fileName
							+ " file is malformed. Error at line [" + count
							+ "]: " + line);
				}
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		for (String pName : localAllProps) {
			GlobalProperty prop = new GlobalProperty();
			prop.setName(pName);
			prop.setUsedIn(getUsedIn(pName));
			app.add(prop);
		}

		Collections.sort(app, new Comparator<GlobalProperty>() {
			@Override
			public int compare(GlobalProperty o1, GlobalProperty o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return app;
	}

	/**
	 * 
	 * @param pName
	 *            The property name
	 * @return The names of the files containing the property <tt>pName</tt>
	 */
	private List<String> getUsedIn(String pName) {
		List<String> usedIn = new ArrayList<String>();
		if (coreProps.contains(pName)) {
			usedIn.add("GVCore.xml");
		}
		if (adapterProps.contains(pName)) {
			usedIn.add("GVAdapters.xml");
		}
		if (supportProps.contains(pName)) {
			usedIn.add("GVSupport.xml");
		}
		return usedIn;
	}

	/**
	 * Initialization of all collections
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws XMLConfigException
	 */
	private void populateAll() throws FileNotFoundException, IOException,
			XMLConfigException {
		coreProps = populateCollection("GVCore.xml");
		adapterProps = populateCollection("GVAdapters.xml");
		supportProps = populateCollection("GVSupport.xml");

		allProps = new TreeSet<String>();
		allProps.addAll(coreProps);
		allProps.addAll(adapterProps);
		allProps.addAll(supportProps);

		xmlConfProps = populateConfCollection("XMLConfig.properties");
	}

	/**
	 * Saves the current global properties state in the XMLConfig.properties
	 * configuration file
	 * 
	 * @throws IOException
	 * @throws XMLConfigException
	 * @throws URISyntaxException
	 */
	public void saveGlobalProperties() throws IOException, URISyntaxException,
			XMLConfigException {

		String[] names = { "XMLConfig.properties", "XMLConfig.properties.old",
				"XMLConfig.properties.old.1", "XMLConfig.properties.old.2",
				"XMLConfig.properties.old.3", "XMLConfig.properties.old.4" };

		URL url = ClassLoader.getSystemResource("XMLConfig.properties");
		if (url == null) {
			url = PropertiesEditor.class.getClassLoader().getResource(
					"XMLConfig.properties");
		}
		if (url == null) {
			throw new IOException(
					"File XMLConfig.properties not found in ClassPath");
		}

		String basePath = (new File(url.getPath())).getParent();
		List<File> files = new ArrayList<File>();
		for (int i = 0; i < names.length; i++) {
			File f = new File(basePath, names[i]);
			if (!f.exists())
				break;
			files.add(f);
		}

		int freeBackup = files.size();
		for (int i = Math.min(freeBackup, names.length - 1); i >= 1; i--) {
			File fileSrc = files.get(i - 1);
			File fileDst = new File(basePath, names[i]);
			fileSrc.renameTo(fileDst);
		}

		FileWriter fw = new FileWriter(new File(basePath, names[0]));
		PrintWriter writer = new PrintWriter(new BufferedWriter(fw));
		try {
			String key = null;
			String value = null;
			String description = null;
			String heading = "#Modified by GVConsole on "
					+ new Date().toString();
			writer.println(heading);
			for (GlobalProperty prop : xmlConfProps) {
				key = prop.getName();
				value = prop.getValue();
				if (!prop.isPresent() && "".equals(prop.getValue()))
					continue;
				if (prop.isEncrypted()) {
					value = XMLConfig.getEncrypted(value);
				}
				value = XMLUtils.replaceXMLInvalidChars(value);
				description = prop.getDescription();
				if (!description.equals("") && description != null) {
					writer.println("#" + description);
				}
				writer.println(key + "=" + value);
			}
		} finally {
			if (writer != null) {
				writer.close();
			}
		}

	}

	public List<GlobalProperty> getProperties() {
		return xmlConfProps;
	}

	public void setProperties(List<GlobalProperty> props) {
		this.xmlConfProps = props;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		PropertiesEditor propsEditor = new PropertiesEditor();

		propsEditor.populateAll();

		/*
		 * for (GlobalProperty prop : propsEditor.xmlConfProps) {
		 * System.out.println(prop.toString());
		 * //System.out.println(prop.getUsedInStr()); if
		 * (prop.getName().equals("xml.not.used")){
		 * prop.setValue(prop.getValue().toUpperCase()); } }
		 * 
		 * propsEditor.saveGlobalProperties();
		 */
	}

}
