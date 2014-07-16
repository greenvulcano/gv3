<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
>
    <xsl:param name="service">SERVICE</xsl:param>
    <xsl:param name="system">SYSTEM</xsl:param>
    <xsl:param name="operation">Request</xsl:param>

    <xsl:variable name="systemServiceNode" select="/GVServices/Services/Service[@id-service=$service]/Clients/Client[@id-system=$system]"/>
    <xsl:variable name="operationNode" select="$systemServiceNode/Operation[(@name=$operation) or (@forward-name=$operation)]"/>
    <xsl:variable name="templateInstanceNode" select="$operationNode/FlowTemplateInstance"/>
    <xsl:variable name="templateNode" select="/GreenVulcano/FlowTemplates/FlowSchema[@name=$templateInstanceNode/@schema]/FlowTemplate[@name=$templateInstanceNode/@name]"/>

    <!--===========================================================================================================
        Inizio.
    -->
    <xsl:template match="/">
        <xsl:apply-templates select="$templateNode"/>
    </xsl:template>

	<xsl:template match="FlowTemplate">

        <graph>
            <xsl:apply-templates select="*[@type='flow-node']"/>
        </graph>

 	</xsl:template>

    <xsl:template match="OperationNode">
        <node id="{@id}">
            <attr name="Label">
                <string><xsl:value-of select="@op-type"/></string>
            </attr>
        </node>
        <xsl:call-template name="manage-inout-services"/>
        <edge id="edge-{@id}">
            <xsl:attribute name="from"><xsl:apply-templates select="." mode="fromNode"/></xsl:attribute>
            <xsl:attribute name="to"><xsl:apply-templates select="$templateNode/*[@id=current()/@next-node-id]" mode="toNode"/></xsl:attribute>
            <attr name="Label">
                <string>success</string>
            </attr>
        </edge>
    </xsl:template>

    <xsl:template match="CheckNode">
        <node id="{@id}">
            <attr name="Label">
                <string><xsl:value-of select="@op-type"/></string>
            </attr>
        </node>
        <edge id="edge-ex-{@id}" from="{@id}">
            <xsl:attribute name="to"><xsl:apply-templates select="$templateNode/*[@id=current()/@on-exception-id]" mode="toNode"/></xsl:attribute>
            <attr name="Label">
                <string>error</string>
            </attr>
        </edge>
        <xsl:if test="@default-id">
            <edge id="edge-{@id}" from="{@id}">
                <xsl:attribute name="to"><xsl:apply-templates select="$templateNode/*[@id=current()/@default-id]" mode="toNode"/></xsl:attribute>
                <attr name="Label">
                    <string>success</string>
                </attr>
            </edge>
        </xsl:if>
        <xsl:apply-templates select="Routing"/>
    </xsl:template>

    <xsl:template match="Routing">
        <edge id="edge-{parent::CheckNode/@id}" from="{parent::CheckNode/@id}">
            <xsl:attribute name="to"><xsl:apply-templates select="$templateNode/*[@id=current()/@next-node-id]" mode="toNode"/></xsl:attribute>
            <attr name="Label">
                <string><xsl:value-of select="@condition"/></string>
            </attr>
        </edge>
    </xsl:template>

    <xsl:template match="ChangeGVBufferNode">
        <node id="{@id}">
            <attr name="Label">
                <string><xsl:value-of select="@op-type"/></string>
            </attr>
        </node>
        <edge id="edge-{@id}" from="{@id}">
            <xsl:attribute name="to"><xsl:apply-templates select="$templateNode/*[@id=current()/@next-node-id]" mode="toNode"/></xsl:attribute>
            <attr name="Label">
                <string>success</string>
            </attr>
        </edge>
    </xsl:template>

    <xsl:template match="WaitNode">
        <node id="{@id}">
            <attr name="Label">
                <string><xsl:value-of select="@op-type"/></string>
            </attr>
        </node>
        <edge id="edge-{@id}" from="{@id}">
            <xsl:attribute name="to"><xsl:apply-templates select="$templateNode/*[@id=current()/@next-node-id]" mode="toNode"/></xsl:attribute>
            <attr name="Label">
                <string>success</string>
            </attr>
        </edge>
    </xsl:template>

    <xsl:template match="NotificationNode">
        <node id="{@id}">
            <attr name="Label">
                <string><xsl:value-of select="@op-type"/></string>
            </attr>
        </node>
        <edge id="edge-{@id}" from="{@id}">
            <xsl:attribute name="to"><xsl:apply-templates select="$templateNode/*[@id=current()/@next-node-id]" mode="toNode"/></xsl:attribute>
            <attr name="Label">
                <string>success</string>
            </attr>
        </edge>
    </xsl:template>

    <xsl:template match="EndNode">
        <node id="{@id}">
            <attr name="Label">
                <string><xsl:value-of select="@op-type"/></string>
            </attr>
        </node>
    </xsl:template>


    <xsl:template match="OperationNode" mode="toNode">
        <xsl:if test="$templateInstanceNode/OperationAssignment[@id=current()/@id]/InputServices">
            <xsl:value-of select="concat('input-service-', @id, '-1')"/>
        </xsl:if>
        <xsl:if test="not($templateInstanceNode/OperationAssignment[@id=current()/@id]/InputServices)">
            <xsl:value-of select="@id"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="OperationNode" mode="fromNode">
        <xsl:if test="$templateInstanceNode/OperationAssignment[@id=current()/@id]/OutputServices">
            <xsl:value-of select="concat('output-service-', @id, '-', count($templateInstanceNode/OperationAssignment[@id=current()/@id]/OutputServices/*[@type='service']))"/>
        </xsl:if>
        <xsl:if test="not($templateInstanceNode/OperationAssignment[@id=current()/@id]/OutputServices)">
            <xsl:value-of select="@id"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="CheckNode" mode="toNode">
        <xsl:value-of select="@id"/>
    </xsl:template>

    <xsl:template match="ChangeGVBufferNode" mode="toNode">
        <xsl:value-of select="@id"/>
    </xsl:template>

    <xsl:template match="WaitNode" mode="toNode">
        <xsl:value-of select="@id"/>
    </xsl:template>

    <xsl:template match="NotificationNode" mode="toNode">
        <xsl:value-of select="@id"/>
    </xsl:template>

    <xsl:template match="EndNode" mode="toNode">
        <xsl:value-of select="@id"/>
    </xsl:template>

    <xsl:template name="manage-inout-services">
        <xsl:variable name="id" select="@id"/>

        <xsl:for-each select="$templateInstanceNode/OperationAssignment[@id=$id]/InputServices/*[@type='service']">
            <node id="input-service-{$id}-{position()}">
                <attr name="Label">
                    <string><xsl:value-of select="*[@type='call']/@name"/></string>
                </attr>
            </node>
            <xsl:if test="position() > 1">
                <edge id="edge-is-{$id}-{position()}" from="input-service-{$id}-{position() - 1}" to="input-service-{$id}-{position()}">
                    <attr name="Label">
                        <string/>
                    </attr>
                </edge>
            </xsl:if>
            <xsl:if test="position() = last()">
                <edge id="edge-is-{$id}-{position()}" from="input-service-{$id}-{position()}" to="{$id}">
                    <attr name="Label">
                        <string/>
                    </attr>
                </edge>
            </xsl:if>
        </xsl:for-each>

        <xsl:for-each select="$templateInstanceNode/OperationAssignment[@id=$id]/OutputServices/*[@type='service']">
            <node id="output-service-{$id}-{position()}">
                <attr name="Label">
                    <string><xsl:value-of select="*[@type='call']/@name"/></string>
                </attr>
            </node>
            <xsl:if test="position() > 1">
                <edge id="edge-is-{$id}-{position()}" from="output-service-{$id}-{position() - 1}" to="output-service-{$id}-{position()}">
                    <attr name="Label">
                        <string/>
                    </attr>
                </edge>
            </xsl:if>
            <xsl:if test="position() = 1">
                <edge id="edge-is-{$id}-{position()}" from="{$id}" to="output-service-{$id}-{position()}">
                    <attr name="Label">
                        <string/>
                    </attr>
                </edge>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
