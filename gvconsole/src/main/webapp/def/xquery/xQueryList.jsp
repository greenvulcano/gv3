<%@ page import="it.greenvulcano.gvesb.gvconsole.gvcon.xquery.XQueriesManager"%>
<%@ page import="it.greenvulcano.gvesb.gvconsole.gvcon.xquery.XQueriesElement"%>
<jsp:useBean id="xQueriesElement" class="it.greenvulcano.gvesb.gvconsole.gvcon.xquery.XQueriesElement" scope="request"/>
<jsp:setProperty name="xQueriesElement" property="*"/>
<%
	XQueriesManager manager = (XQueriesManager)session.getAttribute("xQueriesManager");
	String delete = request.getParameter("delete");
	if(manager==null){
		manager = new XQueriesManager();
		session.setAttribute("xQueriesManager",manager);
	}
	try{
	    if(delete!=null && delete.equals("true"))
	    {
	        manager.deleteElement(xQueriesElement);   
	    }
	    else
	    {
			manager.updateConfig(xQueriesElement);
	    }
	}catch(Throwable t){
		t.printStackTrace();
	}
%>
<form method="post" name="xQueriesElement" action="index.jsp">
	<input type="hidden" name="nome" id="_nome"/>
	<input type="hidden" name="descrizione" id="_descrizione"/>
	<input type="hidden" name="xqueryString" id="_xqueryString"/>
	<input type="hidden" name="delete" id="_delete" value="false"/>
</form>
	<TABLE class="xquery">
		<TR class="xquery">
			<TD class="xquery">
				<nobr>Select:&nbsp;&nbsp;
				<select id="selectQS" name="selectQS" onchange="changeValue()">
				<option></option>
			<% 
				for(int i=0; i<manager.getNumberOfElements(); i++)
				{
					XQueriesElement element = manager.getXQueriesElement(i);
					String nome = element.getNome();
					String xQueryString = element.getXqueryString();
					String descrizione = element.getDescrizione();
			%>
				<option value="<%=xQueryString%>" label="<%=descrizione%>" ><%=nome%></option>
			<%
				}
			%>			
			</select></nobr>
			<input type="button" class="button" name="deleteXQ" value="Del XQuery" onclick="deleteXQ()"> 
			</TD>
		</TR>
		<TR class="xquery">
			<TD class="xquery">
				<br/>
				Description:<br/>
				<textarea class="description" id="descrizioneQS" name="descrizioneQS" readonly></textarea>
			</TD>
		</TR>
	</TABLE>
<script language="JavaScript">
	function changeValue()
	{
		sel = document.getElementById("selectQS");
		i = sel.selectedIndex;
		document.getElementById("descrizioneQS").value = sel.getElementsByTagName("option")[i].label;
		document.getElementById("text").value = sel.value;
	}
	function deleteXQ()
	{
		if(window.confirm("Vuoi cancellare la xquery selezionata?"))
		{
			sel = document.getElementById("selectQS");
 	 	    i = sel.selectedIndex;
     		var xQuery = document.xQueryBean.text.value;
     		var nome = sel.getElementsByTagName("option")[i].text;
     		var descrizione = sel.getElementsByTagName("option")[i].label;

			document.getElementById("_delete").value="true";
			document.getElementById("_nome").value = nome;
			document.getElementById("_descrizione").value = descrizione;
			document.getElementById("_xqueryString").value = xQuery;
			
			document.forms.xQueriesElement.submit();
		}
	}
</script>