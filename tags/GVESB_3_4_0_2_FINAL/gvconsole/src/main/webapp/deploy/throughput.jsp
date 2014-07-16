<%@ page import="it.greenvulcano.configuration.XMLConfig" %>
<%@ page import="org.w3c.dom.Node" %>
<%@ page import="it.greenvulcano.jmx.JMXEntryPoint" %>
<%@ page import="javax.management.MBeanServer" %>
<%@ page import="javax.management.ObjectName" %>
<%@ page import="java.util.*" %>

<%@ include file="../head.jspf" %>
<%
	String confFileName = "GVThroughputConfig.xml";
	Node node = null;
	
	String xpathTime = "/GVThroughputConfig/TimeManage"; 
	node = XMLConfig.getNode(confFileName, xpathTime);
	String frameTime = XMLConfig.get(node, "@frameTime", "1000");

	String xpathUrl = "/GVThroughputConfig/HttpUrl"; 
	node = XMLConfig.getNode(confFileName, xpathUrl);
	String httpUrl = XMLConfig.get(node, "@url", "http://localhost:8080/throughput/StatsServlet");

	String xpathIgnoreList = "/GVThroughputConfig/IgnoreList"; 
	node = XMLConfig.getNode(confFileName, xpathUrl);
	String ignoreList = XMLConfig.get(node, "@ignoreList", "GVAdminServer,GVSupport");
    
    String xpathDomain = "/GVThroughputConfig"; 
    node = XMLConfig.getNode(confFileName, xpathDomain);
    String domain = XMLConfig.get(node, "@domain", "GreenVulcano");
    
	String throughputNodRequired = (String)request.getParameter("throughputNodRequired");
	String throughputSvcRequired = (String)request.getParameter("throughputSvcRequired");
	String historyThroughputNodRequired = (String)request.getParameter("historyThroughputNodRequired");
	String historyThroughputSvcRequired = (String)request.getParameter("historyThroughputSvcRequired");
    
    
 	JMXEntryPoint jmx = JMXEntryPoint.instance();
    MBeanServer mbeanServer = jmx.getServer();
    
    String domainString = domain + ":Name=DomainInfo_Internal,*";
    Set set1 = mbeanServer.queryNames(new ObjectName(domainString), null);
    Iterator itr = set1.iterator();
    ObjectName objname = (ObjectName) itr.next();
    
    String[] objectNodes = (String[])mbeanServer.getAttribute(objname, "serversNames");

    LinkedList linkList = new LinkedList();

    for(int i = 0; i < objectNodes.length; i++) {
    	String server = objectNodes[i];

        int indFloat = ignoreList.indexOf(server);		
        if (indFloat == -1) {
        	linkList.add(server);
        }
    }
    
    linkList.add("GLOBAL");
    String[] locationValues = new String[linkList.size()];
	locationValues = (String[])linkList.toArray(locationValues);
	int arrLen = locationValues.length;

    Arrays.sort(locationValues);
 	String[] locationRequired = new String[arrLen];

	for (int i = 0; i < arrLen; i++) {
		String locationRequiredStr = (String)request.getParameter("location_" + i + "_Required");

		if(locationRequiredStr != null) {
			locationRequired[i] = locationRequiredStr;
		}
		else {
			locationRequired[i] = "";
		}
	}
