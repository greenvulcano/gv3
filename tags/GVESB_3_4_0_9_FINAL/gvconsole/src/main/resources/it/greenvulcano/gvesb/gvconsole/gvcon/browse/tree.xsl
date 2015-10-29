<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:greenvulcano="it.greenvulcano.gvesb.gvcon.browse.Stylesheet"
>

    <!--===========================================================================================================
        Inizio albero.
    -->

    <xsl:template match="/GreenVulcano">
        <html>
            <head>
                <title>Configuration Browser - Tree</title>
                <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE"/> 
                <link rel="stylesheet" href="stile.css" type="text/css"/>
                <link rel="stylesheet" href="dtree/dtree.css" type="text/css"/>
                <script src="dtree/dtree.js" type="text/javascript"/>
            </head>
            <body>
                <script>

                    tree = new dTree('tree', 'dtree/');

                    tree.add(<xsl:value-of select="greenvulcano:id(/)"/>, -1, 'GreenVulcano');

                  <!--  <xsl:call-template name="groups"/> -->
                    <xsl:call-template name="schema-EB"/> 
                    <xsl:call-template name="systems"/>
                    <xsl:call-template name="services"/>
					
		    tree.closeAll();
                    document.write(tree);
                </script>
            </body>
        </html>
    </xsl:template>

	
<!--===========================================================================================================
        Link per schema GreenVulcano
    -->

    <xsl:template name="schema-EB">
	 <xsl:variable name="schemaId" select="greenvulcano:id()"/>

          tree.add(
                <xsl:value-of select="$schemaId"/>,
                <xsl:value-of select="greenvulcano:id(/)"/>,
                'SchemaGreenVulcano',
                '?cmd=schemaEB&amp;schemaEB=schemaEB',
                '',
                'Content',
                'browseConfiguration/images/system.gif',
                'browseConfiguration/images/system.gif'
            );

 </xsl:template>


    <!--===========================================================================================================
        Sotto-albero dei sistemi.
    -->

    <xsl:template name="systems">
        <xsl:variable name="root" select="Systems"/>
        <xsl:variable name="rootId" select="greenvulcano:id($root)"/>

        <!--
            Radice del sottoalbero dei sistemi
        -->
        tree.add(<xsl:value-of select="$rootId"/>, <xsl:value-of select="greenvulcano:id(/)"/>, 'Systems');

        <!--
            Per ogni sistema...
        -->
        <xsl:for-each select="Systems/System">
            <xsl:sort select="@id-system"/>
            <xsl:variable name="systemID" select="@id-system"/>
            <xsl:variable name="system" select="."/>
            <xsl:variable name="systemId" select="greenvulcano:id()"/>

            tree.add(
                <xsl:value-of select="$systemId"/>,
                <xsl:value-of select="$rootId"/>,
                '<xsl:value-of select="@id-system"/>',
                '?cmd=system&amp;system=<xsl:value-of select="@id-system"/>',
                '',
                'Content',
                'browseConfiguration/images/system.gif',
                'browseConfiguration/images/system.gif'
            );

            <!--
                Per ogni gruppo con un servizio a cui questo sistema partecipa...  
                .... al posto del gruppo bisogna mettere il canale
            -->
            
            <!-- canaliiiiii -->
            <xsl:for-each select="Channel">
                <xsl:variable name="channel" select="."/>
                <xsl:variable name="channelId" select="greenvulcano:id()"/>

                tree.add(
                    <xsl:value-of select="$channelId"/>,
                    <xsl:value-of select="$systemId"/>,
                    '<xsl:value-of select="@id-channel"/>',
	                '?cmd=channel&amp;channel=<xsl:value-of select="@id-channel"/>&amp;system=<xsl:value-of select="$systemID"/>',
	                '',
	                'Content',
	                'browseConfiguration/images/group.gif',
	                'browseConfiguration/images/group.gif'
                );
           
            
      <!--      
            
            <xsl:for-each select="/GreenVulcano/Groups/Group[
                    @id-group=/GreenVulcano/Services/Service[Clients/Client/@id-system=$system/@id-system
                        or Clients/Client/Operation/Participant/@id-system=$system/@id-system]/@group-name
                    ]">
                <xsl:variable name="group" select="."/>
                <xsl:variable name="groupId" select="greenvulcano:id()"/>

                tree.add(
                    <xsl:value-of select="$groupId"/>,
                    <xsl:value-of select="$systemId"/>,
                    '<xsl:value-of select="@id-group"/>',
	                '?cmd=group&amp;group=<xsl:value-of select="@id-group"/>',
	                '',
	                'Content',
	                'browseConfiguration/images/group.gif',
	                'browseConfiguration/images/group.gif'
                );

