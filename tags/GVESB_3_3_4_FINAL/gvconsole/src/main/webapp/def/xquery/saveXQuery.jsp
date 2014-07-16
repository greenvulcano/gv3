<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max" %>
<max:nocache/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="StyleSheet" type="text/css" href="<max:prop prop="max.site.root"/>/styles.css"/>
<title>Save XQuery</title>
</head>
<script language="Javascript">
	function updateAndClose()
	{
		if(checkUnique() || window.confirm("Vuoi sovrascrivere la xquery precedente?"))
		{
			opener.document.getElementById("_nome").value = document.getElementById("_nome").value;
			opener.document.getElementById("_descrizione").value = document.getElementById("_descrizione").value;
			opener.document.getElementById("_xqueryString").value = document.getElementById("_xqueryString").value;
			opener.document.forms.xQueriesElement.submit();
			self.close();
		}	
	}
	
	function checkUnique()
	{
		opts = opener.document.getElementById("selectQS").options;
		for(var i=0; opts[i]; i++)
		{
			var nome ="";
			if(opts[i].text)
				nome = opts[i].text;
			nomeComp = document.getElementById("_nome").value;
			if(nome==nomeComp)
				return false;
		}
		return true;
	}
</script>

<body>
	<% String xQueryString = request.getParameter("xQueryString"); 
	   String nome = request.getParameter("nome");
	   String descrizione = request.getParameter("descrizione");
	%>
	
	<table class="xquery">
		<tr class="border">
			<td colspan="2">
				<h3>Save XQuery</h3>
			</td>
		</tr>
		<tr class="border">
			<td>
				Nome:
			</td>
			<td>
				<input type="text" name="nome" id="_nome" class="input200" value="<%=nome%>"/>
			</td>
		</tr>
		<tr class="border">
			<td>
				XQuery:
			</td>
			<td>
				<textarea name="xqueryString" id="_xqueryString" class="xqueryInput"><%=xQueryString%></textarea>
			</td>
		</tr>
		<tr class="border">
			<td>
				Descrizione:&nbsp;&nbsp;
			</td>
			<td>
				<textarea name="descrizione" id="_descrizione" class="xqueryInput"><%=descrizione%></textarea>
			</td>
		<tr class="border">
			<td colspan="2" align="center">
				<input type="button" value="save" onclick="return updateAndClose()">
			</td>
		</tr>
	</table>
</body>
</html>