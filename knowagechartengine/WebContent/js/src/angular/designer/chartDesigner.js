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

app.controller("ChartDesignerController", ["sbiModule_translate","$scope","sbiModule_config", "sbiModule_restServices", ChartDesignerFunction]);

function ChartDesignerFunction(sbiModule_translate,$scope,sbiModule_config, sbiModule_restServices ) {
	
	$scope.translate = sbiModule_translate;
	$scope.previewButtonEnabled = false;
	$scope.selectedChartType = "";
	
	$scope.saveChartTemplate = function() {
		console.log("lll")
		console.log(template)
		if($scope.chartTemplate.hasOwnProperty("COLORPALETTE")){
			var color = $scope.chartTemplate.COLORPALETTE.COLOR;
			for (var i = 0; i < color.length; i++) {
				if(color[i].hasOwnProperty("$$hashKey")){
					delete color[i].$$hashKey;
				}
			}
		}
		
		var temp = {"CHART":$scope.chartTemplate}
		sbiModule_restServices.promisePost('../api/1.0/template/save','', 'jsonTemplate='+angular.toJson(temp)+'&docLabel='+docLabel+'&userId='+userId, {
			transformRequest:angular.identity,
			headers:{'Content-Type': ' application/x-www-form-urlencoded'}
		})
		.then(function(response) {
			console.log("[POST]: SUCCESS!");
			
		}, function(response) {
			
		});
	}
	
	$scope.goBackFromDesigner = function() {
		 var url= sbiModule_config.protocol+"://"+sbiModule_config.host+":"+sbiModule_config.port+sbiModule_config.externalBasePath;
		 url+= "/servlet/AdapterHTTP?PAGE=DetailBIObjectPage&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=FALSE&MESSAGEDET=DETAIL_SELECT&OBJECT_ID="+docId;
		 window.parent.location.href=url;
	}
	
	// The chart template (beneath the CHART tag, i.e. property)
	
	var parseTemplateforBinding = function(template) {

		if( Object.keys(template).length != 0){
			
		Object.keys(template.CHART).forEach(function (key) {
			
			
		    if( template.CHART[key] != ''   && !isNaN(template.CHART[key])){
		    	template.CHART[key] = parseInt(template.CHART[key]);
		    }
		    if(template.CHART[key] === 'true' || template.CHART[key] === 'false'){
		    	
		    	template.CHART[key] = JSON.parse(template.CHART[key])
		    }
		});
		if(template.CHART.LEGEND != null){
		
		Object.keys(template.CHART.LEGEND).forEach(function (key) {
			
			
		    if( template.CHART.LEGEND[key] != ''   && !isNaN(template.CHART.LEGEND[key])){
		    	template.CHART.LEGEND[key] = parseInt(template.CHART.LEGEND[key]);
		    }
		    if(template.CHART.LEGEND[key] === 'true' || template.CHART.LEGEND[key] === 'false'){
		    	
		    	template.CHART.LEGEND[key] = JSON.parse(template.CHART.LEGEND[key])
		    }
		});
		}
		if(template.CHART.PANE != null){
			
			Object.keys(template.CHART.PANE).forEach(function (key) {
				
				
			    if( template.CHART.PANE[key] != ''   && !isNaN(template.CHART.PANE[key])){
			    	template.CHART.PANE[key] = parseInt(template.CHART.PANE[key]);
			    }
			});	
		}
		
		if(template.CHART.LIMIT != null){
			
			Object.keys(template.CHART.LIMIT).forEach(function (key) {
				
				if(template.CHART.LIMIT[key] === 'true' || template.CHART.LIMIT[key] === 'false'){
			    	
					template.CHART.LIMIT[key] = JSON.parse(template.CHART.LIMIT[key])
			    }
			
			});	
		}
		
		
		var allSeriesItems = template.CHART.VALUES.SERIE;
		
		for (i=0; i<allSeriesItems.length; i++) {
			
			Object.keys(allSeriesItems[i]).forEach(function (key) {				
				
				// SERIES CONFIGURATION
				if( allSeriesItems[i][key] != ''   && !isNaN(allSeriesItems[i][key])){
					allSeriesItems[i][key] = parseInt(allSeriesItems[i][key]);
			    }
			    if(allSeriesItems[i][key] === 'true' || allSeriesItems[i][key] === 'false'){			    	
			    	allSeriesItems[i][key] = JSON.parse(allSeriesItems[i][key])
			    }
			    
			    // SERIES TOOLTIP
			    if( allSeriesItems[i].TOOLTIP[key] != ''   && !isNaN(allSeriesItems[i].TOOLTIP[key])){
			    	allSeriesItems[i].TOOLTIP[key] = parseInt(allSeriesItems[i].TOOLTIP[key]);
			    }
			    if(allSeriesItems[i].TOOLTIP[key] === 'true' || allSeriesItems[i].TOOLTIP[key] === 'false'){			    	
			    	allSeriesItems[i].TOOLTIP[key] = JSON.parse(allSeriesItems[i].TOOLTIP[key])
			    }
			    
			});
			
		}
		
		var allAxes = template.CHART.AXES_LIST.AXIS;
		
		for (i=0; i<allAxes.length; i++) {
			
			Object.keys(allAxes[i]).forEach(function (key) {				
				
				// AXIS CONFIGURATION
				if( allAxes[i][key] != '' && !isNaN(allAxes[i][key])){
					allAxes[i][key] = parseInt(allAxes[i][key]);
			    }
			    if(allAxes[i][key] === 'true' || allAxes[i][key] === 'false'){			    	
			    	allAxes[i][key] = JSON.parse(allAxes[i][key])
			    }			    
			    
			});
			
		}
		
		if(template.CHART.AXES_LIST != null){
			
			Object.keys(template.CHART.AXES_LIST).forEach(function (key) {
				
				
			    if( template.CHART.AXES_LIST[key] != ''   && !isNaN(template.CHART.AXES_LIST[key])){
			    	template.CHART.AXES_LIST[key] = parseInt(template.CHART.AXES_LIST[key]);
			    }
			    if(template.CHART.AXES_LIST[key] === 'true' || template.CHART.AXES_LIST[key] === 'false'){
			    	
			    	template.CHART.AXES_LIST[key] = JSON.parse(template.CHART.AXES_LIST[key])
			    }
			});	
		}
		
			return template.CHART;
		}else{
			return null;
		}
	}

	$scope.chartTemplate = parseTemplateforBinding(angular.fromJson(template));
	console.log("chart template: ",$scope.chartTemplate);
	
	
	// Needed for the preview of the chart (calling the Highcharts exporter
	$scope.exporterContextName = exporterContextName;
	
	$scope.allMeasures = [];
	$scope.allAttributes = [];
	
	$scope.categoriesExist = false;
	
	$scope.categoriesContainer = [];
	$scope.seriesContainer = [];
	
	$scope.testChart = function() {
		// add $scope.scaterAxis back to template
		// reformat styles
		console.log($scope.chartTemplate);
		$scope.chartTemplate.isCockpitEngine = isCockpit; // TODO add on save
		$scope.chartTemplate.type = $scope.selectedChartType.toUpperCase();
		$scope.chartTemplate.COLORPALETTE.COLOR = $scope.colors;
		console.log($scope.styleTemplate);
		console.log($scope.chartTemplate);
	}
	
}