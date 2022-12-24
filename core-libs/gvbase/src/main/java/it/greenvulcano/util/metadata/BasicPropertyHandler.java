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

import java.io.StringReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.expression.ognl.OGNLExpressionEvaluator;
import it.greenvulcano.js.initializer.JSInitManager;
import it.greenvulcano.js.util.JavaScriptHelper;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;

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
     * - ${{propname[::fallback]}}  : a System property value, 'fallback' (def empty string) if not found;
     * - sp{{propname[::fallback]}} : a System property value, 'fallback' (def empty string) if not found;
     * - env{{varname[::fallback]}} : an Environment variable value, 'fallback' (def empty string) if not found;
     * - @{{propname[::fallback]}}  : a inProperties property value, 'fallback' if not found;
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
     * - js{{[scope::]script}} : evaluate a JavaScript script, using the scope 'scope',
     *                           the inProperties map is added to the scope as 'inProperties',
     *                           the object is added to the scope as 'object',
     *                           the extra is added to the scope as 'extra'
     * - ognl{{script}} : evaluate a OGNL script,
     *                    the inProperties map is added to the context as 'inProperties',
     *                    the object is added to the context as 'object' (and is also the object on which execute the script),
     *                    the extra is added to the context as 'extra'
     * - escJS{{string}}    : escapes invalid JavaScript characters from 'string' (ex. ' -> \')
     * - escSQL{{string}}   : escapes invalid SQL characters from 'string' (ex. ' -> '')
     * - escXML{{string}}   : escapes invalid XML characters from 'string' (ex. ' -> &apos;)
     * - replace{{string::search::subst::r}}   : replace in 'string' all occurrences of 'search' with 'replace', if defined the last parameter r|R the search term can be a Java regular expression
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
     * @param scope
     *        the JavaScript scope
     * @param extra
     * @return the expanded string
     * @throws PropertiesHandlerException
     */
    @Override
    public String expand(String type, int boundary, String str, Map<String, Object> inProperties, Object object, Scriptable scope,
            Object extra) throws PropertiesHandlerException
    {
        if (type.startsWith("%")) {
            return expandClassProperties(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("$") || type.startsWith("sp")) {
            return expandSystemProperties(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("@")) {
            return expandInProperties(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("env")) {
            return expandEnvVariable(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("xpath")) {
            return expandXPathProperties(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("timestamp")) {
            return expandTimestamp(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("dateformatAdd")) {
            return expandDateFormatAdd(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("dateformat")) {
            return expandDateFormat(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("dateAdd")) {
            return expandDateAdd(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("decodeL")) {
            return expandDecodeL(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("decode")) {
            return expandDecode(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("js")) {
            return expandJavaScript(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("ognl")) {
            return expandOGNLProperties(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("escJS")) {
            return expandEscJS(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("escSQL")) {
            return expandEscSQL(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("escXML")) {
            return expandEscXML(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("replace")) {
            return expandReplace(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("urlEnc")) {
            return expandUrlEnc(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("urlDec")) {
            return expandUrlDec(str, inProperties, object, scope, extra, boundary);
        }
        else if (type.startsWith("xmlp")) {
            // DUMMY replacement - Must be handled by XMLConfig
            return "xmlp" + PROP_START + str + PROP_END;
        }
        return str;
    }

    @Override
    public void cleanupResources() {
        // do nothing
    }

    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandSystemProperties(String str, Map<String, Object> inProperties, Object object,
            Scriptable scope, Object extra, int boundary) throws PropertiesHandlerException
    {
        String propName = str;
        String fallback = "";
        if (!PropertiesHandler.isExpanded(propName)) {
            propName = PropertiesHandler.expand(propName, inProperties, object, scope, extra);
        }
        if (str.matches("^.+::.+$")) {
            String[] values = str.split("::");
            propName = values[0];
            fallback = values[1];
        } else {
            propName = str;
            fallback = "";
        }
        String paramValue = System.getProperty(propName, fallback);
        if (!PropertiesHandler.isExpanded(paramValue)) {
            paramValue = PropertiesHandler.expand(paramValue, inProperties, object, scope, extra);
        }
        return (paramValue != null ? paramValue : "");
    }

    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandEnvVariable(String str, Map<String, Object> inProperties, Object object,
            Scriptable scope, Object extra, int boundary) throws PropertiesHandlerException
    {
        String propName = str;
        String fallback = "";
        if (!PropertiesHandler.isExpanded(propName)) {
            propName = PropertiesHandler.expand(propName, inProperties, object, scope, extra);
        }
        if (str.matches("^.+::.+$")) {
            String[] values = str.split("::");
            propName = values[0];
            fallback = values[1];
        } else {
            propName = str;
            fallback = "";
        }
        String paramValue = System.getenv(propName);
        if (paramValue == null) {
            return PropertiesHandler.isExpanded(fallback) ? fallback : PropertiesHandler.expand(fallback, inProperties, object, scope, extra);
        }
        if (!PropertiesHandler.isExpanded(paramValue)) {
            paramValue = PropertiesHandler.expand(paramValue, inProperties, object, scope, extra);
        }
        return (paramValue != null ? paramValue : "");
    }

    /**
     * @param str
     *        the string to valorize
     * @param inProperties
     *        the hashTable containing the properties
     * @return the expanded string
     */
    private static String expandInProperties(String str, Map<String, Object> inProperties, Object object,
            Scriptable scope, Object extra, int boundary) throws PropertiesHandlerException
    {
        String propName = str;
        String fallback = null;
        if (!PropertiesHandler.isExpanded(propName)) {
            propName = PropertiesHandler.expand(propName, inProperties, object, scope, extra);
        }
        String paramValue = null;
        if (inProperties == null) {
            return "@" + PROPS_START[boundary] + str + PROPS_END[boundary];
        }
        if (str.matches("^.+::.+$")) {
            String[] values = str.split("::");
            propName = values[0];
            fallback = values[1];
        } else {
            propName = str;
            fallback = null;
        }
        paramValue = (String) inProperties.get(propName);
        if ((paramValue == null)) {// || (paramValue.equals(""))) {
            if (fallback != null) {
                return PropertiesHandler.isExpanded(fallback) ? fallback : PropertiesHandler.expand(fallback, inProperties, object, scope, extra);
            }
            return "@" + PROPS_START[boundary] + str + PROPS_END[boundary];
        }
        if (!PropertiesHandler.isExpanded(paramValue)) {
            paramValue = PropertiesHandler.expand(paramValue, inProperties, object, scope, extra);
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
            Scriptable scope, Object extra, int boundary) throws PropertiesHandlerException
    {
        String propName = str;
        String paramValue = "";
        if (object == null) {
            return "%" + PROPS_START[boundary] + str + PROPS_END[boundary];
        }
        if (!PropertiesHandler.isExpanded(propName)) {
            propName = PropertiesHandler.expand(propName, inProperties, object, scope, extra);
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
            return "%" + PROPS_START[boundary] + str + PROPS_END[boundary];
        }
        return paramValue;
    }


    private String expandXPathProperties(String str, Map<String, Object> inProperties, Object object, Scriptable scope,
            Object extra, int boundary) throws PropertiesHandlerException
    {
        XMLUtils parser = null;
        String paramName = null;
        String paramValue = null;
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, scope, extra);
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
            return "xpath" + PROPS_START[boundary] + str + PROPS_END[boundary];
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    private String expandTimestamp(String str, Map<String, Object> inProperties, Object object, Scriptable scope,
            Object extra, int boundary) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, scope, extra);
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
            return "timestamp" + PROPS_START[boundary] + str + PROPS_END[boundary];
        }
    }

    private String expandDateFormat(String str, Map<String, Object> inProperties, Object object, Scriptable scope,
            Object extra, int boundary) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, scope, extra);
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
            return "dateformat" + PROPS_START[boundary] + str + PROPS_END[boundary];
        }
    }

    private String expandDateAdd(String str, Map<String, Object> inProperties, Object object, Scriptable scope,
            Object extra, int boundary) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, scope, extra);
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
            return "dateAdd" + PROPS_START[boundary] + str + PROPS_END[boundary];
        }
    }

    private String expandDateFormatAdd(String str, Map<String, Object> inProperties, Object object, Scriptable scope,
            Object extra, int boundary) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, scope, extra);
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
            return "dateformatAdd" + PROPS_START[boundary] + str + PROPS_END[boundary];
        }
    }

    private String expandDecode(String str, Map<String, Object> inProperties, Object object, Scriptable scope,
            Object extra, int boundary) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, scope, extra);
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
            return "decode" + PROPS_START[boundary] + str + PROPS_END[boundary];
        }
    }

    private String expandDecodeL(String str, Map<String, Object> inProperties, Object obj, Scriptable scope,
            Object extra, int boundary) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, obj, scope, extra);
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
            return "decodeL" + PROPS_START[boundary] + str + PROPS_END[boundary];
        }
    }

    private String expandJavaScript(String str, Map<String, Object> inProperties, Object object, Scriptable scope,
            Object extra, int boundary) throws PropertiesHandlerException
    {
        Context cx = Context.enter();
        String lStr = str;
        String scopeName = "basic";
        String script = "";
        boolean intScope = false;
        try {
            if (!PropertiesHandler.isExpanded(lStr)) {
                lStr = PropertiesHandler.expand(lStr, inProperties, object, scope, extra);
            }
            int pIdx = lStr.indexOf("::");
            if (pIdx != -1) {
                scopeName = lStr.substring(0, pIdx);
                script = lStr.substring(pIdx + 2);
                intScope = true;
            }
            else {
                script = lStr;
            }
            if (intScope) {
                scope = JSInitManager.instance().getJSInit(scopeName).getScope();
            }
            if (scope == null) {
                throw new PropertiesHandlerException("Error handling 'js' metadata '" + str + "', Scope undefined.");
            }
            ScriptableObject.putProperty(scope, "inProperties", inProperties);
            ScriptableObject.putProperty(scope, "object", object);
            ScriptableObject.putProperty(scope, "extra", extra);
            Object result = JavaScriptHelper.executeScript(script, "expandJavaScript", scope, cx);
            String paramValue = JavaScriptHelper.resultToString(result);

            return paramValue;
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
            return "js" + PROPS_START[boundary] + str + PROPS_END[boundary];
        }
        finally {
            if (cx != null) {
                try {
                    Context.exit();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
    }

    private String expandOGNLProperties(String str, Map<String, Object> inProperties, Object object, Scriptable scope,
            Object extra, int boundary) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, scope, extra);
            }

            OGNLExpressionEvaluator ognl = new OGNLExpressionEvaluator();
            ognl.addToContext("inProperties", inProperties);
            ognl.addToContext("object", object);
            ognl.addToContext("extra", extra);
            Object obj = ognl.getValue(str, object);
            String result = (obj == null) ? "" : obj.toString();
            return result;
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
            return "ognl" + PROPS_START[boundary] + str + PROPS_END[boundary];
        }
    }

    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandEscJS(String str, Map<String, Object> inProperties, Object object, Scriptable scope,
            Object extra, int boundary) throws PropertiesHandlerException
    {
        String string = str;
        if (!PropertiesHandler.isExpanded(string)) {
            string = PropertiesHandler.expand(string, inProperties, object, scope, extra);
        }
        String escaped = TextUtils.replaceJSInvalidChars(string);
        return escaped;
    }

    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandEscSQL(String str, Map<String, Object> inProperties, Object object, Scriptable scope,
            Object extra, int boundary) throws PropertiesHandlerException
    {
        String string = str;
        if (!PropertiesHandler.isExpanded(string)) {
            string = PropertiesHandler.expand(string, inProperties, object, scope, extra);
        }
        String escaped = TextUtils.replaceSQLInvalidChars(string);
        return escaped;
    }

    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandEscXML(String str, Map<String, Object> inProperties, Object object, Scriptable scope,
            Object extra, int boundary) throws PropertiesHandlerException
    {
        String string = str;
        if (!PropertiesHandler.isExpanded(string)) {
            string = PropertiesHandler.expand(string, inProperties, object, scope, extra);
        }
        String escaped = XMLUtils.replaceXMLInvalidChars(string);
        return escaped;
    }

    private String expandReplace(String str, Map<String, Object> inProperties, Object object, Scriptable scope,
            Object extra, int boundary) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, scope, extra);
            }
            String params[] = str.split("::");
            String string = params[0];
            String search = params[1];
            String subst = params[2];
            boolean useRX = false;
            if (params.length == 4) {
                useRX = "r".equalsIgnoreCase(params[3]);
            }
            String result = "";
            if (useRX) {
                result = string.replaceAll(search, subst);
            }
            else {
                result = TextUtils.replaceSubstring(string, search, subst);
            }
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
            return "replace" + PROPS_START[boundary] + str + PROPS_END[boundary];
        }
    }

    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandUrlEnc(String str, Map<String, Object> inProperties, Object object, Scriptable scope,
            Object extra, int boundary) throws PropertiesHandlerException
    {
        try {
            String string = str;
            if (!PropertiesHandler.isExpanded(string)) {
                string = PropertiesHandler.expand(string, inProperties, object, scope, extra);
            }
            if (!PropertiesHandler.isExpanded(string)) {
                return "urlEnc" + PROPS_START[boundary] + str + PROPS_END[boundary];
            }
            String encoded = URLEncoder.encode(string, "UTF-8");
            return encoded;
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
            return "urlEnc" + PROPS_START[boundary] + str + PROPS_END[boundary];
        }
    }

    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandUrlDec(String str, Map<String, Object> inProperties, Object object, Scriptable scope,
            Object extra, int boundary) throws PropertiesHandlerException
    {
        try {
            String string = str;
            if (!PropertiesHandler.isExpanded(string)) {
                string = PropertiesHandler.expand(string, inProperties, object, scope, extra);
            }
            if (!PropertiesHandler.isExpanded(string)) {
                return "urlDec" + PROPS_START[boundary] + str + PROPS_END[boundary];
            }
            String decoded = URLDecoder.decode(string, "UTF-8");
            return decoded;
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
            return "urlDec" + PROPS_START[boundary] + str + PROPS_END[boundary];
        }
    }

}
