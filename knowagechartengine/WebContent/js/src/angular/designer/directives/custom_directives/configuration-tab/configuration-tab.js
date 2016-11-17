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
 $scope.selectedConfigurationButton = "";
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
 $scope.scatterChartConfiguration = [
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
 $scope.dimensionMeasureType = [
  {name:"px",value:"pixels"},
  {name:"%",value:"percentage"}                          
                             ]
 $scope.orientationType = [
  {name:"Vertical",value:"vertical"},
  {name:"Horizontal",value:"horizontal"}                          
                               ]
 $scope.fontObj = {
	fontFamily:"",
	fontSize:"",
	fontWeight:"",
	fontStyle:"",
	textDecoration:"",
	backgroundColor:"",
	
 };
 $scope.fontFamilyOptions = [
                       	{name:"Arial",value:"Arial"},
                       	{name:"Times New Roman",value:"Times New Roman"},
                       	{name:"Tahoma",value:"Tahoma"},
                       	{name:"Verdana",value:"Verdana"},
                       	{name:"Impact",value:"Impact"},
                       	{name:"Calibri",value:"Calibri"},
                       	{name:"Cambria",value:"Cambria"},
                       	{name:"Georgia",value:"Georgia"},
                       	{name:"Gungsuh",value:"Gungsuh"},
                                                  ]
 
 $scope.fontSizeOptions = [
	{name:"8px",value:"8px"},
	{name:"9px",value:"9px"},
	{name:"10px",value:"10px"},
	{name:"11px",value:"11px"},
	{name:"12px",value:"12px"},
	{name:"14px",value:"14px"},
	{name:"16px",value:"16px"},
	{name:"18px",value:"18px"},
	{name:"20px",value:"20px"},
	{name:"22px",value:"22px"},
	{name:"24px",value:"24px"},
	{name:"26px",value:"26px"},
	{name:"28px",value:"28px"},
	{name:"36px",value:"36px"},
	{name:"48px",value:"48px"},
	{name:"72px",value:"72px"},
                           ]
 
 $scope.fontStyleOptions = [	
                            	{name:"No style",value:""},
                            	{name:"Bold",value:"bold"},
                            	{name:"Normal",value:"normal"},
                            	{name:"Italic",value:"italic"},
                            	{name:"Underline",value:"underline"},
                            	                        ]
 
 $scope.openConfigurationDetails = function(button) {
	 
	 $scope.selectedConfigurationButton = button;
}
 
 
}