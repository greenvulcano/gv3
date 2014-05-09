
<jsp:useBean id="xQueryBean" class="it.greenvulcano.gvesb.gvconsole.gvcon.xquery.XQueryBean" scope="session"/>
<% xQueryBean.manageBooleans(request); %>
<jsp:setProperty name="xQueryBean" property="*"/>

<script>
 function viewOutput()
 {
     var fileName = document.xQueryBean.fileName.value;
 	window.open("viewOutput.jsp?fileName=" + fileName, "Output", "left=50,top=50,width=540,height=460,resizable, scrollbars");
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
<form name="xQueryBean" action="<jsp:getProperty name="xQueryBean" property="actionHTML"/>">
    <input type="hidden" name="resetBooleans">
    <TABLE class="ui-widget-header ui-corner-all">
        <TR>
            <TD>
            	<TABLE>
                    <TR>
                        <TD>
                        	XQuery:<br/>
                            <textarea class="xqueryInput" id="text" name="text"><jsp:getProperty name="xQueryBean" property="text"/></textarea><br/>
                            <input type="submit" class="button" value="process...">
                            <FONT class="error"><b><br/><jsp:getProperty name="xQueryBean" property="errorHTML"/></b></FONT>
                        </TD>
                         <TD valign="top">
	                        Starting node:
                            <br/><nobr><input <%= xQueryBean.checkStartingNode("root") %> type="radio" class="radio" name="startingNode" value="root"> Root node</nobr>
                            <br/><nobr><input <%= xQueryBean.checkStartingNode("current") %> type="radio" class="radio" name="startingNode" value="current"> Current node</nobr>
                        </TD>
<!--                         
                        <TD valign="top">
                        	Output mode:
                            <br/>&nbsp;&nbsp;&nbsp;&nbsp;<nobr><input <%= xQueryBean.checkOutputMode("simple") %> type="radio" class="radio" name="outputMode" value="simple"> Simple</nobr>
                            <br/>&nbsp;&nbsp;&nbsp;&nbsp;<nobr><input <%= xQueryBean.checkOutputMode("transform") %> type="radio" class="radio" name="outputMode" value="transform"> Transform</nobr>
                        </TD>
 -->                        
                    </TR>
                </TABLE>
            </TD>
        </TR>
        <TR>
              <TD>
                <TABLE>
                    <TR>
                        <TD>
                            Save on File:
                            <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                            	<nobr><input type="checkbox" class= "checkbox" name="saveOnFile" value="true" onchange="enableButton()" <%= xQueryBean.checkSaveOnFile() %> >Save on file
                            	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" name="fileName" class="input200" value="<jsp:getProperty name="xQueryBean" property="fileName"/>"><nobr>
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