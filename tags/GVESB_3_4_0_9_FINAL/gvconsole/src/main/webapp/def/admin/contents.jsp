<%@ include file="../../head.jspf" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="javax.servlet.jsp.*" %>
<%@ page import="max.def.*" %>
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

	void migrate(String source, String destination, JspWriter writer) throws Exception
	{
    	MigrateContents migrateContents = new MigrateContents(source, destination, new PrintWriter(writer));
    	migrateContents.setOutputHtml(true);
    	migrateContents.migrate();
	}
%>
<%
	//------------------------------------------------------------------------------
	// CONTENT MIGRATION

	String command = notNull(request.getParameter("command"));

	String source = notNull(request.getParameter("source"));
	String destination = notNull(request.getParameter("destination"));
%>
	<div class="titleSection"><h1>Contents migration</h1></div>
	
	<table>
	  <tr class="search">
		<td>
			<form method="post">
			    <table class="ui-widget-header ui-corner-all">
			        <tr class="search">
			            <td>Source content provider:</td>
			            <td width="10"></td>
			            <td><input type="text" class="input200" name="source" value="<%= toHtml(source) %>"/></td>
			        </tr>
			        <tr class="search">
			            <td>Destination content provider:</td>
			            <td width="10"></td>
			            <td><input type="text" class="input200" name="destination" size="60" value="<%= toHtml(destination) %>"/></td>
			        </tr>
			        <tr class="search">
			            <td colspan="3"><input type="submit" class="button" name="command" value=" Go "/></td>
			        </tr>
			    </table>
			</form>
		</td>
	  </tr>
	</table>
	<br/>
	<pre><%	
		if(command.equals(" Go ")) {
    		migrate(source, destination, out);
		}
	%></pre>
	
<%@ include file="../../end.jspf" %>
