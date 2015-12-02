<%@ page import="max.config.*" %>
<%@ include file="../../head.jspf" %>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max"%>
	<script type="text/javascript">
		$(function() {
			$("#menuAction, #menuSettingAction").buttonset();
			$(".btnMenu").button({icons: {primary: 'ui-icon-triangle-1-s',secondary: 'ui-icon-gear'},text: false});
			$(".btnMenuIn, .btnMenuInRight").button({icons: {primary: 'ui-icon-triangle-1-n',secondary: 'ui-icon-gear'},text: false});
		});
		$(document).ready(function(){$.pop(); $(".pop_menu").addClass('ui-widget-header ui-corner-all');});
	</script>
    <table>
        <tr class="search">
            <td>
	            <h1><nobr>Configuration editor</nobr></h1>
	        </td>
	        <td>
	        </td>
	        <td>
	            <%@ include file="xmlmenu.jsp" %>
	        </td>
	    </tr>
	    <tr class="search">
	        <td colspan=3>
	            Using this tool you can browse and edit a configuration document.
	        </td>
	    </tr>
	    <tr class="search">
	        <td colspan=3>
	            <%@ include file="xmlinterface.jsp" %>
            </td>
        </tr>
    </table>
    
<%@ include file="../../end.jspf" %>
