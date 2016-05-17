<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
>
<xsl:template match="/graph">
      <graph>
		 <xsl:call-template name="graph"/>
	</graph> 
 </xsl:template>

<xsl:template name="graph">
	<xsl:for-each select="node">
	 	<node id="{@id}">
				<attr name="Label">
					<string><xsl:value-of select="attr[@name='Label']/string"/></string>
				</attr>
				<xsl:if test="attr[@name='IO']/string">
					<attr name="IO">
						<string><xsl:value-of select="attr[@name='IO']/string"/></string>
					</attr>
				</xsl:if>
			</node>
       </xsl:for-each>
               
      <xsl:for-each select="edge">
	       <xsl:variable name="edgeFrom" select="@from"/>
	       <xsl:variable name="edgeTo" select="@to"/>
	      <!--
	            controllo se il nodo corrente ha più di un puntamento ad uno stesso nodo target, per migliorare il layout applicato successivamente.
	        -->
      	       <!--xsl:if test="not(preceding-sibling::edge/@from=@from and preceding-sibling::edge/@to=@to)"-->
      		      <edge id="{@id}" from="{@from}" to="{@to}">
					<attr name="Label">
								<string>
									<xsl:value-of select="attr/string"/>
								</string>							       	
						       	<xsl:if test="(following-sibling::edge/@from=@from and following-sibling::edge/@to=@to) or (following-sibling::edge/@from=@to and following-sibling::edge/@to=@from) or (preceding-sibling::edge/@from=@from and preceding-sibling::edge/@to=@to) or (preceding-sibling::edge/@from=@to and preceding-sibling::edge/@to=@from)">
									<count>
										<xsl:value-of select="count(//edge[@from=$edgeFrom and @to=$edgeTo])+count(//edge[@from=$edgeTo and @to=$edgeFrom])"/>		  
					 				</count>
					 				<!--pos><xsl:value-of select="position()"/></pos-->
								</xsl:if>					
					</attr>
			</edge>
       </xsl:for-each>
</xsl:template>
</xsl:stylesheet>