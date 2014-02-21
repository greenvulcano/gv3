<%@ include file="../../head.jspf" %>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max"%>
    <jsp:useBean id="xQueryBean" class="it.greenvulcano.gvesb.gvconsole.gvcon.xquery.XQueryBean" scope="session"/>
    <jsp:setProperty name="xQueryBean" property="action" value="/def/xquery/xQueryProcessor.jsp"/>
    
    <TABLE>
         <TR class="xquery">
            <TD>
	            <h1><nobr>XQuery Processor</nobr></h1>
	        </TD>
	        <TD>
	        </TD>
	        <TD>
	            <%@ include file="../xmleditor/xmlmenu.jsp" %>
	        </TD>
	    </TR>
	    <TR class="xquery">
	        <TD colspan=3>
	            Using this tool you can launch XQuery through configured documents.
	            <hr>
	            <br>
	        </TD>
	    </TR>
	    <TR class="xquery">
	        <TD colspan=3>
	            <jsp:include page="xQueryFormGlobal.jsp"/>
            </TD>
        </TR>
    </TABLE>
    
    <%= xQueryBean.showFoundDocuments() %>

<%@ include file="../../end.jspf" %>
