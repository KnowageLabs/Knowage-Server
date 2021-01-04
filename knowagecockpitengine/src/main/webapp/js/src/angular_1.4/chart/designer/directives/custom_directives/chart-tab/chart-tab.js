/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

angular.module('chart-tab', [])
.directive('chartTab', function(sbiModule_config,chartDesignerBasePath) {
	return {
		restrict: 'AE',
		replace: true,
		templateUrl: function(){
		      return chartDesignerBasePath + '/directives/custom_directives/chart-tab/chart-tab.html'
	      },
		controller: chartTabControllerFunction
	}

});

function chartTabControllerFunction($scope,$timeout,sbiModule_translate,sbiModule_restServices,sbiModule_messaging,ChartDesignerData,StructureTabService,chartEngineSettings,chartBackwardCompatibilityService){
	$scope.translate = sbiModule_translate;
	$scope.chartLibNamesConfig = chartLibNamesConfig;
	$scope.selectedChartType;
	$scope.libInUse;
	$scope.minMaxCategories = {};
	$scope.minMaxSeries = {};
	$scope.$watch('selectedChartType',function(newValue,oldValue){

		$scope.libInUse=$scope.chartLibNamesConfig[$scope.selectedChartType];
		$scope.minMaxCategories = {};
		$scope.minMaxSeries = {};
		switch(newValue){
		case 'parallel':
			$scope.minMaxCategories.min = 1;
			$scope.minMaxSeries.min = 2;
			break;
		case 'sunburst':
			$scope.minMaxCategories.min = 2;
			$scope.minMaxSeries.min = 1;
			break;
		case 'scatter':
			$scope.minMaxCategories.min = 1;
			$scope.minMaxCategories.max = 1;
			$scope.minMaxSeries.min = 1;
			break;
		case 'treemap':
			$scope.minMaxCategories.min = 2;
			$scope.minMaxSeries.min = 1;
			$scope.minMaxSeries.max = 1;
			break;
		case 'wordcloud':
			$scope.minMaxCategories.min = 1;
			$scope.minMaxCategories.max = 1;
			$scope.minMaxSeries.min = 1;
			$scope.minMaxSeries.max = 1;
			break;
		case 'gauge':
			$scope.minMaxSeries.min = 1;
			break;
		case 'line':
			$scope.minMaxCategories.min = 1;
			$scope.minMaxSeries.min = 1;
			break;
		case 'heatmap':
			$scope.minMaxCategories.min = 2;
			$scope.minMaxCategories.max = 2;
			$scope.minMaxSeries.min = 1;
			$scope.minMaxSeries.max = 1;
			break;
		case 'radar':
			$scope.minMaxCategories.min = 1;
			$scope.minMaxSeries.min = 1;
			break;
		case 'bubble':
			$scope.minMaxCategories.min = 1;
			$scope.minMaxSeries.min = 1;
			break;
		case 'bar':
			$scope.minMaxCategories.min = 1;
			$scope.minMaxSeries.min = 1;
			break;
		case 'pie':
			if($scope.chartLibNamesConfig.pie=="chartJs") {
				$scope.minMaxCategories.min = 1;
				$scope.minMaxSeries.min = 1;
				$scope.minMaxSeries.max = 1;
			} else {
				$scope.minMaxCategories.min = 1;
				$scope.minMaxSeries.min = 1;
			}
			break;
		case 'chord':
			$scope.minMaxCategories.min = 2;
			$scope.minMaxCategories.max = 2;
			$scope.minMaxSeries.min = 1;
			$scope.minMaxSeries.max = 1;
			break;
		default:
			break;
		}
	});

	$scope.$watch('chartTemplate',function(newValue,oldValue){
		if($scope.chartTemplate.type.toLowerCase()=="bar" || $scope.chartTemplate.type.toLowerCase()=="line" || $scope.chartTemplate.type.toLowerCase()=="radar"){
			$scope.minMaxCategories = {};
			$scope.minMaxSeries = {};
			if(newValue.groupCategories){
				$scope.minMaxCategories.max = 2;
				$scope.minMaxCategories.min = 2;
				$scope.minMaxSeries.min = 1;
			} else if (newValue.groupSeries){
				$scope.minMaxCategories.max = 1;
				$scope.minMaxCategories.min = 1;
				$scope.minMaxSeries.max = 2;
				$scope.minMaxSeries.min = 2;
			} else if (newValue.groupSeriesCateg){
				$scope.minMaxCategories.max = 2;
				$scope.minMaxCategories.min = 2;
				//$scope.minMaxSeries.max = 1;
				$scope.minMaxSeries.min = 1;
			}else {
				$scope.minMaxCategories.min = 1;
				$scope.minMaxSeries.min = 1;
			}
		} else if($scope.chartTemplate.type.toLowerCase()=="bubble"){
			if (newValue.groupSeriesCateg){
				$scope.minMaxCategories.max = 2;
				$scope.minMaxCategories.min = 1;
				//$scope.minMaxSeries.max = 1;
				$scope.minMaxSeries.min = 1;
			}
		}

	},true)


	var setConfigurationButtons = function(type) {
		$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
	}

	var checkDocumentType = function(){

		if($scope.isCockpitEng){

			if ($scope.localMod.chartTemplate!=undefined) {
				var chart = angular.copy($scope.localMod.chartTemplate.CHART);

				$scope.chartTemplate = angular.copy(chart);
				$scope.localMod.chartTemplate = $scope.chartTemplate;

				$scope.selectedChartType = $scope.chartTemplate.type.toLowerCase();
				chartBackwardCompatibilityService.updateTemplate($scope.chartTemplate,$scope.enterpriseEdition);
				setConfigurationButtons($scope.chartTemplate.type.toLowerCase());
			} else {

				$scope.chartTemplate = StructureTabService.getBaseTemplate('bar');
				$scope.chartTemplate.alpha = chartEngineSettings.tree_D_Options.alpha;
				$scope.chartTemplate.beta = chartEngineSettings.tree_D_Options.beta;
				$scope.chartTemplate.depth = chartEngineSettings.tree_D_Options.depth;
				$scope.chartTemplate.viewDistance = chartEngineSettings.tree_D_Options.viewDistance;
				$scope.chartTemplate.isCockpitEngine = $scope.isCockpitEng;
				$scope.localMod.chartTemplate = $scope.chartTemplate;


				$scope.selectedChartType = $scope.chartTemplate.type.toLowerCase();
				setConfigurationButtons($scope.chartTemplate.type.toLowerCase());
			}
		}else{

			var templateObj = angular.fromJson(template);
			$scope.chartTemplate = templateObj.CHART;
			console.log("chart template: ",$scope.chartTemplate);

			if ($scope.chartTemplate) {

				$scope.selectedChartType = $scope.chartTemplate.type.toLowerCase();
				chartBackwardCompatibilityService.updateTemplate($scope.chartTemplate);
				setConfigurationButtons($scope.selectedChartType);

			}
			else {

				$scope.chartTemplate = StructureTabService.getBaseTemplate('bar');
				/*
					@author: radmila.selakovic@mht.net
					adding options for 3D
				*/
				$scope.chartTemplate.alpha = chartEngineSettings.tree_D_Options.alpha;
				$scope.chartTemplate.beta = chartEngineSettings.tree_D_Options.beta;
				$scope.chartTemplate.depth = chartEngineSettings.tree_D_Options.depth;
				$scope.chartTemplate.viewDistance = chartEngineSettings.tree_D_Options.viewDistance;
				$scope.chartTemplate.isCockpitEngine = $scope.isCockpitEng;
				$scope.selectedChartType = $scope.chartTemplate.type.toLowerCase();
				setConfigurationButtons($scope.chartTemplate.type.toLowerCase());
			}
		}


	}

	checkDocumentType();
	sbiModule_restServices.promiseGet("../api/1.0/chart/pages/types", "")
	.then(function(response) {
		$scope.chartTypes = response.data.types;
		var typeExculded = ["gauge", "heatmap","treemap", "sunburst", "chord"];
		var index;
		if($scope.isRealTimeDataset ){
			for (var i=0; i<typeExculded.length; i++) {
			    index =  $scope.chartTypes.indexOf(typeExculded[i]);
			    if (index > -1) {
			    	$scope.chartTypes.splice(index, 1);
			    }
			}
		}

		$scope.chartTypes.sort();
		for (var i = 0; i < $scope.chartTypes.length; i++) {
			if($scope.chartTypes && $scope.chartTypes[i].toUpperCase()==$scope.chartTemplate.type.toUpperCase()){
				$scope.selectedChartType = $scope.chartTypes[i];
				setConfigurationButtons($scope.selectedChartType);
			}
		}

	}, function(response) {

		var message = "";

		if (response.status==500) {
			message = response.statusText;
		}
		else {
			message = response.data.errors[0].message;
		}

		sbiModule_messaging.showErrorMessage(message, 'Error');

	});

	sbiModule_restServices.promiseGet("../api/chart/style", "")
	.then(function(response) {
		$scope.chartStyles = response.data;
		getDefaultStyleForEmpty();

	}, function(response) {

		var message = "";

		if (response.status==500) {
			message = response.statusText;
		}
		else {
			message = response.data.errors[0].message;
		}

		sbiModule_messaging.showErrorMessage(message, 'Error');

	});

	$scope.getObjectProperties = function (obj1, obj2) {
		for (var attrname in obj2) {
			if(!(typeof obj1[attrname] == 'object')){
				obj1[attrname] = obj2[attrname];
			} else {
				$scope.getObjectProperties(obj1[attrname], obj2[attrname]);
			}

		}
	}

	var getDefaultStyleForEmpty = function() {
		$scope.styleTemplate = "";
		var type = $scope.chartTemplate.type.toLowerCase();
		if($scope.chartTemplate.styleName=="default"){
			for (var i = 0; i < $scope.chartStyles.length; i++) {
				if($scope.chartStyles[i].CHARTSTYLE.isDefault){
					$scope.styleTemplate = $scope.chartStyles[i].CHARTSTYLE.TEMPLATE;
					var genericStyle = $scope.styleTemplate.generic.CHART;
					var chartSpecificStyle = $scope.styleTemplate[type].CHART;
					for (var attrname in chartSpecificStyle) {
						genericStyle[attrname] = chartSpecificStyle[attrname];
					}

					$scope.getObjectProperties($scope.chartTemplate, genericStyle);

				}
			}
		} else {
			for (var i = 0; i < $scope.chartStyles.length; i++) {
				if($scope.chartStyles[i].CHARTSTYLE.name==$scope.chartTemplate.styleName){
					$scope.styleTemplate = $scope.chartStyles[i].CHARTSTYLE.TEMPLATE;
				}

			}
		}
	}

	$scope.filterStyles = function(item) {
		return item.CHARTSTYLE.name != 'default' || $scope.chartStyles.length==1
	}

	$scope.changeStyle = function(style) {
		if($scope.checkChanged() == -1){
			console.log("Form is clean")
		}else{
			console.log($scope.checkChanged());
		}
		var type = $scope.chartTemplate.type.toLowerCase();
		if(typeof style == 'object'){
			$scope.styleTemplate = style.CHARTSTYLE.TEMPLATE;
		} else {
			return;
		}
		var genericStyle = $scope.styleTemplate.generic.CHART;
		var chartSpecificStyle = $scope.styleTemplate[type].CHART;
		for (var attrname in chartSpecificStyle) {
			genericStyle[attrname] = chartSpecificStyle[attrname];
		}


		$scope.getObjectProperties($scope.chartTemplate, genericStyle);
	}
	$scope.selectChartType = function(chart) {

		var ifNeededTrimDownCategoriesToSizeNeededByChartType = function () {
			var categoriesLimit = 0;
			if(chart == "pie" || chart == "sunburst" || chart == "wordcloud" || chart == "scatter") {
				categoriesLimit = 1;
			} else if (chart == "chord" || chart == "heatmap" || chart == "parallel") {
				categoriesLimit = 2;
			}
			//if there are more than 2 selected categories trim down to first two categories, since 2 is the exact number of categories a chord/heatmap must take, max for parallel is 2
			if(categoriesLimit > 0 && $scope.categories.length>2){
				var newCategories = [];
				for (var i = 0; i < categoriesLimit; i++) {
					if(i<2){
						newCategories.push($scope.categories[i]);
					}
				}
				$scope.categories = newCategories;
			}
		}
		var ifNeededTrimDownSeriesToSizeNeededByChartType = function () {
			var seriesLimit = 0;
			if(chart == "chord" || chart == "treemap" || chart == "wordcloud") {
				seriesLimit = 1;
			} else if (chart=="pie") {
				seriesLimit = 4;
			}
			var series = $scope.chartTemplate.VALUES.SERIE;
			if(seriesLimit > 0 && series.length>1) {
				var newSeries = [];
				for (var i = 0; i < seriesLimit; i++) {
					if(i<1){
						newSeries.push(series[i]);
					}
				}
				$scope.chartTemplate.VALUES.SERIE = newSeries;
			}

		}
		$scope.selectedChartType = chart;
		var styleName = $scope.chartTemplate.styleName;
		var serie = "";
		serie = $scope.chartTemplate.VALUES.SERIE;
		switch (chart) {
		case 'parallel':
			angular.copy(StructureTabService.getParallelTemplate(), $scope.chartTemplate);
			break;
		case 'sunburst':
			angular.copy(StructureTabService.getSunburstTemplate(), $scope.chartTemplate);
			break;
		case 'scatter':
			angular.copy(StructureTabService.getScatterTemplate(), $scope.chartTemplate);
			break;
		case 'treemap':
			angular.copy( StructureTabService.getTreemapTemplate(), $scope.chartTemplate);
			break;
		case 'wordcloud':
			angular.copy(StructureTabService.getWordCloudTemplate(), $scope.chartTemplate);
			break;
		case 'gauge':
			angular.copy(StructureTabService.getGaugeTemplate(), $scope.chartTemplate);
			break;
		case 'line':
			angular.copy(StructureTabService.getBaseTemplate(chart), $scope.chartTemplate);
			$scope.chartTemplate.type="LINE";
			break;
		case 'heatmap':
			angular.copy(StructureTabService.getHeatmapTemplate(), $scope.chartTemplate);
			break;
		case 'radar':
			angular.copy(StructureTabService.getRadarTemplate(), $scope.chartTemplate);
			break;
		case 'bar':
			angular.copy(StructureTabService.getBaseTemplate(chart), $scope.chartTemplate);
			$scope.chartTemplate.alpha = chartEngineSettings.tree_D_Options.alpha;
			$scope.chartTemplate.beta = chartEngineSettings.tree_D_Options.beta;
			$scope.chartTemplate.depth = chartEngineSettings.tree_D_Options.depth;
			$scope.chartTemplate.viewDistance = chartEngineSettings.tree_D_Options.viewDistance;
			break;
		case 'bubble':
			angular.copy(StructureTabService.getBubbleTemplate(), $scope.chartTemplate);
			$scope.chartTemplate.type="BUBBLE";
			break;
		case 'pie':
			angular.copy(StructureTabService.getBaseTemplate(chart), $scope.chartTemplate);
			$scope.chartTemplate.type="PIE";
			$scope.chartTemplate.alpha = chartEngineSettings.tree_D_Options.alpha;
			$scope.chartTemplate.beta = chartEngineSettings.tree_D_Options.beta;
			$scope.chartTemplate.depth = chartEngineSettings.tree_D_Options.depth;
			break;
		case 'chord':
			angular.copy(StructureTabService.getChordTemplate(), $scope.chartTemplate);
			break;
		default:
			break;
		}
		$scope.chartTemplate.isCockpitEngine = $scope.isCockpitEng;
		for (var s in serie) {
			for (var property in $scope.chartTemplate.VALUES.SERIE[0]) {
				if (!serie[s].hasOwnProperty(property)) {
					serie[s][property] = $scope.chartTemplate.VALUES.SERIE[0][property]
				}
			}
		}
		$scope.chartTemplate.VALUES.SERIE =serie
		$scope.numberOfSeriesContainers = 0;
		$scope.checkSeriesForContainers()

		ifNeededTrimDownCategoriesToSizeNeededByChartType();
		ifNeededTrimDownSeriesToSizeNeededByChartType();
		$scope.changeStyle(styleName);
		$scope.chartTemplate.styleName = $scope.clearStyleTag(styleName);

		//$scope.nodeOptions.refresh();
		setConfigurationButtons($scope.selectedChartType);
		$scope.selectedConfigurationButton = "";
	}

}