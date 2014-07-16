<html>
<head>
<link rel="stylesheet" type="text/css" href="../css/styles.css">
</head>
<body>
<%@ page import="java.io.FileReader"%>
<%@ page import="java.io.BufferedReader"%>
<%@ page import="java.lang.StringBuffer"%>
<%@ page import="java.net.URLEncoder"%>
<%
    String fileName = (String) request.getParameter("fileName");

    BufferedReader br = new BufferedReader(new FileReader(fileName));
    StringBuffer buffer = new StringBuffer();

    if (br != null) {
        String line = br.readLine();
        while (line != null) {
            buffer.append(line);
            buffer.append("\n");
            line = br.readLine();
        }
    }
%>
<table>
	<tr>
		<td valign="top" class="border" colspan="4"><b><font
			class="titlesmall">&nbsp;&nbsp;OUTPUT TEST&nbsp;</font> </b></td>
	</tr>
	<tr>
		<td colspan="4">
		<hr></hr>
		</td>
	</tr>
	<tr>
		<td><%=buffer.toString()%></td>
	</tr>
</table>
</body>
</html>