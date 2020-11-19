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

angular.module('chartDesignerManager', ['chart-directives','ChartDesignerService', 'chartengine.settings', 'chartBackwardCompatibilityModule'])

.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}]).directive("chartDesigner" ,function(chartDesignerBasePath){
	return {
		templateUrl: chartDesignerBasePath + '/chartDesignerTemplate.html',
		controller: chartDesignerFunction,
		scope:{
	    	   datasetLabel:'=',
	    	   datasetId:'=',
	    	   isCockpitEng:'=',
	    	   localMod:'='
	    	  },
	}
});

function chartDesignerFunction($scope, sbiModule_translate,channelMessaging,sbiModule_util,$scope,sbiModule_config, sbiModule_restServices,StructureTabService, ChartDesignerData,cockpitModule_widgetServices,sbiModule_messaging,sbiModule_logger,$mdToast,$mdDialog,sbiModule_user,$httpParamSerializer) {
	$scope.translate = sbiModule_translate;
	$scope.httpParamSerializer = $httpParamSerializer;
	$scope.selectedChartType = "";
	$scope.selectedTab = {'tab' : 0};
	var urlForDataset="";
	$scope.enterpriseEdition = sbiModule_user.functionalities.indexOf("SeeAdvancedTab")>-1;
	StructureTabService.enterpriseEdition = $scope.enterpriseEdition;
	ChartDesignerData.enterpriseEdition = $scope.enterpriseEdition;
	if($scope.isCockpitEng){
		urlForDataset = "../api/1.0/chart/jsonChartTemplate/usedDataset/"+$scope.datasetId;
	}else{
		urlForDataset = "../api/1.0/chart/jsonChartTemplate/usedDataset";
	}
	sbiModule_restServices.promiseGet(urlForDataset, "")
		.then(function(response) {

			$scope.isRealTimeDataset = response.data;

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
	$scope.disableHtmlElementForChartJs = function() {
		if($scope.libInUse=='chartJs') {
			return false;
		} else return true;
	}

	var showAction = function(text) {
		var toast = $mdToast.simple()
		.content(text)
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top')
		$mdToast.show(toast).then(function(response) {
			if ( response == 'ok' ) {
			}
		});
	}

	$scope.goBackFromDesigner = function() {
		channelMessaging.sendMessage();
	}

	$scope.allMeasures = [];
	$scope.allAttributes = [];

	$scope.categoriesExist = false;

	$scope.categoriesContainer = [];
	$scope.seriesContainer = [];

	$scope.checkChanged = function() {
		$scope.changedArray = [];
		console.log($scope.configurationForm);
		angular.forEach($scope.configurationForm, function(value, key) {
			 if(key[0] == '$'){
				 return;
			 }
			 if(value.$pristine == false){
				 var changedObj = {};

				 changedObj.name = key;
				 changedObj.value = value.$modelValue;

				 $scope.changedArray.push(changedObj);
			 }
			});
		if($scope.changedArray.length == 0){
			return -1;
		}else{
			return $scope.changedArray;
		}
	}

	$scope.$on('attachCategories',function(event,data){

		$scope.attachCategoriesToTemplate();
		if($scope.selectedChartType == 'scatter'){
			$scope.attachSeriesToTemplate();
		}
		if($scope.selectedChartType == 'bubble' ){
			$scope.attachSeriesToTemplateBubble();
		}
		$scope.chartTemplate.COLORPALETTE.COLOR = $scope.colors;
		var chartObj = angular.copy($scope.chartTemplate);
		var chartTemp = {}
		chartTemp.CHART = chartObj;
		angular.copy(chartTemp, $scope.chartTemplate);
	})

	$scope.$on('validateForm',function(event,data){
		cockpitModule_widgetServices.validateForm($scope.chartDesignerForm.$valid)
	})

	$scope.$on('removeUnnecessarySeries',function(event,data){
		if($scope.chartTemplate.CHART.type.toLowerCase()!='bubble'){
   		 var indexX = sbiModule_util.findInArray($scope.chartTemplate.CHART.VALUES.SERIE, 'axis', 'X')
   		 if(indexX>-1) $scope.chartTemplate.CHART.VALUES.SERIE.splice(indexX,1)
   		 var indexZ = sbiModule_util.findInArray($scope.chartTemplate.CHART.VALUES.SERIE, 'axis', 'Z')
   		 if(indexZ>-1) $scope.chartTemplate.CHART.VALUES.SERIE.splice(indexZ,1)
   	  }
	})

	$scope.$on('updateMeasuresWithCF', function(event,data) {
		$scope.getMetadata();if ($scope.getMetadata){
			$scope.numberOfSeriesContainers = 0;
			$scope.getMetadata();
		}
	})

	$scope.refreshJsonTree = function() {
		$scope.attachCategoriesToTemplate(true);

	}

	$scope.attachSeriesToTemplate = function() {
		var valueSeries = $scope.chartTemplate.VALUES.SERIE;
		var totalSeries = $scope.allMeasures;
		for(var i = 0; i < totalSeries.length; i++){
			var index = sbiModule_util.findInArray(valueSeries,'column',totalSeries[i].alias);
			if(index == -1){
				valueSeries.push({axis:"Y",color:"",column:totalSeries[i].alias,groupingFunction:"NONE", name:totalSeries[i].alias,orderType:"",postfixChar:"",
					precision:2,prefixChar:"",scaleFactor:"empty",showAbsValue:"false",showPercentage:false, showValue: "",type:"",fakeSerie:true})
			}
		}
	}

	$scope.attachCategoriesToTemplate = function(advancedTrue) {
		var chartType = $scope.selectedChartType;
		//attach categories to template for chart types that have an array for categories
		if (chartType.toUpperCase() == "SUNBURST" || chartType.toUpperCase() == "WORDCLOUD" ||
				chartType.toUpperCase() == "TREEMAP" || chartType.toUpperCase() == "PARALLEL" ||
				chartType.toUpperCase() == "HEATMAP" || chartType.toUpperCase() == "CHORD"
					||
				chartType.toUpperCase() == "SCATTER"
					) {
			$scope.chartTemplate.VALUES.CATEGORY = angular.copy($scope.categories);

			if(chartType.toUpperCase() == "SCATTER" && !advancedTrue){

				var valueCategories = $scope.chartTemplate.VALUES.CATEGORY
				var totalAttributes = $scope.allAttributes;

				for(var i = 0; i < totalAttributes.length; i++){
					if(valueCategories[0].column != totalAttributes[i].alias){

						valueCategories.push({column:totalAttributes[i].alias,
												groupby:"",
												groupbyNames:"",
												name:totalAttributes[i].alias,
												orderColumn:"",
												orderType:"",
												fakeCategory:true});
					}
				}

			}

		//attach categories to template for chart types that have an object for categories
		} else if (chartType.toUpperCase() != "GAUGE"){
			if($scope.chartTemplate.VALUES.CATEGORY.drillOrder){
				var tempDrillOrder = $scope.chartTemplate.VALUES.CATEGORY.drillOrder;
			}

			$scope.chartTemplate.VALUES.CATEGORY = {
							column:"",
							groupby:"",
							groupbyNames:"",
							name:"",
					};
			if(chartType.toUpperCase()!='BAR' && chartType.toUpperCase()!='LINE') {
				$scope.chartTemplate.VALUES.CATEGORY.orderColumn="";
				$scope.chartTemplate.VALUES.CATEGORY.orderType="";
			}
			for (var i = 0; i < $scope.categories.length; i++) {
				if(i==0){
					$scope.chartTemplate.VALUES.CATEGORY.column = $scope.categories[i].column;
					$scope.chartTemplate.VALUES.CATEGORY.name = $scope.categories[i].name;
					if($scope.chartTemplate.VALUES.CATEGORY.orderColumn==""){
						if(!(tempDrillOrder && chartType.toUpperCase() !="PIE" && chartType.toUpperCase() !="RADAR")){
							$scope.chartTemplate.VALUES.CATEGORY.orderColumn = $scope.categories[i].orderColumn;
						}

					}

					if($scope.chartTemplate.VALUES.CATEGORY.orderType==""){
						if(!(tempDrillOrder && chartType.toUpperCase() !="PIE" && chartType.toUpperCase() !="RADAR")){
							$scope.chartTemplate.VALUES.CATEGORY.orderType = $scope.categories[i].orderType;
						}
					}

				} else {
					if($scope.chartTemplate.VALUES.CATEGORY.groupby==""){
						$scope.chartTemplate.VALUES.CATEGORY.groupby = $scope.categories[i].column;
					} else {
						$scope.chartTemplate.VALUES.CATEGORY.groupby = $scope.chartTemplate.VALUES.CATEGORY.groupby +", "+ $scope.categories[i].column;
					}
					if($scope.chartTemplate.VALUES.CATEGORY.groupbyNames=="") {
						if($scope.categories[i].name!="") {
							$scope.chartTemplate.VALUES.CATEGORY.groupbyNames = $scope.categories[i].column;
						}
					} else {
						if($scope.categories[i].name!="") {
							$scope.chartTemplate.VALUES.CATEGORY.groupbyNames = $scope.chartTemplate.VALUES.CATEGORY.groupbyNames + ", " + $scope.categories[i].column;
						}
					}
				}
			}
			if(tempDrillOrder)
			$scope.chartTemplate.VALUES.CATEGORY.drillOrder = tempDrillOrder;
		}
	}

	$scope.clearStyleTag = function(style) {
		if(style == "default"){
			return "default";
		}else{
			return style;
		}
	}

	var checkChartSettings = function (){
		var f = true;
		if( $scope.selectedChartType.toUpperCase() == "SCATTER" && $scope.chartTemplate.VALUES.SERIE.length>1){

			var allSeries = $scope.chartTemplate.VALUES.SERIE;
			var counter = 0;

			for (var i = 0; i < allSeries.length; i++) {
				if(allSeries[i].groupingFunction=="NONE"){
					counter++
				};
			}
			console.log(counter);
			if(counter<$scope.chartTemplate.VALUES.SERIE.length){
				f  = false;
			}
			if(counter ==0 ) f = true;

		}  else if (($scope.selectedChartType.toUpperCase() == "BAR" || $scope.selectedChartType.toUpperCase() == "LINE") &&  $scope.chartTemplate.VALUES.SERIE.length>0) {
			  var allSeries =  $scope.chartTemplate.VALUES.SERIE;
				var counterlow = 0;
				var counterhigh = 0;
				for (var i = 0; i < allSeries.length; i++) {
					if(allSeries[i].type=="arearangelow"){
						counterlow++
					};
				}
				for (var i = 0; i < allSeries.length; i++) {
					if(allSeries[i].type=="arearangehigh"){
						counterhigh++
					};
				}
				if(counterlow!=counterhigh){
					f=false;
				}
		  }
		return f;
	}

	$scope.attachSeriesToTemplateBubble = function() {
		var valueSeries = $scope.chartTemplate.VALUES.SERIE;
		var totalSeries = $scope.allMeasures;
		for(var i = 0; i < valueSeries.length; i++){
			var index = sbiModule_util.findInArray(totalSeries,'alias',valueSeries[i].bubbleDimension);
			if(index != -1){
				valueSeries.push({axis:"Y",color:"",column:totalSeries[index].alias,groupingFunction:valueSeries[i].groupingFunction, name:totalSeries[index].alias,orderType:"",postfixChar:"",
					precision:2,prefixChar:"",scaleFactor:"empty",showAbsValue:"false",showPercentage:false, showValue: "",type:"",fakeSerie:true})
			}
		}
	}


}