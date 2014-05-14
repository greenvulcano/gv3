<%@ page language="java" pageEncoding="ISO-8859-1"%>
<%@ page session = "true" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml"  prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<HTML>
<HEAD>
	<TITLE>Administration functions</TITLE>
</HEAD>	
<BODY>
<%@ include file="../head.jspf" %>
<div class="ui-tabs ui-widget ui-widget-content ui-corner-all" style="width:1080px;margin:50px auto;">
<ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
    <li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/deploy/listaServiziCore.jsp">Deploy Services</a></li>
    <li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/deploy/listaServiziAdapter.jsp">Deploy Adapter</a></li>
    <li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active"><a>Deploy Support Parameter</a></li>
</ul> 
  <html:form action="/deploySupport" method="post" enctype="multipart/form-data"> 
   <input type="hidden" name="support"/>
   <input type="hidden" name="file" value='GVSupport'>
   <table cellpadding="4" cellspacing="0">
    <%int numRighe=0;%>
   <c:forEach items="${listaSupport}" var="servizi">
   	<tr>   		 
   		 <td><a href="javascript:invocaDeploy('<c:out value='${servizi}'/>');"><c:out value='${servizi}'/></a></td>
		</tr>
		<%numRighe++;%>
	 </c:forEach>
	 </table> 
	 <%for (int i=numRighe;i<18;i++){%>
	 	<br>
	 <%}%>
  </html:form>
</div>
  <script> 
   function invocaDeploy(support){
    document.forms[0].support.value=support;
    document.forms[0].submit();
   } 
  </script>
<%@ include file="../end.jspf" %>
<br><br>
</BODY>
</HTML>

