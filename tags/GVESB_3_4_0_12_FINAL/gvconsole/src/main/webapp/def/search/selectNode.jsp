<jsp:useBean id="searchForm" class="max.search.SearchForm" scope="session"/>
<jsp:setProperty name="searchForm" property="nodeId"/>

<% searchForm.selectCurrentNode(request); %>

<jsp:forward page="../xmleditor/xmleditor.jsp"/>
