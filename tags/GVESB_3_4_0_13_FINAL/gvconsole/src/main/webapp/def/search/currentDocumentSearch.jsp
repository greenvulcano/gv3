<jsp:useBean id="searchForm" class="max.search.SearchForm" scope="session"/>
<% searchForm.manageBooleans(request); %>
<jsp:setProperty name="searchForm" property="*"/>

<% searchForm.performCurrentDocumentSearch(request); %>

<jsp:forward page="searchCurrentDocument.jsp"/>
