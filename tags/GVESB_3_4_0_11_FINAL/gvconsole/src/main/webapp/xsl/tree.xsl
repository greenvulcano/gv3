<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xtree_xml="it.greenvulcano.gvesb.gvconsole.jaxr.tree.xml.TreeID" 
>
<xsl:output method="html" /> 

<xsl:param name="serviceName"/>

<xsl:template match="/MENU">

	<script type="text/javascript" src="dtree/dtree.js"></script>
	
	<div class="PageTableTree" align="left">
	<script>
		tree = new dTree('tree', 'images/');
		tree.config.useIcons = <xsl:value-of select="@USEICONS"/>;
		tree.config.useLines = <xsl:value-of select="@USELINES"/>;
		tree.config.useStatusText = <xsl:value-of select="@USESTATUSTEXT"/>;
		tree.config.useSelection = <xsl:value-of select="@USESELECTION"/>;
		tree.config.folderLinks = <xsl:value-of select="@FOLDERLINKS"/>;
		tree.add(<xsl:value-of select="xtree_xml:id(generate-id(.))"/>,-1,'<xsl:value-of select="@NAME"/>', '', '', '', '');     	

		<xsl:if test="$serviceName != 'all'">
			<xsl:for-each select="//MENU_ITEM[@NAME=$serviceName]">
				<xsl:call-template name="printmenuitem">
					<xsl:with-param name="parent" select="/MENU"/>
				</xsl:call-template>
			</xsl:for-each>
		</xsl:if>

		<xsl:if test="$serviceName = 'all'">
			<xsl:for-each select="MENU_ITEM">
				<xsl:call-template name="printmenuitem">
					<xsl:with-param name="parent" select="/MENU"/>
				</xsl:call-template>
			</xsl:for-each>
		</xsl:if>


		document.write(tree);

		<xsl:if test="@INITSTATUS = 'open'">
			tree.openAll();
		</xsl:if>
		<xsl:if test="@INITSTATUS = 'close'">
			tree.closeAll();
		</xsl:if>

		<xsl:if test="@OPENTO">
			<xsl:if test="@OPENTO != ''">
				tree.openTo(<xsl:value-of select="@OPENTO"/>, true);
			</xsl:if>
		</xsl:if>

	</script>
	</div>

	<br/>
	<a class="PageTableTreeLink" href="javascript: tree.openAll();">open all</a> | <a class="PageTableTreeLink" href="javascript: tree.closeAll();">close all</a>

</xsl:template>

<xsl:template name="printmenuitem">
	<xsl:param name="parent"/>

	tree.add(<xsl:value-of select="xtree_xml:id(generate-id(.))"/>,<xsl:value-of select="xtree_xml:id(generate-id($parent))"/>,'<xsl:value-of select="@NAME"/>', '<xsl:value-of select="@URL"/><xsl:if test="PARAMETER">?<xsl:for-each select="child::PARAMETER"><xsl:value-of select="attribute::NAME"/>=<xsl:value-of select="attribute::VALUE"/><xsl:if test="position() != last()">&amp;</xsl:if></xsl:for-each></xsl:if>', '<xsl:value-of select="@TITLE"/>', '<xsl:value-of select="@TARGET"/>', '<xsl:value-of select="@IMAGE"/>','<xsl:value-of select="@IMAGEOPEN"/>');     	

	<xsl:for-each select="MENU_ITEM">
		<xsl:call-template name="printmenuitem">
			<xsl:with-param name="parent" select=".."/>
		</xsl:call-template>
	</xsl:for-each>

</xsl:template>


</xsl:stylesheet>
