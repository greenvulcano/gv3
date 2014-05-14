<%@ page import="it.greenvulcano.gvesb.buffer.GVBuffer"%>
<%@ page import="it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestManager"%>
<%@ page import="java.util.Arrays"%>    
<%@ page import="java.io.StringWriter"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.lang.reflect.InvocationTargetException" %>
<%@ page import="javax.management.MBeanException" %>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max" %>

<%@ include file="../head.jspf" %>

<%
    TestManager testManager = new TestManager(request);
    String descriptionTest = "GVBuffer as JMS Body";
    String currentEncoding = (String)testManager.get("charEncoding");
    String[] charEncodings =
    new String[] {"Binary", "US-ASCII", "ISO-8859-1", "UTF-8", "UTF-16BE", "UTF-16LE", "UTF-16"};
    GVBuffer gvBuffer = (GVBuffer)testManager.get("gvBufferInput", false);
    if (gvBuffer == null) {
        gvBuffer = new GVBuffer();
    }
%>

<link href="../css/styles.css" rel="stylesheet" type="text/css">
<form name="gvBufferBody" method="post" id="gvBufferBody" action="../GVTesterManager">
  <script>
        function startPage() {
        }
        
        function goBackFunction() {
            var msg1 = "Vuoi salvare l'GVBuffer prima di tornare indietro?";
	        if (confirm(msg1)) {
	            goBackConfirm();
	        }
	        else {
	           document.gvBufferBody.method.value='goBack';
	           document.gvBufferBody.submit();
	        }
        }
            
	    function goBackConfirm() {
	        document.gvBufferBody.method.value='setBody';
	        document.gvBufferBody.submit();
	    }
        
        function openUploadWindow()
        {   
            var systemInserted = document.gvBufferBody.system.value;
            var serviceInserted = document.gvBufferBody.service.value;
            window.open("../testing/UploadWindow.jsp", "uploadWindow", "left=50,top=50,width=340,height=260,resizable, scrollbars");
        }

        function openHexData()
        {
            window.open("../testing/showHexGVBuffer.jsp?text=yes", "hexGVBufferInput", "left=50,top=50,width=540,height=460,resizable, scrollbars");
        }
    </script>
    <input type="hidden" name="method"/>
    <input type="hidden" name="testType" value="gvBufferBody"/>
    <table>
	    <tr >
	        <td class="border" valign="top" colspan="4"><b><font class="titlesmall">&nbsp;&nbsp;GVBUFFER&nbsp;</font></b></td>
	    </tr>
            <tr>
	        <td colspan="4">
	            <hr></hr>
	        </td>
	    </tr>
            <tr>
	        <td align="left">System</td>
	        <td align="left"><input type="text" name="system" class="input120" value="<%= gvBuffer.getSystem() %>"></td>
	   </tr>
<tr>
	        <td align="left">Service</td>
	        <td align="left"><input type="text" name="service" class="input120" value="<%= gvBuffer.getService()%>"></td>
	    </tr>
    <tr>
	        <td align="left">Data<br/>
	            <input type="button" class="button" name="uploadButton" value="Upload" onclick="openUploadWindow()">
	        </td>
		<td align="left"><textarea name="byteData" class="input120" cols="30" rows="10"><%=gvBuffer.getObject()%></textarea></td>
    </tr>
    <tr>
       <td align="left">
	        	Character Encoding
	        </td>
        <%@ include file="comboEncodingInput.jspf" %>
    </tr>
    <tr>
	        <td align="left">Id*</td>
	        <td align="left">
	            <input type="text" name ="id" class="input60" value="<%= gvBuffer.getId() %>">
	            <input type="submit" name="idButton" class="button" value="GetId" onclick="method.value='generateID'">
	        </td>
	    </tr>
        <tr>
	        <td align="left">ReturnCode</td>
	        <td align="left"><input type="text" name="retCode" class="input120" value="<%= gvBuffer.getRetCode() %>"></td>
	    </tr>
        <tr>
	        <td align="left">
	        	Properties&nbsp;
	        	<input type="button" class="button" value="Add" onclick="addProperties()">
	        </td>
	</tr>
    <tr>
        <td colspan="4" align="center">
            <script>
    		    var counter = 0;

    		    function addProperties()
    		    {
    		    	name = "property" + counter;
    		    	counter++;

    		    	$('#fields').append("<div id='" + name + "' style='margin-top:5px;'>"
    		    	+ "<nobr>Name &nbsp;<input type=text name='property'>"
    		    	+ "&nbsp; Value &nbsp;<input type=text name='propertyValue'>"
    		    	+ "&nbsp; [<span valign=center onclick=\"remove('" + name + "')\" style='text-decoration:underline;cursor:pointer;'>del</span>]</nobr>"
    		    	+ "</div>");
    		    }

    		    function remove(id){$('#'+id).remove();}
    	    </script>
<%
            String fieldNames[] = gvBuffer.getPropertyNames();
            Arrays.sort(fieldNames);
            for(int i = 0; i < fieldNames.length; ++i) {
                String field = fieldNames[i];
                String value = gvBuffer.getProperty(field);
                String name = "property_" + field;
%>
                <div id="<%=name%>">
		    	    <nobr>
		    	        Name &nbsp;<input type=text name="property" value="<%=field%>">
		    	        &nbsp; Value &nbsp;<input type=text name="propertyValue" value="<%=value%>">
		    	        [<span valign="center" onclick="remove('<%=name%>')" style='text-decoration:underline;cursor:pointer;'>del</span>]</nobr>"
		    	    </nobr>
    		   </div>
<%
            }
%>
		    <div id="fields"></div>
		</td>
    </tr>
    <tr>
        <td align="left">
            <input type="submit" class="button" value="ok" name="ok" onclick="method.value='setBody'"/>
            <input type="submit" class="button" value="reset" name="resetAllInfo" onclick="method.value='resetGVBuffer'"/>
            <input type="button" class="button" value="go back" name="goBack" onclick="javascript:goBackFunction()"/>
        </td> 
     </tr>
    <tr>
        <td colspan="4">
            <hr></hr>
        </td>
    </tr>
    </table>
</form>
<%@ include file="../end.jspf" %>
