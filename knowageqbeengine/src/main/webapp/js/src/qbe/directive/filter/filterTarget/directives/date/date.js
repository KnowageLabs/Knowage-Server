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
angular.module('targetApp').directive('date', function(targetAppBasePath,_dateService_) {

		return {

			scope:{
				filter:'=',
				obj:'='
			},
			restrict:'E',
			templateUrl:targetAppBasePath +"/directives/date/date.html",
			controller:function($scope,$filter){
				var dateService = _dateService_;
				$scope.$watch('date',function(){
					$scope.fillInput();
				})

				$scope.fillInput = function(){
					var date = $scope.date.getDate();
					var month = $scope.date.getMonth()+1;
					var year = $scope.date.getFullYear();


					$scope.filter.rightOperandDescription="'"+date+"/"+month+"/"+year+"'"
				}



				var init = function(){
					$scope.filter.rightOperandType="Static Content";
					if(dateService.getFullDate($scope.filter.rightOperandDescription)){
						$scope.date = new Date(	dateService.getYear($scope.filter.rightOperandDescription),
												dateService.getMonth($scope.filter.rightOperandDescription),
												dateService.getDate($scope.filter.rightOperandDescription));
					}else{
						$scope.date = new Date();
					}


				}
				init()


			}

		}
	})
})()