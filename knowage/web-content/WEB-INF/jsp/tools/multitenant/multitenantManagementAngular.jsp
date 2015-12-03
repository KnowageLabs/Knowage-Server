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

	<link rel="stylesheet" type="text/css" href="/knowage/themes/commons/css/generalStyle.css">
</head>

<body>
	<div ng-controller="Controller as ctrl">
		<div layout="row" layout-margin layout-fill layout-padding>
		  <div flex = "35">
		  	<div layout="column" class="left-part">
			  	<div class = "headContainer">
				  	<md-toolbar class="md-blue minihead">
					    <h1 class="md-toolbar-tools">
					      Tenants Management
					    </h1>
					</md-toolbar>
				</div>
				<div>
					<div >
					    <md-button class="md-raised md-ExtraMini" ng-click="addTenant()" ng-disabled = "tenant.MULTITENANT_ID || !tenant.MULTITENANT_NAME || !tenant.MULTITENANT_THEME ">{{translate.load("sbi.generic.add")}}</md-button>
					    <md-button class="md-raised md-ExtraMini" ng-click="addTenant()" ng-disabled = "!tenant.MULTITENANT_ID">{{translate.load("sbi.generic.save")}}</md-button>
					    <md-button class="md-raised md-ExtraMini" ng-click="deleteTenant()">{{translate.load("sbi.generic.delete")}}</md-button>
					    <md-button class="md-raised md-ExtraMini" ng-click="resetForm()" type="reset">{{translate.load("sbi.generic.search.clear")}}</md-button>
					    
					 </div>  
				    <md-content flex  layout-margin class="gridContainer">
					    <angular-table 
							id="tenant" ng-model="tenants" 
							columns='["MULTITENANT_ID","MULTITENANT_NAME"]'
							columns-search='["MULTITENANT_ID","MULTITENANT_NAME"]'
							highlights-selected-item = "true"
							show-search-bar="true"
							no-pagination="true"
							selected-item="tenantSelected"
							click-function = "copyRowInForm(item,cell,listId)"
						></angular-table>
					</md-content>
				</div>
			 </div>
		  </div>
		  
		  <div flex = "5"></div>
		  
		  <div flex = "60">
		     <md-content layout="column">
			    <md-content flex="100" >
				    <md-tabs md-dynamic-height>
						 <md-tab>
    						<md-tab-label>
    							{{translate.load("sbi.generic.details")}}
    						</md-tab-label>
    						<md-tab-body>
    							<form name="tenantForm">
									 <md-input-container>
									 	<label>{{translate.load("sbi.generic.name")}}</label>
									 	<input type="text" ng-model="tenant.MULTITENANT_NAME" md-maxlength="200" required ng-disabled = "tenant.MULTITENANT_ID">
									 </md-input-container>
									 <md-input-container>
									 	<label>{{translate.load("sbi.multitenant.theme")}}</label>
									 	<md-select ng-model="tenant.MULTITENANT_THEME" required>
											<md-option ng-value="theme.VALUE_CHECK" ng-repeat="theme in themes">{{ theme.VALUE_CHECK }}</md-option>
									 	</md-select>
									 </md-input-container>
								 </form>
							 </md-tab-body>
						 </md-tab>
						  <md-tab>
						 	<md-tab-label>
    							Product Types
    						</md-tab-label>
    						<md-tab-body>
						 		 <div flex layout="column" layout-fill>
								 	<angular-table 
										id="productTypes" ng-model="productTypes" 
										columns='["LABEL","checkbox"]'
										columns-search='["LABEL"]'
										highlights-selected-item = "true"
										show-search-bar="true"
										no-pagination="true"
										click-function = "toogleCheckBox(item,cell,listId)"
									></angular-table>
								</div>
							</md-tab-body>								 
						 </md-tab>
						 <md-tab label="Data Source">
							 <md-tab-label>
    							{{translate.load("sbi.ds.dataSource")}}
    						</md-tab-label>
    						<md-tab-body>
						 		<div flex="100">
							 		<angular-table 
										id="datasource" ng-model="datasources" 
										columns='["LABEL","DESCRIPTION","checkbox"]'
										columns-search='["LABEL"]'
										highlights-selected-item = "true"
										show-search-bar="true"
										no-pagination="true"
										click-function = "toogleCheckBox(item,cell,listId)"
									></angular-table>
						 		</div>
						 	</md-tab-body>
						 </md-tab>
					</md-tabs>
				</md-content>
			 </md-content>
		  </div>
		</div>
	</div>
</body>
</html>