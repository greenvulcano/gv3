<%@ include file="../../head.jspf" %>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max"%>
<script type="text/javascript">$(function() { $("#accordion").accordion();});</script>
    <table align="center">
        <tr class="top">
            <td>
	            <h1><nobr>Configuration</nobr></h1>
	        </td>
	        <td class="width4">
	        </td>
	        <td class="right">
	            <%@ include file="/def/xmleditor/xmlmenu.jsp" %>
	        </td>
	    </tr>
	    <tr class="top">
	        <td colspan=3>
	            Using this tool you can select a document for editing and shows
	            document history.
	        </td>
	    </tr>
	    <tr class="top">
	        <td colspan=3>
                <jsp:include page="/documents">
                    <jsp:param name="operation" value="/showRepository"/>
                </jsp:include>
            </td>
        </tr>
    </table>
    
<%@ include file="../../end.jspf" %>
</body>
</html>