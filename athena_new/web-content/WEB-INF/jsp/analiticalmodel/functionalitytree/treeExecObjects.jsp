<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
 

<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>


<%@ page import="it.eng.spagobi.commons.constants.ObjectsTreeConstants,
                 it.eng.spagobi.commons.constants.SpagoBIConstants,
                 it.eng.spagobi.analiticalmodel.document.service.DetailBIObjectModule,
                 it.eng.spago.navigation.LightNavigationManager" %>
<%@page import="org.safehaus.uuid.UUIDGenerator"%>
<%@page import="org.safehaus.uuid.UUID"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>


<%
	SourceBean moduleResponse = (SourceBean) aServiceResponse.getAttribute("TreeObjectsModule");
	
	String pageName = (String) aServiceRequest.getAttribute("PAGE");
	  
	Map viewListUrlPars = new HashMap();
	viewListUrlPars.put("PAGE", pageName);
	viewListUrlPars.put(SpagoBIConstants.OBJECTS_VIEW, SpagoBIConstants.VIEW_OBJECTS_AS_LIST);
	if(ChannelUtilities.isWebRunning()){
		viewListUrlPars.put(SpagoBIConstants.WEBMODE, "TRUE");
	}
	String viewListUrl = urlBuilder.getUrl(request, viewListUrlPars);
	

	Map refreshUrlPars = new HashMap();
	refreshUrlPars.put("PAGE", "MYFOLDERMANAGEMENTPAGE");
	String refreshUrl = urlBuilder.getUrl(request, refreshUrlPars);

	
    // identity string for object of the page
    UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
    UUID uuid = uuidGen.generateTimeBasedUUID();
    String requestIdentity = uuid.toString();
    requestIdentity = requestIdentity.replaceAll("-", "");
    String treeName = "treeExecObj" + requestIdentity;
%>


<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<table class='header-table-portlet-section'>
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section' style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "SBISet.exeObjects.titleTree" />
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a href='<%=viewListUrl%>'> 
      			<img class='header-button-image-portlet-section' 
				src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/listView.png", currTheme)%>' 
				title='<spagobi:message key = "SBISet.exeObjects.listViewButt" />' 
				alt='<spagobi:message key = "SBISet.exeObjects.listViewButt" />' /> 
			</a>		
		</td>

		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a style="text-decoration:none;" href='<%=refreshUrl%>'> 
				<img width="26px" height="26px"
					src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/updateState.png", currTheme)%>' 
					name='refresh' 
					alt='<spagobi:message key = "SBIExecution.refresh"/>' 
					title='<spagobi:message key = "SBIExecution.refresh"/>' /> 
			</a>	
		</td>

	</tr>
</table>


<div class="div_background">
	<spagobi:treeObjects moduleName="TreeObjectsModule" treeName="<%=treeName%>" 
	    				 htmlGeneratorClass="it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ExecTreeHtmlGenerator" />
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
</div>








