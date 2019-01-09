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
							</HEAD>

							<BODY>
								<%@ include file="../head.jspf" %>
									<%@ page import="java.util.List" %>
										<%@ page import="java.util.ArrayList" %>
											<%@ page import="java.util.List" %>
												<%@ page import="it.greenvulcano.gvesb.gvconsole.deploy.*" %>
													<%@ page import="it.greenvulcano.configuration.XMLConfig" %>
														<%@ page import="it.greenvulcano.util.metadata.PropertiesHandler" %>
															<div class="titleSection">
																<h1>Deploy New Service</h1>
															</div>
															<div class="ui-tabs ui-widget ui-widget-content ui-corner-all">
																<ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
																	<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/deploy/listaWarnings.jsp">Report Deploy</a></li>
																	<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/deploy/listaServizi.jsp">Core</a></li>
																	<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/deploy/listaAdapter.jsp">Adapter</a></li>
																	<li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active"><a>File</a></li>
																</ul>
																<div class="ui-tabs ui-widget ui-widget-content ui-corner-all backGv">
																	<div class="arrow-content-gv">
																		<span class="arrow-content-span-gv-left">
																		
																	<button onclick="mergeLeft()"class="button-content-span-gv-left"><i class="fa fa-arrow-up"></i></button>
																		<h6 class="previousTitleLeft">Previous difference </h6>
																    </span>
																		<span class="arrow-content-span-gv-right">
																	<button onclick="mergeRight()"class="button-content-span-gv-right"><i class="fa fa-arrow-down"></i></button>
																		<h6 class="previousTitleRight">Next difference </h6>
																	 </span>

																	</div>
																	<div id="mergely-resizer">
																		<div id="compare">
																		</div>
																	</div>


																	<script src="../js/mergely-3.4.4/lib/jquery-3.2.1.min.js" type="text/javascript"></script>
																	<script type="text/javascript" src="../js/mergely-3.4.4/lib/codemirror.js"></script>
																	<link type="text/css" rel="stylesheet" href="../js/mergely-3.4.4/css/codemirror.css" />

																	<!-- Requires Mergely -->
																	<script type="text/javascript" src="../js/mergely-3.4.4/lib/mergely.js"></script>
																	<!-- CSS-->
																	<link type="text/css" rel="stylesheet" href="../js/mergely-3.4.4/css/mergely.css" />
																	<link type="text/css" rel="stylesheet" href="../js/mergely-3.4.4/css/editor.css" />
																	<link type="text/css" rel="stylesheet" href="../js/mergely-3.4.4/css/farbtastic.css" />
																	<link type="text/css" rel="stylesheet" href="../js/mergely-3.4.4/css/tipsy.css" />
																	<link type="text/css" rel="stylesheet" href="../js/mergely-3.4.4/css/wicked-ui.css" />
																	<link rel="stylesheet" href="../js/mergely-3.4.4/css/font-awesome.min.css">

																	<!-- JS -->
																	<!--script type="text/javascript" src="../js/mergely-3.4.4/js/editor.js"></script-->
																	<script type="text/javascript" src="../js/mergely-3.4.4/js/farbtastic.js"></script>
																	<script type="text/javascript" src="../js/mergely-3.4.4/js/gatag.js"></script>
																	<script type="text/javascript" src="../js/mergely-3.4.4/js/wicked-ui.js"></script>
																	<script type="text/javascript" src="../js/mergely-3.4.4/js/jquery.tipsy.js"></script>
																	<script type="text/javascript" src="../js/mergely-3.4.4/lib/searchcursor.js"></script>
																	<script type="text/javascript">
																		var key = '';
																		var isSample = key == 'usaindep';
																	</script>

																	<form action="/gvconsole/deploy/listaFile.jsp" method="get">
																		<table cellpadding="4" cellspacing="2">
																			<%
   String fileName = request.getParameter("fileName");
   String trasf = request.getParameter("key");
   String xmlZip = "";
   String xmlServer ="";
   try { 
     GVDeploy deploy = (GVDeploy)session.getAttribute("deploy");
	 GVConfig gvConfigZip = deploy.getZipGVConfig();
	 GVConfig gvConfigServer = deploy.getServerGVConfig();
     Map<String, String> listaFileXslZip = gvConfigZip.getListaFileXsl();
	 for(String key:listaFileXslZip.keySet()){
		 %>
																				<tr>
																					<td>
																						<a href="javascript:{}" onclick="invocaViewFile('<%out.write(listaFileXslZip.get(key));%>','<%out.write(key);%>')">
																							<%out.write(listaFileXslZip.get(key));%>
																						</a>

																					</td>
																				</tr>
																				<%
	 }
	 if(fileName!=null){
		String appoDir =deploy.getAppoDir();
		String gvDir = PropertiesHandler.expand("${{gv.app.home}}", null);
		String dataSourceName = gvConfigZip.getDataSourceValueFromTrasf(trasf);				
		String dirDteZip = gvConfigZip.getDteDir(dataSourceName,"xsl");
		xmlZip = gvConfigZip.getXmlFile(appoDir+File.separator+dirDteZip+File.separator+fileName);
		xmlZip = xmlZip.replaceAll("'","\"").replaceAll("\n","\\\\n'\\\n+'").replaceAll("Script","GVScript");
		dataSourceName = gvConfigServer.getDataSourceValueFromTrasf(trasf);				
		String dirDteServer = gvConfigServer.getDteDir(dataSourceName,"xsl");
		System.out.println(gvDir+File.separator+dirDteServer+File.separator+fileName);
		xmlServer = gvConfigServer.getXmlFile(gvDir+File.separator+dirDteServer+File.separator+fileName);
		xmlServer = xmlServer.replaceAll("'","\"").replaceAll("\n","\\\\n'\\\n+'").replaceAll("Script","GVScript");
		System.out.println(xmlZip);
	 }
   }
   catch (Exception e) {
			e.printStackTrace();
   }
 %>
																					<script type="text/javascript">
																						var key = '';
																						var isSample = key == 'usaindep';
																						$(document).ready(function () {
																								//var editor = $('#compare');
																								//console.log(editor);
																								$('#compare').mergely({
																									editor_height: '300px',

																									editor_width: '46%',
																									cmsettings: {
																										mode: 'application/xml',
																										readOnly: true,
																										lineWrapping: false
																									},

																									lhs: function (setValue) {
																										setValue('<%=xmlZip%>');
																									},
																									rhs: function (setValue) {
																										setValue('<%=xmlServer%>');
																									}
																								});

																								function mergeRight() {
																									$('#compare').mergely("scrollToDiff", "next");
																								};

																								function mergeLeft() {
																									$('#compare').mergely("scrollToDiff", "prev");
																								};
																							}

																						);
																					</script>
																		</table>

																		<input type="hidden" name="fileName" />
																		<input type="hidden" name="key" />
																		<script>
																			function invocaViewFile(fileName, key) {
																				document.forms[0].fileName.value = fileName;
																				document.forms[0].key.value = key;
																				document.forms[0].submit();
																			}
																		</script>
																		<form>
																</div>
															</div>
															<%@ include file="../end.jspf" %>
							</BODY>

							</HTML>