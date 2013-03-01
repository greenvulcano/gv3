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
package it.greenvulcano.gvesb.gvconsole.workbench.servlet;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Portal class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class Portal
{

    private Node        configNode  = null;

    private PageContext pageContext = null;

    private JspWriter   out;

    public final String closeDiv    = "</div>";
    public final String table       = "<table aling='center' class=\"portal ui-widget-header ui-corner-all\" style=\"margin:20px auto;width:1000px;\">";
    public final String closeTable  = "</table>";
    public final String tr          = "<tr>";
    public final String closeTr     = "</tr>";
    public final String td          = "<td valign=\"top\">";
    public final String closeTd     = "</td>";
    public final String body        = "<body>";
    public final String closeBody   = "</body>";
    public final String closeHtml   = "</html>";

    /**
     * @param node
     * @param pageContext
     */
    public Portal(Node node, PageContext pageContext)
    {
        configNode = node;
        this.pageContext = pageContext;
        out = pageContext.getOut();
    }

    /**
     * Builds and writes the page
     *
     * @throws Throwable
     */
    public void writePage() throws Throwable
    {
        // head
        writeHeader("Workbench");
        // body
        out.println(body);
        // table
        out.println(table);
        // row
        out.println(tr);
        // columns
        String columnNodesXPath = "./Column";
        NodeList columnNodes = XMLConfig.getNodeList(configNode, columnNodesXPath);
        // sort column elements
        Vector<Node> columns = sort(columnNodes, "position");
        // for each column
        for (int z = 0; z < columns.size(); z++) {
            // column
            Element columnElem = (Element) columns.get(z);
            String width = columnElem.getAttribute("width");
            out.println("<td valign=\"top\" width=\"" + width + "\">");
            // corpo
            out.println(getDiv("corpo", ""));
            // section
            String sectionNodesXPath = "./Section";
            NodeList sectionNodes = XMLConfig.getNodeList(columnElem, sectionNodesXPath);
            Element sectionElem = null;
            // sort section elements
            Vector<?> sections = sort(sectionNodes, "vposition");
            // sections
            if (sections.size() > 0) {
                Iterator<?> it = sections.iterator();
                while (it.hasNext()) {
                    // get current section
                    sectionElem = (Element) it.next();
                    // write the current section
                    writeSection(sectionElem);
                }
            }
            // corpo
            out.println(closeDiv);
            // close td
            out.println(closeTd);
        }
        // close tr
        out.println(closeTr);
        // close table
        out.println(closeTable);
        // footer
        writeFooter("piedipagina");
    }

    /**
     * Prints the section element.
     *
     * @param sectionElem
     * @throws Throwable
     */
    public void writeSection(Element sectionElem) throws Throwable
    {

        String title = sectionElem.getAttribute("title");
        String headColor = sectionElem.getAttribute("headColor");
        String bodyColor = sectionElem.getAttribute("bodyColor");
        String borderColor = sectionElem.getAttribute("borderColor");
        String width = sectionElem.getAttribute("width");
        String position = sectionElem.getAttribute("position");

        String styleSection = "background: " + bodyColor + "; border-color: " + borderColor + "; width: " + width
                + "; float: " + position + ";";
        out.println(getDiv("sezione", styleSection));
        // write section header
        String styleHeader = "background: " + headColor + "; border-color: " + borderColor + ";";
        out.println(getDiv("head", styleHeader));
        out.println(getFont(title, "titlesmall"));
        out.println(closeDiv);
        // write the content
        writeContent(sectionElem);
        // close section
        out.println(closeDiv);
    }

    /**
     * Writes the content elements into section element
     *
     * @param sectionElem
     * @throws Throwable
     */
    public void writeContent(Element sectionElem) throws Throwable
    {

        String includeNodesXPath = "./*";
        NodeList includeNodes = XMLConfig.getNodeList(sectionElem, includeNodesXPath);
        Element includeElem = null;

        // per ogni sezione prendo i contenuti
        for (int j = 0; j < includeNodes.getLength(); j++) {

            includeElem = (Element) includeNodes.item(j);
            String color = includeElem.getAttribute("backgroundColor");
            String positionContent = includeElem.getAttribute("position");
            String src = includeElem.getAttribute("src");

            String style = "background: " + color + "; float: " + positionContent + ";";
            String type = includeElem.getTagName();
            // write the img tag
            if (type.equals("Image")) {
                out.println(getDiv("img", style));
                out.println("<img border=\"0\" src=\"" + src + "\"/>");
                // close div
                out.println(closeDiv);
            }
            else if (type.equals("Include")) {
                out.println(getDiv("sub-sezione", style));
                includeFile(src);
                // close div
                out.println(closeDiv);
            }
            else if (type.equals("Text")) {
                String value = XMLConfig.getNodeValue(includeElem);
                out.println(getDiv("sub-sezione", style));
                out.println(value);
                // close div
                out.println(closeDiv);
            }
            else if (type.equals("Url")) {
                String name = includeElem.getAttribute("name");
                out.println(getDiv("sub-sezione", style));
                out.println("<a href=\"" + src + "\">" + name + "</a>");
                // close div
                out.println(closeDiv);
            }

        }
    }

    /**
     * @param title
     * @throws Throwable
     */
    public void writeHeader(String title) throws Throwable
    {
        String headerNodesXPath = "./Header";
        Element headerElem = (Element) XMLConfig.getNode(configNode, headerNodesXPath);
        String src = headerElem.getAttribute("src");
        // start header
//        out.println("<html>\n<head>\n<title>" + title + "</title>");
//        out.println("  <meta http-equiv=\"content-type\" content=\"text/html; charset=iso-8859-1\">");
//        out.println(getDiv("testa", ""));
        // include file
        includeFile(src);
//        out.println(closeDiv);
//        // close header
//        out.println("</head>");
    }

    /**../..
     * @param style
     * @throws Throwable
     */
    public void writeFooter(String style) throws Throwable
    {
        String headerNodesXPath = "./Footer";
        Element headerElem = (Element) XMLConfig.getNode(configNode, headerNodesXPath);
        String src = headerElem.getAttribute("src");
        // include file
        includeFile(src);
        // close body
        out.println(closeBody);
        // close html
        out.println(closeHtml);
    }

    /**
     * @param src
     * @throws Throwable
     */
    public void includeFile(String src) throws Throwable
    {
        pageContext.include(src);
        out.flush();
    }

    /**
     * @param width
     * @return
     */
    public String getTD(String width)
    {
        String s = "<td width=\"" + width + "\" valign=\"top\">";
        return s;
    }

    /**
     * @param id
     * @param style
     * @return
     */
    public String getDiv(String id, String style)
    {
        String s = "<div id=\"" + id + "\" style=\"" + style + "\">";
        return s;
    }

    /**
     * @param text
     * @param style
     * @return
     */
    public String getFont(String text, String style)
    {
        String s = "<font class=\"" + style + "\">" + text + "</font>";
        return s;
    }

    /**
     * @param list
     * @param order
     * @return
     * @throws XMLConfigException
     */
    public Vector<Node> sort(NodeList list, String order) throws XMLConfigException
    {
        Comparer comparator = new Comparer(order);
        Vector<Node> v = new Vector<Node>();
        for (int i = 0; i < list.getLength(); i++) {
            v.add(list.item(i));
        }
        Collections.sort(v, comparator);
        return v;
    }

    // comparer class
    class Comparer implements Comparator<Object>
    {

        String order = null;

        public Comparer(String order)
        {
            this.order = order;
        }

        public int compare(Object elem1, Object elem2)
        {
            int i1 = Integer.parseInt(((Element) elem1).getAttribute(order));
            int i2 = Integer.parseInt(((Element) elem2).getAttribute(order));
            return Math.abs(i1) - Math.abs(i2);
        }
    }
}
