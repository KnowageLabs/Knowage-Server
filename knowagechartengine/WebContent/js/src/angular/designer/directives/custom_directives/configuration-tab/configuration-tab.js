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


angular.module('configuration-tab', [])
.directive('configurationTab', function(sbiModule_config) {
	return {
		restrict: 'AE',
		replace:true,
		templateUrl: function(){
		      return sbiModule_config.contextName + '/js/src/angular/designer/directives/custom_directives/configuration-tab/configuration-tab.html' 
	      },   
		controller: configurationTabControllerFunction
	}
		
});

function configurationTabControllerFunction(sbiModule_translate,$scope){

 $scope.translate = sbiModule_translate;
 $scope.configurationForDisplay = [];
 $scope.barChartConfiguration = [
                                 {name:"Generic",imgPath:""},
                                 {name:"Title and subtitle",imgPath:""},
                                 {name:"No data message",imgPath:""},
                                 {name:"Color palette",imgPath:""},
                                 {name:"Legend title",imgPath:""},
                                 {name:"Legend items",imgPath:""},                                
                                                              ]
 $scope.lineChartConfiguration = [
                                 {name:"Generic",imgPath:""},
                                 {name:"Title and subtitle",imgPath:""},
                                 {name:"No data message",imgPath:""},
                                 {name:"Color palette",imgPath:""},
                                 {name:"Legend title",imgPath:""},
                                 {name:"Legend items",imgPath:""},                                
                                                              ]
 $scope.pieChartConfiguration = [
                                 {name:"Generic",imgPath:""},
                                 {name:"Title and subtitle",imgPath:""},
                                 {name:"No data message",imgPath:""},
                                 {name:"Color palette",imgPath:""},
                                 {name:"Legend title",imgPath:""},
                                 {name:"Legend items",imgPath:""},                                
                                                              ]
 $scope.radarChartConfiguration = [
                                 {name:"Generic",imgPath:""},
                                 {name:"Title and subtitle",imgPath:""},
                                 {name:"No data message",imgPath:""},
                                 {name:"Color palette",imgPath:""},
                                 {name:"Legend title",imgPath:""},
                                 {name:"Legend items",imgPath:""},                                
                                                              ]
 $scope.scaterChartConfiguration = [
                                 {name:"Generic",imgPath:""},
                                 {name:"Title and subtitle",imgPath:""},
                                 {name:"No data message",imgPath:""},
                                 {name:"Color palette",imgPath:""},
                                 {name:"Legend title",imgPath:""},
                                 {name:"Legend items",imgPath:""},
                                 {name:"Ticks and labels",imgPath:""}
                                                              ]
 $scope.gaugeChartConfiguration = [
                                 {name:"Generic",imgPath:""},
                                 {name:"Title and subtitle",imgPath:""},
                                 {name:"No data message",imgPath:""},
                                 {name:"Color palette",imgPath:""},
                                 {name:"Pane",imgPath:""},                                
                                                              ]
 $scope.treemapChartConfiguration = [
                                   {name:"Generic",imgPath:""},
                                   {name:"Title and subtitle",imgPath:""},
                                   {name:"No data message",imgPath:""},
                                   {name:"Color palette",imgPath:""},
                                   							  ]
 $scope.heatmapChartConfiguration = [
                                   {name:"Generic",imgPath:""},
                                   {name:"Title and subtitle",imgPath:""},
                                   {name:"No data message",imgPath:""},
                                   {name:"Color palette",imgPath:""},
                                   {name:"Legend title",imgPath:""},
                                   {name:"Legend items",imgPath:""},
                                   {name:"Tooltip",imgPath:""}, 
                                                                ]
 $scope.sunburstChartConfiguration = [
                                   {name:"Generic",imgPath:""},
                                   {name:"Title and subtitle",imgPath:""},
                                   {name:"No data message",imgPath:""},
                                   {name:"Color palette",imgPath:""},
                                   {name:"Sequence",imgPath:""},
                                   {name:"Explanation",imgPath:""},
                                                                ]
 $scope.wordcloudChartConfiguration = [
                                      {name:"Generic",imgPath:""},
                                      {name:"Title and subtitle",imgPath:""},
                                      {name:"No data message",imgPath:""},
                                      {name:"Word Settings",imgPath:""},
                                                                   ]
 $scope.chordChartConfiguration = [
                                     {name:"Generic",imgPath:""},
                                     {name:"Title and subtitle",imgPath:""},
                                     {name:"No data message",imgPath:""},
                                     {name:"Color palette",imgPath:""},
                                     							  ]
 $scope.parallelChartConfiguration = [
                                   {name:"Generic",imgPath:""},
                                   {name:"Title and subtitle",imgPath:""},
                                   {name:"No data message",imgPath:""},
                                   {name:"Color palette",imgPath:""},
                                   {name:"Legend title",imgPath:""},
                                   {name:"Legend items",imgPath:""},
                                   {name:"Tooltip",imgPath:""},
                                   {name:"Limit",imgPath:""},
                                   {name:"Axis lines",imgPath:""},
                                                                ]
 $scope.openConfigurationDetails = function(button) {
	
}
 
}