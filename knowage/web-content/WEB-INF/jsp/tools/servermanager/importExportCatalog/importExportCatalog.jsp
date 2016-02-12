<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="impExpDataset">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<!-- non c'entra	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/glossary/commons/LayerTree.js"></script> -->
<link rel="stylesheet" type="text/css"
	href="/knowage/themes/glossary/css/tree-style.css">
<link rel="stylesheet" type="text/css"
	href="/knowage/themes/glossary/css/generalStyle.css">


<link rel="stylesheet" type="text/css"	href="/knowage/themes/catalogue/css/catalogue.css">
<!-- controller -->
<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/servermanager/importExportCatalogController.js"></script>




</head>
<body class="bodyStyle">

<div ng-controller="Controller " layout="column" layout-wrap layout-fill>

	
	<md-toolbar  class="miniheadimportexport">
		<div class="md-toolbar-tools">
			<i class="fa fa-exchange fa-2x"></i>
			<h2 class="md-flex" >{{translate.load("sbi.importexportcatalog");}}</h2>
		</div>
	</md-toolbar>
	<md-content layout="column" layout-wrap flex>
		<md-tabs md-select="ImportExport" flex
					 md-border-bottom> 
				<md-tab label="Export" md-on-select="setTab('Export')"
					md-active="isSelectedTab('Export')">
					<md-tab-body>
					<md-content layout-fill layout="column" layout-wrap>
					<div layout="row" layout-wrap>
					<div flex=35>
						<md-input-container class="small counter"> <label>{{translate.load("sbi.impexpusers.nameexport")}}</label>
								<input class="input_class" ng-model="nameExport"
									required maxlength="100" ng-maxlength="100" md-maxlength="100" />
									
						</md-input-container>
					</div>
					<div flex =10 >
						<md-input-container class="small counter"> 
							
							<md-button ng-show="!wait" ng-click="prepare($event)" aria-label="download Dataset"
								class="md-fab md-mini"  > <md-icon
								md-font-icon="fa fa-download"  >
							</md-icon> </md-button>
							<div ng-show="wait">
							<i  class="fa fa-spinner fa-spin fa-4x"></i>
							</div>
							
							<!--  <md-progress-circular ng-show="wait" md-mode="indeterminate"></md-progress-circular>-->
						</md-input-container>
					</div>
					</div>			
					<md-checkbox  ng-checked="exists('Dataset',listType)" ng-click="toggle('Dataset',listType)">{{translate.load("sbi.importexportcatalog.radiodataset");}}</md-checkbox>
					<div flex style="position: relative;" ng-show="showDataset">
						<angular-table id='datasetlist' 
							ng-model=dataset
							columns='[{"label":"Label","name":"label","size":"20px"},{"label":"Name","name":"name","size":"20px"}]'
							columnsSearch='["label"]' 
							show-search-bar=true
							highlights-selected-item=true 
							multi-select=true
							selected-item=datasetSelected
							no-pagination=true
							scope-functions=tableFunction>
						</angular-table> 
						
					</div>

					</md-content>
					</md-tab-body>
				</md-tab>
				<md-tab	label="Import" md-on-select="setTab('Import')"
					md-active="isSelectedTab('Import')">
					<div  layout="column" layout-wrap>
					<div layout="row" layout-wrap>
						<div flex = 15 >
							<h3>{{translate.load("sbi.importusers.import");}}</h3>
						</div>
						
						<div flex=20 >
							<file-upload flex id="AssociationFileUploadImport" ng-model="importFile"></file-upload>
						</div>
						
						<div flex =10 >
						<md-input-container class="small counter"> 
							<md-button ng-click="upload($event)" aria-label="upload Catalog"
								class="md-fab md-mini"  > <md-icon
								md-font-icon="fa fa-upload fa-2x"  >
							</md-icon> </md-button>
						</md-input-container>
						</div>
						<span flex=20></span>
						<div flex =20>
								
									<md-radio-group layout="row" ng-model="typeSaveMenu">
								      <md-radio-button value="Override">{{translate.load("sbi.importusers.override");}}</md-radio-button>
								      <md-radio-button value="Missing" >{{translate.load("sbi.importusers.addmissing");}} </md-radio-button>
								    </md-radio-group>
								 
								</div>
						<md-input-container class="small counter"> 
							<md-button ng-click="save($event)" aria-label="upload Users" >{{translate.load("sbi.importusers.startimport");}}</md-button>
						</md-input-container>
					</div>
					<div layout="column" layout-fill>
						<md-checkbox  ng-show="exportedDataset.length>0" ng-checked="exists('Dataset',listType)" ng-click="toggle('Dataset',listType)">{{translate.load("sbi.importexportcatalog.radiodataset");}}</md-checkbox>
							<div flex style="position: relative;" ng-show="showDataset">
								<angular-table id='datasetlistImported' 
									ng-model=exportedDataset
									columns='[{"label":"Label","name":"label","size":"20px"},{"label":"Name","name":"name","size":"20px"}]'
									columnsSearch='["label"]' 
									show-search-bar=true
									highlights-selected-item=true 
									multi-select=true
									selected-item=datasetSelected
									no-pagination=true
									scope-functions=tableFunction>
								</angular-table> 
							</div>
							
						
					</div>
					</div>
					</div>
				</md-tab>
				
			</md-tabs>
			</md-content>
			
			</div>
		
</body>


</html>