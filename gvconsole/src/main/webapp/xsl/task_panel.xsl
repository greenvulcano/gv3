<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
>
<xsl:output method="html" /> 

<xsl:template match="/PROJECT/CONF">

	<div id="taskPalette" class="TaskLeftMenu">

	<xsl:for-each select="Task">

		<div class="TaskLeftDrag" align="center" onmousedown="Task_beginDrag();">

			<xsl:for-each select="attribute::*">

				<xsl:attribute name="{local-name()}">
					<xsl:value-of select="."/>
				</xsl:attribute>
		
			</xsl:for-each>

		
			<IMG>
				<xsl:attribute name="src">
					<xsl:value-of select="@image"/>
				</xsl:attribute>
				<xsl:attribute name="alt">
					<xsl:value-of select="@title"/>
				</xsl:attribute>
			
			</IMG>

			<br/>

			<xsl:value-of select="@type"/>
		</div>

	</xsl:for-each>

	</div>

</xsl:template>

<xsl:template match="/PROJECT/FLOW"/>

</xsl:stylesheet>
