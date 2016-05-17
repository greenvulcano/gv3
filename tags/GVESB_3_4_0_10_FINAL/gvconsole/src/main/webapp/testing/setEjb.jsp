<%@ page import="it.greenvulcano.gvesb.gvconsole.workbench.plugin.*"%>
<%@ include file="../head.jspf"%>

<%
    String descriptionTest = "Ejb Parameters";
    TestManager testManager = new TestManager(request);
%>

<form method="post" id="setParameters" action="<%=contextRoot%>/SetParameters">
<div style="margin: 20px auto 10px auto; width: 1080px;"
	class="ui-widget-header ui-corner-all">

<table>
	<tr>
	<td valign="top" colspan="4" class="border"><b><font
		class="titlesmall">&nbsp;&nbsp;EJB Parameters&nbsp;</font></b></td>
	</tr>
	<tr>
		<td colspan="4">
		<hr></hr>
		</td>
	</tr>
	<tr>
		<td align="left">Jndi Name</td>
		<td align="left"><input type="text" class="input120"
			name="jndiName" size="40" value="<%=testManager.get("jndiName")%>"></td>
	</tr>
	<tr>
		<td align="left">Jndi Factory</td>
		<td align="left"><input type="text" class="input120"
			name="jndiFactory" size="40"
			value="<%=testManager.get("jndiFactory")%>"></td>
	</tr>
	<tr>
		<td align="left">URL</td>
		<td align="left"><input type="text" class="input120"
			name="providerUrl" size="40"
			value="<%=testManager.get("providerUrl")%>"></td>
	</tr>
	<tr>
		<td align="left">User</td>
		<td align="left"><input type="text" class="input120" name="user"
			size="40" value="<%=testManager.get("user")%>"></td>
	</tr>
	<tr>
		<td align="left">Password</td>
		<td align="left"><input type="text" class="input120"
			name="password" size="40" value="<%=testManager.get("password")%>"></td>
	</tr>
	<tr>
		<td height='30'></td>
	</tr>
	<tr>
		<td align="center" colspan="2"><input type="submit"
			class="button" name="confirm" value="Confirm"></td>
	</tr>
</table>
</div>
</form>

<%@ include file="../end.jspf"%>
