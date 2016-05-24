<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
--%>

<%@ page language="java" pageEncoding="utf-8" session="true"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="jobMonitor">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<!-- Styles -->
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">

<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/scheduler/jobMonitor.js")%>"></script>

</head>
<body class="bodyStyle">
<angular-list-detail ng-controller="Controller" full-screen="true">
	<list label="'Scheduler Monitor'" layout-column>

		<md-card style="margin-bottom: 0px;">
			<md-card-content layout="row" layout-align="space-between center">
			
				<label>{{translate.load("scheduler.startdate","component_scheduler_messages")}}:</label>
				<md-datepicker ng-model="startDate" md-placeholder={{translate.load("scheduler.startdate","component_scheduler_messages")}} md-min-date="minDate"></md-datepicker>
				<label>&nbsp; {{translate.load("scheduler.starttime","component_scheduler_messages")}}:</label>
				<angular-time-picker required ng-model="startTime"></angular-time-picker>
				
				<div flex></div>
				
				<label>{{translate.load("scheduler.enddate","component_scheduler_messages")}}:</label>
				<md-datepicker ng-model="endDate" md-placeholder={{translate.load("scheduler.enddate","component_scheduler_messages")}} md-min-date="startDate"></md-datepicker>
				<label>&nbsp; {{translate.load("scheduler.endtime","component_scheduler_messages")}}:</label>
				<angular-time-picker required ng-model="endTime"></angular-time-picker>
				
				<div flex></div>
				
				<md-button class="md-icon-button" title="Search" aria-label="Search" ng-click="updateExecutionList()">
					  <md-icon md-font-icon="fa fa-search"></md-icon>
				</md-button>
				
			</md-card-content>
		</md-card>
		
		<md-progress-circular ng-show="loadingExecutions" loading
				md-mode="indeterminate"
				md-diameter="75%"
				style="position:fixed; top:50%; left:50%; z-index:500; background:rgba(255, 255, 255, 0);">
		</md-progress-circular>
    
		<angular-table ng-hide="loadingExecutions" flex style="margin-top: 0px;"
				id = 'jobmonitor'
				ng-model = 'executions'
				columns = '[
					{"label":translate.load("sbi.scheduler.startdate"),"name":"executionDate"},
					{"label":translate.load("sbi.scheduler.activity"),"name":"jobName"},
					{"label":translate.load("sbi.scheduler.schedulation"),"name":"triggerName"},
					{"label":translate.load("sbi.scheduler.schedulationtype"),"name":"triggerType"},
					{"label":translate.load("sbi.scheduler.schedulation.paused.short"),"name":"triggerIsPausedString"},
					{"label":translate.load("sbi.generic.document"),"name":"documentName"}
					]'
				columnsSearch = '["executionDate","jobName","triggerName","triggerType","documentName"]'
				show-search-bar = true
				selected-item = "selectedExecution"
				highlights-selected-item = true
				speed-menu-option = "menuExecution"
				initial-sorting = "'executionDate'"
				current-page-number = "tablePage"
				page-changed-function = "pageChanged(searchValue, itemsPerPage, currentPageNumber, columnsSearch, columnOrdering, reverseOrdering)">
		</angular-table>
	</list>
</angular-list-detail>
</body>
</html>
