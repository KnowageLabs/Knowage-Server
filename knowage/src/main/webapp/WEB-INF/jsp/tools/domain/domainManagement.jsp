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


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!doctype html>
<html ng-app="domainManagementApp">

<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/domain/domainManagement.js")%>"></script>
	
</head>

<body>
	<div ng-controller="Controller as ctrl" layout="column" layout-wrap layout-fill>
		
		<angular-list-detail full-screen=true layout-column ng-cloak>
			<list label="translate.load('sbi.domain.managedomains')" new-function="addDomain" >
			
				<div class="kn-grid-container">
						<div ag-grid="gridOptions" class="ag-theme-balham ag-theme-knowage ag-theme-knowage-default" style="width:100%;"></div>
				</div>

	 		</list>
	 		
	 		<detail label="labelDetailFunction()" save-function="saveRow" cancel-function="closeDetail" disable-save-button="domainForm.$invalid">
			 		<md-content layout="row" layout-align="center center" class="config-content-detail">
						<md-card flex="50" layout="column" layout-align="center center">
							<md-card-content>
							<form name="domainForm">
								<md-input-container flex="100" class="md-block"> 
									<label>{{translate.load("sbi.domain.managedomains.fields.valuecd")}}</label> 
									<input name="valueCd" ng-model="domain.valueCd" required type="text" ng-maxlength="100" ng-pattern="regex.extendedAlphanumeric">
									<div ng-messages="domainForm.valueCd.$error" role="alert" ng-messages-multiple>
									    <div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.extendedAlphanumericRegex")}}</div>
									    <div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 100</div>
  									</div>
								</md-input-container>
								
								<md-input-container flex="100" class="md-block"> 
									<label>{{translate.load("sbi.domain.managedomains.fields.valuenm")}}</label> 
									<input name="valueNm" ng-model="domain.valueName" required type="text" ng-maxlength="40" ng-pattern="regex.extendedAlphanumeric">
									<div ng-messages="domainForm.valueNm.$error" role="alert" ng-messages-multiple>
									    <div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.extendedAlphanumericRegex")}}</div>
									    <div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 40</div>
  									</div>
								</md-input-container>
								
								<md-input-container flex="100" class="md-block"> 
									<label>{{translate.load("sbi.domain.managedomains.fields.domaincd")}}</label> 
									<input name="domainCd" ng-model="domain.domainCode" required type="text" ng-maxlength="20" ng-pattern="regex.extendedAlphanumeric">
									<div ng-messages="domainForm.domainCd.$error" role="alert" ng-messages-multiple>
									    <div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.extendedAlphanumericRegex")}}</div>
									    <div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 20</div>
  									</div>
								</md-input-container>
							
								<md-input-container flex="100" class="md-block"> 
									<label>{{translate.load("sbi.domain.managedomains.fields.domainnm")}}</label> 
									<input name="domainNm" ng-model="domain.domainName" required type="text" ng-maxlength="40" ng-pattern="regex.extendedAlphanumeric">
									<div ng-messages="domainForm.domainNm.$error" role="alert" ng-messages-multiple>
									    <div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.extendedAlphanumericRegex")}}</div>
									    <div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 40</div>
  									</div>
								</md-input-container>
							
								<md-input-container flex="100" class="md-block"> 
									<label>{{translate.load("sbi.domain.managedomains.fields.valueds")}}</label> 
									<input name="valueDs" ng-model="domain.valueDescription" required type="text" ng-maxlength="160" ng-pattern="regex.extendedAlphanumeric">
									<div ng-messages="domainForm.valueDs.$error" role="alert" ng-messages-multiple>
									    <div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.extendedAlphanumericRegex")}}</div>
									    <div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 160</div>
  									</div>
								</md-input-container>
							</form>
							</md-card-content>
						</md-card>
					</md-content>

	 		</detail>
 		</angular-list-detail>
			
		  
				
	</div>
</body>
</html>
