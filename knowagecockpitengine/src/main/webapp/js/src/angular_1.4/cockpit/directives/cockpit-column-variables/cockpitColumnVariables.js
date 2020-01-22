/*
Knowage, Open Source Business Intelligence suite
Copyright (C) 2019 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

(function() {

	angular.module('cockpitModule')
	.directive('cockpitColumnVariables',function(){
		   return{
			   templateUrl: baseScriptPath+ '/directives/cockpit-column-variables/templates/cockpitColumnVariables.html',
			   controller: cockpitColumnVariables,
			   scope: {
				   variables: '='
			   	}
		   }
	});

	function cockpitColumnVariables($scope,cockpitModule_properties,cockpitModule_template,sbiModule_translate, cockpitModule_generalOptions){

		$scope.translate=sbiModule_translate;
		$scope.variablesActions = cockpitModule_generalOptions.tableVariablesActions;
		$scope.conditions = cockpitModule_generalOptions.conditions;
		$scope.cockpitModule_properties = cockpitModule_properties;

		$scope.addVariablesUsage = function(){
			if($scope.variables) $scope.variables.push({});
			else $scope.variables = [{}];
		}

		$scope.deleteVariable = function(i){
			$scope.variables.splice(i,1);
		}

		$scope.isVariableObject = function(variableName){
			return typeof cockpitModule_properties.VARIABLES[variableName] == 'object';
		}

	};

})();