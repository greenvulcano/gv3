<%@ page import="max.xml.*"%>
<%@ page import="max.documents.*"%>

<%@ include file="../../head.jspf"%>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max"%>
<%
    String cmd = request.getParameter("cmd");
			if (cmd == null)
				cmd = "";
			if (cmd.equals("save")) {
				XMLBuilder builder = XMLBuilder.getFromSession(session);
				if (builder != null) {
					SaveDocumentAction saveAction = (SaveDocumentAction) builder
							.getMenuAction("save");
					saveAction.doSave(builder, request, response);
					return;
				}
			} else if (cmd.equals("cancel")) {
%>
<jsp:forward page="/def/xmleditor/xmleditor.jsp" />
<%
    return;
			}
%>

<script>
        
        function save()
        {
            SaveForm.cmd.value = "save";
        }

        function cancel()
        {
            SaveForm.cmd.value = "cancel";
        }

    </script>

<div class="titleSection">
<h1>Save Document</h1>
Use this tool to save any modifications done to the document.</div>

<form name="SaveForm"
	action="<max:prop prop="max.site.root"/>/def/documents/save.jsp">
<input type=hidden class="button" name=cmd value=save> <input
	type=hidden class="button" name=key value="...">
<table class="ui-widget-header ui-corner-all" style="margin-top:10px;">
	<tr>
		<td nowrap>Notes</td>
		<td></td>
		<td><max:popup>
				    This field described the motivations of the modifications.
				    </max:popup></td>
		<td></td>
		<td><textarea cols=45 rows=10 name=notes></textarea></td>
	</tr>
	<tr>
		<td colspan="5">
		<hr>
		</td>
	</tr>
	<tr>
		<td colspan="5"><input name=btnSave type=submit class="button"
			value="   Save   " onClick="save()"> <input name=btnCancel
			type=submit class="button" value="   Cancel   " onClick="cancel()">
		</td>
	</tr>

</table>
</form>

<%@ include file="../../end.jspf"%>
