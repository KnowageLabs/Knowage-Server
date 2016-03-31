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
<html ng-app="scorecardManager">

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/scorecardKpiController.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/scorecardSubController/scorecardDefinitionController.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/scorecardSubController/scorecardPerspectiveDefinitionController.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/scorecardSubController/scorecardTargetDefinitionController.js"></script>

<!-- 	breadCrumb -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/BreadCrumb.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/glossary/css/bread-crumb.css">

<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/directive/kpiSemaphoreIndicator/kpiSemaphoreIndicator.js"></script>

</head>
<body class="kn-scorecardKpiDefinition" ng-clock>

	<angular-list-detail ng-controller="scorecardMasterController"  full-screen="true">
		
		<list label="translate.load('sbi.kpi.measure.list')" ng-controller="scorecardListController" new-function="newScorecardFunction" layout-column>
		 	<angular-table id='scorecardListTable' ng-model=scorecardList
				columns='scorecardColumnsList'
			 	 show-search-bar=true
				 click-function="scorecardClickFunction(item);" > </angular-table>
		</list>
		
		<extra-button>
		 <md-button ng-click="" ng-if="selectedStep.value==2" >Save Goal</md-button>
		 <md-button  ng-click="" ng-if="selectedStep.value==2" >Cancel</md-button>
		 
		 <md-button  ng-click="" ng-if="selectedStep.value==1" >Save Perspective</md-button>
		 <md-button  ng-click="" ng-if="selectedStep.value==1" >Cancel</md-button>
		</extra-button>
		
				
		<detail ng-controller="scorecardDetailController" save-function="saveScorecardFunction" show-save-button="selectedStep.value==0" ng-switch="selectedStep.value" layout="column" >
		 <md-whiteframe class="md-whiteframe-1dp" >
			<bread-crumb ng-model=stepItem item-name='name' selected-index='selectedStep.value' control='stepControl' disable-go-back="true"> </bread-crumb>
		 </md-whiteframe>
			<div ng-switch-when="0" layout="column" flex ng-controller="scorecardDefinitionController">
				<%@include	file="./scorecardTemplate/scorecardDefinitionTemplate.jsp"%>
			</div>
		
			<div ng-switch-when="1" layout="column" flex ng-controller="scorecardPerspectiveDefinitionController">
		<!-- 		<md-button class="md-raised" ng-click="addTarget();">Aggiungi obiettivo</md-button>   -->	
				<%@include	file="./scorecardTemplate/scorecardPerspectiveDefinitionTemplate.jsp"%>
			</div>
			
			<div ng-switch-when="2" layout="column" flex ng-controller="scorecardTargetDefinitionController">	
				<%@include	file="./scorecardTemplate/scorecardTargetDefinitionTemplate.jsp"%>
			</div>
		</detail>
		
	</angular-list-detail>
</body>
</html>
