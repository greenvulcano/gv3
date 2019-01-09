/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 */
package max.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * This class is used to access the localizable configuration files.<br>
 * The application access the properties using the static methods of this class.
 */
public class Config
{
    /**
     * Propriet� dell'applicazione.
     */
    private static ConfigStore      properties = null;
    private static ConfigFileReader fileReader = null;

    // private static File propertyDir = null;
    private static String           files      = null;

    /**
     * Reads a property.
     *
     * @param sect
     *        section contaning the property
     * @param property
     *        property to read.
     *
     * @return the property value or <code>null</code> if the property is not
     *         defined.
     */
    public static synchronized String get(String sect, String property)
    {
        if (properties == null) {
            reload();
        }

        return properties.get(sect, property);
    }

    /**
     * Reads a property.
     *
     * @param sect
     *
     * @param property
     *        property to read.
     * @param defaultValue
     *        value returned if the property is not defined.
     *
     * @return the property value or <code>defaultValue</code> if the property
     *         is not defined.
     */
    public static synchronized String getDef(String sect, String property, String defaultValue)
    {
        if (properties == null) {
            reload();
        }

        return properties.getDef(sect, property, defaultValue);
    }

    /**
     * Reads an integer property.
     *
     * @param sect
     *
     * @param property
     *        property to read.
     *
     * @return the property integer value.
     *
     * @exception NullPointerException
     *            if the property is not defined.
     * @exception NumberFormatException
     *            if the value is not an integer.
     */
    public static synchronized int getInteger(String sect, String property)
    {
        String v = get(sect, property);
        return Integer.parseInt(v);
    }

    /**
     * Reads an integer property.
     *
     * @param sect
     *
     * @param property
     *        property to read.
     * @param defaultValue
     *        value returned if the property is not defined.
     *
     * @return the property value or <code>defaultValue</code> if the property
     *         is not defined or it is not an integer number.
     */
    public static synchronized int getInteger(String sect, String property, int defaultValue)
    {
        try {
            return getInteger(sect, property);
        }
        catch (Exception exc) {
        }
        return defaultValue;
    }

    /**
     * Reads a long property.
     *
     * @param sect
     *
     * @param property
     *        property to read.
     *
     * @return the property long value.
     *
     * @exception NullPointerException
     *            if the property is not defined.
     * @exception NumberFormatException
     *            if the value is not a long.
     */
    public static synchronized long getLong(String sect, String property)
    {
        String v = get(sect, property);
        return Long.parseLong(v);
    }

    /**
     * Reads a long property.
     *
     * @param sect
     *
     * @param property
     *        property to read.
     * @param defaultValue
     *        value returned if the property is not defined.
     *
     * @return the property value or <code>defaultValue</code> if the property
     *         is not defined or it is not a long number.
     */
    public static synchronized long getLong(String sect, String property, long defaultValue)
    {
        try {
            return getLong(sect, property);
        }
        catch (Exception exc) {
        }
        return defaultValue;
    }

    /**
     * Reads a double property.
     *
     * @param sect
     *
     * @param property
     *        property to read.
     *
     * @return the property double value.
     *
     * @exception NullPointerException
     *            if the property is not defined.
     * @exception NumberFormatException
     *            if the value is not a double.
     */
    public static synchronized double getDouble(String sect, String property)
    {
        String v = get(sect, property);
        return Double.parseDouble(v);
    }

    /**
     * Reads a double property.
     *
     * @param sect
     *
     * @param property
     *        property to read.
     * @param defaultValue
     *        value returned if the property is not defined.
     *
     * @return the property value or <code>defaultValue</code> if the property
     *         is not defined or it is not a double number.
     */
    public static synchronized double getDouble(String sect, String property, double defaultValue)
    {
        try {
            return getDouble(sect, property);
        }
        catch (Exception exc) {
        }
        return defaultValue;
    }

    /**
     * Reads a float property.
     *
     * @param sect
     *
     * @param property
     *        property to read.
     *
     * @return the property float value.
     *
     * @exception NullPointerException
     *            if the property is not defined.
     * @exception NumberFormatException
     *            if the value is not a float.
     */
    public static synchronized float getFloat(String sect, String property)
    {
        String v = get(sect, property);
        return Float.parseFloat(v);
    }

    /**
     * Reads a float property.
     *
     * @param sect
     *
     * @param property
     *        property to read.
     * @param defaultValue
     *        value returned if the property is not defined.
     *
     * @return the property value or <code>defaultValue</code> if the property
     *         is not defined or it is not a float number.
     */
    public static synchronized float getFloat(String sect, String property, float defaultValue)
    {
        try {
            return getFloat(sect, property);
        }
        catch (Exception exc) {
        }
        return defaultValue;
    }

    /**
     * Check the existence of a property.
     *
     * @param sect
     *        section containng the property
     * @param property
     *        property to check existence.
     *
     * @return <code>true</code> if the property is defined, <code>false</code>
     *         otherwise.
     */
    public static synchronized boolean exists(String sect, String property)
    {
        if (properties == null) {
            reload();
        }

        return properties.get(sect, property) != null;
    }

    /**
     * @return the default Locale for the JVM
     */
    public static synchronized Locale getLocale()
    {
        if (properties == null) {
            reload();
        }

        return Locale.getDefault();
    }

    /**
     * Ri-Carica i parametri del sistema.
     */
    public static synchronized void reload()
    {
        try {
            fileReader = new ConfigFileReader();
            properties = fileReader.getProperties();
            readRoot(fileReader);
            loadFiles(fileReader);
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private static void readRoot(ConfigFileReader fp)
    {
        try {
            fp.read("root.properties");
        }
        catch (FileNotFoundException exc) {
            System.out.println("Max: file root.properties not found");
        }

        Locale lc = Locale.getDefault();
        String language = properties.getDef("", "max.locale.language", lc.getLanguage());
        String country = properties.getDef("", "max.locale.country", lc.getCountry());
        String variant = properties.getDef("", "max.locale.variant", lc.getVariant());
        Locale.setDefault(new Locale(language, country, variant));

        files = properties.get("", "max.property.files");
    }

    private static void loadFiles(ConfigFileReader fp)
    {
        if (files == null) {
            System.out.println("max.property.files not defined: no property files to read");
            return;
        }

        StringTokenizer tokens = new StringTokenizer(files, ",", false);
        while (tokens.hasMoreTokens()) {
            String file = tokens.nextToken().trim();
            if (file.equals("")) {
                continue;
            }

            try {
                fp.read(file + ".properties");
            }
            catch (FileNotFoundException exc) {
                exc.printStackTrace();
            }
        }
    }

    /**
     * @param sect
     * @return the keys for the section
     */
    public static Enumeration<String> keys(String sect)
    {
        if (properties == null) {
            reload();
        }

        return properties.keys(sect);
    }

    /**
     * @return the sections enumeration
     */
    public static Enumeration<String> sections()
    {
        if (properties == null) {
            reload();
        }

        return properties.sections();
    }

    /**
     * @param section
     * @param property
     * @param value
     * @throws ProtocolException
     * @throws IOException
     */
    public static void write(String section, String property, String value) throws ProtocolException, IOException
    {
        properties.set(section, property, value);
        fileReader.write(section, property, value);
    }

    /**
     * @param section
     * @param property
     * @throws ProtocolException
     * @throws IOException
     */
    public static void remove(String section, String property) throws ProtocolException, IOException
    {
        properties.remove(section, property);
        fileReader.write(section, property, null);
    }
}
