<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
	<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
		<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
			<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>

				<%
  session.removeAttribute("currentMenu");
  session.setAttribute("currentMenu", "webservice");
%>
					<%@ include file="../head.jspf" %>
						<script type="text/javascript">
							<!--
							$(function () {
								$('.infoqtip').tipsy({
									fade: true,
									gravity: 'w'
								});
							});
							//-->
						</script>
						<div class="titleSection">
							<h1>Web Service</h1>
						</div>
						<div class="ui-tabs ui-widget ui-widget-content ui-corner-all gv-tab">
							<ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
								<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/webservice/general.jsp">General&nbsp;Parameter</a></li>
								<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/webservice/managementwsdl.jsp">WSDL</a></li>
								<li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active"><a>UDDI</a></li>
								<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/ode/processes.jsp">BPEL Deployed Processes</a></li>
								<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/ode/instances.jsp">BPEL DCurrently Available Instances</a></li>
							</ul>
							<TABLE cellpadding="0" cellspacing="2">

								<TR>
									<TD>
										<fieldset class="ui-widget-header  ui-corner-all">
											<legend class="ui-widget-header  ui-corner-all">Web Services UDDI publishing</legend>
											<TABLE cellpadding="0" cellspacing="2">
												<html:form action="GVWebServiceAction" styleClass="gvdata_form">
													<TR>
														<TD colspan="2">
															<p>Existing services</p>
															<p>
																<font>
												<html:select multiple="true" property="dest" ondblclick="move(this.form.dest,this.form.src)" styleClass="multipleSelect">              
      												<logic:iterate id="fieldKey" name="GVWebServiceForm" property="wsdlListBean" scope="session">
      													<html:option value="${fieldKey.name}"><c:out value="${fieldKey.name}"/></html:option>					
      												</logic:iterate>
										  		</html:select>
											</font>
														</TD>
														<TD colspan="2">
															<button type="reset" class="button" onclick="move(this.form.dest,this.form.src)"><img src="<%=contextRoot%>/images/deplacer.gif" alt="Seleziona"></button>
															<br>
															<button type="reset" class="button" onclick="move(this.form.src,this.form.dest)"><img src="<%=contextRoot%>/images/deplacer_c.gif" alt="Deseleziona"></button>
														</TD>
														<TD colspan="2">
															<p>Services to publish</p>
															<p>
																<font>
										   	<html:select multiple="true" property="src" ondblclick="move(this.form.src,this.form.dest)" styleClass="multipleSelect"/>
									       </font>
															</p>
														</TD>
													</TR>
													<TR>
														<TD colspan="6">
															<br>
															<input type="reset" class="button" value="Select all" onclick="DoAllSelection(this.form.dest, this.form.src)" />
															<input type="reset" class="button" value="Deselect all" onclick="DoAllSelection(this.form.src, this.form.dest)" />
														</TD>
													</TR>
													<TR>
														<TD colspan="6">
															<p>
																<div align="center">
																	<br>

																	<logic:equal name="GVWebServiceForm" property="flag" value="false">
																		<html:submit property="action" value="Publish WSDL" styleClass="button" onclick="return form_control(this.form)" />
																	</logic:equal>
																</div>
														</TD>
													</TR>
												</html:form>
											</TABLE>
										</fieldset>
									</TD>
								</TR>
							</TABLE>

							<!-- WSDL pubblicati su UDDI Registry-->
							<TABLE cellpadding="0" cellspacing="2">
								<TR>
									<TD>
										<fieldset class="ui-widget-header  ui-corner-all">
											<legend class="ui-widget-header  ui-corner-all">Published services</legend>
											<TABLE cellpadding="0" cellspacing="2">

												<html:form action="GVWebServiceAction" styleClass="gvdata_form">
													<html:hidden property="action" />
													<TR>
														<TD colspan="6">
															<p>
																Web Services Information&nbsp;&nbsp;&nbsp;
																<input class="infoqtip" onclick="submitAction(this.form, 'Reload UDDI')" src="<%=contextRoot%>/images/refresh.gif" type="image" title="Reload UDDI services list." />
															</p>
														</TD>
													</TR>
													<logic:equal name="GVWebServiceForm" property="flag" value="false">
														<TABLE cellpadding="0" cellspacing="2">
															<TR>
																<TD class="input160">
																	<p>State</p>
																	<TD class="input160">
																		<p>Name</p>
																		<TD class="input160">
																			<p>WSDL URL</p>
																			<TD class="input160">
																				<p>Description</p>
																				<TD class="input160">
																					<p>Service Key</p>
																					<TD class="input160">
																						<p>&nbsp;</p>
															</TR>
															<logic:iterate id="fieldKey1" name="GVWebServiceForm" property="uddiServices" scope="session">
																<TR>
																	<c:choose>
																		<c:when test='${fieldKey1.value.flag}'>
																			<TD class="input160"><a class="infoqtip" href="#" title="WSDL file found on GreenVulcanoESB."><img src='<%=contextRoot%>/images/check.gif' border='0'/></a></TD>
																		</c:when>
																		<c:otherwise>
																			<TD class="input160"><a class="infoqtip" href="#" title="WSDL file NOT found on GreenVulcanoESB."><img src='<%=contextRoot%>/images/warning.gif' border='0'/></a></TD>
																		</c:otherwise>
																	</c:choose>
																	<TD class="input160">
																		<c:out value="${fieldKey1.value.serviceName}" />
																	</TD>
																	<TD class="input160">
																		<html:link href="${fieldKey1.value.accessURL}" target="_blank">
																			<c:out value="${fieldKey1.value.accessURL}" />
																		</html:link>
																	</TD>
																	<TD class="input160">
																		<c:out value="${fieldKey1.value.description}" />
																	</TD>
																	<TD class="input160">
																		<c:out value="${fieldKey1.value.serviceKey}" />
																	</TD>
																	<TD class="input160">
																		<html:checkbox property="src" value="${fieldKey1.value.serviceKey}" />
																	</TD>
																</TR>
															</logic:iterate>
															<TR>
																<TD colspan="6">
																	<br>
																	<input type="button" class="button" value="Select all" onClick="check(this.form.src)" />
																	<input type="button" class="button" value="Deselect all" onclick="uncheck(this.form.src)" />
																</TD>
															</TR>
															<TR>
																<TD colspan="6">
																	<div align="center">
																		<p>&nbsp;</p>
																		<p>
																			<html:submit property="actionB" value="Unpublish WSDL" styleClass="button" onclick="if(form_control(this.form)) {submitAction(this.form, 'Unpublish WSDL'); return true;} return false;" />
																		</p>
																	</div>
																</TD>
															</TR>
														</TABLE>
													</logic:equal>

													<logic:equal name="GVWebServiceForm" property="flag" value="true">
														<td>
															<b><font color="RED">No UDDI Registry configured.</font></b>
														</td>
													</logic:equal>
												</html:form>
											</TABLE>
											<script type="text/javascript">
												function submitAction(form, action) {
													form.elements["action"].value = action;
													form.submit();
												}
											</script>
										</fieldset>
										</TD>
								</TR>

							</TABLE>

						</div>
						<%@ include file="../end.jspf" %>