<%@ page import="max.config.*" %>
	<%
  session.removeAttribute("currentMenu");
  session.setAttribute("currentMenu", "modParamConf");
%>
		<%@ include file="../../head.jspf" %>
			<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max"%>
				<script type="text/javascript">
					$(function () {
						$("#accordion").accordion();
					});
				</script>
				<div class="titleSection">
					<h1>Configuration management</h1>
				</div>
				<table align="center">
					<tr class="search">
						<td class="">
							<%@ include file="/def/xmleditor/xmlmenu.jsp" %>
						</td>
					</tr>
					<tr class="search">
						<td colspan=3>
							Using this tool you can select a configuration document for editing and shows document history.
						</td>
					</tr>
					<tr class="search">
						<td colspan=3>
							<hr>
							<br>
							<a name="configured"></a>
							<jsp:include page="/documents" flush="true">
								<jsp:param name="operation" value="/showRepository" />
							</jsp:include>
						</td>
					</tr>
					<!-- <tr>
            <td colspan="3">
	            <hr>
	            <br/>
	            <br/>
	            Using this tool you can select a client document for editing.
            </td>
        </tr> -->
					<%-- <tr>
            <td colspan="3">
                <a href="<%=contextRoot%>/propertiesMain.do">Global Properties Editor</a>
						</td>
						</tr> --%>
				</table>
				<%@ include file="../../end.jspf" %>