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
package it.greenvulcano.gvesb.gvconsole.gvcon.xquery;

import java.io.OutputStream;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import max.search.SearchForm;
import net.sf.saxon.Configuration;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class XQueryProcessorCurrentDocument
{

    private XQueryBean   xQueryBean;

    private OutputStream xQueryResult;
    private Document     xQueryDocument;

    /**
     * @param xQueryBean
     */
    public XQueryProcessorCurrentDocument(XQueryBean xQueryBean)
    {
        this.xQueryBean = xQueryBean;
    }

    /**
     * @return the XQuery result
     */
    public OutputStream getXQueryResult()
    {
        return xQueryResult;
    }

    /**
     * @param node
     * @throws Exception
     */
    public void performXQueryProcessor(Node node) throws Exception
    {
        xQueryResult = null;
        xQueryBean.setError(null);
        xQueryDocument = node.getOwnerDocument();

        if (xQueryBean.getStartingNode().equals(SearchForm.START_ROOT)) {
            node = node.getOwnerDocument().getDocumentElement();
        }

        performXQuery(node);
    }

    /**
     * @return the XQuery document
     */
    public Document getXQueryDocument()
    {
        return xQueryDocument;
    }

    private void performXQuery(Node node) throws Exception
    {
        try {
            final Configuration config = new Configuration();
            final StaticQueryContext sqc = new StaticQueryContext(config);
            final XQueryExpression exp = sqc.compileQuery(xQueryBean.getText());
            final DynamicQueryContext dynamicContext = new DynamicQueryContext(config);
            dynamicContext.setContextItem(sqc.buildDocument(new DOMSource(node)));

            final Properties props = new Properties();
            props.setProperty(OutputKeys.INDENT, "yes");
            exp.run(dynamicContext, new StreamResult(xQueryResult), props);
        }
        catch (Exception exc) {
            exc.printStackTrace();
            xQueryBean.setError("" + exc);
            return;
        }
    }
}