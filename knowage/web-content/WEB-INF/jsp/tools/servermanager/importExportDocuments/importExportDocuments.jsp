<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/documentImportExport/importExportDocumentsController.js"></script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/documentImportExport/importDocumentsStep0Controller.js"></script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/documentImportExport/importDocumentsStep1Controller.js"></script>
<script type="text/javascript"
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
<body class="bodyStyle" ng-app="importExportDocumentModule" id="ng-app">
	<!-- TODO using correct message -->
	<div ng-controller="importExportController " layout="column"
		layout-fill layout-wrap class="contentdemoBasicUsage">

		<md-toolbar class="miniheadimportexport">
		<div class="md-toolbar-tools">
			<i class="fa fa-exchange md-padding"></i>
			<h2 class="md-flex">{{translate.load("SBISet.importexport","component_impexp_messages");}}</h2>
		</div>
		</md-toolbar>

		<md-content flex layout-wrap> <!-- 		md-center-tabs md-stretch-tabs="always" -->
		<md-tabs layout-fill class="absolute"> <!-- Export --> <md-tab
			id="exportTab"> <md-tab-label>{{translate.load("SBISet.export","component_impexp_messages");}}</md-tab-label>
		<md-tab-body> <md-content
			ng-controller="exportController">
		<div ng-if="flags.waitExport" layout="column" layout-padding
			layout-align="space-around center">
			<h3 class="md-subhead">{{translate.load("SBISet.importexport.opProg","component_impexp_messages");}}</h3>
			<i class="fa fa-spinner fa-spin fa-4x"></i>

		</div>
		<h3 ng-if="flags.viewDownload" class="md-body-2">
			<md-button ng-click="toggleViewDownload()"
				class="md-icon-button md-primary" aria-label="Settings">
			<md-icon md-font-icon="fa fa-times"></md-icon> </md-button>

			<span>{{translate.load("SBISet.importexport.opComplete","component_impexp_messages");}}</span>
			<md-button ng-click="downloadFile()">{{translate.load("Sbi.download","component_impexp_messages");}}
			{{downloadedFileName}}.zip</md-button>
			<br> <span>{{translate.load("SBISet.importexport.exportCompleteResourcesWarning","component_impexp_messages");}}</span>
		</h3>

		<div layout="column" layout-padding layout-wrap>
			<div layout="row" layout-wrap class="w100" >

				<md-input-container flex class="md-block"> <label>{{translate.load("SBISet.importexport.nameExp","component_impexp_messages");}}</label>
				<input type="text" ng-model="exportName" required> </md-input-container>

				<md-button class="md-fab md-mini"
					ng-click="exportFiles(selected)"
					ng-disabled="selected.length==0 || exportName===undefined || exportName.length == 0"
					aria-label="{{translate.load('SBISet.importexport.fileArchive','component_impexp_messages')}}">
				<md-icon class="fa fa-download fa-2x"></md-icon> 
				</md-button>

			</div>
			<div layout-padding layout-gt-sm="row"
				layout-align-gt-sm="start center" layout-sm="column"
				layuout-align-sm="start start">
				<md-checkbox class="little-check" ng-model="checkboxs.exportSubObj"
					aria-label="Export sub views">{{translate.load("SBISet.importexport.expSubView","component_impexp_messages");}}</md-checkbox>
				<md-checkbox class="little-check"
					ng-model="checkboxs.exportSnapshots" aria-label="Export snapshots">{{translate.load("SBISet.importexport.expSnapshots","component_impexp_messages");}}</md-checkbox>
			</div>
			<div layout-padding>
				<document-tree ng-model="folders" id="impExpTree" create-tree="true"
					selected-item="selected" multi-select="true" show-files="true">
				</document-tree>
			</div>
		</div>
		</md-content> </md-tab-body> </md-tab> <!-- Import --> <md-tab id="importTab"> <md-tab-label>{{translate.load("SBISet.import","component_impexp_messages");}}</md-tab-label>
		<md-tab-body> <md-content
			ng-controller="importController" layout="column" layout-wrap
			layout-fill ng-cloak ng-switch="selectedStep"> <bread-crumb
			ng-model=stepItem item-name='name' selected-index='selectedStep'
			control='stepControl'></bread-crumb>

		<div class="importSteps" flex ng-controller="importControllerStep0" ng-switch-when="0"><%@include	file="./importDocumentsSteps/importDocumentsStep0.jsp"%></div>
		<div class="importSteps" flex ng-controller="importControllerStep1" ng-switch-when="1"><%@include	file="./importDocumentsSteps/importDocumentsStep1.jsp"%></div>
		<div class="importSteps" flex ng-controller="importControllerStep2" ng-switch-when="2"><%@include	file="./importDocumentsSteps/importDocumentsStep2.jsp"%></div>
		<div class="importSteps" flex ng-controller="importControllerStep3" ng-switch-when="3"><%@include	file="./importDocumentsSteps/importDocumentsStep3.jsp"%></div>
		<div class="importSteps" flex ng-controller="importControllerStep4" ng-switch-when="4"><%@include	file="./importDocumentsSteps/importDocumentsStep4.jsp"%></div>


		</md-content> </md-tab-body> </md-tab> </md-tabs> </md-content>
	</div>
</body>
</html>