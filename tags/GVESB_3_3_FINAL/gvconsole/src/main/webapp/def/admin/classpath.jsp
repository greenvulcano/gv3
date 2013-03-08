<%@ include file="../../head.jspf" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max"%>

<%!
	//------------------------------------------------------------------------------
	// UTILITIES

    String toHtml(String str) {
        StringTokenizer tkzr = new StringTokenizer(str, "<>\"", true);
        StringBuffer ret = new StringBuffer();
        while(tkzr.hasMoreTokens()) {
            String tk = tkzr.nextToken();
            if(tk.equals("<")) ret.append("&lt;");
            else if(tk.equals(">")) ret.append("&gt;");
            else if(tk.equals("\"")) ret.append("&quot;");
            else ret.append(tk);
        }
        return ret.toString();
    }

	String notNull(String str) {
    	if(str != null) {
        	return str;
    	}
    	else {
        	return "";
    	}
	}

	String findResource(String resource, ClassLoader classLoader) throws Exception
	{
		String urls = "";

		URL url = classLoader.getResource(resource);
		if(url == null) {
	    	urls = toHtml(resource + " not found");
		}
		else {
	    	urls = toHtml(url.toString());
		}
		urls = "<font color=\"#navy\">" + urls + "</font>";
		Enumeration en = classLoader.getResources(resource);
		while(en.hasMoreElements()) {
	    	urls += "<br/>" + toHtml(en.nextElement().toString());
		}

		return urls;
	}
%>
<%
	//------------------------------------------------------------------------------
	// RESOURCES

	String command = notNull(request.getParameter("command"));

	String urls = "";
	String resource = "";
	if(command.equals("Find Resource")) {
    	resource = notNull(request.getParameter("resource"));

        ClassLoader classClassLoader = getClass().getClassLoader();
        urls = "<b>From class's ClassLoader</b>:<br/><br/>";
    	urls += findResource(resource, classClassLoader);

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if((contextClassLoader != null) && (classClassLoader != contextClassLoader)) {
	        urls += "<br/><br/><b>From context ClassLoader</b>:<br/><br/>";
	        urls += findResource(resource, contextClassLoader);
        }
	}
%>
	<div class="titleSection"><h1>Resource URLs</h1></div>
	
	<table class="ui-widget-header ui-corner-all">
	  <tr valign="top">
		<td>
			<form method="post">
			    <table>
			        <tr>
			            <td>Resource:</td>
			            <td>
			            	<input type="text" class="input200" name="resource" value="<%= toHtml(resource) %>"/> 
			            	<input type="submit" value="Find Resource" name="command" class="button">
			            </td>
			        </tr>
			    </table>
			</form>
		</td>
		<td>
		</td>
		<td>
			<%= urls %>
		</td>
	  </tr>
	  <tr>
	  	<td>SAX Parser:
	  	<b><%= javax.xml.parsers.SAXParserFactory.newInstance().getClass() %></b>
	  	</td>
	  </tr>
	  <tr>
	  	<td>DOM Parser:
	  	<b><%= javax.xml.parsers.DocumentBuilderFactory.newInstance().getClass() %></b>
	  	</td>
	  </tr>
	  <tr>
	  	<td>Transformer:
	  	<b><%= javax.xml.transform.TransformerFactory.newInstance().getClass() %></b>
	  	</td>
	  </tr>
	</table>
	
<%@ include file="../../end.jspf" %>
