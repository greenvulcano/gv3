<%@ page import="java.util.*" %>
<%@ page import="javax.management.*" %>
<%@ page import="it.greenvulcano.jmx.*" %>
<%@ page import="it.greenvulcano.configuration.jmx.*" %>
<%@ taglib uri="/WEB-INF/maxime.tld" prefix="max" %>
<%
  session.removeAttribute("currentMenu");
  session.setAttribute("currentMenu", "utility");
%>
<%@ include file="../head.jspf" %>
<%
    try {

        JMXEntryPoint jmx = JMXEntryPoint.instance();
        MBeanServer server = jmx.getServer();
        Set set = server.queryNames(new ObjectName(XMLConfigProxy.JMX_FILTER), null);
        Iterator iterator = set.iterator();
        ObjectName objectName = (ObjectName)iterator.next();

        String files[] = (String[])server.getAttribute(objectName, "loadedFiles");
        Arrays.sort(files);
%>
        <script>

            var msg1 = "Do you want to reload all configuration files?"
                + "\n\nIf you want reload only few files, select them and use the 'Reload' command."
                + "\n\nSelect 'OK' to reload all files, select 'Cancel' to cancel the operation";

            function reloadAll(cmd) {
                if(confirm(msg1)) {
                    doReload(cmd);
                }
            }

            var msg2 = "Do you want to reload selected files?"
                + "\n\nSelect 'OK' to reload selected files, select 'Cancel' to cancel the operation";

            function reload(cmd) {
                if(confirm(msg2)) {
                    doReload(cmd);
                }
            }

            function doReload(cmd) {
                $('input:hidden[name=command]').val(cmd);
                $('form[name=ReloadForm]').submit();
            }

        </script>

        <div class="titleSection"><h1>Configuration monitoring</h1></div>

        <form name="ReloadForm">
            <table cellpadding="4" class="ui-widget-header ui-corner-all">
                <tr class="search">
                    <td>
                        <b>Loaded configuration files:</b>
                        <small><a href=".">[Refresh]</a></small>
                    </td>
                </tr>
                <tr class="search">
                    <td class="border">
<%
                        for(int i = 0; i < files.length; ++i) {
%>
                            <nobr>
                                <input type="checkbox" name="file" value="<%= files[i] %>">
                                <b><%= files[i] %></b>
                            </nobr>
                            <br/>
<%
                        }
%>
                    </td>
                    <td>
                    </td>
                    <td>
                        The table shows the configuration files currently loaded by
                        GreenVulcano.
                        <p>
                        To force the hot reloading of the configuration,
                        select the files to reload and click the <b>Reload</b> button.
                        </p>
                        <p>
                        To reload all files click the <b>Reload all</b> button.<br>
                        Note that if you want to reload only few files, it is recommended
                        that you use the <b>Reload</b> button.
                        </p>
                    </td>
                </tr>
                <tr class="search">
                    <td colspan="3">
                        <hr>
                    </td>
                </tr>
                <tr class="search">
                    <td colspan="3">
                        <input type="hidden" class="button" name="command">
                        <input type="button" class="button" value="Reload" onclick="reload('Reload')">
                        <input type="button" class="button" value="Reload all" onclick="reloadAll('Reload all')">
                    </td>
                </tr>
            </table>
        </form>

        <%@ include file="execute.jspf" %>
<%
    }
    catch(Throwable throwable) {
%>
        <%@ include file="exception.jspf" %>
<%
    }
%>
<br><br><br><br><br><br>
<%@ include file="../end.jspf" %>