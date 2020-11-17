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
<link rel="stylesheet" type="text/css"
	href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">
<!-- controller -->
<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/catalogImportExport/importExportCatalogController.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/documentImportExport/importExportDocumentsController.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/catalogImportExport/importCatalogStep0Controller.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/catalogImportExport/importCatalogStep1Controller.js")%>"></script>
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
				<h2 class="md-flex">{{translate.load("sbi.importexportcatalog");}}</h2>
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
							
								 <md-button	ng-show="!wait" ng-click="prepare($event)"	aria-label="download catalogs" class="md-fab md-mini">
									<md-icon md-font-icon="fa fa-download"> </md-icon>
								 </md-button>
							</div>
							<div>
								 <md-tabs md-dynamic-height >
								      <md-tab id="Dataset"  md-on-select="showDataset=true" >
								        <md-tab-label>
								        	{{translate.load("sbi.importexportcatalog.radiodataset");}}
								        	<span ng-show="getCatalogForCategory(catalogSelected, 'Dataset').length > 0" class="tab-badge">{{getCatalogForCategory(catalogSelected, 'Dataset').length}}</span>
								        	<span ng-show="getCatalogForCategory(catalogSelected, 'Dataset').length == 0" class="tab-spacer"></span>
								        </md-tab-label>
								        <md-tab-body >
								         	<export-catalog ng-if="showDataset" type-catalog="Dataset" path-catalog="catalog" catalog-data="catalogDataset" catalog-selected="catalogSelected"></export-catalog>
								        </md-tab-body>
								      </md-tab>
								       <md-tab id="BusinessModel"  md-on-select="showBM=true" >
								        <md-tab-label>
								        	{{translate.load("sbi.importexportcatalog.radiobusinessmodel");}}
								        	<span ng-show="getCatalogForCategory(catalogSelected, 'BusinessModel').length > 0" class="tab-badge">{{getCatalogForCategory(catalogSelected, 'BusinessModel').length}}</span>
								        	<span ng-show="getCatalogForCategory(catalogSelected, 'BusinessModel').length == 0" class="tab-spacer"></span>
								        </md-tab-label>
								        <md-tab-body>
								         	<export-catalog ng-if="showBM" type-catalog="BusinessModel"  path-catalog="catalog" catalog-data="catalogBM" catalog-selected="catalogSelected"></export-catalog>
								        </md-tab-body>
								      </md-tab>
								       <md-tab id="MondrianSchema" md-on-select="showSchema=true">
								        <md-tab-label>
								        	{{translate.load("sbi.importexportcatalog.radiomondrianschema");}}
								        	<span ng-show="getCatalogForCategory(catalogSelected, 'MondrianSchema').length > 0" class="tab-badge">{{getCatalogForCategory(catalogSelected, 'MondrianSchema').length}}</span>
								        	<span ng-show="getCatalogForCategory(catalogSelected, 'MondrianSchema').length == 0" class="tab-spacer"></span>
								        </md-tab-label>
								        <md-tab-body>
								         	<export-catalog  ng-if="showSchema" type-catalog="MondrianSchema"  path-catalog="catalog" catalog-data="catalogSchema" catalog-selected="catalogSelected"></export-catalog>
								        </md-tab-body>
								      </md-tab>
								       <md-tab id="SVG" md-on-select="showSVG=true">
								        <md-tab-label>
								        	{{translate.load("sbi.importexportcatalog.radiosvg");}}
								        	<span ng-show="getCatalogForCategory(catalogSelected, 'SVG').length > 0" class="tab-badge">{{getCatalogForCategory(catalogSelected, 'SVG').length}}</span>
								        	<span ng-show="getCatalogForCategory(catalogSelected, 'SVG').length == 0" class="tab-spacer"></span>
								        </md-tab-label>
								        <md-tab-body>
								         	<export-catalog ng-if="showSVG" type-catalog="SVG"  path-catalog="catalog" catalog-data="catalogSVG" catalog-selected="catalogSelected"></export-catalog>
								        </md-tab-body>
								      </md-tab>
								       <md-tab id="Layer" md-on-select="showLayer=true">
								        <md-tab-label>
								        	<span>{{translate.load("sbi.importexportcatalog.radiolayer");}}</span>
								        	<span ng-show="getCatalogForCategory(catalogSelected, 'Layer').length > 0" class="tab-badge">{{getCatalogForCategory(catalogSelected, 'Layer').length}}</span>
								        	<span ng-show="getCatalogForCategory(catalogSelected, 'Layer').length == 0" class="tab-spacer"></span>
								        </md-tab-label>
								        <md-tab-body>
								         	<export-catalog ng-if="showLayer" type-catalog="Layer"  path-catalog="catalog" catalog-data="catalogLayer" catalog-selected="catalogSelected"></export-catalog>
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
							<!-- catalog section -->
							<h4 ng-show="showDatasetImported">{{translate.load("sbi.importexportcatalog.messagesfederated");}}</h4>												
							<angular-table flex  ng-show="IEDConf.showCatalogImported" id='cataloglistImported' ng-model=exportedCatalog
								columns='[{"label":"Type Catalog","name":"typeCatalog","size":"20px"},{"label":"Name","name":"name","size":"20px"},{"label":"Type","name":"type","size":"20px"}]'
								columnsSearch='["name"]' show-search-bar=true
								highlights-selected-item=true multi-select=true
								selected-item=catalogSelected true
								 no-pagination=true
								scope-functions=tableFunction>  
							</angular-table> 		 
						</div>
						
						</div>
						
						
						<!-- Roles -->
						<div class="importSteps" flex ng-controller="importCatalogControllerStep0" ng-switch-when="1">
							<%@include file="importCatalogStep0.jsp"%>
						</div>
						
						<!-- Datasource -->
						<div class="importSteps" flex ng-controller="importCatalogControllerStep1" ng-switch-when="2">
							<%@include file="importCatalogStep1.jsp"%>
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
