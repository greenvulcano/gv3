<%@ page import="max.xml.*" %>
<%@ page import="max.documents.*" %>

<%@ include file="../../head.jspf" %>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max"%>
<%
   String name = request.getParameter("name");
   
   String cmd = request.getParameter("cmd");
   if(cmd == null) cmd = "";

   else if(cmd.equals("warning")) {
%>
      <jsp:forward page="/documents/forceSelectDocument" />
<%  
   } else if(cmd.equals("cancel")) {
%>
      <jsp:forward page="/def/xmleditor/xmleditor.jsp" />
<%      
   }
%>

   <script>
        
        function warning()
        {
            WarningForm.cmd.value = "warning";
            WarningForm.submit();
        }

        function cancel()
        {
            WarningForm.cmd.value = "cancel";
            WarningForm.submit();
        }

    </script>

   <div class="titleSection"><h1>WARNING</h1></div>
   <form name="WarningForm">
		<input type=hidden name=cmd class="button" value=warning>
		<input type=hidden name=name class="button" value="<%= name %>" >
		<table class="ui-widget-header ui-corner-all" style="padding:15px">
			<tr>
				<td>
				    Another document is in editing mode.<br>
				    Any changes made on current document will be lost.<br> 
				</td>
			</tr>
			<tr>
				<td>
				  <br>
			      <li><b>Continue</b> to edit the new selected document and discard the current one
			      <li><b>Cancel</b> to return to editing the current document
				</td>
			</tr>
			<tr>
                           <td><hr></td>
			</tr>
			<tr>
				<td align=right>
				   <input name=btnWarning type=submit class="button" value="  Continue  " onClick="warning()">
				   <input name=btnCancel type=submit class="button" value="   Cancel   " onClick="cancel()">
				</td>
			</tr>	
			
		</table>
   </form>

<%@ include file="../../end.jspf" %>
