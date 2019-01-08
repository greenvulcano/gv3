<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl int fs ext" version="2.0"
                xmlns:ext="http://www.external-credit.com/services"
                xmlns:fs="http://www.w3.org/2005/xpath-functions"
                xmlns:int="http://www.credit.com/services"
                xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xsl:output encoding="utf-8" indent="yes" method="xml"/>
    <xsl:template match="/">
        <xsl:element name="QueryResponse" namespace="http://www.credit.com/services">
            <xsl:element name="name" namespace="http://www.credit.com/services">
                <xsl:value-of select="string(&apos;EXTERNAL CARD&apos;)"/>
            </xsl:element>
            <xsl:element name="cnumber" namespace="http://www.credit.com/services">
                <xsl:value-of select="/ext:QueryResponse/ext:cnumber"/>
            </xsl:element>
            <xsl:element name="credit" namespace="http://www.credit.com/services">
                <xsl:value-of select="string(&apos;0&apos;)"/>
            </xsl:element>
            <xsl:element name="active" namespace="http://www.credit.com/services">
                <xsl:value-of select="/ext:QueryResponse/ext:active"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>