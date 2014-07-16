<%@ page import="java.util.Map"%>
<%@ page
	import="it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestObject"%>

<html>
<head>
<link rel="stylesheet" type="text/css" href="../css/styles.css">
</head>
<body>
<%
    try {
        String reference = (String) request.getParameter("reference");
        Map mapTestObject = (Map) session.getAttribute("mapTestObject");
%>
<table align="center">
	<tr class="error">
		<td valign="top" colspan="4"><B>&nbsp;&nbsp;EXCEPTION&nbsp;</B></td>
	</tr>
	<tr>
		<td colspan="4">
		<hr></hr>
		</td>
	</tr>
	<tr>
		<%
		    if (mapTestObject != null) {
		            TestObject testObject = (TestObject) mapTestObject.get(new Integer(reference));
		%>
		<td><font face="verdana" size="2"><%=testObject.getThrowableMsg()%></font>
		</td>
		<%
		    }
		%>
	</tr>
</table>

<%@ include file="../exception.jspf"%>

</body>
</html>
