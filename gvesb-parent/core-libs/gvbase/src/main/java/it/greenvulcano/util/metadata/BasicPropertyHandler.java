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
package it.greenvulcano.util.metadata;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.script.ScriptExecutor;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.StringReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Helper class for basic metadata substitution in strings.
 * 
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class BasicPropertyHandler implements PropertyHandler
{
    static {
        BasicPropertyHandler handler = new BasicPropertyHandler();
        PropertiesHandler.registerHandler("%", handler);
        PropertiesHandler.registerHandler("$", handler);
        PropertiesHandler.registerHandler("sp", handler);
        PropertiesHandler.registerHandler("env", handler);
        PropertiesHandler.registerHandler("@", handler);
        PropertiesHandler.registerHandler("xpath", handler);
        PropertiesHandler.registerHandler("timestamp", handler);
        PropertiesHandler.registerHandler("dateformat", handler);
        PropertiesHandler.registerHandler("dateAdd", handler);
        PropertiesHandler.registerHandler("dateformatAdd", handler);
        PropertiesHandler.registerHandler("decode", handler);
        PropertiesHandler.registerHandler("decodeL", handler);
        PropertiesHandler.registerHandler("script", handler);
        PropertiesHandler.registerHandler("js", handler);
        PropertiesHandler.registerHandler("ognl", handler);
        PropertiesHandler.registerHandler("escJS", handler);
        PropertiesHandler.registerHandler("escSQL", handler);
        PropertiesHandler.registerHandler("escXML", handler);
        PropertiesHandler.registerHandler("replace", handler);
        PropertiesHandler.registerHandler("urlEnc", handler);
        PropertiesHandler.registerHandler("urlDec", handler);
        PropertiesHandler.registerHandler("xmlp", handler);
    }

    /**
     * This method insert the correct values for the dynamic parameter found in
     * the input string. The property value can be a combination of:
     * 
     * <pre>
     * - fixed : a text string;
     * - %{{class}}         : the obj class name;
     * - %{{fqclass}}       : the obj fully qualified class name;
     * - %{{package}}       : the obj package name;
     * - ${{propname[::default]}}  : a System property value;
     * - sp{{propname[::default]}} : a System property value;
     * - env{{varname[::default]}} : an Environment variable value;
     * - @{{propname[::default]}}  : a inProperties property value;
     * - xmlp{{propname}}   : a inProperties property value, only used by
     *                        XMLConfig on xml files reading;
     * - xpath{{field::path}} : parse the inProperties 'field' value, then
     *                          apply the xpath and return the found value
     * - xpath{{file://name::path}}  : if 'field' begin with 'file://' the following string
     *                                 must be a file in the classpath on which apply the xpath.
     *                                 The metadata is handled by XMLConfig.
     * - timestamp{{pattern[::tZone]]}} : return the current timestamp, in optional tZone value, formatted as 'pattern'
     * - dateformat{{date::source-pattern::dest-pattern[::source-tZone::dest-tZone]}} : reformat 'date' from 'source-pattern' to 'dest-pattern',
     *                          and optionally from 'source-tZone' to 'dest-tZone'   
     * - dateAdd{{date::pattern::type::value}} : add to 'date', formatted as 'pattern', 'value' element of 'type': [s]econd, [m]inute, [h]our, [d]ay, [M]onth, [y]ear
     * - dateformatAdd{{date::source-pattern::dest-pattern::type::value[::source-tZone::dest-tZone]}} : reformat 'date' from 'source-pattern' to 'dest-pattern',
     *                          and optionally from 'source-tZone' to 'dest-tZone', add to 'date' 'value' element of 'type'
     * - decode{{field[::cond1::val1][::cond2::val2][cond...n::val...n]::default}} :
     *                          evaluate as if-then-else; if 'field' is equal to cond1...n,
     *                          return the value of val1...n, otherwise 'default'
     * - decodeL{{sep::field[::cond1::val1][::cond2::val2][cond...n::val...n]::default}} :
     *                          is equivalent to 'decode', with the difference that 'condX'
     *                          can be a list of values separated by 'sep'
     * - script{{lang::[scope::]script}} : evaluate a 'lang' script, using the base context 'scope',
     *                           the inProperties map is added to the context as 'inProperties',
     *                           the object is added to the context as 'object',
     *                           the extra is added to the context as 'extra'
     * - js{{[scope::]script}} : evaluate a JavaScript script, using the context 'scope',
     *                           the inProperties map is added to the context as 'inProperties',
     *                           the object is added to the context as 'object',
     *                           the extra is added to the context as 'extra'
     * - ognl{{script}} : evaluate a OGNL script,
     *                    the inProperties map is added to the context as 'inProperties',
     *                    the object is added to the context as 'object' (and is also the object on which execute the script !! NO MORE FROM 3.5 !!),
     *                    the extra is added to the context as 'extra'
     * - escJS{{string}}    : escapes invalid JavaScript characters from 'string' (ex. ' -> \')
     * - escSQL{{string}}   : escapes invalid SQL characters from 'string' (ex. ' -> '')
     * - escXML{{string}}   : escapes invalid XML characters from 'string' (ex. ' -> &apos;)
     * - replace{{string::search::subst}}   : replace in 'string' all occurrences of 'search' with 'replace'
     * - urlEnc{{string}}   : URL encode invalid characters from 'string'
     * - urlDec{{string}}   : decode URL encoded characters from 'string'
     * </pre>
     * 
     * @param type
     * 
     * @param str
     *        the string to value
     * @param inProperties
     *        the hashTable containing the properties
     * @param object
     *        the object to work with
     * @param extra
     * @return the expanded string
     * @throws PropertiesHandlerException
     */
    @Override
    public String expand(String type, String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        if (type.startsWith("%")) {
            return expandClassProperties(str, inProperties, object, extra);
        }
        else if (type.startsWith("$") || type.startsWith("sp")) {
            return expandSystemProperties(str, inProperties, object, extra);
        }
        else if (type.startsWith("@")) {
            return expandInProperties(str, inProperties, object, extra);
        }
        else if (type.startsWith("env")) {
            return expandEnvVariable(str, inProperties, object, extra);
        }
        else if (type.startsWith("xpath")) {
            return expandXPathProperties(str, inProperties, object, extra);
        }
        else if (type.startsWith("timestamp")) {
            return expandTimestamp(str, inProperties, object, extra);
        }
        else if (type.startsWith("dateformatAdd")) {
            return expandDateFormatAdd(str, inProperties, object, extra);
        }
        else if (type.startsWith("dateformat")) {
            return expandDateFormat(str, inProperties, object, extra);
        }
        else if (type.startsWith("dateAdd")) {
            return expandDateAdd(str, inProperties, object, extra);
        }
        else if (type.startsWith("decodeL")) {
            return expandDecodeL(str, inProperties, object, extra);
        }
        else if (type.startsWith("decode")) {
            return expandDecode(str, inProperties, object, extra);
        }
        else if (type.startsWith("script")) {
            return expandScript(str, inProperties, object, extra);
        }
        else if (type.startsWith("js")) {
            return expandJavaScript(str, inProperties, object, extra);
        }
        else if (type.startsWith("ognl")) {
            return expandOGNL(str, inProperties, object, extra);
        }
        else if (type.startsWith("escJS")) {
            return expandEscJS(str, inProperties, object, extra);
        }
        else if (type.startsWith("escSQL")) {
            return expandEscSQL(str, inProperties, object, extra);
        }
        else if (type.startsWith("escXML")) {
            return expandEscXML(str, inProperties, object, extra);
        }
        else if (type.startsWith("replace")) {
            return expandReplace(str, inProperties, object, extra);
        }
        else if (type.startsWith("urlEnc")) {
            return expandUrlEnc(str, inProperties, object, extra);
        }
        else if (type.startsWith("urlDec")) {
            return expandUrlDec(str, inProperties, object, extra);
        }
        else if (type.startsWith("xmlp")) {
            // DUMMY replacement - Must be handled by XMLConfig
            return "xmlp" + PROP_START + str + PROP_END;
        }
        return str;
    }

    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandSystemProperties(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
    	if (!PropertiesHandler.isExpanded(str)) {
            str = PropertiesHandler.expand(str, inProperties, object, extra);
        }
        String propName = str;
        String defValue = "";
        int pIdx = str.indexOf("::");
        if (pIdx != -1) {
            propName = str.substring(0, pIdx);
        	defValue = str.substring(pIdx + 2);
        }

        String paramValue = System.getProperty(propName, defValue);
        if (!PropertiesHandler.isExpanded(paramValue)) {
            paramValue = PropertiesHandler.expand(paramValue, inProperties, object, extra);
        }
        return (paramValue != null ? paramValue : defValue);
    }

    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandEnvVariable(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
    	if (!PropertiesHandler.isExpanded(str)) {
            str = PropertiesHandler.expand(str, inProperties, object, extra);
        }
        String propName = str;
        String defValue = "";
        int pIdx = str.indexOf("::");
        if (pIdx != -1) {
            propName = str.substring(0, pIdx);
        	defValue = str.substring(pIdx + 2);
        }
        String paramValue = System.getenv(propName);
        if (paramValue == null) {
            paramValue = defValue;
        }
        if (!PropertiesHandler.isExpanded(paramValue)) {
            paramValue = PropertiesHandler.expand(paramValue, inProperties, object, extra);
        }
        return (paramValue != null ? paramValue : defValue);
    }

    /**
     * @param str
     *        the string to valorize
     * @param inProperties
     *        the hashTable containing the properties
     * @return the expanded string
     */
    private static String expandInProperties(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
    	if (!PropertiesHandler.isExpanded(str)) {
            str = PropertiesHandler.expand(str, inProperties, object, extra);
        }
        String propName = str;
        String defValue = null;
        int pIdx = str.indexOf("::");
        if (pIdx != -1) {
            propName = str.substring(0, pIdx);
        	defValue = str.substring(pIdx + 2);
        }
        if (inProperties == null) {
        	if (defValue == null) {
        		return "@" + PROP_START + str + PROP_END;
        	}
        	return defValue;
        }
        Object obj = inProperties.get(propName);
        if (obj == null) {
        	if (defValue == null) {
        		return "@" + PROP_START + str + PROP_END;
        	}
        	return defValue;
        }
        String paramValue = obj.toString();
        if (!PropertiesHandler.isExpanded(paramValue)) {
            paramValue = PropertiesHandler.expand(paramValue, inProperties, object, extra);
        }
        return paramValue;
    }

    /**
     * @param str
     *        the string to valorize
     * @param object
     *        the object to work with
     * @return the expanded string
     */
    private static String expandClassProperties(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        String propName = str;
        String paramValue = "";
        if (object == null) {
            return "%" + PROP_START + str + PROP_END;
        }
        if (!PropertiesHandler.isExpanded(propName)) {
            propName = PropertiesHandler.expand(propName, inProperties, object, extra);
        }
        if (propName.equals("fqclass")) {
            paramValue = object.getClass().getName();
        }
        else if (propName.equals("package")) {
            paramValue = object.getClass().getPackage().getName();
        }
        else if (propName.equals("class")) {
            String fqClassName = object.getClass().getName();
            paramValue = fqClassName.substring(fqClassName.lastIndexOf(".") + 1);
        }
        if (paramValue.equals("")) {
            return "%" + PROP_START + str + PROP_END;
        }
        return paramValue;
    }


    private String expandXPathProperties(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        XMLUtils parser = null;
        String paramName = null;
        String paramValue = null;
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, extra);
            }
            int pIdx = str.indexOf("::");
            paramName = str.substring(0, pIdx);

            String xpath = str.substring(pIdx + 2);
            if (paramName.startsWith("file://")) {
                paramValue = XMLConfig.get(paramName.substring(7), xpath);
            }
            else {
                parser = XMLUtils.getParserInstance();
                DocumentBuilder db = parser.getDocumentBuilder(false, true, true);
                String xmlDoc = (String) inProperties.get(paramName);
                if ((xmlDoc == null) || ("".equals(xmlDoc))) {
                    xmlDoc = "<dummy/>";
                }
                Document doc = db.parse(new InputSource(new StringReader(xmlDoc)));
                paramValue = parser.get(doc, xpath);
            }

            return (paramValue != null ? paramValue : "");
        }
        catch (Exception exc) {
            System.out.println("Error handling 'xpath' metadata '" + paramName + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'xpath' metadata '" + str + "'", exc);
            }
            return "xpath" + PROP_START + str + PROP_END;
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    private String expandTimestamp(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, extra);
            }
            String pattern = str;
            int pIdx = str.indexOf("::");
            String tZone = DateUtils.getDefaultTimeZone().getID();
            if (pIdx != -1) {
                tZone = str.substring(pIdx + 2);
                pattern = str.substring(0, pIdx);
            }
            String paramValue = DateUtils.nowToString(pattern, tZone);
            if (paramValue == null) {
                throw new PropertiesHandlerException("Error handling 'timestamp' metadata '" + str
                        + "'. FInvalid format.");
            }
            return paramValue;
        }
        catch (Exception exc) {
            System.out.println("Error handling 'timestamp' metadata '" + str + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'timestamp' metadata '" + str + "'", exc);
            }
            return "timestamp" + PROP_START + str + PROP_END;
        }
    }

    private String expandDateFormat(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, extra);
            }
            List<String> parts = TextUtils.splitByStringSeparator(str, "::");
            String sourceTZone = DateUtils.getDefaultTimeZone().getID();
            String destTZone = sourceTZone;
            String date = parts.get(0);
            String sourcePattern = parts.get(1);
            String destPattern = parts.get(2);
            if (parts.size() > 3) {
                sourceTZone = parts.get(3);
                destTZone = parts.get(4);
            }
            String paramValue = DateUtils.convertString(date, sourcePattern, sourceTZone, destPattern, destTZone);
            if (paramValue == null) {
                throw new PropertiesHandlerException("Error handling 'dateformat' metadata '" + str
                        + "'. Invalid format.");
            }
            return paramValue;
        }
        catch (Exception exc) {
            System.out.println("Error handling 'dateformat' metadata '" + str + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'dateformat' metadata '" + str + "'", exc);
            }
            return "dateformat" + PROP_START + str + PROP_END;
        }
    }

    private String expandDateAdd(String str, Map<String, Object> inProperties, Object object, Object extra) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, extra);
            }
            String intType = "";
            List<String> parts = TextUtils.splitByStringSeparator(str, "::");
            String date = parts.get(0);
            String sourcePattern = parts.get(1);
            String type = parts.get(2);
            if ("s".equals(type)) {
            	intType = String.valueOf(Calendar.SECOND);
            }
            else if ("m".equals(type)) {
            	intType = String.valueOf(Calendar.MINUTE);
            }
            else if ("h".equals(type)) {
            	intType = String.valueOf(Calendar.HOUR_OF_DAY);
            }
            else if ("d".equals(type)) {
            	intType = String.valueOf(Calendar.DAY_OF_MONTH);
            }
            else if ("M".equals(type)) {
            	intType = String.valueOf(Calendar.MONTH);
            }
            else if ("y".equals(type)) {
            	intType = String.valueOf(Calendar.YEAR);
            }
            else {
            	throw new PropertiesHandlerException("Invalid value[" + type + "] for 'type'");
            }
            String value = parts.get(3);
            String paramValue = DateUtils.addTime(date, sourcePattern, intType, value);
            if (paramValue == null) {
                throw new PropertiesHandlerException("Error handling 'dateAdd' metadata '" + str
                        + "'. Invalid format.");
            }
            return paramValue;
        }
        catch (Exception exc) {
            System.out.println("Error handling 'dateAdd' metadata '" + str + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'dateAdd' metadata '" + str + "'", exc);
            }
            return "dateAdd" + PROP_START + str + PROP_END;
        }
    }

    private String expandDateFormatAdd(String str, Map<String, Object> inProperties, Object object, Object extra) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, extra);
            }
            String intType = "";
            List<String> parts = TextUtils.splitByStringSeparator(str, "::");
            String sourceTZone = DateUtils.getDefaultTimeZone().getID();
            String destTZone = sourceTZone;
            String date = parts.get(0);
            String sourcePattern = parts.get(1);
            String destPattern = parts.get(2);
            String type = parts.get(3);
            if ("s".equals(type)) {
            	intType = String.valueOf(Calendar.SECOND);
            }
            else if ("m".equals(type)) {
            	intType = String.valueOf(Calendar.MINUTE);
            }
            else if ("h".equals(type)) {
            	intType = String.valueOf(Calendar.HOUR_OF_DAY);
            }
            else if ("d".equals(type)) {
            	intType = String.valueOf(Calendar.DAY_OF_MONTH);
            }
            else if ("M".equals(type)) {
            	intType = String.valueOf(Calendar.MONTH);
            }
            else if ("y".equals(type)) {
            	intType = String.valueOf(Calendar.YEAR);
            }
            else {
            	throw new PropertiesHandlerException("Invalid value[" + type + "] for 'type'");
            }
            String value = parts.get(4);
            if (parts.size() > 5) {
                sourceTZone = parts.get(5);
                destTZone = parts.get(6);
            }
            String paramValue = DateUtils.convertAddTime(date, sourcePattern, sourceTZone, destPattern, destTZone, intType, value);
            if (paramValue == null) {
                throw new PropertiesHandlerException("Error handling 'dateformatAdd' metadata '" + str
                        + "'. Invalid format.");
            }
            return paramValue;
        }
        catch (Exception exc) {
            System.out.println("Error handling 'dateformatAdd' metadata '" + str + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'dateformatAdd' metadata '" + str + "'", exc);
            }
            return "dateformatAdd" + PROP_START + str + PROP_END;
        }
    }

    private String expandDecode(String str, Map<String, Object> inProperties, Object object, Object extra) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, extra);
            }
            String sep = "::";
            int sepLen = sep.length();
            int pIdx = str.indexOf(sep);
            String field = str.substring(0, pIdx);
            boolean match = false;
            int pIdx2 = str.indexOf(sep, pIdx + sepLen);
            String cond = null;
            String val = null;
            while (pIdx2 != -1) {
                cond = str.substring(pIdx + sepLen, pIdx2);
                pIdx = str.indexOf(sep, pIdx2 + sepLen);
                if (cond.equals(field)) {
                    val = str.substring(pIdx2 + sepLen, pIdx);
                    match = true;
                    break;
                }
                pIdx2 = str.indexOf(sep, pIdx + sepLen);
            }
            if (!match) {
                val = str.substring(pIdx + 2);
            }
            return val;
        }
        catch (Exception exc) {
            System.out.println("Error handling 'decode' metadata '" + str + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'decode' metadata '" + str + "'", exc);
            }
            return "decode" + PROP_START + str + PROP_END;
        }
    }

    private String expandDecodeL(String str, Map<String, Object> inProperties, Object obj,
            Object extra) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, obj, extra);
            }
            String sep = "::";
            int sepLen = sep.length();
            int pIdx = str.indexOf(sep);
            String separator = str.substring(0, pIdx);
            boolean match = false;
            int pIdx2 = str.indexOf(sep, pIdx + sepLen);
            String field = str.substring(pIdx + sepLen, pIdx2);
            pIdx = pIdx2;
            pIdx2 = str.indexOf(sep, pIdx2 + sepLen);
            String condL = null;
            String val = null;
            while (pIdx2 != -1) {
                condL = str.substring(pIdx + sepLen, pIdx2);
                pIdx = str.indexOf(sep, pIdx2 + sepLen);
                List<String> condLV = TextUtils.splitByStringSeparator(condL, separator);
                for (String cond : condLV) {
                    if (cond.equals(field)) {
                        val = str.substring(pIdx2 + sepLen, pIdx);
                        match = true;
                        break;
                    }
                }
                if (match) {
                    break;
                }
                pIdx2 = str.indexOf(sep, pIdx + sepLen);
            }
            if (!match) {
                val = str.substring(pIdx + 2);
            }
            return val;
        }
        catch (Exception exc) {
            System.out.println("Error handling 'decodeL' metadata '" + str + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'decodeL' metadata'" + str + "'", exc);
            }
            return "decodeL" + PROP_START + str + PROP_END;
        }
    }

    private String expandJavaScript(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        String lStr = str;
        String scopeName = "basic";
        String script = "";
        try {
            if (!PropertiesHandler.isExpanded(lStr)) {
                lStr = PropertiesHandler.expand(lStr, inProperties, object, extra);
            }
            int pIdx = lStr.indexOf("::");
            if (pIdx != -1) {
                scopeName = lStr.substring(0, pIdx);
                script = lStr.substring(pIdx + 2);
            }
            else {
                script = lStr;
            }
            
            return execScript("js", scopeName, script, inProperties, object, extra);
        }
        catch (Exception exc) {
            System.out.println("Error handling 'js' metadata '" + script + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'js' metadata '" + str + "'", exc);
            }
            return "js" + PROP_START + str + PROP_END;
        }
    }

    private String expandOGNL(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, extra);
            }

            return execScript("ognl", null, str, inProperties, object, extra);
        }
        catch (Exception exc) {
            System.out.println("Error handling 'ognl' metadata '" + str + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'ognl' metadata '" + str + "'", exc);
            }
            return "ognl" + PROP_START + str + PROP_END;
        }
    }
    
    private String expandScript(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        String lStr = str;
        String script = "";
        String lang = null;
        try {
            if (!PropertiesHandler.isExpanded(lStr)) {
                lStr = PropertiesHandler.expand(lStr, inProperties, object, extra);
            }
            String bcName = null;
            List<String> parts = TextUtils.splitByStringSeparator(lStr, "::");
            lang = parts.get(0);
            if (parts.size() > 2) {
                bcName = parts.get(1);
                script = parts.get(2);
            }
            else {
                script = parts.get(1);
            }
            
            return execScript(lang, bcName, script, inProperties, object, extra);
        }
        catch (Exception exc) {
            System.out.println("Error handling 'script[" + lang + "]' metadata '" + script + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'script' metadata '" + str + "'", exc);
            }
            return "script" + PROP_START + str + PROP_END;
        }
    }
    
    private String execScript(String lang, String  bcName, String script, Map<String, Object> inProperties,
            Object object, Object extra) throws Exception
    {
        Map<String, Object> bindings = new HashMap<String, Object>();
        bindings.put("inProperties", inProperties);
        bindings.put("object", object);
        bindings.put("extra", extra);

        Object obj = ScriptExecutor.execute(lang, script, bindings, bcName);
        String paramValue = (obj == null) ? "" : obj.toString();
        
        return paramValue;
    }

    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandEscJS(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        String string = str;
        if (!PropertiesHandler.isExpanded(string)) {
            string = PropertiesHandler.expand(string, inProperties, object, extra);
        }
        String escaped = TextUtils.replaceJSInvalidChars(string);
        return escaped;
    }

    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandEscSQL(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        String string = str;
        if (!PropertiesHandler.isExpanded(string)) {
            string = PropertiesHandler.expand(string, inProperties, object, extra);
        }
        String escaped = TextUtils.replaceSQLInvalidChars(string);
        return escaped;
    }

    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandEscXML(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        String string = str;
        if (!PropertiesHandler.isExpanded(string)) {
            string = PropertiesHandler.expand(string, inProperties, object, extra);
        }
        String escaped = XMLUtils.replaceXMLInvalidChars(string);
        return escaped;
    }

    private String expandReplace(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, extra);
            }
            int pIdx = str.indexOf("::");
            String string = str.substring(0, pIdx);
            int pIdx2 = str.indexOf("::", pIdx + 2);
            String search = str.substring(pIdx + 2, pIdx2);
            String subst = str.substring(pIdx2 + 2);
            String result = TextUtils.replaceSubstring(string, search, subst);
            return result;
        }
        catch (Exception exc) {
            System.out.println("Error handling 'replace' metadata '" + str + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'replace' metadata '" + str + "'", exc);
            }
            return "replace" + PROP_START + str + PROP_END;
        }
    }
    
    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandUrlEnc(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
    	try {
    		String string = str;
    		if (!PropertiesHandler.isExpanded(string)) {
    			string = PropertiesHandler.expand(string, inProperties, object, extra);
    		}
    		if (!PropertiesHandler.isExpanded(string)) {
    			return "urlEnc" + PROP_START + str + PROP_END;
    		}
    		return TextUtils.urlEncode(string);
    	}
        catch (Exception exc) {
            System.out.println("Error handling 'urlEnc' metadata '" + str + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'urlEnc' metadata '" + str + "'", exc);
            }
            return "urlEnc" + PROP_START + str + PROP_END;
        }
    }

    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandUrlDec(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
    	try {
    		String string = str;
    		if (!PropertiesHandler.isExpanded(string)) {
    			string = PropertiesHandler.expand(string, inProperties, object, extra);
    		}
    		if (!PropertiesHandler.isExpanded(string)) {
    			return "urlDec" + PROP_START + str + PROP_END;
    		}
    		return TextUtils.urlDecode(string);
    	}
        catch (Exception exc) {
            System.out.println("Error handling 'urlDec' metadata '" + str + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'urlDec' metadata '" + str + "'", exc);
            }
            return "urlDec" + PROP_START + str + PROP_END;
        }
    }

}
