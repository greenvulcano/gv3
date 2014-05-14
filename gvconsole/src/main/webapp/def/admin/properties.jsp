<%@ include file="../../head.jspf" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max"%>

<%!
	//------------------------------------------------------------------------------
	// UTILITIES

    String toHtml(String str) {
        StringTokenizer tkzr = new StringTokenizer(str, "<>\"", true);
        StringBuffer ret = new StringBuffer();
        while(tkzr.hasMoreTokens()) {
            String tk = tkzr.nextToken();
            if(tk.equals("<")) ret.append("&lt;");
            else if(tk.equals(">")) ret.append("&gt;");
            else if(tk.equals("\"")) ret.append("&quot;");
            else ret.append(tk);
        }
        return ret.toString();
    }
%>
<%
	//------------------------------------------------------------------------------
	// PROPERTIES
%>
    <div class="titleSection"><h1>Java properties</h1>
    
    This page can be used in order to shows the properties set in the JVM running the servlet engine.<br>
    In case of unespected behaviours of the application checks that the properties are correct.
    <p></p></div>
<%
    Properties properties = System.getProperties();
    String propertyNames[] = new String[properties.size()];
    Enumeration e = properties.propertyNames();
    int idx = 0;
    while(e.hasMoreElements()) {
        propertyNames[idx] = (String)e.nextElement();
        idx++;
    }
    Arrays.sort(propertyNames);
%>

    <table class="ui-widget-header ui-corner-all">
        <tr class="search">
            <td><b>Property</b></td>
            <td></td>
            <td><b>Value</b></td>
        </tr>
        <tr class="search">
            <td colspan="3"><hr></td>
        </tr>
<%
        String color[] = new String[] {"#99FF66", "#99FF33"};
        for(idx = 0; idx < propertyNames.length; ++idx) {
            String propertyValue = properties.getProperty(propertyNames[idx]);
%>
            <tr  class="border"> 
                <td><%= toHtml(propertyNames[idx]) %></td>
                <td></td>
                <td><%= toHtml(propertyValue) %></td>
            </tr>
<%
        }
%>
    </table>

<%@ include file="../../end.jspf" %>
