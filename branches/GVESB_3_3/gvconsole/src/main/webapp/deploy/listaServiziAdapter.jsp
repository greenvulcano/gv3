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
	<TITLE> Lista Funzioni administrator </TITLE>
</HEAD>	
<BODY>
<%@ include file="../head.jspf" %>
<div class="ui-tabs ui-widget ui-widget-content ui-corner-all" style="width:1080px;margin:50px auto;">
<ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
    <li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/deploy/listaServiziCore.jsp">Deploy Services</a></li>
    <li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active"><a>Deploy Adapter</a></li>
    <li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/deploy/listaServiziSupport.jsp">Deploy Support Parameter</a></li>
</ul>

  <html:form action="/deployAdapter" method="post" enctype="multipart/form-data"> 
   <input type="hidden" name="servizio"/>
   <input type="hidden" name="adapter"/>
   <input type="hidden" name="file" value='GVAdapters'>
   <table cellpadding="4" cellspacing="1">
   <tr>
   	<td><font color="red" size="4">WEBSERVICES:</font></td>
   </tr>
   <%int numRighe=0;%>
   <c:forEach items="${listaWebServices}" var="listaWebServices">
   	<tr>   		 
   		 <td><a href="javascript:invocaDeploy('<c:out value='${listaWebServices}'/>','WEB_SERVICES');"><c:out value='${listaWebServices}'/></a></td>
		</tr>
		<%numRighe++;%>
	 </c:forEach>
   <tr>
   <tr>
   	<td><font color="red" size="4">EXCEL_WORK:</font></td>
   </tr>
   <%numRighe=0;%>
   <c:forEach items="${listaGVExcelWorkbook}" var="listaGVExcelWorkbook">
   	<tr>   		 
   		 <td><a href="javascript:invocaDeploy('<c:out value='${listaGVExcelWorkbook}'/>','EXCEL_WORK');"><c:out value='${listaGVExcelWorkbook}'/></a></td>
		</tr>
		<%numRighe++;%>
	 </c:forEach>
    <tr>
   	<td><font color="red" size="4">EXCEL_REPO:</font></td>
   </tr>
   <%numRighe=0;%>
   <c:forEach items="${listaGVExcelRepo}" var="listaGVExcelRepo">
   	<tr>   		 
   		 <td><a href="javascript:invocaDeploy('<c:out value='${listaGVExcelRepo}'/>','EXCEL_REPO');"><c:out value='${listaGVExcelRepo}'/></a></td>
		</tr>
		<%numRighe++;%>
	 </c:forEach>
    <tr>
    <td><font color="red" size="4">BIRT_REPO:</font></td>
   </tr>
   <%numRighe=0;%>
   <c:forEach items="${listaGVBirtRepo}" var="listaGVBirtRepo">
    <tr>         
         <td><a href="javascript:invocaDeploy('<c:out value='${listaGVBirtRepo}'/>','BIRT_REPO');"><c:out value='${listaGVBirtRepo}'/></a></td>
        </tr>
        <%numRighe++;%>
     </c:forEach>
    <tr>
    <td><font color="red" size="4">DH_ENGINE:</font></td>
   </tr>
   <%numRighe=0;%>
   <c:forEach items="${listaGVDataHandler}" var="listaGVDataHandler">
    <tr>         
         <td><a href="javascript:invocaDeploy('<c:out value='${listaGVDataHandler}'/>','DH_ENGINE');"><c:out value='${listaGVDataHandler}'/></a></td>
        </tr>
        <%numRighe++;%>
     </c:forEach>
    <tr>
   	<td><font color="red" size="4">DATA PROVIDER:</font></td>
   </tr>
   <%numRighe=0;%>
   <c:forEach items="${listaDataProvider}" var="listaDataProvider">
   	<tr>   		 
   		 <td><a href="javascript:invocaDeploy('<c:out value='${listaDataProvider}'/>','GVDP');"><c:out value='${listaDataProvider}'/></a></td>
		</tr>
		<%numRighe++;%>
	 </c:forEach>
    <tr>
    <td><font color="red" size="4">RULES SET:</font></td>
   </tr>
   <%numRighe=0;%>
   <c:forEach items="${listaKnowledgeBaseConfig}" var="listaKnowledgeBaseConfig">
    <tr>         
         <td><a href="javascript:invocaDeploy('<c:out value='${listaKnowledgeBaseConfig}'/>','RULES_CFG');"><c:out value='${listaKnowledgeBaseConfig}'/></a></td>
        </tr>
        <%numRighe++;%>
     </c:forEach>
    <tr>
    <td><font color="red" size="4">HL7 LISTENERS:</font></td>
   </tr>
   <%numRighe=0;%>
   <c:forEach items="${listaGVHL7}" var="listaGVHL7">
    <tr>         
         <td><a href="javascript:invocaDeploy('<c:out value='${listaGVHL7}'/>','HL7_LISTENERS');"><c:out value='${listaGVHL7}'/></a></td>
        </tr>
        <%numRighe++;%>
     </c:forEach>
   <tr>	
   	<td><font color="red" size="4">ADAPTER:</font></td>
   </tr>
   <%numRighe=0;%>
   <c:forEach items="${listaAdapter}" var="adapter">
   	     <c:if test="${adapter!='WEB_SERVICES' && adapter!='EXCEL_WORK' && adapter!='EXCEL_REPO' && adapter!='GVDP' && adapter!='BIRT_REPO' && adapter!='DH_ENGINE'}"> 
                <tr>   		 
   		 <td><a href="javascript:invocaDeploy('<c:out value='${adapter}'/>','<c:out value='${adapter}'/>');"><c:out value='${adapter}'/></a></td>
		</tr>
             </c:if> 
		<%numRighe++;%>
	 </c:forEach>
	 </table> 
	 <%for (int i=numRighe;i<18;i++){%>
	 	<br>
	 <%}%>
  </html:form>
  </div> 
  <script> 
   function invocaDeploy1(adapter){
    document.forms[0].tipoOggetto.value=adapter;
    document.forms[0].servizio.value=adapter;
    document.forms[0].adapter.value=adapter;
    document.forms[0].submit();
   } 
   function invocaDeploy(servizio,adapter){
    document.forms[0].servizio.value=servizio;
    document.forms[0].adapter.value=adapter;
    document.forms[0].submit();
   } 
  </script>
<%@ include file="../end.jspf" %>
<br><br>
</BODY>
</HTML>

