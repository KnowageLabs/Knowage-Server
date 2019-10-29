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
angular.module('targetApp').directive('multitime', function(targetAppBasePath,_dateService_) {

		return {

			scope:{
				filter:'=',
				obj:'='
			},
			restrict:'E',
			templateUrl:targetAppBasePath +"/directives/multitime/multitime.html",
			controller:function($scope){

				$scope.$watch('times',function(newValues, oldValues, scope){

						$scope.fillInput(newValues);

				},true)

				$scope.addCalendar = function(){
					$scope.times.push({})
				}

				$scope.fillInput = function(times){
					$scope.filter.rightOperandDescription = ""
					for(var i in times){
						if(times[i] && times[i] !== ""){
							$scope.filter.rightOperandDescription += addSeparator(times[i],times," ---- ") + times[i].rightOperandDescription
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
					$scope.times = [{},{}]

					if(getMultiValues($scope.filter.rightOperandDescription)){
						$scope.times.length = 0;
						for(var i in getMultiValues($scope.filter.rightOperandDescription)){
							var time = {}
							time.rightOperandDescription = getMultiValues($scope.filter.rightOperandDescription)[i]
							$scope.times.push(time)
						}

					}



				}
				init()



			}

		}
	})
})()