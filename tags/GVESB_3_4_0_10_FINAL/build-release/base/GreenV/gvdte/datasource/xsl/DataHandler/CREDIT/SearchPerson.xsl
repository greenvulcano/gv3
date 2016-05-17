<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl fs java" version="2.0"
                xmlns:fs="http://www.w3.org/2005/xpath-functions"
                xmlns:java="http://xml.apache.org/xalan/java"
                xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xsl:output encoding="utf-8" indent="yes" method="xml"/>
    <xsl:template match="/">
        <xsl:element name="PersonsData">
            <xsl:apply-templates select="/RowSet/data"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="/RowSet/data">
        <xsl:element name="PersonData">
            <xsl:element name="Name">
                <xsl:value-of select="@key_1"/>
            </xsl:element>
            <xsl:variable name="var_0" select="@key_2"/>
            <xsl:element name="BirthDate">
                <xsl:value-of select="java:it.greenvulcano.util.xml.XSLTUtils.convertDate($var_0,string(&apos;yyyyMMdd HH:mm:ss&apos;),string(&apos;dd/MM/yyyy&apos;))"/>
            </xsl:element>
            <xsl:element name="City">
                <xsl:value-of select="@key_3"/>
            </xsl:element>
            <xsl:element name="CardsData">
                <xsl:apply-templates select="row"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>
    <xsl:template match="row">
        <xsl:element name="CardData">
            <xsl:element name="Number">
                <xsl:value-of select="col[1]"/>
            </xsl:element>
            <xsl:element name="Credit">
                <xsl:value-of select="col[2]"/>
            </xsl:element>
            <xsl:element name="Active">
                <xsl:value-of select="col[3]"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>