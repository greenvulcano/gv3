<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:htm="http://www.w3.org/1999/xhtml" xmlns:ns0="http://www.greenvulcano.com/database">

  <xsl:template match="/ns0:RowSet">
    <xsl:element name="RowSet">
      <xsl:element name="data">
        <xsl:apply-templates select="ns0:data/ns0:row"/>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns0:data/ns0:row">
	<xsl:element name="row">
	   <xsl:element name="ID"><xsl:value-of select="ns0:ID"/></xsl:element>
       <xsl:element name="FIELD1"><xsl:value-of select="ns0:FIELD1"/></xsl:element>
       <xsl:element name="FIELD2"><xsl:value-of select="ns0:FIELD2"/></xsl:element>
       <xsl:element name="FIELD3"><xsl:value-of select="ns0:FIELD3"/></xsl:element>
	</xsl:element>
  </xsl:template>
</xsl:stylesheet>
