<%@ page import="max.xml.*" %>
<%@ page import="max.documents.*" %>

<%@ include file="../../head.jspf" %>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max"%>
<%

   String cmd = request.getParameter("cmd");
   if(cmd == null) cmd = "";

   else if(cmd.equals("discard")) {
      XMLBuilder builder = XMLBuilder.getFromSession(session);
      if(builder != null) {
          DiscardDocumentAction discardAction = (DiscardDocumentAction)builder.getMenuAction("discard");
          discardAction.doDiscard(request, response);
          return;
      }
   } else if(cmd.equals("cancel")) {
%>
      <jsp:forward page="/def/xmleditor/xmleditor.jsp" />
<%      
   }
%>

   <script>
        
        function discard()
        {
            $('input:hidden[name=cmd]').val("discard");
        }

        function cancel()
        {
        	$('input:hidden[name=cmd]').val("cancel");
        }

   </script>
	
	<div class="titleSection"><h1>Discard Document</h1></div>
   	<form id="DiscardForm" name="DiscardForm" action="<max:prop prop="max.site.root"/>/def/documents/discard.jsp">
		<input type="hidden" class="button" name="cmd" value="discard">
		
		<table class="ui-widget-header ui-corner-all" style="font-weight:normal;padding:15px">
			<tr>
			   <td>
			      Any changes will be lost.<br>
			      Do you want to continue the operation or return to the editing?<br>
			      <br>
			      <ul>
			      	<li><b>Continue</b> to discard the editing</li>
			      	<li><b>Cancel</b> to return to the editor</li>
			      </ul>
			   </td>
			</tr>
			<tr>
				<td><hr></td>
			</tr>
			<tr>
				<td>
				   <input name=btnDiscard type=submit class="button" value="  Continue  " onClick="discard()">
				   <input name=btnCancel type=submit class="button" value="   Cancel   " onClick="cancel()">
				</td>
			</tr>	
			
		</table>
   </form>

<%@ include file="../../end.jspf" %>
