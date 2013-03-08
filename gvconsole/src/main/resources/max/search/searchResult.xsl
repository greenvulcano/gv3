<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:output encoding="US-ASCII"/>

    <xsl:variable name="invoke1">..</xsl:variable>

    <xsl:template match="/">

        <hr/>
        
        <xsl:if test="search-result/element">
            <b>Found <xsl:value-of select="search-result/@matches"/> match(es) in <xsl:value-of select="search-result/@nodes"/> element(s)</b>
            <br/>
            <br/>
            <table width="100%" cellspacing="0" cellpadding="1" class="ui-widget-header ui-corner-all">
                <tr>
                    <td width="5">
                    </td>
                    <td width="100%">
                        <b><font>Found elements</font></b>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <table width="100%" cellspacing="0" cellpadding="6" border="0">
        
                            <xsl:apply-templates select="search-result/element"/>
                        </table>
                    </td>
                </tr>
            </table>
        </xsl:if>
        
        <xsl:if test="not(search-result/element)">
            <b>No matches found</b>
        </xsl:if>
        
    </xsl:template>


    <xsl:template match="element">
        <tr bgcolor="#009900">
            <td colspan="5" style="padding-top:0px; padding-bottom:0px"></td>
        </tr>
        <xsl:variable name="color">
            <xsl:if test="position() mod 2 = 0">#f7f7f7</xsl:if>
            <xsl:if test="position() mod 2 = 1">#dddddd</xsl:if>
        </xsl:variable>
        <tr valign="top" bgcolor="{$color}">
            <td>
                <b><nobr><a href="{$invoke1}/def/search/selectNode.jsp?nodeId={@index}"><xsl:value-of select="@node-name"/></a></nobr></b>
            </td>
            <td width="5"></td>
        	<td><nobr><xsl:value-of select="count(match)"/> match(es)</nobr></td>
            <td width="5"></td>
            <td>
                <xsl:for-each select="match">
                    <li><xsl:value-of select="."/><xsl:text> </xsl:text></li>
                </xsl:for-each>
            </td>
    	</tr>
    </xsl:template>

</xsl:stylesheet>
