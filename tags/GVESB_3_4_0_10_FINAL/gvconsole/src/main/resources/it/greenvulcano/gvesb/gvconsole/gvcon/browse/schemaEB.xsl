<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:greenvulcano="it.greenvulcano.gvesb.gvcon.browse.Stylesheet"
    xmlns:gvcon="it.greenvulcano.gvesb.gvcon.jgraph.Stylesheet"
>
    <xsl:param name="serverpath"/>
    
    <!--===========================================================================================================
        Inizio.
    -->

    <xsl:template match="/GreenVulcano">
        <html>
            <head>            	
                <title>GreenVulcano Schema </title>
                <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE"/> 
                <link rel="stylesheet" href="browseConfiguration/stile.css" type="text/css"/>
            </head>
            <body>
            	<font class="fieldlabel">
               		Di seguito uno schema che mostra i sistemi client di front-end abilitati ad invocare GreenVulcano ed i sistemi di back-end a cui GreenVulcano si collega.<br/>
		 <br/> <br/>  <br/>
		</font>
              <!--img src="{gvcon:generateHttpImage($serverpath)}"/-->	
              
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
