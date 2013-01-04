<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:greenvulcano="it.greenvulcano.gvesb.gvcon.browse.Stylesheet">
	<xsl:param name="channel"/>
	<xsl:param name="system"/>
	<!--===========================================================================================================
        Inizio.
    -->
	<xsl:template match="/GreenVulcano">
		<html>
			<head>
				<title>Configuration Browser - System <xsl:value-of select="$system"/>
				</title>
				<META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE"/>
				<link rel="stylesheet" href="browseConfiguration/stile.css" type="text/css"/>
			</head>
			<body>
				<xsl:call-template name="channel"/>
			</body>
		</html>
	</xsl:template>
	<!--===========================================================================================================
        Informazioni relative al canale.
    -->
	<xsl:template name="channel">
        <table cellpadding="4" cellspacing="0" width="100%">
			<tr bgcolor="#009900" valign="top">
				<td>
					<font class="titlelabel6">Channel</font>
				</td>
				<td width="20"/>
				<td>
					<font class="titlelabel6">Description</font>
				</td>
			</tr>
			<xsl:for-each select="Systems/System">
				<xsl:if test="@id-system=$system">
					<xsl:for-each select="Channel">
						<xsl:if test="@id-channel=$channel">
							<tr valign="top">
								<td>
									<font class="fieldlabel">
										<nobr>
											<b>
												<xsl:value-of select="@id-channel"/>
											</b>
										</nobr>
									</font>
								</td>
								<td width="20"/>
								<td>
									<font class="fieldlabel">
										<xsl:value-of select="Description"/>
										<p/>
										<xsl:if test="*[@type='call' or @type='dequeue' or @type='enqueue']">
											<xsl:call-template name="operation-list"/>
										</xsl:if>
										<xsl:if test="not(*[@type='call' or @type='dequeue' or @type='enqueue'])">
                            Nessuna interfaccia di comunicazione definita nel canale di comunicazione <b>
												<xsl:value-of select="@id-channel"/>
											</b>.
                        </xsl:if>
										<p/>
										<xsl:if test="/GreenVulcano/Services/Service/Clients/Client[Operation/Participant/@id-system=$system and Operation/Participant/@id-channel=current()/@id-channel]">
											<xsl:call-template name="channel-service-list"/>
										</xsl:if>
										<xsl:if test="not(/GreenVulcano/Services/Service/Clients/Client[Operation/Participant/@id-system=$system and Operation/Participant/@id-channel=current()/@id-channel])">
                            Nessuna servizio configurato usa il canale di comunicazione <b>
												<xsl:value-of select="@id-channel"/>
											</b>.
                        </xsl:if>
									</font>
								</td>
							</tr>
							<tr>
								<td colspan="3">
									<hr/>
								</td>
							</tr>
							
						</xsl:if>
					</xsl:for-each>
				</xsl:if>
			</xsl:for-each>
		</table>
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
        Lista delle operazioni di comunicazione.
    -->
	<xsl:template name="operation-list">
        Di seguito l'elenco delle interfacce di comunicazione configurate nel canale <b>
			<xsl:value-of select="@id-channel"/>
		</b>:
        <br/>
		<br/>
		<table cellpadding="4" cellspacing="0" width="100%">
			<tr bgcolor="#009900" valign="top">
				<td>
					<font class="titlelabel7">Operation</font>
				</td>
				<td width="20"/>
				<td>
					<font class="titlelabel7">Type</font>
				</td>
				<td width="20"/>
				<td>
					<font class="titlelabel7">Description</font>
				</td>
			</tr>
			<xsl:call-template name="sub-operation-list"/>
		</table>
	</xsl:template>
	<xsl:template name="sub-operation-list">
		<xsl:for-each select="*[@type='call' or @type='dequeue' or @type='enqueue']">
			<tr valign="top">
				<td>
					<font class="fieldlabel">
						<nobr>
							<b>
								<xsl:value-of select="@name"/>
							</b>
						</nobr>
					</font>
				</td>
				<td width="20"/>
				<td>
					<font class="fieldlabel">
						<nobr>
							<xsl:value-of select="name()"/>
						</nobr>
						<br/>(<xsl:value-of select="@type"/>)</font>
				</td>
				<td width="20"/>
				<td>
					<font class="fieldlabel">
						<xsl:value-of select="Description"/>
						<p/>
						<xsl:apply-templates select="."/>
					</font>
				</td>
			</tr>
			<tr>
				<td colspan="5">
					<hr/>
				</td>
			</tr>
			<xsl:call-template name="sub-operation-list"/>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
