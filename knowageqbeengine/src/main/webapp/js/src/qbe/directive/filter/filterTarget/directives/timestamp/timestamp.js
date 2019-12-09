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
	angular.module('targetApp').directive('timestamp', function(targetAppBasePath) {

		return {

			scope:{
				filter:'=',
				obj:'='
			},
			restrict:'E',
			templateUrl:targetAppBasePath +"/directives/timestamp/timestamp.html",
			controller:function($scope,_dateService_){

				var dateService = _dateService_;
				$scope.$watchGroup(['date','time'],function(newValues, oldValues, scope){
					$scope.fillInput(newValues[0],newValues[1]);
				})

				$scope.fillInput = function(newDate,newTime){
					if(newDate && newTime){
						var date = newDate.getDate();
						var month = newDate.getMonth()+1;
						var year = newDate.getFullYear();

						var hour = newTime.split(":")[0]
						var minutes = newTime.split(":")[1]

						$scope.filter.rightOperandDescription = buildTimestamp(date,month,year,hour,minutes);
					}

				}

				var init = function(){
					$scope.filter.rightOperandType="Static Content";
					if(dateService.getFullDate($scope.filter.rightOperandDescription)){
						$scope.date = new Date(	dateService.getYear($scope.filter.rightOperandDescription),
												dateService.getMonth($scope.filter.rightOperandDescription),
												dateService.getDate($scope.filter.rightOperandDescription));

						$scope.time = dateService.getFullDateHour($scope.filter.rightOperandDescription)
										+ ":"
										+ dateService.getFullDateMinutes($scope.filter.rightOperandDescription)
					}else{
						$scope.date = new Date();
					}


				}
				init()

				var buildTimestamp = function(date,month,year,hour,minutes){
					return "".concat(date,"/",month,"/",year," ",hour,":",minutes)
				}

			}

		}
	})
})()

