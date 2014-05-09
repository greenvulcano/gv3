<%@ page import="max.config.*" %>
<%@ include file="../../head.jspf" %>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max"%>

    <table>
        <tr class="search">
            <td>
	            <h1><nobr>Warnings</nobr></h1>
	        </td>
	        <td>
	        </td>
	        <td>
	            <%@ include file="xmlmenu.jsp" %>
	        </td>
	    </tr>
	    <tr class="search">
	        <td colspan=3>
	            This ia a list of warnings and error into the current editing
	            session.
	        </td>
	    </tr>
	    <tr valign=top>
	        <td colspan=3>
	            <%@ include file="xmlwarninginterface.jsp" %>
            </td>
        </tr>
    </table>
    
<%@ include file="../../end.jspf" %>
