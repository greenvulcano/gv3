package max.xml;

import it.greenvulcano.util.xml.XMLUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Write a DOM in a output stream using UTF-8 encoding.
 */
public class DOMWriter
{
    //--------------------------------------------------------------------------------------
    // FIELDS
    //--------------------------------------------------------------------------------------

    private boolean writeDoctype = true;
    private int preferredWidth = 90;

    //--------------------------------------------------------------------------------------
    // CONSTRUCTOR
    //--------------------------------------------------------------------------------------

    public DOMWriter()
    {
        writeDoctype = true;
    }

    //--------------------------------------------------------------------------------------
    // SETTER/GETTERS METHODS
    //--------------------------------------------------------------------------------------

    public boolean getWriteDoctype()
    {
        return writeDoctype;
    }

    public void setWriteDoctype(boolean wdt)
    {
        writeDoctype = wdt;
    }

    public int getPreferredWidth()
    {
        return preferredWidth;
    }

    public void setPreferredWidth(int v)
    {
        preferredWidth = v;
    }

    //--------------------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------------------

    public void write(NodeList nodeList, OutputStream out) throws IOException
    {
        write(nodeList, createPrintWriter(out));
    }

    public void write(Node node, OutputStream out) throws IOException
    {
        write(node, createPrintWriter(out), "");
    }

    //--------------------------------------------------------------------------------------
    // PROTECTED METHODS
    //--------------------------------------------------------------------------------------

    protected PrintWriter createPrintWriter(OutputStream stream) throws IOException
    {
        OutputStreamWriter osw = new OutputStreamWriter(stream, "UTF-8");
        return new PrintWriter(osw);
    }

    protected void write(NodeList nodeList, PrintWriter out) throws IOException
    {
        for(int i = 0; i < nodeList.getLength(); ++i) {
            write(nodeList.item(i), out, "");
        }
    }

    protected void write(Node node, PrintWriter out) throws IOException
    {
        write(node, out, "");
    }

    protected void write(Node node, PrintWriter out, String indent) throws IOException
    {
        // is there anything to do?
        if ( node == null ) {
            return;
        }

        boolean hasChildren = false;
        boolean isEmpty = false;
        node.normalize();
        int type = node.getNodeType();
        switch ( type ) {

        // print document
        case Node.DOCUMENT_NODE: {

                out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                out.println("");

                if(writeDoctype) {
                    Document doc = (Document)node;
                    DocumentType docType = doc.getDoctype();
                    if(docType != null) {
                        out.print("<!DOCTYPE ");
                        out.print(docType.getName());
                        String publicId = docType.getPublicId();
                        if(publicId != null) {
                            out.print(" PUBLIC \"");
                            out.print(publicId);
                            out.print("\" \"");
                            out.print(docType.getSystemId());
                            out.println("\">");
                        }
                        else {
                            out.print(" SYSTEM \"");
                            out.print(docType.getSystemId());
                            out.println("\">");
                        }
                    }
                }
                NodeList children = node.getChildNodes();
                for ( int iChild = 0; iChild < children.getLength(); iChild++ ) {
                    write(children.item(iChild), out);
                }
                out.flush();
                break;
            }

        // print element with attributes
        case Node.ELEMENT_NODE: {
                out.println("");
                out.print(indent);
                out.print('<');
                out.print(node.getNodeName());
                Attr attrs[] = sortAttributes(node.getAttributes());
                int sp = indent.length() + 1 + node.getNodeName().length();
                int cp = sp;
                String indentAtt = indent + " " + spaces(node.getNodeName().length());
                for ( int i = 0; i < attrs.length; i++ ) {
                    Attr attr = attrs[i];
                    StringBuffer bf = new StringBuffer();
                    bf.append(" ").append(attr.getNodeName()).append("=\"");
                    bf.append(normalize(attr.getNodeValue(), true));
                    bf.append("\"");
                    String attrStr = bf.toString();
                    int len = attrStr.length();
                    if(cp > sp) {
                        if(cp + len > preferredWidth) {
                            out.println("");
                            out.print(indentAtt);
                            cp = sp;
                        }
                    }
                    out.print(attrStr);
                    cp += len;
                }
                isEmpty = empty((Element)node);
                if(isEmpty) {
                    out.print("/>");
                }
                else {
                    out.print(">");
                }
                NodeList children = node.getChildNodes();
                if ( children != null ) {
                    int len = children.getLength();
                    for ( int i = 0; i < len; i++ ) {
                        hasChildren |= (children.item(i) instanceof Element);
                        hasChildren |= (children.item(i) instanceof Comment);
                        write(children.item(i), out, indent + "    ");
                    }
                }
                out.flush();
                break;
            }

        // print element with attributes
        case Node.DOCUMENT_FRAGMENT_NODE: {
                NodeList children = node.getChildNodes();
                if ( children != null ) {
                    int len = children.getLength();
                    for ( int i = 0; i < len; i++ ) {
                        write(children.item(i), out, indent);
                    }
                }
                break;
            }

        case Node.COMMENT_NODE: {
                out.println("");
                out.print(indent);
                out.print("<!--");
                out.print(node.getNodeValue());
                out.print("-->");
                break;
            }

        // handle entity reference nodes
        case Node.ENTITY_REFERENCE_NODE: {
                out.print('&');
                out.print(node.getNodeName());
                out.print(';');
                break;
            }

        // print cdata sections
        case Node.CDATA_SECTION_NODE :{
            String v = node.getNodeValue().trim();
            if (!v.equals("")) {
                if (XMLUtils.checkXMLInvalidChars(v)) {
                    out.print("<![CDATA[" + node.getNodeValue() + "]]>");
                }
                else {
                    out.print(node.getNodeValue());
                }
            }
            break;
        }

            // print text
        case Node.TEXT_NODE :{
            String v = node.getNodeValue().trim();
            if (!v.equals("")) {
                if (XMLUtils.checkXMLInvalidChars(v)) {
                    out.print("<![CDATA[" + node.getNodeValue() + "]]>");
                }
                else {
                    out.print(node.getNodeValue());
                }
            }
            break;
        }

        // print processing instruction
        case Node.PROCESSING_INSTRUCTION_NODE: {
                out.println("");
                out.print("<?");
                out.print(node.getNodeName());
                String data = node.getNodeValue();
                if ( data != null && data.length() > 0 ) {
                    out.print(' ');
                    out.print(data);
                }
                out.print("?>");
                break;
            }
        }

        if ( type == Node.ELEMENT_NODE) {
            if(hasChildren) {
                out.println("");
                out.print(indent);
            }
            if(!isEmpty) {
                out.print("</");
                out.print(node.getNodeName());
                out.print('>');
            }
        }

        out.flush();
    }

