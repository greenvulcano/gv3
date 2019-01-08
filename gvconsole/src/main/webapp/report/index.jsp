<%@ page isELIgnored="false" %>

<%@ page import="java.util.*" %>
<%@ page import="org.apache.struts.util.LabelValueBean" %>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/birt.tld" prefix="birt" %>

<%
  session.removeAttribute("currentMenu");
  session.setAttribute("currentMenu", "report");
%>
<%@ include file="../head.jspf" %>
    <style type="text/css">
       hr {margin-top: 620px;}
    </style>
    
        <script language="JavaScript">
        var buttonPressed;

        function buttonPress(num) {
          this.buttonPressed = num;
        }

        function checkForm(form) {
            return true;
        }

        function reportChanged() {
            document.forms[0].action = '<%=contextRoot%>/report/HandleReportAction.do?skipValidation=true&methodToCall=<bean:message key="birtreport.setparams"/>';
            document.forms[0].submit();
        }

        function groupChanged() {
            document.forms[0].action = '<%=contextRoot%>/report/HandleReportAction.do?skipValidation=true&methodToCall=<bean:message key="birtreport.setreports"/>';
            document.forms[0].submit();
        }

        </script>
<br/>
<div class="titleSection">
												<h1>Report</h1>
											</div>
<div style="float: left;">
    <div  style="float: left; width: 300px">
        <html:form action="/report/HandleReportAction" onsubmit="return checkForm(this);">
            <html:hidden styleId="skipValidation" property="skipValidation" value="true"/>
            <html:hidden styleId="methodToCall" property="methodToCall" value=""/>
            <table cellspacing=0 cellpadding=2>
                <tr>
                    <th bgcolor="lightgrey">Gruppi</th>
                </tr>
                <tr>
                    <td valign="top" nowrap>
                        <html:select styleId="group" property="group" onchange="groupChanged()">
                            <html:options collection="listGroup" property="value" labelProperty="label" />
                        </html:select>
                    </td>
                </tr>
                <tr>
                    <th bgcolor="lightgrey">Report</th>
                </tr>
                <tr>
                    <td valign="top" nowrap>
                        <html:select styleId="report" property="report" onchange="reportChanged()">
                            <html:options collection="listReport" property="value" labelProperty="label" />
                        </html:select>
                    </td>
                </tr>
            </table>
        </html:form>
        <br>
        <birt:parameterPage id="pp" name="parameters" reportDesign="${BirtReportForm.map.reportConfig}"
                            pattern="frameset" isCustom="true" target="birtViewer"
                            showToolBar="true" showNavigationBar="true"
                            showTitle="true" forceOverwriteDocument="true">
            <table cellspacing=0 cellpadding=2>
                <tr>
                    <th colspan="2" bgcolor="lightgrey">Parametri</th>
                </tr>
            <c:forEach var="parametro" items="${listParams}" varStatus="p_status">
                <tr>
                    <td valign="top" nowrap>${parametro.label}:<c:if test="${parametro.required}">*</c:if></td>
                    <td valign="top" nowrap>
                <c:set var="parName" scope="page" value="${parametro.name}"/>
                <c:set var="parType" scope="page" value="${parametro.type}"/>
                <c:set var="parCtrlType" scope="page" value="${parametro.controlType}"/>
                <c:choose>
                    <c:when test="${parametro.defaultParam}">
                        <c:choose>
                            <c:when test="${parCtrlType == 'TEXT'}">
                                <c:choose>
                                    <c:when test="${parType == 'DATE'}">
                                        <script type="text/javascript">
                                            jQuery(document).ready(function() {
                                                jQuery("#${parName}").dynDateTime({
                                                    showsTime: false,
                                                    ifFormat: "${parametro.format}",
                                                    align: "TL",
                                                    electric: false,
                                                    singleClick: true
                                                });
                                            });
                                        </script>
                                    </c:when>
                                    <c:when test="${parType == 'DATE_TIME'}">
                                        <script type="text/javascript">
                                            jQuery(document).ready(function() {
                                                jQuery("#${parName}").dynDateTime({
                                                    showsTime: true,
                                                    ifFormat: "${parametro.format}",
                                                    align: "TL",
                                                    electric: false,
                                                    singleClick: true
                                                });
                                            });
                                        </script>
                                    </c:when>
                                </c:choose>
                            </c:when>
                        </c:choose>
                        <birt:paramDef name="${parName}" id="${parName}" />
                    </c:when>
                    <c:otherwise>
                        <c:choose>
                            <c:when test="${parCtrlType == 'TEXT'}">
                                <c:choose>
                                    <c:when test="${parType == 'DATE'}">
                                        <script type="text/javascript">
                                            jQuery(document).ready(function() {
                                                jQuery("#${parName}").dynDateTime({
                                                    showsTime: false,
                                                    ifFormat: "${parametro.format}",
                                                    align: "TL",
                                                    electric: false,
                                                    singleClick: true
                                                });
                                            });
                                        </script>
                                    </c:when>
                                    <c:when test="${parType == 'DATE_TIME'}">
                                        <script type="text/javascript">
                                            jQuery(document).ready(function() {
                                                jQuery("#${parName}").dynDateTime({
                                                    showsTime: true,
                                                    ifFormat: "${parametro.format}",
                                                    align: "TL",
                                                    electric: false,
                                                    singleClick: true
                                                });
                                            });
                                        </script>
                                    </c:when>
                                </c:choose>
                                <input type="text" name="${parName}" id="${parName}" value="${parametro.defValue}"/>
                            </c:when>
                            <c:when test="${parCtrlType == 'SELECT'}">
                                <select name="${parName}" id="${parName}">
                                    <c:forEach var="option" items="${sessionScope[parName]}" varStatus="o_status">
                                         <option value="${option.value}" <c:if test="${option.value == parametro.defValue}">selected</c:if>>${option.label}</option>
                                    </c:forEach>
                                </select>
                            </c:when>
                        </c:choose>
                    </c:otherwise>
                 </c:choose>
                    </td>
                </tr>
            </c:forEach>
            </table>
            <br>
            <input type="submit" name="submit" value="<bean:message key="birtreport.makereport"/>"/>
            <br>
        </birt:parameterPage>
    </div>
    <div style="float: left;">
        <iframe name="birtViewer" frameborder="yes"  scrolling = "auto"  style='height:600px;width:900px;' ></iframe>
    </div>
</div>

<%@ include file="../end.jspf" %>