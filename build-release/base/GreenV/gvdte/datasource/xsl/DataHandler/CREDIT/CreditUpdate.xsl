<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl fs java" version="2.0"
                xmlns:fs="http://www.w3.org/2005/xpath-functions"
                xmlns:java="http://xml.apache.org/xalan/java"
                xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xsl:output encoding="utf-8" indent="yes" method="xml"/>
    <xsl:template match="/">
        <xsl:element name="RowSet">
            <xsl:apply-templates select="/PersonsData/PersonData"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="/PersonsData/PersonData">
        <xsl:element name="data">
            <xsl:apply-templates select="CardsData/CardData"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="CardsData/CardData">
        <xsl:element name="row">
            <xsl:element name="col">
                <xsl:value-of select="Number"/>
            </xsl:element>
            <xsl:variable name="var_0" select="/PersonsData/PersonData/Name"/>
            <xsl:variable name="var_1" select="/PersonsData/PersonData/City"/>
            <xsl:element name="col">
                <xsl:value-of select="java:it.greenvulcano.gvesb.datahandling.utils.GenericRetriever.getData(&apos;getPersonID&apos;,concat($var_0,&apos;,&apos;,$var_1))"/>
            </xsl:element>
            <xsl:element name="col">
                <xsl:attribute name="type">
                    <xsl:value-of select="string(&apos;float&apos;)"/>
                </xsl:attribute>
                <xsl:value-of select="Credit"/>
            </xsl:element>
            <xsl:element name="col">
                <xsl:value-of select="Active"/>
            </xsl:element>
            <xsl:element name="col-update">
                <xsl:attribute name="type">
                    <xsl:value-of select="string(&apos;float&apos;)"/>
                </xsl:attribute>
                <xsl:value-of select="Credit"/>
            </xsl:element>
            <xsl:element name="col-update">
                <xsl:value-of select="Active"/>
            </xsl:element>
            <xsl:element name="col-update">
                <xsl:value-of select="Number"/>
            </xsl:element>
            <xsl:variable name="var_2" select="/PersonsData/PersonData/Name"/>
            <xsl:variable name="var_3" select="/PersonsData/PersonData/City"/>
            <xsl:element name="col-update">
                <xsl:value-of select="java:it.greenvulcano.gvesb.datahandling.utils.GenericRetriever.getData(&apos;getPersonID&apos;,concat($var_2,&apos;,&apos;,$var_3))"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>