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
angular.module('targetApp').directive('multidate', function(targetAppBasePath,_dateService_) {

		return {

			scope:{
				filter:'=',
				obj:'='
			},
			restrict:'E',
			templateUrl:targetAppBasePath +"/directives/multidate/multidate.html",
			controller:function($scope){

				$scope.$watch('dates',function(newValues, oldValues, scope){

						$scope.fillInput(newValues);

				},true)

				$scope.addCalendar = function(){
					$scope.dates.push({})
				}

				$scope.fillInput = function(dates){
					$scope.filter.rightOperandDescription = ""
					for(var i in dates){
						if(dates[i] && dates[i] !== ""){
							$scope.filter.rightOperandDescription += addSeparator(dates[i],dates," ---- ") + dates[i].rightOperandDescription
						}

					}


				}

				var addSeparator = function(item,array,separatory){
					return isFirst(item,array) ? "" : " ---- "
				}

				var isFirst = function(item,array){
					return item === array[0];
				}

				var getMultiValues = function(rightOperandDescription){
					if(rightOperandDescription.includes("----")){
						return rightOperandDescription.split(" ---- ")
					}
				}



				var init = function(){
					$scope.filter.rightOperandType="Static Content";
					$scope.dates = [{},{}]

					if(getMultiValues($scope.filter.rightOperandDescription)){
						$scope.dates.length = 0;
						for(var i in getMultiValues($scope.filter.rightOperandDescription)){
							var date = {}
							date.rightOperandDescription = getMultiValues($scope.filter.rightOperandDescription)[i]
							$scope.dates.push(date)
						}

					}



				}
				init()



			}

		}
	})
})()