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
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>



<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="templateBuild">

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Template build</title>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/angular_1.x/kpi-dinamic-list/KpiDinamicList.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/angular_1.x/controllerBuildTemplate/kpiEditController.js"></script>
<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/sbi_default/css/commons/css/customStyle.css"> 
<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/sbi_default/css/designerKpi/designerCss.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/angular_1.x/style/kpiStyleController.js"></script>
<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/js/angular_1.x/kpi-dinamic-list/css/kpiWidgetStyle.css
">

</head>
<body ng-controller="templateBuildController">
<md-toolbar  class="miniheadimportexport" layout="row">
	<div class="md-toolbar-tools" flex>
		<h2 class="md-flex" >{{translate.load("sbi.kpidocumentdesigner")}}</h2>
	</div>
	<span flex></span>
	<md-button class="md-primary" ng-click="saveTemplate()">{{translate.load("sbi.general.save")}}</md-button>
	<!-- <md-button class="md-primary" >{{translate.load("sbi.general.close")}}</md-button> -->
</md-toolbar>
<md-whiteframe class="md-whiteframe-2dp relative" layout-fill layout-margin flex  >
	<md-radio-group layout="row" ng-model="typeChart">
	     		<md-radio-button  value='kpi' >Kpi</md-radio-button>
	     		<md-radio-button  value='scorecard'> Scorecard </md-radio-button>
   	</md-radio-group>
   	<md-toolbar  class="miniheadimportexport" layout="row" ng-if="typeChart=='kpi'">
	<div class="md-toolbar-tools" flex>
		<h2 class="md-flex" >Type of Document</h2>
	</div>
	</md-toolbar>
	<md-radio-group layout="row" ng-model="typeDocument" ng-show="typeChart=='kpi'">
	     		<md-radio-button  value='list' >List</md-radio-button>
	     		<md-radio-button  value='widget'> Widget </md-radio-button>
   	</md-radio-group>

	<expander-box id="Info" color="white" ng-if="typeChart=='scorecard'" expanded="true" title="'Scorecard List'">
	<md-whiteframe class="md-whiteframe-4dp layout-padding " layout layout-margin style ="height: 80%;" > 

 		<angular-table flex style ="height: 80%;"
		id='dinamicListTable' ng-model="scorecardSelected"
		columns='[{"label":"Name","name":"name"},{"label":"Creation Date","name":"creationDate"}]'
		columns-search='["name"]' show-search-bar=true
		scope-functions=tableFunction  speed-menu-option=measureMenuOption 
		>
		<queue-table>
			<div layout="row"> 
				<span flex></span>
				<md-button ng-click="scopeFunctions.loadListScorecard()">Add Scorecard Association</md-button>
			</div>
		</queue-table> 
		</angular-table>	
   </md-whiteframe>       
	</expander-box>
	<expander-box id="Info" color="white" ng-show="typeChart=='kpi'" expanded="true" title="'Kpi List'">
		<dinamic-list ng-model="selectedKpis" type-chart="typeDocument" multi-select=true selected-item ="addKpis"></dinamic-list>  
	</expander-box>
	<expander-box id="Info" color="white" expanded="false" title="'Options'" ng-if="typeChart=='kpi'">
		 <md-whiteframe class="md-whiteframe-4dp layout-padding " layout="column" layout layout-fill layout-margin  >
		 
		 <div layout="row">
		 	<span flex = 15><h4>{{translate.load("sbi.kpidocumentdesigner.showvalue")}}:</h4></span>
		  	  <md-checkbox ng-model="options.showvalue" aria-label="show value">
          </md-checkbox>

		 </div>
		<div layout="row">
		 	<span flex = 15><h4>{{translate.load("sbi.kpidocumentdesigner.showtarget")}}:</h4></span>
   		 	<md-radio-group layout="row" ng-model="options.showtarget">
	    	<md-checkbox ng-model="options.showtarget" aria-label="show target">
   		</div>
   		<div layout="row">
		 	<span flex = 15><h4>{{translate.load("sbi.kpidocumentdesigner.showpercentage")}}:</h4></span>
   			 
	     		<md-checkbox ng-model="options.showtargetpercentage" aria-label="show percentage">
   			 </div>
   			 <div layout="row">
		 	<span flex = 15><h4>{{translate.load("sbi.kpidocumentdesigner.showgauge")}}:</h4></span>
   			 <md-radio-group layout="row" ng-model="options.showlineargauge">
	     		<md-checkbox ng-model="options.showlineargauge" aria-label="show linear gauge">
   			 </div>
   			 <div layout="row">
		 	<span flex = 15><h4>{{translate.load("sbi.kpidocumentdesigner.showthreshold")}}:</h4></span>
   			
	     		<md-checkbox ng-model="options.showthreshold" aria-label="show threshold">
   			 </div>
   			 
   			<span layout="row">
			<md-input-container class="small counter" flex=15 > <label>{{translate.load("sbi.kpidocumentdesigner.precision")}}</label>
					<input class="input_class"ng-model="options.history.size"
						 type="number" min="0"> 
			</md-input-container>
			</span>
			<span layout="row">
				<h4 flex=15>Units</h4>
				<md-select aria-label="aria-label" flex=30 ng-model="options.history.units">
					<md-option ng-repeat="unit in units" value="{{unit}}">{{unit}}</md-option>
				</md-select>
			</span>
		 </md-whiteframe>
	</expander-box>
	<expander-box id="Info" color="white"  title="'Style'">
		<!-- direttiva style -->
		<kpi-style ng-model="style"></kpi-style>
	</expander-box>
</md-whiteframe>
</body>
</html>
