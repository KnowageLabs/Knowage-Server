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


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="impExpDataset">

<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<link rel="stylesheet" type="text/css"
	href="/knowage/themes/commons/css/customStyle.css">
<!-- non c'entra	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/glossary/commons/LayerTree.js"></script> -->
<!-- controller -->
<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/servermanager/importExportCatalogController.js"></script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/BreadCrumb.js"></script>
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/themes/glossary/css/bread-crumb.css">
</head>
<body class="bodyStyle kn-importExportDocument">

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
			
					<md-tab-body layout-fill><md-card>
						<md-content layout-fill layout="column" layout-wrap>
							<div layout="row" layout-wrap>
								<div flex>
										<md-input-container flex class="md-block">
										<label>{{translate.load("sbi.impexpusers.nameexport")}}</label>
										<input type="text" ng-model="nameExport" requiredmaxlength="100" ng-maxlength="100" md-maxlength="100" /> 
									</md-input-container>
									
								</div>
								<div flex=5>
									<md-input-container class="small counter"> <md-button
										ng-show="!wait" ng-click="prepare($event)"
										aria-label="download Dataset" class="md-fab md-mini">
									<md-icon md-font-icon="fa fa-download"> </md-icon> </md-button>
									<div ng-show="wait">
										<i class="fa fa-spinner fa-spin fa-4x"></i>
									</div>
									<!--  <md-progress-circular ng-show="wait" md-mode="indeterminate"></md-progress-circular>-->
									</md-input-container>
								</div>
							</div>
							<md-checkbox ng-checked="exists('Dataset',listType)" ng-click="toggle('Dataset',listType)">{{translate.load("sbi.importexportcatalog.radiodataset");}}</md-checkbox>
							<div flex ng-show="showDataset">
								<angular-table id='datasetlist' ng-model=dataset
									columns='[{"label":"Label","name":"label","size":"20px"},{"label":"Name","name":"name","size":"20px"}]'
									columnsSearch='["label"]' show-search-bar=true
									highlights-selected-item=true multi-select=true
									selected-item=datasetSelected no-pagination=true
									scope-functions=tableFunction> 
								</angular-table>
							</div>

						</md-content> </md-card>
					</md-tab-body> 
				</md-tab> 
				<md-tab label="Import" md-on-select="setTab('Import')" id="importTab" md-active="isSelectedTab('Import')">
					<md-tab-body layout-fill ><md-card>
					<md-content ng-cloak>
					
					
					<div layout="row" layout-wrap>
								<h3>{{translate.load("sbi.importusers.import")}}</h3>
							</div>
							<div layout="row" layout-wrap >
								<file-upload flex id="AssociationFileUploadImport" ng-model="importFile"></file-upload>
								<md-button ng-click="upload($event)" aria-label="upload Menu"
										class="md-fab md-mini"  > <md-icon
										md-font-icon="fa fa-upload"  >
							</div>
							<div layout="row" layout-wrap >
								<md-radio-group layout="row" ng-model="typeSaveMenu">
								      <md-radio-button value="Override" ng-click="reloadTree()">{{translate.load("sbi.importusers.override");}}</md-radio-button>
								      <md-radio-button value="Missing" ng-click="reloadTree()">{{translate.load("sbi.importusers.addmissing");}} </md-radio-button>
								    </md-radio-group>
								
								<span flex></span>
									<md-button class="md-raised" ng-click="save($event)" aria-label="upload Menu" >{{translate.load("sbi.importusers.startimport");}}</md-button>
							</div>
				
					<div layout="column" layout-fill>
						<md-checkbox ng-show="exportedDataset.length>0"
							ng-checked="exists('DatasetImported',listTypeImported)"
							ng-click="toggle('DatasetImported',listTypeImported)">{{translate.load("sbi.importexportcatalog.radiodataset");}}</md-checkbox>
						<h4 ng-show="showDatasetImported">{{translate.load("sbi.importexportcatalog.messagesfederated");}}</h4>
						<div flex  ng-show="showDatasetImported">
							<angular-table id='datasetlistImported' ng-model=exportedDataset
								columns='[{"label":"Label","name":"label","size":"20px"},{"label":"Name","name":"name","size":"20px"},{"label":"Type","name":"type","size":"20px"}]'
								columnsSearch='["label"]' show-search-bar=true
								highlights-selected-item=true multi-select=true
								selected-item=datasetSelected no-pagination=true
								scope-functions=tableFunction> 
							</angular-table>
						</div>
					</div>
					</md-content></md-card>
					</md-tab-body>
			</md-tab>

		</md-tabs>
	</md-content>
	</div>
</body>
</html>
