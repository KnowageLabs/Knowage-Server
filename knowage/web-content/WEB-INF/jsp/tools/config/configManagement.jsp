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


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!doctype html>
<html ng-app="configManagementApp">

<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/config/configManagement.js"></script>
	<link rel="stylesheet" type="text/css"    href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css">
</head>

<body  ng-controller="Controller as ctr" >
	<angular-list-detail full-screen=true layout-column>
		<list label="translate.load('sbi.config.manageconfig')" new-function="addConfig" >
			 <angular-table flex 
				id="table" ng-model="data" 
				columns='["label","name","valueCheck","valueTypeId","category","active"]'
				columns-search='["label","name","valueCheck","valueTypeId","category","active"]'
				highlights-selected-item = "true"
				show-search-bar="true"
				selected-item="itemSelected"
				speed-menu-option="configSpeedMenu"
			></angular-table>	
		</list>
		<detail label="labelDetailFunction()" save-function="saveRow" cancel-function="closeDetail" disable-save-button="configForm.$invalid">
			<form name="configForm">
				<md-content layout="row" layout-align="center center" class="config-content-detail">
					<md-card flex="50" layout="column" layout-align="center center">
						<md-content layout="row" layout-align="center center">
							<md-input-container flex="80" > 
								<label>{{translate.load("sbi.config.manageconfig.fields.label")}}</label> 
								<input ng-model="config.label" required type="text"> 
							</md-input-container>
						</md-content>
						<md-content layout="row" layout-align="center center">
						<md-input-container flex="80"> 
							<label>{{translate.load("sbi.config.manageconfig.fields.name")}}</label> 
							<input ng-model="config.name" required type="text"> 
						</md-input-container>
						</md-content>
						<md-content layout="row" layout-align="center center">
						<md-input-container flex="80"> 
							<label>{{translate.load("sbi.config.manageconfig.fields.description")}}</label> 
							<input ng-model="config.description" type="text"> 
						</md-input-container>
						</md-content>
						<md-content layout="row" layout-align="center center">
						<md-input-container flex="80"> 
							<md-select	placeholder="{{translate.load('sbi.config.manageconfig.fields.isactive')}}" ng-model="config.active" required> 
								<md-option value="true">True</md-option> 
								<md-option value="false">False</md-option>
							</md-select> 
						</md-input-container>
						</md-content>
						<md-content layout="row" layout-align="center center">
						<md-input-container flex="80">
							<label>{{translate.load("sbi.config.manageconfig.fields.valuecheck")}}</label> 
							<input ng-model="config.valueCheck" type="text"> 
						</md-input-container>
						</md-content>
						<md-content layout="row" layout-align="center center">
						<md-input-container flex="80"> 
							<md-select placeholder="{{translate.load('sbi.config.manageconfig.fields.valuetype')}}" ng-model="config.valueTypeId"> 
								<md-option value="407">NUM</md-option> 
								<md-option value="408">STRING</md-option>
							</md-select> 
						</md-input-container>
						</md-content>
						<md-content layout="row" layout-align="center center">
						<md-input-container flex="80"> 
							<md-select placeholder="{{translate.load('sbi.config.manageconfig.fields.category')}}" ng-model="config.category" required>
								<md-option ng-value="cnf.value" ng-repeat="cnf in filterCategory">
								{{cnf.value }}
								</md-option> 
							</md-select> 
						</md-input-container>
						</md-content>
					</md-card>
				</md-content>
			</form>
		</detail>
	</angular-list-detail>
</body>
</html>
