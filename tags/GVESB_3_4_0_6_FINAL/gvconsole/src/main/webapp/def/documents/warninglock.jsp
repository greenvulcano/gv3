<%@ page import="max.xml.*" %>
<%@ page import="max.documents.*" %>

<%@ include file="../../head.jspf" %>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max"%>

	<script>
		function goBack()
        {
            this.location.href = "def/xmleditor";
        }		
	</script>   
   <h1><nobr>LOCK FOUND</nobr></h1>
   <form name="WarningForm">
		<table class="search">
			<tr>
				<td>
				    Sorry!<br>The file selected has been locked by another user.<br> 
				</td>
			</tr>
			<tr>
				<td>
				  <b>Go Back</b> to the document list
				</td>
			</tr>
			<tr>
               <td><hr></td>
			</tr>
			<tr>
				<td align=right>
				   <input name=btnWarning type=button value="  Go Back  " onclick="goBack();">
				</td>
			</tr>	
		</table>
   </form>

<%@ include file="../../end.jspf" %>
