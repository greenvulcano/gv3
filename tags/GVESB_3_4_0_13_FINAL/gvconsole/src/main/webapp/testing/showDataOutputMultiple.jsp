<%@ page import="it.greenvulcano.gvesb.gvconsole.workbench.plugin.*"%>
<%@ page import="it.greenvulcano.gvesb.buffer.GVBuffer"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.Iterator"%>
<%
    String currentTest = (String) session.getAttribute("currentTest");
			String show = (String) request.getParameter("show");
			String reference = (String) request.getParameter("reference");
			String encoding = (String) request.getParameter("encoding");

			Map mapTestObject = (Map) session.getAttribute("mapTestObject");

			TestManager testManager = new TestManager(request);
			try {
				testManager.cleanCache(response);
			} catch (Throwable exc) {
				exc.printStackTrace();
			}

			if (mapTestObject != null) {
				TestGVBufferObject testObject = (TestGVBufferObject) mapTestObject
						.get(new Integer(reference));
				if (testObject != null) {
					GVBuffer output = (GVBuffer) testObject.getDataOutput();

					if (output != null) {
%>
<html>
<head>
<link rel="stylesheet" type="text/css" href="../css/styles.css">
</head>
<title>Output GVBuffer</title>
<body>
<table class="dataMultiple">
	<tr>
		<td valign='top'>
		<table align="center" width="98%">
			<tr>
				<td valign='top' CLASS="border" colspan="2"><FONT
					class="titlesmall">&nbsp;&nbsp;GVBUFFER&nbsp;OUTPUT&nbsp;</FONT></td>
			</tr>
			<tr>
				<td colspan="2">
				<hr></hr>
				</td>
			</tr>
			<!-- SYSTEM -->
			<tr>
				<td align="left">
				<h5>SYSTEM</h5>
				</td>
				<td align="left"><%=output.getSystem()%></td>
			</tr>
			<!-- END SYSTEM -->
			<!-- SERVICE -->
			<tr>
				<td align="left">
				<h5>SERVICE</h5>
				</td>
				<td align="left"><%=output.getService()%></td>
			</tr>
			<!-- END SERVICE -->
			<!-- ID -->
			<tr>
				<td align="left">
				<h5>ID</h5>
				</td>
				<td align="left"><%=output.getId().toString()%></td>
			</tr>
			<!-- END ID -->
			<!-- RETURN CODE -->
			<tr>
				<td align="left">
				<h5>RETURN CODE&nbsp;</h5>
				</td>
				<td align="left"><%=output.getRetCode()%></td>
			</tr>
			<!-- RETURN CODE -->
			<%
			    if (output.getPropertyNamesIterator().hasNext()) {
			%>
			<tr>
				<td colspan="2">
				<hr></hr>
				</td>
			</tr>
			<!-- PROPERTIES -->
			<tr>
				<td align="left" colspan="2">
				<h5>PROPERTIES</h5>
				</td>
			</tr>
			<%
			    Iterator iteratorOutput = output
												.getPropertyNamesIterator();
										while (iteratorOutput.hasNext()) {
											String field = (String) iteratorOutput.next();
											String value = output.getProperty(field);
											String name = "field_" + field;
			%>
			<tr>
				<td align="left"><nobr> <%=field%> </nobr></td>
				<td><%=value%></td>
			</tr>
			<%
			    }
									}
			%>
			<!-- END PROPERTIES -->

			<!-- DATA -->
			<tr>
				<td colspan="2">
				<hr></hr>
				</td>
			</tr>
			<tr>
				<td align="left">
				<h5>DATA</h5>
				</td>
			</tr>
			<tr>
				<%
				    if (show != null) {
											if (show.equals("text")) {
												if (encoding != null) {
													if (encoding.equals("text")) {
				%>
				<td align="left" size="2" colspan="2"><pre><%=TestPluginWrapper
												.encode(output.getObject()
														.toString())%></pre>
				</td>
				<%
				    } else {
				%>
				<td align="left" size="2" colspan="2"><pre><%=output.getObject()
														.toString()%></pre>
				</td>
				<%
				    }
												}
											} else {
												if (show.equals("binary")) {
				%>
				<td align="left" size="2" colspan="2"><pre><%=TestPluginWrapper
											.encode(TestPluginWrapper
													.dump(output.getObject()
															.toString()))%></pre>
				</td>
				<%
				    }
											}
										} // END show
				%>
			</tr>
			<!-- FINE DATA -->
			<%
			    }
							}
						}
			%>
		</table>
		</td>
	</tr>
</table>
<script>
            window.focus();
        </script>
</body>
</html>
