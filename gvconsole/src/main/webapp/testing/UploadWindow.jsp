<%@ page import="it.greenvulcano.gvesb.gvconsole.workbench.plugin.*"%>
<html>
<head>
<link rel="stylesheet" type="text/css" href="../css/styles.css">
</head>
<title>Upload</title>
<body>
<%
    try {
				TestManager testManager = new TestManager(request);
				TestPlugin testPlugin = testManager.getPlugin();
				testPlugin.setResetValue("no");

				String currentEncoding = (String) testManager
						.get("charEncoding");

				// Not for all test there are system/service parameters
				//
				String system = request.getParameter("system");
				String service = request.getParameter("service");
				if (system != null && service != null) {
					testManager.set("inputSystem", system);
					testManager.set("inputService", service);
				}

				String[] charEncodings = new String[]{"Binary", "US-ASCII",
						"ISO-8859-1", "UTF-8", "UTF-16BE", "UTF-16LE", "UTF-16"};

				testManager.cleanCache(response);
%>
<table>
	<tr>
		<td valign="top"><!-- Upload function -->
		<form action="../Upload" method="post" enctype="multipart/form-data">
		<table>
			<tr>
				<td class="border" colspan="4"><font class="titlesmall">&nbsp;&nbsp;UPLOAD&nbsp;</font></td>
			</tr>
			<tr>
				<td>
				<hr></hr>
				</td>
			</tr>
			<tr>
				<td align="center"><b>Select file</b></td>
			</tr>
			<tr>
				<td align="left"><input type="file" name="data"><br>
				</td>
			</tr>
			<%@ include file="comboEncodingInput.jspf"%>
			<tr>
				<td align="left"><input type="submit" class="button"
					value="Upload" /></td>
			</tr>
		</table>
		</form>
		</td>
	</tr>
</table>
<script>
            window.focus();
        </script>

<%@ include file="../exception.jspf"%>

</body>
</html>
