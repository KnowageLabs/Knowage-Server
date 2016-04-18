/*
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

/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */
angular.module('geoModule')
.directive('geoMapMenu',function(sbiModule_config){
	return{
		restrict: "E",
		templateUrl: sbiModule_config.contextName + '/js/src/angular_1.x/geo/geoMapMenu/templates/geoMapMenuTemplate.jspf',
		controller: geoMapMenuControllerFunction,
//		require: "^geoMap",
		scope: {
			id:"@"
		}
	}
});

function geoMapMenuControllerFunction(
		geoModule_layerServices, geoModule_dataset, $scope, $timeout, 
		$mdDialog, $map, geoModule_template, geoModule_filters, 
		geoModule_indicators, geo_interaction ,sbiModule_translate) {

	$scope.template = geoModule_template;
	$scope.dataset = geoModule_dataset;
	$scope.filters = geoModule_filters;
	$scope.indicators = geoModule_indicators;
	$scope.selectModeInteraction = geo_interaction;
	$scope.translate=sbiModule_translate;
	$scope.firstCallInteraction = true;
	$scope.openRigthMenu = false;
	$scope.measureInsert=0;
	$scope.selectMisure="kilometers";
		//inizializzo il valore dell'indicator
	for(var i=0;i<geoModule_indicators.length;i++){
		if(geoModule_indicators[i].header==geoModule_template.selectedIndicator.header){
			geoModule_template.selectedIndicator=geoModule_indicators[i];
			break;
		}
	}
	
	
	$scope.analysisTypeList = [
	                           {label:sbiModule_translate.load("gisengine.rigthMapMenu.analysisType.choropleth"),type:"choropleth",img:"fa  fa-area-chart "},
	                           {label:sbiModule_translate.load("gisengine.rigthMapMenu.analysisType.proportionalSymbol"),type:"proportionalSymbol",img:"fa fa-circle"},
	                           {label:sbiModule_translate.load("gisengine.rigthMapMenu.analysisType.chart"),type:"chart",img:"fa fa-bar-chart"}
	                           ];
	$scope.selectModeTypeList = [
	                             {label:sbiModule_translate.load("gisengine.rigthMapMenu.selectModeType.identify"), type:"identify"},
	                             {label:sbiModule_translate.load("gisengine.rigthMapMenu.selectModeType.cross"), type:"cross"}
	                             ];



	$scope.filterTypes = [
	                      {label: sbiModule_translate.load("gisengine.rigthMapMenu.spatialFilterType.near"), type:"near"},	
	                      {label: sbiModule_translate.load("gisengine.rigthMapMenu.spatialFilterType.intersect"), type:"intersect"},	
	                      {label: sbiModule_translate.load("gisengine.rigthMapMenu.spatialFilterType.inside"), type:"inside"}
	                      ];
    
	$scope.typeOfMisure = [
	                       {label: "m",type:"miglia"},
	                       {label: "km",type:"kilometers"},
	                       
	                       ];
	
	$scope.setSelectedFilterType = function(type) {

		geo_interaction.selectedFilterType=type;
		if(type=="near"){
			
		}else{
			geoModule_layerServices.setInteraction();
			
			if ($scope.$root.$$phase != '$apply') {
				$scope.$apply();
			}
		}
		
	};
	$scope.save = function(select,num,type){
		$scope.selectMisure=select;
		$scope.measureInsert=2*num;
		if($scope.selectMisure=="miglia"){
			//conversion in km
			$scope.measureInsert=$scope.measureInsert*1.852;
		}
		geo_interaction.selectedFilterType=type;
		geoModule_layerServices.measure=$scope.measureInsert*500;
		geoModule_layerServices.setInteraction();
	}
	$scope.setDefaultDraw = function(){
		if($scope.firstCallInteraction){
			geoModule_layerServices.setInteraction();
			$scope.firstCallInteraction=false;
		}

	}
	$scope.isCrossRadioButtonDisabled = function(selectModeType) {
		var isCross = (selectModeType.toLowerCase() == "cross");
		var isCrossNavigationInTemplate = (geoModule_template.crossNavigation == true);

		var isCrossRadioButtonOptionDisabled = (isCross && !isCrossNavigationInTemplate);

		return isCrossRadioButtonOptionDisabled;
	};

	if(!$scope.template.hasOwnProperty('analysisType')){
		$scope.template.analysisType = $scope.analysisTypeList[1].type;
	}

	$scope.updateMap = function(){
		$timeout(function() {
			geoModule_layerServices.updateTemplateLayer();
		}, 0);
	};

	$scope.indicatorIsSelected = function(item){
		return angular.equals(geoModule_template.selectedIndicator, item);
	};



	$scope.toggleIndicator = function (item){
		var index = $scope.indexInList(item, geoModule_template.selectedMultiIndicator);

		if(index == -1){
			geoModule_template.selectedMultiIndicator.push(item);
		}else{
			geoModule_template.selectedMultiIndicator.splice(index,1);
		}
		$scope.updateMap();
	};

	$scope.exist= function(item){
		return  $scope.indexInList(item, geoModule_template.selectedMultiIndicator)>-1;
	};

	$scope.indexInList = function(item, list) {
		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.name==item.name){
				return i;
			}
		}
		return -1;
	};  
	


};

