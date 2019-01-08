<%@ page isELIgnored="false" %>

	<%@ page import="java.util.*" %>
		<%@ page import="org.apache.struts.util.LabelValueBean" %>

			<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
				<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
					<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
						<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
							<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
								<%@ taglib uri="/birt.tld" prefix="birt" %>

									<%
  session.removeAttribute("currentMenu");
  session.setAttribute("currentMenu", "savepoint");
%>
										<%@ include file="../head.jspf" %>

											<style type="text/css">
												.sv-list {}
												
												.sv-list-H {
													background-color: lightgrey;
												}
												/*th { border: 1px solid #777; border-collapse: collapse; white-space:nowrap;}*/
												
												.sv-list td {
													line-height: 4px;
												}
												
												.sv-list td {
													border: 1px solid #777;
													border-collapse: collapse;
													white-space: nowrap;
												}
												
												.tdC_sel {
													background-color: #FFFF33;
												}
												
												.tablescroll_head {
													margin-left: 0px;
												}
												
												.SAVED {}
												
												.RUNNING {
													background-color: #FF3333;
												}
												
												.Infos {}
												
												.Params {}
											</style>


											<script language="JavaScript">
												var SavePoint = {};
												var contextRoot = '<%=contextRoot%>';
												var btnListSavePointL = "<bean:message key='savepoint.listSavePoint'/>";
												var btnRecoverSavePointL = "<bean:message key='savepoint.recoverSavePoint'/>";
												var btnDeleteSavePointL = "<bean:message key='savepoint.deleteSavePoint'/>";

												function checkForm(form) {
													return true;
												}

												function idChanged() {
													listSavePoint();
												}

												function serviceChanged() {
													listSavePoint();
												}

												function listSavePoint() {
													$('#savepointDIV').html('');
													$.ajax({
														url: contextRoot + '/savepoint/HandleSavePointAction.do?skipValidation=true&methodToCall=' + btnListSavePointL,
														cache: false,
														type: 'POST',
														data: ({
															id: $('#id').val(),
															service: $('#service').val(),
															date: $('#date').val()
														}),
														success: function (data) {
															SavePoint = data;
															if (data.message != 'OK') {
																alert(data.message);
															}
															redrawList();
														},
														error: function (request, textStatus) {
															alert("Error retrieving SavePoint list: " + textStatus);
														},
														dataType: 'json',
														async: true
													});
												}

												function redrawList() {
													$('#savepointDIV').html('');
													$('#btnRecoverSavePoint').attr('disabled', true);
													$('#btnDeleteSavePoint').attr('disabled', true);

													var templateList = $('#template-list').html();
													var node = $(templateList).bindTo(SavePoint, {
														root: 'SavePoint',
														fill: 'false',
														appendTo: '#savepointDIV'
													});
													$('#savepointDIV table tbody tr').each(function () {
														//this.onmouseover = function () { $(this).find('td').darking('backgroundColor'); };
														//this.onmouseout = function () { $(this).find('td').darking('backgroundColor', true); };
														this.onclick = function () {
															$('#savepointDIV table tbody tr td.tdC_sel').removeClass('tdC_sel');
															$(this).find('td').addClass('tdC_sel');

															$('#rec_id').val($(this).find("td input[name='rec_id']").val());
															var valid = $(this).find("input[name='state']").val() == 'SAVED';
															$('#btnRecoverSavePoint').attr('disabled', !valid);
															$('#btnDeleteSavePoint').attr('disabled', !valid);
														};
														$(this).find('img').each(function () {
															$(this).click(function () {
																showSavePointInfo(SavePoint.savepoint[$(this).parent().parent().attr('idx')]);
															});
															$(this).mouseover(function () {
																$(this).css('cursor', 'hand');
															});
															$(this).mouseout(function () {
																$(this).css('cursor', 'pointer');
															});
														});
													});
													$('#savepointDIV table.sv-list').tableScroll({
														height: 300,
														containerClass: 'sv-list'
													});
												}

												function recoverSavePoint() {
													$.ajax({
														url: contextRoot + '/savepoint/HandleSavePointAction.do?skipValidation=true&methodToCall=' + btnRecoverSavePointL,
														cache: false,
														type: 'POST',
														data: ({
															rec_id: $('#rec_id').val()
														}),
														success: function (data) {
															alert("Recovering SavePoint: " + data.message);
															listSavePoint();
														},
														error: function (request, textStatus) {
															alert("Error retrieving SavePoint list: " + textStatus);
														},
														dataType: 'json',
														async: true
													});
												}

												function deleteSavePoint() {
													$.ajax({
														url: contextRoot + '/savepoint/HandleSavePointAction.do?skipValidation=true&methodToCall=' + btnDeleteSavePointL,
														cache: false,
														type: 'POST',
														data: ({
															rec_id: $('#rec_id').val()
														}),
														success: function (data) {
															alert("Deleting SavePoint: " + data.message);
															listSavePoint();
														},
														error: function (request, textStatus) {
															alert("Error deleting SavePoint: " + textStatus);
														},
														dataType: 'json',
														async: true
													});
												}


												function showSavePointInfo(sp) {
													var div = $('#InfoPopup');

													var buttons = {};
													buttons["Close"] = function () {
														$('#InfoPopup').dialog('close');
													};
													div.dialog('option', "buttons", buttons);

													div.dialog('option', "title", sp.server + '#' + sp.service + '#' + sp.id);

													var html = '<tr><td class="name">ID</td><td>' + sp.id + '</td></tr>' +
														'<tr><td class="name">SYSTEM</td><td>' + sp.system + '</td></tr>' +
														'<tr><td class="name">SERVICE</td><td>' + sp.service + '</td></tr>' +
														'<tr><td class="name">OPERATION</td><td>' + sp.operation + '</td></tr>' +
														'<tr><td class="name">SP NODE</td><td>' + sp.recNode + '</td></tr>';
													div.find('table.Infos tbody').html('').append(html);

													html = '';
													var props = sp.detail;
													for (var i in props) {
														var item = props[i];
														html += '<tr><td class="name">' + item.name + '</td><td>' + item.value + '</td></tr>';
													}
													div.find('table.Params tbody').html('').append(html);

													div.dialog('open');
												}

												$(document).ready(function () {
													$.fn.bindTo.SavePoint = function (template, data, idx) {
														template = template.replace(/\{idx\}/g, idx);
														return template;
													}

													$('#InfoPopup').dialog({
														autoOpen: false,
														width: 400,
														height: 350,
														modal: true
													});

													$(document).oneTime(500, listSavePoint);
												});
											</script>

											<div class="titleSection">
												<h1>Save Point</h1>
											</div>
											<div class="ui-widget-header">
												<table>
													<tr>
														<td>
															<div id="filterDIV">
																<html:form action="/savepoint/HandleSavePointAction" onsubmit="return checkForm(this);">
																	<html:hidden styleId="skipValidation" property="skipValidation" value="true" />
																	<html:hidden styleId="methodToCall" property="methodToCall" value="" />
																	<html:hidden styleId="rec_id" property="rec_id" />
																	<table cellspacing=0 cellpadding=2>
																		<tr>
																			<th bgcolor="lightgrey" nowrap>Services ID</th>
																			<td valign="top" nowrap>
																				<html:select styleId="id" property="id" onchange="idChanged()">
																					<html:options collection="listID" property="value" labelProperty="label" />
																				</html:select>
																			</td>
																			<th bgcolor="lightgrey">Services </th>
																			<td valign="top" nowrap>
																				<html:select styleId="service" property="service" onchange="serviceChanged()">
																					<html:options collection="listSVC" property="value" labelProperty="label" />
																				</html:select>
																			</td>
																		</tr>
																	</table>
																</html:form>
															</div>
														</td>
													</tr>
													<tr>
														<td>
															<div id="savepointDIV" style="width: auto; heigth: 300px;border:2px solid #A5B5B5;" class="sv-list">
															</div>
															<br/>
															<input id="btnRecoverSavePoint" onclick="recoverSavePoint()" type="button" disabled value='<bean:message key="savepoint.recoverSavePoint"/>' />
															<input id="btnListSavePoint" onclick="listSavePoint()" type="button" value='<bean:message key="savepoint.listSavePoint"/>' />
															<input id="btnDeleteSavePoint" onclick="deleteSavePoint()" type="button" disabled value='<bean:message key="savepoint.deleteSavePoint"/>' />
														</td>
													</tr>
												</table>
											</div>
											<div id="InfoPopup" class="dialog">
												<table class="Infos">
													<tbody></tbody>
												</table>
												<table class="Params">
													<h5>Properties</h5>
													<tbody></tbody>
												</table>
											</div>

											<div id='template-list' style="display:none;">
												<div class="content">
													<table class="sv-list">
														<thead>
															<tr>
																<th class="sv-list-H">&nbsp;</th>
																<th class="sv-list-H">ID</th>
																<th class="sv-list-H">System</th>
																<th class="sv-list-H">Service</th>
																<th class="sv-list-H">Operation</th>
																<th class="sv-list-H">Recovery Node</th>
																<th class="sv-list-H">Creation Date</th>
																<th class="sv-list-H">Last Update</th>
																<th class="sv-list-H">Server</th>
															</tr>
														</thead>
														<tbody>
															<!--SavePoint-->
															<!--savepoint-->
															<!--action:SavePoint-->
															<tr idx="{idx}">
																<td class="{state}">
																	<img src="<%=contextRoot%>/images/info16x16.png" alt="Info" />
																	<input type="hidden" name="rec_id" value="{rec_id}" />
																	<input type="hidden" name="state" value="{state}" />
																</td>
																<td class="{state}">
																	<p>{id}</p>
																</td>
																<td class="{state}">
																	<p>{system}</p>
																</td>
																<td class="{state}">
																	<p>{service}</p>
																</td>
																<td class="{state}">
																	<p>{operation}</p>
																</td>
																<td class="{state}">
																	<p>{recNode}</p>
																</td>
																<td class="{state}">
																	<p>{creation}</p>
																</td>
																<td class="{state}">
																	<p>{lastUpdate}</p>
																</td>
																<td class="{state}">
																	<p>{server}</p>
																</td>
															</tr>
															<!--savepoint-->
															<!--SavePoint-->
														</tbody>
													</table>
												</div>
											</div>

											<%@ include file="../end.jspf" %>