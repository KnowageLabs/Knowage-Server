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

	angular
		.module('knTable', ['ngMaterial','sbiModule'])
		.directive('knTable', knTableDirective)

	function knTableDirective(sbiModule_config) {
		return {
			templateUrl: sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/commons/kn-table/template/knTable.tpl.html',
			controller: knTableController,
			restrict: 'E',
			scope: {
				columns: "=",
				model: "=",
				searchModel: "=",
				clickFunction: "&",
				customClass: "@"
			}
		}
	};

	function knTableController($scope,sbiModule_config){
		$scope.sortingColumn = $scope.columns[0].name;
		$scope.sortingDirection = false;

		$scope.setDate = function(string){
			return moment(string).locale(sbiModule_config.curr_language).format('LLL');
		}
		$scope.setColumnSorting = function(column){
			$scope.sortingDirection = column.name == $scope.sortingColumn ? !$scope.sortingDirection : false;
			$scope.sortingColumn = column.name;
		}

		$scope.clickFunctionWrapper = function(row,e){
			$scope.selectedItem = row;
			$scope.clickFunction({'item':row,'evt':e});
		}
		$scope.actionClick = function(row,button,e){
			e.stopPropagation();
			button.action(row);
		}
	}
})();