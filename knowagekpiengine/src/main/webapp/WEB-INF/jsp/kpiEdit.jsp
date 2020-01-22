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
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jsp"%>



<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="templateBuild">

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>{{translate.load("sbi.kpidocumentdesigner.tempbuild")}}</title>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/angular_1.x/kpi-dinamic-list/KpiDinamicList.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/angular_1.x/controllerBuildTemplate/kpiEditController.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/angular_1.x/controllerBuildTemplate/services/documentService.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/angular_1.x/style/kpiStyleController.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/angular_1.x/services/channelMessaging.js"></script>

</head>
<body ng-controller="templateBuildController" class="kn-kpi-definition">
	<md-toolbar>
		<div class="md-toolbar-tools">
			<h2>{{translate.load("sbi.kpidocumentdesigner")}}</h2>
			<span flex></span>
			<md-button ng-disabled="selectedKpis.length == 0 && scorecardSelected.length == 0" ng-click="saveTemplate(createJSONFromInfo())">{{translate.load("sbi.general.save")}}</md-button>
			<md-button ng-if="!isWorkspace();" ng-click="closeTemplate()">{{translate.load("sbi.general.close")}}</md-button>
		</div>
	</md-toolbar>
	<div flex>
		<md-radio-group layout="row" ng-model="typeChart" layout-margin>
		     		<md-radio-button  value='kpi' >{{translate.load("sbi.kpi.kpi")}}</md-radio-button>
		     		<md-radio-button ng-if="showScorecards" value='scorecard'>{{translate.load("sbi.kpi.scorecard")}} </md-radio-button>
	   	</md-radio-group>
	   	
	   	<md-toolbar  layout="row" ng-if="typeChart=='kpi'" class="secondaryToolbar">
			<div class="md-toolbar-tools" flex>
				<h2 class="md-flex" >{{translate.load("sbi.kpi.type")}}</h2>
			</div>
		</md-toolbar>
		<md-radio-group layout="row" ng-model="typeDocument" ng-show="typeChart=='kpi'" layout-margin>
     		<md-radio-button  value='list' >{{translate.load("sbi.kpi.list")}}</md-radio-button>
     		<md-radio-button  value='widget'>{{translate.load("sbi.kpi.widget")}} </md-radio-button>
	   	</md-radio-group>
	
		<expander-box id="scorecard" color="white" toolbar-class="secondaryToolbar" ng-if="typeChart=='scorecard'" expanded="true" expander-title="translate.load('sbi.kpiedit.listscorecard')">
			<angular-table flex
			id='dinamicListTable' ng-model="scorecardSelected"
			columns='[{"label":"Name","name":"name"},{"label":"Creation Date","name":"creationDate"}]'
			columns-search='["name"]' show-search-bar=true
			scope-functions=tableFunction  speed-menu-option=measureMenuOption 
			>
				<queue-table>
					<div layout="row"> 
						<span flex></span>
						<md-button ng-click="scopeFunctions.loadListScorecard()">{{scopeFunctions.translate.load('sbi.kpi.addscoreass')}}</md-button>
					</div>
				</queue-table> 
			</angular-table>
		</expander-box>
		
		<expander-box id="list" color="white" toolbar-class="ternaryToolbar" ng-show="typeChart=='kpi'" expanded="true" expander-title="translate.load('sbi.kpiedit.kpilist')">
			<dinamic-list ng-model="selectedKpis" type-chart="typeDocument" multi-select=true selected-item ="addKpis"></dinamic-list>  
		</expander-box>
		
		<expander-box id="style" color="white" toolbar-class="ternaryToolbar zindexback" expander-title="translate.load('sbi.kpiedit.style')" class="overflowVisible">
			<kpi-style ng-model="style"></kpi-style>
		</expander-box>
		
		<expander-box id="kpi" color="white" toolbar-class="ternaryToolbar" expanded="false" expander-title="translate.load('sbi.kpiedit.options')" ng-if="typeChart=='kpi'">
			<div layout="row" layout-wrap flex>
				<div flex="50">
					<md-checkbox ng-model="options.showvalue" aria-label="show value">{{translate.load("sbi.kpidocumentdesigner.showvalue")}}</md-checkbox>
				</div>
				<div flex="50">
					<md-checkbox ng-model="options.showtarget" aria-label="show target">{{translate.load("sbi.kpidocumentdesigner.showtarget")}}</md-checkbox>
				</div>
				<div flex="50">
					<md-checkbox ng-model="options.showtargetpercentage" aria-label="show percentage">{{translate.load("sbi.kpidocumentdesigner.showpercentage")}}</md-checkbox>
				</div>
				<div flex="50">
					<md-checkbox ng-model="options.showthreshold" aria-label="show threshold">{{translate.load("sbi.kpidocumentdesigner.showthreshold")}}</md-checkbox>
				</div>
			</div>
			<div layout="row">
				<md-input-container flex="50"> 
					<label>{{translate.load("sbi.kpidocumentdesigner.precision")}}</label>
					<input ng-model="options.history.size" type="number" min="0"> 
				</md-input-container>
				<md-input-container flex="50"> 
					<label>{{translate.load("sbi.kpidocumentdesigner.units")}}</label>
					<md-select ng-model="options.history.units">
						<md-option ng-repeat="unit in units" value="{{unit}}">{{unit}}</md-option>
					</md-select>
				</md-input-container>
			</div>
		</expander-box>
	</div>
</body>
</html>
