<%@ page language="java" pageEncoding="ISO-8859-1"%>
	<%@ page session = "true" %>
		<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

			<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
				<%@ taglib uri="http://java.sun.com/jsp/jstl/xml"  prefix="x" %>
					<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
						<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


							<%
  session.removeAttribute("currentMenu");
  session.setAttribute("currentMenu", "utility");
%>

								<%@include file="../head.jspf"%>
									<%@ page import="java.util.List" %>
										<%@ page import="java.util.ArrayList" %>
											<%@ page import="java.util.List" %>
												<%@ page import="it.greenvulcano.gvesb.gvconsole.deploy.*" %>
													<%@ page import="it.greenvulcano.configuration.XMLConfig" %>

														<div class="titleSection">
															<h1>Export Configuration file</h1>
														</div>

														<div class="gv-container gv-height-min">
															<div class="ui-tabs ui-widget ui-widget-content ui-corner-all  gv-side">
																<ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
																	<li class="tab-title-gv">Choose service</li>
																</ul>

																<div class="gv-side ui-tabs ui-widget ui-widget-content ui-corner-all backGv ">
																	<div class="side-left">
																		<html:form action="/fileExport" method="post">

																			<table cellpadding="4" cellspacing="1">
																				</tr>

																				<%
   GVConfig gvConfig = new GVConfig("server", XMLConfig.getURL("GVCore.xml"),XMLConfig.getURL("GVAdapters.xml"));
   List<String> listaServizi = gvConfig.getListaServizi();
   for(String servizio:listaServizi){
   %>
																					<tr>
																						<td>
																							<input type="checkbox" name="<%out.write(servizio);%>" value="<%out.write(servizio);%>">
																							<%out.write(servizio);%>
																								</input>
																						</td>
																					</tr>
																					<%
   }
   %>


																						<tr>
																			</table>
																		</html:form>

																	</div>
																	<div class="side-right">
																		<div class="ui-widget-header ui-corner-all" style="margin:100px auto 0 auto; width: 200px; text-align: center;padding:30px">
																			<a href="javascript:goExport();" title="Download config" style="text-decoration:none;">
																				<img alt="Download" src="../images/downConfig.png" border="0" align="middle" />
																				<br/> Download config
																			</a>
																		</div>
																		<script>
																			function goExport() {
																				document.forms[0].submit();
																			}
																		</script>
																	</div>

																</div>

															</div>
														</div>


														<%@include file="../end.jspf"%>