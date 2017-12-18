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
<html ng-app="schedulerKpi">

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Target definition</title>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLinkByTheme(request, "/css/angularjs/kpi/targetDefinition.css", currTheme)%>">
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLinkByTheme(request, "/css/angularjs/kpi/schedulerKpi.css", currTheme)%>">
<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/cronFrequency/cronFrequency.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/schedulerKpiController.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/schedulerKpiSubController/listSchedulerController.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/schedulerKpiSubController/kpiController.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/schedulerKpiSubController/filterController.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/schedulerKpiSubController/executionLogController.js"></script>


<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/theme/eclipse.css">  
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.js"></script>  
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/codemirror/ui-codemirror.js"></script> 
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/mathematicaModified.js"></script>  

<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.css" />
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/sql-hint.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/mode/clike/clike.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/selection/mark-selection.js"></script>
<script type="text/ng-template" id="templatesaveKPIScheduler.html">
<md-dialog aria-label="Select Function"  ng-cloak>
  <form>
    <md-toolbar>
      <div class="md-toolbar-tools">
        <h1>{{translate.load("sbi.kpi.scheduler.save")}}</h1>
        <span flex></span>
         <md-button class="md-icon-button" ng-click="close()">
          <md-icon md-font-icon="fa fa-times closeIcon" aria-label="Close dialog"></md-icon>
        </md-button>
      </div>
	
    </md-toolbar>
    <md-dialog-content >
     <div class="md-dialog-content">
		<md-input-container class="small counter" class="small counter">
			<label>{{translate.load("sbi.generic.name")}}</label>
			<input class="input_class" ng-model="selectedScheduler.name" required maxlength="40" ng-maxlength="40" md-maxlength="40">
		 </md-input-container>
		<div class="footer">
	<md-button class="dialogButton" ng-disabled="(selectedScheduler.name == undefined || (selectedScheduler.name.trim()).length == 0)" ng-click="apply()" ng-disable="selectedScheduler.name.length==0" md-autofocus>Save <md-icon md-font-icon="fa fa-check buttonIcon" aria-label="apply"></md-icon></md-button>
	</div>
   	 </md-dialog-content>
  </form>
</md-dialog>
</script>

<script type="text/ng-template" id="templatesaveKPI.html">
<md-dialog aria-label="Select2 Function"  ng-cloak>
  <form>
    <md-toolbar>
      <div class="md-toolbar-tools">
        <h1>{{translate.load("sbi.kpi.edit.save")}}</h1>
        <span flex></span>
		<md-button class="md-primary" ng-click="addKPIToCheck()">
          	{{translate.load("sbi.generic.update")}}
        </md-button>
		<md-button class="md-primary" ng-click="close()">
			{{translate.load("sbi.general.close")}}    
        </md-button>
      </div>
	
    </md-toolbar>
    <md-dialog-content >
     <div class="md-dialog-content">
		<angular-table class="cssTable"
		id='listKpiTable' ng-model=kpiAllList
		columns='[{"label":"KPI Name","name":"name"},{"label":"Category","name":"valueCd"},{"label":"Date","name":"datacreation"},{"label":"Author","name":"author"},{"label":" ","name":"icon","size":"30px"}]'
		columns-search='["name"]' show-search-bar=true
		multi-select= true selected-item=kpiSelected comparison-column="'id'"
		scope-functions=tableFunction 
		> </angular-table>
		</div>
   	 </md-dialog-content>
  </form>
</md-dialog>
</script>

</head>
<body class="kn-schedulerKpi">
	<angular-list-detail ng-controller="schedulerKpiController" full-screen=true >
		<list label="translate.load('sbi.kpi.listscheduler')"  ng-controller="listSchedulerController" new-function="addScheduler" >
		<div layout="row" layout-sm="column" id="preview" layout-align="space-around" ng-show="showCircular" layout-fill>
     		<md-progress-circular md-mode="indeterminate" ></md-progress-circular>
 		</div>
		<angular-table flex
		id='targetListTable' ng-model=engines
		columns='tableColumn'
		columns-search='["name"]' show-search-bar=true
		scope-functions=tableFunction 
		click-function="loadEngine(item);"
		speed-menu-option=engineMenuOptionList 
		initial-sorting="'name'"> </angular-table>
		</list>
		<detail label="getNameForBar()" save-function="saveSc" cancel-function="cancel">
		
		<md-whiteframe class="md-whiteframe-4dp" layout="row"  ng-if="expired">
		   <p flex>{{translate.load("sbi.kbi.scheduler.error.expired.cron.interval")}}</p>
		   <md-button    ng-click="closeExpired()"  > {{translate.load("sbi.general.ok")}} </md-button>
 	 	</md-whiteframe>
		<md-tabs flex md-selected='selectedTab.tab'>
				<md-tab id="tab1" >
       				<md-tab-label>{{translate.load("sbi.generic.kpi")}}</md-tab-label>
        			<md-tab-body>
        			<%@include	file="./schedulerKpiTemplate/kpi.jsp"%>
					</md-tab-body>
				</md-tab>
				<md-tab id="tab2" md-on-select='loadAllInformationForKpi()'>
       				<md-tab-label >{{translate.load("sbi.kpi.filters")}}</md-tab-label>
        			<md-tab-body >
        			<%@include	file="./schedulerKpiTemplate/filterTemplate.jsp"%>
					</md-tab-body>
				</md-tab>
				
				<md-tab id="tab3">
       				<md-tab-label>{{translate.load("sbi.scheduler.schedulation.events.event.frequency")}}</md-tab-label>
        			<md-tab-body>
        			<%@include	file="./schedulerKpiTemplate/frequencyTemplate.jsp"%>
					</md-tab-body>
				</md-tab>
				
				<md-tab id="tab4" md-on-select="checkFilterParams()" >
       				<md-tab-label>{{translate.load("sbi.execution.parametersselection.executionbutton.message")}}</md-tab-label>
        			<md-tab-body>
        			<%@include	file="./schedulerKpiTemplate/executeTemplate.jsp"%>
					</md-tab-body>
				</md-tab>
			</md-tabs>
		

		</detail>
		</angular-list-detail>
</body>
</html>
