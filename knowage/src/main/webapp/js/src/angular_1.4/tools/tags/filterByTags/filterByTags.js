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

		module.directive('filterByTags',['sbiModule_config','sbiModule_restServices','sbiModule_urlBuilderService','$timeout','$filter',function(sbiModule_config,$timeout,$filter) {
		return {
			restrict: 'E',
			templateUrl: sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/tags/filterByTags/filterByTags.html',
			controller: filterTagsController,
			scope:	{
					tagsArray : '=',
					currentDatasetsTab : '=',
					datasets : '=?',
					filterFunction : '&?',
					inverse: '@?'
				}
		};
	}]);

		function filterTagsController($scope,tagsHandlerService,sbiModule_restServices,sbiModule_urlBuilderService,$timeout,$filter){
			var tagLimit = 8;
			$scope.tagsVisible = tagLimit;
			$scope.colapsed = {};
			$scope.colapsed.name = "tagsUp";
			$scope.remove = true;
			$scope.allTags=[];
			var filteringTags = angular.copy($scope.tagsArray);
			$scope.allTags = angular.copy($scope.tagsArray);

			var endPath = "";

			$scope.toggleAllTags = function(){
				if($scope.tagsVisible == tagLimit) $scope.tagsVisible = 100;
				else $scope.tagsVisible = tagLimit;
			}

			$scope.toggleTag = function(tag){
				tag.isSelected = !tag.isSelected;
				tagsHandlerService.toggleTag(tag);
				if(isFromCatalog()){
					$scope.filterFunction()
				}else if(isFromWorkspace()) {
					filterForWorkspace();
				}

			}

			var setPathByType = function(type){
				switch(type){
				case "myDataSet":
					endPath = 'owned';
					break;
				case "sharedDataSet":
					endPath = 'shared';
					break;
				case "enterpriseDataSet":
					endPath = 'enterprise';
					break;
				case "ckanDataSet":
					break;
				case "allDataSet":
					endPath = 'all';
					break;
				}
			}

			var isFromCatalog = function(){
				return $scope.currentDatasetsTab == "catalog";
			}

			var isFromWorkspace = function(){
				return $scope.currentDatasetsTab != "catalog";
			}

			var filterForWorkspace = function(){
				setPathByType($scope.currentDatasetsTab);
				$scope.$parent.searching = true;
				$timeout(function () {

					if(tagsHandlerService.getFilteredTagIds($scope.tagsArray).length == 0){
						restoreOriginalDatasets($scope.currentDatasetsTab);
					}else{
						filterExistingDatasets($scope.currentDatasetsTab) 
					}
					$scope.$parent.searching = false;
				}, 1000);
			}

			var restoreOriginalDatasets = function(type){
				switch(type){
				case "myDataSet":
					$scope.$parent.myDatasets = angular.copy(tagsHandlerService.getOwnedDS());
					break;
				case "sharedDataSet":
					$scope.$parent.sharedDatasets = angular.copy(tagsHandlerService.getSharedDS());
					break;
				case "enterpriseDataSet":
					$scope.$parent.enterpriseDatasets =  angular.copy(tagsHandlerService.getEnterpriseDS());
					break;
				case "ckanDataSet":
					break;
				case "allDataSet":
					$scope.$parent.datasets = angular.copy(tagsHandlerService.getAllDS());
					break;
				}
			}
			var filterExistingDatasets = function(type){
					switch(type){
					case "myDataSet":
						$scope.$parent.myDatasets = getDatasetsWithTag(tagsHandlerService.getOwnedDS(),tagsHandlerService.getFilteredTags($scope.tagsArray));
						break;
					case "sharedDataSet":
						$scope.$parent.sharedDatasets = getDatasetsWithTag(tagsHandlerService.getSharedDS(),tagsHandlerService.getFilteredTags($scope.tagsArray));
						break;
					case "enterpriseDataSet":
						$scope.$parent.enterpriseDatasets =  getDatasetsWithTag(tagsHandlerService.getEnterpriseDS(),tagsHandlerService.getFilteredTags($scope.tagsArray));
						break;
					case "ckanDataSet":
						break;
					case "allDataSet":
						$scope.$parent.datasets = getDatasetsWithTag(tagsHandlerService.getAllDS(),tagsHandlerService.getFilteredTags($scope.tagsArray));
						break;
					}
				}

				var filterByArrayPropertyOfObject = function(dsTags,selectedTags){
					var tempArray = [];
					for(var i = 0; i < selectedTags.length; i++){
						var tempId = selectedTags[i].tagId;
						if($filter('filter')(dsTags,{tagId:tempId}).length > 0){
							return true;
						}
					}
				}

				var getDatasetsWithTag = function(listOfDS,tags){
					var tempDataSets = [];
					for(var i = 0; i < listOfDS.length; i++){
						if(filterByArrayPropertyOfObject(listOfDS[i].tags,tags))
							tempDataSets.push(listOfDS[i]);
					}
					return tempDataSets;
				}

}

})();