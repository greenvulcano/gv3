<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="/WEB-INF/std/c.tld" prefix="c" %>
<jsp:useBean class="it.greenvulcano.gvesb.gvconsole.webservice.forms.GVWebServiceForm" id="GVWebServiceForm" scope="session" />

<%
  session.removeAttribute("currentMenu");
  session.setAttribute("currentMenu", "webservice");
%>	
<%@ include file="../head.jspf" %>
<script type="text/javascript">
<!--
$(function() {$('.infoqtip').tipsy({fade: true,gravity: 'w'});});
//-->
</script>
<div class="ui-tabs ui-widget ui-widget-content ui-corner-all" style="width: 1080px; margin: 50px auto;">
<ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
    <li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active"><a>General&nbsp;Parameter</a></li>
	<li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/webservice/managementwsdl.jsp">WSDL</a></li>
    <li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/webservice/managementuddi.jsp">UDDI</a></li>
    <li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/ode/processes.jsp">BPEL Deployed Processes</a></li>
    <li class="ui-state-default ui-corner-top"><a href="<%=contextRoot%>/ode/instances.jsp">BPEL DCurrently Available Instances</a></li>


</ul>
<!-- CONFIGURAZIONE GENERALE BUSINESS -->
<TABLE cellpadding="0" cellspacing="2"> 
		<TR > 
			<TD>
				<fieldset class="ui-widget-header  ui-corner-all">
					<legend class="ui-widget-header ui-corner-all">General configuration of business web services</legend>
					<div id="tooltip1"></div>
					<TABLE cellpadding="0" cellspacing="2"> 
						<TR> 
							<TD >authenticated-http-soap-address:</TD> 
							<TD class="input160"><bean:write name="GVWebServiceForm" property="businessWebServicesBean.authenticatedHttpSoapAddress"/><TD > 
								<a class="infoqtip" href="#" title="URL per la costruzione del soap:address per i web services che richiedono autenticazione esposti con protocollo http."><IMG src="<%=contextRoot%>/images/info16x16.png"></IMG></a>
							</TD>
						</TR>
						<TR> 
							<TD>authenticated-https-soap-address:</TD>
							<TD class="input160"><bean:write name="GVWebServiceForm" property="businessWebServicesBean.authenticatedHttpsSoapAddress"/></TD>
							<TD>
								<a class="infoqtip" href="#" title="URL per la costruzione del soap:address per i web services che richiedono autenticazione esposti con protocollo https."><IMG src="<%=contextRoot%>/images/info16x16.png"></IMG></a>
							</TD>
						</TR>		
						<TR > 
							<TD>http-soap-address:</TD>
							<TD class="input160"><bean:write name="GVWebServiceForm" property="businessWebServicesBean.httpSoapAddress"/></TD>
							<TD>
								<a class="infoqtip" href="#" title="URL per la costruzione del soap:address per i web services che non richiedono autenticazione esposti con protocollo http."><IMG src="<%=contextRoot%>/images/info16x16.png"></IMG></a>
							</TD>
						</TR>
						<TR > 
							<TD>https-soap-address:</TD>
							<TD class="input160"><bean:write name="GVWebServiceForm" property="businessWebServicesBean.httpsSoapAddress"/></TD>
							<TD>
								<a class="infoqtip" href="#" title="URL per la costruzione del soap:address per i web services che non richiedono autenticazione esposti con protocollo https."><IMG src="<%=contextRoot%>/images/info16x16.png"></IMG></a>
							</TD>
						</TR>
						<TR >
							<TD>services-directory:</TD>
							<TD class="input160"><bean:write name="GVWebServiceForm" property="businessWebServicesBean.servicesDirectory"/></TD>
							<TD>
								<a class="infoqtip" href="#" title="Directory contenente i servizi pubblicati."><IMG src="<%=contextRoot%>/images/info16x16.png"></IMG></a>
							</TD>
						</TR>
						<TR >
							<TD>wsdl-directory:</TD>
							<TD class="input160"><bean:write name="GVWebServiceForm" property="businessWebServicesBean.wsdlDirectory"/></TD>
							<TD>
								<a class="infoqtip" href="#" title="Directory contenente i WSDL generati."><IMG src="<%=contextRoot%>/images/info16x16.png"></IMG></a>
							</TD>
						</TR>
					</TABLE>
				</fieldset>
		   </TD>
		</TR>
	</TABLE>
<!-- CONFIGURAZIONE UDDI Registry -->
	 <TABLE cellpadding="0" cellspacing="2"> 
		<TR>  
			<TD>
			<fieldset class="ui-widget-header ui-corner-all">
					<legend class="ui-widget-header ui-corner-all">UDDI Registry's configuration</legend>
			  <div id="tooltip3"></div>
			  <logic:empty name="GVWebServiceForm" property="uddiInfoBean">
				  <TABLE cellpadding="0" cellspacing="2"> 
					<TR >
						<TD class="input160" align="center">NOT CONFIGURED</TD> 
					</TR>
				  </TABLE>
			  </logic:empty>
			  <logic:notEmpty name="GVWebServiceForm" property="uddiInfoBean">
				  <TABLE cellpadding="0" cellspacing="2"> 
					<TR > 
					  <TD >Registry name:</TD> 
					  <TD class="input160"><bean:write name="GVWebServiceForm" property="uddiInfoBean.id"/></TD> 
					  <TD>
					  	<a class="infoqtip" href="#" title="UDDI Registry Identifier."><IMG src="<%=contextRoot%>/images/info16x16.png"></IMG></a> 
					  </TD> 
					</TR>
					<TR >
					  <TD>Registry publish url:</TD>
					  <TD class="input160"><bean:write name="GVWebServiceForm" property="uddiInfoBean.urlp"/></TD>
					  <TD>
					  	<a class="infoqtip" href="#" title="UDDI Registry URL to publish business services."><IMG src="<%=contextRoot%>/images/info16x16.png"></IMG></a> 
					  </TD>
					</TR>
					<TR >
					  <TD>Registry inquiry url:</TD>
					  <TD class="input160"><bean:write name="GVWebServiceForm" property="uddiInfoBean.urli"/></TD>
					  <TD>
					  	<a class="infoqtip" href="#" title="UDDI Registry URL to inquiry for services."><IMG src="<%=contextRoot%>/images/info16x16.png"></IMG></a> 
					  </TD>
					</TR>
					<TR >
					  <TD>Organization name:</TD>
					  <TD class="input160"><bean:write name="GVWebServiceForm" property="uddiInfoBean.organization"/></TD>
					  <TD>
					  	<a class="infoqtip" href="#" title="Business organization identifier."><IMG src="<%=contextRoot%>/images/info16x16.png"></IMG></a> 
					  </TD>
					</TR>
				  </TABLE>
			  </logic:notEmpty>
        </fieldset></TD>
      </TR>
    </TABLE>
</div>
<%@ include file="../end.jspf" %>
