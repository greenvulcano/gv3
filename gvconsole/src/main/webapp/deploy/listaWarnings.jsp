<%@ page language="java" pageEncoding="ISO-8859-1"%>
	<%@ page session = "true" %>
		<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

			<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
				<%@ taglib uri="http://java.sun.com/jsp/jstl/xml"  prefix="x" %>
					<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
						<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
							<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

							<HTML>

							<HEAD>
								<TITLE>Administration functions</TITLE>
								<style>
									table,
									th,
									td {
										border: 1px solid black;
										border-collapse: collapse;
									}
									
									th,
									td {
										padding: 5px;
										text-align: left;
									}
								</style>
							</HEAD>

							<BODY>
								<%@ include file="../head.jspf" %>
									<%@ page import="java.util.List" %>
										<%@ page import="java.util.ArrayList" %>
											<%@ page import="java.util.List" %>
												<%@ page import="it.greenvulcano.gvesb.gvconsole.deploy.*" %>
													<%@ page import="org.w3c.dom.Node" %>
														<%@ page import="org.w3c.dom.NodeList" %>
															<%@ page import="it.greenvulcano.util.xml.XMLUtils" %>
																<%@ page import="it.greenvulcano.util.xml.XMLUtilsException" %>
																	<%@ page import="org.custommonkey.xmlunit.DetailedDiff" %>
																		<%@ page import="org.custommonkey.xmlunit.Diff" %>
																			<%@ page import="org.custommonkey.xmlunit.Difference" %>
																				<%@ page import="org.custommonkey.xmlunit.XMLUnit" %>
																					<div class="titleSection">
																						<h1>Deploy New Service</h1>
																					</div>
																					<div class="ui-tabs ui-widget ui-widget-content ui-corner-all">
																						<ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
																							<li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active"><a href="<%=contextRoot%>/deploy/listaWarnings.jsp">Report Deploy</a></li>
																							<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/deploy/listaServizi.jsp">Core</a></li>
																							<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/deploy/listaAdapter.jsp">Adapter</a></li>
																							<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/deploy/listaFile.jsp">File</a></li>


																						</ul>

																						<div class="ui-tabs ui-widget ui-widget-content ui-corner-all backGv">
																							<div align="center">

																								<!-- JS -->
																								<!--script type="text/javascript" src="../js/mergely-3.4.4/js/editor.js"></script-->
																								<script type="text/javascript" src="../js/mergely-3.4.4/js/farbtastic.js"></script>
																								<script type="text/javascript" src="../js/mergely-3.4.4/js/gatag.js"></script>
																								<script type="text/javascript" src="../js/mergely-3.4.4/js/wicked-ui.js"></script>
																								<script type="text/javascript" src="../js/mergely-3.4.4/js/jquery.tipsy.js"></script>
																								<script type="text/javascript" src="../js/mergely-3.4.4/lib/searchcursor.js"></script>


																								<div class="ui-tabs ui-widget ui-widget-content ui-corner-all">
																									<ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
																										<li class="tab-title-gv">Services</li>
																									</ul>

																									<div class="ui-tabs ui-widget ui-widget-content ui-corner-all backGv">
																										<table cellpadding="4" cellspacing="2">

																											<tr>
																												<th>Service</th>
																												<th></th>
																											</tr>
																											<%
   try { 
    GVDeploy deploy = (GVDeploy)session.getAttribute("deploy");
	GVConfig gvConfigZip = deploy.getZipGVConfig();
	GVConfig gvConfigServer = deploy.getServerGVConfig();
	String commento ="";
    Map<String,Node> listaServiziZip = gvConfigZip.getListaNodeServizi();
			Map<String,Node> listaServiziServer = gvConfigServer.getListaNodeServizi();
			for(String servizio:listaServiziZip.keySet()){
				Node nodeServiceZip = listaServiziZip.get(servizio);
				Node nodeServiceServer = listaServiziServer.get(servizio);
				if(nodeServiceServer==null)
					commento = "New";
				else{
				    List<String> diffs = new ArrayList<String>();
				    if(nodeServiceServer==null)
					  commento = "New";
				    else{
					  XmlDiff xmlDiff = new XmlDiff();
					  List<String> listDiff= xmlDiff.compareXML(nodeServiceZip, nodeServiceServer);
					  if(listDiff.size()>0)
						commento = "update:"+listDiff.toString();
					  else
						commento = "No change";
				   }
			     }
		 %>
																												<tr>
																													<td>
																														<%out.write(servizio);%>
																													</td>
																													<td>
																														<%out.write(commento);%>
																													</td>
																												</tr>
																												<%
	   }
   }
   catch (Exception e) {
			e.printStackTrace();
   }
 %>
																										</table>
																									</div>
																								</div>

																								<div class="ui-tabs ui-widget ui-widget-content ui-corner-all">
																									<ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
																										<li class="tab-title-gv">Services</li>
																									</ul>

																									<div class="ui-tabs ui-widget ui-widget-content ui-corner-all backGv">
																										<table cellpadding="4" cellspacing="2">

																											<tr>
																												<th>GVCall</th>
																												<th>in</th>
																											</tr>
																											<%
   try { 
    GVDeploy deploy = (GVDeploy)session.getAttribute("deploy");
	GVConfig gvConfigZip = deploy.getZipGVConfig();
	GVConfig gvConfigServer = deploy.getServerGVConfig();
    Map<String,Node> listaServiziZip = gvConfigZip.getListaNodeServizi();
	Map<String,Node> listaServiziServer = gvConfigServer.getListaNodeServizi();
	for(String servizio:listaServiziZip.keySet()){
		List<String> listaServizi = gvConfigServer.getListaServiziGVCall(servizio);
		if(listaServizi.size()>0){
		 %>
																												<tr>
																													<td>
																														<%out.write(servizio);%>
																													</td>
																													<td>
																														<%out.write(listaServizi.toString());%>
																													</td>
																												</tr>
																												<%
	   }
	 }
   }
   catch (Exception e) {
			e.printStackTrace();
   }
 %>
																										</table>
																									</div>
																								</div>



																								<div class="ui-tabs ui-widget ui-widget-content ui-corner-all">
																									<ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
																										<li class="tab-title-gv">Vcl Operation</li>
																									</ul>

																									<div class="ui-tabs ui-widget ui-widget-content ui-corner-all backGv">
																										<table cellpadding="4" cellspacing="2">

																											<tr>
																												<th>Operation</th>
																												<th>Used in</th>
																											</tr>
																											<%
   String key = null;					
   try { 
    GVDeploy deploy = (GVDeploy)session.getAttribute("deploy");
	GVConfig gvConfigZip = deploy.getZipGVConfig();
	GVConfig gvConfigServer = deploy.getServerGVConfig();
    Map<String,Node> listaNodeServizio = gvConfigZip.getListaNodeServizi();
	for(String servizio:listaNodeServizio.keySet()){
	Node nodeServizio = listaNodeServizio.get(servizio);
	NodeList operations = XMLUtils.selectNodeList_S(nodeServizio,"Operation/*/*[@op-type='call']");
	for(int i=0;i<operations.getLength();i++){
	  String system = XMLUtils.get_S(operations.item(i), "@id-system");
      String operation = XMLUtils.get_S(operations.item(i), "@operation-name");
      key = servizio + ":" + operation + ":" + system;
	  String channel = XMLUtils.selectSingleNode_S(nodeServizio,"Operation/Participant[@id-system='"+system+"']/@id-channel").getNodeValue();
	  List<String> listaServizi = gvConfigServer.getListaServiziVclOP(system, channel,operation);
	  if(listaServizi.size()>0){
		 %>
																												<tr>
																													<td>
																														<%out.write(system+"/"+channel+"/"+operation);%>
																													</td>
																													<td>
																														<%out.write(listaServizi.toString());%>
																													</td>
																												</tr>
																												<%
         }
	   }
	 }
   }
   catch (Exception e) {
	   System.err.println("Error searching Partecipant for " + key);
		e.printStackTrace();
   }
 %>
																										</table>
																									</div>
																								</div>
																								<div class="ui-tabs ui-widget ui-widget-content ui-corner-all">
																									<ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
																										<li class="tab-title-gv">Transformations</li>
																									</ul>

																									<div class="ui-tabs ui-widget ui-widget-content ui-corner-all backGv">
																										<table>

																											<tr>
																												<th>Transformation</th>
																												<th>Used in</th>
																											</tr>
																											<%
   try { 
    GVDeploy deploy = (GVDeploy)session.getAttribute("deploy");
	GVConfig gvConfigZip = deploy.getZipGVConfig();
	GVConfig gvConfigServer = deploy.getServerGVConfig();
    Map<String,Node> listaTrasf = gvConfigZip.getListaTrasformazioni();
	for(String trasf:listaTrasf.keySet()){
	List<String> listaServiziTrasf = gvConfigServer.getListaServiziTrasf(trasf);
	if(listaServiziTrasf.size()>0){
		 %>
																												<tr>
																													<td>
																														<%out.write(trasf);%>
																													</td>
																													<td>
																														<%out.write(listaServiziTrasf.toString());%>
																													</td>
																												</tr>
																												<%
      }
	 }
   }
   catch (Exception e) {
			e.printStackTrace();
   }
 %>
																										</table>
																									</div>
																								</div>
																								<html:form action="/deploy" method="post" enctype="multipart/form-data">

																									<div class="buttonGv" style="margin-top:5px;">
																										<html:submit>deploy</html:submit>
																									</div>

																								</html:form>
																							</div>
																						</div>
																					</div>
																					<%@ include file="../end.jspf" %>

							</BODY>

							</HTML>