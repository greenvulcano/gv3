<%@ page import="org.w3c.dom.*, max.xml.*, java.util.*, javax.xml.transform.*"%>
<%@ page import="javax.xml.transform.stream.*, javax.xml.transform.dom.*, java.io.*"%>
<%
    XMLBuilder builder = XMLBuilder.getFromSession(session);
    if(builder == null) return;

    MaxXMLFactory factory = MaxXMLFactory.instance();
    Transformer transformer = null;
    ByteArrayOutputStream outxml = null;
    
    try {
        Document intfc = builder.getInterface();
        
		transformer = factory.getXMLEditorXSLT();
        outxml = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(intfc), new StreamResult(outxml));
    }
    catch(Exception exc) {
        exc.printStackTrace();

        // 20/11/2004: SDM
        //      Rilancia l'eccezione dopo la stampa dello stack.
        //      Tanto va in eccezione dopo, almeno cosi' si capisce il vero
        //      motivo del problema.
        //
        throw exc;
    }
    finally {
        factory.releaseXMLEditorXSLT(transformer);
    }
%>
<%= outxml %>
