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
<html ng-app="geoTemplateBuild">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<%@include file="/WEB-INF/jsp/commons/angular/geoImport.jsp"%>

<!-- document-viewer -->
<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/commons/document-viewer/documentViewer.js"></script>

<title>{{translate.load("gisengine.designer.title")}}</title>
<script>
 var documentLabel='<%=docLabel%>';
 var dataset='<%=docDatasetLabel%>';
 var docTemplate= '<%=template%>';
 var isTechnicalUser = '<%=isUserTechnical%>';
 var datasetLabel='<%=datasetLabel%>';
 if(datasetLabel==null){
	 datasetLabel='';
 }
</script>
</head>
<body ng-controller="geoTemplateBuildController">
<md-toolbar  class="miniheadimportexport" layout="row">
	<div class="md-toolbar-tools" flex>
		<h2 class="md-flex" >{{translate.load("gisengine.designer.title")}}</h2>
	</div>
	<span flex></span>
	<md-button class="md-primary" ng-click="editMap()" ng-disabled="editDisabled" >{{translate.load("gisengine.designer.edit.map")}}</md-button>

	<md-button class="md-primary" ng-click="saveTemplate()">{{translate.load("sbi.generic.save")}}</md-button>
    <md-button class="md-primary" ng-if="!tecnicalUser" ng-click="cancelBuildTemplate()">{{translate.load("sbi.generic.close")}}</md-button>
    <md-button class="md-primary" ng-if="tecnicalUser" ng-click="cancelBuildTemplateAdmin()">{{translate.load("sbi.generic.close")}}</md-button>
</md-toolbar>
<md-whiteframe class="md-whiteframe-2dp relative" layout-fill layout-margin flex  >
<!-- map name always visible -->
<!-- <div layout="row" flex> -->
 
<!-- <md-input-container flex=30> -->
<!--   <label>{{translate.load("gisengine.designer.mapname")}}</label> -->
<!--   <input type="text" ng-model="mapName"> -->
<!-- </md-input-container> -->
<!-- </div> -->
<!-- CHOSE DATA SET FOR FINAL USER -->
<div layout="row" flex ng-if="!tecnicalUser">
<label flex=20>{{datasetLabel}}</label>
  <md-button class="md-fab md-mini md-primary" ng-if="!isDatasetChosen && !disableChooseDs" ng-click="choseDataset()" aria-label="Add dataset">
          <md-icon class="fa fa-plus-circle fa-2x"></md-icon>
        </md-button>
    <md-button class="md-fab md-mini md-primary" ng-if="isDatasetChosen && !disableChooseDs" ng-click="clearDataset()" aria-label="Clear dataset">
          <md-icon class="fa fa-minus-circle fa-2x"></md-icon>
        </md-button>      
