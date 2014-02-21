<%@ page language="java" pageEncoding="ISO-8859-1"%>
<%@ page session = "true" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml"  prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@ include file="../head.jspf" %>

<html:html>
  <head>
   <html:base />
   <title>Deploy</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	
	<!-- link rel="stylesheet" type="text/css" href="styles.css" -->	
	  <link href="<%=contextRoot %>/css/XMLDisplay.css" type="text/css" rel="stylesheet">
	  <script type="text/javascript" src="<%=contextRoot %>/js/XMLDisplay.js"></script>
  </head>
  <body> 
<html:html>

<head>
    <title>Green Vulcano Service Adder</title>
</head>
<body onload="visualizzaFile('<c:out value="${datiServizio.nodoServer}"/>',
		                        '<c:out value="${datiServizio.nodoNew}"/>',
		                        '<c:out value="${datiServizio.exist}"/>',
		                        '<c:out value="${datiServizio.equals}"/>');">
</body>
</html:html>
  
<div id="XMLContainer">
	<div class="confronto1"  style="position:absolute;  left: 5px; top: 180px; width: 420px;"><center><b>XML NEW</b></center><br>
		<div id="XMLHolderNEW" class="confronto"></div>
	</div>
	<div class="confronto1"  style="position:absolute;  left: 430px; top: 180px; width: 420px;"><center><b>XML SERVER</b></center><br>
		<div id="XMLHolderOLD" class="confronto"></div>
	</div>
</div>

<script type="text/javascript">
 function visualizzaFile(xmlserver,xmlnew,exist,equals){		
   if(exist=='true' && equals=='true')
    document.getElementById('XMLHolder').innerHTML = "Service already exists";
   else if(exist=='true' && equals=='false'){
     document.getElementById('XMLHolder').innerHTML = "Service already exists but with different values"; 
   }
   else
   	  document.getElementById('XMLHolder').innerHTML = "Service doesn't exist";	 
   document.getElementById('XMLHolderOLD').innerHTML ='';
   document.getElementById('XMLHolderNEW').innerHTML ='';
   div = "XMLHolder";
   document.getElementById("XMLContainer").style.display = 'block';
   if(exist=='true'){
     LoadXMLString(div + "OLD",xmlserver);
   }
   LoadXMLString(div + "NEW",xmlnew);
 }
</script>

<div id="XMLHolder" style="float: right;
          Background: mintcream;
          border: 1px solid gray;
          height:34px;
          margin-bottom:2px;
          margin-left:150px;
          margin-right:2px;
          margin-top:2px;
          text-align:center;
          vertical-align:text-top;
          width:300px;">
         </div><br><br>
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
function ImpostaDefault(variabile){
	document.getElementById('VARIABILE_'+variabile).value = document.getElementById('VARIABILE_DEF_'+variabile).value;
}
</script>

<html:form action="/deploy" method="post" enctype="multipart/form-data" style="position:absolute;  left: 370px; top: 570px;">
  <input type="hidden" name="servizio" value='<c:out value="${servizio}"/>'>
  <input type="hidden" name="oggettoFile">
  <input type="hidden" name="file" value='<c:out value="${file}"/>'> 
  <input type="hidden" name="salvaXml"> 
  <input type="button" value="Deploy"  onclick="javascript:CheckBox();">
  <input type="button" value="Cancel"  onclick="javascript:goListaServizi();">  
</html:form> 
<c:if test="${variabili!=null}">  
<html:form action="/save" method="post" enctype="multipart/form-data" style="position:absolute;  left: 860px; top: 200px;"> 
  	GLOBAL VARIABLES<br>
  <input type="hidden" name="servizio" value='<c:out value="${servizio}"/>'>
  	<table>
    <tr><td><b>name</b></td><td><b>value</b></td><td><b>default</b></td><td><b>set def value</b></td><td><b>description</b></td></tr>
  	<c:forEach items="${variabili}" var="variabili"> 		
     <tr><td>${variabili.nome}</td><td><input type="text" id=VARIABILE_${variabili.nome} name=VARIABILE_${variabili.nome} value='<c:out value="${variabili.valore}"/>'style="width: 80px;"></td><td><input type="text" id=VARIABILE_DEF_${variabili.nome} value='<c:out value="${variabili.valoreServer}"/>'style="width: 80px;" readonly></td><td><button type="button" onclick="javascript:ImpostaDefault('${variabili.nome}');"></td><td>${variabili.descrizione}</td></tr>
    </c:forEach>
    </table>
    <br>
    <input type="submit" value="Accept" onclick="javascript:goSave();"> 
    <input type="hidden" name="file" value='<c:out value="${file}"/>'>
</html:form> 
</c:if> 

  </body>

</html:html> 

<%@ include file="../end.jspf" %>

