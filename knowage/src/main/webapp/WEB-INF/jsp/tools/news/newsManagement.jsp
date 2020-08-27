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

<%@ page language="java" pageEncoding="UTF-8" session="true"%>
<%@ page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<% String contextName = KnowageSystemConfiguration.getKnowageContext(); %>

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
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "/js/src/angular_1.4/tools/news/newsManagement.js")%>"></script>
</head>

<body class="kn-news-management" ng-app="newsManagement" id="ng-app">
	<angular-list-detail ng-controller="newsManagementController" full-screen="false" ng-cloak>
       	<list label="'News Management'" new-function="newNews">
			<div ag-grid="listGridOptions" class="ag-theme-balham ag-theme-knowage-default ag-hide-outline" style="width:100%;height:calc(100% - 32px);"></div>
		</list>
        <detail label="'News Detail'" save-function="saveFunc">
        	<form name="newsForm">
				<div layout-fill class="containerDiv">
					<div layout="row" layout-align="center center" layout-fill style="z-index:510;background-color:rgba(0,0,0,.3); position:absolute;" ng-if="loading">
				      <md-progress-circular md-mode="indeterminate"></md-progress-circular>
				    </div>
					<div ng-if="!selectedNews" class="noNews">
						<div class="emptyIconSvg">
						</div>
						<div class="emptyIconText">
							{{::translate.load('sbi.news.nonews')}}
						</div>
					</div>
					<md-card ng-show="selectedNews">
						<md-subheader class="switchSubheader"><span>{{::translate.load('sbi.news.settings')}}</span><span flex></span><md-switch ng-model="selectedNews.active">{{::translate.load('sbi.news.active')}}</md-switch></md-subheader>
						<md-card-content>
							<div layout="row">
								<md-input-container flex="50">
									<label>{{::translate.load('sbi.news.title')}}</label>
									<input type="text" ng-model="selectedNews.title" required/>
								</md-input-container>
								<md-input-container flex="20">
									<label>{{::translate.load('sbi.news.expiration')}}</label>
									<md-datepicker ng-model="tempExpirationDate" required></md-datepicker>
								</md-input-container>
								<md-input-container flex="30">
									<label>{{::translate.load('sbi.news.type')}}</label>
									<md-select ng-model="selectedNews.type" required>
										<md-option ng-repeat="opt in typeMapping" ng-value="opt.id">{{opt.value}}</md-option>
									</md-select>
								</md-input-container>
							</div>
							
							<md-input-container class="md-block">
								<label>{{::translate.load('sbi.news.description')}}</label>
								<textarea ng-model="selectedNews.description" md-maxlength="140" rows="2" placeholder="{{::translate.load('sbi.news.description.placeholder')}}" required></textarea>
							</md-input-container>
							<wysiwyg-edit ng-show="selectedNews" content="selectedNews.html" config="editorConfig" class="kn-custom-wysiwyg-editor"></wysiwyg-edit>
						</md-card-content>
					</md-card>
					<md-card ng-show="selectedNews">
						<md-subheader>{{::translate.load('sbi.news.roles')}}</md-subheader>
						<md-card-content>
							<div ag-grid="permissionGridOptions" class="ag-theme-balham ag-theme-knowage-default ag-hide-selection ag-hide-outline" style="width:100%;height:200px;"></div>
						</md-card-content>
					</md-card>
				</div>
			</form>
		</detail>
	</angular-list-detail>
</body>
</html>

<%}else{ %>
Access Denied
<%} %>

