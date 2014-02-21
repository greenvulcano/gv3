<%@ page language="java" pageEncoding="ISO-8859-1"%>
<%@ page session = "true" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml"  prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@ include file="../head.jspf" %>

<html:html lang="true">
  <head>
   <html:base />
   <title>Deploy</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	
	<!-- link rel="stylesheet" type="text/css" href="styles.css" -->	
	  <link href="../css/XMLDisplay.css" type="text/css" rel="stylesheet">
	  <script type="text/javascript" src="../scripts/XMLDisplay.js"></script>
  </head>
  <body> 
<html:html>

<head>
    <title>Green Vulcano Service Adder</title>
</head>
<body>	                        	
	
</body>
</html:html>
<script>
function CheckBox(){
    document.forms[0].submit();
}
function goSave(){
  document.forms[1].submit();
}
function goListaServizi(){
  history.back();
}
</script>
<c:if test="${variabili!=null}">  
<html:form action="/save" method="post" enctype="multipart/form-data"> 
  	GLOBAL VARIABLES<br>
  <input type="hidden" name="servizio" value='<c:out value="${servizio}"/>'>
  	<c:forEach items="${variabili}" var="variabili"> 		
       ${variabili.nome} <input type="text" name=VARIABILE_${variabili.nome} value='<c:out value="${variabili.valore}"/>'><input type="text" name=VARIABILE_${variabili.nome} value='<c:out value="${variabili.valoreServer}"/>'>${variabili.descrizione}<br>
    </c:forEach>
    <br>
    <input type="submit" value="Accept" onclick="javascript:goSave();"> 
    <input type="hidden" name="file" value='<c:out value="${file}"/>'>
</html:form> 
</c:if> 
</body>
</html:html>

<%@ include file="../end.jspf" %>
