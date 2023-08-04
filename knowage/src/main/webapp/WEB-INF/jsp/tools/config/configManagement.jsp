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
<html ng-app="configManagementApp">

<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/config/configManagement.js")%>"></script>
	<link rel="stylesheet" type="text/css"    href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">
</head>

<body  ng-controller="Controller as ctr" >
	<angular-list-detail full-screen=true layout-column>
		<list label="translate.load('sbi.config.manageconfig')" new-function="addConfig" >
			<div style="padding: 0px 16px;">
				<md-input-container md-no-float class="md-block">
			      <md-icon md-font-icon="fa fa-search"></md-icon>
			      <input ng-model="configSearchText" ng-change="filterConfig()" type="text" placeholder="{{::translate.load('kn.internationalization.search')}}">
			    </md-input-container>
		    </div>
			<div class="kn-grid-container">
				<div ag-grid="configurationGridOptions" class="ag-theme-balham ag-theme-knowage ag-theme-knowage-default" style="width:100%;"></div>
			</div>
			 <!--  angular-table flex 
				id="table" ng-model="data" 
				columns='["label","name","valueCheck","category"]'
				columns-search='["label","name","valueCheck","category"]'
				highlights-selected-item = "true"
				show-search-bar="true"
				selected-item="itemSelected"
				speed-menu-option="configSpeedMenu"
			></angular-table-->	
		</list>
		<detail label="labelDetailFunction()" save-function="saveRow" layout="column"   cancel-function="closeDetail" disable-save-button="configForm.$invalid">
			<form name="configForm" layout="row" flex layout-align="center start"> 
					<md-card flex="50" flex-sm="100" >
						<md-card-content layout="column" layout-padding >
						<div layout="row">
							<md-input-container flex> 
								<label>{{translate.load("sbi.config.manageconfig.fields.label")}}</label> 
								<input name="label" ng-model="config.label" required type="text" ng-maxlength="100" ng-pattern="regex.extendedAlphanumeric">
								<div ng-messages="configForm.label.$error" role="alert" ng-messages-multiple>
								    <div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.extendedAlphanumericRegex")}}</div>
								    <div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 100</div>
  								</div>
							</md-input-container>
						</div>
						<div layout="row">
							<md-input-container flex>
								<label>{{translate.load("sbi.config.manageconfig.fields.name")}}</label> 
								<input name="name" ng-model="config.name" required type="text" ng-maxlength="100" ng-pattern="regex.extendedAlphanumeric"> 
								<div ng-messages="configForm.name.$error" role="alert">
								    <div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.extendedAlphanumericRegex")}}</div>
								    <div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 100</div>
  								</div>
							</md-input-container>
						</div>
						<div layout="row">
							<md-input-container flex> 
								<label>{{translate.load("sbi.config.manageconfig.fields.description")}}</label> 
								<input name="description"  ng-model="config.description" type="text" ng-maxlength="500" ng-pattern="regex.extendedAlphanumeric">
								<div ng-messages="configForm.description.$error" role="alert"  ng-messages-multiple>
								    <div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.extendedAlphanumericRegex")}}</div>
								    <div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 500</div>
  								</div>
							</md-input-container>
						</div>
						<div layout="row">
							<md-input-container flex> 
								<label>{{translate.load('sbi.config.manageconfig.fields.isactive')}}</label>
								<md-select	placeholder="{{translate.load('sbi.config.manageconfig.fields.isactive')}}" ng-model="config.active" required> 
									<md-option value="true">True</md-option> 
									<md-option value="false">False</md-option>
								</md-select> 
							</md-input-container>
						</div>
						<div layout="row">
							<md-input-container flex>
								<label>{{translate.load("sbi.config.manageconfig.fields.valuecheck")}}</label> 
								<input name="valuecheck" ng-model="config.valueCheck" type="text" ng-maxlength="1000" ng-pattern="regex.xss">
								<div ng-messages="configForm.valuecheck.$error" role="alert"  ng-messages-multiple>
								    <div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.xssRegex")}}</div>
								    <div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 1000</div>
  								</div>
							</md-input-container>
						</div>
						<div layout="row">
						<md-input-container flex>
							<label>{{translate.load('sbi.config.manageconfig.fields.valuetype')}}</label>
							<md-select placeholder="{{translate.load('sbi.config.manageconfig.fields.valuetype')}}" ng-model="config.valueTypeId" required> 
								<md-option value="407">NUM</md-option> 
								<md-option value="408">STRING</md-option>
							</md-select> 
						</md-input-container>
						</div>
						<div layout="row">
						<md-input-container flex> 
							<label>{{translate.load('sbi.config.manageconfig.fields.category')}}</label>
							<md-select placeholder="{{translate.load('sbi.config.manageconfig.fields.category')}}" ng-model="config.category" required>
								<md-option ng-value="cnf.value" ng-repeat="cnf in filterCategory">
								{{cnf.value }}
								</md-option> 
							</md-select> 
						</md-input-container>
						</div>
						</md-card-content>
					</md-card>
			</form>
		</detail>
	</angular-list-detail>
</body>
</html>
