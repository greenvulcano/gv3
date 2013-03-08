<%@ page import="it.greenvulcano.gvesb.gvconsole.workbench.plugin.*"%>
<html>

<head>
<link rel="stylesheet" type="text/css" href="../css/styles.css">
</head>
<title>Output Data</title>
<body>
<%
    try {
				TestManager testManager = new TestManager(request);
				String encoding = request.getParameter("encoding");
				if (encoding != null) {
					testManager.set("charEncodingOutput", encoding);
				}

				testManager.cleanCache(response);
%>
<table class="data">
	<tr>
		<td valign="top">
		<table align="center" width="98%">
			<tr>
				<td class="border" colspan='4'><FONT class="titlesmall">&nbsp;&nbsp;GVBUFFER&nbsp;OUTPUT&nbsp;</FONT></td>
			</tr>
			<tr>
				<td>
				<hr></hr>
				</td>
			</tr>
			<tr>
				<td><font class="titlesmall">Data</font></td>
			</tr>
			<tr>
				<td align="left"><pre><%=testManager.get("data")%></pre></td>
			</tr>
		</table>
	</tr>
</table>
<script>
            window.focus();
        </script>

<%@ include file="../exception.jspf"%>

</body>
</html>
