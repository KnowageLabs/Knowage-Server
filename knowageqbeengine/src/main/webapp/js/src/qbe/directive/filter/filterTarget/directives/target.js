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
	angular.module('targetApp').directive('target', function(multiConditionTemplateRetriver) {

		return {
			scope:{
				obj:'=',
				filter:'=',
				fieldType:'=',
				targetType:'='
			},

			restrict:'E',
			template:"",
			controller:function($scope,$compile,$element){
				$scope.$watchGroup(['filter.operator', 'fieldType','targetType'], function(newValues, oldValues, scope) {

					$scope.directive = 	multiConditionTemplateRetriver
					.withOperator(newValues[0])
					.withFieldType(newValues[1])
					.withTargetType(newValues[2])
					.getTemplate();


					$element.empty()
					var el = $compile( "<"+$scope.directive+" flex filter='filter' obj='obj'></"+$scope.directive+">" )( $scope );
					$element.append( el );


					});



			var init = function(){

				if(!$scope.filter.rightOperandValue){
					$scope.filter.rightOperandValue = [];
				}

			}

			init();

			}



		}
	})
})()
