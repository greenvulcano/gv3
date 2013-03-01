<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="java hl7" version="1.0"
                xmlns:java="http://xml.apache.org/xalan/java"
                xmlns:hl7="urn:hl7-org:v2xml"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml"/>

<xsl:template match="/">
<ACK xmlns="urn:hl7-org:v2xml">
    <MSH>
        <MSH.1>|</MSH.1>
        <MSH.2>^~\&amp;</MSH.2>
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
        <MSA.1>AA</MSA.1>
        <MSA.2><xsl:value-of select="//hl7:MSH/hl7:MSH.10"/></MSA.2>
        <!--<MSA.2>12345</MSA.2>-->
    </MSA>
</ACK>
</xsl:template>

</xsl:stylesheet>