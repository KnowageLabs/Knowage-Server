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

		module.directive('filterByTags',['sbiModule_config','sbiModule_restServices','urlBuilderService','$timeout','$filter',function(sbiModule_config,$timeout,$filter) {
		return {
			restrict: 'E',
			templateUrl: sbiModule_config.contextName + '/js/src/angular_1.4/tools/tags/filterByTags/filterByTags.html',
			controller: filterTagsController,
			scope:	{
					tagsArray : '=',
					currentDatasetsTab : '=',
					datasets : '=?',
					filterFunction : '&?'
				}
		};
	}]);

		function filterTagsController($scope,tagsHandlerService,sbiModule_restServices,urlBuilderService,$timeout,$filter){
			$scope.limitation = 8;
			$scope.colapsed = {};
			$scope.colapsed.name = "tagsUp";
			$scope.remove = true;
			$scope.allTags=[];
			var filteringTags = angular.copy($scope.tagsArray);
			$scope.allTags = angular.copy($scope.tagsArray);
			$scope.tagsArray = $scope.allTags.slice(0,8);

			var endPath = "";

			$scope.toggleAllTags = function(){
				if($scope.tagsArray.length == 8){
					$scope.colapsed.name = "tagsDown";
					$scope.tagsArray = filteringTags ;
				}else{
					$scope.colapsed.name = "tagsUp";
					$scope.tagsArray = filteringTags.slice(0,8);
				}
			}

			$scope.toggleTag = function(tag){
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
				$timeout(function () {

					if(tagsHandlerService.getFilteredTagIds($scope.tagsArray).length == 0){
						restoreOriginalDatasets($scope.currentDatasetsTab);
					}else{
						filterExistingDatasets($scope.currentDatasetsTab)  //for frontend filtering

						//FOR BACKEND FILTERING IF NEEDED EVER
			/*			urlBuilderService.setBaseUrl("filterbytags/"+endPath);
						var tags = {"tags":tagsHandlerService.getFilteredTagIds($scope.tagsArray)};
						urlBuilderService.addQueryParams(tags);
						sbiModule_restServices.promiseGet('1.0/datasets',urlBuilderService.build() ).then(function(response){
							setListByType($scope.currentDatasetsTab,response.data.root);
						})*/
					}
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


// FOR BACKEND FILTERING
//				var setListByType = function(type,response){
			//
//							switch(type){
//							case "myDataSet":
//								$scope.$parent.myDatasets = response;
//								break;
//							case "sharedDataSet":
//								$scope.$parent.sharedDatasets = response;
//								break;
//							case "enterpriseDataSet":
//								$scope.$parent.enterpriseDatasets = response;
//								break;
//							case "ckanDataSet":
//								$scope.$parent.ckanDatasetsList = response;
//								break;
//							case "allDataSet":
//								$scope.$parent.datasets = response;
//								break;
//							}
//						}

}

})();