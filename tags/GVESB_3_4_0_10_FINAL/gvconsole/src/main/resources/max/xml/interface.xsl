<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:max="max.xml.Stylesheet">

	<xsl:output method="html" />

	<xsl:variable name="invoke">
		<xsl:value-of select="/interface/@invoke" />
	</xsl:variable>
	<xsl:variable name="invoke-visual">
		<xsl:value-of select="/interface/@invoke-visual" />
	</xsl:variable>
	<xsl:variable name="context">
		<xsl:value-of select="/interface/@context" />
	</xsl:variable>


	<xsl:template match="interface">
		<script language="JavaScript">
        <![CDATA[
            function popup(target, str)
            {
                var pageStr = "<html><head><title>?</title>"
                + "<link REL=stylesheet TYPE=text/css HREF="
                + "]]><xsl:value-of select="$context" /><![CDATA["
                + "/styles.css TITLE=normal page>"
                + "</head><body LINK=#013583 ALINK=#013583 TEXT=#000000 VLINK=#013583 BGCOLOR=#FFFFFF>"
                + "<img border=0 src="
                + "]]><xsl:value-of select="$context" />"
			+ "<![CDATA[/images/greenvulcano250x177.jpg><p><br>"
                + str
                + "</body></html>";

                var wnd = window.open("", target, "left=50,top=50,width=650,height=300,resizable,scrollbars", true);
                wnd.document.body.innerHTML = "";
                wnd.document.write(pageStr);
                wnd.focus();
            }

            needToConfirmAttr = false;
			needToConfirmText = false;
	
			window.onbeforeunload = confirmExit;
	
			function modificato(form) {

				form.applyBtn.disabled = false;
				form.resetBtn.disabled = false;
				if (form.name=='attrForm') 
					needToConfirmAttr = true;
				else if (form.name=='pcdataForm')
					needToConfirmText = true;
			}
	
			function cancella(form) {
				
				form.reset();
				form.applyBtn.disabled = true;
				form.resetBtn.disabled = true;
				if (form.name=='attrForm') 
					needToConfirmAttr = false;
				else if (form.name=='pcdataForm')
					needToConfirmText = false;
			}
  	
			function apply(form) {
			
				needToConfirmAttr = false;
				needToConfirmText = false;
				form.submit();
			}
	
			function confirmExit() {
				
				if (needToConfirmAttr || needToConfirmText)
					return "Attenzione: le modifiche apportate a questo elemento non verranno salvate. Continuare?"
			}
            ]]>
		</script>

		<br />
		<br />
		<xsl:if test="@anchor = 'yes'">
			<a name="anchor" />
		</xsl:if>
		<TABLE cellspacing="0" cellpadding="2">
			<TR class="top">
				<TD>
					<xsl:apply-templates select="tree" />
				</TD>
				<TD>
					<TABLE cellspacing="4" cellpadding="4" class="search ui-widget-header ui-corner-all">
						<xsl:if test="action">
							<TR class="top">
								<TD>
									<!-- <span class="toolbar ui-widget-header ui-corner-all"> -->
									<span id="menuAction">
										<xsl:apply-templates select="action" />
									</span>
									<span id="menuSettingAction">
										<xsl:call-template name="ui-settings-actions" />
									</span>
									<!-- </span> -->
								</TD>
							</TR>
						</xsl:if>
						<TR class="top">
							<xsl:if test="attributes">
								<TD class="ui-widget-header ui-corner-all">
									<xsl:apply-templates select="attributes" />
								</TD>
								<TD class="ui-widget-header ui-corner-all">
									<TABLE cellspacing="0" cellpadding="3">
										<xsl:apply-templates select="ancestors|element" />
									</TABLE>
								</TD>
							</xsl:if>
							<xsl:if test="not(attributes)">
								<TD colspan="2" class="ui-widget-header ui-corner-all">
									<TABLE cellspacing="0" cellpadding="3">
										<xsl:apply-templates select="ancestors|element" />
									</TABLE>
								</TD>
							</xsl:if>
						</TR>
						<xsl:if test="element/warning">
							<TR class="top">
								<TD colspan="2" class="ui-widget-header ui-corner-all" style="padding:20px;">
									<FONT class="error">
										<b>WARNING</b>
									</FONT>
									<xsl:for-each select="element/warning">
										<li>
											<xsl:value-of select="@warn" />
										</li>
									</xsl:for-each>
								</TD>
							</TR>
						</xsl:if>
						<xsl:if test="external-data">
							<TR class="top">
								<TD colspan="2">
									<xsl:apply-templates select="external-data/node()" />
								</TD>
							</TR>
						</xsl:if>
						<xsl:if test="operations">
							<TR>
								<TD colspan="2" class="width2 ui-widget-header ui-corner-all">
									<TABLE cellspacing="0" cellpadding="3" class="search" border="0">
										<xsl:apply-templates select="operations" />
									</TABLE>
								</TD>
							</TR>
						</xsl:if>
						<xsl:if test="graphics">
							<TR>
								<TD colspan="2" width="100%">
									<iframe
										src="{$invoke-visual}&amp;xsl-in={graphics/@xsl-in}&amp;xsl-out={graphics/@xsl-out}#anchor"
										width="100%" height="800">
										If you can see this, your browser doesn't
										understand IFRAME.
									</iframe>
								</TD>
							</TR>
						</xsl:if>
					</TABLE>
				</TD>
			</TR>
		</TABLE>

		<br />
		<br />

	</xsl:template>


	<xsl:template match="action">
		<a href="{$context}/MaxXMLServlet?operation=action&amp;key={@key}#anchor"
			title="{@text}">
			<xsl:if test="@target">
				<xsl:attribute name="target"><xsl:value-of select="@target" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="@label = 'Save'">
				<img src="{$context}/images/save.png" border="0" />
			</xsl:if>
			<xsl:if test="@label = 'SaveTemp'">
				<img src="{$context}/images/save.png" border="0" />
			</xsl:if>
			<xsl:if test="@label = 'SaveIbx'">
				<img src="{$context}/images/save.png" border="0" />
			</xsl:if>
			<xsl:if test="@label = 'SaveMap'">
				<img src="{$context}/images/save.png" border="0" />
			</xsl:if>
			<xsl:if test="@label = 'Discard'">
				<img src="{$context}/images/discard.png" border="0" />
			</xsl:if>
			<xsl:if test="@label = 'GoDic'">
				<img src="{$context}/images/dizionario.gif" border="0" />
			</xsl:if>
			<xsl:if test="@label = 'GoMO'">
				<img src="{$context}/images/schedaop.gif" border="0" />
			</xsl:if>
			<xsl:if test="@label = 'Search'">
				<img src="{$context}/images/search-new.png" border="0" />
			</xsl:if>
			<xsl:if test="@label = 'XQuery'">
				<img src="{$context}/images/xqSearch.png" border="0" />
			</xsl:if>
			<xsl:if test="@label = 'Download document'">
				<img src="{$context}/images/import.png" border="0" />
			</xsl:if>
			<xsl:if test="@label = 'View element'">
				<img src="{$context}/images/inspector.png" border="0" />
			</xsl:if>
			<xsl:if test="@label = 'View document'">
				<img src="{$context}/images/visualizza.png" border="0" />
			</xsl:if>
		</a>
	</xsl:template>


	<xsl:template name="ui-settings-actions">
		<xsl:if test="tree/@active = 'no'">
			<a href="{$invoke}&amp;opkey=0&amp;cmd=tree-on#anchor" title="Activate the tree">
				<img src="{$context}/images/tree-on.png" border="0" />
			</a>
		</xsl:if>
		<xsl:if test="tree/@active = 'yes'">
			<a href="{$invoke}&amp;opkey=0&amp;cmd=tree-off#anchor" title="Deactivate the tree">
				<img src="{$context}/images/tree-off.png" border="0" />
			</a>
		</xsl:if>

		<xsl:if test="@autocheck = 'no'">
			<a href="{$invoke}&amp;opkey=0&amp;cmd=check#anchor" title="Check document">
				<img src="{$context}/images/check.png" border="0" />
			</a>
			<a href="{$invoke}&amp;opkey=0&amp;cmd=autocheck-on#anchor" title="Enable autocheck">
				<img src="{$context}/images/autocheck-on.png" border="0" />
			</a>
		</xsl:if>
		<xsl:if test="@autocheck = 'yes'">
			<a href="{$invoke}&amp;opkey=0&amp;cmd=autocheck-off#anchor" title="Disable autocheck">
				<img src="{$context}/images/autocheck-off.png" border="0" />
			</a>
		</xsl:if>
		<xsl:if test="@graphic-mode = 'no'">
			<a href="{$invoke}&amp;opkey=0&amp;cmd=graphic-mode-on#anchor"
				title="Enable graphic mode">
				<img src="{$context}/images/designer_on.gif" border="0" />
			</a>
		</xsl:if>
		<xsl:if test="@graphic-mode = 'yes'">
			<a href="{$invoke}&amp;opkey=0&amp;cmd=graphic-mode-off#anchor"
				title="Enable text mode">
				<img src="{$context}/images/designer_off.gif" border="0" />
			</a>
		</xsl:if>
	</xsl:template>


	<xsl:template match="ancestors">
		<xsl:apply-templates />
	</xsl:template>


	<xsl:template match="ancestor">
		<TR class="top">
			<TD>
				<xsl:text> </xsl:text>
			</TD>
			<TD>
				<nobr>
					<a href="{$invoke}&amp;opkey={@key}#anchor" title="go to element &quot;{@element-name}&quot;">
						<b>
							<xsl:value-of select="@element-name" />
						</b>
					</a>
					<xsl:if test="@label">
						<xsl:text> - </xsl:text>
						<b>
							<xsl:value-of select="@label" />
						</b>
					</xsl:if>
				</nobr>
			</TD>
			<TD>
				<xsl:text> </xsl:text>
			</TD>
			<TD class="right">
				<xsl:text> </xsl:text>
			</TD>
		</TR>
	</xsl:template>


	<xsl:template match="attributes">
		<TABLE cellspacing="0" cellpadding="3">
			<form name="attrForm" action="{$invoke}#anchor" method="post">
				<xsl:apply-templates />
				<xsl:text> </xsl:text>
				<TR class="top">
					<TD colspan="5">
						<hr />
					</TD>
				</TR>
				<TR>
					<TD colspan="5" class="left">

						<xsl:if test="not(@readOnly)">
							<input type="reset" class="button" name="resetBtn" value=" Reset "
								disabled="true" onclick="cancella(this.form)" />
							<xsl:text> </xsl:text>
							<input type="submit" class="button" name="applyBtn" value=" Apply "
								disabled="true" onclick="apply(this.form)" />
							<input type="hidden" class="button" name="cmd" value="setAttributes" />
							<input type="hidden" class="button" name="opkey" value="dummy" />
						</xsl:if>
					</TD>
				</TR>
			</form>
		</TABLE>
	</xsl:template>


	<xsl:template match="attribute">
		<xsl:variable name="var">
			__max_popup_attr<xsl:value-of select="position()" />
		</xsl:variable>
		<TR class="top">
			<TD>
				<xsl:apply-templates select="remove-attribute" />
			</TD>
			<xsl:apply-templates select="choice-attribute" />
			<xsl:apply-templates select="edit-attribute" />
			<xsl:apply-templates select="view-attribute" />
			<xsl:apply-templates select="add-attribute" />
			<TD>
				<xsl:if test="description/@popup-style">
					<script>
						var <xsl:value-of select="$var" />='<xsl:value-of select="description/@popup-style" />';
					</script>
					<a title="help on '{@attribute-name}'" href="javascript:popup('__max_popup', {$var})">
						<b>
							<small>?</small>
						</b>
					</a>
				</xsl:if>
			</TD>
		</TR>
	</xsl:template>


	<xsl:template match="choice-attribute">
		<TD class="width">
			<nobr>
				<xsl:value-of select="../@attribute-name" />
				:
			</nobr>
		</TD>
		<TD>
			<xsl:text> </xsl:text>
		</TD>
		<TD class="width6">
			<select name="attr_{../@attribute-name}" onchange="modificato(this.form)">
				<option>
					<xsl:value-of select="../@attribute-value" />
				</option>
				<xsl:for-each select="choice[@value != ../../@attribute-value]">
					<option>
						<xsl:value-of select="@value" />
					</option>
				</xsl:for-each>
			</select>
		</TD>
	</xsl:template>


	<xsl:template match="edit-attribute">
		<TD class="width">
			<nobr>
				<xsl:value-of select="../@attribute-name" />
				:
			</nobr>
		</TD>
		<TD>
			<xsl:text> </xsl:text>
		</TD>
		<TD class="width6">
			<input type="text" class="input200" name="attr_{../@attribute-name}"
				value="{../@attribute-value}" onchange="modificato(this.form)" />
		</TD>
	</xsl:template>


	<xsl:template match="view-attribute">
		<TD class="width">
			<nobr>
				<xsl:value-of select="../@attribute-name" />
				:
			</nobr>
		</TD>
		<TD>
			<xsl:text> </xsl:text>
		</TD>
		<TD class="width6">
			<b>
				<xsl:value-of select="../@attribute-value" />
				<xsl:text />
			</b>
		</TD>
	</xsl:template>


	<xsl:template match="remove-attribute">
		<xsl:call-template name="attribute-link">
			<xsl:with-param name="testo">
				&lt;img border="0" src="
				<xsl:value-of select="$context" />/images/del.png"&gt;
			</xsl:with-param>
			<xsl:with-param name="popup">
				remove "
				<xsl:value-of select="../@attribute-name" />
				"
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>


	<xsl:template match="add-attribute">
		<TD>
			<xsl:text> </xsl:text>
		</TD>
		<TD>
			<xsl:text> </xsl:text>
		</TD>
		<TD class="right">
			<xsl:call-template name="attribute-link">
				<xsl:with-param name="testo">
					&lt;nobr&gt;
					<nobr>
						<xsl:value-of select="../@attribute-name" />
					</nobr>
					&lt;img border="0" src="
					<xsl:value-of select="$context" />/images/ins.png"&gt;
					&lt;/nobr&gt;
				</xsl:with-param>
				<xsl:with-param name="popup">
					insert "
					<xsl:value-of select="../@attribute-name" />
					"
				</xsl:with-param>
			</xsl:call-template>
		</TD>
	</xsl:template>


	<xsl:template match="element">
		<xsl:variable name="immagine">
			&lt;img border="0" src="
			<xsl:value-of select="$context" />/images/info.png"&gt;
		</xsl:variable>
		<TR class="top">
			<TD>
				<xsl:if test="description/@popup-style">
					<script>
						var __max_popup_element = '<xsl:value-of select="description/@popup-style" />';
					</script>
					<a title="help on '{@element-name}'" href="javascript:popup('__max_popup',__max_popup_element)">
						<xsl:value-of select="$immagine"
							disable-output-escaping="yes" />
					</a>
				</xsl:if>
			</TD>
			<TD>
				<h4>
					<xsl:value-of select="@element-name" />
				</h4>
			</TD>
			<TD>
				<xsl:text> </xsl:text>
			</TD>
			<TD class="right">
				<xsl:apply-templates select="/interface/undo" />
				<xsl:apply-templates select="/interface/redo" />
				<xsl:if test="/interface/@warnings = 'yes'">
					<a href="{$context}/def/xmleditor/xmlwarnings.jsp" title="go to the list of warnings for the whole document">
						<img src="{$context}/images/warnings.png" border="0" />
					</a>
				</xsl:if>
			</TD>
		</TR>
	</xsl:template>


	<xsl:template match="undo">
		<a href="{$invoke}&amp;opkey=0&amp;cmd=undo#anchor" title="undo {@text}">
			<img src="{$context}/images/undo.png" border="0" />
		</a>
	</xsl:template>


	<xsl:template match="redo">
		<a href="{$invoke}&amp;opkey=0&amp;cmd=redo#anchor" title="redo {@text}">
			<img src="{$context}/images/redo.png" border="0" />
		</a>
		<xsl:text> </xsl:text>
	</xsl:template>


	<xsl:template match="operations">
		<xsl:apply-templates />
	</xsl:template>


	<xsl:template match="row">
		<xsl:if test="position() mod 2 = 0">
			<TR valign="top" class="ui-widget-header">
				<xsl:call-template name="row2" />
			</TR>
		</xsl:if>
		<xsl:if test="position() mod 2 = 1">
			<TR valign="top">
				<xsl:call-template name="row2" />
			</TR>
		</xsl:if>
	</xsl:template>


	<xsl:template name="row2">
		<TD class="width1">  <!-- quiii -->
			<xsl:if test="@anchor = 'yes'">
				<a name="anchor" />
			</xsl:if>
			<xsl:if test="select">
				<xsl:apply-templates select="menu" />
			</xsl:if>
		</TD>
		<TD>
			<xsl:apply-templates select="select | edit" />
			<xsl:text> </xsl:text>
		</TD>
		<TD>
			<xsl:text> </xsl:text>
		</TD>
		<TD class="right">
			<xsl:if test="not(select)">
				<xsl:apply-templates select="menu" />
			</xsl:if>
		</TD>
	</xsl:template>


	<xsl:template match="select">
		<xsl:if test="@is-hidden='no'">
			<xsl:if test="@new-element">
				<a name="newElement" />
			</xsl:if>
			<b>
				<nobr>
					<a href="{$invoke}&amp;opkey={@key}#anchor" title="go to element &quot;{@element-name}&quot;">
						<xsl:value-of select="@element-name" />
					</a>
					<xsl:if test="@label">
						<xsl:text> </xsl:text>
						<b>
							<xsl:value-of select="@label" />
						</b>
					</xsl:if>
				</nobr>
			</b>
			<xsl:if test="@new-element = 'yes'">
				<img src="{$context}/images/new.png" />
			</xsl:if>
			<xsl:for-each select="warning">
				<a href="{$invoke}&amp;opkey={@key}#anchor" title="{@warn}" style="margin-left:5px;">
					<img src="{$context}/images/warn.png" border="0" />
				</a>
			</xsl:for-each>
			<xsl:for-each select="details">
				<blockquote>
					<xsl:value-of disable-output-escaping="yes" select="." />
					<xsl:text> </xsl:text>
				</blockquote>
			</xsl:for-each>
			<xsl:for-each select="external-data">
				<blockquote>
					<xsl:apply-templates select="node()" />
					<xsl:text> </xsl:text>
				</blockquote>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>


	<xsl:template match="edit">
		<b>
			<xsl:value-of select="/interface/element/@element-name" />
		</b>
		<form name="pcdataForm" action="{$invoke}#anchor" method="post">
			<table cellspacing="0" cellpadding="2">
				<tr>
					<td>
						<xsl:if test="not(@readOnly)">
							<input type="hidden" name="cmd" value="edit" />
							<input type="hidden" name="opkey" value="{@key}" />

							<xsl:if test="free-text">
								<xsl:text disable-output-escaping="yes">&lt;textarea onchange="modificato(this.form)" cols="60" rows="10" name="value"&gt;</xsl:text>
	                            <xsl:value-of select="free-text"/>
	                            <xsl:text disable-output-escaping="yes">&lt;/textarea&gt;</xsl:text>
	                        </xsl:if>
	
	                        <xsl:if test="choice-element">
	                            <select name="value" onchange="modificato(this.form)">
	                                <xsl:for-each select="choice-element/choice">
	                                    <option><xsl:value-of select="@value"/></option>
	                                </xsl:for-each>
	                            </select>
	                        </xsl:if>
                        </xsl:if>

						<xsl:if test="@readOnly">
	                        <xsl:if test="free-text">
								<xsl:if test="@isXML">
		                        	<pre><xsl:value-of select="free-text"/></pre>
		                        </xsl:if>
								<xsl:if test="not(@isXML)">
		                        	<xsl:value-of select="free-text"/>
		                        </xsl:if>
	                        </xsl:if>
	
	                        <xsl:if test="choice-element">
	                        	<pre><xsl:value-of select="choice-element/choice[1]/@value"/></pre>
	                        </xsl:if>
						</xsl:if>
                    </td>
                </tr>
                <tr>
                    <td align="right">
                    	<xsl:if test="not(@readOnly)">
	                        <input type="reset" name="resetBtn" disabled="true" value="  Reset  " onclick="cancella(this.form)"/>
	                        <input type="submit" name="applyBtn" disabled="true" value="  Apply  " onclick="apply(this.form)"/>
                        </xsl:if>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>


    <xsl:template match="delete">
        <small><nobr>
            <xsl:call-template name="operation-link">
                <xsl:with-param name="testo">&lt;img border="0" src="<xsl:value-of select="$context"/>/images/del.png"&gt;</xsl:with-param>
                <xsl:with-param name="popup">delete "<xsl:value-of select="@content-model"/>"</xsl:with-param>
            </xsl:call-template>
        </nobr></small>
    </xsl:template>


    <xsl:template match="change">
        <small><nobr>
            <xsl:call-template name="operation-link">
                <xsl:with-param name="testo">
                    &lt;nobr&gt;
                        <xsl:value-of select="substring(@content-model,1,20)"/>
                        &lt;img border="0" src="<xsl:value-of select="$context"/>/images/chg.png"&gt;
                    &lt;/nobr&gt;
                </xsl:with-param>
                <xsl:with-param name="popup">change to "<xsl:value-of select="@content-model"/>"</xsl:with-param>
            </xsl:call-template>
        </nobr></small>
        <xsl:if test="position() != last()">
            <br/>
        </xsl:if>
    </xsl:template>


    <xsl:template match="insert">
        <small><nobr>
            <xsl:call-template name="operation-link">
                <xsl:with-param name="testo">
                    &lt;nobr&gt;
                        <xsl:value-of select="substring(@content-model,1,20)"/>
                        &lt;img border="0" src="<xsl:value-of select="$context"/>/images/ins.png"&gt;
                    &lt;/nobr&gt;
                </xsl:with-param>
                <xsl:with-param name="popup">insert "<xsl:value-of select="@content-model"/>"</xsl:with-param>
            </xsl:call-template>
        </nobr></small>
        <xsl:if test="position() != last()">
            <br/>
        </xsl:if>
    </xsl:template>


    <xsl:template name="operation-link">
        <xsl:param name="key" select="@key"/>
        <xsl:param name="popup"><xsl:value-of select="@content-model"/></xsl:param>
        <xsl:param name="testo" select="''"/>
        <a href="{$invoke}&amp;opkey={$key}#anchor" title="{$popup}">
            <xsl:value-of disable-output-escaping="yes" select="$testo"/>
        </a>
    </xsl:template>


    <xsl:template name="attribute-link">
        <xsl:param name="key" select="@key"/>
        <xsl:param name="popup"><xsl:value-of select="../@attribute-name"/></xsl:param>
        <xsl:param name="testo" select="''"/>
        <nobr>
            <small>
                <a href="{$invoke}&amp;opkey={@key}#anchor" title="{$popup}">
                    <xsl:value-of disable-output-escaping="yes" select="$testo"/>
                </a>
            </small>
        </nobr>
    </xsl:template>


    <!--========================================================================
        tabelle
    =========================================================================-->

    <xsl:template match="table[not(trow)]">
        <!-- non visualizziamo le tabelle senza righe -->
    </xsl:template>


    <xsl:template match="table[trow]">
        <TR>
			<TD colspan="4">
				<TABLE cellspacing="0" cellpadding="1" class="ui-widget-header ui-corner-all">
					<TR>
						<TD>
							<b>
								<font>
									<xsl:value-of select="@description"/>
								</font>
							</b>
						</TD>
					</TR>
					<TR>
						<TD colspan="2">
							<TABLE cellspacing="0" cellpadding="3">
								<!-- testata della tabella -->
								<xsl:apply-templates select="theader"/>
								<!-- righe senza elementi (quindi con operazioni di inserimento) -->
								<xsl:apply-templates select="trow[not(select)]"/>
								<!-- righe con dati -->
								<xsl:apply-templates select="trow[select]">
									<xsl:sort select="tcell[1]/value[1]/@value"/>
								</xsl:apply-templates>
								<!-- righe senza elementi (quindi con operazioni di inserimento) -->
								<xsl:apply-templates select="trow[not(select)]"/>
							</TABLE>
						</TD>
					</TR>
				</TABLE>
			</TD>
		</TR>
    </xsl:template>


    <xsl:template match="theader">
        <TR valign="bottom" class="color1"> <!-- class="color2"  -->
			<TD class="width7">
				<!-- spazio per il menu' -->
			</TD>
			<TD class="width7">
				<!-- spazio per le operazioni di selezione -->
			</TD>
			<TD class="width1">
				<!-- spazio per i warnings -->
			</TD>
			<xsl:for-each select="hcell">
				<TD>
					<b>
						<xsl:value-of select="@label"/>
					</b>
				</TD>
			</xsl:for-each>
			<TD>
				<!-- spazio per il menu' di insert -->
			</TD>
		</TR>
    </xsl:template>


    <xsl:template match="trow">
        <TR class="color1"> <!-- bordo red -->
				<TD colspan="{sum(tcell/@colspan) + 4}" style="padding-top:0px; padding-bottom:0px"/>
			</TR>
			<TR class="top"> <!-- bgcolor="green" -->
			<TD width="10"> <!-- quii align="center" -->
				<xsl:if test="select">
					<xsl:apply-templates select="menu"/>
				</xsl:if>
			</TD>
			<TD class="center1" width="10"> <!--  quii align="center" -->
				<xsl:if test="select[@new-element='yes']">
					<a href="{$invoke}&amp;opkey={select/@key}#anchor" title="go to element &quot;{select/@element-name}&quot;"><img border="0" src="{$context}/images/selectnew.gif"/></a>
				</xsl:if>
				<xsl:if test="select[not(@new-element='yes')]">
					<a href="{$invoke}&amp;opkey={select/@key}#anchor" title="go to element &quot;{select/@element-name}&quot;"><img border="0" src="{$context}/images/select.png"/></a>
				</xsl:if>
			</TD>
			<TD class="left">
				<xsl:for-each select="select/warning">
					<a href="{$invoke}&amp;opkey={@key}#anchor" title="{@warn}"><img src="{$context}/images/warndot.gif" border="0"/></a>
					<xsl:text/>
				</xsl:for-each>
			</TD>
			<xsl:for-each select="tcell">
				<xsl:variable name="position" select="position()"/>
				<TD colspan="{@colspan}">
					<xsl:if test="(../@anchor = 'yes') and ($position = 1)">
						<a name="anchor"/>
					</xsl:if>
					<xsl:for-each select="value">
						<xsl:value-of select="@value"/>
						<br/>
					</xsl:for-each>
				</TD>
			</xsl:for-each>
			<TD class="right">
				<xsl:if test="not(select)">
					<xsl:apply-templates select="menu"/>
				</xsl:if>
			</TD>
		</TR>
    </xsl:template>


    <!--========================================================================
        dmenu.js
    =========================================================================-->

    <xsl:template match="menu[not(menu-item)]">
        <!-- Scartiamo i menu senza items -->
    </xsl:template>

    <!--
        Menu' con operazioni di insert
    -->
    <xsl:template match="menu[menu-item]">
        <!--
            Menu' insert
        -->
        
        <div class="contBtnMenu"><button class="btnMenu" >menu</button></div>   
        <xsl:element name="div">
        	<xsl:choose>
        		<xsl:when test="menu-item[@type = 'insert' or @type = 'insert-paste'] ">
        			<xsl:attribute name="class">pop popRight</xsl:attribute>
        		</xsl:when>
        		<xsl:otherwise>
        			<xsl:attribute name="class">pop</xsl:attribute>
        		</xsl:otherwise>
        	</xsl:choose>
        
        	<div class="contBtnMenu">
	        	<xsl:element name="button">
		        	<xsl:choose>
		        		<xsl:when test="menu-item[@type = 'insert' or @type = 'insert-paste'] ">
		        			<xsl:attribute name="class">btnMenuInRight</xsl:attribute>
		        		</xsl:when>
		        		<xsl:otherwise>
		        			<xsl:attribute name="class">btnMenuIn</xsl:attribute>
		        		</xsl:otherwise>
		        	</xsl:choose>
		        	<xsl:text>menu</xsl:text>
	        	</xsl:element>
        		<!-- <button class="btnMenuIn" >menu</button> -->
        	</div>
	        <ul class="popMenuList">
		        <xsl:for-each select="menu-item[@type = 'insert']">
		   			<li>
				   		<xsl:variable name="ahref"><xsl:value-of select="$invoke"/>&amp;opkey=<xsl:value-of select="@key"/>#anchor</xsl:variable>
				   		<xsl:variable name="desc"><xsl:value-of select="@description"/></xsl:variable>
			           	<a style="background: url('{$context}/images/ins.png') no-repeat left;" href="{$ahref}" title="{$desc}">
			           		<xsl:value-of select="@label"/>
			           	</a>
					</li>
				</xsl:for-each>
		        <xsl:for-each select="menu-item[@type = 'insert-paste']">
					<li>
				   		<xsl:variable name="ahref"><xsl:value-of select="$invoke"/>&amp;opkey=<xsl:value-of select="@key"/>#anchor</xsl:variable>
				   		<xsl:variable name="desc"><xsl:value-of select="@description"/></xsl:variable>
			           	<a style="background: url('{$context}/images/paste.png') no-repeat left;" href="{$ahref}" title="{$desc}">
							<xsl:value-of select="@label"/>
			           	</a>
					</li>
				</xsl:for-each>
        <!--Altri menu'-->
	        	<xsl:for-each select="menu-item[@type = 'change']">
	        		<li>
		        		<xsl:variable name="ahref"><xsl:value-of select="$invoke"/>&amp;opkey=<xsl:value-of select="@key"/>#anchor</xsl:variable>
		        		<xsl:variable name="desc"><xsl:value-of select="@description"/></xsl:variable>
		                <a style="background: url('{$context}/images/chg.png') no-repeat left;" href="{$ahref}" title="{$desc}">
		                	<xsl:value-of select="@label"/>
		                </a>
	            	</li>
	            </xsl:for-each>
		        <xsl:for-each select="menu-item[@type = 'delete']">
	            	<li>
			            <xsl:variable name="ahref"><xsl:value-of select="$invoke"/>&amp;opkey=<xsl:value-of select="@key"/>#anchor</xsl:variable>
			            <xsl:variable name="desc"><xsl:value-of select="@description"/></xsl:variable>
			            <a style="background: url('{$context}/images/del.png') no-repeat left;" href="{$ahref}" title="{$desc}">
			            	<xsl:value-of select="@label"/>
			            </a>
		        	</li>
		        </xsl:for-each>
	        	<xsl:for-each select="menu-item[@type = 'copy']">
		        	<li>
		        		<xsl:variable name="ahref"><xsl:value-of select="$invoke"/>&amp;opkey=<xsl:value-of select="@key"/>#anchor</xsl:variable>
			            <xsl:variable name="desc"><xsl:value-of select="@description"/></xsl:variable>
			            <a style="background: url('{$context}/images/copy.png') no-repeat left;" href="{$ahref}" title="{$desc}">
			            	<xsl:value-of select="@label"/>
			            </a>
		        	</li>
	            </xsl:for-each>
	        	<xsl:for-each select="menu-item[@type = 'cut']">
		        	<li>
		        		<xsl:variable name="ahref"><xsl:value-of select="$invoke"/>&amp;opkey=<xsl:value-of select="@key"/>#anchor</xsl:variable>
			            <xsl:variable name="desc"><xsl:value-of select="@description"/></xsl:variable>
			            <a style="background: url('{$context}/images/cut.png') no-repeat left;" href="{$ahref}" title="{$desc}">
			            	<xsl:value-of select="@label"/>
			            </a>
		        	</li>
	            </xsl:for-each>
	        	<xsl:for-each select="menu-item[@type = 'paste']">
			        <li>
		        		<xsl:variable name="ahref"><xsl:value-of select="$invoke"/>&amp;opkey=<xsl:value-of select="@key"/>#anchor</xsl:variable>
			            <xsl:variable name="desc"><xsl:value-of select="@description"/></xsl:variable>
			            <a style="background: url('{$context}/images/paste.png') no-repeat left;" href="{$ahref}" title="{$desc}">
			            	<xsl:value-of select="@label"/>
			            </a>
		        	</li>
	            </xsl:for-each>
            </ul>
        </xsl:element>
    </xsl:template>


    <!--========================================================================
        dtree.js
    =========================================================================-->

    <xsl:template match="tree">
        <xsl:if test="@active = 'yes'">
        	<script type="text/javascript">

        		tree = new dTree(
        		    '<xsl:value-of select="max:jsString(@name)"/>',
        		    '<xsl:value-of select="max:jsString(/interface/@context)"/>/dtree/'
        		);

                tree.config.inOrder = true;
                tree.config.useIcons = false;

                <xsl:apply-templates select="tree-node"/>

        		document.write(tree);

                <xsl:if test="@openTo">
                    tree.openTo(<xsl:value-of select="@openTo"/>, true);
                </xsl:if>

        	</script>
        </xsl:if>
    </xsl:template>


    <xsl:template match="tree-node">
        tree.add(<xsl:value-of select="@id"/>,
                 <xsl:value-of select="@pid"/>,
                 '<nobr><xsl:value-of select="max:jsString(@name)" disable-output-escaping="yes"/></nobr>',
                 <xsl:if test="@url">
                    '<xsl:value-of select="max:jsString(@url)"/>',
                 </xsl:if>
                 <xsl:if test="not(@url)">
                    '<xsl:value-of select="$invoke"/>&amp;opkey=<xsl:value-of select="@id"/>&amp;cmd=treeSelect#anchor',
                 </xsl:if>
                 '<xsl:value-of select="max:jsString(@title)"/>',
                 '<xsl:value-of select="max:jsString(@target)"/>',
                 '<xsl:value-of select="max:jsString(@icon)"/>',
                 '<xsl:value-of select="max:jsString(@iconOpen)"/>'
                <xsl:if test="@open">
                    , <xsl:value-of select="@open"/>
                </xsl:if>
        );
        <xsl:apply-templates select="tree-node"/>
    </xsl:template>

    <!--========================================================================
        DOCUMENT SPECIFIC TRANSFORMATIONS
        Following templates applies only to external data of specific documents.
    =========================================================================-->

    <xsl:template match="test-external-data">
   <TABLE cellspacing="0" cellpadding="0">
			<TR>
				<TD>
					<b>Test external data</b>
				</TD>
			</TR>
			<TR>
				<TD>
					<b>Element:</b>
				</TD>
				<TD>
					<b>
						<FONT class="error">
							<xsl:value-of select="@element-name"/>
						</FONT>
					</b>
				</TD>
			</TR>
			<TR>
				<TD>
					<b>Parameter:</b>
				</TD>
				<TD>
					<b>
						<xsl:value-of select="."/>
					</b>
				</TD>
			</TR>
		</TABLE>
     </xsl:template>

</xsl:stylesheet>
