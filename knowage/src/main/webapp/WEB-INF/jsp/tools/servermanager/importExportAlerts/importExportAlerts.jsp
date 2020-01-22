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
<html ng-app="impExpAlerts">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/importexport/css/importExportStyle.css")%>">
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">
<!-- controller -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/alertImportExport/importExportAlertsController.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/documentImportExport/importExportDocumentsController.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/alertImportExport/importAlertsStep0Controller.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/alertImportExport/importAlertsStep1Controller.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/alertImportExport/importAlertsStep2Controller.js")%>"></script>
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/importexport/css/importExportStyle.css")%>">
<%-- 	<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/generalStyle.css")%>"> --%>

<!-- 	breadCrumb -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/commons/BreadCrumb.js")%>"></script>


</head>
<body class="bodyStyle kn-importExportDocument">
<rest-loading></rest-loading>

	<div ng-controller="alertImportExportController" layout="column" layout-wrap layout-fill>
		<md-toolbar class="miniheadimportexport">
			<div class="md-toolbar-tools">
				<i class="fa fa-exchange fa-2x""></i>
				<h2 class="md-flex">{{translate.load("sbi.impexpalerts");}}</h2>
			</div>
		</md-toolbar>
		<md-content layout="column" class="mainContainer" layout-wrap flex>
		<md-tabs layout-fill class="absolute">
			<!----- EXPORT ------------------------------------------------>
			<md-tab id="alertExportTab">
				<md-tab-label>{{translate.load("SBISet.export","component_impexp_messages");}}</md-tab-label>
				<md-tab-body>
				<md-card>
				<md-card-content layout="column" layout-wrap ng-controller="alertExportController">
					<div layout="row" layout-wrap layout-align="center center">
						<div flex>
							<md-input-container class="md-block">
								<label>{{translate.load("sbi.impexpkpis.nameexport")}}</label>
								<input class="input_class" ng-model="nameExport" required maxlength="100" ng-maxlength="100" md-maxlength="100" />
							</md-input-container>
						</div>
						<div>
							<md-input-container class="md-block">
								<md-button ng-show="!wait" ng-click="prepare($event)" aria-label="download Alerts" class="md-fab md-mini">
									<md-icon md-font-icon="fa fa-download fa-2x"> </md-icon>
								</md-button>
							
							</md-input-container>
						</div>
					</div>
		
<!-- 					<div layout-padding layout-gt-sm="row" -->
<!-- 								layout-align-gt-sm="start center" layout-sm="column" -->
<!-- 								layuout-align-sm="start start"> -->
<!-- 							<md-checkbox class="little-check"   ng-model="targetsAndRelatedKpis" aria-label="Include targets and related KPIs">Include targets and related KPIs</md-checkbox> -->
<!-- 							<md-checkbox class="little-check"  ng-model="scorecardsAndRelatedKpis" aria-label="Include scorecards and related KPIs">Include scoreacards and related KPIs</md-checkbox> -->
<!-- 							<md-checkbox class="little-check"   ng-model="schedulersAndRelatedKpis" aria-label="Include KPI schedulers and related KPIs">Include KPI schedulers and related KPIs</md-checkbox> -->
<!-- 					</div> -->
						<div layout="row" flex>
			
					<div id="lista">
						<div layout="row" layout-wrap>
							<div>
								<md-checkbox ng-checked="flagCheck" ng-click="selectAll()">
									<h4>{{translate.load("sbi.importkpis.selectall");}}</h4>
								</md-checkbox>
							</div>
						</div>
						<div layout="row" layout-wrap flex>
							<div flex="90" ng-repeat="alert in alerts">
								<md-checkbox ng-checked="exists(alert, alertsSelected)" ng-click="toggle(alert, alertsSelected)"> {{ alert.name }} </md-checkbox>
							</div>
						</div>
					</div>
				</md-card-content>
				</md-card>
				</md-tab-body>
			</md-tab>
			
			<!----- IMPORT ------------------------------------------------>
			<md-tab id="alertImportTab">
				<md-tab-label>{{translate.load("SBISet.import","component_impexp_messages");}}</md-tab-label>
				<md-tab-body>
				<md-card ng-controller="alertImportController">
					<md-toolbar class="ternaryToolbar" flex="nogrow">
			            <div class="md-toolbar-tools noPadding" layout="row">
							<bread-crumb flex ng-model='stepItem' item-name='name' selected-index='selectedStep' control='stepControl'>
							</bread-crumb>
						</div>
					</md-toolbar>
				<md-card-content  layout-wrap  ng-cloak ng-switch="selectedStep">
					<div class="importSteps" flex ng-controller="importAlertControllerStep0" ng-switch-when="0">
						<%@include file="./importAlertsSteps/importAlertsStep0.jsp"%>
					</div>
					
					<div class="importSteps" flex ng-controller="importAlertControllerStep1" ng-switch-when="1">
						<%@include file="./importAlertsSteps/importAlertsStep1.jsp"%>
					</div>
					<div class="importSteps" flex ng-controller="importAlertControllerStep2" ng-switch-when="2">
						<%@include file="./importAlertsSteps/importAlertsStep2.jsp"%>
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
