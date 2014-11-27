<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
     xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <xsl:output version="1.0"/>
    <xsl:output encoding="US-ASCII"/>
    <xsl:output omit-xml-declaration="no"/>

    <!--========================================================================
        document/@font-family
        document/@break-before
    -->
    <xsl:template match ="document">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

            <fo:layout-master-set>

                <fo:simple-page-master master-name="all"
                    page-height="29.7cm" page-width="21cm">
                    <fo:region-body margin-top="3cm" margin-bottom="3cm"
                        margin-left="3cm" margin-right="3cm"/>
            		<fo:region-after extent="1.5cm"/>
                </fo:simple-page-master>

            </fo:layout-master-set>

            <fo:page-sequence master-reference="all">

                <fo:static-content flow-name="xsl-region-after">
                    <fo:block text-align="end" margin-right="3cm"
                        font-size="11pt"
                        line-height="1em + 3pt">
                        <xsl:if test="/document/@font-family">
                            <xsl:attribute name="font-family">
                                <xsl:value-of select="/document/@font-family"/>
                            </xsl:attribute>
                        </xsl:if>
                        <xsl:if test="not(/document/@font-family)">
                            <xsl:attribute name="font-family">serif</xsl:attribute>
                        </xsl:if>
                        - <fo:page-number/> -
                    </fo:block>
                </fo:static-content>

                <fo:flow flow-name="xsl-region-body"
                	     text-align="start"
                	     font-size="11pt">
                    <xsl:if test="/document/@font-family">
                        <xsl:attribute name="font-family">
                            <xsl:value-of select="/document/@font-family"/>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:if test="not(/document/@font-family)">
                        <xsl:attribute name="font-family">serif</xsl:attribute>
                    </xsl:if>
                    <xsl:apply-templates select="cover"/>
                	<xsl:call-template name="table-of-contents"/>
                    <xsl:apply-templates select="chapter"/>
                </fo:flow>

            </fo:page-sequence>

        </fo:root>
    </xsl:template>

    <!--========================================================================
        COPERTINA
    -->

    <xsl:template match="cover">
    	<fo:block font-size="28pt"
    	          border-before-style="solid"
    	          border-before-width="0.5pt"
    	          space-before="3mm"
    	          border-after-style="solid"
    	          border-after-width="0.5pt"
    	          space-after="3mm"
    	          text-align="right"
    	>
    		<xsl:value-of select="@company"/>
    	</fo:block>

    	<fo:block space-after="5cm"/>

    	<fo:block font-size="28pt"
    	          text-align="right"
    	          font-weight="bold"
    	>
    		<xsl:value-of select="@title"/>
    	</fo:block>

    	<fo:block font-size="20pt"
    	          text-align="right"
    	          font-weight="bold"
    	>
    		<xsl:value-of select="@subtitle"/>
    	</fo:block>

    	<fo:block font-size="17pt"
    	          text-align="right"
    	          space-before="5mm"
    	>
    		Versione <xsl:value-of select="@version"/>
    		del <xsl:value-of select="@date"/>
    	</fo:block>

    	<fo:block font-size="17pt"
    	          text-align="right"
    	          space-before="5mm"
    	>
    		Autore: <fo:inline font-weight="bold"><xsl:value-of select="@author"/></fo:inline>
    	</fo:block>
    </xsl:template>

    <!--========================================================================
        INDICE
    -->

    <xsl:template name="table-of-contents">
    	<fo:block font-size="28pt"
    	          border-after-style="solid"
    	          border-after-width="0.5pt"
    	          space-after="2mm"
    	>
            <xsl:if test="/document/@break-before">
                <xsl:attribute name="break-before">
                    <xsl:value-of select="/document/@break-before"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="not(/document/@break-before)">
                <xsl:attribute name="break-before">odd-page</xsl:attribute>
            </xsl:if>
    		Table of contents
    	</fo:block>
    	<fo:block space-after="2cm"/>
		<xsl:for-each select="chapter">
		    <xsl:variable name="chapter-id" select="generate-id()"/>
        	<fo:block font-size="12pt"
        	          space-before="2mm"
        	          font-weight="bold"
        	>
        	    <fo:basic-link internal-destination="{$chapter-id}">
    		        Chapter <xsl:value-of select="position()"/> - <xsl:value-of select="title"/>,
        		    <fo:page-number-citation ref-id="{$chapter-id}"/>
    		    </fo:basic-link>
        	</fo:block>
        	<xsl:for-each select="section[@title]">
            	<fo:block font-size="11pt"
            	          space-before="1mm"
            	          margin-left="2cm"
                >
                    <fo:basic-link internal-destination="{generate-id(@title)}">
        		        <xsl:value-of select="@title"/>,
            		    <fo:page-number-citation ref-id="{generate-id(@title)}"/>
        		    </fo:basic-link>
            	</fo:block>
        	</xsl:for-each>
		</xsl:for-each>
    </xsl:template>

    <!--========================================================================
        CAPITOLI
    -->

    <!--========================================================================
        chapter/title           - titolo del capitolo
        chapter/title/@subtitle - sottotitolo
        chapter/section         - sezioni (una o piu')
    -->
    <xsl:template match="chapter">
    	<fo:block font-size="28pt"
    	          id="{generate-id()}"
    	>
            <xsl:if test="/document/@break-before">
                <xsl:attribute name="break-before">
                    <xsl:value-of select="/document/@break-before"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="not(/document/@break-before)">
                <xsl:attribute name="break-before">odd-page</xsl:attribute>
            </xsl:if>
    		Chapter <xsl:text> </xsl:text> <xsl:value-of select="position()"/>
    	</fo:block>
    	<fo:block font-size="34pt"
    	          space-before="1cm"
    	          space-after="2cm"
    	          border-after-style="solid"
    	          border-after-width="0.5pt"
    	>
    		<xsl:apply-templates select="title"/>
    		<xsl:if test="title/@subtitle">
    		    <fo:block font-size="60%">
    		        <xsl:value-of select="title/@subtitle"/>
    		    </fo:block>
    		</xsl:if>
    	</fo:block>

    	<xsl:if test="./section[@title]">
        	<fo:block margin-left="2cm">

            	<fo:block font-size="13pt"
            	    space-after="2cm" font-weight="bold"
            	    border-after-style="solid"
            	    border-after-width="0.5pt"
            	>
            	    Chapter contents:
                	<xsl:for-each select="./section/@title">
                    	<fo:block font-size="11pt"
                    	    margin-left="3cm"
                    	    space-before="2mm"
                    	    font-weight="normal"
                    	>
                    	    <fo:basic-link internal-destination="{generate-id()}">
                    		    <xsl:value-of select="."/>
                    		</fo:basic-link>
                    	</fo:block>
                	</xsl:for-each>
                	<fo:block space-after="2mm"/>
            	</fo:block>

        	</fo:block>
    	</xsl:if>

    	<fo:block text-align="justify"
    	>
    	    <xsl:apply-templates select="section"/>
    	</fo:block>
    </xsl:template>

    <!--========================================================================
        SEZIONI
    -->

    <!--========================================================================
        section/@title - opzionale - titolo della sezione
    -->
    <xsl:template match="section">
    	<fo:block font-size="13pt"
    	          space-before="0.8cm"
    	          font-weight="bold"
    	          id="{generate-id(@title)}"
    	>
    	    <xsl:value-of select="@title"/>
    	</fo:block>
    	<fo:block space-before="0.3cm"
    	          text-align="justify"
    	          margin-left="0.4cm"
    	>
    	    <xsl:apply-templates/>
    	</fo:block>
    </xsl:template>

    <!--========================================================================
        LINKS
    -->

    <!--========================================================================
        target/@id
    -->
    <xsl:template match="target">
        <fo:block id="{@id}">
    	    <xsl:apply-templates/>
    	</fo:block>
    </xsl:template>

    <!--========================================================================
        reference/@id
    -->
    <xsl:template match="reference">
        <fo:basic-link internal-destination="{@id}">
    	    <xsl:apply-templates/>
    	</fo:basic-link>
    </xsl:template>

    <!--========================================================================
        page-of/@id
    -->
    <xsl:template match="page-of">
        <fo:page-number-citation ref-id="{@id}"/>
    </xsl:template>

    <!--========================================================================
        TABELLE
    -->

    <xsl:template match="table">
        <fo:table width="{@width}"
            table-layout="fixed"
        >
            <xsl:apply-templates select="colspec"/>
            <fo:table-body text-align="start">
                <xsl:apply-templates select="row"/>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template match="colspec">
        <fo:table-column column-width="{@width}">
            <xsl:attribute name="column-number">
                <xsl:number count="colspec"/>
            </xsl:attribute>
        </fo:table-column>
    </xsl:template>

    <xsl:template match="row">
        <xsl:variable name="position" select="position()"/>
        <xsl:variable name="last" select="last()"/>
        <fo:table-row> <!-- keep-together="always"-->
            <xsl:if test="$position = 1">
                <xsl:attribute name="keep-with-next">always</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="entry">
                <xsl:with-param name="position" select="$position"/>
                <xsl:with-param name="last" select="$last"/>
            </xsl:apply-templates>
        </fo:table-row>
    </xsl:template>

    <xsl:template match="entry">
        <xsl:param name="position">0</xsl:param>
        <xsl:param name="last">0</xsl:param>
        <fo:table-cell
              border-bottom-style="solid"
              padding-top="1mm"
              padding-bottom="1mm"
              font-size="10pt"
        >
            <xsl:if test="$position = 1">
                <xsl:attribute name="font-weight">bold</xsl:attribute>
                <xsl:attribute name="border-top-style">solid</xsl:attribute>
                <xsl:attribute name="border-top-width">1pt</xsl:attribute>
                <xsl:attribute name="border-top-color">rgb(120,120,150)</xsl:attribute>
                <xsl:attribute name="background-color">rgb(230,230,250)</xsl:attribute>
            </xsl:if>
            <xsl:if test="($position != 1) and ($position != $last)">
                <xsl:attribute name="border-bottom-width">0.2pt</xsl:attribute>
                <xsl:attribute name="border-bottom-color">rgb(130,130,160)</xsl:attribute>
            </xsl:if>
            <xsl:if test="($position = 1) or ($position = $last)">
                <xsl:attribute name="border-bottom-width">1pt</xsl:attribute>
                <xsl:attribute name="border-bottom-color">rgb(120,120,150)</xsl:attribute>
            </xsl:if>
            <fo:block>
                <xsl:apply-templates/>
            </fo:block>
        </fo:table-cell>
    </xsl:template>

    <!--========================================================================
        LISTE
    -->

    <!--========================================================================
        Lista ordinata
        ol/@format - opzionale - formato della numerazione. Es.: a. A. 1. i. I.
    -->
    <xsl:template match="ol">
        <fo:list-block provisional-distance-between-starts="15mm"
                       provisional-label-separation="5mm">
            <xsl:apply-templates/>
        </fo:list-block>
    </xsl:template>

    <xsl:template match="ol/li">
        <fo:list-item>
            <fo:list-item-label start-indent="body-start() - 12mm" end-indent="label-end()">
                <fo:block space-before="1mm">
                    <xsl:if test="../@format">
                        <xsl:number format="{../@format}"/>
                    </xsl:if>
                    <xsl:if test="not(../@format)">
                        <xsl:number format="1."/>
                    </xsl:if>
                </fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block space-after="1mm">
                    <xsl:apply-templates select="li"/>
                </fo:block>
            </fo:list-item-body>
        </fo:list-item>
    </xsl:template>

    <!--========================================================================
        Lista puntata
        ul/@point - opzionale - carattere da utilizzare come punto della lista
    -->
    <xsl:template match="ul">
        <fo:list-block provisional-distance-between-starts="10mm"
                       provisional-label-separation="5mm">
            <xsl:apply-templates select="li"/>
        </fo:list-block>
    </xsl:template>

    <xsl:template match="ul/li">
        <fo:list-item>
            <fo:list-item-label start-indent="body-start() - 7mm" end-indent="label-end()">
                <fo:block space-before="1mm">
                    <xsl:if test="../@point">
                        <xsl:value-of select="../@point"/>
                    </xsl:if>
                    <xsl:if test="not(../@point)">
                        &#8226;
                    </xsl:if>
                </fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block space-after="1mm">
                    <xsl:apply-templates/>
                </fo:block>
            </fo:list-item-body>
        </fo:list-item>
    </xsl:template>

    <!--========================================================================
        STILI DEI FONT
    -->

    <!--========================================================================
    -->
    <xsl:template match="b">
        <fo:inline font-weight="bold">
    	    <xsl:apply-templates/>
    	</fo:inline>
    </xsl:template>

    <!--========================================================================
    -->
    <xsl:template match="u">
        <fo:inline text-decoration="underline">
    	    <xsl:apply-templates/>
    	</fo:inline>
    </xsl:template>

    <!--========================================================================
    -->
    <xsl:template match="i">
        <fo:inline font-style="italic">
    	    <xsl:apply-templates/>
    	</fo:inline>
    </xsl:template>

    <!--========================================================================
    -->
    <xsl:template match="code">
        <fo:inline font-family="monospace"
                   font-size="10pt"
        >
    	    <xsl:apply-templates/>
    	</fo:inline>
    </xsl:template>

    <!--========================================================================
        RITORNI A CAPO
    -->

    <!--========================================================================
    -->
    <xsl:template match="br">
        <fo:block>
    	    <xsl:apply-templates/>
    	</fo:block>
    </xsl:template>

    <!--========================================================================
    -->
    <xsl:template match="p">
        <fo:block space-before="0.3cm">
    	    <xsl:apply-templates/>
    	</fo:block>
    </xsl:template>

    <!--========================================================================
        TESTO PREFORMATTATO
        pre
    -->
    <xsl:template match="pre">
        <fo:block font-family="monospace"
                  font-size="10pt"
                  white-space-collapse="false"
                  text-align="left"
        >
    	    <xsl:apply-templates/>
    	</fo:block>
    </xsl:template>

    <!--========================================================================
        ALLINEAMENTI
    -->
    <xsl:template match="left">
        <fo:block text-align="left">
    	    <xsl:apply-templates/>
    	</fo:block>
    </xsl:template>

    <xsl:template match="right">
        <fo:block text-align="right">
    	    <xsl:apply-templates/>
    	</fo:block>
    </xsl:template>

    <xsl:template match="center">
        <fo:block text-align="center">
    	    <xsl:apply-templates/>
    	</fo:block>
    </xsl:template>

    <xsl:template match="justify">
        <fo:block text-align="justify">
    	    <xsl:apply-templates/>
    	</fo:block>
    </xsl:template>

    <!--========================================================================
    -->
    <xsl:template match="small">
        <fo:inline font-size="80%">
    	    <xsl:apply-templates/>
    	</fo:inline>
    </xsl:template>

    <!--========================================================================
    -->
    <xsl:template match="big">
        <fo:inline font-size="120%">
    	    <xsl:apply-templates/>
    	</fo:inline>
    </xsl:template>

    <!--========================================================================
        La versione corrente di FOP non implementa pienamente keep-together
    -->
    <xsl:template match="together">
        <fo:block keep-together="always">
    	    <xsl:apply-templates/>
    	</fo:block>
    </xsl:template>

    <!--========================================================================
        Immagini
    -->
    <xsl:template match="img">
        <fo:external-graphic src="{@src}">
            <xsl:if test="@width">
                <xsl:attribute name="width"><xsl:value-of select="@width"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="@height">
                <xsl:attribute name="height"><xsl:value-of select="@height"/></xsl:attribute>
            </xsl:if>
        </fo:external-graphic>
    </xsl:template>

</xsl:stylesheet>
