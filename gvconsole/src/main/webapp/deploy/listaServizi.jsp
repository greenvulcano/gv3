<%@ page language="java" pageEncoding="ISO-8859-1"%>
	<%@ page session = "true" %>
		<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

			<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
				<%@ taglib uri="http://java.sun.com/jsp/jstl/xml"  prefix="x" %>
					<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
						<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

							<%@ include file="../head.jspf" %>
								<%@ page import="java.util.List" %>
									<%@ page import="java.util.ArrayList" %>
										<%@ page import="java.util.List" %>
											<%@ page import="it.greenvulcano.gvesb.gvconsole.deploy.*" %>
												<%@ page import="it.greenvulcano.configuration.XMLConfig" %>
													<div class="titleSection">
														<h1>Deploy New Service</h1>
													</div>
													<div class="ui-tabs ui-widget ui-widget-content ui-corner-all">
														<ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
															<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/deploy/listaWarnings.jsp">Report Deploy</a></li>
															<li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active"><a>Core</a></li>
															<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/deploy/listaAdapter.jsp">Adapter</a></li>
															<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/deploy/listaFile.jsp">File</a></li>

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
															<table cellpadding="4" cellspacing="2">
															</table>
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

															<%
   String xmlZip = null;
   String xmlServer =null;
   try {
     GVDeploy deploy = (GVDeploy)session.getAttribute("deploy");
     GVConfig gvConfigServer = deploy.getServerGVConfig();
     GVConfig gvConfigZip = deploy.getZipGVConfig();
     List<String> listaServizi = gvConfigZip.getListaServizi();
     xmlZip = gvConfigZip.getGvCore(listaServizi,false).replaceAll("'","\"").replaceAll("\n","\\\\n'\\\n+'").replaceAll("Script","GVScript");
     xmlServer = gvConfigServer.getGvCore(listaServizi,false).replaceAll("'","\"").replaceAll("\n","\\\\n'\\\n+'").replaceAll("Script","GVScript");
   }
   catch (Exception e) {
			e.printStackTrace();
   }
%>

															$(document).ready(function () {
																	//var editor = $('#compare');
																	//console.log(editor);
																	$('#compare').mergely({
																		editor_height: '300px',
																		editor_width: '46%',
																		autoresize: 'false',
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


																}

															);

															function mergeRight() {
																console.log('test');
																$('#compare').mergely("scrollToDiff", "next");
															};

															function mergeLeft() {
																console.log('test2');
																$('#compare').mergely("scrollToDiff", "prev");
															};
														</script>
													</div>
													<%@ include file="../end.jspf" %>
														</BODY>

														</HTML>