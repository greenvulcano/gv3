<jsp:useBean id="xQueryBean" class="it.greenvulcano.gvesb.gvconsole.gvcon.xquery.XQueryBean" scope="session"/>
<% xQueryBean.manageBooleans(request); %>
<jsp:setProperty name="xQueryBean" property="*"/>

<% xQueryBean.performXQueryProcessorDocuments(request); %>

<jsp:forward page="index.jsp"/>