<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


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