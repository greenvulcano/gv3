<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output omit-xml-declaration="no" method="xml" indent="yes" encoding="UTF-8"/>
    
    <xsl:template match="/service">
      <xsl:comment>
  ~ Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
  ~ reserved.
  ~ 
  ~ This file is part of GreenVulcano ESB.
  ~ 
  ~ GreenVulcano ESB is free software: you can redistribute it and/or modify it
  ~ under the terms of the GNU Lesser General Public License as published by the
  ~ Free Software Foundation, either version 3 of the License, or (at your
  ~ option) any later version.
  ~ 
  ~ GreenVulcano ESB is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  ~ FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  ~ for more details.
  ~ 
  ~ You should have received a copy of the GNU Lesser General Public License
  ~ along with GreenVulcano ESB. If not, see &lt;http://www.gnu.org/licenses/&gt;.
</xsl:comment>
      <service name="{@name}">
        <description>GreenVulcano ESB WebService - <xsl:value-of select="@name"/></description>
        <transports> 
        	<xsl:apply-templates mode="transport" select="bindinghttp"/>
        	<xsl:apply-templates mode="transport" select="bindingjms"/>
    	</transports>
        <xsl:if test="@useOriginalwsdl">
           <parameter name="useOriginalwsdl">true</parameter>
        </xsl:if>
        <xsl:apply-templates mode="params" select="bindinghttp"/>
        <xsl:apply-templates mode="params" select="bindingjms"/>
        <messageReceivers>
          <messageReceiver mep="http://www.w3.org/2004/08/wsdl/in-only"
            class="it.greenvulcano.gvesb.axis2.receivers.GVInOnlyMessageReceiver"/>
          <messageReceiver mep="http://www.w3.org/2004/08/wsdl/in-out"
            class="it.greenvulcano.gvesb.axis2.receivers.GVInOutMessageReceiver"/>
        </messageReceivers>

        <xsl:for-each select="modules/module">
          <module ref="{@name}"></module>
        </xsl:for-each>

        <xsl:for-each select="modules/module/policy">
          <xsl:copy-of select="*"/>
        </xsl:for-each>
        
        <xsl:for-each select="operations/operation">
          <operation name="{@name}" namespace="{@namespace}">
	        <xsl:for-each select="policy">
	          <xsl:copy-of select="*"/>
	        </xsl:for-each>
          </operation>
        </xsl:for-each>
      </service>
    </xsl:template>
    
    <xsl:template mode="transport" match="bindinghttp">
       <transport>http</transport>
    </xsl:template>
    
    <xsl:template mode="params" match="bindinghttp">
    </xsl:template>
    
    <xsl:template mode="transport" match="bindingjms">
       <transport>jms</transport>
    </xsl:template>
    
    <xsl:template mode="params" match="bindingjms">
   	   <parameter name="transport.jms.ConnectionFactory" locked="true"><xsl:value-of select="@connectionFactory"/></parameter>
       <parameter name="transport.jms.Destination" locked="true"><xsl:value-of select="@queue"/></parameter>
       <parameter name="transport.jms.DestinationType" locked="true"><xsl:value-of select="@destinationType"/></parameter>
       <xsl:if test="@replyDestination">
         <parameter name="transport.jms.ReplyDestination" locked="true"><xsl:value-of select="@replyDestination"/></parameter>
       </xsl:if>
       <xsl:if test="@contentType">
         <parameter name="transport.jms.ContentType">
          <rules>
           <jmsProperty><xsl:value-of select="@contentType"/></jmsProperty>
           <xsl:if test="@bytesMessage">
             <bytesMessage><xsl:value-of select="@bytesMessage"/></bytesMessage>
           </xsl:if>
           <xsl:if test="@textMessage">
             <textMessage><xsl:value-of select="@textMessage"/></textMessage>
           </xsl:if>
           <default>text/xml</default>
         </rules>
         </parameter>
       </xsl:if>
    </xsl:template>
</xsl:stylesheet>
