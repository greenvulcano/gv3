<%@ include file="../../head.jspf" %>
<%@ page import="java.util.*" %>
<%@ page import="max.documents.factory.*" %>
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
%>
<%
	//------------------------------------------------------------------------------
	// CONTENT MIGRATION

	String command = notNull(request.getParameter("command"));
%>
	<h1>Clean session</h1>
	
	<TABLE>
	  <TR class="search">
		<TD>
			<form method="post">
			    <TABLE class="search">
			        <TR class="search">
			        	<td colspan="2">&nbsp;</td>
			            <TD><input type="submit" class="button" name="command" value=" Go "/></TD>
			        </TR>
			    </TABLE>
			</form>
		</TD>
	  </TR>
	</TABLE>
	<br/>
<%	
		if(command.equals(" Go ")) {
		    session.invalidate();
%>
			<hr/>
			<b>Session cleaned</b>
<%
		}
%>
	
<%@ include file="../../end.jspf" %>
