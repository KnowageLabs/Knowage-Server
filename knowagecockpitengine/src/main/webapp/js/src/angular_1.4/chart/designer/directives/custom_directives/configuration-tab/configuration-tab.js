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
.directive('configurationTab', function(sbiModule_config,chartDesignerBasePath) {
	return {
		restrict: 'AE',
		replace:true,
		templateUrl: function(){
		      return chartDesignerBasePath + '/directives/custom_directives/configuration-tab/configuration-tab.html'
	      },
		controller: configurationTabControllerFunction
	}

});

function configurationTabControllerFunction(sbiModule_translate,$scope,sbiModule_config,ChartDesignerData,$mdColorPalette,cockpitModule_userPalette,$mdColors, $mdColorUtil){
 $scope.translate = sbiModule_translate;
 $scope.configurationForDisplay = [];
 $scope.selectedConfigurationButton = "";
 $scope.selectedColor = "#FFFFFF";
 $scope.colorObj = {
		 gradient:"",
		 name:"",
		 order:"",
		 value:""
 }
 $scope.customColorObj = {customName:"",customValue:""};
 $scope.colorPickerOptionsCockpit = {
			swatch:true,
			alpha:false,
			pos:"bottom right",
			format:"hex"
	 }

 $scope.colors = [];

 $scope.chartsThatHaveCustomColors= ["bar","pie","line","bubble"];
 var checkForCustomColors = function (){
	if($scope.chartsThatHaveCustomColors.indexOf($scope.selectedChartType) >-1 && !$scope.chartTemplate.CUSTOMCOLORS){
		$scope.chartTemplate.CUSTOMCOLORS = {}
		$scope.chartTemplate.CUSTOMCOLORS.COLOR = [];
	}
 };


 checkForCustomColors();
 //$scope.customColors=$scope.chartTemplate.CUSTOMCOLORS.COLOR
 if(cockpitModule_userPalette &&  cockpitModule_userPalette.colors && cockpitModule_userPalette.colors.length > 0) {
	 $scope.presetColors = cockpitModule_userPalette.colors;
}
else {
	 $scope.presetColors = Object.keys($mdColorPalette);
}

 if($scope.chartTemplate != null && $scope.chartTemplate.COLORPALETTE != "" ){
	 if($scope.chartTemplate.COLORPALETTE.COLOR.constructor === Array) {
		 $scope.colors = $scope.chartTemplate.COLORPALETTE.COLOR;
	 } else if ($scope.chartTemplate.COLORPALETTE.COLOR.constructor === Object) {
		 $scope.colors.push($scope.chartTemplate.COLORPALETTE.COLOR);
	 }
 }else{
	 if($scope.chartTemplate){
		 $scope.chartTemplate.COLORPALETTE = {};
		 $scope.chartTemplate.COLORPALETTE.COLOR = [];
	 }
	 $scope.colors = [];
 }

$scope.addCustomColor = function(){
	checkForCustomColors();
	$scope.chartTemplate.CUSTOMCOLORS.COLOR.push($scope.customColorObj);
	$scope.customColorObj = {customName:"",customValue:""};

}

$scope.deleteCustomColor = function(index) {
	$scope.chartTemplate.CUSTOMCOLORS.COLOR.splice(index,1);

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


 function hex (c) {
	  var s = "0123456789abcdef";
	  var i = parseInt (c);
	  if (i == 0 || isNaN (c))
	    return "00";
	  i = Math.round (Math.min (Math.max (0, i), 255));
	  return s.charAt ((i - i % 16) / 16) + s.charAt (i % 16);
	}

	/* Convert an RGB triplet to a hex string */
	function convertToHex (rgb) {
	  return hex(rgb[0]) + hex(rgb[1]) + hex(rgb[2]);
	}

	/* Remove '#' in color hex string */
	function trim (s) { return (s.charAt(0) == '#') ? s.substring(1, 7) : s }

	/* Convert a hex string to an RGB triplet */
	function convertToRGB (hex) {
	  var color = [];
	  color[0] = parseInt ((trim(hex)).substring (0, 2), 16);
	  color[1] = parseInt ((trim(hex)).substring (2, 4), 16);
	  color[2] = parseInt ((trim(hex)).substring (4, 6), 16);
	  return color;
	}

	function generateColor(colorStart,colorEnd,colorCount){

		// The beginning of your gradient
		var start = convertToRGB (colorStart);

		// The end of your gradient
		var end   = convertToRGB (colorEnd);

		// The number of colors to compute
		var len = colorCount;

		//Alpha blending amount
		var alpha = 0.0;

		var gradientArray = [];

		for (i = 0; i < len; i++) {
			var c = [];
			alpha += (1.0/len);

			c[0] = start[0] * alpha + (1 - alpha) * end[0];
			c[1] = start[1] * alpha + (1 - alpha) * end[1];
			c[2] = start[2] * alpha + (1 - alpha) * end[2];

			gradientArray.push("#"+convertToHex (c).toUpperCase());

		}
		gradientArray[0] = colorEnd;
		return gradientArray;

	}
	$scope.gradientFirstColor = "#FFFFFF";
	$scope.gradientLastColor = "#000000";
	$scope.gradientSteps = 3;



	$scope.makeGradient = function(first,last,step) {
		var firstColor = first;
		var lastColor = last;

		var gradientStrings = generateColor(first,last,step);
		for (var i = 0; i < gradientStrings.length; i++) {
			$scope.addColor(gradientStrings[i]);
		}

		$scope.colors.reverse();
	}


 $scope.templateUrls = ChartDesignerData.getTemplateURLs();

 $scope.dimensionMeasureType = ChartDesignerData.getDimensionMeasureTypeOptions();
 $scope.orientationType = ChartDesignerData.getOrientationTypeOptions();
 $scope.fontFamilyOptions = ChartDesignerData.getFontFamilyOptions();
 $scope.fontSizeOptions = ChartDesignerData.getFontSizeOptions();
 $scope.fontStyleOptions = ChartDesignerData.getFontStyleOptions();
 $scope.fontStyleOptionsNS = ChartDesignerData.getFontStyleOptionsNS();
 $scope.fontAlignOptions = ChartDesignerData.getAlignTypeOptions();
 $scope.tooltipBreadcrumbValueType = ChartDesignerData.getTooltipBreadcrumbValueTypeOptions();
 $scope.positionType = ChartDesignerData.getPositionTypeOptions();
 $scope.verticalAlignType = ChartDesignerData.getVerticalAlignTypeOptions();
 $scope.wordLayoutOptions=ChartDesignerData.getWordLayoutOptions();
 $scope.orderParallelOptions=ChartDesignerData.getParallelOrderOptions();

 $scope.changeAngles = function (item){
	 if(item=="horizontal") {
		 $scope.chartTemplate.minAngle = 0;
		 $scope.chartTemplate.maxAngle = 0;
	 }
	 else if(item=="vertical") {
		 $scope.chartTemplate.minAngle = 90;
		 $scope.chartTemplate.maxAngle = 90;
	 }
	 else if(item=="horizontalAndVertical") {
		 $scope.chartTemplate.minAngle = 0;
		 $scope.chartTemplate.maxAngle = 90;
	 }
 }

 $scope.openConfigurationDetails = function(button) {
	 $scope.selectedConfigurationButton = button;
	 $scope.disableLegendCheckbox = false;

	 if($scope.selectedChartType == 'parallel'){
		 $scope.chartTemplate.LEGEND.show = true;
		 $scope.disableLegendCheckbox = true;
		/* console.log($scope.seriesContainers)
		 if($scope.seriesContainers.length >0){
			 $scope.seriesForParallel = $scope.seriesContainers[0].series;
		 }else{
			 $scope.seriesForParallel = [];
		 }*/

	 }

}


}

