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
	<link rel="stylesheet" type="text/css"   href="<%=urlBuilder.getResourceLinkByTheme(request, "/themes/commons/css/generalStyle.css", currTheme)%>">
</head>

<body>
	<div ng-controller="Controller as ctrl"  layout="row" layout-wrap layout-fill>
			<angular-2-col style="width : 100%;"> <!-- left BOX --> 
				<left-col>
				<div class="leftBox">
				  	
				  	<md-toolbar class="md-blue minihead">	
				  	<div class="md-toolbar-tools">				    
					      Tenants Management
							<!-- button trash, to insert if we decide to active multi-select in angular-table 
							
							<md-button aria-label="delete tenant" class="md-fab md-ExtraMini" style="position:absolute; right:26px; top:0px; background-color:#E91E63" ng-click="deleteTenant()">
								<md-icon md-font-icon="fa fa-trash" style=" margin-top: 6px ; color: white;">
								</md-icon>
							</md-button>
							 -->
						    <md-button aria-label="new tenant" class="md-fab md-ExtraMini addButton" ng-click="formTenant()" style="position:absolute; right:11px; top:0px;"> 
							    <md-icon md-font-icon="fa fa-plus" style=" margin-top: 6px ; color: white;">
								</md-icon>
							</md-button>
					</div>
					</md-toolbar>
				    
				    <md-content layout-padding style="background-color: rgb(236, 236, 236);" class="ToolbarBox miniToolbar noBorder leftListbox">
					    <angular-table layout-fill
							id="tenant" ng-model="tenants" 
							columns='[{"label":"ID","name":"MULTITENANT_ID"},{"label":"Name","name":"MULTITENANT_NAME"}]'
							columns-search='["MULTITENANT_ID","MULTITENANT_NAME"]'
							highlights-selected-item = "true"
							show-search-bar="true"
							no-pagination="true"
							selected-item="tenantSelected"
							click-function = "copyRowInForm(item,cell,listId)"
							speed-menu-option="tenantSpeedMenu"
						></angular-table>
					</md-content>
			  </div>
			  </left-col>
			  
			  <right-col>
			  	<form name="tenantForm" novalidate>	
			  	  <div  ng-show="loadinMessage" class="loading-message">
						<i class="fa fa-spinner fa-spin fa-5x"></i>
							<span class="">
							{{translate.load("sbi.generic.wait");}} 
							</span>
			  	  </div> 
				  <div ng-show="showForm && !loadinMessage">
					<md-content layout="column" >
						<md-toolbar class="md-blue minihead">
							<div class="md-toolbar-tools h100 ">
								<div style="text-align: center; font-size: 24px;">Tenant</div>
								<div style="position: absolute; right: 0px" class="h100">							
								    <md-button tabindex="-1" aria-label="Add Tenant" class="md-raised md-ExtraMini mozilla color-black " style=" margin-top: 2px;" ng-click="resetForm(tenantForm)" type="reset">{{translate.load("sbi.generic.search.clear")}}</md-button>
									<md-button tabindex="-1" aria-label="Add Tenant" class="md-raised md-ExtraMini mozilla color-black " style=" margin-top: 2px;" ng-click="saveTenant(tenantForm)" ng-disabled = "!tenant.MULTITENANT_NAME || !tenant.MULTITENANT_THEME || datasourcesSelected.length==0 || productsSelected.length==0">{{translate.load("sbi.generic.save")}}</md-button>
								</div>
							</div>	
						</md-toolbar>
					    <md-content layout="column" layout-align="space-around stretch">  
							<md-tabs md-dynamic-height md-selected="idx_tab" layout-fill>
								<md-tab>
									<md-tab-label>
										{{translate.load("sbi.generic.details")}}
									</md-tab-label>
									<md-tab-body>
										
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
							 		</md-tab-body>
								 </md-tab>
								 <md-tab>
								 	<md-tab-label>
										Product Types
									</md-tab-label>
									<md-tab-body>
								 		<div flex layout="column" layout-fill>
										 	<md-content style="min-height: 30rem;">
											 	<angular-table 
													id="productTypes" ng-model="productTypes" 
													columns='[{"label":"Label","name":"LABEL"},{"label":"Select","name":"checkbox"}]'
													columns-search='["LABEL"]'
													highlights-selected-item = "true"
													show-search-bar="true"
													no-pagination="true"
													click-function = "toggleCheckBox(item,cell,listId)"  
													multi-select = "true"
													selected-item = "productsSelected"
												></angular-table>
											</md-content>
										</div>
									</md-tab-body>								 
								</md-tab>
								<md-tab>
									<md-tab-label>
										{{translate.load("sbi.ds.dataSource")}}
									</md-tab-label>
									<md-tab-body>
								 		<div flex layout="column" layout-fill>
								 			<md-content style="min-height: 30rem;">
										 		<angular-table 
													id="datasource" ng-model="datasources" 
													columns='[{"label":"Label","name":"LABEL"},{"label":"Description","name":"DESCRIPTION"},{"label":"Select","name":"checkbox"}]'
													columns-search='["LABEL"]'
													highlights-selected-item = "true"
													show-search-bar="true"
													no-pagination="true"
													click-function = "toggleCheckBox(item,cell,listId)"
													multi-select = "true"
													selected-item = "datasourcesSelected"
												></angular-table>
											</md-content>
								 		</div>
								 	</md-tab-body>
								</md-tab>
							</md-tabs>
						</md-content>
					 </md-content>
				  </div>
			  </form>
			  </right-col>
		  </angular-2-col>
	</div>
</body>
</html>