<%@ include file="../../head.jspf" %>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max"%>

    <jsp:useBean id="pdfRegistry" class="max.documentation.PDFRegistryBean" scope="session"/>
    <jsp:setProperty name="pdfRegistry" property="*"/>

    <table>
        <tr class="search">
            <td>
                <h1><nobr>PDF Documentation</nobr></h1>
            </td>
        </tr>
        <tr class="search">
            <td>
                Di seguito la documentazione in formato PDF.<p>
            </td>
            <td>
            </td>
            <td>
            </td>
        </tr>
    </table>

    <hr/>

    <%= pdfRegistry.showDocuments() %>

<%@ include file="../../end.jspf" %>
