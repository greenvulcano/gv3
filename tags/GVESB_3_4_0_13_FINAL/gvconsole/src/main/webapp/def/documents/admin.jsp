<%@ page import="max.xml.*"%>
<%@ page import="max.documents.*"%>

<%@ include file="../../head.jspf"%>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max"%>
<%
    DocumentRepository dr = null;
			String[] documentList = new String[0];
			dr = DocumentRepository.instance();
			documentList = dr.getDocumentNames();
			Arrays.sort(documentList);
%>
<script>

        function removeView()
        {
            var docName = ViewForm.name.value;
            if(confirm("Are you sure to remove the view for the document '" + docName + "'")) {
                ViewForm.action = "<max:prop prop="max.site.root"/>/documents/removeView";
                ViewForm.submit();
            }
        }

        function insertView()
        {
            var docName = ViewForm.name.value;
            if(confirm("Are you sure to insert a view for the document '" + docName + "'")) {
                ViewForm.action = "<max:prop prop="max.site.root"/>/documents/insertView";
                ViewForm.submit();
            }
        }

        function removeFilterForDoc()
        {
            var docName = FilterForm.name.value;
            if(confirm("Are you sure to remove the filter for the document '" + docName + "'")) {
                FilterForm.action = "<max:prop prop="max.site.root"/>/documents/removeFilter";
                FilterForm.submit();
            }
        }

        function insertFilter()
        {
            var docName = FilterForm.name.value;
            if(confirm("Are you sure to insert a filter for the document '" + docName + "'")) {
                FilterForm.action = "<max:prop prop="max.site.root"/>/documents/insertFilter";
                FilterForm.submit();
            }
        }

        function cancel()
        {
            document.location = "<max:prop prop="max.site.root"/>/def/xmleditor";
        }

    </script>

<table>
	<tr>
		<td class=right><%@ include file="/def/xmleditor/xmlmenu.jsp"%></td>
	</tr>
</table>
<table class="ui-widget-header ui-corner-all" cellpadding="4"
	style="margin-bottom: 10px">
	<tbody>
		<tr>
			<td><b id=title>SET/REMOVE A VIEW FOR A DOCUMENT</b></td>
		</tr>

		<tr>
			<td nowrap>View</td>
			<td></td>
			<td><max:popup>
				    This field shows the path of the view to be associate to the 'document'.
				    </max:popup></td>
			<td><input type=file class="button" name="view"></td>
		</tr>
		<tr>
			<td colspan=5><input type="button" class="button"
				value="   Insert   " onclick="insertView()"> <input
				type="button" class="button" value="   Remove   "
				onclick="removeView()"> <input type="button" class="button"
				value="   Cancel   " onclick="cancel()"></td>
		</tr>
	</tbody>
</table>
<form name="FilterForm" method="post" enctype="multipart/form-data">
<table class="ui-widget-header ui-corner-all" cellpadding="4"
	style="margin-bottom: 10px">
	<tbody>
		<tr>
			<td><b id=title>SET/REMOVE A FILTER FOR A DOCUMENT</b></td>
		</tr>
		<tr>
			<td nowrap>Document</td>
			<td></td>
			<td><max:popup>
				    This choice shows the list of the 'document' contained by the Repository.
				    </max:popup></td>
			<td><SELECT id=select1 name="name">
				<%
				    for (int i = 0; i < documentList.length; i++) {
				%>
				<OPTION
					value="<%=(dr.getDocumentDescriptor(documentList[i]))
								.getName()%>"><%=(dr.getDocumentDescriptor(documentList[i]))
						.getLabel()%></OPTION>
				<%
				    }
				%>
			</SELECT></td>
		</tr>
		<tr>
			<td nowrap>Filter</td>
			<td></td>
			<td><max:popup>
				    This field shows the path of the view to be associate to the 'document'.
				    </max:popup></td>
			<td><input type=file class="button" name="filter"></td>
		</tr>
		<tr>
			<td colspan=5><input type="button" class="button"
				value="   Insert   " onclick="insertFilter()"> <input
				type="button" class="button" value="   Remove   "
				onclick="removeFilterForDoc()"> <input type="button"
				class="button" value="   Cancel   " onclick="cancel()"></td>
		</tr>
	</tbody>
</table>
</form>
<%@ include file="../../end.jspf"%>
