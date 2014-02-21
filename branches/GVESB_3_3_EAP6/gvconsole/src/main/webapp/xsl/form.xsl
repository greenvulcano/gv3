<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
>
<xsl:output method="html" /> 

<xsl:template match="/PROJECT/CONF">

	<form id="frmMain" name="frmMain" method="post" target="_parent">
		<xsl:attribute name="action">
			<xsl:value-of select="@outputUrl"/>
		</xsl:attribute>
		<input type="hidden" id="xml_data" name="xml_data"/>
	</form>

</xsl:template>

<xsl:template match="/PROJECT/FLOW"/>

</xsl:stylesheet>