    protected boolean empty(Node node)
    {
        NodeList list = node.getChildNodes();
        int l = list.getLength();
        return l == 0;
        /*
        for(int i = 0; i < l; ++i) {
            Node ch = list.item(i);
            if(ch instanceof Element) return false;
            else if(ch instanceof Text) {
                if(!ch.getNodeValue().trim().equals("")) return false;
            }
        }
        return true;
        */
    }

    /** Returns a sorted list of attributes. */
    protected Attr[] sortAttributes(NamedNodeMap attrs) {

        int len = (attrs != null) ? attrs.getLength() : 0;
        Attr array[] = new Attr[len];
        for ( int i = 0; i < len; i++ ) {
            array[i] = (Attr)attrs.item(i);
        }
        for ( int i = 0; i < len - 1; i++ ) {
            String name  = array[i].getNodeName();
            int    index = i;
            for ( int j = i + 1; j < len; j++ ) {
                String curName = array[j].getNodeName();
                if ( curName.compareTo(name) < 0 ) {
                    name  = curName;
                    index = j;
                }
            }
            if ( index != i ) {
                Attr temp    = array[i];
                array[i]     = array[index];
                array[index] = temp;
            }
        }

        return(array);
    }

    /** Normalizes the given string. */
    protected String normalize(String s, boolean isAttribute)
    {
        StringBuffer str = new StringBuffer();

        int len = (s != null) ? s.length() : 0;
        for ( int i = 0; i < len; i++ ) {
            char ch = s.charAt(i);
            switch ( ch ) {
            case '<': {
                    str.append("&lt;");
                    break;
                }
            case '>': {
                    str.append("&gt;");
                    break;
                }
            case '&': {
                    str.append("&amp;");
                    break;
                }
            case '"': {
                    str.append("&quot;");
                    break;
                }
            case '\'': {
                    str.append("&apos;");
                    break;
                }
            case '\r':
            case '\n':
                if(isAttribute) {
                    str.append("&#");
                    str.append(Integer.toString(ch));
                    str.append(';');
                    break;
                }
            default: {
                    str.append(ch);
                }
            }
        }

        return(str.toString());

    }

    //--------------------------------------------------------------------------------------
    // HELPERS
    //--------------------------------------------------------------------------------------

    private static final String spcs = "                                                  ";

    public String spaces(int len)
    {
        StringBuffer ret = new StringBuffer();
        while(len > 0) {
            if(len > 50) {
                len -= 50;
                ret.append(spcs);
            }
            else {
                ret.append(spcs.substring(0, len));
                len = 0;
            }
        }
        return ret.toString();
    }

    //--------------------------------------------------------------------------------------
    // DEBUG
    //--------------------------------------------------------------------------------------

    public static void main(String args[]) throws Exception
    {
        String file = args[0];
        int pw = Integer.parseInt(args[1]);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        DOMWriter writer = new DOMWriter();
        writer.setPreferredWidth(pw);
        writer.write(doc, System.out);
    }
}
