<%@ page isELIgnored="false" %>

<%@ page import="java.util.*" %>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
  session.removeAttribute("currentMenu");
  session.setAttribute("currentMenu", "log");
%>
<%@ include file="../head.jspf" %>

    <style type="text/css">
       .DEBUG,.INFO {}
       .WARNING,.ERROR {background-color:#FF3333; color:#FFFFFF;}
    </style>
    <script language="JavaScript">
        var contextRoot = '<%=contextRoot%>';
        var btnFilterL = "<bean:message key='log.filter'/>";
        var btnShowMsgL = "<bean:message key='log.showMsg'/>";
    </script>

    <style type="text/css" src="<%=contextRoot%>/css/jquery-ui-1.8.10.custom.css"></style>
    <style type="text/css" src="<%=contextRoot%>/js/datatable/css/demo_page.css"></style>
    <style type="text/css" src="<%=contextRoot%>/js/datatable/css/demo_table_jui.css"></style>
    <script type="text/javascript" src="<%=contextRoot%>/js/datatable/js/jquery.dataTables.js"></script>
    <script type="text/javascript" src="<%=contextRoot%>/js/jquery-ui-1.8.10.custom.min.js"></script>
    <script type="text/javascript" src="<%=contextRoot%>/log/main.js"></script>
<br/>
    <div class="ui-widget-header" style="margin-top:40px;">
        <div style="width:1100px;">
        <html:form action="/log/HandleLogAction" onsubmit="return checkForm(this);">
            <html:hidden styleId="skipValidation" property="skipValidation" value="true"/>
            <html:hidden styleId="methodToCall" property="methodToCall" value="Filter"/>
            <div style="float:left;margin-left:5px;margin-right:5px;">
                <label for="service">Service</label><br>
                <html:text styleId="service" property="service"></html:text>
            </div>
            <div style="float:left;margin-right:5px;">
                <label for="system">System</label><br>
                <html:text styleId="system" property="system"></html:text>
            </div>
            <div style="float:left;margin-right:5px;">
                <label for="id">ID</label><br>
                <html:text styleId="id" property="id"></html:text>
            </div>
            <div style="float:left;margin-right:5px;">
                <label for="dateFrom">Date From:</label><br>
                <html:text styleId="dateFrom" property="dateFrom" style="width:10em;"></html:text>
                <script type="text/javascript">
                    jQuery(document).ready(function() {
                        jQuery("#dateFrom").dynDateTime({
                            showsTime: true,
                            ifFormat: "dd/MM/yyyy HH:mm",
                            align: "TL",
                            electric: false,
                            singleClick: true
                        });
                    });
                </script>
            </div>
            <div style="float:left;margin-right:5px;">
                <label for="dateTo">Date To:</label><br>
                <html:text styleId="dateTo" property="dateTo" style="width:10em;"></html:text>
                <script type="text/javascript">
                    jQuery(document).ready(function() {
                        jQuery("#dateTo").dynDateTime({
                            showsTime: true,
                            ifFormat: "dd/MM/yyyy HH:mm",
                            align: "TL",
                            electric: false,
                            singleClick: true
                        });
                    });
                </script>
            </div>
            <div style="float:left;margin-right:5px;">
                <label for="severity">Severity</label><br>
                <html:select styleId="severity" property="severity">
                    <html:option value=""></html:option>
                    <html:option value="ERROR">ERROR</html:option>
                    <html:option value="WARNING">WARNING</html:option>
                    <html:option value="INFO">INFO</html:option>
                    <html:option value="DEBUG">DEBUG</html:option>
                </html:select>
            </div>
            <br>
            <button class="ui-state-default ui-corner-all" type="submit">Go</button>
        </html:form>
        </div>
        <div style="width:200px; margin-left:1150px; margin-top:-35px;">
        <html:form action="/log/HandleLogAction" target="_blank">
            <html:hidden styleId="skipValidation" property="skipValidation" value="true"/>
            <html:hidden styleId="methodToCall" property="methodToCall" value="Download"/>
            <div style="float:left;margin-right:5px;">
                <label for="date">Date:</label><br>
                <html:text styleId="date" property="date" style="width:7em;"></html:text>
                <script type="text/javascript">
                    jQuery(document).ready(function() {
                        jQuery("#date").dynDateTime({
                            showsTime: true,
                            ifFormat: "dd/MM/yyyy",
                            align: "TL",
                            electric: false,
                            singleClick: true
                        });
                    });
                </script>
            </div>
            <br>
            <button class="ui-state-default ui-corner-all" type="submit">Download</button>
        </html:form>
        </div>
    </div>
    
      <br>
        <table id="tableResult" width="100%" border="1" class="display"  style=" font-size: 0.9em;">
          <thead>
                <tr>
                  <th>Source</th>
                  <th>Timestamp</th>
                  <th>Severity</th>
                  <th>Server</th>
                  <th>ID</th>
                  <th>System</th>
                  <th>Service</th>
                  <th>Operation</th>
                  <th>Message</th>
                  <th>Throwable</th>
                </tr>
              </thead>
          <tbody>
            <%
            int listSize = 0;

                List<List<String>> listLog = (List<List<String>>) session.getAttribute("iteratorListLog");
                if (listLog != null) {
                    listSize = listLog.size();
                    for (int i = 0; i < listSize; i++) {
                        List<String> listStr = listLog.get(i);
                        String msg = listStr.get(12);
                        String throwable = new String(listStr.get(13));
                        %>
                            <tr class="<%= listStr.get(3) %>">
                                <td><%= listStr.get(0) %></td>        <!-- SOURCE -->
                                <%-- <td><%= listStr.get(1) %></td> --%>        <!-- ID MESSAGE -->
                                <td NOWRAP><%= listStr.get(2) %></td> <!-- TSTAMP -->
                                <td><%= listStr.get(3) %></td>        <!-- PRIO -->
                                <%-- <td><%= listStr.get(4) %></td> --%>        <!-- IPRIO -->
                                <%-- <td><%= listStr.get(5) %></td> --%>        <!-- CAT -->
                                <%-- <td><%= listStr.get(6) %></td> --%>        <!-- THREAD-->
                                <td NOWRAP><%= listStr.get(7) %></td> <!-- SERVER -->
                                <td NOWRAP><%= listStr.get(8) %></td> <!-- ID-->
                                <td><%= listStr.get(9) %></td>        <!-- SYSTEM-->
                                <td><%= listStr.get(10) %></td>       <!-- SERVICE-->
                                <td><%= listStr.get(11) %></td>       <!-- OPERATION-->
                        <%  if(msg==null){ %>
                              <td align="let"><img src="<%=contextRoot%>/images/openMsg.png" onclick="showMessage('Msg','<%=listStr.get(1)%>');" style="cursor:pointer;"></td>
                        <% } else{ %>
                                <td><%= msg %></td>  <!-- MSG -->
                        <% }
                           if(throwable.equals("1")){ %>
                           <td align="left"><img src="<%=contextRoot%>/images/scroll_error.png" onclick="showMessage('Exc','<%=listStr.get(1)%>');" style="cursor:pointer;"></td>
                      <% } else {%>
                                <td>&nbsp;</td>
                        <% } %>
                            </tr>
                        <%
                    }
                }
            %>
        </tbody>
        </table>

        <div id="dialog-message" title="Message" style="display:none; height:400; width:800; overflow-y: auto;">
            <p>
                <div id="textMessage"></div>
            </p>
        </div>

<%@ include file="../end.jspf" %>