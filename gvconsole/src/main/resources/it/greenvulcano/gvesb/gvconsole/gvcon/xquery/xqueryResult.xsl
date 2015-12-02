<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:result="http://saxon.sf.net/xquery-results" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0">
	<xsl:output method="html"/>
	<xsl:template match="/">
		<hr/>
		<xsl:if test="result:sequence/result:element">
			<TABLE cellspacing="0" cellpadding="1" class="xquery">
				<TR>
					<TD class="width5">
                    </TD>
					<TD>
						<b>
							<font>XQuery results</font>
						</b>
					</TD>
				</TR>
				<TR>
					<TD colspan="2">
						<TABLE cellspacing="0" cellpadding="6" class="xquery">
							<xsl:apply-templates select="result:sequence/result:element"/>
						</TABLE>
					</TD>
				</TR>
			</TABLE>
		</xsl:if>
		<xsl:if test="not(result:sequence/result:element)">
			<b>No result found</b>
		</xsl:if>
	</xsl:template>
	<xsl:template match="result:element">
		<TR>
			<TD colspan="5" style="padding-top:0px; padding-bottom:0px"/>
		</TR>
		<xsl:variable name="color">
			<xsl:if test="position() mod 2 = 0">color2</xsl:if>
			<xsl:if test="position() mod 2 = 1">color3</xsl:if>
		</xsl:variable>
		<TR valign="top" bgcolor="{$color}">
			<TD>
				<b>
					<li/>
				</b>
			</TD>
			<TD class="width5"/>
			<TD>
				<nobr>
					<xsl:value-of select="position()"/>
				</nobr>
			</TD>
			<TD>
			
			
			
			
			
			
			
			
				<nobr>
					<textarea rows="6" cols="100" readonly="true">
						<xsl:copy-of select="./*"/>
					</textarea>
				</nobr>
			</TD>
			<TD class="width5"/>
		</TR>
	</xsl:template>
</xsl:stylesheet>
