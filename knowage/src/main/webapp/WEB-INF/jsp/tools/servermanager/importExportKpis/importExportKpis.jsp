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
<html ng-app="impExpKpis">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/importexport/css/importExportStyle.css")%>">
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">
<!-- controller -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/kpiImportExport/importExportKpisController.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/documentImportExport/importExportDocumentsController.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/kpiImportExport/importKpisStep0Controller.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/kpiImportExport/importKpisStep2Controller.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/kpiImportExport/importKpisStep1Controller.js")%>"></script>
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/importexport/css/importExportStyle.css")%>">
<%-- 	<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/generalStyle.css")%>"> --%>

<!-- 	breadCrumb -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/commons/BreadCrumb.js")%>"></script>


</head>
<body class="bodyStyle kn-importExportDocument">
<rest-loading></rest-loading>

	<div ng-controller="kpiImportExportController" layout="column" layout-wrap layout-fill>
		<md-toolbar class="miniheadimportexport">
			<div class="md-toolbar-tools">
				<i class="fa fa-exchange fa-2x""></i>
				<h2 class="md-flex">{{translate.load("sbi.impexpkpis");}}</h2>
			</div>
		</md-toolbar>
		<md-content layout="column" class="mainContainer" layout-wrap flex>
		<md-tabs layout-fill class="absolute">
			<!----- EXPORT ------------------------------------------------>
			<md-tab id="kpiExportTab">
				<md-tab-label>{{translate.load("SBISet.export","component_impexp_messages");}}</md-tab-label>
				<md-tab-body>
				<md-card>
				<md-card-content layout="column" layout-wrap ng-controller="kpiExportController">
					<div layout="row" layout-wrap layout-align="center center">
						<div flex>
							<md-input-container class="md-block">
								<label>{{translate.load("sbi.impexpkpis.nameexport")}}</label>
								<input class="input_class" ng-model="nameExport" required maxlength="100" ng-maxlength="100" md-maxlength="100" />
							</md-input-container>
						</div>
						<div>
							<md-input-container class="md-block">
								<md-button ng-show="!wait" ng-click="prepare($event)" aria-label="download KPIs" class="md-fab md-mini">
									<md-icon md-font-icon="fa fa-download fa-2x"> </md-icon>
								</md-button>
							
							</md-input-container>
						</div>
					</div>
		
					<div layout-padding layout-gt-sm="row"
								layout-align-gt-sm="start center" layout-sm="column"
								layuout-align-sm="start start">
						
							<md-checkbox class="little-check"   ng-model="targetsAndRelatedKpis" aria-label="{{translate.load('sbi.importkpis.targetsAndRelatedKpis');}}">{{translate.load('sbi.importkpis.targetsAndRelatedKpis');}}</md-checkbox>
							<md-checkbox class="little-check"  ng-model="scorecardsAndRelatedKpis" aria-label="{{translate.load('sbi.importkpis.scorecardsAndRelatedKpis');}}">{{translate.load('sbi.importkpis.scorecardsAndRelatedKpis');}}</md-checkbox>
							<md-checkbox class="little-check"   ng-model="schedulersAndRelatedKpis" aria-label="{{translate.load('sbi.importkpis.schedulersAndRelatedKpis');}}">{{translate.load('sbi.importkpis.schedulersAndRelatedKpis');}}</md-checkbox>
					</div>
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
							<div flex="90" ng-repeat="kpi in kpis">
								<md-checkbox ng-checked="exists(kpi, kpisSelected)" ng-click="toggle(kpi, kpisSelected)"> {{ kpi.name }} </md-checkbox>
							</div>
						</div>
					</div>
				</md-card-content>
				</md-card>
				</md-tab-body>
			</md-tab>
			
			<!----- IMPORT ------------------------------------------------>
			<md-tab id="kpiImportTab">
				<md-tab-label>{{translate.load("SBISet.import","component_impexp_messages");}}</md-tab-label>
				<md-tab-body>
				<md-card ng-controller="kpiImportController">
				<md-toolbar class="ternaryToolbar" flex="nogrow">
		        	<div class="md-toolbar-tools noPadding" layout="row">
						<bread-crumb flex ng-model='stepItem' item-name='name' selected-index='selectedStep' control='stepControl'>
						</bread-crumb>
					</div>
				</md-toolbar>
				<md-card-content  layout-wrap  ng-cloak ng-switch="selectedStep">
					<div class="importSteps" flex ng-controller="importKpiControllerStep0" ng-switch-when="0">
						<%@include file="./importKpisSteps/importKpisStep0.jsp"%>
					</div>
					<div class="importSteps" flex ng-controller="importKpiControllerStep1" ng-switch-when="1">
						<%@include file="./importKpisSteps/importKpisStep1.jsp"%>
					</div>
					<div class="importSteps" flex ng-controller="importKpiControllerStep2" ng-switch-when="2">
						<%@include file="./importKpisSteps/importKpisStep12.jsp"%>
					</div>
						
					</md-card>
				</md-tab-body>
			</md-tab>
		</md-tabs>
		</md-content>
	</div>

</body>
</html>
