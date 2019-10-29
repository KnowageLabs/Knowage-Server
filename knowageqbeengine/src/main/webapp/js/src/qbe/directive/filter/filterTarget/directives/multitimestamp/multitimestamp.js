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
angular.module('targetApp').directive('multitimestamp', function(targetAppBasePath,_dateService_) {

		return {

			scope:{
				filter:'=',
				obj:'='
			},
			restrict:'E',
			templateUrl:targetAppBasePath +"/directives/multitimestamp/multitimestamp.html",
			controller:function($scope){

				$scope.$watch('timestamps',function(newValues, oldValues, scope){

						$scope.fillInput(newValues);

				},true)

				$scope.addCalendar = function(){
					$scope.timestamps.push({})
				}

				$scope.fillInput = function(timestamps){
					$scope.filter.rightOperandDescription = ""
					for(var i in timestamps){
						if(timestamps[i] && timestamps[i] !== ""){
							$scope.filter.rightOperandDescription += addSeparator(timestamps[i],timestamps," ---- ") + timestamps[i].rightOperandDescription
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
					$scope.timestamps = [{},{}]

					if(getMultiValues($scope.filter.rightOperandDescription)){
						$scope.timestamps.length = 0;
						for(var i in getMultiValues($scope.filter.rightOperandDescription)){
							var timestamp = {}
							timestamp.rightOperandDescription = getMultiValues($scope.filter.rightOperandDescription)[i]
							$scope.timestamps.push(timestamp)
						}

					}



				}
				init()



			}

		}
	})
})()