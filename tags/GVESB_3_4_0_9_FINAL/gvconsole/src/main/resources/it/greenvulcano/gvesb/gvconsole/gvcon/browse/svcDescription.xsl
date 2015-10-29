<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:greenvulcano="it.greenvulcano.gvesb.gvcon.browse.Stylesheet"
    xmlns:gvcon="it.greenvulcano.gvesb.gvcon.jgraph.Stylesheet"
>

    <xsl:param name="service"/>
    <xsl:param name="serverpath"/>

    <!--===========================================================================================================
        Inizio.
    -->

    <xsl:template match="/GreenVulcano">
        <html>
            <head>            	
                <title>Configuration Browser - Service <xsl:value-of select="$service"/></title>
                <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE"/> 
                <link rel="stylesheet" href="browseConfiguration/stile.css" type="text/css"/>
            </head>
            <body>
            
                <xsl:apply-templates select="Services/Service[@id-service=$service]"/>

            </body>
        </html>
    </xsl:template>

    <!--===========================================================================================================
        Informazioni relative al servizio.
    -->

    <xsl:template match="Service">
        <table cellpadding="4" cellspacing="0" width="100%">
            <tr bgcolor="#009900" valign="top">
                <td><font class="titlelabel5">Service <b><xsl:value-of select="$service"/></b></font></td>
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
                    <b><xsl:value-of select="@service-activation"/></b>                    
                </font></td>
            </tr>
            <tr valign="top">
                <td><font class="fieldlabel">
                    <nobr>Gruppo</nobr>
                </font></td>
                <td width="20">
                </td>
                <td><font class="fieldlabel">
                    <b><a class="link" href="?cmd=group&amp;group={@group-name}"><xsl:value-of select="@group-name"/></a></b>
                </font></td>
            </tr>
             <tr valign="top">
                <td><font class="fieldlabel">
                    <nobr>Data di creazione</nobr>
                </font></td>
                <td width="20">
                </td>
                <td><font class="fieldlabel">
                    <b><xsl:value-of select="@creation-date"/></b>
                </font></td>
            </tr>
             <tr valign="top">
                <td><font class="fieldlabel">
                    <nobr>Descrizione breve</nobr>
                </font></td>
                <td width="20">
                </td>
                <td><font class="fieldlabel">
                    <b><xsl:value-of select="@brief-description"/></b>
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
                    <table>
                        <xsl:for-each select="Notes">
                            <tr valign="top">
                                <td><font class="fieldlabel"><xsl:value-of select="@date"/></font></td>
                                <td width="20"></td>
                                <td><font class="fieldlabel"><xsl:value-of select="Description"/></font></td>
                            </tr>
                        </xsl:for-each>
                    </table>
                </font></td>
            </tr>
        </table>
        
        <hr/>
        <br/>
        <br/>
        <font class="fieldlabel">
        <xsl:if test="Clients/Client">
            <xsl:call-template name="client-list"/>
        </xsl:if>
        <xsl:if test="not(Clients/Client)">
            Nessun client configurato per il servizio <b><xsl:value-of select="$service"/></b>.
        </xsl:if>
		</font>
    </xsl:template>

    <!--===========================================================================================================
        Lista dei clients.
    -->

    <xsl:template name="client-list">    	
        Di seguito l'elenco dei sistemi client abilitati ad invocare il servizio <b><xsl:value-of select="$service"/></b>.<br/>
        Per ogni sistema client, sono indicate anche le primitive GreenVulcano configurate per il colloquio.        
        <br/>
        <br/>
        <table cellpadding="4" cellspacing="0" width="100%">
            <tr bgcolor="#009900" valign="top">
                <td><font class="titlelabel6">Client</font></td>
                <td width="20"></td>
                <td><font class="titlelabel6">Description</font></td>
            </tr>

            <xsl:for-each select="Clients/Client">
                <xsl:sort select="@id-system"/>
                <tr valign="top">
                    <td><font class="fieldlabel"><nobr><b><a class="link" href="?cmd=system&amp;system={@id-system}"><xsl:value-of select="@id-system"/></a></b></nobr></font></td>
                    <td width="20"></td>
                    <td><font class="fieldlabel">
                        <xsl:value-of select="Description"/>
                        <p/>
                        <xsl:if test="Operation">
                            <xsl:call-template name="operation-list"/>
                        </xsl:if>
                        <xsl:if test="not(Operation)">
                            Nessuna primitiva GreenVulcano configurata il client <b><xsl:value-of select="@id-system"/></b>.
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
        Lista delle operazioni GreenVulcano.
    -->

    <xsl:template name="operation-list">    	
        Di seguito l'elenco delle primitive GreenVulcano configurate per il client <b><xsl:value-of select="@id-system"/></b>,
        i sistemi, con i corrispondenti canali, coinvolti nella comunicazione.
        Per ogni primitiva è inoltre indicato il template di comunicazione utilizzato.        
        <br/>
        <br/>
        <table cellpadding="4" cellspacing="0" width="100%">
            <tr bgcolor="#009900" valign="top">
                <td><font class="titlelabel7">GreenVulcano API</font></td>
                <td width="20"></td>
                <td><font class="titlelabel7">System <i>(channel)</i></font></td>
                <td width="20"></td>
                <td><font class="titlelabel7">Template <i>(schema)</i></font></td>
                <td width="20"></td>
                <td><font class="titlelabel7">Description</font></td>
            </tr>

            <xsl:for-each select="Operation">
                <tr valign="top">
                    <td bgcolor="#009900">
                        <font class="titlelabel8">
                        <nobr>
                            <b>
                                <xsl:value-of select="@name"/>
                                <xsl:if test="@forward-name">
                                    <br/>(<xsl:value-of select="@forward-name"/>)
                                </xsl:if>
                            </b>
                        </nobr>
                        </font>
                    </td>
                    <td width="20"></td>
                    <td>
                    	<font class="fieldlabel">
                        <xsl:for-each select="Participant">
                            <xsl:sort select="@id-system"/>
                            <nobr>
                                <a class="link" href="?cmd=system&amp;system={@id-system}"><b><xsl:value-of select="@id-system"/></b></a>
                                <xsl:text> </xsl:text>
                                <i>(<xsl:value-of select="@id-channel"/>)</i>
                            </nobr>
                            <br/>
                        </xsl:for-each>
                        </font>
                    </td>
                    <td width="20"></td>
                    <td><font class="fieldlabel">
                        <xsl:value-of select="FlowTemplateInstance/@name"/> 
                        <xsl:text> </xsl:text>
                        <i>(<xsl:value-of select="FlowTemplateInstance/@schema"/>)</i>
                        </font>
                    </td>
                    <td width="20"></td>
                    <td><font class="fieldlabel"><xsl:value-of select="Description"/></font></td>
                </tr>
                <tr>
                    <td colspan="7"><font class="fieldlabel">
                        <xsl:call-template name="show-flow"/>
                    </font></td>
                </tr>
                <tr>
                    <td colspan="7"><hr/></td>
                </tr>
            </xsl:for-each>
    
        </table>
    </xsl:template>

    <!--===========================================================================================================
        Flusso di esecuzione di una operazione GreenVulcano.
    -->
    
    <xsl:template name="show-flow">
        <xsl:variable name="instance" select="FlowTemplateInstance"/>
        <xsl:variable name="flow" select="/GreenVulcano/FlowTemplates/FlowSchema[@name=$instance/@schema]/FlowTemplate[@name=$instance/@name]"/>
        
        <!--xsl:variable name="servizio" select="ancestor::Service/@id-service"/-->
        <xsl:variable name="sistema" select="ancestor::Client/@id-system"/>
        <xsl:variable name="operation" select="concat(self::Operation[@forward-name]/@forward-name,self::Operation[not(@forward-name)]/@name)"/>
        
        
        <br/>
        La seguente tabella mostra il flusso della primitiva <b><xsl:value-of select="@name"/></b>.<br/>
        Il flusso della primitiva inizia dal nodo <b><xsl:value-of select="$flow/@first-node"/></b>.<br/>
        <br/>
        
        <table border="0" cellspacing="0" cellpadding="5" width="100%">
            <tr valign="bottom" bgcolor="#99FF66">
                <td width="15%"><font class="fieldlabel"><b>Node</b><br/><small>(Type)</small></font></td>
                <td width="15%"><font class="fieldlabel"><b>System</b><br/><small>(Operation)</small></font></td>
                <td width="15%"><font class="fieldlabel"><b>Input</b></font></td>
                <td width="15%"><font class="fieldlabel"><b>Output</b></font></td>
                <td width="15%"><font class="fieldlabel"><b>Routing</b></font></td>
                <td width="15%"><font class="fieldlabel"><b>If success</b></font></td>
                <td width="15%"><font class="fieldlabel"><b>If error</b></font></td>
            </tr>
            <xsl:for-each select="$flow/*[@type='flow-node']">
                <xsl:sort select="@id"/>
                <xsl:variable name="assignment" select="$instance/OperationAssignment[@id=current()/@id]"/>
                <tr valign="top">
                    <td><font class="fieldlabel">
                        <b><xsl:value-of select="@id"/></b><br/>
                        <small>(<xsl:value-of select="@op-type"/>)</small>
                        </font>
                    </td>
                    <td><font class="fieldlabel">
                        <xsl:if test="$assignment">
                            <b><a class="link" href="?cmd=system&amp;system={$assignment/@id-system}"><xsl:value-of select="$assignment/@id-system"/></a></b><br/>
                            <small>(<xsl:value-of select="$assignment/@operation-name"/>)</small>
                        </xsl:if>
                        </font>
                    </td>
                    <td><font class="fieldlabel"><xsl:value-of select="@input"/></font></td>
                    <td><font class="fieldlabel"><xsl:value-of select="@output"/></font></td>
                    <td><font class="fieldlabel">
                        <xsl:for-each select="Routing">
                            <nobr>
                                if <b><xsl:value-of select="@condition"/></b>
                                <xsl:text> </xsl:text>
                                then <b><xsl:value-of select="@next-node-id"/></b>
                            </nobr>
                            <br/>
                        </xsl:for-each>
                        </font>
                    </td>
                    <td><font class="fieldlabel"><xsl:value-of select="@next-node-id | @default-id"/></font></td>
                    <td><font class="fieldlabel"><xsl:value-of select="@on-exception-id"/></font></td>
                </tr>
            </xsl:for-each>
        </table>
        <!-- insert GreenVulcano WorkFlow -->
              <br/>
        		<!--img src="{gvcon:generateHttpImage($serverpath, $sistema, $service, $operation)}"/-->		
    </xsl:template>

</xsl:stylesheet>
