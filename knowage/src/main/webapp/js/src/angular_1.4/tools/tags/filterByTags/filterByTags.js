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
			$scope.limitation = 8;
			$scope.colapsed = {};
			$scope.colapsed.name = "tagsUp";
			$scope.remove = true;
			$scope.tagsArray = $scope.allTags.slice(0,8);
			var endPath = "";
			$scope.toggleAllTags = function(){
				if($scope.tagsArray.length == 8){
					$scope.colapsed.name = "tagsDown";
					$scope.tagsArray = $scope.allTags ;
				}else{
					$scope.colapsed.name = "tagsUp";
					$scope.tagsArray = $scope.allTags.slice(0,8);
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
					endPath = 'owned';
					break;
				case "sharedDataSet":
					$scope.sharedDatasets = response;
					endPath = 'shared';
					break;
				case "enterpriseDataSet":
					$scope.enterpriseDatasets = response;
					endPath = 'enterprise';
					break;
				case "ckanDataSet":
					$scope.ckanDatasetsList = response;
					break;
				case "allDataSet":
					$scope.datasets = response;
					endPath = 'all';
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
					if(tagsHandlerService.getFilteredTagIds($scope.allTags).length == 0){
						$scope.loadInitialForDatasets()
					}else{
						urlBuilderService.setBaseUrl("filterbytags/"+$scope.currentDatasetsTab);
						var tags = {"tags":tagsHandlerService.getFilteredTagIds($scope.allTags)};
						urlBuilderService.addQueryParams(tags);
						$scope.restServices.promiseGet('1.0/datasets',urlBuilderService.build() ).then(function(response){
							setListByType($scope.currentDatasetsTab,response.data);
						})
					}
				}, 1000);
			}

			getPathFromTab
}

})();