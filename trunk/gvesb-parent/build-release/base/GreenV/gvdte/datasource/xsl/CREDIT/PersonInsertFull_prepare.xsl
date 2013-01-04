<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:java="http://xml.apache.org/xalan/java"
                exclude-result-prefixes="java">

    <xsl:output method="xml"/>

    <xsl:template match="/PersonsData">
        <xsl:element name="PersonsData">
            <xsl:apply-templates select="PersonData"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="PersonData">
        <xsl:apply-templates select="CardsData/CardData">
            <xsl:with-param name="Name" select="Name"/>
            <xsl:with-param name="City" select="City"/>
            <xsl:with-param name="BirthDate" select="BirthDate"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="CardData">
        <xsl:param name="Name"/>
        <xsl:param name="City"/>
        <xsl:param name="BirthDate"/>

        <xsl:element name="PersonData">
            <xsl:element name="Name"><xsl:value-of select="$Name"/></xsl:element>
            <xsl:element name="City"><xsl:value-of select="$City"/></xsl:element>
            <xsl:element name="BirthDate"><xsl:value-of select="$BirthDate"/></xsl:element>
            <xsl:element name="CardsData">
                <xsl:copy-of select="." />
            </xsl:element>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>