%>
	<div style="margin: 10pt auto; border: 1px solid rgb(247, 247, 247);">
	<form name="index" action="./prepareWindows.jsp">
	    <input type="hidden" name="arrLen" value="<%=arrLen%>"/>
		<script>

			function init() {
				checklocation();
				checkthis();
			}
			
			function checklocation()
			{
<%
				for(int ind = 0; ind < arrLen; ++ind) {
%>	
					if (document.index.location_<%=ind %>.checked==true) {
			        	document.index.location_<%=ind %>_Required.value = '<%=locationValues[ind]%>';
			        	<%
			        	    locationRequired[ind] = locationValues[ind];
			        	%>
			    	}
			    	else {
			    		document.index.location_<%=ind%>_Required.value = '';
			    	}
<%
				}
%>			    	
		    }	
		    
			function checkthis()
			{
				if (document.index.throughputNod.checked==true) {
		        	document.index.throughputNodRequired.value = 'yes';
		    	}
		    	else {
		    		document.index.throughputNodRequired.value = 'no';
		    	}
		    	
		    	if (document.index.throughputSvc.checked == true) {
		        	document.index.throughputSvcRequired.value  = 'yes';
		    	}
		    	else {
		    		document.index.throughputSvcRequired.value  = 'no';
		    	}
		    	
		    	if (document.index.historyThroughputNod.checked == true) {
		        	document.index.historyThroughputNodRequired.value  = 'yes';
		    	}
		    	else {
		    		document.index.historyThroughputNodRequired.value  = 'no';
		    	}
		    	
		    	if (document.index.historyThroughputSvc.checked == true) {
		        	document.index.historyThroughputSvcRequired.value  = 'yes';
		    	}
		    	else {
		    		document.index.historyThroughputSvcRequired.value  = 'no';
		    	}
		    }
		    
		    function checkAll()
			{
				document.index.throughputNodRequired.value = 'yes';
				document.index.throughputSvcRequired.value = 'yes';
				document.index.historyThroughputNodRequired.value = 'yes';
				document.index.historyThroughputSvcRequired.value = 'yes';
				
				document.index.throughputNod.checked = true;
				document.index.throughputSvc.checked = true;
				document.index.historyThroughputNod.checked = true;
				document.index.historyThroughputSvc.checked = true;	
		    }
		    
		    function checkAllNodes()
			{
				document.index.throughputNodRequired.value = 'yes';
				document.index.historyThroughputNodRequired.value = 'yes';
				
				document.index.throughputNod.checked = true;
				document.index.historyThroughputNod.checked = true;
				
				document.index.throughputSvcRequired.value = 'no';
				document.index.historyThroughputSvcRequired.value = 'no';
				
				document.index.throughputSvc.checked = false;
				document.index.historyThroughputSvc.checked = false;
			}
			
			function checkAllServices()
			{
				
				document.index.throughputSvcRequired.value = 'yes';
				document.index.historyThroughputSvcRequired.value = 'yes';
				
				document.index.throughputSvc.checked = true;
				document.index.historyThroughputSvc.checked = true;
				
				document.index.throughputNodRequired.value = 'no';
				document.index.historyThroughputNodRequired.value = 'no';
				
				document.index.throughputNod.checked = false;
				document.index.historyThroughputNod.checked = false;
		    }	
		</script>
		<table width="100%">
			<tr>	
				<td class="center">
					<table class="data">
						<tr class="borderlabel">
							<td colspan="2"><font class="titlesmall">Throughput</font></td>
						</tr>
						<tr>
					    	<td>ThroughputNod&nbsp;</td>
					    	<td>
					    		<%
					    			if ((throughputNodRequired != null) && (throughputNodRequired.equals("yes"))) {
					    		%>
				                		<input type="checkbox" id="throughputNod" name="throughputNodRequired" checked onclick="checkthis()"/>
				                <%
				                	}
				                	else {
				                %>	
				                		<input type="checkbox" id="throughputNod" name="throughputNodRequired" onclick="checkthis()"/>
				                <%
				                	}
				                %>		
				            </td>
		            	</tr>
		            	<tr>
			            	<td>ThroughputSvc&nbsp;</td>
					    	<td>
				            <%
					    		if ((throughputSvcRequired != null) && (throughputSvcRequired.equals("yes"))) {
					    	%>
				               		<input id="throughputSvc" type="checkbox" name="throughputSvcRequired" checked onclick="checkthis()"/>
				            <%
				               	}
				              	else {
				              		if (throughputSvcRequired == null) {
				            %>
				               			<input id="throughputSvc" type="checkbox" name="throughputSvcRequired" checked onclick="checkthis()"/>
				            <%
				              		}
				              		else {
				            %>
										<input id="throughputSvc" type="checkbox" name="throughputSvcRequired" onclick="checkthis()"/>
							<%  		
				              		}
				              	}
				            %>			
				            </td>
		            	</tr>
		            	<tr>
				            <td>HistoryThroughputNod&nbsp;</td>
					    	<td>
				            <%
				    			if ((historyThroughputNodRequired != null) && (historyThroughputNodRequired.equals("yes"))) {
				    		%>
				                	<input id="historyThroughputNod" type="checkbox" name="historyThroughputNodRequired" checked onclick="checkthis()"/>
				            <%
				            	}
				            	else {
				            %>
				            		<input id="historyThroughputNod" type="checkbox" name="historyThroughputNodRequired" onclick="checkthis()"/>
				            <%
				            	}
				            %>						    
				            </td>
		            	</tr>
		            	<tr>
				            <td>HistoryThroughputSvc&nbsp;</td>
					    	<td align="left">
			            	<%
				    			if ((historyThroughputSvcRequired != null) && (historyThroughputSvcRequired.equals("yes"))) {
			    			%>
				                	<input id="historyThroughputSvc" type="checkbox" name="historyThroughputSvcRequired" checked onclick="checkthis()"/>
				            <%
				            	}
				            	else {
				            		if (historyThroughputSvcRequired == null) {
				            %>
				            		<input id="historyThroughputSvc" type="checkbox" name="historyThroughputSvcRequired" checked onclick="checkthis()"/>    		
				            <%
				            		}
				            		else {
				            %>
									<input id="historyThroughputSvc" type="checkbox" name="historyThroughputSvcRequired" onclick="checkthis()"/>    	
							<%
				            		}
				            	}
				            %>				
				            </td>
			        	</tr>
			        	<tr>
					        <td align="center" colspan="2">
					        	<input title="All Throughput information requested" type="button" name="all" class="button" value="All" onclick="checkAll()">&nbsp;
					        	<input title="Only the Throughput for GreenVulcano Nodes" type="button" name="nodes" class="button" value="Nodes" onclick="checkAllNodes()">&nbsp;
					        	<input title="Only the Throughput for GreenVulcano Services" type="button" name="services" class="button" value="Services" onclick="checkAllServices()">
					        </td>
				    	</tr>        
					</table>
				</td>
				<td class="center">
					<table class="data">
					<tr class="borderlabel">
						<td colspan="2"><font class="titlesmall">Graphic properties</font></td>
					</tr>
					<tr>
			   			<td>Http URL</td>
				    	<td>
		    		    	<input title="The URL value of servlet keeping JMX throughhput data" type="text" class="input120" name="httpUrl" value="<%=httpUrl%>"/>
		    			</td>
				    </tr>
				    <tr>
				    	<td>Frame Time&nbsp;</td>
				    	<td>
				    	    <input title="The millisecond value to keep the JMX throughput data" type="text" class="input120" name="frameTime" value="<%=frameTime%>"/>
				    	</td>
				    </tr>
		    		<tr>
				        <td align="center" colspan="2">
				        	<input title="Open only one window for the selected server" type="radio" name="graphicWindow" value="unique" checked>One Window
				        	<input title="Open different windows for every selected server" type="radio" name="graphicWindow" value="different">Different Windows
				        </td>
				    </tr>						
					</table>
				</td>
				<td class="center">
					<table class="data">
						<tr class="borderlabel">
							<td colspan="2"><font class="titlesmall">Location</font></td>
						</tr>
					<% 
						for(int i = 0; i < locationValues.length; ++i) {
					%>	
					
						<tr>
					    	<td><%=locationValues[i]%></td>
			    			<td>
			    				
			    				<%
			    					if ((locationRequired[i] != null) && (!locationRequired[i].equals("")) && (locationValues[i].equals("GLOBAL"))) {
			    				%>
					    				<input type="checkbox" id="location_<%=i%>" name="location_<%=i%>_Required" checked onclick="checklocation()"/>				    				
					    		<%
					    			}
					    			else {
					    		%>
										<input type="checkbox" id="location_<%=i%>" name="location_<%=i%>_Required" onclick="checklocation()"/>				    				
								<%
									}
								%>   			
					    	</td>
			    		</tr>
			    		<%
			    			}
			    		%>
					</table>
				</td>
			</tr>
			<tr>
			    <td align="center" colspan="3">
			      	<input type="submit" name="getThroughput" class="button" value="Get Throughput">
			    </td>
		    </tr>
	    </table>
    </form>
    </div>
<%@ include file="../end.jspf" %>  