</div>
<!-- SINGLE SELECT FROM LAYER CATALOG  WHEN DATASET IS CHOSEN-->
	<expander-box id="layersList" color="white" ng-if="isDatasetChosen" expanded="true" title="translate.load('gisengine.designer.layer.select')">
	<md-whiteframe class="md-whiteframe-4dp layout-padding " flex layout layout-margin  > 

 		<angular-table flex 
		id='table' ng-model="selectedLayer"
		columns='[{"label":"ID","name":"layerId"},{"label":"Name","name":"name"},{"label":"Description","name":"descr"},{"label":"Type","name":"type"}]'
		columns-search='["name"]' show-search-bar=false
		scope-functions='tableFunctionSingleLayer' 
		>
		<queue-table>
			<div layout="row"> 
				<span flex></span>
				<md-button ng-click="scopeFunctions.loadListLayers()">{{scopeFunctions.translate.load('gisengine.designer.layer.add')}}</md-button>
			</div>
		</queue-table> 
		</angular-table>	
   </md-whiteframe>       
	</expander-box>
	<!-- MULTI SELECT LAYER  -->
	<expander-box id="layersListMultiSelect" color="white" ng-if="!isDatasetChosen" expanded="true" title="translate.load('gisengine.designer.layer.select')">
	<md-whiteframe class="md-whiteframe-4dp layout-padding " flex layout layout-margin style ="height: 40%;" > 

 		<angular-table flex  
		id='tableLayerMultiSelect' ng-model="selectedLayer"
		columns='[{"label":"ID","name":"layerId"},{"label":"Name","name":"name"},{"label":"Description","name":"descr"},{"label":"Type","name":"type"}]'
		columns-search='["name"]' show-search-bar=true
		scope-functions='tableFunctionMultiLayer' speed-menu-option='multipleLayerSpeedMenu'
		>
		<queue-table>
			<div layout="row"> 
				<span flex></span>
				<md-button ng-click="scopeFunctions.loadListLayers()">{{scopeFunctions.translate.load('gisengine.designer.layer.add')}}</md-button>
			</div>
		</queue-table> 
		</angular-table>	
   </md-whiteframe>       
	</expander-box>
	<!-- CHOSING LAYER FILTERS visible if there is no dataset-->
	<expander-box id="filterSelectBox" color="white" ng-if="!isDatasetChosen" expanded="false" title="translate.load('gisengine.designer.layer.filters')">
	<md-whiteframe class="md-whiteframe-4dp layout-padding " flex layout layout-margin style ="height: 40%;" > 
        <div layout="row">
 		<angular-table flex  
		id='tableFilters' ng-model="selectedFilters"
		columns='[{"label":"Filter","name":"property"}]'
		columns-search='["name"]' show-search-bar=true
		scope-functions='tableFunctionFilters' speed-menu-option='filtersSpeedMenu'
		>
		<queue-table>
			<div layout="row"> 
				<span flex></span>
				<md-button ng-click="scopeFunctions.loadFilters()">{{scopeFunctions.translate.load('gisengine.designer.layer.filters.add')}}</md-button>
			</div>
		</queue-table> 
		</angular-table>
		<div flex ng-if="allDriverParamteres.length">
		<angular-table flex 
		id='tableDriver' ng-model="selectedDriverParamteres"
		columns='[{"label":"Driver parameter","name":"label"}]'
		columns-search='["name"]' show-search-bar=true
		scope-functions='tableFunctionFilters' speed-menu-option='filtersSpeedMenu'
		>
		<queue-table>
			<div layout="row"> 
				<span flex></span>
				<md-button ng-click="scopeFunctions.loadFilters()">{{scopeFunctions.translate.load('gisengine.designer.layer.filters.add')}}</md-button>
			</div>
		</queue-table> 
		</angular-table>
		</div>
		</div>	
   </md-whiteframe>       
	</expander-box>
	<!-- DATASET JOIN COLUMNS INTERFACE -->
	<expander-box id="datasetJoinBox" color="white" ng-if="isDatasetChosen" expanded="false" title="translate.load('gisengine.designer.dataset.joincolumns')">
	<md-whiteframe class="md-whiteframe-4dp layout-padding " flex layout layout-margin style ="height: 40%;" > 
	    <angular-table flex  
		id='datasetJoinColumnsTable' ng-model="datasetJoinColumns"
		columns='[{"label":"Dataset join column","name":"datasetColumnView","hideTooltip":true},{"label":"Layer join column","name":"layerColumnView","hideTooltip":true}]'
		columns-search='["datasetColumn","layerColumn"]' show-search-bar=true
		scope-functions='tableFunctionsJoin' speed-menu-option='datasetJoinSpeedMenu'
		allow-edit="true"
		>
		<queue-table>
			<div layout="row"> 
				<span flex></span>
				<md-button ng-click="scopeFunctions.addJoinColumn()">{{scopeFunctions.translate.load('gisengine.designer.dataset.joincolumns.add')}}</md-button>
			</div>
		</queue-table> 
		</angular-table>	
	</md-whiteframe>
	</expander-box>
	<!-- DATASET INDICATORS -->
	<expander-box id="datasetIndicators" color="white" ng-if="isDatasetChosen" expanded="false" title="translate.load('gisengine.designer.dataset.indicators')">
	<md-whiteframe class="md-whiteframe-4dp layout-padding " flex layout layout-margin style ="height: 40%;" > 
	    <angular-table flex  
		id='indicatorsTable' ng-model="datasetIndicators"
		columns='[{"label":"Measure","name":"indicatorNameView","hideTooltip":true},{"label":"Label","name":"indicatorLabel","hideTooltip":true,"editable":true}]'
		columns-search='["indicatorName","indicatorLabel"]' show-search-bar=true
		scope-functions='tableFunctionIndicator' speed-menu-option='indicatorsSpeedMenu'
		allow-edit="true"
		>
		<queue-table>
			<div layout="row"> 
				<span flex></span>
				<md-button ng-click="scopeFunctions.addIndicator()">{{scopeFunctions.translate.load('gisengine.designer.dataset.indicators.add')}}</md-button>
			</div>
		</queue-table> 
		</angular-table>	
	</md-whiteframe>
	</expander-box>
	<!-- DATASET FILTERS  -->
	<expander-box id="datasetFilters" color="white" ng-if="isDatasetChosen" expanded="false" title="translate.load('gisengine.designer.dataset.filters')">
	<md-whiteframe class="md-whiteframe-4dp layout-padding " flex layout layout-margin style ="height: 40%;" > 
	    <angular-table flex  
		id='indicatorsTable' ng-model="datasetFilters"
		columns='[{"label":"Name","name":"dsFilterNameView","hideTooltip":true},{"label":"Label","name":"dsFilterLabel","hideTooltip":true,"editable":true}]'
		columns-search='["dsFilterName","dsFilterLabel"]' show-search-bar=true
		scope-functions='tableFunctionDatasetFilters' speed-menu-option='dsFiltersSpeedMenu'
		allow-edit="true"
		>
		<queue-table>
			<div layout="row"> 
				<span flex></span>
				<md-button ng-click="scopeFunctions.addDatasetFilter()">{{scopeFunctions.translate.load('gisengine.designer.dataset.dsfilters.add')}}</md-button>
			</div>
		</queue-table> 
		</angular-table>	
	</md-whiteframe>
	</expander-box>
	
 </md-whiteframe>
</body>
</html>