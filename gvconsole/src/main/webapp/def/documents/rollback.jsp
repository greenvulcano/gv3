<%@ page import="max.xml.*" %>
<%@ page import="max.documents.*" %>

<%@ include file="../../head.jspf" %>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max"%>
<%
   String name = request.getParameter("name");
   String label = request.getParameter("label");
   String version = request.getParameter("version");
   String author = request.getParameter("author");
   String date = request.getParameter("date");
   
   String cmd = request.getParameter("cmd");
   if(cmd == null) cmd = "";

   else if(cmd.equals("rollback")) {
%>
      <jsp:forward page="/documents/forceRollback" />
<% 
   }
   else if(cmd.equals("cancel")) {
%>
      <jsp:forward page="/def/documents/document_history.jsp" />
<%      
   }
%>

   <script>
        
        function rollback()
        {
            var version = RollForm.version.value;
            RollForm.cmd.value = "rollback";
            RollForm.notes.value = "Ripristino della ver. " + version + "\n" + RollForm.notes.value;
        }

        function cancel()
        {
            RollForm.cmd.value = "cancel";
        }

    </script>

   <div class="titleSection"><h1>Restore Document</h1>
   Use this tool to restore the document version.</div>
   
   <form name="RollForm">
		<input type=hidden class="button" name=cmd value="rollback" >
		<input type=hidden class="button" name=name value="<%= name %>">
		<input type=hidden class="button" name=version value="<%= version %>">
		<table class="ui-widget-header ui-corner-all" style="margin-top:10px;">
	                <tr>
	                  <br>
	                </tr>
	                <tr>
				<td><b>Documento:</b></td>
				<td colspan=2><%= label %></td>
			</tr>
			<tr>
				<td><b>Versione:</b></td>
				<td colspan=2><%= version %></td>
			</tr>
			<tr>
				<td><b>Autore:</b></td>
				<td colspan=2><%= author %></td>
			</tr>
			<tr>
				<td><b>Data:</b></td>
				<td colspan=2><%= date %></td>
			</tr>
	        </table>
		<table class="ui-widget-header ui-corner-all">
			<tr>
				<td colspan="2" nowrap>Notes</td>
				<td></td>
				<td colspan="2">
				    <max:popup>
				    This field described the motivations of the modifications.
				    </max:popup>
				</td>    
				<td></td>
				<td><textarea cols=47 rows=10 name="notes"></textarea></td>
			</tr>
			<tr>
                                 <td colspan="7"><hr></td>
			</tr>
			<tr>
				<td colspan=7 align=right>
				   <input name=btnRollback type=submit class="button" value="   Restore   " onClick="rollback()">
				   <input name=btnCancel type=submit class="button" value="   Cancel   " onClick="cancel()">
				</td>
			</tr>	
			
		</table>
   </form>

<%@ include file="../../end.jspf" %>