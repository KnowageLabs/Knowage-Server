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
<html ng-app="kpiTarget">

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Target definition</title>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLinkByTheme(request, "/css/angularjs/kpi/targetDefinition.css", currTheme)%>">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/targetDefinitionController.js"></script>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/theme/eclipse.css">  
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.js"></script>  
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/codemirror/ui-codemirror.js"></script> 
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/mathematicaModified.js"></script>  
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/targetDefinitionSubController/listController.js"></script>

<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.css" />
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/sql-hint.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/mode/clike/clike.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/selection/mark-selection.js"></script>
<script type="text/ng-template" id="templatesaveTarget.html">
<md-dialog aria-label="Select Function" ng-cloak>
	<form>
		<md-toolbar>
			<div class="md-toolbar-tools">
				<h1>{{translate.load("sbi.target.save")}}</h1>
				<span flex></span>
				<md-button class="md-icon-button" ng-click="close()">
					<md-icon md-font-icon="fa fa-times closeIcon" aria-label="translate.load('sbi.general.close.dialog')"></md-icon>
				</md-button>
			</div>
		</md-toolbar>
		<md-dialog-content>
			<div class="md-dialog-content">
				<md-autocomplete 
						ng-disabled="false" 
						md-selected-item="targetCategory" 
						md-search-text="searchText" 
						md-items="item in querySearchCategory(searchText)"
						md-item-text="item.valueCd" 
						md-floating-label={{translate.load('sbi.generic.category')}}
						md-autoselect="true">
					<md-item-template>
						<span md-highlight-text="searchText">{{item.valueCd}}</span>
					</md-item-template>
				</md-autocomplete>
			</div>
			<div class="footer">
				<md-button class="dialogButton" ng-click="apply()" md-autofocus>
				{{translate.load('sbi.generic.apply')}}<md-icon md-font-icon="fa fa-check buttonIcon" aria-label={{translate.load('sbi.generic.apply')}}></md-icon>
				</md-button>
			</div>
		</md-dialog-content>
	</form>
</md-dialog>
</script>

</head>
<body>
	<angular-list-detail ng-controller="targetDefinitionController" full-screen="true">
		<list label="translate.load('sbi.target.list')" ng-controller="listController" new-function="addTarget">
			<angular-table  flex
				id='targetListTable' ng-model=targets
				columns='targetsColumns'
				columnsSearch='["name"]'
				show-search-bar="true"
				speed-menu-option="targetsActions"
				click-function="loadTarget(item);"> </angular-table>
		</list>
		<detail label="getLabelForBar()" save-function="showSaveTargetDialog" cancel-function="cancel">
			<div id="contentWhiteFrame" class="overflow" flex layout="column">
				<md-whiteframe class="md-whiteframe-4dp layout-padding" layout-margin>
					<div>
						<div>
							<md-input-container class="small counter" class="small counter">
							<label>{{translate.load("sbi.behavioural.lov.details.name")}}</label>
							<input class="input_class" ng-model="target.name" required
								maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
						</div>
						<div>
							<label>{{translate.load("sbi.target.startvalidity")}}</label>
							<md-datepicker ng-model="target.startValidityDate" name="Select Data"
								ng-change="parseDate()" md-placeholder={{translate.load("sbi.target.selectdate");}} ></md-datepicker>
							<label>{{translate.load("sbi.target.endvalidity")}}</label>
							<md-datepicker ng-model="target.endValidityDate" name="Select Data"
								ng-change="parseDate()" md-placeholder={{translate.load("sbi.target.selectdate");}} ></md-datepicker>
						</div>
					</div>
				</md-whiteframe>
				<md-whiteframe class="md-whiteframe-4dp layout-padding" layout-margin layout="column" flex>
					<md-toolbar>
						<div class="md-toolbar-tools" layout="row" class="headerNote">
							<h1>{{translate.load("sbi.target.headerkpi")}}</h1>
						</div>
					</md-toolbar>
					<angular-table flex 
						id="kpisTable" ng-model="kpis"
						columns='[{"label":"KPI name","name":"name"},{"label":"Value","name":"value", "editable": "true"}]'
						columnsSearch='["name"]'
						speed-menu-option="kpisActions"
						show-search-bar="false"
						no-pagination="true"
						allow-edit="true"
						scope-functions="kpisFunctions"
						click-function="alert(item);">
						<queue-table>
							<div layout="row"> 
								<span flex></span>
								<md-button type="button" id="add-kpi" ng-click="scopeFunctions.openShowDialog($event);">{{scopeFunctions.translate.load("sbi.kpi.addkpiassociation.single")}}</md-button>
							</div>
						</queue-table> 
					</angular-table>
				</md-whiteframe>
			</div>
			</detail>
		</angular-list-detail>
</body>
</html>