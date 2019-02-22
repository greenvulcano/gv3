<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

	<%
  session.removeAttribute("currentMenu");
  session.setAttribute("currentMenu", "deploy");
%>

		<%@ include file="../head.jspf"%>
			<div class="titleSection">
				<h1>Deploy New Service</h1>
			</div>
			<div class="gv-container">
				<div class="ui-tabs ui-widget ui-widget-content ui-corner-all">
					<ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
						<li class="tab-title-gv">Load Zip File</li>
					</ul>

					<div class="ui-tabs ui-widget ui-widget-content ui-corner-all backGv">
						<html:form action="/fileUpload" method="post" enctype="multipart/form-data">
							<table>

								<tr>
									<td colspan=5>
										<br>
									</td>
									<tr>

										<td>
											<div class="chooseFileGgv">
												<html:file property="theFile" size="60" />
											</div>
										</td>
									</tr>
									<tr>
										<td colspan=5>
											<div class="buttonGv">
												<html:submit>Submit</html:submit>
											</div>
										</td>
									</tr>
							</table>
						</html:form>
					</div>

				</div>
			</div>
			<%@ include file="../end.jspf" %>