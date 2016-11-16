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
	
	sbiModule_restServices.promiseGet("../api/1.0/jsonChartTemplate/fieldsMetadata", "")
	.then(function(response) {
		$scope.fieldsMetadata = response.data;
	}, function(response) {
		sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
	});
	
	$scope.changeStructureDetailsFlex = function() {
		$scope.structurePreviewFlex = 25;
	}
	
	$scope.people = [
	                 { name: 'Janet Perkins',  newMessage: true },
	                 { name: 'Mary Johnson',  newMessage: false },
	                 { name: 'Peter Carlsson', newMessage: false },
	                 { name: 'Janet Perkins',  newMessage: true },
	                 { name: 'Mary Johnson',  newMessage: false },
	                 { name: 'Peter Carlsson', newMessage: false },
	                 { name: 'Janet Perkins',  newMessage: true },
	                 { name: 'Mary Johnson',  newMessage: false },
	                 { name: 'Peter Carlsson', newMessage: false },
	                 { name: 'Janet Perkins',  newMessage: true },
	                 { name: 'Mary Johnson',  newMessage: false },
	                 { name: 'Peter Carlsson', newMessage: false }
	               ];
	
	$scope.moveAttributeToCategories = function(item) {
		console.log(item);
	}
	 
	$scope.moveMeasureToSeries = function(item) {
		console.log(item);
	}
	
}