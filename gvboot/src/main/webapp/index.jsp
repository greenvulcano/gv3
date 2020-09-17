<HTML>
    <HEAD>
        <TITLE>GVBoot - GreenVulcano boot</TITLE>
    </HEAD>
    <BODY>
        GVBoot (GreenVulcano boot) is a simple web application that, at sturtup time,
        performs GreenVulcano initialization tasks.
        <p/>
        If you can see this page, that GVBoot is started and the server hosting it
<%
        String serverName = System.getProperty("weblogic.Name");
        if(serverName == null) {
            serverName = System.getProperty("jboss.server.name");
        }
%>
            (<%= serverName %>) 
        is started.
        <p/>
        Please check GreenVulcano logs and server logs if you are esperiencing some
        problems.
    </BODY>
</HTML>