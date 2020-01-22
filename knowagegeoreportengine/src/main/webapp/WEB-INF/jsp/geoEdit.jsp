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
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="geoTemplateBuild">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<%@include file="/WEB-INF/jsp/commons/angular/geoImport.jsp"%>

<!-- document-viewer 
<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/commons/document-viewer/documentViewer.js"></script>-->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(knowageContext, "/js/src/angular_1.4/tools/commons/document-viewer/documentViewer.js")%>"></script>

<title>{{::translate.load("gisengine.designer.title")}}</title>
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
<body ng-controller="geoTemplateBuildController" class="kn-geoEdit">
<md-toolbar  class="toolbar" layout="row">
	<div class="md-toolbar-tools" flex>
		<h2 class="md-flex" >{{::translate.load("gisengine.designer.title")}}</h2>
		<span flex></span>
		<md-button  ng-click="editMap()" ng-disabled="editDisabled" >{{::translate.load("gisengine.designer.edit.map")}}</md-button>
		<md-button  ng-click="saveTemplate()">{{::translate.load("sbi.generic.save")}}</md-button>
	    <md-button  ng-if="!tecnicalUser" ng-click="cancelBuildTemplate()">{{::translate.load("sbi.generic.close")}}</md-button>
	    <md-button  ng-if="tecnicalUser" ng-click="cancelBuildTemplateAdmin()">{{::translate.load("sbi.generic.close")}}</md-button>
	</div>
</md-toolbar>
<md-whiteframe class="md-whiteframe-2dp relative" layout-fill flex  >
<!-- CHOSE DATA SET FOR FINAL USER -->
	<expander-box id="datasetList" ng-if="!tecnicalUser" color="white" background-color="#a9c3db"  expanded="isOpenedDataset" title="{{::translate.load('gisengine.designer.dataset.select')}}">
		<div flex  class="innerExpander" layout-column> 
	 		<angular-table class="datasetLayer"
			id='table' ng-model="selectedDataset"
			columns='[{"label":"ID","name":"label"},{"label":"Name","name":"name"},{"label":"Description","name":"descr"},{"label":"Type","name":"dsTypeCd"}]'
			columns-search='["name"]' show-search-bar=false
			no-pagination="true"
			scope-functions='tableFunctionDataset'  speed-menu-option='datasetSpeedMenu'
			items-per-page="1"
			>
				<queue-table>
					<div layout="row"> 
						<span flex></span>
						<md-button ng-if="!$parent.isDatasetChosen" ng-click="scopeFunctions.choseDataset(true)">{{::scopeFunctions.translate.load('gisengine.designer.dataset.add')}}</md-button>
						<md-button ng-if="$parent.isDatasetChosen" ng-click="scopeFunctions.choseDataset(false)">{{::scopeFunctions.translate.load('gisengine.designer.dataset.change')}}</md-button>
						<!-- <md-button ng-if="!$parent.isDatasetChosen && !$parent.disableChooseDs" ng-click="scopeFunctions.choseDataset(true)">{{::scopeFunctions.translate.load('gisengine.designer.dataset.add')}}</md-button>
						<md-button ng-if="$parent.isDatasetChosen && !$parent.disableChooseDs" ng-click="scopeFunctions.choseDataset(false)">{{::scopeFunctions.translate.load('gisengine.designer.dataset.change')}}</md-button>-->
					</div>
				</queue-table> 
			</angular-table>	
   		</div>       
	</expander-box>

