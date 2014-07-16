
<%
	String contextRoot=request.getContextPath();
%>
 
<HTML>

	<HEAD>
		<TITLE> GreenVulcano Administrator </TITLE>
		<script type="text/javascript" src="<%=contextRoot%>/js/login/prettyForm.js"></script>
		<link rel="stylesheet" type="text/css" href="<%=contextRoot%>/css/login-style.css"/>
	</HEAD>
	<body onload="prettyForms()">
	<div id="login-container" align="center">
		<form id="myForm" method="POST" action="j_security_check">
			<div style="margin-bottom:25px;text-align: center;"><h3>GreenVulcano Administrator</h3></div>
			<p>
				<label><strong>User name: </strong></label><input name="j_username" type="text"/>
				<br class="clearAll" /><br />
			</p>
			<p>
				<label><strong>Password: </strong></label><input name="j_password" type="password" />
				<br class="clearAll" />
			</p>
			<div style="margin:30 0  0 120">
			<p><input type="submit" value="Login" /></p>
			</div>
		</form>
	</div>
	<div id="footer" align="center" style="top:800px;">
	 GreenVulcano ESB - Powered by <a href="http://www.greenvulcano.com/" class="footer" target="_top">GreenVulcano s.r.l.</a>
	 P.IVA 10544871006
	</div>
	</body>
</html>