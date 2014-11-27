<%session.removeAttribute("currentMenu");session.setAttribute("currentMenu", "monitoring");%>	
<%@ include file="../head.jspf" %>
<div class="titleSection"><h1>Monitoring</h1></div>

<script type="text/javascript">var contextRoot = '<%=contextRoot%>';</script>
<script src="<%=contextRoot%>/js/highcharts/highcharts.js" type="text/javascript"></script>
<!--[if IE]><script src="<%=contextRoot%>/js/highcharts/excanvas.compiled.js" type="text/javascript"></script><![endif]-->
<script src="<%=contextRoot%>/js/highcharts/monitoringHeap.js" type="text/javascript"></script>
<br/>
<div id="content" align="center">
	<div id="tabs"><ul></ul></div>
	<div id="content-tabs"></div>
</div>   

<%@ include file="../end.jspf" %>