<!-- SINGLE SELECT FROM LAYER CATALOG  WHEN DATASET IS CHOSEN-->
	<expander-box id="layersList" color="white" background-color="#a9c3db" ng-if="isDatasetChosen" expanded="isOpenedLayer" title="{{::translate.load('gisengine.designer.layer.select')}}">
		<div flex  class="innerExpander" layout-column> 
	 		<angular-table class="datasetLayer"
			id='table' ng-model="selectedLayer"
			columns='[{"label":"ID","name":"layerId"},{"label":"Name","name":"name"},{"label":"Description","name":"descr"},{"label":"Type","name":"type"}]'
			columns-search='["name"]' show-search-bar=false
			no-pagination="true"
			scope-functions='tableFunctionSingleLayer' 
			>
				<queue-table>
					<div layout="row"> 
						<span flex></span>
						<md-button ng-click="scopeFunctions.loadListLayers()">{{::scopeFunctions.translate.load('gisengine.designer.layer.change')}}</md-button>
					</div>
				</queue-table> 
			</angular-table>	
   		</div>       
	</expander-box>
	<!-- MULTI SELECT LAYER  -->
	<expander-box id="layersListMultiSelect" color="white" background-color="#a9c3db" ng-if="!isDatasetChosen" expanded="isOpenedLayer" title="{{::translate.load('gisengine.designer.layer.select')}}">
		<div class="innerExpander" layout-column>  
	 		<angular-table 
			id='tableLayerMultiSelect' ng-model="selectedLayer"
			columns='[{"label":"ID","name":"layerId"},{"label":"Name","name":"name"},{"label":"Description","name":"descr"},{"label":"Type","name":"type"}]'
			columns-search='["name"]' show-search-bar=true no-pagination="false" items-per-page="5"
			scope-functions='tableFunctionMultiLayer' speed-menu-option='multipleLayerSpeedMenu'
			>
				<queue-table>
					<div layout="row"> 
						<span flex></span>
						<md-button ng-click="scopeFunctions.loadListLayers()">{{::scopeFunctions.translate.load('gisengine.designer.layer.add')}}</md-button>
					</div>
				</queue-table> 
			</angular-table>	
   		</div>       
	</expander-box>
	<!-- CHOSING LAYER FILTERS visible if there is no dataset-->
	<expander-box id="filterSelectBox" color="white" background-color="#a9c3db" ng-if="!isDatasetChosen && allDriverParamteres.length" expanded="false" title="{{::translate.load('gisengine.designer.layer.filters')}}">
		<div class="innerExpander" layout-column>  
			<angular-table
			id='tableDriver' ng-model="selectedDriverParamteres"
			columns='[{"label":"Driver parameter","name":"label"},{"label":"URL","name":"url"}]'
			columns-search='["name"]' show-search-bar=true
			scope-functions='tableFunctionFilters' speed-menu-option='filtersSpeedMenu'
			>
				<queue-table>
					<div layout="row"> 
						<span flex></span>
						<md-button ng-click="scopeFunctions.loadFilters()">{{::scopeFunctions.translate.load('gisengine.designer.layer.filters.add')}}</md-button>
					</div>
				</queue-table> 
			</angular-table>
		</div>      
	</expander-box>
	<!-- DATASET JOIN COLUMNS INTERFACE -->
	<expander-box id="datasetJoinBox" color="white" background-color="#a9c3db" ng-if="isDatasetChosen" expanded="false" title="{{::translate.load('gisengine.designer.dataset.joincolumns')}}">
	<div class="innerExpander" layout-column>  
	    <angular-table 
		id='datasetJoinColumnsTable' ng-model="datasetJoinColumns"
		columns='[{"label":"Dataset join column","name":"datasetColumnView","hideTooltip":true},{"label":"Layer join column","name":"layerColumnView","hideTooltip":true}]'
		columns-search='["datasetColumn","layerColumn"]' show-search-bar=true
		scope-functions='tableFunctionsJoin' speed-menu-option='datasetJoinSpeedMenu'
		allow-edit="true"
		>
			<queue-table>
				<div layout="row"> 
					<span flex></span>
					<md-button ng-click="scopeFunctions.addJoinColumn()">{{::scopeFunctions.translate.load('gisengine.designer.dataset.joincolumns.add')}}</md-button>
				</div>
			</queue-table> 
		</angular-table>	
	</div>
	</expander-box>
	<!-- DATASET INDICATORS -->
	<expander-box id="datasetIndicators" color="white" background-color="#a9c3db" ng-if="isDatasetChosen" expanded="false" title="{{::translate.load('gisengine.designer.dataset.indicators')}}">
	<div class="innerExpander" layout-column> 
		 <angular-table 
			id='indicatorsTable' ng-model="datasetIndicators"
			columns='indicatorColumns'
			columns-search='["indicatorName","indicatorLabel"]' show-search-bar=true
			scope-functions='tableFunctionIndicator' speed-menu-option='indicatorsSpeedMenu'
			allow-edit="true"
		>
			<queue-table>
				<div layout="row"> 
					<span flex></span>
					<md-button ng-click="scopeFunctions.addIndicator()">{{::scopeFunctions.translate.load('gisengine.designer.dataset.indicators.add')}}</md-button>
				</div>
			</queue-table> 
		</angular-table>	
	</div>
	</expander-box>
	<!-- DATASET FILTERS  -->
	<expander-box id="datasetFilters" color="white" background-color="#a9c3db" ng-if="isDatasetChosen" expanded="false" title="{{::translate.load('gisengine.designer.dataset.filters')}}">
	<div class="innerExpander" layout-column> 
		<angular-table 
		id='filtersTable' ng-model="datasetFilters"
		columns='filterColumns'
		columns-search='["dsFilterName","dsFilterLabel"]' show-search-bar=true
		scope-functions='tableFunctionDatasetFilters' speed-menu-option='dsFiltersSpeedMenu'
		allow-edit="true"
		>
			<queue-table>
				<div layout="row"> 
					<span flex></span>
					<md-button ng-click="scopeFunctions.addDatasetFilter()">{{::scopeFunctions.translate.load('gisengine.designer.dataset.dsfilters.add')}}</md-button>
				</div>
			</queue-table> 
		</angular-table>	
	</div>
	</expander-box>
	<!-- GEO CONFIG VISIBILITY SETTINGS -->
	<expander-box id="visibilitySettings" background-color="#a9c3db" aria-label="menu configuration" color="white" expanded="false" title="{{::translate.load('gisengine.designer.menuConfiguration')}}">
 	<div flex layout="row" layout-wrap> 
 	      
 	      <md-checkbox ng-model="visibility.showRightConfigMenu" aria-label="show right menu" flex=40 class="md-block">
            {{::translate.load('gisengine.designer.showRigtMenu')}}
          </md-checkbox> 
            <md-checkbox ng-model="visibility.showLegendButton" aria-label="show legend button" flex=40 class="md-block">
            {{::translate.load('gisengine.designer.showLegendButton')}}
          </md-checkbox> 
            <md-checkbox ng-model="visibility.showDistanceCalculator" aria-label="show distance calculator" flex=40 class="md-block">
            {{::translate.load('gisengine.designer.showDistanceCalculator')}}
          </md-checkbox> 
          
           
          <md-checkbox ng-model="visibility.showDownloadButton" aria-label="show download button" flex=40 class="md-block">
            {{::translate.load('gisengine.designer.showDownloadButton')}}
          </md-checkbox> 
              <md-checkbox ng-model="visibility.showSelectMode" ng-disabled="!visibility.showRightConfigMenu" aria-label="show select mode configuration" flex=40 class="md-block">
            {{::translate.load('gisengine.designer.showSelectMode')}}
          </md-checkbox> 
             <md-checkbox ng-model="visibility.showLayer" ng-disabled="!visibility.showRightConfigMenu" aria-label="show layer selection" flex=40 class="md-block">
            {{::translate.load('gisengine.designer.showLayer')}}
          </md-checkbox> 
             <md-checkbox ng-model="visibility.showBaseLayer" ng-disabled="!visibility.showRightConfigMenu" aria-label="show base layer selection" flex=40>
            {{::translate.load('gisengine.designer.showBaseLayer')}}
          </md-checkbox> 
          <md-checkbox ng-model="visibility.showMapConfig" ng-disabled="!visibility.showRightConfigMenu" aria-label="show map style configuration panel" flex=40>
            {{::translate.load('gisengine.designer.showMapConfig')}}
          </md-checkbox> 
           <md-checkbox ng-model="crossOption.crossNavigationMultiselect" ng-disabled="!visibility.showRightConfigMenu || !showCrossOptions" aria-label="active cross with multiselection" flex=40>
            {{::translate.load('gisengine.designer.showCrossMultiSelection')}}
          </md-checkbox> 
 	</div>
 	</expander-box>
 </md-whiteframe>
</body>
</html>