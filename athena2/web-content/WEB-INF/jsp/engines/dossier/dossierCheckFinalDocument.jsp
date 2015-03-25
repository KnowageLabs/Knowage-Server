<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page import="it.eng.spago.navigation.LightNavigationManager,
				it.eng.spagobi.engines.dossier.constants.DossierConstants,
				it.eng.spagobi.commons.constants.SpagoBIConstants" %>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spagobi.engines.dossier.utils.DossierUtilities"%>

<%
	// RETRIVE ACTIVITY KEY	
	String activityKey = (String)aServiceResponse.getAttribute("ActivityKey");
    if(activityKey==null)
    	activityKey = (String)aServiceRequest.getAttribute("ActivityKey");
	// BUILT URL TO DOWNLOAD THE DOCUMENT
	String recoverUrl = DossierUtilities.getDossierServiceUrl(request) + "&" +
						DossierConstants.DOSSIER_SERVICE_TASK + "=" +
						DossierConstants.DOSSIER_SERVICE_TASK_DOWN_FINAL_DOC + "&" +
						SpagoBIConstants.ACTIVITYKEY + "=" + activityKey;
	
	// BUILT BACK URL 
	Map backUrlPars = new HashMap();
	backUrlPars.put("PAGE", "WorkflowToDoListPage");
	backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_RESET, "true");
	String backUrl = urlBuilder.getUrl(request, backUrlPars);
   	
	// BUILT APPROVE URL 
	Map checkedDocUrlPars = new HashMap();
	checkedDocUrlPars.put("PAGE", DossierConstants.DOSSIER_COLLABORATION_PAGE);
	checkedDocUrlPars.put("OPERATION", DossierConstants.OPERATION_APPROVE_PRESENTATION);
	checkedDocUrlPars.put(SpagoBIConstants.ACTIVITYKEY, activityKey);
	checkedDocUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
	String checkedDocUrl = urlBuilder.getUrl(request, checkedDocUrlPars);
	
%>

<table class='header-table-portlet-section'>
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section' style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "dossier.approval"  bundle="component_dossier_messages"/>
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a href='<%= backUrl %>'> 
      			<img class='header-button-image-portlet-section' 
      				 title='<spagobi:message key = "dossier.back" bundle="component_dossier_messages" />' 
      				 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/back.png", currTheme)%>' 
      				 alt='<spagobi:message key = "dossier.back"  bundle="component_dossier_messages"/>' />
			</a>
		</td>
	</tr>
</table>

<br/>

<form id="approveForm" action="<%=checkedDocUrl %>" method="POST"/>
<table style="width:100%">
	<tr>
		<td width="2%">&nbsp;</td>
		<td width="47%" style="padding:10px;" class="div_detail_area_forms">
		    <span class='portlet-form-field-label'>
				<spagobi:message key="dossier.downloadDescr"  bundle="component_dossier_messages"/>
			</span>
			&nbsp;&nbsp;&nbsp;&nbsp;
			<a href="<%=recoverUrl %>">
			<img class='header-button-image-portlet-section' 
	      				 title='<spagobi:message key = "dossier.download" bundle="component_dossier_messages" />' 
	      				 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/download32.png", currTheme)%>' 
	      				 alt='<spagobi:message key = "dossier.download"  bundle="component_dossier_messages"/>' />
	        </a>
		</td>
		<td width="2%">&nbsp;</td>
		<td width="47%" style="padding:10px;" class="div_detail_area_forms">
			
			<table>
				<tr>
					<td class='portlet-form-field-label'>
						<spagobi:message key="dossier.finalDocApproved"  bundle="component_dossier_messages"/>
					</td>
					<td class='portlet-form-field-label'>
						<input type="radio" name="approved" value="true" default>True<br>
					</td>
					<td class='portlet-form-field-label'>
						<input type="radio" name="approved" value="false">False<br>
					</td>
					<td class='portlet-form-field-label'>
						<input type="image" 
			       			   title='<spagobi:message key = "dossier.approvalBotton" bundle="component_dossier_messages" />' 
      		 	   			   src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/save32.png", currTheme)%>' 
      		 	  			   alt='<spagobi:message key = "dossier.approvalBotton"  bundle="component_dossier_messages"/>' />
					</td>
				</tr>
			</table>
			
		</td>
		<td width="2%">&nbsp;</td>
	</tr>
</table>
</form>
	

<br/>
<br/>

	















