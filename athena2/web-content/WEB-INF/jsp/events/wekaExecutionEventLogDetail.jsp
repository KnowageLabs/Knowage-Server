<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page import="it.eng.spagobi.commons.constants.SpagoBIConstants,
         		it.eng.spagobi.events.EventsManager,
         		it.eng.spagobi.events.bo.EventLog,
         		it.eng.spago.base.SourceBean,
         		java.util.List,
         		it.eng.spago.navigation.LightNavigationManager,
         		it.eng.spagobi.commons.bo.Subreport,
         		it.eng.spagobi.analiticalmodel.document.bo.BIObject,
         		it.eng.spagobi.commons.utilities.GeneralUtilities,
         		it.eng.spagobi.commons.constants.ObjectsTreeConstants"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>

<%
	SourceBean moduleResponse = (SourceBean) aServiceResponse.getAttribute("DetailEventLogModule"); 
	EventLog event = (EventLog) moduleResponse.getAttribute("firedEvent");
	BIObject biObject = (BIObject) moduleResponse.getAttribute("biobject");
	// if the process has ended the 'startEventIdStr' and 'result' variables are not null
	String startEventIdStr = (String) moduleResponse.getAttribute("startEventId");
	String result = (String) moduleResponse.getAttribute("operation-result");
	
	Map backUrlPars = new HashMap();
	backUrlPars.put("PAGE", "EVENTS_MONITOR_PAGE");
	backUrlPars.put("REFRESH", "TRUE");
	backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
	String backUrl = urlBuilder.getUrl(request, backUrlPars);
	
	Map backToListUrlPars = new HashMap();
	backToListUrlPars.put("PAGE", "EVENTS_MONITOR_PAGE");
	backToListUrlPars.put("REFRESH", "TRUE");
	backToListUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_RESET, "true");
	String backToListUrl = urlBuilder.getUrl(request, backToListUrlPars);
   	
%>


<table class='header-table-portlet-section'>		
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section' 
		    style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "sbievents.detail.title" />
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a href='<%= backToListUrl %>'> 
      			<img class='header-button-image-portlet-section' title='<spagobi:message key = "sbievents.detail.backToListButton" />' 
      				 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/listView.png", currTheme)%>' alt='<spagobi:message key = "sbievents.detail.backToListButton" />' />
			</a>
		</td>
		<td class='header-button-column-portlet-section'>
			<a href='<%= backUrl %>'> 
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
				<td class='portlet-form-field-label'><spagobi:message key = "weka.execution.documentDetails" /></td>
				<td class='portlet-section-subheader'>
					<p style='margin:5px'>
					<%=biObject.getLabel() + ": " + biObject.getName() + " [" + biObject.getDescription() + "]"%>
					</p>
				</td>
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
				<td class='portlet-section-subheader' style='text-align:left'>
					<p style='margin:5px'><%=(description == null ? "" : description)%>
					<%
					if (startEventIdStr != null) {
						
						Map startEventDetailUrlPars = new HashMap();
						startEventDetailUrlPars.put("PAGE", "DetailEventLogPage");
						startEventDetailUrlPars.put("MESSAGEDET", "DETAIL_SELECT");
						startEventDetailUrlPars.put("ID", startEventIdStr);
						String startEventDetailUrl = urlBuilder.getUrl(request, startEventDetailUrlPars);
						%>
						<spagobi:message key = "weka.execution.startEventId" /> <a href='<%= startEventDetailUrl %>'><%=startEventIdStr%></a>.						
						<%
					}
					%>
					</p>
				</td>
			</tr>
			<tr>
				<td class='portlet-form-field-label'><spagobi:message key = "weka.execution.relatedDocuments" /></td>
				<td class='portlet-section-subheader'>
					<p style='margin:5px'>
					<%
					List linkedBIObjects = (List) moduleResponse.getAttribute("linkedBIObjects");
					if (linkedBIObjects.size() == 0) {
						%>
						<spagobi:message key = "weka.execution.noRelatedDocuments" />
						<%
					} else {
						for (int i = 0; i < linkedBIObjects.size(); i++) {
							BIObject linkedObject = (BIObject) linkedBIObjects.get(i);
							if (startEventIdStr != null && result != null && result.equalsIgnoreCase("success")) {
								// if it is an end process event, shows the execution links to correlated documents
								
								Map linkedObjectExecutionUrlPars = new HashMap();
								linkedObjectExecutionUrlPars.put("PAGE", "ExecuteBIObjectPage");
								linkedObjectExecutionUrlPars.put("MESSAGEDET", "EXEC_PHASE_CREATE_PAGE");  
								linkedObjectExecutionUrlPars.put("OBJECT_ID", linkedObject.getId().toString());
								linkedObjectExecutionUrlPars.put("ACTOR", "USER_ACTOR");
								String linkedObjectExecutionUrl = urlBuilder.getUrl(request, linkedObjectExecutionUrlPars);
				   				%>
				   				<a href='<%=linkedObjectExecutionUrl %>'><%=linkedObject.getLabel() + ": " + linkedObject.getName() + " [" + linkedObject.getDescription() + "]"%></a>
				   				<br/>
				   				<%
							} else {
								// if it's a start process event or the process did not success, shows the correlated documents with no links for execution
								%>
								<%=linkedObject.getLabel() + ": " + linkedObject.getName() + " [" + linkedObject.getDescription() + "]"%>
								<br/>
								<%
							}
						}
					}
					%>					
					</p>
				</td>
			</tr>
			<tr>
				<td class='portlet-form-field-label'>Output File</td>
				<td class='portlet-section-subheader'>
					<p style='margin:5px'>
				<%
					String outputFile = (String) moduleResponse.getAttribute("operation-output");
					if(outputFile != null && startEventIdStr != null && result != null && result.equalsIgnoreCase("success")) {
					//String downloadUrl = "http://localhost:8080/SpagoBIWekaEngine/DownloadOutputFileServlet?outputFileName=" + outputFile;
					String downloadUrl = (String) moduleResponse.getAttribute("engineBaseUrl");
					downloadUrl += "/DownloadOutputFileServlet?outputFileName=" + outputFile;
				%>				
					<a href=<%=downloadUrl %> >	<%=outputFile %> </a>	
				<% 
					} else {
				%>
					Not defined
				<%} %>
				</td>
			</tr>
		</table>
	</div>
</div>