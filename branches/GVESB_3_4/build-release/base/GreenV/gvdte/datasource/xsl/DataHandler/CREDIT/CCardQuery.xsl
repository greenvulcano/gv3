<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl int fs" version="2.0"
                xmlns:fs="http://www.w3.org/2005/xpath-functions"
                xmlns:int="http://www.credit.com/services"
                xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xsl:output encoding="utf-8" indent="yes" method="xml"/>
    <xsl:template match="/">
        <xsl:apply-templates select="/RowSet/data/row"/>
    </xsl:template>
    <xsl:template match="/RowSet/data/row">
        <xsl:element name="QueryResponse" namespace="http://www.credit.com/services">
            <xsl:element name="name" namespace="http://www.credit.com/services">
                <xsl:value-of select="col[1]"/>
            </xsl:element>
            <xsl:element name="cnumber" namespace="http://www.credit.com/services">
                <xsl:value-of select="col[2]"/>
            </xsl:element>
            <xsl:element name="credit" namespace="http://www.credit.com/services">
                <xsl:value-of select="col[3]"/>
            </xsl:element>
            <xsl:choose>
                <xsl:when test="col[4] = &apos;Y&apos;">
                    <xsl:element name="active" namespace="http://www.credit.com/services">
                        <xsl:value-of select="&apos;true&apos;"/>
                    </xsl:element>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:element name="active" namespace="http://www.credit.com/services">
                        <xsl:value-of select="&apos;false&apos;"/>
                    </xsl:element>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>