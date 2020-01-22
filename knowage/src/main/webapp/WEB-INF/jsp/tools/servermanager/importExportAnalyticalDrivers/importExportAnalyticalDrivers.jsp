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


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="impExpModule">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">

<!-- controller -->
<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/analyticalDriversImportExport/importExportAnalyticalDriversController.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/documentImportExport/importExportDocumentsController.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/analyticalDriversImportExport/importAnalyticalDriversStep0Controller.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/analyticalDriversImportExport/importAnalyticalDriversStep1Controller.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/directive/ngExportCatalog.js")%>">"></script>

	
<!-- 	breadCrumb -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/commons/BreadCrumb.js")%>"></script>

	
</head>
<body class="bodyStyle kn-importExportDocument">
<rest-loading></rest-loading>
	<div ng-controller="Controller " layout="column" layout-wrap
		layout-fill>
		<md-toolbar class="miniheadimportexport">
			<div class="md-toolbar-tools">
				<i class="fa fa-exchange fa-2x"></i>
				<h2 class="md-flex">{{translate.load("sbi.importexportdrivers");}}</h2>
			</div>
		</md-toolbar>
		<md-content layout="column" layout-wrap flex class="mainContainer">
			 <md-tabs md-select="ImportExport" layout-fill class="absolute">  
				<md-tab label="Export" md-on-select="setTab('Export')" md-active="isSelectedTab('Export')"> 
			
					<md-tab-body layout-fill>
					 <md-card>
						<md-card-content  layout="column" >  
					
							<div layout="row" >
								<md-input-container flex class="md-block">
									<label>{{translate.load("sbi.impexpusers.nameexport")}}</label>
									<input type="text" ng-model="nameExport" requiredmaxlength="100" ng-maxlength="100" md-maxlength="100" /> 
								</md-input-container>
							
								 <md-button	ng-show="!wait" ng-click="prepare($event)"	aria-label="download drivers" class="md-fab md-mini">							
									<md-icon md-font-icon="fa fa-download"> </md-icon>
								 </md-button>
							</div>
							<div>
								 <md-tabs md-dynamic-height >
								      <md-tab id="Drivers"  md-on-select="showDrivers=true" >
								        <md-tab-label>{{translate.load("sbi.importexportcatalog.radiodrivers");}}</md-tab-label>
								        <md-tab-body >
								         	<export-catalog ng-if="showDrivers" type-catalog="AnalyticalDrivers"  path-catalog="analyticaldrivers" catalog-data="elementsDrivers" catalog-selected="catalogSelected"></export-catalog>
								        </md-tab-body>
								      </md-tab>								      
								</md-tabs>
							</div>
						</md-card-content> 
						</md-card> 
			
					</md-tab-body> 
				</md-tab> 
				<md-tab label="Import" md-on-select="setTab('Import')" id="importTab" md-active="isSelectedTab('Import')">
					<md-tab-body layout-fill >
					<md-card >
						<md-toolbar class="ternaryToolbar" flex="nogrow">
			                <div class="md-toolbar-tools noPadding" layout="row">
								<bread-crumb flex ng-model='stepItem' item-name='name' selected-index='selectedStep' control='stepControl'>
								</bread-crumb>
							</div>
						</md-toolbar>
					<md-card-content ng-cloak layout-wrap  ng-cloak ng-switch="selectedStep">
						
						<!-- Upload file -->
						<div class="importSteps" flex  ng-switch-when="0">
							<div layout="row" layout-wrap >
								<file-upload flex id="AssociationFileUploadImport" ng-model="IEDConf.fileImport" file-max-size="<%=importFileMaxSizeMB%>" ></file-upload>
								<md-button ng-click="upload($event)" aria-label="upload Menu"
										class="md-fab md-mini"  > <md-icon
										md-font-icon="fa fa-upload"  >
							</div>
							<div layout="row" layout-wrap >
								<md-radio-group layout="row" ng-model="typeSaveMenu">
								      <md-radio-button value="Override" ng-click="setTypeSaveMenu('Override')">{{translate.load("sbi.importusers.override");}}</md-radio-button>
								      <md-radio-button value="Missing" ng-click="setTypeSaveMenu('Missing')">{{translate.load("sbi.importusers.addmissing");}} </md-radio-button>
								    </md-radio-group>
								
								<span flex></span>							
								<md-button class="md-raised" ng-click="associatedrole($event)" aria-label="upload Menu" >{{translate.load('sbi.generic.next');}}</md-button>
							</div>
						 
							<div layout="column">
								<!-- section -->											
								<angular-table flex  ng-show="IEDConf.showDriversImported" id='cataloglistImported' ng-model=exportedCatalog
									columns='[{"label":"Name","name":"name","size":"20px"},{"label":"Type","name":"type","size":"20px"}]'
									columnsSearch='["name"]' show-search-bar=true
									highlights-selected-item=true multi-select=true
									selected-item=catalogSelected no-pagination=true
									scope-functions=tableFunction> 
								</angular-table>														
							</div>
							
						</div>
						
						
						<!-- Roles -->
						<div class="importSteps" flex ng-controller="importAnalyticalDriversControllerStep0" ng-switch-when="1">
							<%@include file="importAnalyticalDriversStep0.jsp"%>
						</div>
						
						<!-- Datasource -->
						<div class="importSteps" flex ng-controller="importAnalyticalDriversControllerStep1" ng-switch-when="2">
							<%@include file="importAnalyticalDriversStep1.jsp"%>
						</div>

					</md-card-content>
					</md-card>
					</md-tab-body>
			</md-tab>

		</md-tabs>
	</md-content>
	</div>
</body>
</html>
