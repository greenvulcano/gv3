<jsp:useBean id="xQueryBean" class="it.greenvulcano.gvesb.gvconsole.gvcon.xquery.XQueryBean" scope="session"/>
<jsp:setProperty name="xQueryBean" property="*"/>
<script>
 function viewOutput()
 {
     var fileName = document.xQueryBean.fileName.value;
 	 window.open("viewOutput.jsp?fileName=" + fileName, "Output", "left=50,top=50,width=540,height=460,resizable, scrollbars");
 }
 function saveXQuery()
 {
     sel = document.getElementById("selectQS");
 	 i = sel.selectedIndex;
     var xQuery = document.xQueryBean.text.value;
     var nome = sel.getElementsByTagName("option")[i].text;
     var descrizione = sel.getElementsByTagName("option")[i].label;
     
     var query = "saveXQuery.jsp?xQueryString="+xQuery+"&descrizione="+descrizione+"&nome="+nome;
 	 childWindow = window.open(query, "Output", "left=50,top=50,width=580,height=530,resizable, scrollbars");
     if (childWindow.opener == null) 
     	childWindow.opener = self;
 }
 
 function enableButton()
 {
 	textFileName = document.xQueryBean.fileName;
 	saveCheck = document.xQueryBean.saveOnFile;
 	
 	if(saveCheck.checked)
 	{
 		textFileName.disabled=false;	
 	}
 	else
 	{
 		textFileName.disabled=true;
 	}
 }
</script>
<TABLE class="xquery">
	<TR class="border">
		<TD class="border">
			<form name="xQueryBean" action="<jsp:getProperty name="xQueryBean" property="actionHTML"/>">
			    <input type="hidden" name="resetBooleans">
			    <TABLE class="xquery">
			        <TR class="border">
			            <TD class="border">
			                <TABLE class="xquery">
			                    <TR class="xquery">
			                        <TD>
			                            XQuery:<br/>
			                            <textarea class="xqueryInput" id="text" name="text"><jsp:getProperty name="xQueryBean" property="text"/></textarea><br/>
			                            <input type="submit" class="button" value="process...">&nbsp;&nbsp;&nbsp;&nbsp;<input type="button" class="button" value="Save XQuery" onclick="saveXQuery()">
			                            <FONT class="error"><b><br/><jsp:getProperty name="xQueryBean" property="errorHTML"/></b></FONT>
			                        </TD>
			                    </TR>
			                </TABLE>
			            </TD>
			        </TR>
<!-- 			        
			        <TR class="border">
			            <TD class="border">
			                <TABLE class="xquery">
			                    <TR class="xquery">
			                        <TD>
			                        	Output mode:
			                            <br/>&nbsp;&nbsp;&nbsp;&nbsp;<nobr><input <%= xQueryBean.checkOutputMode("simple") %> type="radio" class="radio" name="outputMode" value="simple"> Simple</nobr>
			                            <br/>&nbsp;&nbsp;&nbsp;&nbsp;<nobr><input <%= xQueryBean.checkOutputMode("transform") %> type="radio" class="radio" name="outputMode" value="transform"> Transform</nobr>
			                        </TD>
			                    </TR>
			                </TABLE>
			            </TD>
			        </TR>
-->			        
			        <TR class="border">
			              <TD class="border" colspan="2">
			                <TABLE class="xquery">
			                    <TR class="xquery">
			                        <TD>
			                            Save on File:
			                            <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			                           	<nobr><input type="checkbox" class= "checkbox" name="saveOnFile" value="true" onchange="enableButton()" <%= xQueryBean.checkSaveOnFile() %>>Save on file
			                           	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" name="fileName" class="input120" value="<jsp:getProperty name="xQueryBean" property="fileName"/>" disabled="disabled"></nobr>
			                           	<br/>
			                           	<% if(xQueryBean.isSaveOnFile()) { %>
			                            	<input type="button" class="button" name="view" value="View File" onclick="viewOutput()"/>
			                        	<% } %>
			                        </TD>
			                    </TR>
			                </TABLE>
			            </TD>
			        </TR>        
			    </TABLE>
			</form>
		</TD>
		<TD class="border"  valign="top">
            <jsp:include page="xQueryList.jsp" flush="true"/>
        </TD>
    </TR>
</TABLE>