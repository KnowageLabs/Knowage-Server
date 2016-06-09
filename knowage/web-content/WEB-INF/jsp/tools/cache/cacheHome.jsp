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
<html ng-app="cacheManager">

<head>

	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

	<!-- Styles -->
	<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/angularChart/angular-chart.css">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/themes/sbi_default/css/cacheChart/css/cache.css">

	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/cache/cacheRuntimeController.js"></script>	
	<script src="${pageContext.request.contextPath}/js/lib/Chart.js/Chart.js"></script>
	<script src="${pageContext.request.contextPath}/js/lib/angularChart/angular-chart.js"></script>

	<title>CacheManager</title>

</head>

<body class="bodyStyle cacheManager" ng-cloak ng-controller="cacheRuntimeCtrl as ctrl" layout="column">

<md-toolbar class="md-knowage-theme"> <div class="md-toolbar-tools"> <h2>{{ctrl.translate.load("cache.manager.cacheInformation")}} </h2></div> </md-toolbar>
<md-content flex>
<md-tabs md-dynamic-height> 
	<md-tab label="Overview">
	  <div layout="row" ng-if="!ctrl.isUndefined(ctrl.data)">
			<md-card flex="50" layout-padding>

				<md-toolbar class="md-knowage-theme"> <div class="md-toolbar-tools"> {{ctrl.translate.load("cache.manager.runtimeInformation")}}</div> </md-toolbar>
				<div layout-align="center center" layout-padding layout-margin flex>	<!-- rimuovibile-->		
					<div layout="row" layout-wrap layout-margin>
						<div flex="50">
					
							<p>{{ctrl.translate.load("cache.manager.cacheEnabled")}}				{{ ctrl.data.enabled }}</p>
			 
							<p>{{ctrl.translate.load("cache.manager.totalMemory")}}					{{ ctrl.formatSizeUnits(ctrl.data.totalMemory) }}</p>
			
							<p>{{ctrl.translate.load("cache.manager.availableMemory")}}				{{ ctrl.formatSizeUnits(ctrl.data.availableMemory) }}</p>  
	
							<p>{{ctrl.translate.load("cache.manager.numberOfCachedObjects")}}		{{ ctrl.data.cachedObjectsCount }}</p>
			
							<p>{{ctrl.translate.load("cache.manager.availableMemoryPercentage")}}	{{ ctrl.data.availableMemoryPercentage }}%</p>
							
						</div>
			
						<div flex="50">
							<div class="cacheChartContainer" flex>
								<canvas id="pie" class="chart chart-pie" chart-data="ctrl.chartData"
									chart-labels="ctrl.labels" width="auto";> </canvas>
							</div>
						</div>
						
					</div>
				</div>	
			</md-card>
			
			<md-card flex="50" layout-padding>
				<md-toolbar class="md-knowage-theme"> <div class="md-toolbar-tools"> {{ctrl.translate.load("cache.manager.generalSettings")}} <div flex></div> <md-button ng-click="ctrl.saveFunction()"  ng-disabled="manageForm.$invalid">{{ctrl.translate.load("cache.manager.save")}}</md-button> <md-button ng-click="ctrl.discardFunction()">{{ctrl.translate.load("cache.manager.discard")}}</md-button> </div> </md-toolbar>
						
				<form layout="row" layout-wrap name=manageForm >
					<md-switch class="md-primary" md-no-ink ng-model="ctrl.variableEnabled" flex="50" ng-disabled="true"> {{ctrl.translate.load("cache.manager.disabledEnabled")}} </md-switch>
										
					<div flex="50">
						
						<md-input-container>
            				<label>{{ctrl.translate.load("cache.manager.prefixForCacheTablesName")}}</label>
            				<input ng-model="ctrl.variableNamePrefix" >
          				</md-input-container>
						
						<md-input-container>
            				<label>{{ctrl.translate.load("cache.manager.maximumPercentOfCacheCleaningQuota")}}</label>
            				<input ng-model="ctrl.variableLimitForClean" type="number" min="0" max="100">
          				</md-input-container>

					    <md-input-container>
					    	<label>{{ctrl.translate.load("cache.manager.frequencyOfCleaningDaemon")}}</label>
					       		<md-select ng-model="ctrl.variableSchedulingFullClean">
					         		<md-option ng-repeat="val in ctrl.schedulingValues" ng-value="val">{{val}}</md-option>
					       		</md-select>
					    </md-input-container>
				<!-- New -->		
						<md-input-container>
            				<label>{{ctrl.translate.load("cache.manager.ttlForCachedDataset")}}</label>
            				<input ng-model="ctrl.variableLastAccessTtl" type="number">
          				</md-input-container>

					    <md-input-container>
					    	<label>{{ctrl.translate.load("cache.manager.tToCreateTempTable")}}</label>
					       	<input ng-model="ctrl.variableCreateAndPersistTimeout" type="number">
					    </md-input-container>
						
		
					</div>
		
					<div flex="50">
						<md-input-container>
            				<label>{{ctrl.translate.load("cache.manager.totalBytesAvailableForCache")}}</label>
            				<input ng-model="ctrl.variableSpaceAvailable" type="number" min="0">
           				
          				</md-input-container>
          				
          				<md-input-container>
            				<label>{{ctrl.translate.load("cache.manager.cacheDimensionSingleDataset")}}</label>
            				<input ng-model="ctrl.variableCacheLimitForStore" type="number" min="0" max="100">
          				</md-input-container>

          				<md-input-container>
					    	<label>{{ctrl.translate.load("cache.manager.targetDatasource")}}</label>
					       		<md-select ng-model="ctrl.variableSelectedDataSource">
					         		<md-option ng-repeat="dataSource in ctrl.filteredDataSources" ng-value="dataSource">{{dataSource.label}}</md-option>
					       		</md-select>
					    </md-input-container>
					 
			<!-- New -->
						<md-input-container>
            				<label>{{ctrl.translate.load("cache.manager.timeToLock")}}</label>
            				<input ng-model="ctrl.variableSqldbCacheTimeout" type="number">
          				</md-input-container>

					    <md-input-container>
					    	<label>{{ctrl.translate.load("cache.manager.hazelcastTimeToLock")}}</label>
					       	<input ng-model="ctrl.variableHazelcastTimeout" type="number">
					    </md-input-container>		    
					    
					    <md-input-container>
					    	<label>{{ctrl.translate.load("cache.manager.hazelcastTimeToReleaseLock")}}</label>
					       	<input ng-model="ctrl.variableHazelcastLeaseTime" type="number">
					    </md-input-container>	
					    
						
					</div>
					
				</form>					
		
			</md-card>
			
		</div>	
	</md-tab> 
	
	<md-tab label="Manage"> 
		<md-card flex>

			<md-toolbar class="md-knowage-theme"> <div class="md-toolbar-tools">{{ctrl.translate.load("cache.manager.addRemoveDataset")}}<div flex></div><md-button ng-click="ctrl.deleteFunction()" ng-disabled="ctrl.itemSelected.length<=0">{{ctrl.translate.load("cache.manager.delete")}}</md-button> <md-button ng-click="ctrl.cleanAllFunction()">{{ctrl.translate.load("cache.manager.cleanAll")}}</md-button></div></md-toolbar>
			<div layout-align="center center" layout-padding layout-margin flex>			
					
					<div ng-if="ctr.metadata != undefined && ctr.metadata.length > 0">  <!-- METADATA TABLE -->
						
					<angular-table 	id="manageTable" 	ng-model=ctrl.metadata 
											columns=ctrl.tableColumns
											highlights-selected-item = "true"
											show-search-bar="true"
											columns-search='["name"]'
											no-pagination="false"
											multi-select = "true"
											selected-item="ctrl.itemSelected"
					></angular-table>					
					
				</div>	
				<div ng-if="ctr.metadata == undefined || ctr.metadata.length == 0">  <!--  METADATA TABLE -->
					{{ctrl.translate.load("cache.manager.metadataUnavailable")}}
				</div>	
	
			</div>
				
		</md-card>

	 </md-tab>
</md-tabs>
</md-content>


</body>
