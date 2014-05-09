<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>
    <xsl:output indent="yes"/>
    <xsl:output doctype-public="-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN"/>
    <xsl:output doctype-system="http://java.sun.com/j2ee/dtds/ejb-jar_2_0.dtd"/>

    <xsl:template match="/">
        <ejb-jar>
            <enterprise-beans>
                <xsl:apply-templates select="/GVCore/GVForwards/ForwardConfiguration/*[@type='forward-deployment' and @enabled='true']" mode="enterprise-beans"/>
            </enterprise-beans>
            <assembly-descriptor>
                <xsl:apply-templates select="/GVCore/GVForwards/ForwardConfiguration/*[@type='forward-deployment' and @enabled='true']" mode="assembly-descriptor"/>
            </assembly-descriptor>
        </ejb-jar>
    </xsl:template>

    <xsl:template match="/GVCore/GVForwards/ForwardConfiguration/*" mode="enterprise-beans">
        <xsl:call-template name="message-driven">
            <xsl:with-param name="count">1</xsl:with-param>
        </xsl:call-template>
        <xsl:if test="@number-of-mdb">
            <xsl:call-template name="loop-message-driven">
                <xsl:with-param name="count">2</xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template match="/GVCore/GVForwards/ForwardConfiguration/*" mode="assembly-descriptor">
        <xsl:call-template name="container-transaction">
            <xsl:with-param name="count">1</xsl:with-param>
        </xsl:call-template>
        <xsl:if test="@number-of-mdb">
            <xsl:call-template name="loop-container-transaction">
                 <xsl:with-param name="count">2</xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="loop-message-driven">
        <xsl:param name="count"/>
        <xsl:if test="$count &lt;= @number-of-mdb">
            <xsl:call-template name="message-driven">
                <xsl:with-param name="count"><xsl:value-of select="$count"/></xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="loop-message-driven">
                <xsl:with-param name="count"><xsl:value-of select="$count + 1"/></xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="loop-container-transaction">
        <xsl:param name="count"/>
        <xsl:if test="$count &lt;= @number-of-mdb">
            <xsl:call-template name="container-transaction">
                <xsl:with-param name="count"><xsl:value-of select="$count"/></xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="loop-container-transaction">
                <xsl:with-param name="count"><xsl:value-of select="$count + 1"/></xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="message-driven">
        <xsl:param name="count"/>
        <message-driven>
            <description><xsl:value-of select="description"/></description>
            <ejb-name>GV_FORWARD_<xsl:value-of select="@forward-name"/>_<xsl:value-of select="$count"/></ejb-name>
            <ejb-class>it.greenvulcano.gvesb.core.ejb.J2EEForwardBean</ejb-class>
			<xsl:choose>
				<xsl:when test="@trans-attribute='UserTransaction'">
					<transaction-type>Bean</transaction-type>
				</xsl:when>
				<xsl:otherwise>
					<transaction-type>Container</transaction-type>
				</xsl:otherwise>
			</xsl:choose>
            <acknowledge-mode>Auto-acknowledge</acknowledge-mode>
            <message-driven-destination>
                <xsl:if test="@destination-type='queue'">
                    <destination-type>javax.jms.Queue</destination-type>
                </xsl:if>
                <xsl:if test="@destination-type='topic'">
                    <destination-type>javax.jms.Topic</destination-type>
                </xsl:if>
            </message-driven-destination>
            <env-entry>
				<env-entry-name>transaction-type-entry</env-entry-name>
				<env-entry-type>java.lang.String</env-entry-type>
				<env-entry-value><xsl:value-of select="@trans-attribute"/></env-entry-value>
			</env-entry>
			<xsl:if test="@trans-attribute='UserTransaction'">
				<env-entry>
					<env-entry-name>transTimeoutSeconds-type-entry</env-entry-name>
					<env-entry-type>java.lang.String</env-entry-type>
					<env-entry-value><xsl:value-of select="@trans-timeout-seconds"/></env-entry-value>
				</env-entry>
			</xsl:if>
			<env-entry>
                <env-entry-name>forward-name</env-entry-name>
                <env-entry-type>java.lang.String</env-entry-type>
                <env-entry-value><xsl:value-of select="@forward-name"/></env-entry-value>
            </env-entry>
            <env-entry>
                <env-entry-name>server-name-entry</env-entry-name>
                <env-entry-type>java.lang.String</env-entry-type>
				<env-entry-value><xsl:value-of select="@server-name-property-name"/></env-entry-value>
            </env-entry>
        </message-driven>
    </xsl:template>

    <xsl:template name="container-transaction">
        <xsl:param name="count"/>
		<xsl:if test="@trans-attribute='Required' or @trans-attribute='NotSupported'">
            <container-transaction>
                <method>
                    <ejb-name>GV_FORWARD_<xsl:value-of select="@forward-name"/>_<xsl:value-of select="$count"/></ejb-name>
                    <method-name>*</method-name>
                </method>
                <trans-attribute><xsl:value-of select="@trans-attribute"/></trans-attribute>
            </container-transaction>
		</xsl:if>
    </xsl:template>

</xsl:stylesheet>
