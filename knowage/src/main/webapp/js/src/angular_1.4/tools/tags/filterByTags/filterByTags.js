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
			$scope.limitation = 6;


			$scope.toggleAllTags = function(){
				$scope.colapsed = !$scope.colapsed;
				if($scope.limitation == 6){
					$scope.limitation = $scope.tags.length;
				}else
					$scope.limitation = 6;
			}

			$scope.toggleTag = function(tag){
				tagsHandlerService.toggleTag(tag);
				$timeout(function () {
					$scope.datasetLike($scope.searchValue,$scope.itemsPerPage, $scope.currentPageNumber, $scope.columnsSearch, $scope.columnOrdering, $scope.reverseOrdering)
				 }, 1000);
				}



		}



})();