-->

                <!--
                    Per ogni servizio del gruppo a cui questo sistema partecipa...
                    .....per ogni servizio del canale a cui questo sistema appartiene
                -->
                <xsl:for-each select="/GreenVulcano/Services/Service[
                        (Clients/Client/Operation/Participant/@id-channel=$channel/@id-channel
                            and Clients/Client/Operation/Participant/@id-system=$system/@id-system)
                        ]">
                    <xsl:variable name="serviceId" select="greenvulcano:id()"/>

                    tree.add(
                        <xsl:value-of select="$serviceId"/>,
                        <xsl:value-of select="$channelId"/>,
                        '<xsl:value-of select="@id-service"/>',
                        '?cmd=service&amp;service=<xsl:value-of select="@id-service"/>',
                        '',
                        'Content',
                        'browseConfiguration/images/service.gif',
                        'browseConfiguration/images/service.gif'
                    );

                </xsl:for-each>
         
         <!--       
                 <xsl:for-each select="/GreenVulcano/Services/Service[
                        @group-name=$group/@id-group
                        and (Clients/Client/@id-system=$system/@id-system
                            or Clients/Client/Operation/Participant/@id-system=$system/@id-system)
                        ]">
                    <xsl:variable name="serviceId" select="greenvulcano:id()"/>

                    tree.add(
                        <xsl:value-of select="$serviceId"/>,
                        <xsl:value-of select="$groupId"/>,
                        '<xsl:value-of select="@id-service"/>',
                        '?cmd=service&amp;service=<xsl:value-of select="@id-service"/>',
                        '',
                        'Content',
                        'browseConfiguration/images/service.gif',
                        'browseConfiguration/images/service.gif'
                    );

                </xsl:for-each>

