<%@ page import="max.config.*, max.core.*, java.io.*, java.util.*" %>
<%@ include file="../head.jspf" %>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max"%>
<%
    Integer status_code = (Integer)request.getAttribute("javax.servlet.error.status_code");
    Class exception_type = (Class)request.getAttribute("javax.servlet.error.exception_type");
    String message = (String)request.getAttribute("javax.servlet.error.message");
    Throwable exception = (Throwable)request.getAttribute("javax.servlet.error.exception");
    String request_uri = (String)request.getAttribute("javax.servlet.error.request_uri");
    String servlet_name = (String)request.getAttribute("javax.servlet.error.servlet_name");

    Throwable exc1 = exception;
    while(message == null) {
        message = exc1.getMessage();
        if(message == null) {
            if(exc1 instanceof ServletException) {
                ServletException exc2 = (ServletException)exc1;
                exc1 = exc2.getRootCause();
            }
            else if(exc1 instanceof MaxException) {
                MaxException exc2 = (MaxException)exc1;
                exc1 = exc2.getNestedException();
            }
            else {
                message = "" + exc1;
            }
        }
        else {
            message = "" + exc1;
        }
    }

    if(status_code != null) {
        message = "[" + status_code + "] - " + message;
    }

    if(exception != null) {
%>
        <h1>Error</h1>

        <table>
            <tr>
                <td colspan=3><hr></td>
            </tr>
            <tr class="search">
                <td colspan=3>
                    <b><%= message %></b>
                </td>
            </tr>
            <tr>
                <td colspan=3><hr></td>
            </tr>
            <tr class="search">
                <td><nobr><i>Exception type</i></nobr></td>
                <td width=15></td>
                <td><%= exception_type %></td>
            </tr>
            <tr class="search">
                <td><nobr><i>Request URI</i></nobr></td>
                <td></td>
                <td><a href="<%= request_uri %>"><%= request_uri %></a></td>
            </tr>
            <tr class="search">
                <td><nobr><i>Servlet name</i></nobr></td>
                <td></td>
                <td><%= servlet_name %></td>
            </tr>
            <tr>
                <td colspan=3><hr><b>Parameters:</b><br><br></td>
            </tr>
<%
            Map map = request.getParameterMap();
            for(Iterator keys = map.keySet().iterator(); keys.hasNext();) {
                String param = (String)keys.next();
                String values[] = (String[])map.get(param);
%>
                <tr class="search">
                    <td><nobr><%= param %></nobr></td>
                    <td></td>
                    <td><%= values[0] %></td>
                </tr>
<%
                for(int i = 1; i < values.length; ++i) {
%>
                    <tr class="search">
                        <td></td>
                        <td></td>
                        <td><%= values[i] %></td>
                    </tr>
<%
                }
            }
%>
            <tr>
                <td colspan=3><hr><b>Headers:</b><br><br></td>
            </tr>
<%
            for(Enumeration nms = request.getHeaderNames(); nms.hasMoreElements();) {
                String name = (String)nms.nextElement();
                boolean first = true;
                for(Enumeration vals = request.getHeaders(name); vals.hasMoreElements();) {
                    Object v = vals.nextElement();
%>
                    <tr class="search">
                        <td><nobr><%= first ? name : "" %></nobr></td>
                        <td></td>
<%
                        if(name.equalsIgnoreCase("referer")) {
%>
                            <td><a href="<%= v %>"><%= v %></a></td>
<%                        
                        }
                        else {
%>
                            <td><%= v %></td>
<%                        
                        }
%>
                    </tr>
<%
                    first = false;
                }
            }
%>
            <tr>
                <td colspan=3><hr></td>
            </tr>
        </table>
<%
        while(exception != null) {
            StringWriter buf = new StringWriter();
            exception.printStackTrace(new PrintWriter(buf));
%>
            <max:popup>
<pre>
<%= buf %>
</pre>
            </max:popup>
            <%= exception.getClass() %>
            <br>
<%
            if(exception instanceof ServletException) {
                ServletException exc = (ServletException)exception;
                exception = exc.getRootCause();
            }
            else if(exception instanceof MaxException) {
                MaxException exc = (MaxException)exception;
                exception = exc.getNestedException();
            }
            else {
                exception = null;
            }
        }
    }
%>
<%@ include file="../end.jspf" %>
