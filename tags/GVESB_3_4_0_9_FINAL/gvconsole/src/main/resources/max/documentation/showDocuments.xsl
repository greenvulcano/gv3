<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:param name="generate"/>

    <xsl:output omit-xml-declaration="yes"/>
    <xsl:output method="html"/>
    
	<xsl:template match="documents">
	    <xsl:if test="*/cover">
		    <xsl:call-template name="handle" />
      	</xsl:if>
	</xsl:template>
	
	<xsl:template name="handle">
		<table width="100%">
			<tr valign="top">
				<td><b>ID</b></td>
				<td><b>Title</b></td>
				<td><b>Version</b></td>
				<td><b>Author</b></td>
				<td><b>Owner</b></td>
				<td><b>Date</b></td>
				<td><b>Description</b></td>
			</tr>
			<tr>
				<td colspan="7"><hr/></td>
			</tr>
			<xsl:apply-templates select="*/cover">
				<xsl:sort select="../@id"/>
			</xsl:apply-templates>
		</table>
	</xsl:template>
	
	<xsl:template match="cover">
		<tr valign="top" bgcolor="#99FF33">
			<td><a href="{$generate}/{@title}-{@version}.pdf?id={../@id}"><xsl:value-of select="../@id"/></a><xsl:text> </xsl:text></td>
			<td><xsl:value-of select="@title"/><xsl:text> </xsl:text></td>
			<td><xsl:value-of select="@version"/><xsl:text> </xsl:text></td>
			<td><xsl:value-of select="@author"/><xsl:text> </xsl:text></td>
			<td><xsl:value-of select="@owner"/><xsl:text> </xsl:text></td>
			<td><xsl:value-of select="@date"/><xsl:text> </xsl:text></td>
			<td><xsl:value-of select="description"/><xsl:text> </xsl:text></td>
		</tr>
	</xsl:template>

</xsl:stylesheet>