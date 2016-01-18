<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="java hl7" version="1.0"
                xmlns:java="http://xml.apache.org/xalan/java"
                xmlns:hl7="urn:hl7-org:v2xml"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml"/>

<xsl:param name="HL7_REC_APPLICATION"/>
<xsl:param name="HL7_REC_FACILITY"/>
<xsl:param name="ERROR">Generic Error</xsl:param>

<xsl:template match="/">
<ACK xmlns="urn:hl7-org:v2xml">
    <MSH>
        <MSH.1>|</MSH.1>
        <MSH.2>^~\&amp;</MSH.2>
        <MSH.3>
            <xsl:choose>
                <xsl:when test="$HL7_REC_APPLICATION != ''">
                    <xsl:value-of select="$HL7_REC_APPLICATION"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="//hl7:MSH/hl7:MSH.5"/>
                </xsl:otherwise>
            </xsl:choose>
        </MSH.3>
        <MSH.4>
            <xsl:choose>
                <xsl:when test="$HL7_REC_FACILITY != ''">
                    <xsl:value-of select="$HL7_REC_FACILITY"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="//hl7:MSH/hl7:MSH.6"/>
                </xsl:otherwise>
            </xsl:choose>
        </MSH.4>
        <MSH.5><xsl:value-of select="//hl7:MSH/hl7:MSH.3"/></MSH.5>
        <MSH.6><xsl:value-of select="//hl7:MSH/hl7:MSH.4"/></MSH.6>
        <MSH.7>
            <TS.1><xsl:value-of select="java:it.greenvulcano.util.xml.XSLTUtils.nowToString('yyyyMMddHHmmss.SSS')"/></TS.1>
        </MSH.7>
        <MSH.9>
            <CM_MSG.1>ACK</CM_MSG.1>
        </MSH.9>
        <MSH.10><xsl:value-of select="java:it.greenvulcano.util.xml.XSLTUtils.nowToString('SYSTEM_TIME')"/></MSH.10>
        <MSH.11>P</MSH.11>
        <MSH.12>2.2</MSH.12>
    </MSH>
    <MSA>
        <MSA.1>AR</MSA.1>
        <MSA.2><xsl:value-of select="//hl7:MSH/hl7:MSH.10"/></MSA.2>
        <MSA.3><xsl:value-of select="$ERROR"/></MSA.3>
    </MSA>
    <ERR>
        <ERR.1>
            <ELD.4>
                <CE.1>000</CE.1>
                <CE.2>Application Internal Error</CE.2>
                <CE.3>AAAAAAAA</CE.3>
            </ELD.4>
        </ERR.1>
    </ERR>
</ACK>
</xsl:template>

</xsl:stylesheet>