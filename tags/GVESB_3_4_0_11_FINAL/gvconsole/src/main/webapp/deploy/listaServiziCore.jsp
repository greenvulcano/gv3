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
    <li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active"><a>Deploy Services</a></li>
    <li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/deploy/listaServiziAdapter.jsp">Deploy Adapter</a></li>
    <li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/deploy/listaServiziSupport.jsp">Deploy Support Parameter</a></li>
</ul>
  <html:form action="/deployCore" method="post" enctype="multipart/form-data"> 
   <input type="hidden" name="servizio"/>
   <input type="hidden" name="file" value='GVCore'>
   <input type="hidden" name="tipoOggetto" value=''>  
   <table cellpadding="4" cellspacing="1">
   <tr>
   	<td><font color="red" size="4">SERVICES:</font></td>
  </tr>
   <%int numRighe=0;%>
   <c:forEach items="${listaServizi}" var="servizi">		 
    <tr><td><a href="javascript:invocaDeployServizi('<c:out value='${servizi}'/>');"><c:out value='${servizi}'/></a></td></tr>
		<%numRighe++;%>
	 </c:forEach>
   <tr>
    <td><font color="red" size="4">DTE TRANSFORMATIONS:</font></td>
  </tr>
   <c:forEach items="${listaTransformation}" var="transformation">         
    <tr><td><a href="javascript:invocaDeployTransformation('<c:out value='${transformation}'/>');"><c:out value='${transformation}'/></a></td></tr>
        <%numRighe++;%>
     </c:forEach>
	 <tr>
	 <td><font color="red" size="4">EXTRA COMPONENTS:</font></td>
	 </td>
	</tr>
	 <c:forEach items="${listaCoreParametri}" var="parametri">	
   	 <tr><td><a href="javascript:invocaDeployParameter('<c:out value='${parametri}'/>');"><c:out value='${parametri}'/></a></td></tr>
		<%numRighe++;%>
	 </c:forEach> 
	 </tr>
	 </table> 
	 <%for (int i=numRighe;i<18;i++){%>
	 	<br>
	 <%}%>	
  </html:form>
  </div>
  <script> 
   function invocaDeployServizi(servizio){
    document.forms[0].servizio.value=servizio;
    document.forms[0].tipoOggetto.value='Servizio';
    document.forms[0].submit();
   } 
   function invocaDeployTransformation(transformation){
    document.forms[0].servizio.value=transformation;
    document.forms[0].tipoOggetto.value='Transformation';
    document.forms[0].submit();
   } 
   function invocaDeployParameter(parameter){
    document.forms[0].servizio.value='Parameter';
    document.forms[0].tipoOggetto.value=parameter;
    document.forms[0].submit();
   }
  </script>
<%@ include file="../end.jspf" %>
<br><br>
</BODY>
</HTML>

