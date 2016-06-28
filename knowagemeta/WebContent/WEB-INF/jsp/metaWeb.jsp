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

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jsp"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="metaManager">

<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<%
String datasourceId= request.getParameter("datasourceId");
%>

<script> 
var datasourceId='<%= datasourceId%>';
</script>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>meta Definition</title>
	
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/associatordirective/associatordirective.js"></script>
	<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/metaDefinitionController.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/metaModelCreationController.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/metaModelDefinitionController.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/dialogController/addBusinessModelDialogController.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/dialogController/addBusinessViewDialogController.js"></script>
</head>
<body ng-controller="metaDefinitionController" layout="column" ng-switch on="steps.current">
	<md-toolbar>
		<h1 class="md-toolbar-tools" layout="row">
			<span flex>{{translate.load("sbi.meta.definition")}}</span>
			 <md-button ng-click="closeMetaDefinition()">{{translate.load("sbi.general.close")}}</md-button>
			 <md-button ng-click="continueToMeta()" ng-if="steps.current==0">{{translate.load("sbi.general.continue")}}</md-button>
			 <md-button ng-click="gobackToMetaDefinition()" ng-if="steps.current==1">{{translate.load("sbi.generic.back")}}</md-button>
			 <md-button ng-click="saveModel()" ng-if="steps.current==1">{{translate.load("sbi.generic.update")}}</md-button>
		</h1>
	</md-toolbar>
	<md-content ng-controller="metaModelDefinitionController" flex layout ng-switch-when="0">
		<angular-table flex id='datasourceStructureListTable' ng-model=dataSourceStructure
				columns='datasourceStructureColumnsList'
				scope-functions='datasourceStructureScopeFunctions'
			 	show-search-bar=true no-pagination="true"
			 	sortable-column="['columnName']" >
			 	
			 	<queue-table>
				 	<md-divider ></md-divider>
			 		<div layout="row" layout-align="space-around center" >
				 		<md-checkbox aria-label='check all business' ng-checked='scopeFunctions.allPhysicalModelAreChecked()' ng-click='scopeFunctions.toggleAllPhysicalModel()'> {{scopeFunctions.translate.load("sbi.meta.model.physical.selectAll")}}</md-checkbox>
				 		<md-checkbox aria-label='check all business' ng-checked='scopeFunctions.allBusinessModelAreChecked()' ng-click='scopeFunctions.toggleAllBusinessModel()'>  {{scopeFunctions.translate.load("sbi.meta.model.business.selectAll")}}</md-checkbox>
			 		</div>
			 	</queue-table>
	 	 </angular-table>
	
	</md-content>
	
	<md-content ng-controller="metaModelCreationController" flex layout ng-switch-when="1">
		
		<md-tabs flex>
			<md-tab id="businessTab">
				<md-tab-label>{{translate.load("sbi.meta.model.business")}}</md-tab-label>
				<md-tab-body >
					<%@include	file="./metaWebTemplates/businessModelTab.jsp"%>
				</md-tab-body>
			</md-tab>
			
			<md-tab id="physicalTab">
				<md-tab-label>{{translate.load("sbi.meta.model.physical")}}</md-tab-label>
				<md-tab-body>
					<%@include	file="./metaWebTemplates/physicalModelTab.jsp"%>
				</md-tab-body>
			</md-tab>
		</md-tabs>

	</md-content>

</body>
</html>

<style>
angular-table#datasourceStructureListTable .centerCheckbox{
	margin-left: 50%;
}
angular-table#datasourceStructureListTable .centerHeadText{
    text-align: center;
}

.goldKey{
color: gold !important;
}

component-tree#bcmTree>div , component-tree#pmTree>div{
margin:20px;
}

md-input-container.md-knowage-theme .md-input[disabled], md-input-container.md-knowage-theme .md-input [disabled]{
	color: black;
}

md-tabs.md-knowage-theme .md-tab.md-active{
    font-weight: 500;
}

</style>