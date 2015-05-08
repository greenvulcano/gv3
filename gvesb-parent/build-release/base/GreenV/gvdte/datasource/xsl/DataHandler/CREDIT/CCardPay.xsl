<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl int fs" version="2.0"
                xmlns:fs="http://www.w3.org/2005/xpath-functions"
                xmlns:int="http://www.credit.com/services"
                xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xsl:output encoding="utf-8" indent="yes" method="xml"/>
    <xsl:template match="/">
        <xsl:element name="RowSet">
            <xsl:apply-templates select="/int:Pay"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="/int:Pay">
        <xsl:element name="data">
            <xsl:element name="row">
                <xsl:element name="col">
                    <xsl:attribute name="type">
                        <xsl:value-of select="string(&apos;numeric&apos;)"/>
                    </xsl:attribute>
                    <xsl:value-of select="int:amount"/>
                </xsl:element>
                <xsl:element name="col">
                    <xsl:value-of select="int:cnumber"/>
                </xsl:element>
                <xsl:element name="col">
                    <xsl:attribute name="type">
                        <xsl:value-of select="string(&apos;numeric&apos;)"/>
                    </xsl:attribute>
                    <xsl:value-of select="int:amount"/>
                </xsl:element>
                <xsl:element name="col-update"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>