<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         import="it.eng.spagobi.commons.constants.SpagoBIConstants,
         		 it.eng.spago.configuration.ConfigSingleton,
                 it.eng.spago.base.SourceBean,
                 it.eng.spago.security.IEngUserProfile" %>

<%@ taglib uri='http://java.sun.com/portlet' prefix='portlet'%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>


<portlet:defineObjects/>


<div class="div_background">
    <br/>	
	<table>
		<% if (userProfile.isAbleToExecuteAction(SpagoBIConstants.ENGINES_MANAGEMENT)) {%>
			<tr class="portlet-font">
				<td width="100" align="center">
					<img src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/engineAdministrationIcon.png", currTheme)%>' />
				</td>
				<td width="20">
					&nbsp;
				</td>
				<td vAlign="middle">
				    <br/> 
					<a href='<portlet:actionURL><portlet:param name="PAGE" value="ListEnginesPage"/></portlet:actionURL>' 
						class="link_main_menu" >
					 	<spagobi:message key="SBISet.linkEngConf" />
					</a>
				</td>
			</tr>
		<%} %>
		<% if (userProfile.isAbleToExecuteAction(SpagoBIConstants.DATASOURCE_MANAGEMENT)) {%>		
			<tr class="portlet-font">
					<td width="100" align="center">
						<img src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/tools/datasource/datasource_2.png", currTheme)%>' />
					</td>
					<td width="20">
						&nbsp;
					</td>
					<td vAlign="middle">
					    <br/> 
						<a href='<portlet:actionURL><portlet:param name="PAGE" value="ListDataSourcePage"/></portlet:actionURL>' 
							class="link_main_menu" >
						 	<spagobi:message key="SBISet.linkDsConf" />
						</a>
					</td>
			</tr>
		<%} %>
		<% if (userProfile.isAbleToExecuteAction(SpagoBIConstants.DATASET_MANAGEMENT)) {%>
			<tr class="portlet-font">
				<td width="100" align="center">
					<img src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/tools/dataset/img_menu.png", currTheme)%>' />
				</td>
				<td width="20">
					&nbsp;
				</td>
				<td vAlign="middle">
				    <br/> 
					<a href='<portlet:actionURL><portlet:param name="PAGE" value="ListDatasetPage"/></portlet:actionURL>' 
						class="link_main_menu" >
					 	<spagobi:message key="SBISet.linkDSetConf" />
					</a>
				</td>
			</tr>
		<%} %>
		
		<tr class="portlet-font">
				<td width="100" align="center">
					<img src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/tools/objmetadata/document-meta.gif", currTheme)%>' />
				</td>
				<td width="20">
					&nbsp;
				</td>
				<td vAlign="middle">
				    <br/> 
					<a href='<portlet:actionURL><portlet:param name="PAGE" value="ListObjMetadataPage"/></portlet:actionURL>' 
						class="link_main_menu" >
					 	<spagobi:message key="SBISet.linkEngConf" />
					</a>
				</td>
			</tr>
	
	</table>
	<br/>
</div>
<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
