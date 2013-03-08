<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max" %>
<max:nocache/>
<html>
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
		while(line != null) {
			buffer.append(line);
			buffer.append("\n");
			line = br.readLine();
		}
	}
%>	
	<link rel="StyleSheet" type="text/css" href="<max:prop prop="max.site.root"/>/styles.css"/>
	<table align="center" width="95%">
    <td valign="top" class="border" colspan="4"><FONT class="titlesmall">&nbsp;&nbsp;XQuery Result&nbsp;</FONT></B></td>
    <tr>
        <td>
            <%=buffer.toString()%>
        </td>
    </tr>
</table>
</body>
</html>