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
	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);

angular.module('qbe_parameters', ['ngMaterial','angular_table','filters' ])
.directive('qbeParameters', function() {
	return {

		templateUrl:  currentScriptPath +'parameter.html',
		controller: qbeParameters,

		scope: {
			 ngModel: '='
		},
		link: function (scope, elem, attrs) {

		}
	}
});

function qbeParameters($scope,$rootScope ,sbiModule_translate, sbiModule_config,$mdPanel,$mdDialog,params_service,filters_service){
	$scope.translate = sbiModule_translate;
	$scope.parameterItems = angular.copy($scope.ngModel.pars);

	$scope.parameterTypes = params_service.getParamTypes();

	$scope.parametersColumns = [
		{
			"label" : $scope.translate.load("kn.qbe.params.name"),
			"name" : "name",
			hideTooltip : true,

			transformer : function() {
				return '<md-input-container class="md-block" style="margin:0"><input ng-model="row.name" ></md-input-container>';
			}
		},

		{
			"label" : $scope.translate.load("kn.qbe.params.type"),
			"name" : "type",
			hideTooltip : true,

			transformer : function() {
				return '<md-select ng-model=row.type class="noMargin" ><md-option ng-repeat="col in scopeFunctions.parameterTypes" value="{{col.value}}">{{col.name}}</md-option></md-select>';
			}
		},

		{
			"label" : $scope.translate
					.load("kn.qbe.params.default.value"),
			"name" : "defaultValue",
			hideTooltip : true,

			transformer : function() {
				return '<md-input-container class="md-block" style="margin:0"><input  ng-model="row.defaultValue" ></md-input-container>';
			}
		},

		{
			"label" : $scope.translate.load("kn.qbe.params.multivalue"),
			"name" : "multiValue",
			hideTooltip : true,

			transformer : function() {
				return '<md-checkbox ng-model="row.multiValue"  aria-label="Checkbox"></md-checkbox>';
			}
		}

		];

	$scope.parameterDelete = [
		{
			label : $scope.translate.load("sbi.generic.delete"),
			icon : 'fa fa-trash',
			backgroundColor : 'transparent',
			action : function(item,event) {
				var confirm = $mdDialog.confirm().title(
						$scope.translate.load("kn.qbe.params.delete.param"))
						.targetEvent(event).textContent(
								$scope.translate
										.load("kn.qbe.params.delete.param.ok"))
						.ariaLabel("Delete dataset parameter").ok(
								$scope.translate.load("kn.qbe.general.yes"))
						.cancel($scope.translate.load("kn.qbe.general.no"));

				$mdDialog.show(confirm).then(function() {
					for (i = 0; i < $scope.parameterItems.length; i++) {

						if ($scope.parameterItems[i].index == item.index) {
							$scope.parameterItems.splice(i, 1);
							break;
						}


					}


					filters_service.deleteFilterByProperty('paramName',item.name,$scope.ngModel.filters,$scope.ngModel.expression,$scope.ngModel.advancedFilters)

				});
			}
		}
		];

	$scope.parametersCounter = 0;
	$scope.parametersAddItem = function() {

		$scope.parameterItems.push({"name":"","type":"", "defaultValue":"","multiValue":false,"index":$scope.parametersCounter++});
	};

	$scope.paramScopeFunctions = {
			parameterTypes: $scope.parameterTypes
	};

	$scope.hasDuplicates = function(array,property){
		if(!array && !Array.isArray(array)){
			return false;
		}
		var obj = {};
		for(var i in array){
			obj[array[i][property]] = array[i]
		}

		return array.length > Object.keys(obj).length
	}

	$scope.saveParams = function(){


		$scope.ngModel.pars.length=0;
		Array.prototype.push.apply($scope.ngModel.pars, $scope.parameterItems);
		$scope.ngModel.mdPanelRef.close();
	};

	$scope.closeParams=function(){
		$scope.ngModel.mdPanelRef.close();
	};

	$scope.deleteAllParameters =function(){

		if ($scope.parameterItems.length > 0) {
			var confirm = $mdDialog
					.confirm()
					.title($scope.translate.load("kn.qbe.params.clear.all.dataset.params"))
					.targetEvent(event)
					.textContent($scope.translate.load("kn.qbe.params.cofirm.delete.params"))
					.ariaLabel("Clear all dataset parameters")
					.ok($scope.translate.load("kn.qbe.general.yes"))
					.cancel($scope.translate.load("kn.qbe.general.no"));

			$mdDialog.show(confirm).then(function() {
				$scope.parameterItems = [];
			});
		} else {
			$mdDialog.show($mdDialog
					.alert()
					.clickOutsideToClose(true)
					.title($scope.translate.load("kn.qbe.params.no.dataset.params"))
					.ariaLabel('Dataset has no parameters to delete')
					.ok($scope.translate.load("kn.qbe.general.ok")));
		}

	}
}
})();