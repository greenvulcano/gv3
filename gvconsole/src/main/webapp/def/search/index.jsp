<%@ page import="max.config.*" %>
<%@ include file="../../head.jspf" %>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max"%>

    <jsp:useBean id="searchForm" class="max.search.SearchForm" scope="session"/>
    <jsp:setProperty name="searchForm" property="action" value="/def/search/documentSearch.jsp"/>
    
    <table>
        <tr class="search">
            <td>
            	<h1><nobr>Search documents</nobr></h1>
	        </td>
	        <td>
	        </td>
	        <td>
	            <%@ include file="../xmleditor/xmlmenu.jsp" %>
	        </td>
	    </tr>
	    <tr class="search">
	        <td colspan=3>
	            Using this tool you can search through configured documents.
	            <hr>
	            <br>
	        </td>
	    </tr>
	    <tr class="search">
	        <td colspan=3>
	            <jsp:include page="searchForm.jsp"/>
            </td>
        </tr>
    </table>
    
    <%= searchForm.showFoundDocuments() %>

<%@ include file="../../end.jspf" %>
