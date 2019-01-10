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
(function() {
	var module = angular.module('tagsModule');

		module.directive('filterByTags',['sbiModule_config','$timeout',function(sbiModule_config,$timeout) {
		return {
			restrict: 'E',
			templateUrl: sbiModule_config.contextName + '/js/src/angular_1.4/tools/tags/filterByTags/filterByTags.html',
			controller: filterTagsController,
			scope: true
		};

	}]);

		function filterTagsController($scope,tagsHandlerService,$timeout){
			$scope.limitation = 5;
			$scope.colapsed = {};
			$scope.colapsed.name = "tagsUp";

			$scope.toggleAllTags = function(){

				if($scope.limitation == 5){
					$scope.limitation = $scope.allTags.length;
					$scope.colapsed.name = "tagsDown";
				}else{
					$scope.limitation = 5;
					$scope.colapsed.name = "tagsUp";
				}
			}

			$scope.toggleTag = function(tag){
				tagsHandlerService.toggleTag(tag);
				if(isFromCatalog()){
					filterForCatalog();
				}else if(isFromWorkspace()) {
					filterForWorkspace();
				}

			}

			var setListByType = function(type,response){
				switch(type){
				case "myDataSet":
					$scope.myDatasets = response;
					break;
				case "sharedDataSet":
					$scope.sharedDatasets = response;
					break;
				case "enterpriseDataSet":
					$scope.enterpriseDatasets = response;
					break;
				case "ckanDataSet":
					$scope.ckanDatasetsList = response;
					break;
				case "allDataSet":
					$scope.datasets = response;
					break;
				}
			}

			var isFromCatalog = function(){
				return $scope.location == "catalog";
			}

			var isFromWorkspace = function(){
				$scope.location == "workspace"
			}

			var filterForCatalog = function (){
				$timeout(function () {
					$scope.datasetLike($scope.searchValue,$scope.itemsPerPage, $scope.currentPageNumber, $scope.columnsSearch, $scope.columnOrdering, $scope.reverseOrdering)
				 }, 1000);
			}

			var filterForWorkspace = function(){
				$timeout(function () {
					if(tagsHandlerService.getFilteredTagIds().length == 0){
						$scope.loadInitialForDatasets()
					}else{
						$scope.restServices.promiseGet(  toBeCreated,$scope.currentDatasetsTab ).then(function(response){
							setListByType($scope.currentDatasetsTab,response.data);
						})
					}
				}, 1000);
			}
}



})();