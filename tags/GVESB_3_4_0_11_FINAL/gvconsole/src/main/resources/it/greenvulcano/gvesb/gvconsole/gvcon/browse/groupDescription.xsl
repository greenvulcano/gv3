<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:greenvulcano="it.greenvulcano.gvesb.gvcon.browse.Stylesheet"
>

    <xsl:param name="group"/>


    <!--===========================================================================================================
        Inizio.
    -->

    <xsl:template match="/GreenVulcano">
        <html>
            <head>
                <title>Configuration Browser - Group <xsl:value-of select="$group"/></title>
                <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE"/> 
                <link rel="stylesheet" href="browseConfiguration/stile.css" type="text/css"/>
            </head>
            <body>
            
                <xsl:apply-templates select="Groups/Group[@id-group=$group]"/>

            </body>
        </html>
    </xsl:template>


    <!--===========================================================================================================
        Informazioni relative al gruppo.
    -->

    <xsl:template match="Group">
        <table cellpadding="4" cellspacing="0" width="100%">
            <tr bgcolor="#009900" valign="top">
                <td><font class="titlelabel5">Group <b><xsl:value-of select="$group"/></b></font></td>
            </tr>
        </table>
        <br/>
        <table>
            <tr valign="top">
                <td><font class="fieldlabel">
                    <nobr>Stato di attivazione</nobr>
                    </font>
                </td>
                <td width="20">
                </td>
                <td><font class="fieldlabel">
                    <b><xsl:value-of select="@group-activation"/></b>
                    </font>
                </td>
            </tr>
            <tr valign="top">
                <td><font class="fieldlabel">
                    Descrizione
                </font></td>
                <td width="20">
                </td>
                <td><font class="fieldlabel">
                    <xsl:value-of select="Description"/>
                </font></td>
            </tr>
        </table>
        
        <hr/>
        <br/>
        <br/>
        <font class="fieldlabel">
        <xsl:if test="/GreenVulcano/Services/Service[@group-name=$group]">
            <xsl:call-template name="service-list"/>
        </xsl:if>
        <xsl:if test="not(/GreenVulcano/Services/Service[@group-name=$group])">
            Il gruppo <b><xsl:value-of select="$group"/></b> non ha alcun servizio associato.
        </xsl:if>
        </font>
    </xsl:template>


    <!--===========================================================================================================
        Lista dei servizi.
    -->

    <xsl:template name="service-list">    	
        Di seguito l'elenco dei servizi del gruppo <b><xsl:value-of select="$group"/></b> con una breve descrizione e lo stato di attivazione:        
        <br/>
        <br/>
        <table cellpadding="4" cellspacing="0" width="100%">
            <tr bgcolor="#009900" valign="top">
                <td><font class="titlelabel6">Service</font></td>
                <td width="20"></td>
                <td><font class="titlelabel6">Activation</font></td>
                <td width="20"></td>
                <td><font class="titlelabel6">Description</font></td>
            </tr>

            <xsl:for-each select="/GreenVulcano/Services/Service[@group-name=$group]">
                <xsl:sort select="@id-service"/>
                <tr valign="top">
                    <td><font class="fieldlabel"><b><a class="link" href="?cmd=service&amp;service={@id-service}"><xsl:value-of select="@id-service"/></a></b></font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel"><xsl:value-of select="@service-activation"/></font></td>
                    <td width="20"></td>
                    <td>
                    	<font class="fieldlabel">
                        <xsl:value-of select="Description"/>
                        <table>
                            <xsl:for-each select="Notes">
                                <tr valign="top">
                                    <td><font class="fieldlabel"><xsl:value-of select="@date"/></font></td>
                                    <td width="20"></td>
                                    <td><font class="fieldlabel"><xsl:value-of select="Description"/></font></td>
                                </tr>
                            </xsl:for-each>
                        </table>
                        </font>
                    </td>
                </tr>
                <tr>
                    <td colspan="5"><hr/></td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>

</xsl:stylesheet>