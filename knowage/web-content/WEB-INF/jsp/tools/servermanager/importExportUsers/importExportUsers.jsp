<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="impExpUsers">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
 
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/importexport/css/importExportStyle.css">
<link rel="stylesheet" type="text/css"	href="/knowage/themes/commons/css/customStyle.css"> 
<!-- controller -->
<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/servermanager/userImportExport/importExportUsersController.js"></script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/documentImportExport/importExportDocumentsController.js"></script>

<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/userImportExport/importUsersStep0Controller.js"></script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/userImportExport/importUsersStep1Controller.js"></script>
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
<body class="bodyStyle">

<div ng-controller="userImportExportController" layout="column" layout-wrap layout-fill>

	
	<md-toolbar  class="miniheadimportexport">
		<div class="md-toolbar-tools">
			<i class="fa fa-exchange md-padding"></i>
			<h2 class="md-flex" >{{translate.load("sbi.impexpusers");}}</h2>
		</div>
	</md-toolbar>
	<md-content layout="column" layout-wrap flex>
		<md-tabs  layout-fill class="absolute"> 
			<md-tab  id="userExportTab" >
			<md-tab-label>{{translate.load("SBISet.export","component_impexp_messages");}}</md-tab-label>
				<md-tab-body> 
				<md-content layout="column" layout-wrap ng-controller="userExportController" >
					<div layout="row" layout-wrap>
						<div flex>
							<md-input-container class="small counter"> <label>{{translate.load("sbi.impexpusers.nameexport")}}</label>
									<input class="input_class" ng-model="nameExport" required maxlength="100" ng-maxlength="100" md-maxlength="100" />
							</md-input-container>
						</div>
					 
					 <div layout="row" layout-wrap >
						<md-checkbox   style="line-height: 61px;"  ng-model="exportCheckboxs.exportPersonalFolder" aria-label="Checkbox 1">{{translate.load("sbi.impexpusers.exportPersonalFolder")}}</md-checkbox>
						<md-checkbox ng-if="exportCheckboxs.exportPersonalFolder"  style="line-height: 61px;"  ng-model="exportCheckboxs.exportSubObj" aria-label="Checkbox 1">{{translate.load("SBISet.importexport.expSubView","component_impexp_messages");}}</md-checkbox>
						<md-checkbox ng-if="exportCheckboxs.exportPersonalFolder"  style="line-height: 61px;"  ng-model="exportCheckboxs.exportSnapshots" aria-label="Checkbox 1">{{translate.load("SBISet.importexport.expSnapshots","component_impexp_messages");}}</md-checkbox>
					 </div>
						<div>
							<md-input-container class="small counter"> 
								
								<md-button ng-show="!wait" ng-click="prepare($event)" aria-label="download Users"
									class="md-fab md-mini"  > <md-icon
									md-font-icon="fa fa-download fa-2x"  >
								</md-icon> </md-button>
								<div ng-show="wait">
								<i  class="fa fa-spinner fa-spin fa-4x"></i>
								</div>
								
								<!--  <md-progress-circular ng-show="wait" md-mode="indeterminate"></md-progress-circular>-->
							</md-input-container>
						</div>
					</div>
					 
					<div id="lista" style="background:#eceff1">
						<div layout="row" layout-wrap>
							<div >
							<md-checkbox  ng-checked="flagCheck" ng-click="selectAll()">{{translate.load("sbi.importusers.selectall");}}</md-checkbox>
							</div>
						</div>
						<div layout="row" layout-wrap flex>
							<div flex="90" ng-repeat="us in users">
								<md-checkbox ng-checked="exists(us, usersSelected)"
									ng-click="toggle(us, usersSelected)"> {{ us.userId }} </md-checkbox>
		
							</div>
						</div>
					</div>
			
					</md-content>
					</md-tab-body>
					
				</md-tab>
				
				<md-tab	id="userImportTab"  >
					<md-tab-label>{{translate.load("SBISet.import","component_impexp_messages");}}</md-tab-label>
					<md-tab-body> 
					<md-content ng-controller="userImportController" layout="column" layout-wrap layout-fill ng-cloak ng-switch="selectedStep">
						<bread-crumb ng-model=stepItem item-name='name' selected-index='selectedStep' control='stepControl'>
						</bread-crumb>

						<div class="importSteps" flex ng-controller="importUserControllerStep0" ng-switch-when="0"><%@include	file="./importUsersSteps/importUsersStep0.jsp"%></div>
						<div class="importSteps" flex ng-controller="importUserControllerStep1" ng-switch-when="1"><%@include	file="./importUsersSteps/importUsersStep1.jsp"%></div>
						<div class="importSteps" flex ng-controller="importControllerStep2" ng-switch-when="2"><%@include	file="../importExportDocuments/importDocumentsSteps/importDocumentsStep2.jsp"%></div>
						<div class="importSteps" flex ng-controller="importControllerStep3" ng-switch-when="3"><%@include	file="../importExportDocuments/importDocumentsSteps/importDocumentsStep3.jsp"%></div>
						<div class="importSteps" flex ng-controller="importControllerStep4" ng-switch-when="4" ng-init="importType='user'"><%@include	file="../importExportDocuments/importDocumentsSteps/importDocumentsStep4.jsp"%></div>


					</md-content>
				</md-tab-body> 
				</md-tab>
				
			</md-tabs>
			</md-content>
			
	</div>
		
</body>


</html>