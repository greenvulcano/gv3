<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<!--===========================================================================================================
        Inizio.
    -->
	<xsl:template match="/">
		<graph>
			<node id="EAIBUS">
				<attr name="Label">
					<string>GreenVulcano-system</string>
				</attr>
			</node>
			<xsl:for-each select="/GVSystems/Systems/System">
				<xsl:variable name="id-system" select="@id-system"/>
				<!--xsl:variable name="technologies-out" select="@technologies-out"/>
				<xsl:variable name="technologies-in" select="@technologies-in"/-->
				<xsl:if test="not(@id-system='GVESB')">
					<node id="{$id-system}">
						<attr name="Label">
							<xsl:call-template name="manage-label"/>
						</attr>
					</node>
					<xsl:call-template name="manage-edge"/>
				</xsl:if>
			</xsl:for-each>
		</graph>
	</xsl:template>
	<!--===========================================================================================================
       verifico per ogni sistema se esso è un sistema di front-end, un sistema di back-end, oppure entrambi.
    -->
	<xsl:template name="manage-edge">
		<xsl:variable name="id-system" select="@id-system"/>
		<xsl:variable name="SystemNode" select="/GVSystems/Systems/System[@id-system=$id-system]"/>
		<!--xsl:variable name="technologies-out" select="@technologies-out"/>
		<xsl:variable name="technologies-in" select="@technologies-in"/-->
		<xsl:for-each select="/GVServices/Services/Service">
			<xsl:for-each select="Clients/Client">
				<xsl:variable name="clientIdSystem" select="@id-system"/>
				<xsl:if test="(@id-system=$id-system)">
					<edge id="{$id-system}" from="{$id-system}" to="GVESB">
						<attr name="Label">
							<xsl:choose>
								<xsl:when test="(($SystemNode/@technologies-in) and ($SystemNode/@technologies-out))">
								<xsl:variable name="tec-in" select="$SystemNode/@technologies-in"/>
								<xsl:variable name="tec-out" select="$SystemNode/@technologies-out"/>
									<string><xsl:value-of select="concat('I:',$tec-in,'O:',$tec-out)"/>
									</string>
								</xsl:when>
								<xsl:when test="($SystemNode/@technologies-in)">
								      <xsl:variable name="tec-in" select="$SystemNode/@technologies-in"/>
									<string><xsl:value-of select="concat('I:',$tec-in)"/>
									</string>
								</xsl:when>
								<xsl:when test="($SystemNode/@technologies-out)">
								       <xsl:variable name="tec-out" select="$SystemNode/@technologies-out"/>
									<string><xsl:value-of select="concat('O:',$tec-out)"/>
									</string>
								</xsl:when>
							</xsl:choose>
						</attr>
					</edge>
				</xsl:if>
				<xsl:if test="(Operation/Participant[@id-system=$id-system])">
					<edge id="{$id-system}" from="GVESB" to="{$id-system}">
						<attr name="Label">
							<!--xsl:if test="not($clientIdSystem=$id-system)"-->
							<xsl:choose>
								<xsl:when test="(($SystemNode/@technologies-in) and ($SystemNode/@technologies-out))">
								<xsl:variable name="tec-in" select="$SystemNode/@technologies-in"/>
								<xsl:variable name="tec-out" select="$SystemNode/@technologies-out"/>
									<string><xsl:value-of select="concat('I:',$tec-in,'O:',$tec-out)"/>
									</string>
								</xsl:when>
								<xsl:when test="($SystemNode/@technologies-in)">
								      <xsl:variable name="tec-in" select="$SystemNode/@technologies-in"/>
									<string><xsl:value-of select="concat('I:',$tec-in)"/>
									</string>
								</xsl:when>
								<xsl:when test="($SystemNode/@technologies-out)">
								       <xsl:variable name="tec-out" select="$SystemNode/@technologies-out"/>
									<string><xsl:value-of select="concat('O:',$tec-out)"/>
									</string>
								</xsl:when>
							</xsl:choose>
							<!--/xsl:if-->
						</attr>
					</edge>
				</xsl:if>
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>
	<!--===========================================================================================================
       verifico per ogni sistema se esso è un sistema di front-end, un sistema di back-end, oppure entrambi per assegnare opportunamente un colore.
    -->
	<xsl:template name="manage-label">
		<xsl:variable name="id-system" select="@id-system"/>
		<xsl:for-each select="/GVServices/Services/Service">
			<xsl:for-each select="Clients/Client">
				<xsl:choose>
					<xsl:when test="(Operation/Participant[@id-system=$id-system])">
						<xsl:for-each select="/GVServices/Services/Service">
							<xsl:for-each select="Clients/Client">
								<xsl:if test="(@id-system=$id-system)">
									<string>in-out-system</string>
								</xsl:if>
							</xsl:for-each>
						</xsl:for-each>
						<string>outbound-system</string>
					</xsl:when>
					<xsl:when test="(@id-system=$id-system)">
						<xsl:for-each select="/GVServices/Services/Service">
							<xsl:for-each select="Clients/Client">
								<xsl:if test="(Operation/Participant[@id-system=$id-system])">
									<string>in-out-system</string>
								</xsl:if>
							</xsl:for-each>
						</xsl:for-each>
						<string>inbound-system</string>
					</xsl:when>
				</xsl:choose>
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
