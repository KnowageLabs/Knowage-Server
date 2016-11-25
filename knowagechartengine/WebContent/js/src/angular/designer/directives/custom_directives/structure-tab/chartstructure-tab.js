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

/**
 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 */

angular.module('chartstructure-tab', [])
	.directive('chartstructureTab', function(sbiModule_config) {
		return {
			restrict: 'AE',
			replace: true,
			templateUrl: function(){
			      return sbiModule_config.contextName + '/js/src/angular/designer/directives/custom_directives/structure-tab/chartstructure-tab.html' 
		      },   
			controller: structureTabControllerFunction
		}	
	});

function structureTabControllerFunction($scope,sbiModule_translate,sbiModule_restServices,StructureTabService,ChartDesignerData){

	$scope.translate = sbiModule_translate;
	$scope.structureDetailsShown = false;
	$scope.structurePreviewFlex = 50;
	
	$scope.categoriesContainer = [];
	$scope.seriesContainers = [];
	
	$scope.numberOfSeriesContainers = 0;
	$scope.maxNumberOfSeriesContainers = 4;
	$scope.seriesContainersAliases = [];
	
	$scope.detailsForSeriesItem = {color: ""};
	
	// Indicator whether we should show the message that the maximum number of Series containers is exceeded
	$scope.showMaxNmbSerAxesExceeded = false;
		
	// Get all metadata of the chart's dataset (all measures and attributes)
	sbiModule_restServices.promiseGet("../api/1.0/jsonChartTemplate/fieldsMetadata", "")
		.then(function(response) {
			
			$scope.fieldsMetadata = response.data;
			
			var results = $scope.fieldsMetadata.results;
			
			for(var i=0; i<results.length; i++) {
				
				if (results[i].nature=="measure") {
					$scope.allMeasures.push(results[i]);					
				}
				else {
					$scope.allAttributes.push(results[i]);
				}
				
			}
			
			$scope.checkCategories();
			$scope.checkSeries();
			
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
	
	/**
	 * Show/hide the Structure Details panel.
	 */
	
	$scope.showStructureDetails = function(detailsForOption) {
		$scope.structureTabDetailsName = StructureTabService.getStructureTabDetailsName(detailsForOption);
		$scope.structureTabDetailsTemplateURL = StructureTabService.getSeriesItemsConfDetailsTemplateURL(detailsForOption);
		$scope.structurePreviewFlex = 25;
		$scope.structureDetailsShown = true;
	}
	
	$scope.hideStructureDetails = function() {
		$scope.structurePreviewFlex = 50;
		$scope.structureDetailsShown = false;
	}
	
	// Called when the user clicks on the attribute in its container, so the attribute can be used as a category in the chart.
	$scope.moveAttributeToCategories = function(item) {		
		if ($scope.categoriesContainer.indexOf(item.id)<0) {
			$scope.categoriesContainer.push(item.id)
		}		
	}
	 
	// Called when the user clicks on the measure in its container, so the measure can be used as a series item in the chart.
	$scope.moveMeasureToSeries = function(item,seriesContainer) {	
		
		// If we send an information about the particular Series container (happens when there are more more than 1 container)
		if (seriesContainer) {
			for (i=0; i<$scope.seriesContainers.length; i++) {				
				if ($scope.seriesContainers[i].name == seriesContainer.name) {
					if ($scope.seriesContainers[i].series.indexOf(item.id)<0) {
						$scope.seriesContainers[i].series.push(item.id);
					}
				}				
			}
		}
		// If we want to move measure into the only series container that we have
		else {
			if ($scope.seriesContainers[0].series.indexOf(item.id)<0) {
				$scope.seriesContainers[0].series.push(item.id);
			}
		}
				
	}	
	
	$scope.prepareSeriesContainersAliases = function() {
		
		// If the chart is already defined (it is NOT the new one - not yet persisted)
		if ($scope.chartTemplate) {
			
			var chartType = $scope.selectedChartType.toLowerCase();
			var typesWithMultipleSeriesContainersEnabled = $scope.seriesContainerAddAndRemoveIncludeTypes;
			var allChartAxes = $scope.chartTemplate.AXES_LIST.AXIS;
									
			// If the chart type is not GAUGE (in other words); if there is only one axis (only Series container)
			if (allChartAxes.length) {								
				for (i=0; i<allChartAxes.length; i++) {					
					if (allChartAxes[i].type.toLowerCase()=="serie") {
						$scope.seriesContainers.push({"name":allChartAxes[i].alias,"series":[]});
						$scope.numberOfSeriesContainers++;
					}					
				}				
			}
			else {
				$scope.numberOfSeriesContainers++;
				$scope.seriesContainers.push({"name":allChartAxes.alias,"series":[]});
			}
			
		}
		else {
			$scope.numberOfSeriesContainers++;
			$scope.seriesContainers.push({"name":"Y","series":[]});
			//console.log($scope.seriesContainers);
		}
		
	}
	
	$scope.checkSeriesForContainers = function() {
		
		$scope.prepareSeriesContainersAliases();
		
		if ($scope.chartTemplate) {
			
			var allSeries = $scope.chartTemplate.VALUES.SERIE;
			
			for (i=0; i<$scope.seriesContainers.length; i++) {
						
				if (allSeries.length) {
					for (j=0; j<allSeries.length; j++) {
						
						if ($scope.seriesContainers[i].name==allSeries[j].axis) {
							$scope.seriesContainers[i].series.push(allSeries[j].column);
						}
						
					}
				}
				else {
					$scope.seriesContainers[i].series.push(allSeries.column);
				}
				
			}
		}		
		
//		console.log($scope.seriesContainers);
		
	}
	
	// When user clicks on the button for editing the Series item configuration
	$scope.prepareSeriesItemConfiguration = function(item) {
				
		$scope.detailsForSeriesItem = null;
		$scope.detailsForSeriesTooltip = null;
		
		var allSeries = $scope.chartTemplate.VALUES.SERIE;
				
		for (i=0; i<allSeries.length; i++) {
			if (allSeries[i].column == item) {
				$scope.detailsForSeriesItem = allSeries[i];
				$scope.detailsForSeriesTooltip = $scope.seriesTooltipStyles[i];
				return;
			}
		}
		
	}
	
	$scope.prepareSeriesContainerConfiguration = function(seriesContainerAlias,option) {
		
		var allAxes = $scope.chartTemplate.AXES_LIST.AXIS;
		console.log(allAxes);
		
		for (i=0; i<allAxes.length; i++) {
			
			if (allAxes[i].alias == seriesContainerAlias) {
				
				$scope.detailsForSeriesContainer = allAxes[i];
				
				// Objects that hold the JSON form for styles for all axis configurations for the target axis
				$scope.detailsAxisConfigurationStyle = $scope.axisConfigurationStyles[i];
				$scope.detailsAxisTitleConfigurationStyle = $scope.axisTitleConfigurationStyles[i];
				$scope.detailsAxisMajorGridConfigurationStyle = $scope.axisMajorGridConfigurationStyles[i];
				$scope.detailsAxisMinorGridConfigurationStyle = $scope.axisMinorGridConfigurationStyles[i];
				
//				console.log($scope.detailsForSeriesContainer);
				console.log($scope.detailsAxisConfigurationStyle);
				console.log($scope.detailsAxisTitleConfigurationStyle);
				console.log($scope.detailsAxisMajorGridConfigurationStyle);
				console.log($scope.detailsAxisMinorGridConfigurationStyle);
				return;
			}
		}
		
	}
	
	/**
	 * Arrange categories that are available for the chart (set inside the chart template of the already existing chart)
	 * so that they populate their container (Category container).
	 */
	$scope.checkCategories = function() {		
		
		// If the chart is already defined (it is NOT the new one - not yet persisted)
		if ($scope.chartTemplate) {
			
			$scope.categoriesExist = $scope.chartTemplate.VALUES.CATEGORY ? true : false;
			
			if ($scope.categoriesExist) {
				
				var categoryTag = $scope.chartTemplate.VALUES.CATEGORY;
//				console.log(categoryTag.length);
				// If the CATEGORY tag contains an array (e.g. this goes for the SUNBURST chart type)
				if (categoryTag.length) {
					
					for (i=0; i<categoryTag.length; i++) {
						$scope.categoriesContainer.push(categoryTag[i].column);
					}
					
				}
				// If all (if there are more than one) categories are under the single tag (column and groupby properties (attributes))
				else {
					
					$scope.categoriesContainer.push(categoryTag.column);
					
					//console.log("categoryTag:",categoryTag);
					
					if (categoryTag.groupby.indexOf(",") > -1) {
						var groupBySplit = categoryTag.groupby.split(",");
						
						for (i=0; i<groupBySplit.length; i++) {
							$scope.categoriesContainer.push(groupBySplit[i]);
						}
						
					}
					else {
						categoryTag.groupby ? $scope.categoriesContainer.push(categoryTag.groupby) : null;
					}
					
					//console.log("categoriesContainer:",$scope.categoriesContainer);
				}
				
			}
			
		}
		else {
//			if ($scope.selectedChartType.toLowerCase()!="gauge") {
				$scope.categoriesExist = true;
//			}
		}
		
	}
	
	/**
	 * Arrange series that are available for the chart (set inside the chart template of the already existing chart)
	 * so that they populate their container(s) (Series container(s)).
	 */
	$scope.checkSeries = function() {
			
		$scope.checkSeriesForContainers();
		
		// If the chart is already defined (it is NOT the new one - not yet persisted)
		if ($scope.chartTemplate) {
			
			// All series available inside the chart (series of the chart template). We will use this to populate the series container.
			var series = [];
			// Series from chart template (from its JSON)
			var chartSeries = $scope.chartTemplate.VALUES.SERIE;
			
			chartSeries.length ? series = chartSeries : series.push(chartSeries);
			
			for (i=0; i<series.length; i++) {
				$scope.seriesContainer.push(series[i].column);
			}
			
			
		}
		
	}
	
	/**
	 * Operations for categories inside the Category container: move up, move down and delete item.
	 */
	
	$scope.categoryMoveUp = function(item) {
		var index = $scope.categoriesContainer.indexOf(item);
		var nextIndex = index-1;
		var temp = $scope.categoriesContainer[index];
		$scope.categoriesContainer[index] = $scope.categoriesContainer[nextIndex];
		$scope.categoriesContainer[nextIndex] = temp;
	}
	
	$scope.categoryMoveDown = function(item) {
		var index = $scope.categoriesContainer.indexOf(item);
		var nextIndex = index+1;
		var temp = $scope.categoriesContainer[index];
		$scope.categoriesContainer[index] = $scope.categoriesContainer[nextIndex];
		$scope.categoriesContainer[nextIndex] = temp;
	}
	
	$scope.categoryRemove = function(indexOfItem) {
		$scope.categoriesContainer.splice(indexOfItem,1);
	}
	
	$scope.categoryRemoveAll = function() {
		$scope.categoriesContainer = [];
	}
	
	/**
	 * Operations for series inside the Series container: move up, move down and delete item.
	 */
	
	$scope.seriesItemMoveUp = function(item,seriesContainer) {		
		for (i=0; i<$scope.seriesContainers.length; i++) {		
			if ($scope.seriesContainers[i].name == seriesContainer.name) {				
				var index = $scope.seriesContainers[i].series.indexOf(item);
				var nextIndex = index-1;
				var temp = $scope.seriesContainers[i].series[index];
				$scope.seriesContainers[i].series[index] = $scope.seriesContainers[i].series[nextIndex];
				$scope.seriesContainers[i].series[nextIndex] = temp;	
				return;
			}			
		}		
	}
	
	$scope.seriesItemMoveDown = function(item,seriesContainer) {
		for (i=0; i<$scope.seriesContainers.length; i++) {		
			if ($scope.seriesContainers[i].name == seriesContainer.name) {				
				var index = $scope.seriesContainers[i].series.indexOf(item);
				var nextIndex = index+1;
				var temp = $scope.seriesContainers[i].series[index];
				$scope.seriesContainers[i].series[index] = $scope.seriesContainers[i].series[nextIndex];
				$scope.seriesContainers[i].series[nextIndex] = temp;	
				return;
			}			
		}
	}
	
	$scope.seriesItemRemove = function(seriesItem,seriesContainerName) {
				
		// Go through all Series containers in order to focus on the one from which we want to remove a series item.
		for (i=0; i<$scope.seriesContainers.length; i++) {
			
			if ($scope.seriesContainers[i].name == seriesContainerName) {
				
				// Go through all it's series items in order to eliminate the one that is aimed to be removed.
				for (j=0; j<$scope.seriesContainers[i].series.length; j++) {
					
					if ($scope.seriesContainers[i].series[j] == seriesItem) {
						$scope.seriesContainers[i].series.splice(j,1);
						return;
					}
					
				}
				
			}
			
		}
		
	}
	
	$scope.seriesRemoveAll = function(seriesContainer) {
		
		// Go through all Series containers in order to focus on the one from which we want to remove a series item.
		for (i=0; i<$scope.seriesContainers.length; i++) {			
			if ($scope.seriesContainers[i].name == seriesContainer.name) {				
				$scope.seriesContainers[i].series = [];	
				break;
			}			
		}
		
	}
	
	$scope.addSeriesContainer = function() {
		
		// Number of Series containers
		var nmbOfSerConts = $scope.seriesContainers.length;
		
		if (nmbOfSerConts < $scope.maxNumberOfSeriesContainers) {
			
			var lastSerContName = $scope.seriesContainers[nmbOfSerConts-1].name;
			var newSerContName = "Axis_";	// The prefix of the name of the new Series container that will be added to Designer
			
			if (lastSerContName!="Y") {
				var splitLastSerContName = lastSerContName.split("_");
				newSerContName += (Number(splitLastSerContName[1])+1);
			}
			else {
				newSerContName += "1";
			}
			
			$scope.seriesContainers.push({name:newSerContName, series:[]});
			
			// If we reach the maximum number of Series containers and we still click on plus to add a new one, set the indicator that should show the info.
			$scope.showMaxNmbSerAxesExceeded = $scope.seriesContainers.length==$scope.maxNumberOfSeriesContainers ? true : false;
			
			$scope.numberOfSeriesContainers++;
			
		}
		
	}
	
	$scope.removeSeriesContainer = function(seriesContainer) {
		
		// Go through all Series containers in order to focus on the one from which we want to remove a series item.
		for (i=0; i<$scope.seriesContainers.length; i++) {			
			if ($scope.seriesContainers[i].name == seriesContainer.name) {				
				$scope.seriesContainers.splice(i,1);
				$scope.showMaxNmbSerAxesExceeded = false;
				$scope.numberOfSeriesContainers--;
				break;
			}			
		}
		
	}
	
	// Function that enables the drop-down menu functionality 
	$scope.openMenu = function($mdOpenMenu, ev) {
	      originatorEv = ev;
	      $mdOpenMenu(ev);
    };
    
    /**
     * Manage visibility (show/hide) of the options for the Series container.
     */
    
    // TODO: we need also to take care if the series container is the main (for plus)/additional (for X)
    $scope.seriesContainerAddAndRemoveIncludeTypes = ["bar","line"];   
    
    $scope.seriesContainerConfigDropDownExcludeTypes = ["pie","sunburst","treemap","wordcloud"];
    
    $scope.seriesAxisTitleExcludeTypes = ["chord","parallel"];
    $scope.seriesAxisMajorMinorGirdExcludeTypes = ["chord","gauge","heatmap","parallel"];
	
    /**
     * Manage visibility (show/hide) of the options for the Series container.
     */
    
    // TODO: Check if these chart types are the only one that for which we should exclude the Ordering column option in the categories drop-down menu 
    $scope.categoriesContainerConfigDropDownExcludeTypes = ["wordcloud","treemap"];
    
    $scope.categoriesOrderColumnExcludeTypes = ["parallel","chord"];    
    $scope.categoriesConfigExcludeTypes = ["pie","sunburst"];    
    $scope.categoriesTitleConfigExcludeTypes = ["parallel","pie","sunburst","chord"];
    
    $scope.seriesItemTypes = StructureTabService.getSeriesItemTypes();
    $scope.seriesItemOrderingTypes = StructureTabService.getSeriesItemOrderingTypes();
    $scope.scaleFactorsFixed = StructureTabService.getScaleFactorsFixed();
    
    // seriesTooltipStyles - an array that will hold deparsed 'style' data for the TOOLTIP of all series items.
    $scope.seriesTooltipStyles = [];
    $scope.seriesTooltipStyle = StructureTabService.getSeriesTooltipStyle();
        
    $scope.textAlignment = ChartDesignerData.getAlignTypeOptions();
    $scope.fontFamily = ChartDesignerData.getFontFamilyOptions();
    $scope.fontStyle = ChartDesignerData.getFontStyleOptions();
    $scope.fontSize = ChartDesignerData.getFontSizeOptions();
    
    $scope.seriesItemAggregationTypes = StructureTabService.getSeriesItemAggregationTypes();
    
    // axisConfigurationStyles - an array that will hold deparsed 'style' data for the AXIS of all series items.
    $scope.axisConfigurationStyles = [];
    $scope.axisConfigurationStyle = StructureTabService.getAxisConfigurationStyle();
    
    // axisTitleConfigurationStyles - an array that will hold deparsed 'style' data for the AXIS->TITLE of all series items.
    $scope.axisTitleConfigurationStyles = [];
    $scope.axisTitleConfigurationStyle = StructureTabService.getAxisTitleConfigurationStyle();
    
    // axisMinorGridConfigurationStyles - an array that will hold deparsed 'style' data for the AXIS->MAJORGRID of all series items.
    $scope.axisMinorGridConfigurationStyles = [];
    $scope.axisMinorGridConfigurationStyle = StructureTabService.getAxisMinorGridConfigurationStyle();
    
    // axisMajorGridConfigurationStyles - an array that will hold deparsed 'style' data for the AXIS->MINORGRID of all series items.
    $scope.axisMajorGridConfigurationStyles = [];
    $scope.axisMajorGridConfigurationStyle = StructureTabService.getAxisMajorGridConfigurationStyle();
    
}