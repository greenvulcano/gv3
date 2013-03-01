<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl int fs ext" version="2.0"
                xmlns:ext="http://www.external-credit.com/services"
                xmlns:fs="http://www.w3.org/2005/xpath-functions"
                xmlns:int="http://www.credit.com/services"
                xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xsl:output encoding="utf-8" indent="yes" method="xml"/>
    <xsl:template match="/">
        <xsl:element name="Pay" namespace="http://www.external-credit.com/services">
            <xsl:element name="cnumber"
                         namespace="http://www.external-credit.com/services">
                <xsl:value-of select="/int:Pay/int:cnumber"/>
            </xsl:element>
            <xsl:element name="amount" namespace="http://www.external-credit.com/services">
                <xsl:value-of select="/int:Pay/int:amount"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>