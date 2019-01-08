<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
	<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
		<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
			<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>

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
								<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/webservice/managementwsdl.jsp">WSDL</a></li>
								<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/webservice/managementuddi.jsp">UDDI</a></li>
								<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/ode/processes.jsp">BPEL Deployed Processes</a></li>
								<li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active"><a href="<%=contextRoot%>/ode/instances.jsp">BPEL Deployed Processes</a></li>
							</ul>
							<link rel="stylesheet" href="css/style.css" type="text/css" media="screen, projection" />
							<link rel="stylesheet" href="js/yui/css/container.css" media="screen, projection" />
							<link rel="stylesheet" href="js/yui/css/button.css" media="screen, projection" />
							<link rel="stylesheet" href="js/bubbling/assets/accordion.css" media="screen, projection" />
							<script type="text/javascript" src="js/WSRequest.js">
							</script>
							<script type="text/javascript" src="js/ProcessManagementAPI.js">
							</script>
							<script type="text/javascript" src="js/InstanceManagementAPI.js">
							</script>
							<script type="text/javascript" src="js/yui/utilities.js">
							</script>
							<script type="text/javascript" src="js/yui/container.js"></script>
							<script type="text/javascript" src="js/yui/button.js"></script>
							<script type="text/javascript" src="js/yui/button.js">
							</script>
							<script type="text/javascript" src="js/yui/animation.js">
							</script>
							<script type="text/javascript" src="js/bubbling/bubbling.js">
							</script>
							<script type="text/javascript" src="js/bubbling/accordion.js">
							</script>
							<script type="text/javascript" src="js/ODE.js">
							</script>
							<script type="text/javascript">
								function init() {
									org.apache.ode.InstanceHandling.populateContent();
									setInterval('org.apache.ode.InstanceHandling.populateContent()', 15000);
								}

								YAHOO.util.Event.onDOMReady(init);
							</script>
							<style type="text/css">
								button {
									background: transparent url(../button/assets/add.gif) no-repeat scroll 10% 50%;
									padding-left: 2em;
								}
								
								.link {
									margin-left: 5px;
									color: blue;
								}
								
								.myAccordion .yui-cms-accordion .yui-cms-item {
									margin-bottom: 10px;
								}
								
								.bd {
									background: #FFFFFF none repeat scroll 0 0;
								}
							</style>
							</head>

							<body>
								<div id="wrapper">
									<dir id="insideW">
										<div id="insideL">
											<div id="content" class="yui-skin-sam">
											</div>
										</div>
									</dir>
								</div>
						</div>
						<%@include file="../end.jspf"%>