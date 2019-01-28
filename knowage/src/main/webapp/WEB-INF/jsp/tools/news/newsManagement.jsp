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

<%@ page language="java" pageEncoding="utf-8" session="true"%>
<%@ page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<% String contextName = request.getParameter(SpagoBIConstants.SBI_CONTEXT); %>

<%
		//TODO check for user profile autorization
		boolean canSee=false,canSeeAdmin=false;
		if(UserUtilities.haveRoleAndAuthorization(userProfile, null, new String[]{SpagoBIConstants.MANAGE_CROSS_NAVIGATION})){
			canSee=true;
		 canSeeAdmin=UserUtilities.haveRoleAndAuthorization(userProfile, SpagoBIConstants.ADMIN_ROLE_TYPE, new String[]{SpagoBIConstants.MANAGE_CROSS_NAVIGATION});
		}
%>

<% if(canSee ){ %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html >

<head>
	<!-- include libraries(jQuery, bootstrap) -->
	<link href="http://netdna.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.css" rel="stylesheet">
	<script src="http://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.js"></script> 
	<script src="http://netdna.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.js"></script> 
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/news/newsManagement.js")%>"></script>
	
	</head>

<body class="kn-news-management" ng-app="newsManagement" id="ng-app">
	<angular-list-detail ng-controller="newsManagementController" full-screen="false" ng-cloak>
       	<list label="'News Management'" new-function="newNews">
			<div ag-grid="listGridOptions" class="ag-theme-balham ag-theme-knowage ag-theme-knowage-secondary" style="padding:8px;width:100%;height:calc(100% - 32px);"></div>
		</list>
		
        <detail label="News" save-function="saveFunc" cancel-function="cancelFunc">
			<div layout-fill class="containerDiv" ng-show="selectedNews">
				<div ng-show="folderDocuments.length==0" class="emptyContainer">
					<div class="outerIcon">
						<div class="emptyIconSvg"></div>
					</div>
					<div class="emptyIconText">
						{{translate.load("sbi.browser.document.noDocument")}}
					</div>
				</div>
				<md-card>
					<md-subheader class="switchSubheader"><span>Composition</span><span flex></span><md-switch ng-model="selectedNews.active">Active</md-switch></md-subheader>
					<md-card-content>
						<div layout="row">
							<md-input-container flex="50">
								<label>News Title</label>
								<input type="text" ng-model="selectedNews.title"/>
							</md-input-container>
							<md-input-container flex="20">
								<label>Expiration date</label>
								<md-datepicker ng-model="selectedNews.expirationDate"></md-datepicker>
							</md-input-container>
							<md-input-container flex="30">
								<label>Type</label>
								<md-select ng-model="selectedNews.type">
									<md-option ng-repeat="opt in ['news','notification','warning','announcement']" ng-value="opt">{{opt}}</md-option>
								</md-select>
							</md-input-container>
						</div>
						
						<md-input-container class="md-block">
							<label>News Description</label>
							<textarea ng-model="selectedNews.description" md-maxlength="140" rows="2" placeholder="This is the text that will appear on the news notification"></textarea>
						</md-input-container>
						<summernote ng-model="selectedNews.html" config="wysiwygOptions"></summernote>
					</md-card-content>
				</md-card>
				<md-card>
					<md-subheader>Permissions</md-subheader>
					<md-card-content>
						<div ag-grid="premissionGridOptions" class="ag-theme-balham" style="width:100%;height:200px;"></div>
					</md-card-content>
				</md-card>
			</div>
		</detail>
	</angular-list-detail>
</body>
</html>

<%}else{ %>
Access Denied
<%} %>

