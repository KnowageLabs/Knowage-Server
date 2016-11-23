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

angular.module('chart-tab', [])
.directive('chartTab', function(sbiModule_config) {
	return {
		restrict: 'AE',
		replace: true,
		templateUrl: function(){
		      return sbiModule_config.contextName + '/js/src/angular/designer/directives/custom_directives/chart-tab/chart-tab.html' 
	      },   
		controller: chartTabControllerFunction
	}
		
});

function chartTabControllerFunction($scope,sbiModule_translate,sbiModule_restServices,sbiModule_messaging,ChartDesignerData){
	$scope.translate = sbiModule_translate;
	$scope.datasetLabel = datasetLabel;
	var setConfigurationButtons = function(type) {
		
		switch (type) {
		case 'parallel':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'sunburst':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'scatter':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'treemap':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'wordcloud':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'gauge':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'line':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'heatmap':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'radar':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'bar':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'pie':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;
		case 'chord':
			$scope.configurationForDisplay = ChartDesignerData.getChartConfigurationOptions(type);
			break;	
		default:
			break;
		}
		
}
	var setFontProps = function(array) {
		
		console.log(array);
		var style="";
		var type="";
		for (var i = 0; i < array.length; i++) {
			style = array[i].array;
			type =	array[i].type;
			var res = style.split(";");
			res.pop();
			var formated = [];
			for (var j = 0; j < res.length; j++) {
				var obj = {};
				var temp = res[j].split(":")
				obj.name = temp[0];
				obj.value = temp[1];
				formated.push(obj);
			}	
			if(type == 'chart'){
				for (var k = 0; k < formated.length; k++) {
					if($scope.fontObj.hasOwnProperty(formated[k].name)){
						$scope.fontObj[formated[k].name] = formated[k].value;
					}
				}
				}
				if(type == 'title'){
					for (var k = 0; k < formated.length; k++) {
						if($scope.titleFontObj.hasOwnProperty(formated[k].name)){
							$scope.titleFontObj[formated[k].name] = formated[k].value;
						}
					}
					}
				if(type == 'subtitle'){
					for (var k = 0; k < formated.length; k++) {
						if($scope.subtitleFontObj.hasOwnProperty(formated[k].name)){
							$scope.subtitleFontObj[formated[k].name] = formated[k].value;
						}
					}
					}
				if(type == 'nodata'){
					for (var k = 0; k < formated.length; k++) {
						if($scope.nodataFontObj.hasOwnProperty(formated[k].name)){
							$scope.nodataFontObj[formated[k].name] = formated[k].value;
						}
					}
					}
				if(type == 'legendtitle'){
					for (var k = 0; k < formated.length; k++) {
						if($scope.legendObj.title.hasOwnProperty(formated[k].name)){
							$scope.legendObj.title[formated[k].name] = formated[k].value;
						}
					}
					}
				if(type == 'legend'){
					for (var k = 0; k < formated.length; k++) {
						if($scope.legendObj.hasOwnProperty(formated[k].name)){
							$scope.legendObj[formated[k].name] = formated[k].value;
						}
					}
					$scope.legendObj.borderWidth = parseInt($scope.legendObj.borderWidth);
					}
		}
	}	

	sbiModule_restServices.promiseGet("../api/1.0/pages/types", "")
	.then(function(response) {
		$scope.chartTypes = response.data.types;
		for (var i = 0; i < $scope.chartTypes.length; i++) {
			
			if ($scope.chartTemplate) {
				
				if($scope.chartTypes[i].toUpperCase()==$scope.chartTemplate.type.toUpperCase()){
					$scope.selectedChartType = $scope.chartTypes[i];
					setConfigurationButtons($scope.selectedChartType);
				}	
			}
			else {
				$scope.selectedChartType = 'bar';
				setConfigurationButtons($scope.selectedChartType);
			}
			
		}		
		
		if ($scope.chartTemplate) {
		var cssArray = ChartDesignerData.getCssStyles($scope.chartTemplate);
		$scope.selectedConfigurationButton = "";
		setFontProps(cssArray);
		
		}
		
	}, function(response) {
		
		var message = "";
		
		if (response.status==500) {
			message = response.statusText;
		}
		else {
			message = response.data.errors[0].message;
		}
		
		sbiModule_messaging.showErrorMessage(message, 'Error');
		
	});
	
	sbiModule_restServices.promiseGet("../api/style", "")
	.then(function(response) {
		$scope.chartStyles = response.data;
		getDefaultStyleForEmpty();
		
	}, function(response) {
		
		var message = "";
		
		if (response.status==500) {
			message = response.statusText;
		}
		else {
			message = response.data.errors[0].message;
		}
		
		sbiModule_messaging.showErrorMessage(message, 'Error');
		
	});
	
	var getDefaultStyleForEmpty = function() {
		$scope.styleTemplate = "";
		for (var i = 0; i < $scope.chartStyles.length; i++) {
			if($scope.chartStyles[i].STYLE.isDefault == 'true'){
				$scope.styleTemplate = $scope.chartStyles[i].STYLE.TEMPLATE;
				if($scope.chartTemplate == null){
					$scope.chartTemplate = $scope.styleTemplate.generic.CHART;
					console.log($scope.chartTemplate)
				}
				
			}else{
				if($scope.chartStyles[i].STYLE.name == 'sfnas'){
					$scope.styleTemplate = $scope.chartStyles[i].STYLE.TEMPLATE;
					if($scope.chartTemplate == null){
						$scope.chartTemplate = $scope.styleTemplate.generic.CHART;
						console.log($scope.chartTemplate);
					}
				}
			}
		}
	}
	$scope.filterStyles = function(item) {
		return item.STYLE.name != 'sfnas'
	}
	$scope.selectChartType = function(chart) {
		$scope.selectedChartType = chart;
		setConfigurationButtons($scope.selectedChartType);
		$scope.selectedConfigurationButton = "";
		
	}
		
}