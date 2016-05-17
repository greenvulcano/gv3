<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:greenvulcano="it.greenvulcano.gvesb.gvcon.browse.Stylesheet"
>

    <xsl:param name="system"/>

    <!--===========================================================================================================
        Inizio.
    -->

    <xsl:template match="/GreenVulcano">
        <html>
            <head>
                <title>Configuration Browser - System <xsl:value-of select="$system"/></title>
                <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE"/> 
                <link rel="stylesheet" href="browseConfiguration/stile.css" type="text/css"/>
            </head>
            <body>
            
                <xsl:apply-templates select="Systems/System[@id-system=$system]"/>

            </body>
        </html>
    </xsl:template>

    <!--===========================================================================================================
        Informazioni relative al sistema.
    -->

    <xsl:template match="System">
        <table cellpadding="4" cellspacing="0" width="100%">
            <tr bgcolor="#009900" valign="top">
                <td><font class="titlelabel5">System <b><xsl:value-of select="$system"/></b></font></td>
            </tr>
        </table>
        <br/>
        <table>
            <tr valign="top">
                <td><font class="fieldlabel">
                    <nobr>Stato di attivazione</nobr>                    
                </font></td>
                <td width="20">
                </td>
                <td><font class="fieldlabel">
                    <b><xsl:value-of select="@system-activation"/></b>
                </font></td>
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
        <xsl:if test="Contact">
            <xsl:call-template name="contact-list"/>
        </xsl:if>
        <xsl:if test="not(Contact)">
            Nessun contatto disponibile per il sistema <b><xsl:value-of select="$system"/></b>.
        </xsl:if>
        <br/>
        <br/>
        <xsl:if test="Channel">
            <xsl:call-template name="channel-list"/>
        </xsl:if>
        <xsl:if test="not(Channel)">
            Non è stato configurato alcun canale di comunicazione per il sistema <b><xsl:value-of select="$system"/></b>.<br/>
            Il sistema <b><xsl:value-of select="$system"/></b> si comporta nei confronti di GreenVulcano esclusivamente come client.<br/>
            GreenVulcano non intraprende alcuna azione attiva per contattare il sistema <b><xsl:value-of select="$system"/></b>.
        </xsl:if>
		</font>
    </xsl:template>

    <!--===========================================================================================================
        Lista dei contatti.
    -->

    <xsl:template name="contact-list">
        Di seguito l'elenco dei contatti per il sistema <b><xsl:value-of select="$system"/></b>:
        <br/>
        <br/>
        <table cellpadding="4" cellspacing="0" width="100%">
            <tr bgcolor="#009900" valign="top">
                <td><font class="titlelabel6">First name</font></td>
                <td width="20"></td>
                <td><font class="titlelabel6">Last name</font></td>
                <td width="20"></td>
                <td><font class="titlelabel6">Qualification</font></td>
                <td width="20"></td>
                <td><font class="titlelabel6">Mobile</font></td>
                <td width="20"></td>
                <td><font class="titlelabel6">Phone</font></td>
                <td width="20"></td>
                <td><font class="titlelabel6">E-mail</font></td>
            </tr>

            <xsl:for-each select="Contact">
                <xsl:sort select="@last-name"/>
                <tr valign="top">
                    <td><font class="fieldlabel"><nobr><xsl:value-of select="@first-name"/></nobr></font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel"><nobr><xsl:value-of select="@last-name"/></nobr></font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel"><nobr><xsl:value-of select="@qualification"/></nobr></font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel"><nobr><xsl:value-of select="@mobile"/></nobr></font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel"><nobr><xsl:value-of select="@telephone"/></nobr></font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel"><nobr><a class="link" href="mailto:{@e-mail}"><xsl:value-of select="@e-mail"/></a></nobr></font></td>
                </tr>
                <tr>
                    <td colspan="11"><hr/></td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>

    <!--===========================================================================================================
        Lista dei canali di comunicazione.
    -->

    <xsl:template name="channel-list">
        Di seguito l'elenco dei canali di comunicazione configurati per il sistema <b><xsl:value-of select="$system"/></b>.<br/>
        Per ogni canale sono riportate, con una breve descrizione, le operazioni in esso contenute.<br/>
        Inoltre per ogni canale sono indicati anche i servizi ed i client che lo utilizzano.
        <br/>
        <br/>
        <table cellpadding="4" cellspacing="0" width="100%">
            <tr bgcolor="#009900" valign="top">
                <td><font class="titlelabel6">Channel</font></td>
                <td width="20"></td>
                <td><font class="titlelabel6">Description</font></td>
            </tr>

            <xsl:for-each select="Channel">
                <xsl:sort select="@id-channel"/>
                <tr valign="top">
                    <td><font class="fieldlabel"><nobr><b><xsl:value-of select="@id-channel"/></b></nobr></font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel">
                        <xsl:value-of select="Description"/>
                        <p/>
                        <xsl:if test="*[@type='call' or @type='dequeue' or @type='enqueue']">
                            <xsl:call-template name="operation-list"/>
                        </xsl:if>
                        <xsl:if test="not(*[@type='call' or @type='dequeue' or @type='enqueue'])">
                            Nessuna interfaccia di comunicazione definita nel canale di comunicazione <b><xsl:value-of select="@id-channel"/></b>.
                        </xsl:if>
                        <p/>
                        <xsl:if test="/GreenVulcano/Services/Service/Clients/Client[Operation/Participant/@id-system=$system and Operation/Participant/@id-channel=current()/@id-channel]">
                            <xsl:call-template name="channel-service-list"/>
                        </xsl:if>
                        <xsl:if test="not(/GreenVulcano/Services/Service/Clients/Client[Operation/Participant/@id-system=$system and Operation/Participant/@id-channel=current()/@id-channel])">
                            Nessuna servizio configurato usa il canale di comunicazione <b><xsl:value-of select="@id-channel"/></b>.
                        </xsl:if>
                        </font>
                    </td>
                </tr>
                <tr>
                    <td colspan="3"><hr/></td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>

    <!--===========================================================================================================
        Lista delle operazioni di comunicazione.
    -->

    <xsl:template name="operation-list">
        Di seguito l'elenco delle interfacce di comunicazione configurate nel canale <b><xsl:value-of select="@id-channel"/></b>:
        <br/>
        <br/>
        <table cellpadding="4" cellspacing="0" width="100%">
            <tr bgcolor="#009900" valign="top">
                <td><font class="titlelabel7">Operation</font></td>
                <td width="20"></td>
                <td><font class="titlelabel7">Type</font></td>
                <td width="20"></td>
                <td><font class="titlelabel7">Description</font></td>
            </tr>

            <xsl:call-template name="sub-operation-list"/>
        </table>
    </xsl:template>


    <xsl:template name="sub-operation-list">
        <xsl:for-each select="*[@type='call' or @type='dequeue' or @type='enqueue']">
            <tr valign="top">
                <td><font class="fieldlabel"><nobr><b><xsl:value-of select="@name"/></b></nobr></font></td>
                <td width="20"></td>
                <td><font class="fieldlabel"><nobr><xsl:value-of select="name()"/></nobr><br/>(<xsl:value-of select="@type"/>)</font></td>
                <td width="20"></td>
                <td><font class="fieldlabel">
                    <xsl:value-of select="Description"/>
                    <p/>
                    <xsl:apply-templates select="."/>
                    </font>
                </td>
            </tr>
            <tr>
                <td colspan="5"><hr/></td>
            </tr>
            <xsl:call-template name="sub-operation-list"/>
        </xsl:for-each>
    </xsl:template>

    <!--===========================================================================================================
        Lista dei servizi per canale.
    -->

    <xsl:template name="channel-service-list">
        Di seguito l'elenco dei servizi che utilizzano il canale <b><xsl:value-of select="@id-channel"/></b> del sistema <b><xsl:value-of select="$system"/></b>.<br/>
        Per ogni servizio è indicato il sistema client che attiva il canale e le primitive GreenVulcano coinvolte:
        <br/>
        <br/>
        <table cellpadding="4" cellspacing="0" width="100%">
            <tr bgcolor="#009900" valign="top">
                <td><font class="titlelabel7">Client</font></td>
                <td width="20"></td>
                <td><font class="titlelabel7">Service</font></td>
                <td width="20"></td>
                <td><font class="titlelabel7">GreenVulcano API</font></td>
            </tr>

            <xsl:variable name="id-channel" select="@id-channel"/>

            <xsl:for-each select="/GreenVulcano/Services/Service/Clients/Client[Operation/Participant/@id-system=$system and Operation/Participant/@id-channel=$id-channel]">
                <xsl:sort select="ancestor::Service/@id-service"/>
                <tr valign="top">
                    <td><font class="fieldlabel"><nobr><b><a class="link" href="?cmd=system&amp;system={@id-system}"><xsl:value-of select="@id-system"/></a></b></nobr></font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel"><nobr><b><a class="link" href="?cmd=service&amp;service={ancestor::Service/@id-service}"><xsl:value-of select="ancestor::Service/@id-service"/></a></b></nobr></font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel">
                        <xsl:for-each select="Operation[Participant/@id-system=$system and Participant/@id-channel=$id-channel]">
                            <nobr>
                                <xsl:value-of select="@name"/>
                                <xsl:if test="@forward-name">
                                    (<xsl:value-of select="@forward-name"/>)
                                </xsl:if>
                            </nobr>
                            <br/>
                        </xsl:for-each>
                        </font>
                    </td>
                </tr>
                <tr>
                    <td colspan="5"><hr/></td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>

    <!--===========================================================================================================
        Templates per le operazioni.
        Presi dal DTD e aggiustati leggermente.
    -->

        <xsl:template match="j2ee-ejb-call">
            <table cellpadding="0" cellspacing="0">
                <tr valign="top">
                    <td width="100"><font class="fieldlabel">JNDI Name:</font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel"><b><xsl:value-of select="@jndi-name"/></b></font></td>
                </tr>
                <tr valign="top">
                    <td width="100"><font class="fieldlabel">Method Name:</font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel"><b><xsl:value-of select="@method"/></b></font></td>
                </tr>
                <xsl:if test="@provider-url">
                    <tr valign="top">
                        <td width="100"><font class="fieldlabel">Provider URL:</font></td>
                        <td width="20"></td>
                        <td><font class="fieldlabel"><b><xsl:value-of select="@provider-url"/></b></font></td>
                    </tr>
                </xsl:if>
            </table>
        </xsl:template>

        <xsl:template match="jca-call">
            <table cellpadding="0" cellspacing="0">
                <tr valign="top">
                    <td><font class="fieldlabel">JNDI name</font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel"><b><xsl:value-of select="@jndi-name-eis"/></b></font></td>
                </tr>
                <tr valign="top">
                    <td><font class="fieldlabel">Function</font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel"><b><xsl:value-of select="@funzione"/></b></font></td>
                </tr>
            </table>
        </xsl:template>

        <xsl:template match="r4-r3-bridge">
            <table cellpadding="0" cellspacing="0">
                <tr valign="top">
                    <td width="100"><font class="fieldlabel">JNDI Name:</font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel"><b><xsl:value-of select="@jndi-name"/></b></font></td>
                </tr>
                <tr valign="top">
                    <td width="100"><font class="fieldlabel">Method Name:</font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel"><b><xsl:value-of select="@method"/></b></font></td>
                </tr>
                <xsl:if test="@provider-url">
                    <tr valign="top">
                        <td width="100"><font class="fieldlabel">Provider URL:</font></td>
                        <td width="20"></td>
                        <td><font class="fieldlabel"><b><xsl:value-of select="@provider-url"/></b></font></td>
                    </tr>
                </xsl:if>
            </table>
        </xsl:template>

        <xsl:template match="test-service-call">
            <table cellpadding="0" cellspacing="0">
                <tr valign="top">
                    <td width="100"><font class="fieldlabel">Service:</font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel"><b><xsl:value-of select="@service"/></b></font></td>
                </tr>
            </table>
        </xsl:template>

        <xsl:template match="generic-call">
            <table cellpadding="0" cellspacing="0">
                <tr valign="top">
                    <td width="100"><font class="fieldlabel">Class:</font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel"><b><xsl:value-of select="@class"/></b></font></td>
                </tr>
                <xsl:if test="parameter">
                    <xsl:for-each select="parameter">
                        <tr valign="top">
                            <td width="100"><font class="fieldlabel"><xsl:value-of select="@name"/><xsl:text>: </xsl:text></font></td>
                            <td width="20"></td>
                            <td><font class="fieldlabel"><b><xsl:value-of select="@value"/></b></font></td>
                        </tr>
                    </xsl:for-each>
                </xsl:if>
            </table>
        </xsl:template>

        <xsl:template match="routed-call">
            <table cellpadding="0" cellspacing="0">
                <tr valign="top">
                    <td><font class="fieldlabel">Condition</font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel">VMOperation</font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel">Description</font></td>
                </tr>
                <tr height="5">
                    <td colspan="5"></td>
                </tr>
                <xsl:for-each select="VMRouting">
                    <tr valign="top">
                        <td><font class="fieldlabel"><b><xsl:value-of select="@condition"/></b></font></td>
                        <td></td>
                        <td><font class="fieldlabel"><b><xsl:value-of select="@operation-name"/></b></font></td>
                        <td></td>
                        <td><font class="fieldlabel"><small><xsl:value-of select="Description"/></small></font></td>
                    </tr>
                </xsl:for-each>
            </table>
        </xsl:template>

        <xsl:template match="routed-enqueue">
            <table cellpadding="0" cellspacing="0">
                <tr valign="top">
                    <td><font class="fieldlabel">Condition</font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel">VMOperation</font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel">Description</font></td>
                </tr>
                <tr height="5">
                    <td colspan="5"></td>
                </tr>
                <xsl:for-each select="VMRouting">
                    <tr valign="top">
                        <td><font class="fieldlabel"><b><xsl:value-of select="@condition"/></b></font></td>
                        <td></td>
                        <td><font class="fieldlabel"><b><xsl:value-of select="@operation-name"/></b></font></td>
                        <td></td>
                        <td><font class="fieldlabel"><small><xsl:value-of select="Description"/></small></font></td>
                    </tr>
                </xsl:for-each>
            </table>
        </xsl:template>

        <xsl:template match="routed-dequeue">
            <table cellpadding="0" cellspacing="0">
                <tr valign="top">
                    <td><font class="fieldlabel">Condition</font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel">VMOperation</font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel">Description</font></td>
                </tr>
                <tr height="5">
                    <td colspan="5"></td>
                </tr>
                <xsl:for-each select="VMRouting">
                    <tr valign="top">
                        <td><font class="fieldlabel"><b><xsl:value-of select="@condition"/></b></font></td>
                        <td></td>
                        <td><font class="fieldlabel"><b><xsl:value-of select="@operation-name"/></b></font></td>
                        <td></td>
                        <td><font class="fieldlabel"><small><xsl:value-of select="Description"/></small></font></td>
                    </tr>
                </xsl:for-each>
            </table>
        </xsl:template>

        <xsl:template match="jms-enqueue">
            <table cellpadding="0" cellspacing="0">
                <tr valign="top">
                    <td><font class="fieldlabel">JNDI name:</font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel"><b><xsl:value-of select="@destination-name"/></b></font></td>
                </tr>
                <xsl:if test="@provider-url">
                    <tr valign="top">
                        <td><font class="fieldlabel">Provider URL</font></td>
                        <td width="20"></td>
                        <td><font class="fieldlabel"><b><xsl:value-of select="@provider-url"/></b></font></td>
                    </tr>
                </xsl:if>
            </table>
        </xsl:template>

        <xsl:template match="jms-dequeue">
            <table cellpadding="0" cellspacing="0">
                <tr valign="top">
                    <td><font class="fieldlabel">JNDI name:</font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel"><b><xsl:value-of select="@destination-name"/></b></font></td>
                </tr>
                <xsl:if test="@provider-url">
                <tr valign="top">
                    <td><font class="fieldlabel">Provider URL</font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel"><b><xsl:value-of select="@provider-url"/></b></font></td>
                </tr>
                </xsl:if>
            </table>
        </xsl:template>

</xsl:stylesheet>
