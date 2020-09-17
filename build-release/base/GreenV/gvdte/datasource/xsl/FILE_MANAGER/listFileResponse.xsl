<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:param name="GVFM_FOUND_FILES_LIST"/>

    <xsl:template match="/">
        <xsl:element name="listFileResponse" namespace="http://www.greenvulcano.it/greenvulcano">
            <xsl:call-template name="split">
                <xsl:with-param name="string" select="$GVFM_FOUND_FILES_LIST" />
            </xsl:call-template>
        </xsl:element>
    </xsl:template>

    <xsl:template mode="split" name="split">
        <xsl:param name="string" />

        <xsl:choose>
            <!-- if the string contains a semicolon... -->
            <xsl:when test="contains($string, ';')">
                <!-- give the part before the semicolon... -->
                <xsl:call-template name="makeElement">
                    <xsl:with-param name="value" select="substring-before($string, ';')" />
                </xsl:call-template>
                <!-- and then call the template recursively on the rest of the string -->
                <xsl:call-template name="split">
                    <xsl:with-param name="string" select="substring-after($string, ';')" />
                </xsl:call-template>
            </xsl:when>
            <!-- if the string doesn't contain a semicolon and is not empty, just give its value -->
            <xsl:otherwise>
                <xsl:if test="$string != ''">
                    <xsl:call-template name="makeElement">
                        <xsl:with-param name="value" select="$string" />
                    </xsl:call-template>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="makeElement">
        <xsl:param name="value"/>

        <xsl:element name="fileName" namespace="http://www.greenvulcano.it/greenvulcano">
            <xsl:value-of select="normalize-space($value)"/>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>