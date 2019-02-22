<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:variable name="NEWLINE" select="'&#x0A;'"/>
    <xsl:variable name="jboss4-doctype" select="'!DOCTYPE jboss PUBLIC &quot;-//JBoss//DTD JBOSS 4.0//EN&quot; &quot;http://www.jboss.org/j2ee/dtd/jboss_4_0.dtd&quot;'"/>
    <xsl:variable name="wls8-doctype" select="'!DOCTYPE jboss PUBLIC &quot;-//BEA Systems, Inc.//DTD WebLogic 8.1.0 EJB//EN&quot; &quot;http://www.bea.com/servers/wls810/dtd/weblogic-ejb-jar.dtd&quot;'"/>

    <xsl:output method="xml"/>
    <xsl:output indent="yes"/>

    <xsl:template match="/">
        <xsl:if test="local-name(/GVCore/GVForwards/ForwardConfiguration/*[@type='forward-deployment' and @enabled='true'])='JBoss4ForwardDeployment'">
            <xsl:text disable-output-escaping="yes">&lt;</xsl:text>
            <xsl:value-of select="$jboss4-doctype"/>
            <xsl:text disable-output-escaping="yes">&gt;</xsl:text>
            <xsl:value-of select="$NEWLINE"/>
            <jboss>
                <enterprise-beans>
                    <xsl:apply-templates select="/GVCore/GVForwards/ForwardConfiguration/JBoss4ForwardDeployment[@type='forward-deployment' and @enabled='true']" mode="enterprise-beans"/>
                </enterprise-beans>
                <invoker-proxy-bindings>
                    <xsl:apply-templates select="/GVCore/GVForwards/ForwardConfiguration/JBoss4ForwardDeployment[@type='forward-deployment' and @enabled='true']" mode="invoker-proxy-bindings"/>
                </invoker-proxy-bindings>
            </jboss>
        </xsl:if>
        <xsl:if test="local-name(/GVCore/GVForwards/ForwardConfiguration/*[@type='forward-deployment' and @enabled='true'])='JBoss4ResourceManagerForwardDeployment'">
            <xsl:text disable-output-escaping="yes">&lt;</xsl:text>
            <xsl:value-of select="$jboss4-doctype"/>
            <xsl:text disable-output-escaping="yes">&gt;</xsl:text>
            <xsl:value-of select="$NEWLINE"/>
            <jboss>
                <enterprise-beans>
                    <xsl:apply-templates select="/GVCore/GVForwards/ForwardConfiguration/JBoss4ResourceManagerForwardDeployment[@type='forward-deployment' and @enabled='true']" mode="enterprise-beans-rm"/>
                </enterprise-beans>
            </jboss>
        </xsl:if>
        <xsl:if test="local-name(/GVCore/GVForwards/ForwardConfiguration/*[@type='forward-deployment' and @enabled='true'])='WLS8ForwardDeployment'">
            <xsl:text disable-output-escaping="yes">&lt;</xsl:text>
            <xsl:value-of select="$wls8-doctype"/>
            <xsl:text disable-output-escaping="yes">&gt;</xsl:text>
            <xsl:value-of select="$NEWLINE"/>
            <weblogic-ejb-jar>
                <xsl:apply-templates select="/GVCore/GVForwards/ForwardConfiguration/WLS8ForwardDeployment[@type='forward-deployment' and @enabled='true']" mode="weblogic-enterprise-bean"/>
            </weblogic-ejb-jar>
        </xsl:if>
    </xsl:template>

    <xsl:template match="/GVCore/GVForwards/ForwardConfiguration/JBoss4ForwardDeployment" mode="enterprise-beans">
        <xsl:call-template name="message-driven">
            <xsl:with-param name="count">1</xsl:with-param>
        </xsl:call-template>
        <xsl:if test="@number-of-mdb">
            <xsl:call-template name="loop-message-driven">
                <xsl:with-param name="count">2</xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template match="/GVCore/GVForwards/ForwardConfiguration/JBoss4ResourceManagerForwardDeployment" mode="enterprise-beans-rm">
        <xsl:call-template name="message-driven-rm">
            <xsl:with-param name="count">1</xsl:with-param>
        </xsl:call-template>
        <xsl:if test="@number-of-mdb">
            <xsl:call-template name="loop-message-driven-rm">
                <xsl:with-param name="count">2</xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template match="/GVCore/GVForwards/ForwardConfiguration/JBoss4ForwardDeployment" mode="invoker-proxy-bindings">
        <xsl:call-template name="invoker-proxy-binding">
            <xsl:with-param name="count">1</xsl:with-param>
        </xsl:call-template>
        <xsl:if test="@number-of-mdb">
            <xsl:call-template name="loop-invoker-proxy-binding">
                <xsl:with-param name="count">2</xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template match="/GVCore/GVForwards/ForwardConfiguration/WLS8ForwardDeployment">
        <xsl:call-template name="weblogic-enterprise-bean">
            <xsl:with-param name="count">1</xsl:with-param>
        </xsl:call-template>
        <xsl:if test="@number-of-mdb">
            <xsl:call-template name="loop-weblogic-enterprise-bean">
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

    <xsl:template name="loop-message-driven-rm">
        <xsl:param name="count"/>
        <xsl:if test="$count &lt;= @number-of-mdb">
            <xsl:call-template name="message-driven-rm">
                <xsl:with-param name="count"><xsl:value-of select="$count"/></xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="loop-message-driven-rm">
                <xsl:with-param name="count"><xsl:value-of select="$count + 1"/></xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="loop-invoker-proxy-binding">
        <xsl:param name="count"/>
        <xsl:if test="$count &lt;= @number-of-mdb">
            <xsl:call-template name="invoker-proxy-binding">
                <xsl:with-param name="count"><xsl:value-of select="$count"/></xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="loop-invoker-proxy-binding">
                <xsl:with-param name="count"><xsl:value-of select="$count + 1"/></xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="message-driven">
        <xsl:param name="count"/>
        <message-driven>
            <ejb-name>GV_FORWARD_<xsl:value-of select="@forward-name"/>_<xsl:value-of select="$count"/></ejb-name>
            <destination-jndi-name><xsl:value-of select="@destination-jndi-name"/></destination-jndi-name>
            <xsl:if test="@local-jndi-name">
	            <local-jndi-name><xsl:value-of select="@local-jndi-name"/></local-jndi-name>
            </xsl:if>
            <xsl:if test="@mdb-user">
                <mdb-user><xsl:value-of select="@mdb-user"/></mdb-user>
            </xsl:if>
            <xsl:if test="@mdb-passwd">
                <mdb-passwd><xsl:value-of select="@mdb-passwd"/></mdb-passwd>
            </xsl:if>
            <xsl:if test="@mdb-client-id">
                <mdb-client-id><xsl:value-of select="@mdb-client-id"/></mdb-client-id>
            </xsl:if>
            <xsl:if test="@mdb-subscription-id">
                <mdb-subscription-id><xsl:value-of select="@mdb-subscription-id"/></mdb-subscription-id>
            </xsl:if>
            <invoker-bindings>
                <invoker>
                    <invoker-proxy-binding-name>INVOKER_<xsl:value-of select="@forward-name"/>_<xsl:value-of select="$count"/></invoker-proxy-binding-name>
                </invoker>
            </invoker-bindings>
        </message-driven>
    </xsl:template>

    <xsl:template name="message-driven-rm">
        <xsl:param name="count"/>
        <message-driven>
            <ejb-name>GV_FORWARD_<xsl:value-of select="@forward-name"/>_<xsl:value-of select="$count"/></ejb-name>
            <destination-jndi-name><xsl:value-of select="@destination-jndi-name"/></destination-jndi-name>
            <xsl:if test="@local-jndi-name">
	            <local-jndi-name><xsl:value-of select="@local-jndi-name"/></local-jndi-name>
            </xsl:if>
            <resource-adapter-name><xsl:value-of select="@resource-adapter-name"/></resource-adapter-name>
        </message-driven>
    </xsl:template>

    <xsl:template name="invoker-proxy-binding">
        <xsl:param name="count"/>
        <invoker-proxy-binding>
            <name>INVOKER_<xsl:value-of select="@forward-name"/>_<xsl:value-of select="$count"/></name>
            <invoker-mbean>default</invoker-mbean>
            <proxy-factory>org.jboss.ejb.plugins.jms.JMSContainerInvoker</proxy-factory>
            <proxy-factory-config>
                <xsl:choose>
                    <xsl:when test="@jms-provider">
                        <JMSProviderAdapterJNDI><xsl:value-of select="@jms-provider"/></JMSProviderAdapterJNDI>
                    </xsl:when>
                    <xsl:otherwise>
                        <JMSProviderAdapterJNDI>DefaultJMSProvider</JMSProviderAdapterJNDI>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:choose>
                    <xsl:when test="@jms-pool">
                        <ServerSessionPoolFactoryJNDI><xsl:value-of select="@jms-pool"/></ServerSessionPoolFactoryJNDI>
                    </xsl:when>
                    <xsl:otherwise>
                        <ServerSessionPoolFactoryJNDI>StdJMSPool</ServerSessionPoolFactoryJNDI>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="@MaximumSize">
                    <MaximumSize><xsl:value-of select="@MaximumSize"/></MaximumSize>
                </xsl:if>
                <xsl:if test="@MaxMessages">
                    <MaxMessages><xsl:value-of select="@MaxMessages"/></MaxMessages>
                </xsl:if>
                <MDBConfig>
                    <ReconnectIntervalSec><xsl:value-of select="@ReconnectIntervalSec"/></ReconnectIntervalSec>
                    <xsl:if test="DLQConfig">
                        <DLQConfig>
                            <DestinationQueue><xsl:value-of select="DLQConfig/@DestinationQueue"/></DestinationQueue>
                            <MaxTimesRedelivered><xsl:value-of select="DLQConfig/@MaxTimesRedelivered"/></MaxTimesRedelivered>
                            <TimeToLive><xsl:value-of select="DLQConfig/@TimeToLive"/></TimeToLive>
                            <xsl:if test="DLQConfig/@DLQUser">
                                <DLQUser><xsl:value-of select="DLQConfig/@DLQUser"/></DLQUser>
                            </xsl:if>
                            <xsl:if test="DLQConfig/@DLQPassword">
                                <DLQPassword><xsl:value-of select="DLQConfig/@DLQPassword"/></DLQPassword>
                            </xsl:if>
                        </DLQConfig>
                    </xsl:if>
                </MDBConfig>
            </proxy-factory-config>
        </invoker-proxy-binding>
    </xsl:template>

    <xsl:template name="loop-weblogic-enterprise-bean">
        <xsl:param name="count"/>
        <xsl:if test="$count &lt;= @number-of-mdb">
            <xsl:call-template name="weblogic-enterprise-bean">
                <xsl:with-param name="count"><xsl:value-of select="$count"/></xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="loop-weblogic-enterprise-bean">
                <xsl:with-param name="count"><xsl:value-of select="$count + 1"/></xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="weblogic-enterprise-bean">
        <xsl:param name="count"/>
        <weblogic-enterprise-bean>
            <ejb-name>FORWARD_<xsl:value-of select="$count"/>_<xsl:value-of select="@forward-name"/></ejb-name>
            <message-driven-descriptor>
                <pool>
                    <max-beans-in-free-pool><xsl:value-of select="@max-beans-in-free-pool"/></max-beans-in-free-pool>
                    <initial-beans-in-free-pool><xsl:value-of select="@initial-beans-in-free-pool"/></initial-beans-in-free-pool>
                </pool>
                <destination-jndi-name><xsl:value-of select="@destination-jndi-name"/></destination-jndi-name>
                <connection-factory-jndi-name><xsl:value-of select="@connection-factory-jndi-name"/></connection-factory-jndi-name>
            </message-driven-descriptor>
            <xsl:if test="@trans-attribute='Required'">
                <transaction-descriptor>
                    <trans-timeout-seconds><xsl:value-of select="@trans-timeout-seconds"/></trans-timeout-seconds>
                </transaction-descriptor>
            </xsl:if>
            <jndi-name>gvesb/core/forward/<xsl:value-of select="@forward-name"/>/idx<xsl:value-of select="$count"/></jndi-name>
            <xsl:if test="@execution-queue">
              <dispatch-policy><xsl:value-of select="@execution-queue"/></dispatch-policy>
            </xsl:if>
        </weblogic-enterprise-bean>
    </xsl:template>

</xsl:stylesheet>
