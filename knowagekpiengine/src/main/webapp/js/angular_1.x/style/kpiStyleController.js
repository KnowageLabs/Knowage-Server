/* Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

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
(function () {
	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);
	
	angular.module('kpi-style', ['ngMaterial','sbiModule','color.picker'])
	.directive('kpiStyle', function() {
		return {
			templateUrl: currentScriptPath + 'template/kpiStyle.html',
			controller: kpiStyleController,
			scope: {
				ngModel:'=',
			},
			link: function (scope, elm, attrs) { 
	
			}
		};
	});
	
	function kpiStyleController($scope,$mdDialog,$q,$mdToast,$timeout,sbiModule_restServices,sbiModule_translate,sbiModule_config,knModule_selections){
		$scope.translate=sbiModule_translate;
		$scope.colorPickerOptions = {
				pos: 'top left',
				format: 'rgb'
		};
		$scope.measure = knModule_selections['font-size-old'];
		$scope.fontFamily = knModule_selections['font-family'];
		$scope.fontWeight = knModule_selections['font-weight'];
	};

})();