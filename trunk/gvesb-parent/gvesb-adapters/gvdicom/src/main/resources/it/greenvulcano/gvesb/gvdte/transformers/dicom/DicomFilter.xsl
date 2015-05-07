<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:htm="http://www.w3.org/1999/xhtml">

	<xsl:template match="@*|node()">
		<xsl:if test="local-name() != 'DicomTag'">
			<xsl:copy>
				<xsl:apply-templates select="@*|node()" />
			</xsl:copy>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
