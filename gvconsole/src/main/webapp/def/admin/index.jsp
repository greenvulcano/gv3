<%
  session.removeAttribute("currentMenu");
  session.setAttribute("currentMenu", "utility");
%>
<%@ include file="../../head.jspf" %>
<%
    String cmd = request.getParameter("cmd");
    if(cmd == null) cmd = "";

    if(cmd.equals("logoff")) {
        session.invalidate();
    }
%>

<div class="titleSection"><h1>GVConsole tools</h1></div><p></p>
    <TABLE  class="ui-widget-header ui-corner-all">
         <TR valign="baseline">
            <TD>
				<ul>
	                <li><a href="properties.jsp">Java properties</a> - View the Java properties of the JVM running GVConsole.</li>
	                <li><a href="locks.jsp">Locks file management</a> - Manages the lock files.</li>
					<li><a href="<%=contextRoot%>/config/index.jsp"><span>Reload Configuration</span></a></li>
					<li><a href="<%=contextRoot%>/deploy/confExport.jsp"><span>Export Services Configuration</span></a></li>
            	</ul>
            </TD>
         </TR>
    </TABLE>
<br><br><br><br><br><br><br><br>        
<%@ include file="../../end.jspf" %>
