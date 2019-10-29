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
(function(){
	angular.module('targetApp').directive('time', function(targetAppBasePath,dateService) {

		return {

			scope:{
				filter:'=',
				obj:'='
			},
			restrict:'E',
			templateUrl:targetAppBasePath +"/directives/time/time.html",
			controller:function($scope){
				$scope.$watch('time',function(){
					$scope.fillInput();
				})
				$scope.fillInput = function(){
					if($scope.time && $scope.time.includes(":")){
						var hour = $scope.time.split(':')[0]
						var minutes = $scope.time.split(':')[1]

						$scope.filter.rightOperandDescription="'"+hour+"."+minutes+".00'"

					}

				}

				var init = function(){
					$scope.filter.rightOperandType="Static Content";
					if(dateService.getTime($scope.filter.rightOperandDescription)){
						$scope.time = dateService.getHour($scope.filter.rightOperandDescription)
										+ ":"
										+ dateService.getMinutes($scope.filter.rightOperandDescription)
					}


				}
				init()
			}

		}
	})
})()

