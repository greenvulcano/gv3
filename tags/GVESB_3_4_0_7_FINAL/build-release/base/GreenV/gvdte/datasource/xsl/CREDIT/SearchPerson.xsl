<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:java="http://xml.apache.org/xalan/java"
                exclude-result-prefixes="java">

    <xsl:output method="xml"/>

    <xsl:template match="/RowSet">
        <xsl:element name="PersonsData">
            <xsl:apply-templates select="data"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="data">
        <xsl:element name="PersonData">
            <xsl:element name="Name">
                <xsl:value-of select="@key_1"/>
            </xsl:element>
            <xsl:element name="BirthDate">
                <xsl:value-of select="java:it.greenvulcano.util.xml.XSLTUtils.convertDate(@key_2, 'yyyyMMdd HH:mm:ss', 'dd/MM/yyyy')"/>
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
            <xsl:element name="Enabled">
                <xsl:value-of select="col[3]"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>
