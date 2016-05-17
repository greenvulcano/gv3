package max.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import max.core.MaxException;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

public class MaxDTDParser implements LexicalHandler, DeclHandler
{
    //--------------------------------------------------------------------------
    // FIELDS
    //--------------------------------------------------------------------------

    public static final String DUMMYELEMENT   = "__my_dummy__dummy___";

    protected DocumentModel    documentModel;
    protected StringBuffer     currentComment = new StringBuffer();
    protected XMLReader        parser         = null;
    protected Document         document;


    //--------------------------------------------------------------------------
    // CONSTRUCTORS
    //--------------------------------------------------------------------------

    public MaxDTDParser() throws SAXException
    {
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = parserFactory.newSAXParser();
            parser = saxParser.getXMLReader();
            parser.setProperty("http://xml.org/sax/properties/declaration-handler", this);
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", this);

            MaxXMLFactory xmlFact = MaxXMLFactory.instance();
            EntityResolver er = xmlFact.getEntityResolver();
            setEntityResolver(er);
        }
        catch (ParserConfigurationException exc) {
            throw new SAXException(exc);
        }
        catch (MaxException exc) {
            throw new SAXException("" + exc);
        }
    }

    //--------------------------------------------------------------------------
    // HELPERS
    //--------------------------------------------------------------------------

    protected String getComment()
    {
        String ret = currentComment.toString();
        currentComment.delete(0, currentComment.length());
        return ret;
    }


    //--------------------------------------------------------------------------
    // IMPLEMENTAZIONE INTERFACCIA LexicalHandler
    //--------------------------------------------------------------------------

    public void comment(char[] ch, int start, int length)
    {
        String cmt = new String(ch, start, length);
        if (currentComment.length() != 0) {
            currentComment.append("\r\n");
        }
        currentComment.append(cmt);
    }

    public void endCDATA()
    {
    }

    public void endDTD()
    {
    }

    public void endEntity(String name)
    {
    }

    public void startCDATA()
    {
    }

    public void startDTD(String name, String publicId, String systemId)
    {
        documentModel = new DocumentModel(publicId, systemId);
        currentComment = new StringBuffer();
    }

    public void startEntity(String name)
    {
    }

    //--------------------------------------------------------------------------
    // IMPLEMENTAZIONE INTERFACCIA DeclHandler
    //--------------------------------------------------------------------------

    public void attributeDecl(String elementName, String attributeName, String type, String defaultType,
            String defaultValue)
    {

        String enumString = null;
        if (type.startsWith("(")) {
            enumString = type;
            type = "";
        }

        if (defaultType == null)
            defaultType = "";
        if (defaultValue == null)
            defaultValue = "";

        AttributeModel attributeModel = new AttributeModel(elementName, attributeName, type,
                parseEnumString(enumString), defaultType, defaultValue);
        attributeModel.setComment(getComment());
        documentModel.addAttributeModel(elementName, attributeModel);
    }

    public void elementDecl(String name, String contentModel)
    {
        if (name.equals(DUMMYELEMENT))
            return;

        ElementModel elementModel = new ElementModel(name, parseContentModel(contentModel));
        elementModel.setComment(getComment());
        documentModel.addElementModel(elementModel);
    }

    public void externalEntityDecl(String name, String publicId, String systemId)
    {
    }

    public void internalEntityDecl(String name, String value)
    {
    }

    //--------------------------------------------------------------------------
    // CONTENT MODEL PARSING
    //--------------------------------------------------------------------------

    public ContentModel parseContentModel(String contentModel)
    {
        try {
            ContentModelParser cmParser = new ContentModelParser(contentModel);
            ContentModel cm = cmParser.parseContentModel();
            return cm;
        }
        catch (MaxException exc) {
            exc.printStackTrace();
            return new ContentModel();
        }
    }


    public String[] parseEnumString(String enumString)
    {
        if (enumString == null)
            return null;
        enumString = enumString.trim();
        enumString = enumString.substring(1, enumString.length() - 1);
        StringTokenizer stk = new StringTokenizer(enumString, "|", false);
        String ret[] = new String[stk.countTokens()];
        if (ret.length == 0)
            return null;
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = stk.nextToken().trim();
        }
        return ret;
    }


    //--------------------------------------------------------------------------
    // GETTERS
    //--------------------------------------------------------------------------

    public DocumentModel getDocumentModel()
    {
        return documentModel;
    }

    public Document getDocument()
    {
        return document;
    }


    //--------------------------------------------------------------------------
    // SETTERS
    //--------------------------------------------------------------------------

    public synchronized void setFeature(String feature, boolean val) throws SAXException
    {
        parser.setFeature(feature, val);
    }

    public synchronized void setEntityResolver(EntityResolver entityResolver)
    {
        parser.setEntityResolver(entityResolver);
    }

    //--------------------------------------------------------------------------
    // START PARSING
    //--------------------------------------------------------------------------

    public synchronized DocumentModel parseDTD(InputStream dtdStream) throws SAXException
    {
        File temp = null;
        try {
            temp = File.createTempFile("temp_", ".dtd");
            temp.deleteOnExit();
            FileOutputStream os = new FileOutputStream(temp);
            byte buf[] = new byte[2048];
            int l;
            while ((l = dtdStream.read(buf)) != -1) {
                os.write(buf, 0, l);
            }
            os.write('\n');
            os.close();
            dtdStream.close();
            DocumentModel ret = parseDTD(temp);

            return ret;
        }
        catch (IOException exc) {
            throw new SAXException(exc);
        }
        finally {
            temp.delete();
        }
    }


    public synchronized DocumentModel parseDTD(File file) throws SAXException
    {
        File temp = null;
        try {
            temp = File.createTempFile("temp_", ".xml");
            temp.deleteOnExit();
            FileOutputStream os = new FileOutputStream(temp);
            PrintStream out = new PrintStream(os);
            out.println("<?xml version=\"1.0\" standalone=\"no\"?>");

            String absolutePath = file.getAbsolutePath();
            String prefix = absolutePath.indexOf(':') == -1 ? "file://" : "";

            out.println("<!DOCTYPE " + DUMMYELEMENT + " SYSTEM \"" + prefix + absolutePath + "\" [");
            out.println("<!ELEMENT " + DUMMYELEMENT + " EMPTY>");
            out.println("]>");
            out.println("<" + DUMMYELEMENT + "></" + DUMMYELEMENT + ">");
            out.close();
            parser.parse(temp.getAbsolutePath());

            return documentModel;
        }
        catch (IOException exc) {
            throw new SAXException(exc);
        }
        finally {
            temp.delete();
        }
    }


    public synchronized void parse(String systemId) throws IOException, SAXException
    {
        try {
            parser.parse(systemId);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
            documentBuilder.setEntityResolver(parser.getEntityResolver());
            document = documentBuilder.parse(systemId);
        }
        catch (ParserConfigurationException exc) {
            throw new SAXException(exc);
        }
    }

    //--------------------------------------------------------------------------
    // COMMAND LINE
    //--------------------------------------------------------------------------

    public static void main(String args[]) throws Exception
    {
        MaxDTDParser parser = new MaxDTDParser();
        DocumentModel documentModel = parser.parseDTD(new File(args[0]));

        System.out.println(documentModel.toString());
    }
}
