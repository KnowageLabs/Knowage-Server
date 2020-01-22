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
<html ng-app="impExpMetadata">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/importexport/css/importExportStyle.css")%>">
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">
<!-- controller -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/metadataImportExport/importExportMetadataController.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/documentImportExport/importExportDocumentsController.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/metadataImportExport/importMetadataStep0Controller.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/metadataImportExport/importMetadataStep1Controller.js")%>"></script>
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/importexport/css/importExportStyle.css")%>">
<%-- 	<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/generalStyle.css")%>"> --%>

<!-- 	breadCrumb -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/commons/BreadCrumb.js")%>"></script>


</head>
<body class="bodyStyle kn-importExportDocument">
<rest-loading></rest-loading>
	<div ng-controller="metadataImportExportController" layout="column" layout-wrap layout-fill>
		<md-toolbar class="miniheadimportexport">
			<div class="md-toolbar-tools">
				<i class="fa fa-exchange fa-2x""></i>
				<h2 class="md-flex">{{translate.load("sbi.impexpmetadata");}}</h2>
			</div>
		</md-toolbar>
		<md-content layout="column" class="mainContainer" layout-wrap flex>
		<md-tabs layout-fill class="absolute">
			<!----- EXPORT ------------------------------------------------>
			<md-tab id="metadataExportTab">
				<md-tab-label>{{translate.load("SBISet.export","component_impexp_messages");}}</md-tab-label>
				<md-tab-body>
				<md-card>
				<md-card-content layout="column" layout-wrap ng-controller="metadataExportController">
					<div layout="row" layout-wrap layout-align="center center">
						<div flex>
							<md-input-container class="md-block">
								<label>{{translate.load("sbi.impexpmetadata.nameexport")}}</label>
								<input class="input_class" ng-model="nameExport" required maxlength="100" ng-maxlength="100" md-maxlength="100" />
							</md-input-container>
						</div>
						<div>
							<md-input-container class="md-block">
								<md-button ng-show="!wait" ng-click="prepare($event)" aria-label="download Metadata" class="md-fab md-mini">
									<md-icon md-font-icon="fa fa-download fa-2x"> </md-icon>
								</md-button>
						
							</md-input-container>
						</div>
					</div>
					<!--div id="lista">
						<div layout="row" layout-wrap>
							<div>
								<md-checkbox ng-checked="flagCheck" ng-click="selectAll()">
									<h4>{{translate.load("sbi.importmetadata.selectall");}}</h4>
								</md-checkbox>
							</div>
						</div>
						<div layout="row" layout-wrap flex>
							<div flex="90" ng-repeat="metadata in metadatas">
								<md-checkbox ng-checked="exists(metadata, metadatasSelected)" ng-click="toggle(metadata, metadatasSelected)"> {{ metadata.name }} </md-checkbox>
							</div>
						</div>
					</div-->
				</md-card-content>
				</md-card>
				</md-tab-body>
			</md-tab>
			
			<!----- IMPORT ------------------------------------------------>
			<md-tab id="metadataImportTab">
				<md-tab-label>{{translate.load("SBISet.import","component_impexp_messages");}}</md-tab-label>
				<md-tab-body>
				<md-card ng-controller="metadataImportController">
				<bread-crumb ng-model=stepItem item-name='name' selected-index='selectedStep' control='stepControl'></bread-crumb>
				<md-card-content  layout-wrap  ng-cloak ng-switch="selectedStep">
					<div class="importSteps" flex ng-controller="importMetadataControllerStep0" ng-switch-when="0">
						<%@include file="./importMetadataSteps/importMetadataStep0.jsp"%>
					</div>
					<div class="importSteps" flex ng-controller="importMetadataControllerStep1" ng-switch-when="1">
						<%@include file="./importMetadataSteps/importMetadataStep1.jsp"%>
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
