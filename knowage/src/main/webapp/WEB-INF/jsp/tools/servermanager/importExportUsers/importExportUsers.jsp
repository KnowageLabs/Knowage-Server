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
<html ng-app="impExpUsers">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
 
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/importexport/css/importExportStyle.css")%>">
<link rel="stylesheet" type="text/css"	href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>"> 
<!-- controller -->
<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/userImportExport/importExportUsersController.js")%>"></script>
<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/documentImportExport/importExportDocumentsController.js")%>"></script>

<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/userImportExport/importUsersStep0Controller.js")%>"></script>
<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/userImportExport/importUsersStep1Controller.js")%>"></script>
<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/userImportExport/importUsersStep2Controller.js")%>"></script>
<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/documentImportExport/importDocumentsStep1Controller.js")%>">
	</script><script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/documentImportExport/importDocumentsStep2Controller.js")%>"></script>
<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/documentImportExport/importDocumentsStep3Controller.js")%>"></script>
<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/documentImportExport/importDocumentsStep4Controller.js")%>"></script>
<link rel="stylesheet" type="text/css"
	href="<%=urlBuilder.getResourceLink(request, "themes/importexport/css/importExportStyle.css")%>">
<%-- 	<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/generalStyle.css")%>"> --%>

<!-- 	breadCrumb -->
<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/commons/BreadCrumb.js")%>"></script>




</head>
<body class="bodyStyle kn-importExportDocument">
<rest-loading></rest-loading>
<div ng-controller="userImportExportController" layout="column" layout-wrap layout-fill>

	
	<md-toolbar  class="miniheadimportexport">
		<div class="md-toolbar-tools">
			<i class="fa fa-exchange fa-2x""></i>
			<h2 class="md-flex" >{{translate.load("sbi.impexpusers");}}</h2>
		</div>
	</md-toolbar>
	<md-content layout="column" class="mainContainer" layout-wrap flex>
		<md-tabs  layout-fill class="absolute"> 
			<md-tab  id="userExportTab" >
			<md-tab-label>{{translate.load("SBISet.export","component_impexp_messages");}}</md-tab-label>
				<md-tab-body> 
				<md-card>
				<md-card-content layout="column" ng-controller="userExportController" >
					<div layout="row" layout-align="center center" layout-wrap>
						<div flex flex-sm="100" flex-xs="100">
							<md-input-container class="md-block"> 
								<label>{{translate.load("sbi.impexpusers.nameexport")}}</label>
								<input class="input_class" ng-model="nameExport" required
								maxlength="100" ng-maxlength="100" md-maxlength="100" /> 
							</md-input-container>
						</div>
						<div flex-sm="100" flex-xs="100" layout="row" layout-align="center center">
							<md-checkbox ng-model="exportCheckboxs.exportPersonalFolder"  class="md-block"
								aria-label="Checkbox 1"><h4>{{translate.load("sbi.impexpusers.exportPersonalFolder");}}</h4></md-checkbox>
						
						
							<md-checkbox ng-disabled="!exportCheckboxs.exportPersonalFolder"  class="md-block"
								ng-model="exportCheckboxs.exportSubObj" aria-label="Checkbox 1"><h4>{{translate.load("SBISet.importexport.expSubView","component_impexp_messages");}}</h4></md-checkbox>
						
						
							<md-checkbox  ng-disabled="!exportCheckboxs.exportPersonalFolder"  class="md-block"
								ng-model="exportCheckboxs.exportSnapshots" aria-label="Checkbox 1"><h4>{{translate.load("SBISet.importexport.expSnapshots","component_impexp_messages");}}</h4></md-checkbox>
						
			 				<md-button 
								ng-show="!wait" ng-click="prepare($event)"
								aria-label="download Users" class="md-fab md-mini internalFab"> <md-icon
								md-font-icon="fa fa-download fa-2x"> </md-icon> </md-button>
						
			
						</div>
					</div>
					<div layout-padding layout-gt-sm="row"	layout-align-gt-sm="start center" layout-sm="column">
						<h4>{{translate.load("sbi.impexpuser.filterusers");}}:</h4>
						<md-datepicker ng-model="filterDate" md-placeholder="Enter date"></md-datepicker>
						<md-button class="md-icon-button" ng-click="filterUsers()">
							<md-icon md-font-icon="fa fa-filter" aria-label="Filter"></md-icon>
						</md-button>
						<md-button class="md-icon-button" ng-click=removeFilter()>
							<md-icon md-font-icon="fa fa-times" aria-label="Remove Filter"></md-icon>
						</md-button>
					</div>
					<div id="lista">
							<div layout="row" layout-wrap>
									<div >
										<md-checkbox  ng-checked="flagCheck" ng-click="selectAll()"><h4>{{translate.load("sbi.importusers.selectall");}}</h4></md-checkbox>
										
									</div>
							</div>
							<div layout="row" layout-wrap flex>
									<div flex="90" ng-repeat="us in users">
										<md-checkbox ng-checked="exists(us, usersSelected)"
											ng-click="toggle(us, usersSelected)"> {{ us.userId }} </md-checkbox>
					
									</div>
							</div>
					</div>
						
				</md-card-content>
				</md-card>
				</md-tab-body>
					
				</md-tab>
				
				<md-tab	id="userImportTab"  >
					<md-tab-label>{{translate.load("SBISet.import","component_impexp_messages");}}</md-tab-label>
					<md-tab-body> 
					<md-card ng-controller="userImportController" ng-switch="selectedStep">
					
						<md-toolbar class="ternaryToolbar" flex="nogrow">
			                <div class="md-toolbar-tools noPadding" layout="row">
								<bread-crumb flex ng-model='stepItem' item-name='name' selected-index='selectedStep' control='stepControl'>
								</bread-crumb>
							</div>
						</md-toolbar>
					
					<md-card-content   layout-wrap ng-cloak >
						
						<div class="importSteps" flex ng-controller="importUserControllerStep0" ng-switch-when="0"><%@include	file="./importUsersSteps/importUsersStep0.jsp"%></div>
						<div class="importSteps" flex ng-controller="importUserControllerStep1" ng-switch-when="1"><%@include	file="./importUsersSteps/importUsersStep1.jsp"%></div>
<%-- 						<div class="importSteps" flex ng-controller="importUserControllerStep2" ng-switch-when="2"><%@include	file="./importUsersSteps/importUsersStep2.jsp"%></div> --%>
						<div class="importSteps" flex ng-controller="importControllerStep1" ng-switch-when="2" ng-init="importType='user'"><%@include	file="../importExportDocuments/importDocumentsSteps/importDocumentsStep1.jsp"%></div>
						<div class="importSteps" flex ng-controller="importControllerStep2" ng-switch-when="3"><%@include	file="../importExportDocuments/importDocumentsSteps/importDocumentsStep2.jsp"%></div>
						<div class="importSteps" flex ng-controller="importControllerStep3" ng-switch-when="4"><%@include	file="../importExportDocuments/importDocumentsSteps/importDocumentsStep3.jsp"%></div>
						<div class="importSteps" flex ng-controller="importControllerStep4" ng-switch-when="5" ng-init="importType='user'"><%@include	file="../importExportDocuments/importDocumentsSteps/importDocumentsStep4.jsp"%></div>

					</md-card-content>
					</md-card>
				</md-tab-body> 
				</md-tab>
				
			</md-tabs>
			</md-content>
			
	</div>
		
</body>


</html>
