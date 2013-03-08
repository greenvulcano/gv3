<%@ page session = "true" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<%
  session.removeAttribute("currentMenu");
  session.setAttribute("currentMenu", "utility");
%>

<%@include file="../head.jspf"%> 

<div class="titleSection"><h1>Export Configuration file</h1></div>
<div class="ui-widget-header ui-corner-all" style="margin: 0pt auto; width: 200px; text-align: center;padding:30px">
	<a href="javascript:goExport();" title="Download config" style="text-decoration:none;">
		<img alt="Download" src="../images/downConfig.png" border="0" align="middle" />
		<br/>
		Download config
	</a> 
</div>
<script>function goExport(){document.forms[0].submit();}</script> 
 
<html:form action="/fileExport" method="post"> 
<br></html:form>  
<br><br><br><br><br><br><br><br><br><br><br><br>
<%@include file="../end.jspf"%> 