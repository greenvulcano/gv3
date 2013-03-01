<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:template match="/graph">
		<graph>
			<xsl:call-template name="graph"/>
		</graph>
	</xsl:template>
	<xsl:template name="graph">
		<xsl:for-each select="node">
			<xsl:variable name="nameNode" select="attr/string"/>
			<xsl:if test="not(preceding-sibling::node/@id=@id)">
				<node id="{@id}">
					<attr name="Label">
						<string>
							<xsl:value-of select="attr[@name='Label']/string"/>
						</string>
					</attr>
				</node>
			</xsl:if>
		</xsl:for-each>
		<xsl:for-each select="edge">
			<xsl:variable name="edgeFrom" select="@from"/>
			<xsl:variable name="edgeTo" select="@to"/>
			<!--
	            controllo se il nodo corrente ha più di un puntamento ad uno stesso nodo target, per migliorare il layout applicato successivamente.
	        -->
			<xsl:if test="(not(preceding-sibling::edge/@from=@from and preceding-sibling::edge/@to=@to) and (not(@from=@to)))">
				<edge id="{@id}" from="{@from}" to="{@to}">
					<attr name="Label">
						<string>
							<xsl:value-of select="attr/string"/>
						</string>
					</attr>
				</edge>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
