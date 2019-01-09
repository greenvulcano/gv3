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
	String factoryId = notNull(request.getParameter("factoryId"));
	DocumentFactory factory = DocumentFactory.instance();
	Set factoryIds = factory.getFactoryIdentifiers();
%>
	<h1>New configuration files</h1>
	
	<TABLE>
	  <TR class="search">
		<TD>
			<form method="post">
			    <TABLE class="search">
			        <TR class="search">
			            <TD>New Document:</TD>
			            <TD width="10"></TD>
			            <TD><select name="factoryId">
<%
							DocumentFactoryDescriptor descriptor = factory.getDocumentFactoryDescriptor(factoryId);
							if(descriptor != null) {
								String label = descriptor.getLabel();
%>
								<option value="<%= toHtml(factoryId) %>"><%= toHtml(label) %></option>
<%							    
							}
							for(Iterator it = factoryIds.iterator(); it.hasNext();) {
							    String id = (String)it.next();
							    descriptor = factory.getDocumentFactoryDescriptor(id);
							    if(descriptor != null) {
							    	String label = descriptor.getLabel();
%>
									<option value="<%= toHtml(id) %>"><%= toHtml(label) %></option>
<%							    
							    }
							}
%>
			            	</select>
			            </TD>
			        </TR>
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
    		factory.createAndRegisterDocument(factoryId);
%>
			<hr/>
			<b><%= toHtml(factory.getDocumentFactoryDescriptor(factoryId).getLabel()) %></b> - Created
<%
		}
%>
	
<%@ include file="../../end.jspf" %>
