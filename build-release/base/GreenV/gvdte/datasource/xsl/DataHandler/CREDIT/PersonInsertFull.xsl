<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:java="http://xml.apache.org/xalan/java"
                exclude-result-prefixes="java">

    <xsl:output method="xml"/>

    <xsl:template match="/PersonsData">
        <xsl:element name="RowSet">
            <xsl:element name="data">
                <xsl:apply-templates select="PersonData"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>


    <xsl:template match="PersonData">
        <xsl:variable name="PersonId" select="java:it.greenvulcano.gvesb.datahandling.utils.GenericRetriever.getData('getPersonID', concat(Name, ',', City))"/>
        <xsl:variable name="CityId" select="java:it.greenvulcano.gvesb.datahandling.utils.GenericRetriever.getData('getCityID', City)"/>

        <xsl:choose>
          <xsl:when test="string-length($PersonId) &gt; 0">
              <xsl:element name="row">
                  <xsl:attribute name="id">1</xsl:attribute>
                  <xsl:element name="col">
                      <xsl:attribute name="type">numeric</xsl:attribute>
                      <xsl:value-of select="$CityId"/>
                  </xsl:element>
                  <xsl:element name="col">
                      <xsl:attribute name="type">numeric</xsl:attribute>
                      <xsl:value-of select="$PersonId"/>
                  </xsl:element>
              </xsl:element>

              <xsl:apply-templates select="CardsData/CardData">
                  <xsl:with-param name="OwnerId" select="$PersonId"/>
              </xsl:apply-templates>
          </xsl:when>
          <xsl:otherwise>
              <xsl:variable name="PersonId" select="java:it.greenvulcano.gvesb.datahandling.utils.GenericRetriever.getData('getSeqVal', '')"/>

              <xsl:element name="row">
                  <xsl:attribute name="id">0</xsl:attribute>
                  <xsl:element name="col">
                      <xsl:attribute name="type">numeric</xsl:attribute>
                      <xsl:value-of select="$PersonId"/>
                  </xsl:element>
                  <xsl:element name="col">
                      <xsl:attribute name="type">numeric</xsl:attribute>
                      <xsl:value-of select="$CityId"/>
                  </xsl:element>
                  <xsl:element name="col">
                      <xsl:value-of select="Name"/>
                  </xsl:element>
                  <xsl:element name="col">
                      <xsl:attribute name="type">timestamp</xsl:attribute>
                      <xsl:attribute name="format">dd/MM/yyyy</xsl:attribute>
                      <xsl:value-of select="BirthDate"/>
                  </xsl:element>
              </xsl:element>

              <xsl:apply-templates select="CardsData/CardData">
                  <xsl:with-param name="OwnerId" select="$PersonId"/>
              </xsl:apply-templates>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>



    <xsl:template match="CardData">
        <xsl:param name="OwnerId"/>
        <xsl:variable name="CardId" select="java:it.greenvulcano.gvesb.datahandling.utils.GenericRetriever.getData('getCardID', concat(Number, ',', $OwnerId))"/>

        <xsl:choose>
          <xsl:when test="string-length($CardId) &gt; 0">
              <xsl:element name="row">
                  <xsl:attribute name="id">3</xsl:attribute>
                  <xsl:element name="col">
                      <xsl:attribute name="type">float</xsl:attribute>
                      <xsl:value-of select="Credit"/>
                  </xsl:element>
                  <xsl:element name="col">
                      <xsl:value-of select="Active"/>
                  </xsl:element>
                  <xsl:element name="col">
                      <xsl:attribute name="type">numeric</xsl:attribute>
                      <xsl:value-of select="$CardId"/>
                  </xsl:element>
              </xsl:element>
          </xsl:when>
          <xsl:otherwise>
              <xsl:element name="row">
                  <xsl:attribute name="id">2</xsl:attribute>
                  <xsl:element name="col">
                      <xsl:attribute name="type">numeric</xsl:attribute>
                      <xsl:value-of select="java:it.greenvulcano.gvesb.datahandling.utils.GenericRetriever.getData('getSeqVal', '')"/>
                  </xsl:element>
                  <xsl:element name="col">
                      <xsl:attribute name="type">numeric</xsl:attribute>
                      <xsl:value-of select="$OwnerId"/>
                  </xsl:element>
                  <xsl:element name="col">
                      <xsl:value-of select="Number"/>
                  </xsl:element>
                  <xsl:element name="col">
                      <xsl:attribute name="type">float</xsl:attribute>
                      <xsl:value-of select="Credit"/>
                  </xsl:element>
                  <xsl:element name="col">
                      <xsl:value-of select="Active"/>
                  </xsl:element>
              </xsl:element>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
