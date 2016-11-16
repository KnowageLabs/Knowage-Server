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
		      return sbiModule_config.contextName + '/js/src/angular/designer/directives/custom_directives/chart-tab/chart-tab.html' 
	      },   
		controller: chartTabControllerFunction
	}
		
});

function chartTabControllerFunction($scope,sbiModule_translate,sbiModule_restServices,sbiModule_messaging){
	$scope.translate = sbiModule_translate;
	$scope.datasetLabel = datasetLabel;

	sbiModule_restServices.promiseGet("../api/1.0/pages/types", "")
	.then(function(response) {
		$scope.chartTypes = response.data.types;
		for (var i = 0; i < $scope.chartTypes.length; i++) {
			
			if ($scope.chartTemplate) {
				if($scope.chartTypes[i].toUpperCase()==$scope.chartTemplate.type.toUpperCase()){
					$scope.selectedChartType = $scope.chartTypes[i];
				}
			}
			else {
				$scope.selectedChartType = null;
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
	
	sbiModule_restServices.promiseGet("../api/style", "")
	.then(function(response) {
		$scope.chartStyles = response.data;
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
	
	$scope.selectChartType = function(chart) {
		$scope.selectedChartType = chart;
	}
		
}