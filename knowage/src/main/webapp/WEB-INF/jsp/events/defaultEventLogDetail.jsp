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


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page import="it.eng.spagobi.events.bo.EventLog,
                 it.eng.spagobi.commons.bo.Domain,
                 it.eng.spagobi.commons.utilities.GeneralUtilities,
                 it.eng.spago.navigation.LightNavigationManager" %>
<%@page import="it.eng.spago.util.JavaScript"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>


<%
	SourceBean moduleResponse = (SourceBean) aServiceResponse.getAttribute("DetailEventLogModule"); 
	EventLog event = (EventLog) moduleResponse.getAttribute("firedEvent");
	
	Map backUrlPars = new HashMap();
	backUrlPars.put("PAGE", "EVENTS_MONITOR_PAGE");
	backUrlPars.put("REFRESH", "TRUE");
	backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
	String backUrl = urlBuilder.getUrl(request, backUrlPars);
	
%>


<table class='header-table-portlet-section'>		
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section' 
		    style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "sbievents.detail.title" />
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a href='<%=backUrl%>'> 
      			<img class='header-button-image-portlet-section' title='<spagobi:message key = "sbievents.detail.backButton" />' 
      			      src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme)%>' alt='<spagobi:message key = "sbievents.detail.backButton" />' />
			</a>
		</td>
	</tr>
</table>

<div class="div_background_no_img">
	<div style="width:70%;" class="div_detail_area_forms">
		<table style="margin:10px;">
			<tr>
				<td class='portlet-form-field-label'><spagobi:message key = "sbievents.detail.id" /></td>
				<td class='portlet-section-subheader'><p style='margin:5px'><%=event.getId()%></p></td>
			</tr>
			<tr>
				<td class='portlet-form-field-label'><spagobi:message key = "sbievents.detail.date" /></td>
				<td class='portlet-section-subheader'><p style='margin:5px'><%=event.getDate().toString()%></p></td>
			</tr>
			<tr>
				<td class='portlet-form-field-label'><spagobi:message key = "sbievents.detail.user" /></td>
				<td class='portlet-section-subheader'><p style='margin:5px'><%=event.getUser()%></p></td>
			</tr>
			<tr>
				<td class='portlet-form-field-label'><spagobi:message key = "sbievents.detail.description" /></td>
				<%
				String description = event.getDesc();
				if (description != null) {
					description = description.replaceAll("&gt;", ">");
					description = description.replaceAll("&lt;", "<");
					description = description.replaceAll("&quot;", "\"");
					description = GeneralUtilities.replaceInternationalizedMessages(description);
				}
				%>
				<td class='portlet-section-subheader' style='text-align:left'><p style='margin:5px'><%=(description == null ? "" : description)%></p></td>
			</tr>
		</table>
	</div>
</div>
