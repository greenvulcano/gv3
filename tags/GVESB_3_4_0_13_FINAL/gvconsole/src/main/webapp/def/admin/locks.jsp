<%@ include file="../../head.jspf" %>
<%@ page import="java.util.*, max.documents.*" %>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

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
    <c:set var="maxSiteRoot"><max:prop prop='max.site.root'/></c:set>
    
    <div class="titleSection"><h1>Lock Files</h1>
    
    This page contains the list of lock files.<br>
    <p>Removing a lock can create inconsistency; before remove a lock be sure that the corresponding 
    document is not currently edited by some GVConsole user.</p><p></p>
    <br></div>
    <center>
    
    <table class="ui-widget-header ui-corner-all">
<%

    String[] locks = request.getParameterValues("lock");
    if (locks != null) {
        for (int i=0; i < locks.length; i++)
            LocksManager.forceUnlockDocument(locks[i]);
    }
    LockInfo[] locksInfo = LocksManager.getLocksInfo();
    
    int length = locksInfo.length;
    
    if (length > 0 ) {
%>
    <script>
        function removeAll(select) {
            theForm = document.forms[0];
            for(var i= 0; i < theForm.elements.length; i++) {
                if(theForm.elements[i].type == "checkbox") {
                    theForm.elements[i].checked=true
                }
            }
            if (confirm('Do you really want to clear all locks?'))
                theForm.submit();
            else
                theForm.reset();
        }
        
        function removeLock() {
            theForm = document.forms[0];
            ok=false;
            for(var i= 0; i < theForm.elements.length; i++ ) {
                if(theForm.elements[i].type == "checkbox" ) {
                    if(theForm.elements[i].checked) {
                        ok = true;
                        break;
                    }
                }
            }
            if(!ok) {
                alert('Please select a lock')
                return;     
            }           
            if (confirm('Do you really want to clear the selected locks?'))
                theForm.submit();
            else
                theForm.reset();
        }
        
    </script>
    <form name="lockForm">
        <tr class="search">
            <td><b>Document</b></td>
            <td width="10"></td>
            <td><b>User</b></td>
            <td width="10"></td>
            <td><b>IP Address</b></td>
            <td width="10"></td>
            <td><b>Host</b></td>
            <td width="10"></td>
            <td><b>Date</b></td>
        </tr>
        <tr valign="top">
            <td colspan="9"><hr></td>
        </tr>
<%        
        String color[] = new String[] {"#99FF66", "#99FF33"};
        for(int idx = 0; idx < length; ++idx) {
            
            String name = locksInfo[idx].getName();
            String label = locksInfo[idx].getLabel();
            String userName = locksInfo[idx].getUser();
            String date = locksInfo[idx].getDateString();
            String ipAddress = locksInfo[idx].getIpAddress();
            String hostName = locksInfo[idx].getHostName();
%>
         <tr class="border">
             <td><input type="checkbox" name="lock" value="<%= name %>">&nbsp;<b><%= toHtml(label) %></b></td>
             <td width="10"></td>
             <td><%= toHtml(userName) %></td>
             <td width="10"></td>
             <td><%= toHtml(ipAddress) %></td>
             <td width="10"></td>
             <td><%= toHtml(hostName) %></td>
             <td width="10"></td>
             <td><%= toHtml(date) %></td>
         </tr>
<%
        }
%>
         <tr><td colspan="9"><hr></td></tr>
         <tr>
            <td colspan="9" align="center"><input type="button" name="btnRemove" value="  Remove Lock  " onclick="javascript:removeLock();">
            <input type="button" name="btnRemoveAll" value="  Remove All Locks " onclick="javascript:removeAll();">
            <input type="button" name="btnRefreshList" value="  Refresh List  " onclick="document.location.href='${maxSiteRoot}/def/admin/locks.jsp'"></td>
         </tr>
         </form>
<%
      } else {
%>
            <tr><td><b>No locks found.</b></td></tr>
            <tr><td><hr></td></tr>
            <tr><td align="center"><input type="button" name="btnRefreshList" value="  Refresh List  " onclick="document.location.href='${maxSiteRoot}/def/admin/locks.jsp'"></td></tr>
<% 
        } 
%>
    </table>
    </center>

<%@ include file="../../end.jspf" %>
