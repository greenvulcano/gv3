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
package it.greenvulcano.util.txt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.Map.Entry;

/**
 * PropertiesFileReader class
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class PropertiesFileReader
{

    /**
     * @param filename
     * @param basePath
     * @return the properties read from file
     * @throws Exception
     */
    public static Properties readFile(String filename, String basePath) throws Exception
    {
        Properties props = null;

        try {
            if (basePath != null) {
                filename = basePath + File.separator + filename;
            }

            System.out.println("PropertiesFileReader - Reading: " + filename);

            String locBasePath = filename;
            int lastSep = locBasePath.lastIndexOf(File.separator);
            if (lastSep != -1) {
                locBasePath = locBasePath.substring(0, lastSep);
            }
            else {
                locBasePath = ".";
            }

            props = new Properties();
            FileInputStream in = null;
            try {
                in = new FileInputStream(filename);

                props.load(in);
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (Exception exc) {
                        // do nothing
                    }
                }
            }

            props = processInclude(props, locBasePath);

            return props;
        }
        catch (FileNotFoundException exc) {
            throw exc;
        }
        catch (Exception exc) {
            System.err.println("Error reading properties file [" + filename + " from ClassPath");
            exc.printStackTrace();
            throw exc;
        }
    }

    /**
     * @param filename
     * @return the properties read from file
     * @throws Exception
     */
    public static Properties readFileFromCP(String filename) throws Exception
    {
        Properties props = null;

        try {
            URL url = ClassLoader.getSystemResource(filename);
            if (url == null) {
                url = PropertiesFileReader.class.getClassLoader().getResource(filename);
            }
            if (url == null) {
                System.err.println("PropertiesFileReader: file not found: " + filename);
                throw new FileNotFoundException("File " + filename + " not found in ClassPath");
            }

            System.out.println("PropertiesFileReader - Reading: " + url);

            String basePath = url.getFile();
            basePath = basePath.substring(0, basePath.lastIndexOf("/"));

            props = new Properties();

            InputStream in = url.openStream();
            try {
                props.load(in);
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (Exception exc) {
                        // do nothing
                    }
                }
            }

            props = processInclude(props, basePath);

            return props;
        }
        catch (FileNotFoundException exc) {
            throw exc;
        }
        catch (Exception exc) {
            System.err.println("Error reading properties file [" + filename + " from ClassPath");
            exc.printStackTrace();
            throw exc;
        }
    }

    /**
     * @param properties
     * @return the properties as map
     */
    public static Map<String, String> propertiesToMap(Properties properties)
    {
        Map<String, String> map = new HashMap<String, String>();
        for (Entry<Object, Object> entry : properties.entrySet()) {
            map.put((String) entry.getKey(), (String) entry.getValue());
        }
        return map;
    }

    /**
     * @param props
     * @param basePath
     * @return
     */
    private static Properties processInclude(Properties props, String basePath) throws Exception
    {
        Properties includes = new Properties();
        Vector<String> includeNames = getIncludeNames(props);
        Iterator<String> i = includeNames.iterator();
        while (i.hasNext()) {
            Properties include = readFile(i.next(), basePath);
            includes.putAll(include);
        }

        props.putAll(includes);
        return props;
    }

    /**
     * @param props
     * @return
     */
    private static Vector<String> getIncludeNames(Properties props)
    {
        Vector<String> includeNames = new Vector<String>();
        Vector<String> inclKeys = new Vector<String>();
        Enumeration<Object> keys = props.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            if (key.startsWith("include.file_")) {
                inclKeys.add(key);
            }
        }

        Collections.sort(inclKeys);
        Iterator<String> i = inclKeys.iterator();
        while (i.hasNext()) {
            includeNames.add((String) props.remove(i.next()));
        }

        return includeNames;
    }
}