-->

            </xsl:for-each>
        </xsl:for-each>
    </xsl:template>


    <!--===========================================================================================================
        Sotto-albero dei gruppi.
    -->

    <xsl:template name="groups">
        <xsl:variable name="root" select="Groups"/>
        <xsl:variable name="rootId" select="greenvulcano:id(Groups)"/>

        <!--
            Radice del sottoalbero dei gruppi
        -->
        tree.add(<xsl:value-of select="$rootId"/>, <xsl:value-of select="greenvulcano:id(/)"/>, 'Groups');

        <!--
            Per ogni gruppo...
        -->
        <xsl:for-each select="Groups/Group">
            <xsl:sort select="@id-group"/>
            <xsl:variable name="group" select="."/>
            <xsl:variable name="groupId" select="greenvulcano:id()"/>

            tree.add(
                <xsl:value-of select="$groupId"/>,
                <xsl:value-of select="$rootId"/>,
                '<xsl:value-of select="@id-group"/>',
                '?cmd=group&amp;group=<xsl:value-of select="@id-group"/>',
                '',
                'Content',
                'browseConfiguration/images/group.gif',
                'browseConfiguration/images/group.gif'
            );

            <!--
                Per ogni servizio nel gruppo...
            -->
            <xsl:for-each select="/GreenVulcano/Services/Service[@group-name=$group/@id-group]">
                <xsl:sort select="@id-service"/>
                <xsl:variable name="service" select="."/>
                <xsl:variable name="serviceId" select="greenvulcano:id()"/>

                tree.add(
                    <xsl:value-of select="$serviceId"/>,
                    <xsl:value-of select="$groupId"/>,
                    '<xsl:value-of select="@id-service"/>',
                    '?cmd=service&amp;service=<xsl:value-of select="@id-service"/>',
                    '',
                    'Content',
                    'browseConfiguration/images/service.gif',
                    'browseConfiguration/images/service.gif'
                );

                <!--
                    Per ogni sistema client e server nel servizio...
                -->
                <xsl:for-each select="/GreenVulcano/Systems/System[
                        @id-system=$service/Clients/Client/@id-system
                        or @id-system=$service/Clients/Client/Operation/Participant/@id-system
                        ]">
                    <xsl:sort select="@id-system"/>
                    <xsl:variable name="systemId" select="greenvulcano:id()"/>
                    tree.add(
                        <xsl:value-of select="$systemId"/>,
                        <xsl:value-of select="$serviceId"/>,
                        '<xsl:value-of select="@id-system"/>',
	                    '?cmd=system&amp;system=<xsl:value-of select="@id-system"/>',
	                    '',
	                    'Content',
	                    'browseConfiguration/images/system.gif',
	                    'browseConfiguration/images/system.gif'
                    );
                </xsl:for-each>
            </xsl:for-each>

        </xsl:for-each>
    </xsl:template>


    <!--===========================================================================================================
        Sotto-albero dei servizi.
    -->

    <xsl:template name="services">
        <xsl:variable name="root" select="Services"/>
        <xsl:variable name="rootId" select="greenvulcano:id($root)"/>

        <!--
            Radice del sottoalbero dei servizi
        -->
        tree.add(<xsl:value-of select="$rootId"/>, <xsl:value-of select="greenvulcano:id(/)"/>, 'Services');

        <!--
            Per ogni servizio...
        -->
        <xsl:for-each select="Services/Service">
            <xsl:sort select="@id-service"/>
            <xsl:variable name="service" select="."/>
            <xsl:variable name="serviceId" select="greenvulcano:id()"/>

            tree.add(
                <xsl:value-of select="$serviceId"/>,
                <xsl:value-of select="$rootId"/>,
                '<xsl:value-of select="@id-service"/>',
                '?cmd=service&amp;service=<xsl:value-of select="@id-service"/>',
                '',
                'Content',
                'browseConfiguration/images/service.gif',
                'browseConfiguration/images/service.gif'
            );

            <!--
                Per ogni sistema client...
            -->
            <xsl:for-each select="/GreenVulcano/Systems/System[@id-system=$service/Clients/Client/@id-system]">
                <xsl:sort select="@id-system"/>
                <xsl:variable name="clientId" select="greenvulcano:id()"/>

                tree.add(
                    <xsl:value-of select="$clientId"/>,
                    <xsl:value-of select="$serviceId"/>,
                    '<xsl:value-of select="@id-system"/>',
                    '?cmd=system&amp;system=<xsl:value-of select="@id-system"/>',
                    '',
                    'Content',
                    'browseConfiguration/images/system.gif',
                    'browseConfiguration/images/system.gif'
                );

                <!--
                    Per ogni sistema partecipante...
                -->
                <xsl:for-each select="/GreenVulcano/Systems/System[
                        @id-system=$service/Clients/Client/Operation/Participant/@id-system
                        ]">
                    <xsl:sort select="@id-system"/>
                    <xsl:variable name="systemId" select="greenvulcano:id()"/>
    
                    tree.add(
                        <xsl:value-of select="$systemId"/>,
                        <xsl:value-of select="$clientId"/>,
                        '<xsl:value-of select="@id-system"/>',
                        '?cmd=system&amp;system=<xsl:value-of select="@id-system"/>',
                        '',
                        'Content',
                        'browseConfiguration/images/system.gif',
                        'browseConfiguration/images/system.gif'
                    );
    
                </xsl:for-each>
            </xsl:for-each>

        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>