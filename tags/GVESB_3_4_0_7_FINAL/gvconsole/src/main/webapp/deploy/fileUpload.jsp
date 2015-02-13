<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<%
  session.removeAttribute("currentMenu");
  session.setAttribute("currentMenu", "deploy");
%>

<%@ include file="../head.jspf"%>

<div class="titleSection"><h1>Deploy New Service</h1></div>
<html:form action="/fileUpload" method="post" enctype="multipart/form-data">            		
            		<table class="ui-widget-header ui-corner-all">
            			<tr  class="color1">
            			   <td colspan="5"><b>Load Zip File</b></td>
            			</tr>
            			<tr>
            				<td colspan=5><br>
            			</td>
            			<tr>
            				<td nowrap>File</td>
            				
            				<td><html:file property="theFile" size="60"/> </td>
            			</tr>
            			<tr>
            				<td colspan=5>
            					<html:submit>Submit</html:submit>
            		        </td>
                	    </tr>
                    </table>
            	</html:form>
<br/><br><br><br><br><br><br><br><br><br><br><br>            	
<%@ include file="../end.jspf" %>