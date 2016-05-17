<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:param name="XMLNS"/>

    <xsl:output method="xml"/>

    <xsl:template match="/">
      <xsl:apply-templates mode="copy" select="."/>
    </xsl:template>

    <xsl:template mode="copy" match="*">
        <xsl:element name="{local-name()}" namespace="{$XMLNS}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates mode="copy"/>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>