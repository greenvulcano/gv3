<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet exclude-result-prefixes="java" version="1.0"
                xmlns:java="http://xml.apache.org/xalan/java"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="text"/>

    <xsl:template match="/RowSet">
        <xsl:apply-templates select="data[@id='1']"/>
    </xsl:template>

    <xsl:template match="data">
        {"message" : "OK",
         "savepoint" : [
             <xsl:apply-templates select="row" mode="savepoint"/>
         ]
        }
    </xsl:template>

    <xsl:template match="row" mode="savepoint">
        <xsl:if test="position() != 1">,</xsl:if>
        {"rec_id":"<xsl:value-of select="col[1]"/>",
         "id":"<xsl:value-of select="col[2]"/>",
         "server":"<xsl:value-of select="col[3]"/>",
         "system":"<xsl:value-of select="col[4]"/>",
         "service":"<xsl:value-of select="col[5]"/>",
         "operation":"<xsl:value-of select="col[6]"/>",
         "recNode":"<xsl:value-of select="col[7]"/>",
         "creation":"<xsl:value-of select="java:it.greenvulcano.util.xml.XSLTUtils.convertDate(col[8], 'yyyyMMdd HH:mm:ss', 'dd/MM/yyyy HH:mm:ss')"/>",
         "lastUpdate":"<xsl:value-of select="java:it.greenvulcano.util.xml.XSLTUtils.convertDate(col[9], 'yyyyMMdd HH:mm:ss', 'dd/MM/yyyy HH:mm:ss')"/>",
         "state":"<xsl:value-of select="col[10]"/>",
         "detail" : [
             <xsl:apply-templates select="/RowSet/data[@id='2' and @key_1=current()/col[1]]/row" mode="detail"/>
         ]
        }
    </xsl:template>

    <xsl:template match="row" mode="detail">
        <xsl:if test="position() != 1">,</xsl:if>
        {"name":"<xsl:value-of select="col[1]"/>",
         "value":"<xsl:value-of select="col[2]"/>"
        }
    </xsl:template>
</xsl:stylesheet>