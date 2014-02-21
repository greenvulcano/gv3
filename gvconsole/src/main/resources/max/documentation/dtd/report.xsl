<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:output version="1.0"/>
    <xsl:output encoding="US-ASCII"/>
    <xsl:output omit-xml-declaration="no"/>

    <xsl:template match="configuration-guide">
        <document
            font-family="serif"
            break-before="page"
        >
            <cover title="{@title}"
                subtitle="Manuale di configurazione"
                version="{@version}"
                date="{@date}"
                author="{@author}"
                company="{@company}"
            />

            <chapter>
                <title>
                    <xsl:value-of select="@title"/> - <br/>
                    <small>Manuale di configurazione</small>
                </title>

                <section title="Introduzione">

                    Questo documento mostra tutti i parametri che possono essere
                    configurati per definire le funzionalità di
                    <b><xsl:value-of select="@title"/></b>.
                    <p/>
                    I parametri di configurazione trattati nel seguente manuale
                    sono suddivisi nei capitoli:
                    <p/>
                    <ul>
                        <xsl:for-each select="dtd">
                            <li><b><xsl:value-of select="@title"/></b></li>
                        </xsl:for-each>
                    </ul>
                    <p/>
                    La configurazione è strutturata come un albero, ed in effetti
                    è rappresentata con uno o più files in formato XML con
                    elementi ed attributi.
                    <p/>
                    Questo documento mostra una descrizione per tutti gli elementi,
                    i loro attributi e le relazioni padre/figlio che intercorrono
                    tra gli elementi.
                </section>
            </chapter>

            <xsl:apply-templates select="dtd"/>

            <xsl:call-template name="index"/>
        </document>
    </xsl:template>

    <!--========================================================================
        FORMATTAZIONE DEL DTD
    -->
    <xsl:template match="dtd">
        <chapter>
            <title>
                <xsl:value-of select="@title"/> - <br/>
                <small>Configurazione</small>
            </title>

            <section title="Document type">
                Per utilizzare correttamente il tool di configurazione, il file
                di configurazione di <b><xsl:value-of select="@title"/></b> deve
                avere la clausola DOCTYPE con i seguenti parametri:
                <p/>
                <ul>
                    <li>Root element:
                        <reference id="R{generate-id(element[@name=current()/@root-element])}">
                            <b><xsl:value-of select="@root-element"/></b>
                        </reference>
                    </li>
                    <li>System id: <b><xsl:value-of select="@system-id"/></b></li>
                    <li>Public id: <b><xsl:value-of select="@public-id"/></b></li>
                </ul>
                <p/>
                <left>
                    <xsl:if test="@public-id and @public-id != ''">
                        <code>&lt;!DOCTYPE <xsl:value-of select="@root-element"/> PUBLIC "<xsl:value-of select="@public-id"/>" "<xsl:value-of select="@system-id"/>"&gt;</code>
                    </xsl:if>
                    <xsl:if test="not(@public-id and @public-id != '')">
                        <code>&lt;!DOCTYPE <xsl:value-of select="@root-element"/> SYSTEM "<xsl:value-of select="@system-id"/>"&gt;</code>
                    </xsl:if>
                </left>
            </section>

            <section title="Elementi">
                Le seguenti sezioni, una per ogni elemento, descrivono tutti gli
                elementi ed i loro attributi.
            </section>

            <xsl:apply-templates>
                <xsl:sort select="@name"/>
            </xsl:apply-templates>

        </chapter>
    </xsl:template>

    <!--========================================================================
        FORMATTAZIONE DEGLI ELEMENTI
    -->
    <xsl:template match="element">
        <section>
            <target id="R{generate-id(.)}">
                <b><big><big><xsl:value-of select="@name"/></big></big></b>
            </target>
            <br/>
            <code><small><b><xsl:apply-templates select="model"/></b></small></code>
            <p/>

            <xsl:copy-of select="description/node()"/>
            <xsl:if test="value">
                <p/>
                Valori ammessi:
                <ul>
                    <xsl:for-each select="value">
                        <li><b><xsl:value-of select="@value"/></b></li>
                    </xsl:for-each>
                </ul>
            </xsl:if>
            <xsl:if test="not-null">
                <p/>
                Il valore dell'elemento non può essere nullo.
            </xsl:if>

            <xsl:if test="used-in">
                <p/>
                <xsl:call-template name="used-in"/>
            </xsl:if>

            <xsl:if test="attribute">
                <p/>
                La tabella seguente mostra gli attributi supportati dall'elemento
                <b><xsl:value-of select="@name"/></b>:
                <p/>
                <table width="15cm">
                    <colspec width="4cm"/>
                    <colspec width="2cm"/>
                    <colspec width="9cm"/>
                    <row>
                        <entry>Attributo</entry>
                        <entry>Tipo</entry>
                        <entry>Descrizione</entry>
                    </row>
                    <xsl:apply-templates select="attribute"/>
                </table>
                <p/>
            </xsl:if>
        </section>
    </xsl:template>


    <!-- MODELLO -->

    <xsl:template match="model">
        <left>
            <xsl:apply-templates select="node()"/>
        </left>
    </xsl:template>

    <xsl:template match="child">
        <xsl:variable name="element"><xsl:value-of select="."/></xsl:variable>
        <reference id="R{generate-id(ancestor::dtd/element[@name=$element])}"><xsl:value-of select="$element"/></reference>
    </xsl:template>

    <!-- PARENTS -->

    <xsl:template name="used-in">
        L'elemento <b><xsl:value-of select="@name"/></b> e' usato in:
        <xsl:for-each select="used-in">
            <xsl:sort select="@element"/>
            <xsl:variable name="element"><xsl:value-of select="@element"/></xsl:variable>

            <reference id="R{generate-id(ancestor::dtd/element[@name=$element])}"><i><xsl:value-of select="$element"/></i></reference>
            <xsl:if test="position() &lt; last()">
                <xsl:text>, </xsl:text>
            </xsl:if>
            <xsl:if test="position() = last()">
                <xsl:text>.</xsl:text>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <!--========================================================================
        FORMATTAZIONE DEGLI ATTRIBUTI
    -->
    <xsl:template match="attribute">
        <row>
            <entry>
                <b><xsl:value-of select="@name"/></b>
                <!--
                    Debugging
                -->
                <!--br/>
                <xsl:value-of select="@type"/><br/>
                <xsl:value-of select="@default-type"/><br/>
                <xsl:value-of select="@default-value"/><br/-->
            </entry>

            <entry>
                <xsl:choose>
                    <xsl:when test="@default-type='#REQUIRED'">required</xsl:when>
                    <xsl:when test="@default-type='#FIXED'">fixed</xsl:when>
                    <xsl:otherwise>optional</xsl:otherwise>
                </xsl:choose>
            </entry>

            <entry>
                <xsl:copy-of select="description/node()"/>
                <xsl:if test="@default-type='#FIXED'">
                    <p/>
                    Questo attributo <i>deve</i> assumere il valore
                    <b><xsl:value-of select="@default-value"/></b>.
                </xsl:if>
                <xsl:choose>
                    <xsl:when test="@default-type='#FIXED'"/>
                    <xsl:when test="@default-type='#REQUIRED'"/>
                    <xsl:when test="@default-type='#IMPLIED'"/>
                    <xsl:otherwise>
                        <p/>
                        Il valore di default per l'attributo è:
                        <b><xsl:value-of select="@default-value"/></b>.
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="value">
                    <p/>
                    I valori ammessi per l'attributo sono:
                    <ul>
                        <xsl:for-each select="value">
                            <li><b><xsl:value-of select="@value"/></b></li>
                        </xsl:for-each>
                    </ul>
                </xsl:if>
                <xsl:if test="not-null">
                    <p/>
                    Il valore dell'attributo non può essere nullo.
                </xsl:if>
            </entry>
        </row>
    </xsl:template>

    <!--========================================================================
        INDICE ANALITICO
    -->
    <xsl:template name="index">
        <chapter>
            <title>Indice analitico</title>

            <section>
                <xsl:for-each select="/configuration-guide/dtd/element">
                    <xsl:sort select="@name"/>
                    <reference id="R{generate-id(.)}">
                        <xsl:value-of select="@name"/>, <page-of id="R{generate-id(.)}"/>
                    </reference>
                    <br/>
                </xsl:for-each>
            </section>
        </chapter>
    </xsl:template>

</xsl:stylesheet>
