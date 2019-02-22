<%@ include file="../../head.jspf" %>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max"%>

    <table>
        <tr class="top">
            <td>
	        <h1><nobr>Document History</nobr></h1>
	    </td>
	    <td class="width">
	      </td>
	        <td class="right">
	            <%@ include file="/def/xmleditor/xmlmenu.jsp" %>
	        </td>
	    </tr>
	    <tr class="top">
	        <td colspan=3>
                <jsp:include page="/documents">
                    <jsp:param name="operation" value="/showHistory"/>
                </jsp:include>
            </td>
        </tr>
    </table>
    
<%@ include file="../../end.jspf" %>