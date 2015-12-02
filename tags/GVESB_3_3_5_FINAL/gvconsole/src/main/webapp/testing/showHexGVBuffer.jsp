<%@ page import="it.greenvulcano.gvesb.gvconsole.workbench.plugin.*"%>
<html>
<head>
<link rel="stylesheet" type="text/css" href="../css/styles.css">
</head>
<title>Input</title>
<body>
<%
    try {
				TestManager testManager = new TestManager(request);
				testManager.cleanCache(response);
%>
<table class="data">
	<tr>
		<td valign="top">
		<table align="center" width="98%">
			<tr>
				<td class="border" colspan="4"><font class="titlesmall">&nbsp;&nbsp;GVBUFFER&nbsp;INPUT&nbsp;</font></td>
			</tr>
			<tr>
				<td>
				<hr></hr>
				</td>
			</tr>
			<tr>
				<td align="center"><font class="titlesmall">Data</font></td>
			</tr>
			<tr>
				<td align="left"><pre><%=testManager.get("inputDataDump")%></pre>
				</td>
			</tr>
		</table>
		</td>
	</tr>
</table>
<script>
            window.focus();
        </script>

<%@ include file="../exception.jspf"%>

</body>
</html>
