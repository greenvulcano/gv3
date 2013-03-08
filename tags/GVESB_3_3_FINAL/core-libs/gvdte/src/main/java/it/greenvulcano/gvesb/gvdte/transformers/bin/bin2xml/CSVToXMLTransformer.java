/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.gvdte.transformers.bin.bin2xml;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvdte.config.DataSourceFactory;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransfException;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransformer;
import it.greenvulcano.gvesb.gvdte.util.TransformerHelper;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class handle data transformations from generic CSV to XML.
 * 
 * @version 3.2.0 29/09/2011
 * @author GreenVulcano Developer Team
 */
public class CSVToXMLTransformer implements DTETransformer
{
    private static Logger           logger                      = GVLogger.getLogger(CSVToXMLTransformer.class);

    private static final String     DEFAULT_TARGET_TAG_ROOT     = "RowSet";
    private static final String     DEFAULT_TARGET_TAG_GROUP    = "data";
    private static final String     DEFAULT_TARGET_TAG_RECORD   = "row";
    private static final String     DEFAULT_TARGET_TAG_FIELD    = "col";
    private static final String     DEFAULT_ATT_KEY_NAME_PREFIX = "key_";

    private String                  fieldsSeparator	            = ",";
    private String                  fieldDelimiter              = "";
    private boolean                 useCDATA                    = false;
    private boolean                 excludeFirstRow             = false;
    private String                  tagRoot                     = DEFAULT_TARGET_TAG_ROOT;
    private String                  tagGroup                    = DEFAULT_TARGET_TAG_GROUP;
    private String                  tagRecord                   = DEFAULT_TARGET_TAG_RECORD;
    private String                  tagField                    = DEFAULT_TARGET_TAG_FIELD;
    private String                  tagKey                      = DEFAULT_ATT_KEY_NAME_PREFIX;
    private List<Integer>           groupByIndexes              = new ArrayList<Integer>();
    private Set<Integer>            groupIndexes                = new HashSet<Integer>();
    private boolean                 withGroups                  = false;
    private Comparator<String>      groupComparator             = null;

    private List<TransformerHelper> helpers                     = new ArrayList<TransformerHelper>();

    public CSVToXMLTransformer()
    {
        // do nothing
    }

    /**
     * Initialize the instance.
     * 
     * @param node
     * @param dsf
     * @throws DTETransfException
     *         if configuration parameters or conversion properties can't be
     *         accessed for any reason.
     */
    @Override
    public void init(Node node, DataSourceFactory dsf) throws DTETransfException
    {
        logger.debug("Init start");
        String tagsStr = "";
        try {
            fieldsSeparator = XMLConfig.get(node, "@FieldsSeparator", ",");
            fieldDelimiter = XMLConfig.get(node, "@FieldDelimiter", "");
            useCDATA = XMLConfig.getBoolean(node, "@UseCDATA", false);
            excludeFirstRow = XMLConfig.getBoolean(node, "@ExcludeFirstRow", false);
            tagsStr = XMLConfig.get(node, "@Tags", "");
            String groupByStr = XMLConfig.get(node, "@GroupBy", "");
            if (groupByStr.length() > 0) {
                String[] groupBy = groupByStr.split(","); // 1 based
                for (String index : groupBy) {
                    groupByIndexes.add(Integer.parseInt(index, 10) - 1);
                }
            }

            logger.debug("Loaded parameters: FieldSeparator = [" + fieldsSeparator + "] - FieldDelimiter = ["
                    + fieldDelimiter + "] - UseCDATA = [" + useCDATA + "] - ExcludeFirstRow = [" + excludeFirstRow
                    + "] - Tags = [" + tagsStr + "] - GroupBy = [" + groupByStr + "]");

            logger.debug("Init stop");
        }
        /*catch (XMLConfigException exc) {
            logger.error("Error while accessing configuration", exc);
            throw new DTETransfException("GVDTE_XML_CONFIG_ERROR", exc);
        }*/
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new DTETransfException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", " Unexpected error."}}, exc);
        }

        if (tagsStr.length() > 0) {
            String[] tags = tagsStr.split(",");
            if ((tags.length > 0)) {
                tagRoot = tags[0];
            }
            if (tags.length > 1) {
                tagGroup = tags[1];
            }
            if (tags.length > 2) {
                tagRecord = tags[2];
            }
            if (tags.length > 3) {
                tagField = tags[3];
            }
            if (tags.length > 4) {
                tagKey = tags[4];
            }
        }

