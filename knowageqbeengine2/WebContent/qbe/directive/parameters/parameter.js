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

angular.module('qbe_parameters', ['ngMaterial','angular_table' ])
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

function qbeParameters($scope,$rootScope ,sbiModule_translate, sbiModule_config,$mdPanel,$mdDialog){
	$scope.translate = sbiModule_translate;
	$scope.parameterItems = angular.copy($scope.ngModel.pars);
	$scope.datasetParameterTypes = [
	                                {
	                                	name:$scope.translate.load("kn.qbe.params.string"),
	                                	value:"String"
	                        		},

	                                {
	                        			name:$scope.translate.load("kn.qbe.params.number"),
	                        			value:"Number"
	                        		},

	                                {
	                        			name:$scope.translate.load("kn.qbe.params.raw"),
	                        			value:"Raw"
	                        		},

	                                {
	                        			name:$scope.translate.load("kn.qbe.params.generic"),
	                        			value: "Generic"
	                        		}

	                            ];
	$scope.parametersColumns = [
/*$scope.translate.load("kn.qbe.params.name")
 *$scope.translate.load("sbi.generic.type")
 * $scope.translate.load("sbi.generic.defaultValue")
 * $scope.translate.load("sbi.ds.multivalue"*/
	                            {
	                            	"label":$scope.translate.load("kn.qbe.params.name"),
	                            	"name":"name",
	                            	hideTooltip:true,

	                            	transformer: function() {
	                            		return '<md-input-container class="md-block" style="margin:0"><input ng-model="row.name" ></md-input-container>';
	                            	}
	                        	},

	                        	{
	                        		"label":$scope.translate.load("kn.qbe.params.type"),
	                        		"name":"type",
	                        		hideTooltip:true,

	                            	transformer: function() {
	                            		return '<md-select ng-model=row.type class="noMargin" ><md-option ng-repeat="col in scopeFunctions.datasetParameterTypes" value="{{col.name}}">{{col.name}}</md-option></md-select>';
	                            	}
	                    		},

	                        	{
	                    			"label":$scope.translate.load("kn.qbe.params.default.value"),
	                    			"name":"defaultValue",
	                    			hideTooltip:true,

	                            	transformer: function() {
	                            		return '<md-input-container class="md-block" style="margin:0"><input ng-model="row.defaultValue" ></md-input-container>';
	                            	}
	                    		},

	                    		{
	                    			"label":$scope.translate.load("kn.qbe.params.multivalue"),
	                    			"name":"multiValue",
	                    			hideTooltip:true,

	                            	transformer: function() {
	                            		return '<md-checkbox ng-model="row.multiValue"  aria-label="Checkbox"></md-checkbox>';
	                            	}
	                    		}

	                        ];
	$scope.parametersCounter = 0;
	$scope.parametersAddItem = function() {

		$scope.parameterItems.push({"name":"","type":"", "defaultValue":"","multiValue":"","index":$scope.parametersCounter++});
	}
	$scope.paramScopeFunctions = {
			datasetParameterTypes: $scope.datasetParameterTypes
		};
	$scope.saveParams = function(){
		$scope.ngModel.pars.length=0;
		Array.prototype.push.apply($scope.ngModel.pars, $scope.parameterItems);
		$scope.ngModel.mdPanelRef.close();
	}
	$scope.closeParams=function(){
		$scope.ngModel.mdPanelRef.close();
	}
	$scope.deleteAllParameters =function(){

		if ($scope.parameterItems.length>0)
		{
			// TODO: translate
	    	var confirm = $mdDialog.confirm()
		         .title($scope.translate.load("kn.qbe.params.clear.all.dataset.params"))
		         .targetEvent(event)
		         .textContent($scope.translate.load("kn.qbe.params.cofirm.delete.params"))
		         .ariaLabel("Clear all dataset parameters")
		         .ok($scope.translate.load("kn.qbe.general.yes"))
		         .cancel($scope.translate.load("kn.qbe.general.no"));

			$mdDialog
				.show(confirm)
				.then(
						function() {
							$scope.parameterItems = [];
				 		}
					);
		}
		else {

			$mdDialog
			.show(
					$mdDialog.alert()
				        .clickOutsideToClose(true)
				        .title($scope.translate.load("kn.qbe.params.no.dataset.params"))
				        .ariaLabel('Dataset has no parameters to delete')
				        .ok($scope.translate.load("kn.qbe.general.ok"))
			    );

		}

	}


}
})();