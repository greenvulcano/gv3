<%@ page import="org.w3c.dom.*, max.xml.*, java.util.*, javax.xml.transform.*"%>
<%@ page import="javax.xml.transform.stream.*, javax.xml.transform.dom.*, java.io.*"%>
<%
    XMLBuilder builder = XMLBuilder.getFromSession(session);
    if(builder == null) return;

    MaxXMLFactory factory = MaxXMLFactory.instance();
    Transformer transformer = null;
    ByteArrayOutputStream outxml = null;

    try {
        Document intfc = builder.getWarningsInterface();

        transformer = factory.getXMLWarningsXSLT();
        outxml = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(intfc), new StreamResult(outxml));
    }
    catch(Exception exc) {
        exc.printStackTrace();
    }
    finally {
        factory.releaseXMLWarningsXSLT(transformer);
    }
%>
<%= outxml %>