        withGroups = (groupByIndexes.size() > 0);
        if (withGroups) {
            groupComparator = new RowComparatorByFields(groupByIndexes, fieldsSeparator);
        }
        groupIndexes.addAll(groupByIndexes);
    }

    /**
     * The <code>input</code> parameter is a byte array or String. The return
     * value is a Document.
     * 
     * @param input
     *        the input data of the transformation (a byte array or String).
     * @param buffer
     *        the intermediate result of the transformation (if needed).
     * @param mapParam
     * @return a Document representing the result of the data transformation.
     * @throws DTETransfException
     *         if any transformation error occurs.
     */
    @Override
    public Object transform(Object input, Object buffer, Map<String, Object> mapParam) throws DTETransfException
    {
        logger.debug("Transform start");
        try {
            String inputStr = null;
            if (input instanceof byte[]) {
                inputStr = new String((byte[]) input);
            }
            else if (input instanceof String) {
                inputStr = (String) input;
            }
            else {
                throw new ClassCastException("Input object is not a binary buffer or String: " + input.getClass());
            }

            // remove \r char as row terminator
            inputStr = inputStr.replaceAll("\r", "");

            int fieldDelimiterLen = fieldDelimiter.length();
            XMLUtils parser = null;
            try {
                parser = XMLUtils.getParserInstance();
                Document doc = parser.newDocument(tagRoot);
                Element root = doc.getDocumentElement();

                List<String> rowsStr = TextUtils.splitByStringSeparator(inputStr, "\n");
                int idxLen = groupByIndexes.size();
                if (withGroups) {
                    Collections.sort(rowsStr, groupComparator);
                }

                Element groupEl = null;
                List<String> prevGroup = null;
                int l = rowsStr.size(), i = (excludeFirstRow ? 1 : 0);
                for (; i < l; i++) {
                    String rowStr = rowsStr.get(i);
                    if (rowStr.length() > 1) {
                        List<String> colsStr = TextUtils.splitByStringSeparator(rowStr, fieldsSeparator);

                        if (withGroups) {
                            List<String> curGroup = new ArrayList<String>();
                            for (int g = 0; g < idxLen; g++) {
                                String val = colsStr.get(groupByIndexes.get(g));
                                if ((fieldDelimiterLen > 0) && val.startsWith(fieldDelimiter)
                                        && val.endsWith(fieldDelimiter)) {
                                    val = val.substring(fieldDelimiterLen, val.length() - fieldDelimiterLen);
                                }
                                curGroup.add(val);
                            }

                            if ((prevGroup == null) || !prevGroup.equals(curGroup)) {
                                prevGroup = curGroup;
                                groupEl = parser.insertElement(root, tagGroup);
                                for (int g = 0; g < idxLen; g++) {
                                    parser.setAttribute(groupEl, tagKey + (groupByIndexes.get(g) + 1), curGroup.get(g));
                                }
                            }
                        }

                        Element rowsEl = (groupEl == null ? root : groupEl);
                        Element recordEl = parser.insertElement(rowsEl, tagRecord);

                        int ll = colsStr.size(), ii = 0;
                        for (; ii < ll; ii++) {
                            if (withGroups && groupIndexes.contains(Integer.valueOf(ii))) {
                                continue;
                            }
                            String col = colsStr.get(ii);
                            if ((fieldDelimiterLen > 0) && col.startsWith(fieldDelimiter)
                                    && col.endsWith(fieldDelimiter)) {
                                col = col.substring(fieldDelimiterLen, col.length() - fieldDelimiterLen);
                            }
                            Element fieldEl = parser.insertElement(recordEl, tagField);
                            if (useCDATA) {
                                parser.insertCDATA(fieldEl, col);
                            }
                            else {
                                parser.insertText(fieldEl, col);
                            }
                        }
                    }
                }
                logger.debug("Transform stop");
                return doc;
            }
            finally {
                XMLUtils.releaseParserInstance(parser);
            }
        }
        catch (ClassCastException exc) {
            logger.error("Input object is not a binary buffer or String", exc);
            throw new DTETransfException("GVDTE_CAST_ERROR", exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new DTETransfException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", " Unexpected error."}}, exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#getMapName()
     */
    @Override
    public String getMapName()
    {
        return "";
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#setValidate(java.lang.String)
     */
    @Override
    public void setValidate(String validate)
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#validate()
     */
    @Override
    public boolean validate()
    {
        // do nothing
        return false;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#clean()
     */
    @Override
    public void clean()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#destroy()
     */
    @Override
    public void destroy()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#getHelpers()
     */
    @Override
    public List<TransformerHelper> getHelpers()
    {
        return helpers;
    }

    public static class RowComparatorByFields implements Comparator<String>
    {
        private List<Integer> fieldIndexes;
        private String        fieldsSeparator;

        public RowComparatorByFields(List<Integer> fieldIndexes, String fieldsSeparator)
        {
            this.fieldIndexes = fieldIndexes;
            this.fieldsSeparator = fieldsSeparator;
        }

        @Override
        public int compare(String row1, String row2)
        {
            List<String> cols1 = Arrays.asList(row1.split(fieldsSeparator));
            List<String> cols2 = Arrays.asList(row2.split(fieldsSeparator));
            for (int i = 0, l = fieldIndexes.size(); i < l; i++) {
                int index = fieldIndexes.get(i).intValue();
                int c = cols1.get(index).compareTo(cols2.get(index));
                if (c != 0) {
                    return c;
                }
            }
            return 0;
        }
    }
}
