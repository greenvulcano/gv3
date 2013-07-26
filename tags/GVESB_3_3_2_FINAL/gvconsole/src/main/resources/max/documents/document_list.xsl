<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" encoding="US-ASCII"/>

    <xsl:variable name="invoke">/gvconsole/documents</xsl:variable>
    <xsl:variable name="invoke1">/gvconsole</xsl:variable>

    <xsl:template match="/">

        
		<br/>
        <xsl:if test="document-list/document">
              <TABLE  cellspacing="0" cellpadding="1" class="search">
                <TR>
                    <TD class="ui-widget-header ui-corner-all" style="padding:2px 20px">
                        <div style="float:left;">Configuration</div>
                        <div style="float:right;"><xsl:apply-templates select="document-list/groups"/></div>
                    </TD>
                </TR>
                <TR align="center">
                    <TD colspan="2">
                    	<div id="accordion">
		                	<xsl:apply-templates select="document-list/document">
		                    	<xsl:sort select="label"/>
		                    </xsl:apply-templates>
		                </div>
                     </TD>
                </TR>
            </TABLE>
        </xsl:if>
    </xsl:template>


    <xsl:template match="document">
   		<h3><a class="titleDoc" href="#"><xsl:value-of select="label"/></a> 
			<xsl:if test="@isInError">
         		<img src="{$invoke1}/images/warning.gif" border="0" alt="The document contains warnings"/>
         	</xsl:if>
        </h3>
        <div class="iconDoc">
        	<div class="viewDoc">
        		<div class="info-list"></div>
        		<a href="{$invoke}/viewDocument?name={@name}" target="_self" title="view"><img src="{$invoke1}/images/lente.png" alt="View" border="0"/></a>
        	</div>
        	<div class="editDoc">
        		<div class="info-list"></div>
				<xsl:choose>
					<xsl:when test="permission = 'RW'" >
						<a href="{$invoke}/selectDocument?name={@name}" title="edit"><img src="{$invoke1}/images/edit_list.png" alt="Edit" border="0"/></a>
					</xsl:when>
					<xsl:when test="permission = 'LCK'">
						<a href="#"><span><img src="{$invoke1}/images/locking.png" border="0" title="The document is locked by {permission/@user} at {permission/@host} [{permission/@address}]"/></span></a>
					</xsl:when>
				</xsl:choose>
			</div>
			<div class="historyDoc">
				<div class="info-list"></div>
				<xsl:if test="history = 'yes'">
	              		<a href="{$invoke1}/def/documents/document_history.jsp?name={@name}" title="history"><img src="{$invoke1}/images/history_view.png" alt="History" border="0"/></a>
	            </xsl:if>
            </div>
        </div> 
    </xsl:template>

    <xsl:template match="group">
        <option value="{@name}"><xsl:value-of select="@label"/></option>
    </xsl:template>

</xsl:stylesheet>
