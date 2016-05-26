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
<html ng-app="impExpKpis">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
 
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/importexport/css/importExportStyle.css">
<link rel="stylesheet" type="text/css"	href="/knowage/themes/commons/css/customStyle.css"> 
<!-- controller -->
<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/servermanager/kpiImportExport/importExportKpisController.js"></script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/documentImportExport/importExportDocumentsController.js"></script>

<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/kpiImportExport/importKpisStep0Controller.js"></script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/kpiImportExport/importKpisStep1Controller.js"></script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/kpiImportExport/importKpisStep2Controller.js"></script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/documentImportExport/importDocumentsStep1Controller.js">
	</script><script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/documentImportExport/importDocumentsStep2Controller.js"></script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/documentImportExport/importDocumentsStep3Controller.js"></script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/documentImportExport/importDocumentsStep4Controller.js"></script>
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/themes/importexport/css/importExportStyle.css">
<%-- 	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/commons/css/generalStyle.css"> --%>

<!-- 	breadCrumb -->
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/BreadCrumb.js"></script>
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/themes/glossary/css/bread-crumb.css">




</head>
<body class="bodyStyle kn-importExportDocument">

<div ng-controller="kpiImportExportController" layout="column" layout-wrap layout-fill>

	
	<md-toolbar  class="miniheadimportexport">
		<div class="md-toolbar-tools">
			<i class="fa fa-exchange fa-2x""></i>
			<h2 class="md-flex" >{{translate.load("sbi.impexpkpis");}}</h2>
		</div>
	</md-toolbar>
	<md-content layout="column" class="mainContainer" layout-wrap flex>
		<md-tabs  layout-fill class="absolute"> 
			<md-tab  id="kpiExportTab" >
			<md-tab-label>{{translate.load("SBISet.export","component_impexp_messages");}}</md-tab-label>
				<md-tab-body> 
				<md-card>
				<md-content layout="column" layout-wrap ng-controller="kpiExportController" >
		<div layout="row" layout-wrap >
			<div flex >
				<md-input-container class="small counter"> <label>{{translate.load("sbi.impexpkpis.nameexport")}}</label>
				<input class="input_class" ng-model="nameExport" required
					maxlength="100" ng-maxlength="100" md-maxlength="100" /> </md-input-container>
			</div>
			
				<!--md-checkbox ng-model="exportCheckboxs.exportPersonalFolder"
					aria-label="Checkbox 1"><h4>{{translate.load("sbi.impexpkpis.exportPersonalFolder");}}</h4></md-checkbox>
			
			
				<md-checkbox ng-if="exportCheckboxs.exportPersonalFolder"
					ng-model="exportCheckboxs.exportSubObj" aria-label="Checkbox 1"><h4>{{translate.load("SBISet.importexport.expSubView","component_impexp_messages");}}</h4></md-checkbox>
			
			
				<md-checkbox  ng-if="exportCheckboxs.exportPersonalFolder"
					ng-model="exportCheckboxs.exportSnapshots" aria-label="Checkbox 1"><h4>{{translate.load("SBISet.importexport.expSnapshots","component_impexp_messages");}}</h4></md-checkbox-->
			
			<div>
				<md-input-container class="small counter"> <md-button
					ng-show="!wait" ng-click="prepare($event)"
					aria-label="download KPIs" class="md-fab md-mini"> <md-icon
					md-font-icon="fa fa-download fa-2x"> </md-icon> </md-button>
				<div ng-show="wait">
					<i class="fa fa-spinner fa-spin fa-4x"></i>
				</div>

				<!--  <md-progress-circular ng-show="wait" md-mode="indeterminate"></md-progress-circular>-->
				</md-input-container>
			</div>
		</div>

		<div id="lista">
						<div layout="row" layout-wrap>
							<div >
							<md-checkbox  ng-checked="flagCheck" ng-click="selectAll()"><h4>{{translate.load("sbi.importkpis.selectall");}}</h4></md-checkbox>
							
							</div>
						</div>
						<div layout="row" layout-wrap flex>
							<div flex="90" ng-repeat="us in kpis">
								<md-checkbox ng-checked="exists(us, kpisSelected)"
									ng-click="toggle(us, kpisSelected)"> {{ us.name }} </md-checkbox>
		
							</div>
						</div>
					</div>
			
					</md-content>
					</md-card>
					</md-tab-body>
					
				</md-tab>
				
				<md-tab	id="kpiImportTab"  >
					<md-tab-label>{{translate.load("SBISet.import","component_impexp_messages");}}</md-tab-label>
					<md-tab-body> 
					<md-card>
					<md-content ng-controller="kpiImportController"  layout-wrap layout-fill ng-cloak ng-switch="selectedStep">
						<bread-crumb ng-model=stepItem item-name='name' selected-index='selectedStep' control='stepControl'>
						</bread-crumb>

						<div class="importSteps" flex ng-controller="importKpiControllerStep0" ng-switch-when="0"><%@include	file="./importKpisSteps/importKpisStep0.jsp"%></div>
						<div class="importSteps" flex ng-controller="importKpiControllerStep1" ng-switch-when="1"><%@include	file="./importKpisSteps/importKpisStep1.jsp"%></div>
<%-- 						<div class="importSteps" flex ng-controller="importKpiControllerStep2" ng-switch-when="2"><%@include	file="./importKpisSteps/importKpisStep2.jsp"%></div> --%>
						<div class="importSteps" flex ng-controller="importControllerStep1" ng-switch-when="2" ng-init="importType='kpi'"><%@include	file="../importExportDocuments/importDocumentsSteps/importDocumentsStep1.jsp"%></div>
						<div class="importSteps" flex ng-controller="importControllerStep2" ng-switch-when="3"><%@include	file="../importExportDocuments/importDocumentsSteps/importDocumentsStep2.jsp"%></div>
						<div class="importSteps" flex ng-controller="importControllerStep3" ng-switch-when="4"><%@include	file="../importExportDocuments/importDocumentsSteps/importDocumentsStep3.jsp"%></div>
						<div class="importSteps" flex ng-controller="importControllerStep4" ng-switch-when="5" ng-init="importType='kpi'"><%@include	file="../importExportDocuments/importDocumentsSteps/importDocumentsStep4.jsp"%></div>


					</md-content>
					</md-card>
				</md-tab-body> 
				</md-tab>
				
			</md-tabs>
			</md-content>
			
	</div>
		
</body>


</html>
