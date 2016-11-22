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

function structureTabControllerFunction($scope,sbiModule_translate,sbiModule_restServices ){

	$scope.translate = sbiModule_translate;
	$scope.structureDetailsShown = false;
	$scope.structurePreviewFlex = 50;
	
	$scope.categoriesContainer = [];
	$scope.seriesContainers = [];
	
	$scope.numberOfSeriesContainers = 1;
	$scope.maxNumberOfSeriesContainers = 4;
	$scope.seriesContainersAliases = [];
		
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
	
	$scope.showStructureDetails = function() {
		$scope.structureDetailsShown = true;
	}
	
	$scope.hideStructureDetails = function() {
		$scope.structureDetailsShown = false;
	}
	
	$scope.changeStructureDetailsFlex = function() {
		$scope.structurePreviewFlex = 25;
	}
	
	// Called when the user clicks on the attribute in its container, so the attribute can be used as a category in the chart.
	$scope.moveAttributeToCategories = function(item) {
		
		if ($scope.categoriesContainer.indexOf(item.id)<0) {
			$scope.categoriesContainer.push(item.id)
		}
		
	}
	 
	// Called when the user clicks on the measure in its container, so the measure can be used as a series item in the chart.
	$scope.moveMeasureToSeries = function(item) {
				
		if ($scope.seriesContainer.indexOf(item.id)<0) {
			$scope.seriesContainer.push(item.id)
		}
		
	}	
	
	$scope.prepareSeriesContainersAliases = function() {
		
		// If the chart is already defined (it is NOT the new one - not yet persisted)
		if ($scope.chartTemplate) {
			
			var chartType = $scope.selectedChartType.toLowerCase();
			var typesWithMultipleSeriesContainersEnabled = $scope.seriesContainerAddAndRemoveIncludeTypes;
			var allChartAxes = $scope.chartTemplate.AXES_LIST.AXIS;
			
			var counterOfSeriesContainers = 0;
			
			if (typesWithMultipleSeriesContainersEnabled.indexOf(chartType)>=0 && allChartAxes.length>2) {
								
				for (i=0; i<allChartAxes.length; i++) {
					
					if (allChartAxes[i].type.toLowerCase()=="serie") {
						$scope.seriesContainers.push({"name":allChartAxes[i].alias,"series":[]});
					}
					
				}
				
			}
			
		}
		
	}
	
	$scope.checkSeriesForContainers = function() {
		
		$scope.prepareSeriesContainersAliases();
		
		var allSeries = $scope.chartTemplate.VALUES.SERIE;
				
		for (i=0; i<$scope.seriesContainers.length; i++) {
						
			for (j=0; j<allSeries.length; j++) {
				
				if ($scope.seriesContainers[i].name==allSeries[j].axis) {
					$scope.seriesContainers[i].series.push(allSeries[j].column);
				}
				
			}
		}
		
		console.log($scope.seriesContainers);
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
		
	}
	
	/**
	 * Arrange series that are available for the chart (set inside the chart template of the already existing chart)
	 * so that they populate their container(s) (Series container(s)).
	 */
	$scope.checkSeries = function() {
			
		// If the chart is already defined (it is NOT the new one - not yet persisted)
		if ($scope.chartTemplate) {
			
			//$scope.numberOfSeriesContainers = $scope.checkNumberOfSeriesContainers();
			
			$scope.checkSeriesForContainers();
			
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
	
	$scope.seriesItemMoveUp = function(item) {
		var index = $scope.seriesContainer.indexOf(item);
		var nextIndex = index-1;
		var temp = $scope.seriesContainer[index];
		$scope.seriesContainer[index] = $scope.seriesContainer[nextIndex];
		$scope.seriesContainer[nextIndex] = temp;
	}
	
	$scope.seriesItemMoveDown = function(item) {
		var index = $scope.seriesContainer.indexOf(item);
		var nextIndex = index+1;
		var temp = $scope.seriesContainer[index];
		$scope.seriesContainer[index] = $scope.seriesContainer[nextIndex];
		$scope.seriesContainer[nextIndex] = temp;
	}
	
	$scope.seriesItemRemove = function(indexOfItem) {
		$scope.seriesContainer.splice(indexOfItem,1);
	}
	
	$scope.seriesRemoveAll = function() {
		$scope.seriesContainer = [];
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
}