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
<html ng-app="timespanManager">
<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 
	
	<link rel="stylesheet" type="text/css" href="/knowage/themes/timespan/css/timespanStyle.css">
	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/timespan/timespan.js"></script>
	
</head>


<body ng-cloak>
	<angular-list-detail ng-controller="Controller as ctrl">
		<list label="translate.load('sbi.timespan')"  new-function="ctrl.newTs" >
			<angular-table flex 
					id='ts' 
               		ng-model=ctrl.tsList
               		columns='ctrl.TSTableColumns'
               		columns-search="ctrl.TSTableColumnsSearch"
               		show-search-bar=true 
               		click-function="ctrl.loadTimespan(item)"
               		speed-menu-option=ctrl.menuTs>
              	</angular-table>
		</list>
		<detail label="ctrl.selectedItem.name" save-function="ctrl.saveTimespan" cancel-function="ctrl.cancel">
							<div layout="row" layout-wrap>
								<div flex="50">
									<md-input-container > <label>{{translate.load("sbi.generic.name");}}</label>
									<input maxlength="100" type="text" ng-model="ctrl.selectedItem.name"> </md-input-container>
								</div>
			
								<div flex="50">
									<md-input-container > <label>{{translate.load("sbi.generic.type");}}</label> 
									<md-select ng-model="ctrl.selectedItem.type" ng-change="ctrl.changeType()"> 
										<md-option ng-repeat="type in ctrl.tsType" value="{{type.value}}" >
											{{type.label}} 
										</md-option> 
									</md-select> 
									
									</md-input-container>
								</div>
							</div>
							
							<div layout="row" >
								<div flex="50">
									<md-input-container > <label>{{translate.load("sbi.generic.category");}}</label> 
										<md-select ng-model="ctrl.selectedItem.category"> 
											<md-option
												ng-repeat="cat in ctrl.tsCategory" value="{{cat.VALUE_ID}}">
												{{cat.VALUE_NM}} 
											</md-option> 
										</md-select> 
									</md-input-container>
								</div>
							</div>
							<div ng-if="ctrl.selectedItem.type!=undefined && ctrl.selectedItem.type!=''" class="md-whiteframe-2dp" flex layout>
								<angular-table  flex layout-padding style="background-color: white;"
									id='newTimespanIntervals' 
				               		ng-model=ctrl.selectedItem.definition
				               		no-pagination=true
				               		columns='[{label:translate.load("sbi.timespan.from") , name:"from"},{label:translate.load("sbi.timespan.to") , name:"to"}]'
				               		speed-menu-option=ctrl.timespanIntervalAction
				               		scope-functions="ctrl.newtimespanTableFunction" 
				               	  >
				               		 <queue-table>
											<div layout="row" layout-align="space-around center" > 
												<md-datepicker flex ng-if="scopeFunctions.selectedItem.type=='temporal'" ng-model="scopeFunctions.from" md-placeholder="Enter date"></md-datepicker>
												<angular-time-picker id="fromTP" flex ng-if="scopeFunctions.selectedItem.type=='time'"  ng-model="scopeFunctions.from" ></angular-time-picker>
												
												<md-datepicker flex ng-if="scopeFunctions.selectedItem.type=='temporal'" ng-model="scopeFunctions.to" md-placeholder="Enter date" ></md-datepicker>
												<angular-time-picker id="toTP" flex ng-if="scopeFunctions.selectedItem.type=='time'"  ng-model="scopeFunctions.to" ></angular-time-picker>
												<md-button class="md-raised absoluteToRight" ng-disabled="scopeFunctions.from=='' || scopeFunctions.to==''" ng-click="scopeFunctions.addInterval(scopeFunctions.from,scopeFunctions.to)">add</md-button>
												
											</div>
										</queue-table> 
				              	</angular-table>
							
							</div>
							
						 
		</detail>
	</angular-list-detail>



</body>
</html>
