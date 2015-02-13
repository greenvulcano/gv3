<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

<xsl:template match="/">
		<html>
			<head>
			<xsl:element name="Portal">
				<xsl:call-template name="Head"/>
			</xsl:element>	
			</head>
			<body>
				<xsl:call-template name="Body"/>
			</body>
		</html>
</xsl:template>

<xsl:template name="Head">	 
	<title>Workbench</title>
	<meta http-equiv="content-type" content="text/html; charset=iso-8859-1"/>
	<!-- testa -->
	<div id="testa">
		aaaa	
	</div>
	<!-- /testa -->
</xsl:template> 

<xsl:template name="Body">	 
	<table width="100%">
		<tbody>
			<tr>
				<td>
					<div id="corpo">
						<xsl:for-each select="//section[@position='LEFT']">
							<xsl:sort select="@valign" order="ascending" />																	<xsl:call-template  name="section"/>
						</xsl:for-each>
					</div>
				</td>
				<td>
					<div id="corpo">
						<xsl:for-each select="//section[@position='RIGHT']">
							<xsl:sort select="@valign" order="ascending" />
								<xsl:call-template name="section"/>
						</xsl:for-each>
					</div>
				</td>
			</tr>
		</tbody>
	</table>	
</xsl:template> 

<xsl:template name = "section">
	<div id = "sezione"  style = "background: {@background}; float: {@position}; clear: {@position}; width: {@width};" >	
		<div id="head" style="background: {@headColor}; border-color: {@borderColor};">
 			<font class="titlesmall"><xsl:value-of select="@title"/></font>
 		</div>
 		<xsl:call-template name="include"/>
    	</div>	 
</xsl:template>

<xsl:template name = "include">
	<xsl:for-each select="./include">
		<xsl:if test="@id='image'">
			<div id="img" style="background: {@backgroundColor}; float: {@position};"> 
				<img border="0" src="{.}"/>
		       </div>	
		</xsl:if> 
		<xsl:if test="@id='file'">
			<div id="sub-sezione" style="background: {@backgroundColor}; float: {@position};"> 
				<xsl:value-of select="."/>	       
			</div>	
		</xsl:if> 
		<xsl:if test="@id='text'">
			<div id="sub-sezione" style="background: {@backgroundColor}; float: {@position};"> 
				<xsl:value-of select="."/>
		       </div>	
		</xsl:if> 
    	</xsl:for-each> 
</xsl:template>


</xsl:transform>