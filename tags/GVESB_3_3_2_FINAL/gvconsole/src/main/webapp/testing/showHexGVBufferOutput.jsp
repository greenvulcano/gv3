<%@ page import="it.greenvulcano.gvesb.gvconsole.workbench.plugin.*"%>
<html>
<head>
<link rel="stylesheet" type="text/css" href="../css/styles.css">
</head>
<title>Output Binary Data</title>
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
			<!-- Upload function -->
			<tr>
				<td class="border" colspan='4'><FONT class="titlesmall">&nbsp;&nbsp;GVBUFFER&nbsp;BINARY&nbsp;DATA&nbsp;OUTPUT&nbsp;</FONT></td>
			</tr>
			<tr>
				<td>
				<hr></hr>
				</td>
			</tr>
			<tr>
				<td align="center"><font class="titlesmall">Binary data</font>
				</td>
			</tr>
			<tr>
				<td align="left"><pre><%=testManager.get("outputDataDump")%></pre>
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
