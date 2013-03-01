<%@ page import="max.config.*" %>
<%@ include file="../../head.jspf" %>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max"%>
<%
    String bgcolor[] = {"color2", "color3"};
    int k;
%>

    <a name=top>

    <table>
        <tr class="top">
            <td>
                <h1><nobr>DTD Repository</nobr></h1>
            </td>
            <td width=15>
            </td>
            <td class="right">
                <%@ include file="xmlmenu.jsp" %>
            </td>
        </tr>
        <tr valign=top>
            <td colspan=3>
            	Click <input type="button" class="button" value="Register" onclick="javascript:registerDTD()" />to register a new document template.<br/>
            </td>
        </tr>
    </table>

    <hr>

    <table class="search" cellspacing=0 cellpadding=3>

        <tr valign=top class="color1">
            <td colspan=3>
                <b>AVAILABLE DTDs</b> <a href=#top>^</a>
                <br><br>
            </td>
        </tr>
        <max:selContent rule="showDtd_selection_rule" attribute="dtd" scope="page" orderBy="DOCTYPE ASC"/>
        <% k = 0; %>
        <max:loop attribute="dtd" scope="page">
        	<% k++; %>

            <script>
                var menu<%= k %> = new dMenu();
                menu<%= k %>.add("View <max:data fld="DOCTYPE"/> DTD", "javascript:downloadDTD('<%= k %>')", "",  "", "View the DTD");
                menu<%= k %>.addSeparator();
                menu<%= k %>.add("Edit", "javascript:editDTD('<%= k %>')", "",  "", "Update the DTD attributes");
                menu<%= k %>.add("Upload", "javascript:updateDTD('<%= k %>')", "",  "", "Upload the whole DTD");
                menu<%= k %>.add("Delete", "javascript:deleteDTD('<%= k %>')", "",  "", "Delete this DTD");
                menu<%= k %>.addSeparator();
                menu<%= k %>.add("New <max:data fld="DOCTYPE"/> document", "javascript:newDTD('<%= k %>')", "",  "", "Create a new document based on this DTD");
                menu<%= k %>.addSeparator();
                menu<%= k %>.add("View XSLT", "javascript:xsltDTDdownload('<%= k %>')", "",  "", "Download the detail XSLT for this DTD");
                menu<%= k %>.add("Upload XSLT", "javascript:xsltDTD('<%= k %>')", "",  "", "Set a detail XSLT for this DTD");
                menu<%= k %>.add("Delete XSLT", "javascript:xsltDTDremove('<%= k %>')", "",  "", "Remove the detail XSLT for this DTD");
            </script>

            <tr valign=top class="<%= bgcolor[k % 2] %>">
                <td>
                    <img border="0" src="<max:prop prop="max.site.root"/>/images/menu.gif" onMouseover="menu<%= k %>.show(event)" onMouseout="dMenu.delayHide()"/>
                    <a href="javascript:downloadDTD('<%= k %>')" title="Download this DTD"><b id="pd<%= k %>"><max:data fld="DOCTYPE"/></b></a>
                </td>
                <td width=15></td>
                <td>
                    <i id="pds<%= k %>"><max:data fld="DESCRIPTION"/></i><br><br>
                    <small>
                    <table cellpadding="0" cellspacing="0">
                        <tr>
                            <td class="left">FPI:&nbsp;&nbsp;</td>
                            <td><nobr><b id="pf<%= k %>"><max:data fld="PUBLIC"/></b></nobr></td>
                        </tr>
                        <tr>
                            <td class="rigth">URI:&nbsp;&nbsp;</td>
                            <td><nobr><b id="pu<%= k %>"><max:data fld="SYSTEM"/></b></nobr></td>
                        </tr>
                        <tr>
                            <td class="right"><nobr>Namespace URI:&nbsp;&nbsp;</nobr></td>
                            <td><nobr><b id="nsu<%= k %>"><max:data fld="NAMESPACE_URI"/></b></nobr></td>
                        </tr>
                    </table>
                    </small>
                </td>
            </tr>
        </max:loop>

    </table>

    <script>

        // UTILITIES //////////////////////////////////////////

        function enabling(doct, fpi, uri, file, desc, nsu)
        {
            DTDForm.doctype.disabled = !doct;
            DTDForm.fpi.disabled = !fpi;
            DTDForm.uri.disabled = !uri;
            DTDForm.file.disabled = !file;
            DTDForm.namespace.disabled = !nsu;
            DTDForm.description.disabled = !desc;
        }

        function initForm(doct, fpi, uri, desc, key, nsu)
        {
            DTDForm.doctype.value = doct;
            DTDForm.fpi.value = fpi;
            DTDForm.uri.value = uri;
            DTDForm.namespace.value = nsu;
            DTDForm.description.value = desc;
            DTDForm.key.value = key;
        }

        function init(k)
        {
            uri = document.all.item("pu" + k).innerHTML;
            initForm(
                document.all.item("pd" + k).innerHTML,
                document.all.item("pf" + k).innerHTML,
                uri,
                document.all.item("pds" + k).innerHTML,
                uri,
                document.all.item("nsu" + k).innerHTML
            );
        }


        // OPERATIONS /////////////////////////////////////////

        function registerDTD()
        {
            DTDForm.operation.value = "registerDTD";
            initForm("", "", "", "", "", "");
            enabling(true, true, true, true, true, true);
            DTDForm.btn.value = "  Register a new DTD  ";
            title.innerHTML = "REGISTER A NEW DTD";
            document.location = "#form";
            DTDForm.target = "";
        }

        function editDTD(k)
        {
            DTDForm.operation.value = "editDTD";
            init(k);
            enabling(true, true, true, false, true, true);
            DTDForm.btn.value = "  Apply  ";
            title.innerHTML = "EDIT THE DTD ATTRIBUTES";
            document.location = "#form";
            DTDForm.target = "";
            DTDForm.btn.value = "  Submit DTD attributes  ";
        }

        function updateDTD(k)
        {
            DTDForm.operation.value = "updateDTD";
            init(k);
            enabling(false, false, false, true, false, false);
            DTDForm.btn.value = "  Apply  ";
            title.innerHTML = "UPLOAD THE DTD";
            document.location = "#form";
            DTDForm.target = "";
            DTDForm.btn.value = "  Upload the DTD  ";
        }

        function deleteDTD(k)
        {
            msg = "Deleting '" + document.all.item("pd" + k).innerHTML + "' document.\n";
            if(confirm(msg + "Are you sure?")) {
                DTDForm.operation.value = "deleteDTD";
                init(k);
                enabling(true, true, true, false, true, true);
                DTDForm.target = "";
	            DTDForm.btn.value = "  Delete the DTD  ";
	            title.innerHTML = "DELETE THE DTD";
                DTDForm.submit();
            }
        }

        function downloadDTD(k)
        {
            DTDForm.operation.value = "downloadDTD";
            init(k);
            enabling(true, true, true, false, true, true);
            DTDForm.target = "_blank";
            DTDForm.btn.value = "  Download the DTD  ";
            title.innerHTML = "DOWNLOAD THE DTD";
            DTDForm.submit();
        }

        function xsltDTDdownload(k)
        {
            DTDForm.operation.value = "downloadXSLT";
            init(k);
            enabling(true, true, true, false, true, true);
            DTDForm.target = "_blank";
            DTDForm.btn.value = "  Download the XSLT  ";
            title.innerHTML = "DOWNLOAD THE XSLT";
            DTDForm.submit();
        }

        function xsltDTDremove(k)
        {
            msg = "Deleting XSLT for '" + document.all.item("pd" + k).innerHTML + "' document.\n";
            if(confirm(msg + "Are you sure?")) {
                DTDForm.operation.value = "deleteXSLT";
                init(k);
                enabling(true, true, true, false, true, true);
                DTDForm.target = "";
            	DTDForm.btn.value = "  Delete the XSLT  ";
            	title.innerHTML = "DELETE THE XSLT";
                DTDForm.submit();
            }
        }

        function newDTD(k)
        {
            DTDForm.operation.value = "newDoc";
            init(k);
            enabling(true, true, true, false, true, true);
            DTDForm.target = "";
        	DTDForm.btn.value = "  Create a new document  ";
        	title.innerHTML = "CREATE A NEW DOCUMENT";
            DTDForm.submit();
        }

        function xsltDTD(k)
        {
            DTDForm.operation.value = "registerXSLT";
            init(k);
            enabling(false, false, false, true, false, false);
            DTDForm.btn.value = "  Apply  ";
            title.innerHTML = "REGISTER XSLT";
            document.location = "#form";
            DTDForm.target = "";
        	DTDForm.btn.value = "  Upload the XSLT  ";
        	title.innerHTML = "UPLOAD THE XSLT";
        }

    </script>

    <hr>
    <a name=form><b id=title>REGISTER A NEW DTD</b>
    <a href=#top>^</a>
    <a href="javascript:registerDTD()">register new</a>

    <form name=DTDForm action=<max:prop prop="max.site.root"/>/MaxXMLServlet method=post enctype="multipart/form-data">
        <input type=hidden class="button" name=operation value=registerDTD>
        <input type=hidden class="button" name=key value="...">
        <table class="search">
            <tr>
                <td nowrap>Document type
                <td class="width">
                <td>
                    <max:popup>
                    This will be the root element for new created documents.
                    </max:popup>
                <td width=15>
                <td><input type=text class="input200" name=doctype >
            <tr>
                <td nowrap>FPI
                <td class="width">
                <td>
                    <max:popup>
                    The FPI (Formal Public Identifier). This is usually composed
                    by four fields separed by double slash:<p>
                    <li>Formal standard.
                    <li>Maintainer.
                    <li>Document type description.
                    <li>Language.
                    <p>
                    Example: <nowrap><b>-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN</b></nowrap>
                    <p>
                    If this field is leaved blank then the DTD will be consider a SYSTEM DTD,
                    otherwise it will be consider a PUBLIC DTD.
                    </max:popup>
                <td>
                <td><input type=text class="input200" name=fpi>
            <tr>
                <td nowrap>URI
                <td class="width">
                <td>
                    <max:popup>
                    URI for the DTD.
                    </max:popup>
                <td>
                <td><input type=text class="input200" name=uri>
            <tr>
                <td nowrap>Namespace URI
                <td class="width">
                <td>
                    <max:popup>
                    If the document has a namespace you must specify its URI.
                    </max:popup>
                <td>
                <td><input type=text class="input200" name=namespace>
            <tr>
            <tr>
                <td nowrap>File
                <td class="width">
                <td>
                    <max:popup>
                    This file will be uploaded.
                    </max:popup>
                <td>
                <td><input type=file name=file size=50>
            <tr>
                <td nowrap>Description
                <td width=15>
                <td>
                    <max:popup>
                    A convenient description for the document template.
                    </max:popup>
                <td>
                <td><textarea cols=47 rows=10 name=description></textarea>
            <tr>
                <td colspan=5><hr>
            <tr>
                <td colspan=5 align=right>
                    <input name=btn type=submit class="button" value="  Register  ">
                </td>
            </tr>
        </table>
    </form>

<%@ include file="../../end.jspf" %>
