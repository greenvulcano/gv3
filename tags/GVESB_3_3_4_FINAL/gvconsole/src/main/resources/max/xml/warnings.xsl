<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="html"/>

    <xsl:variable name="invoke"><xsl:value-of select="/warnings/@invoke"/></xsl:variable>
    <xsl:variable name="context"><xsl:value-of select="/warnings/@context"/></xsl:variable>


    <xsl:template match="warnings">
        <br/><br/>
        <a name="interface"/>
        <table width="100%" cellspacing="4" cellpadding="4" class="ui-widget-header ui-corner-all">
            <tr>
                <td>
                    <table width="100%" cellspacing="0" cellpadding="4" border="0">
                        <xsl:apply-templates/>
                    </table>
                </td>
            </tr>
        </table>

        <br/>
        <br/>

    </xsl:template>

    <xsl:template match="warning">
        <xsl:if test="position() mod 2 = 0">
            <xsl:call-template name="out-warning">
                <xsl:with-param name="color">#DDDDDD</xsl:with-param>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="position() mod 2 = 1">
            <xsl:call-template name="out-warning">
                <xsl:with-param name="color">#BBBBBB</xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="out-warning">
        <xsl:param name="color"/>
        <tr bgcolor="{$color}">
            <td>
                <a href="{$invoke}&amp;opkey={@key}#anchor" title="go to the element containing the error">
                    <img src="{$context}/images/goto.png" border="0"/>
                </a>
                <b><xsl:text> </xsl:text><xsl:value-of select="@text"/></b>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
