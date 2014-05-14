<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="java" version="1.0"
                xmlns:java="http://xml.apache.org/xalan/java"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml"/>

<xsl:param name="FILE_NAME" select="'NONAME.BOH'"/>

<xsl:template match="/">
  <xsl:element name="RowSet">
    <xsl:element name="data">
      <xsl:element name="row">
          <xsl:attribute name="id">0</xsl:attribute>
          <xsl:element name="col">
              <xsl:value-of select="$FILE_NAME"/>
          </xsl:element>
          <xsl:element name="col">
              <xsl:attribute name="type">long-string</xsl:attribute>
              <xsl:value-of select="java:it.greenvulcano.util.xml.XMLUtils.serializeDOM_S(.)"/>
          </xsl:element>
      </xsl:element>
    </xsl:element>
  </xsl:element>
</xsl:template>

</xsl:stylesheet>