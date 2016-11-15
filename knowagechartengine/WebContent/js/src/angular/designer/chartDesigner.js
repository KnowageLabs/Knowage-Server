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

var app =angular.module('chartDesignerManager', ['chart-directives','ChartDesignerService'])

app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}]);

app.controller("ChartDesignerController", ["sbiModule_translate","$scope", ChartDesignerFunction]);

function ChartDesignerFunction(sbiModule_translate,$scope) {
	
	$scope.translate = sbiModule_translate;
	$scope.previewButtonEnabled = false;
	
	$scope.saveChartTemplate = function() {
		$scope.showStructureDetails = true;
		$scope.structurePreviewFlex = 25;
	}
	
	$scope.goBackFromDesigner = function() {
		$scope.showStructureDetails = false;
		$scope.structurePreviewFlex = 50;
	}
	
	$scope.chartTemplate = jsonTemplate;
	
	// Needed for the preview of the chart (calling the Highcharts exporter
	$scope.exporterContextName = exporterContextName;
	
}