<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
	<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
		<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
			<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>

				<jsp:useBean id="GVWebServiceForm" class="it.greenvulcano.gvesb.gvconsole.webservice.forms.GVWebServiceForm" scope="session" />

				<%
  session.removeAttribute("currentMenu");
  session.setAttribute("currentMenu", "webservice");
%>
					<%@ include file="../head.jspf" %>
						<div class="titleSection">
							<h1>Web Service</h1>
						</div>
						<div class="ui-tabs ui-widget ui-widget-content ui-corner-all gv-tab">
							<ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
								<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/webservice/general.jsp">General&nbsp;Parameter</a></li>
								<li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active"><a>WSDL</a></li>
								<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/webservice/managementuddi.jsp">UDDI</a></li>
								<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/ode/processes.jsp">BPEL Deployed Processes</a></li>
								<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/ode/instances.jsp">BPEL DCurrently Available Instances</a></li>
							</ul>
							<TABLE cellpadding="0" cellspacing="2">

								<!-- GENERAZIONE DINAMICA WSDL -->
								<html:form action="GVWebServiceAction" styleClass="gvdata_form">
									<TR>
										<TD>
											<fieldset class="ui-widget-header  ui-corner-all">
												<legend class="ui-widget-header  ui-corner-all">Dynamic WSDL Generation</legend>
												<TABLE cellpadding="0" cellspacing="2">

													<TR>
														<TD>
															<p>Web Services</p>
															<p><font>
										  <html:select multiple="true" property="dest" ondblclick="move(this.form.dest,this.form.src)" styleClass="multipleSelect">              
      											<logic:iterate id="fieldKey" name="GVWebServiceForm" property="businessWebServicesBean.webServicesBeanMap" scope="session">
      												<html:option value="${fieldKey.key}"><c:out value="${fieldKey.key} - BUSINESS"/></html:option>					
      											</logic:iterate>     											
										  </html:select>
											    </font>
															</p>
														</TD>
														<TD>
															<button type="reset" class="button" onclick="move(this.form.dest,this.form.src)"><img src="<%=contextRoot%>/images/deplacer.gif" alt="Seleziona"></button>
															<br>
															<button type="reset" class="button" onclick="move(this.form.src,this.form.dest)"><img src="<%=contextRoot%>/images/deplacer_c.gif" alt="Deseleziona"></button>
														</TD>
														<TD>
															<p>WSDL to create</p>
															<p><font>
										   <html:select multiple="true" property="src" ondblclick="move(this.form.src,this.form.dest)" styleClass="multipleSelect"/>
											      </font>
															</p>
														</TD>
													</TR>
													<TR>
														<TD colspan="3">
															<br>
															<input type="reset" class="button" value="Select all" onclick="DoAllSelection(this.form.dest, this.form.src)" />
															<input type="reset" class="button" value="Deselect all" onclick="DoAllSelection(this.form.src, this.form.dest)" />
														</TD>
													</TR>
													<TR>
														<TD colspan="6">
															<div align="center">
																<html:submit property="action" value="Create WSDL" styleClass="button" onclick="return form_control(this.form)" />
															</div>
														</TD>
													</TR>
												</TABLE>
											</fieldset>
										</TD>
								</html:form>
							</TABLE>


							<!-- WSDL ESISTENTI -->

							<TABLE cellpadding="0" cellspacing="2">
								<TR>
									<TD>
										<fieldset class="ui-widget-header ui-corner-all">
											<legend class="ui-widget-header ui-corner-all">Existing WSDLs</legend>
											<TABLE cellpadding="0" cellspacing="2">
												<html:form action="GVWebServiceAction" styleClass="gvdata_form">
													<TR>
														<TD colspan="6">File name and last modified date:</TD>
													</TR>
													<!--logic:iterate id="fieldMap" name="GVWebServiceForm" property="wsdlMapBean" scope="session"-->
													<c:forEach items="${GVWebServiceForm.wsdlListBean}" var="fieldKey1">
														<TR>
															<c:choose>
																<c:when test='${fieldKey1.flag}'>
																	<TD class="input160"><img src="<%=contextRoot%>/images/check.gif" border='0' /></TD>
																</c:when>
																<c:otherwise>
																	<TD class="input160"><img src="<%=contextRoot%>/images/warning.gif" border='0' /></TD>
																</c:otherwise>
															</c:choose>
															<TD class="input160">
																<c:set target="${GVWebServiceForm}" property="url" value="${fieldKey1.name}" />
																<html:link href="<%=GVWebServiceForm.getBusinessValue()%>" target="_blank">
																	<c:out value="${fieldKey1.name}" />
																</html:link>
															</TD>
															<TD class="input160">
																<c:out value="${fieldKey1.data}" />
															</TD>
															<TD class="input160">
																<html:checkbox property="src" value="${fieldKey1.name}" />
															</TD>
														</TR>
													</c:forEach>
													<!--/logic:iterate-->
													<TR>
														<TD colspan="4">
															<br>
															<input type="button" class="button" value="Select all" onClick="check(this.form.src);check(this.form.src)" />
															<input type="button" class="button" value="Deselect all" onClick="uncheck(this.form.src);uncheck(this.form.src)" />
														</TD>
													</TR>

													<TR>
														<TD colspan="4">
															<div align="center">
																<p>
																	<html:submit property="action" value="Delete WSDL" styleClass="button" />
																	<html:submit property="action" value="Deploy" styleClass="button" />
															</div>
														</TD>
													</TR>

												</html:form>
											</TABLE>
										</fieldset>
									</TD>
								</TR>

							</TABLE>

						</div>
						<%@ include file="../end.jspf" %>