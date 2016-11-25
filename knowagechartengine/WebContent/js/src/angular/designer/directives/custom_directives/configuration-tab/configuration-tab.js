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

function configurationTabControllerFunction(sbiModule_translate,$scope,sbiModule_config,ChartDesignerData,$mdColorPalette,$mdColors, $mdColorUtil){

 $scope.translate = sbiModule_translate;
 $scope.configurationForDisplay = [];
 $scope.selectedConfigurationButton = "";
 $scope.fontObj = {
			fontFamily:"",
			fontSize:"",
			fontWeight:"",
			backgroundColor:"",
		 };
 $scope.titleFontObj = {
			fontFamily:"",
			fontSize:"",
			fontWeight:"",
			color:"",
			align:""
		 };
 $scope.subtitleFontObj = {
		 	fontFamily:"",
			fontSize:"",
			fontWeight:"",
			color:"",
			align:""
		 };
 
 $scope.nodataFontObj = {
		 	fontFamily:"",
			fontSize:"",
			fontWeight:"",
			color:"",
			align:""
		 };
 $scope.legendObj ={
	title:{
		fontFamily:"",
		fontSize:"",
		fontWeight:"",
		color:"",
		align:""
	},
	fontFamily:"",
	fontSize:"",
	fontWeight:"",
	color:"",
	align:"",
	borderWidth:"",
	backgroundColor:""	 
 }
 $scope.selectedColor = "#FFFFFF";
 $scope.colorObj = {
		 gradient:"",
		 name:"",
		 order:"",
		 value:""
 }
 $scope.colors = [];
 $scope.presetColors = Object.keys($mdColorPalette);
 if($scope.chartTemplate != null && $scope.chartTemplate.COLORPALETTE.COLOR.length > 0){
	 $scope.colors = $scope.chartTemplate.COLORPALETTE.COLOR;
 }
 
 
 
 $scope.addColor = function(color) {
	 var value ="";
	 var name = "";
	 var order = $scope.colors.length +1;
	if(!color.startsWith("#")){
		value= $mdColorUtil.rgbaToHex($mdColors.getThemeColor(color)).toLowerCase();
		name = value.substring(1);
	}else{
		value = color.toLowerCase();
		name = value.substring(1);
	}
	$scope.colorObj.name = name;
	$scope.colorObj.value = value;
	$scope.colorObj.order = order.toString();
	$scope.colors.push($scope.colorObj);
	$scope.colorObj = {
			 gradient:"",
			 name:"",
			 order:"",
			 value:""
	 }
}
 
 $scope.moveColorUp = function(item) {
	 	var index = $scope.colors.indexOf(item);
		var nextIndex = index-1;
		var temp = $scope.colors[index];
		$scope.colors[index] = $scope.colors[nextIndex];
		$scope.colors[nextIndex] = temp;
		for (var i = 0; i < $scope.colors.length; i++) {
			 $scope.colors[i].order = (i+1).toString();
		}
		
		
}
 $scope.moveColorDown = function(item) {
	 	var index = $scope.colors.indexOf(item);
		var nextIndex = index+1;
		var temp = $scope.colors[index];
		$scope.colors[index] = $scope.colors[nextIndex];
		$scope.colors[nextIndex] = temp;
		for (var i = 0; i < $scope.colors.length; i++) {
			 $scope.colors[i].order = (i+1).toString();
		}

 }
 $scope.deleteColor = function(item) {
	 var index = $scope.colors.indexOf(item);
	 var nextIndex = index+1;
	 if(index == $scope.colors.length-1){
		$scope.colors.splice(index, 1);
	 }else{
		 $scope.colors.splice(index, 1);
		 for (var i = 0; i < $scope.colors.length; i++) {
			 $scope.colors[i].order = (i+1).toString();
		}
	 }
 }
 $scope.checkColor = function(color) {

	 var rgb = parseInt(color, 16);   // convert rrggbb to decimal
	 var r = (rgb >> 16) & 0xff;  // extract red
	 var g = (rgb >>  8) & 0xff;  // extract green
	 var b = (rgb >>  0) & 0xff;  // extract blue

	 var luma = 0.2126 * r + 0.7152 * g + 0.0722 * b; // per ITU-R BT.709

	 if (luma < 100) {
	     return true;
	 }else{
		 return false;
	 } 
	 
}
 
 $scope.limitParallelObj = {
		 maxNumberOfLines:"",
		 serieFilterColumn:"",
		 orderTopMinBottomMax:"",
 }
 
 $scope.axesListlObj = {
		 axisColNamePadd:"",
		 brushWidth:"",
		 axisColor:"",
		 brushColor:"",
 }
 
 $scope.templateUrls = ChartDesignerData.getTemplateURLs();
 
 $scope.dimensionMeasureType = ChartDesignerData.getDimensionMeasureTypeOptions();
 $scope.orientationType = ChartDesignerData.getOrientationTypeOptions();
 $scope.fontFamilyOptions = ChartDesignerData.getFontFamilyOptions();
 $scope.fontSizeOptions = ChartDesignerData.getFontSizeOptions();
 $scope.fontStyleOptions = ChartDesignerData.getFontStyleOptions();
 $scope.fontAlignOptions = ChartDesignerData.getAlignTypeOptions();
 $scope.tooltipBreadcrumbValueType = ChartDesignerData.getTooltipBreadcrumbValueTypeOptions();
 $scope.positionType = ChartDesignerData.getPositionTypeOptions();
 $scope.verticalAlignType = ChartDesignerData.getVerticalAlignTypeOptions();
 $scope.wordLayoutOptions=ChartDesignerData.getWordLayoutOptions();
 $scope.orderParallelOptions=ChartDesignerData.getParallelOrderOptions();
 
 
 $scope.openConfigurationDetails = function(button) {
	 $scope.selectedConfigurationButton = button;
	 $scope.disableLegendCheckbox = false;
	 if($scope.selectedChartType == 'heatmap'){
		 $scope.chartTemplate.LEGEND.show = true;
		 $scope.disableLegendCheckbox = true;
	 }
	 if($scope.selectedChartType == 'scatter'){
		 $scope.axisList = $scope.chartTemplate.AXES_LIST.AXIS;
		 $scope.scaterAxis ={};
		 for (var i = 0; i < $scope.axisList.length; i++) {
			if ($scope.axisList[i].type == 'Category') {
				$scope.scaterAxis.endOnTick = $scope.axisList[i].endOnTick;
				$scope.scaterAxis.startOnTick = $scope.axisList[i].startOnTick;
				$scope.scaterAxis.showLastLabel = $scope.axisList[i].showLastLabel;
				
			}
		}
	 }
	 if($scope.selectedChartType == 'parallel'){
		 $scope.chartTemplate.LEGEND.show = true;
		 $scope.disableLegendCheckbox = true;
		 console.log($scope.seriesContainers)
		 if($scope.seriesContainers.length >0){
			 $scope.seriesForParallel = $scope.seriesContainers[0].series;
		 }else{
			 $scope.seriesForParallel = [];
		 }
		 
	 }
	 
}
 
 
}

