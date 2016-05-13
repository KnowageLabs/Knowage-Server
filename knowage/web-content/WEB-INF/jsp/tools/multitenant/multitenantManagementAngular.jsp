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
<html ng-app="tenantManagementApp">

<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/multitenant/tenantManagement.js"></script>

	<link rel="stylesheet" type="text/css"   href="<%=urlBuilder.getResourceLinkByTheme(request, "/css/multitenant/tenantStyle.css", currTheme)%>">
	<link rel="stylesheet" type="text/css"   href="<%=urlBuilder.getResourceLinkByTheme(request, "/themes/commons/css/customStyle.css", currTheme)%>">
</head>

<body ng-cloak>
	<div ng-controller="Controller as ctrl"  layout="row" layout-wrap layout-fill>
		<angular-list-detail layout-column show-detail="showMe">
			<list label="translate.load('sbi.multitenant')" new-function="addTenant" >				  	
			    <angular-table flex
					id="tenant" ng-model="tenants" 
					columns='[{"label":"Name","name":"MULTITENANT_NAME"}]'
					columns-search='["MULTITENANT_NAME"]'
					highlights-selected-item = "true"
					show-search-bar="true"
					selected-item="tenantSelected"
					click-function = "copyRowInForm(item,cell,listId)"
					speed-menu-option="tenantSpeedMenu"
				></angular-table>
			</list>
			<detail label="property == undefined ? '' : translate.load('sbi.generic.details')" save-function="saveTenant" cancel-function="resetForm" 
				disable-save-button="tenantForm.$invalid || productsSelected.length == 0 || datasourcesSelected.length == 0" show-save-button="showMe" show-cancel-button="showMe">
				<form name="tenantForm" flex="90" layout="column">
					<div  ng-show="loadinMessage" class="loading-message">
						<md-progress-circular loading ng-show="loadinMessage" md-mode="indeterminate" md-diameter="75%" style="position:fixed;top:50%;left:50%;z-index: 500;background:rgba(255, 255, 255, 0);"></md-progress-circular>
						<span class="">{{translate.load("sbi.generic.wait");}}</span>
					</div> 
					<md-tabs md-selected="selectedIndex.idx" ng-if="!loadinMessage" md-dynamic-height>
						<md-tab>
							<md-tab-label>
								{{translate.load("sbi.generic.details")}}
							</md-tab-label>
							<md-tab-body>
								<md-card>
									<md-content layout="row" layout-align="center center">
										<md-input-container flex="90">
										 	<label>{{translate.load("sbi.generic.name")}}</label>
										 	<input type="text" ng-model="tenant.MULTITENANT_NAME" md-maxlength="200" required ng-disabled = "tenant.MULTITENANT_ID">
									 	</md-input-container>
									 </md-content>
									 <md-content layout="row" layout-align="start center">
									 	<md-content flex="5"></md-content>
										<md-input-container flex="50">
										 	<label>{{translate.load("sbi.multitenant.theme")}}</label>
										 	<md-select ng-model="tenant.MULTITENANT_THEME" required>
												<md-option ng-value="theme.VALUE_CHECK" ng-repeat="theme in themes">{{ theme.VALUE_CHECK }}</md-option>
										 	</md-select>
										 </md-input-container>
									 </md-content>
								 </md-card>
					 		</md-tab-body>
						 </md-tab>
						 <md-tab>
						 	<md-tab-label>
								{{translate.load("sbi.multitenant.producttypes")}}
							</md-tab-label>
							<md-tab-body layout="column">
						 		<md-card layout-fill layout="column">
								 	<angular-table layout-fill
										id="productTypes" ng-model="productTypes" 
										columns='[{"label":"Name","name":"LABEL"}]'
										columns-search='["LABEL"]'
										highlights-selected-item = "true"
										show-search-bar="true"
										click-function = "toggleCheckBox(item,cell,listId)"  
										multi-select = "true"
										selected-item = "productsSelected"
									></angular-table>
								</md-card>
							</md-tab-body>								 
						</md-tab>
						<md-tab>
							<md-tab-label>
								{{translate.load("sbi.ds.dataSource")}}
							</md-tab-label>
							<md-tab-body flex="90">
								<md-card layout-fill layout="column">
							 		<angular-table layout-fill
										id="datasource" ng-model="datasources" 
										columns='[{"label":"Label","name":"LABEL"},{"label":"Description","name":"DESCRIPTION"}]'
										columns-search='["LABEL"]'
										highlights-selected-item = "true"
										show-search-bar="true"
										click-function = "toggleCheckBox(item,cell,listId)"
										multi-select = "true"
										selected-item = "datasourcesSelected"
									></angular-table>
								</md-card>
						 	</md-tab-body>
						</md-tab>
					</md-tabs>
	 			 </form>
 			 </detail>
		</angular-list-detail>
	</div>
</body>
</html>
