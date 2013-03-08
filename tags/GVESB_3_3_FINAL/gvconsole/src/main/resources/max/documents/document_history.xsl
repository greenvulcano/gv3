<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:variable name="invoke">/gvconsole</xsl:variable>

    <xsl:template match="/">
        History of <b><xsl:value-of select="document-history/label"/></b><p/>
        <xsl:value-of select="document-history/description"/><p/>
        
        <table width="100%" cellspacing="0" cellpadding="4" border="0" class="ui-widget-header ui-corner-all">
            <tr valign="bottom">
                <td><b>Version</b></td>
                <td width="15"></td>
                <td><b>Date</b></td>
                <td width="15"></td>
                <td><b>Author</b></td>
                <td width="15"></td>
                <td><b>Notes</b></td>
                <td width="15"></td>
                <td><b>Commands</b></td>
            </tr>
            <xsl:apply-templates select="document-history/version" />
        </table>
    </xsl:template>

    <xsl:template match="version">
        <xsl:variable name="color">
            <xsl:if test="position() mod 2 = 0">#BBBBBB</xsl:if>
            <xsl:if test="position() mod 2 = 1"></xsl:if>
        </xsl:variable>

        <tr valign="top" bgcolor="{$color}">
            <xsl:variable name="id" select="@id"></xsl:variable>
            <xsl:variable name="name1" select="/document-history/name"></xsl:variable>
            <xsl:variable name="label1" select="/document-history/label"></xsl:variable>
            <xsl:variable name="notes1" select="notes"></xsl:variable>
            <xsl:variable name="date" select="date"></xsl:variable>
            <xsl:variable name="author" select="author"></xsl:variable>

            <td><xsl:value-of select="$id"/></td>
            <td width="15"></td>
            <td><xsl:value-of select="date"/></td>
            <td width="15"></td>
            <td><xsl:value-of select="author"/></td>
            <td width="15"></td>
            <td><small><xsl:value-of select="notes"/></small></td>
            <td width="15"></td>
            <td>
                <small>
                    <nobr>[<a href="{$invoke}/documents/viewDocumentVersion?name={$name1}&amp;version={$id}" title="View Version" target="_blank" style="color: #191970">view</a>]</nobr>
                    <xsl:if test="permission = 'RW'" >
                        <nobr>[<a href="{$invoke}/def/documents/rollback.jsp?name={$name1}&amp;label={$label1}&amp;version={$id}&amp;notes={$notes1}&amp;author={$author}&amp;date={$date}" title="Restore Version" style="color: #191970">restore</a>]</nobr>
                    </xsl:if>
                </small>
            </td>
    	</tr>
    </xsl:template>

</xsl:stylesheet>