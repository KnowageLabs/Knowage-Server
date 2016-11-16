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
	$scope.showStructureDetails = false;
	$scope.structurePreviewFlex = 50;
		
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
	
	$scope.changeStructureDetailsFlex = function() {
		$scope.structurePreviewFlex = 25;
	}
	
	$scope.moveAttributeToCategories = function(item) {
		console.log(item);
	}
	 
	$scope.moveMeasureToSeries = function(item) {
		console.log(item);
	}
	
//	console.log($scope.fieldsMetadata);
	
	$scope.checkCategories = function() {		
		
		$scope.categoriesExist = $scope.chartTemplate.VALUES.CATEGORY ? true : false;
		
		if ($scope.categoriesExist) {
			
			var categoryTag = $scope.chartTemplate.VALUES.CATEGORY;
			console.log(categoryTag.length);
			// If the CATEGORY tag contains an array (e.g. this goes for the SUNBURST chart type)
			if (categoryTag.length) {
				
				for (i=0; i<categoryTag.length; i++) {
					$scope.categoriesContainer.push(categoryTag[i].column);
				}
				
			}
			// If all (if there are more than one) categories are under the single tag (column and groupby properties (attributes))
			else {
				
				$scope.categoriesContainer.push(categoryTag.column);
				
				console.log("categoryTag:",categoryTag);
				
				if (categoryTag.groupby.indexOf(",") > -1) {
					var groupBySplit = categoryTag.groupby.split(",");
					
					for (i=0; i<groupBySplit.length; i++) {
						$scope.categoriesContainer.push(groupBySplit[i]);
					}
					
				}
				else {
					categoryTag.groupby ? $scope.categoriesContainer.push(categoryTag.groupby) : null;
				}
				
				console.log("categoriesContainer:",$scope.categoriesContainer);
			}
			
		}
				
	}
	
	$scope.checkSeries = function() {
		
	}
	
}