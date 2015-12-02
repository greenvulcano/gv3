<span id="menuxml"> 
    <a href="<max:prop prop="max.site.name"/>/def/xmleditor/index.jsp" title="Document Management">
    	<img src="<max:prop prop="max.site.name"/>/images/DocumentManagement.png" border="0"/>
    </a> 	
	<% if(max.xml.XMLBuilder.getFromSession(session) != null) { %>
        <a href="<max:prop prop="max.site.name"/>/def/xmleditor/xmleditor.jsp" title="Document Editor">
        	<img src="<max:prop prop="max.site.name"/>/images/edit_menu.png" border="0"/>
        </a> 
	<% } %>
	<max:grant roles="consoleAdministrator">
    	<a href="<max:prop prop="max.site.name"/>/def/documents/admin.jsp" title="Filter View">
    		<img src="<max:prop prop="max.site.name"/>/images/FilterView.png" border="0"/>
    	</a> <!-- View/Filter management -->
	</max:grant>
</span>