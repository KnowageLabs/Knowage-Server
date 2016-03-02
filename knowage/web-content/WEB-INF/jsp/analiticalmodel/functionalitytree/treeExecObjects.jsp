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








