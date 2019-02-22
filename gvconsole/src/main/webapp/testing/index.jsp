<%@ page
	import="it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestManager"%>
	<%@ page
	import="it.greenvulcano.gvesb.gvconsole.workbench.plugin.TestPlugin"%>
		<%
  session.removeAttribute("currentMenu");
  session.setAttribute("currentMenu", "testing");
%>

			<%@ include file="../head.jspf"%>
				<%
    String currentTest = "Core";
        session.setAttribute("currentTest", currentTest);
        String descriptionTest = "In this section you can test the Core functions calling the Core Ejb methods";
        String firstTime = (String) request.getAttribute("firstTime");
        if (firstTime == null) {
            firstTime = request.getParameter("firstTime");
        }
        TestManager testManager = new TestManager(request);
        TestPlugin testPlugin = testManager.getPlugin();
        if (firstTime != null) {
            if (firstTime.equals("yes")) {
                String resetValue = testPlugin.getResetValue();
                if (resetValue == null) {
                    testPlugin.clear(request);
                    testManager.reset();
                }
                else {
                    testPlugin.setResetValue(null);
                }
            }
        }
%>
					<div class="titleSection">
						<h1>Testing</h1>
					</div>
					<table>
						<tr>
							<th>
								<%@ include file="gvTest.jspf"%>
							</th>
						</tr>
					</table>

					<%@ include file="../end.jspf"%>