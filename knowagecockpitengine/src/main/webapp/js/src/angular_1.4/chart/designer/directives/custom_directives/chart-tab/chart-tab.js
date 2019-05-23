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
.directive('chartTab', function(sbiModule_config) {
	return {
		restrict: 'AE',
		replace: true,
		templateUrl: function(){
		      return sbiModule_config.contextName + '/js/src/angular_1.4/chart/designer/directives/custom_directives/chart-tab/chart-tab.html'
	      },
		controller: chartTabControllerFunction
	}

});

function chartTabControllerFunction($scope,$timeout,sbiModule_translate,sbiModule_restServices,sbiModule_messaging,ChartDesignerData,StructureTabService,chartEngineSettings){
	$scope.translate = sbiModule_translate;
	$scope.datasetLabel = datasetLabel;
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
				$scope.minMaxSeries.max = 1;
				$scope.minMaxSeries.min = 1;
			}else {
				$scope.minMaxCategories.min = 1;
				$scope.minMaxSeries.min = 1;
			}
		}

	},true)


	var setConfigurationButtons = function(type) {

		switch (type) {
		case 'parallel':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'sunburst':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'scatter':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'treemap':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'wordcloud':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'gauge':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'line':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'heatmap':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'radar':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'bar':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'pie':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'chord':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		default:
			break;
		}

}

	$scope.getChartTemplate = function (type) {
		var base = "";
		switch (type) {
			case 'parallel':
				base = StructureTabService.getParallelTemplate();
				break;
			case 'sunburst':
				base = StructureTabService.getSunburstTemplate();
				break;
			case 'scatter':
				base = StructureTabService.getScatterTemplate();
				break;
			case 'treemap':
				base = StructureTabService.getTreemapTemplate();
				break;
			case 'wordcloud':
				base = StructureTabService.getWordCloudTemplate();
				break;
			case 'gauge':
				base = StructureTabService.getGaugeTemplate();
				break;
			case 'line':
				base = StructureTabService.getBaseTemplate();
				break;
			case 'heatmap':
				base = StructureTabService.getHeatmapTemplate();
				break;
			case 'radar':
				base = StructureTabService.getRadarTemplate();
				break;
			case 'bar':
				base = StructureTabService.getBaseTemplate();
				break;
			case 'pie':
				base = StructureTabService.getBaseTemplate();
				break;
			case 'chord':
				base = StructureTabService.getChordTemplate();
				break;
			default:
				break;
		}
		return base;
	};

	var checkDocumentType = function(){

		if($scope.isCockpitEng){

			if (parent.angular.element(window.frameElement).scope().localMod.chartTemplate!=undefined) {
				var chart = angular.copy(parent.angular.element(window.frameElement).scope().localMod.chartTemplate.CHART);

				$scope.chartTemplate = angular.copy(chart);

				$scope.selectedChartType = $scope.chartTemplate.type.toLowerCase();

				if($scope.selectedChartType == 'scatter' && !$scope.chartTemplate.VALUES.SERIE[0].TOOLTIP.tooltipExpression){
					$scope.chartTemplate.VALUES.SERIE[0].TOOLTIP.tooltipExpression = ""
				}

				var categoryBackup = angular.copy($scope.chartTemplate.VALUES.CATEGORY);
                $scope.chartTemplate = angular.merge({}, $scope.getChartTemplate($scope.chartTemplate.type.toLowerCase()),$scope.chartTemplate)
                $scope.chartTemplate.VALUES.CATEGORY = categoryBackup;

				setConfigurationButtons($scope.chartTemplate.type.toLowerCase());
				parent.angular.element(window.frameElement).scope().localMod.chartTemplate = $scope.chartTemplate
			} else {

				$scope.chartTemplate = StructureTabService.getBaseTemplate();
				$scope.chartTemplate.alpha = chartEngineSettings.tree_D_Options.alpha;
				$scope.chartTemplate.beta = chartEngineSettings.tree_D_Options.beta;
				$scope.chartTemplate.depth = chartEngineSettings.tree_D_Options.depth;
				$scope.chartTemplate.viewDistance = chartEngineSettings.tree_D_Options.viewDistance;
				$scope.chartTemplate.isCockpitEngine = $scope.isCockpitEng;
				parent.angular.element(window.frameElement).scope().localMod.chartTemplate = $scope.chartTemplate;


				$scope.selectedChartType = $scope.chartTemplate.type.toLowerCase();
				setConfigurationButtons($scope.chartTemplate.type.toLowerCase());
			}
		}else{

			var templateObj = angular.fromJson(template);
			$scope.chartTemplate = templateObj.CHART;
			console.log("chart template: ",$scope.chartTemplate);

			if ($scope.chartTemplate) {

				$scope.selectedChartType = $scope.chartTemplate.type.toLowerCase();
				if($scope.selectedChartType == 'scatter' && !$scope.chartTemplate.VALUES.SERIE[0].TOOLTIP.tooltipExpression){
					$scope.chartTemplate.VALUES.SERIE[0].TOOLTIP.tooltipExpression = ""
				}

				var categoryBackup = angular.copy($scope.chartTemplate.VALUES.CATEGORY);
                $scope.chartTemplate = angular.merge({}, $scope.getChartTemplate($scope.chartTemplate.type.toLowerCase()),$scope.chartTemplate)
                $scope.chartTemplate.VALUES.CATEGORY = categoryBackup;

				setConfigurationButtons($scope.selectedChartType);

			}
			else {

				$scope.chartTemplate = StructureTabService.getBaseTemplate();
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
			if($scope.chartTypes[i].toUpperCase()==$scope.chartTemplate.type.toUpperCase()){
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
			if($scope.categories.length>2){
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
			if(series.length>1) {
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
		var serie = $scope.chartTemplate.VALUES.SERIE;
		angular.copy($scope.getChartTemplate($scope.selectedChartType), $scope.chartTemplate);
		$scope.chartTemplate.VALUES.SERIE = serie;
		$scope.chartTemplate.isCockpitEngine = $scope.isCockpitEng;
		switch (chart) {
		case 'parallel':
			ifNeededTrimDownCategoriesToSizeNeededByChartType();
			break;
		case 'sunburst':
			ifNeededTrimDownCategoriesToSizeNeededByChartType();
			break;
		case 'scatter':
			ifNeededTrimDownCategoriesToSizeNeededByChartType();
			break;
		case 'treemap':
			ifNeededTrimDownSeriesToSizeNeededByChartType();
			break;
		case 'wordcloud':
			ifNeededTrimDownCategoriesToSizeNeededByChartType();
			ifNeededTrimDownSeriesToSizeNeededByChartType();
			break;
		case 'line':
			$scope.chartTemplate.type="LINE";
			break;
		case 'heatmap':
			ifNeededTrimDownCategoriesToSizeNeededByChartType();
			break;
		case 'radar':
			ifNeededTrimDownCategoriesToSizeNeededByChartType();
			break;
		case 'bar':
			$scope.chartTemplate.alpha = chartEngineSettings.tree_D_Options.alpha;
			$scope.chartTemplate.beta = chartEngineSettings.tree_D_Options.beta;
			$scope.chartTemplate.depth = chartEngineSettings.tree_D_Options.depth;
			$scope.chartTemplate.viewDistance = chartEngineSettings.tree_D_Options.viewDistance;
			break;
		case 'pie':
			$scope.chartTemplate.type="PIE";
			$scope.chartTemplate.alpha = chartEngineSettings.tree_D_Options.alpha;
			$scope.chartTemplate.beta = chartEngineSettings.tree_D_Options.beta;
			$scope.chartTemplate.depth = chartEngineSettings.tree_D_Options.depth;
			ifNeededTrimDownCategoriesToSizeNeededByChartType();
			ifNeededTrimDownSeriesToSizeNeededByChartType();
			break;
		case 'chord':
			ifNeededTrimDownCategoriesToSizeNeededByChartType();
			ifNeededTrimDownSeriesToSizeNeededByChartType();
			break;
		default:
			break;
		}
		$scope.changeStyle(styleName);
		$scope.chartTemplate.styleName = $scope.clearStyleTag(styleName);

		//$scope.nodeOptions.refresh();
		setConfigurationButtons($scope.selectedChartType);
		$scope.selectedConfigurationButton = "";
	}


}