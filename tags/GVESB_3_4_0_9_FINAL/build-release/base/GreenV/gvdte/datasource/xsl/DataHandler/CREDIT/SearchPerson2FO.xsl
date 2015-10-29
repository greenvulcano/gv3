<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:java="http://xml.apache.org/xalan/java"
    exclude-result-prefixes="java fo">

    <xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>

    <xsl:template match="RowSet">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="A4-portrait" page-height="297mm" page-width="210mm" margin-top="5mm" margin-bottom="5mm" margin-left="10mm" margin-right="5mm">
                    <fo:region-body region-name="PageBody"/>
                    <fo:region-after region-name="Footer" extent="0.3in"/>
                </fo:simple-page-master>
            </fo:layout-master-set>

            <fo:page-sequence master-reference="A4-portrait">
                <fo:static-content flow-name="Footer">
                    <fo:block text-align="right" border-top="1pt solid black"
                        padding-top="1mm">
                        Page
                        <fo:page-number/>
                        of
                        <fo:page-number-citation ref-id="theEnd"/>
                    </fo:block>
                </fo:static-content>
                <fo:flow flow-name="PageBody">

                    <xsl:apply-templates mode="SEARCH" select="."/>

                    <fo:block id="theEnd"/>

                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>

    <xsl:template mode="SEARCH" match="RowSet">
        <fo:block font-size="12pt">
            <fo:table table-layout="fixed" width="100%" border-collapse="collapse">
                <fo:table-column column-width="200mm"/>
                <fo:table-body>
                    <xsl:apply-templates mode="BodyTable" select="data"/>
                </fo:table-body>
            </fo:table>
        </fo:block>
    </xsl:template>

    <xsl:template mode="BodyTable" match="data">
        <fo:table-row>
            <fo:table-cell>
                <fo:block white-space-collapse="false" font-size="12pt">
                    <fo:table table-layout="fixed" width="100%" border-collapse="collapse" border="1pt solid">
                        <fo:table-column column-width="60mm"/>
                        <fo:table-column column-width="25mm"/>
                        <fo:table-column column-width="26mm"/>
                        <fo:table-body>
                            <fo:table-row>
                                <fo:table-cell>
                                    <fo:block>
                                        <fo:inline font-style="italic">Name
                                        </fo:inline>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block>
                                        <fo:inline font-style="italic">Birthday
                                        </fo:inline>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block>
                                        <fo:inline font-style="italic">City
                                        </fo:inline>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                    <fo:block>
                                        <xsl:value-of select="@key_1"/>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block>
                                        <xsl:value-of select="java:it.greenvulcano.util.xml.XSLTUtils.convertDate(@key_2, 'yyyyMMdd HH:mm:ss', 'dd/MM/yyyy')"/>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block>
                                        <xsl:value-of select="@key_3"/>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                        </fo:table-body>
                    </fo:table>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="12pt">
                    <fo:table table-layout="fixed"
                                width="100%"
                                border-collapse="collapse"
                                border="1pt solid" >
                        <fo:table-column column-width="70mm"/>
                        <fo:table-column column-width="25mm"/>
                        <fo:table-column column-width="16mm"/>
                        <fo:table-body>
                            <fo:table-row>
                                <fo:table-cell border="1pt solid">
                                    <fo:block text-align="center">Card Number</fo:block>
                                </fo:table-cell>
                                <fo:table-cell border="1pt solid">
                                    <fo:block text-align="center">Credit</fo:block>
                                </fo:table-cell>
                                <fo:table-cell border="1pt solid">
                                    <fo:block text-align="center">Active</fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <xsl:apply-templates mode="Table_content" select="row"/>
                        </fo:table-body>
                    </fo:table>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell height="0.5cm"
            overflow="hidden" display-align="center" text-align="center">
                <fo:block font-size="20pt" color="red">
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template mode="Table_content" match="row">
        <fo:table-row>
            <fo:table-cell>
                <fo:block text-align="center"><xsl:value-of select="col[1]"/></fo:block>
            </fo:table-cell>
            <fo:table-cell>
                <fo:block text-align="right"><xsl:value-of select="col[2]"/></fo:block>
            </fo:table-cell>
            <fo:table-cell>
                <fo:block text-align="center"><xsl:value-of select="col[3]"/></fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

</xsl:stylesheet>

