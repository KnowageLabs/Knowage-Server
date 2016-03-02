<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>


<%@page import="java.util.Set"%>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="java.util.HashMap"%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<LINK rel='StyleSheet' href='<%=urlBuilder.getResourceLink(request, "css/analiticalmodel/portal_admin.css")%>' type='text/css' />

<%
SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute("JobManagementModule");
Map documentsExceeding = (Map) moduleResponse.getAttribute("EXCEEDING_CONFIGURATIONS");
Map backUrlPars = new HashMap();
backUrlPars.put("PAGE", "JobManagementPage");
backUrlPars.put("MESSAGEDET", SpagoBIConstants.RETURN_TO_ACTIVITY_DETAIL);
backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
String backUrl = urlBuilder.getUrl(request, backUrlPars);

Map saveUrlPars = new HashMap();
saveUrlPars.put("PAGE", "JobManagementPage");
saveUrlPars.put("MESSAGEDET", SpagoBIConstants.IGNORE_WARNING);
saveUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
String saveUrl = urlBuilder.getUrl(request, saveUrlPars);
%>

<table class='header-table-portlet-section'>
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section' style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "scheduler.manyExecutionWarning"  bundle="component_scheduler_messages"/>
		</td>
	</tr>
</table>

<div class="div_detail_area_forms" style="margin:10px;width:600px">
	<span class='portlet-form-field-label'>
	<%
	Set keys = documentsExceeding.keySet();
	Iterator it = keys.iterator();
	while (it.hasNext()) {
		String documentName = (String) it.next();
		Float combinations = (Float) documentsExceeding.get(documentName);
		String message = null;
		if (combinations.floatValue() != Float.POSITIVE_INFINITY) {
			message = msgBuilder.getMessage("scheduler.numberOfExecution", "component_scheduler_messages", request);
			message = message.replaceAll("%0", documentName);
			message = message.replaceAll("%1", combinations.toString());
		} else {
			message = msgBuilder.getMessage("scheduler.numberOfExecutionNotCalculable", "component_scheduler_messages", request);
			message = message.replaceAll("%0", documentName);
		}
		%>
		<%= message %><br/>
		<%
	}
	%>
	<br/>
	<spagobi:message key = "scheduler.manyExecutionConfirm"  bundle="component_scheduler_messages"/>
	</span>
	<div class='buttons' style='font-size:8pt;'>
	<ul>
		<li><a href='<%= saveUrl %>' class='button p_save_button'><b><b><b><spagobi:message key = "scheduler.saveButton"  bundle="component_scheduler_messages"/></b></b></b></a></li>
		<li><a href='<%= backUrl %>' class='button p_reset_button'><b><b><b><spagobi:message key = "scheduler.returnToActivityDetail"  bundle="component_scheduler_messages"/></b></b></b></a></li>
	</ul>
	</div>
	<div style="clear:both;"></div>
</div>

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
