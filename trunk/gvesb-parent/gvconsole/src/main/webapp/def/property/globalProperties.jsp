<%@ page import="it.greenvulcano.gvesb.gvconsole.gvcon.property.GlobalProperty"%>
<%@ page isELIgnored="false" %>

<%@ page import="java.util.*" %>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>

<%@ include file="../../head.jspf" %>

<%!
    String getColorStyle(String p, String u, String v){
        Boolean present = new Boolean(p);
        boolean used = (!u.isEmpty() && u!=null);
        boolean valued = (!v.isEmpty() && v!=null);
        
        if(present && used && valued) return "black";
        if(present && used && !valued) return "orange";
        if(!present && used) return "red";
        if(present && !used) return "orange";
        return "none";
    }
%>

<%
    //String mode = (String) request.getParameter("mode");
    String mode = (String) session.getAttribute("MODE");
%>
    <script type="text/javascript" src="<%=contextRoot%>/js/jquery-1.4.2.min.js"></script>

    <style type="text/css" src="<%=contextRoot%>/css/jquery-ui-1.8.custom.css"></style>
    <script type="text/javascript" src="<%=contextRoot%>/js/jquery-ui-1.8.custom.min.js"></script>

    <script type="text/javascript" src="<%=contextRoot%>/def/property/globalProperties.js"></script>
    <link rel="stylesheet" type="text/css" href="<%=contextRoot%>/def/property/globalProperties.css"/>

    <div class="titleSection"><h1>Configuration properties</h1>

    This page can be used in order to edit the GVESB global properties saved in <i>XMLConfig.properties</i> configuration file.<br>
    <p></p></div>

    <br/>
    <div class="ui-widget-header central">
        <html:form action="/property/HandlePropertiesAction" onsubmit="return checkForm(this);">
            <html:hidden styleId="skipValidation" property="skipValidation" value="true"/>
            <%-- <html:hidden styleId="methodToCall" property="methodToCall" value=""/> --%>

            <table class="norm_val_details">
                <tr>
                    <th>Crypted</th>
                    <th>Name</th>
                    <th>Value</th>
                </tr>
                <logic:iterate id="properties" name="PropertiesEditorForm" property="properties" indexId="idx">
                    <tr id='<%="tr_"+idx%>'>
                        <td id='<%="td_"+idx%>' class="centered crptcontainer" valign="top" nowrap>
                            <bean:define id="encptd" name="properties" property="encrypted"/>
                            <bean:define id="usedin" name="properties" property="strUsedIn" type="java.lang.String"/>
                            <bean:define id="present" name="properties" property="present" type="java.lang.String"/>
                            <bean:define id="value" name="properties" property="value" type="java.lang.String"/>
                            <html:hidden styleId='<%="cptd_"+idx%>' indexed="true" name="properties" property="encrypted"/>
    <%
        String onClick = "";
        String msg = "";
        if(!mode.equals("view")){
            onClick = "onclick=\"toggleCrypted("+idx+", '"+contextRoot+"')\"";
        }
        if(encptd.toString().equals("true")){
            msg = (mode.equals("view"))?"Encrypted":"Encrypted. Click to decrypt.";
    %>
                            <img id='<%="img_"+idx%>' title="<%=msg%>" class="crptico" alt="Encrypted" src="<%=contextRoot%>/images/properties/encrypted.png" <%=onClick%>/>
    <%
        }
        else{
            msg = (mode.equals("view"))?"Decrypted":"Decrypted. Click to encrypt.";
    %>
                            <img id='<%="img_"+idx%>' title="<%=msg%>" class="crptico" alt="Decrypted" src="<%=contextRoot%>/images/properties/decrypted.png" <%=onClick%>/>
    <%
        }
    %>
                        </td>
    <%
            String usdInMsg = (usedin.isEmpty())?"Not Used":"Used in: " + usedin;
    %>
                        <td class="<%=getColorStyle(present, usedin, value)%> name" title="<%=usdInMsg%>" valign="top" nowrap>
                            <%-- <bean:define id="profile" name="formName"/> --%>
                            <bean:write name="properties" property="name"/>
                        </td>
                        <td class="centered" valign="top" nowrap>
                            <html:text name="properties" property="value" styleClass="<%=mode%>" size="60" indexed="true"/>
                            <%-- <html:text name="properties" property="value" size="60" indexed="true"/> --%>
                        </td>
                    </tr>
                </logic:iterate>
            </table>
            <div class="buttonpad">
                <html:submit styleId="revert" property="methodToCall" onclick="buttonPress(0)" styleClass='<%=mode+" button"%>'>
                    <bean:message key="globalprops.revert"/>
                </html:submit>
                <html:submit styleId="save" property="methodToCall" onclick="buttonPress(1)" styleClass='<%=mode+" button"%>'>
                    <bean:message key="globalprops.save"/>
                </html:submit>
            </div>
        </html:form>
    </div>
    <br>

<%@ include file="../../end.jspf" %>
